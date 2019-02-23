package com.flair.server.interop;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.interop.AbstractMesageSender;
import com.flair.shared.interop.AuthToken;
import com.flair.shared.interop.MessagePipelineType;
import com.flair.shared.interop.ServerAuthenticationToken;
import com.flair.shared.interop.ServerMessage;

/*
 * Endpoint for message handling requests
 */
public class MessagePipeline
{
	private static MessagePipeline SINGLETON = null;

	public static MessagePipeline get() 
	{
		if (SINGLETON == null)
		{
			synchronized (MessagePipeline.class)
			{
				if (SINGLETON == null)
					SINGLETON = new MessagePipeline();
			}
		}

		return SINGLETON;
	}
	
	private final MessagePipelineType			type;
	private final Map<ServerAuthenticationToken, PullMessageSender>		token2Pull;
	private final Map<PullMessageSender, ServerAuthenticationToken>		pull2Token;
	
	/*
	 * Pull implementation of a message sender
	 */
	class PullMessageSender implements AbstractMesageSender, PullMessageQueue
	{
		private final Queue<ServerMessage>		messageQueue;		//queue of messages sent to server 
		private boolean							registered;
		
		public PullMessageSender() 
		{
			messageQueue = new ArrayDeque<>();
			registered = false;
		}
		/**
		 * Adds a message to the queue of messages on the server
		 * @param msg ServerMessage object that represents a message from the server to the client
		 */
		private synchronized void doEnqueue(ServerMessage msg) {	
			messageQueue.add(msg);
		}
		/**
		 * performs a dequeue operation on the message queue for a specified number of messages
		 * if count is an invalid number, all messages on the queue will be dequeued
		 * @param count the number of messages to dequeu
		 * @return returns a ServerMessage array containing all of the messages that were dequeued
		 */
		private synchronized ServerMessage[] doDequeue(int count)	
		{
			if (count <= 0)
				count = messageQueue.size();
			else if (count > messageQueue.size())
				count = messageQueue.size();
			
			ServerMessage[] out = new ServerMessage[count];
			for (int i = 0; i < count; i++)
				out[i] = messageQueue.poll();
			
			return out;
		}
		/**
		 * Gets the size of the server message queue
		 * @return integer representing the size of the server message queue
		 */
		private synchronized int doSize() {
			return messageQueue.size();
		}
		/**
		 * Associates this PullMessageSender instance with a ServerAuthenticationToken, this PullMessageSender 
		 * instance is now registered with a token
		 * @param token Authentication token associated with this instance of the PullMessageSender
		 */
		private synchronized void doOpen(ServerAuthenticationToken token)
		{
			registerPullMessageSender(token, this);
			registered = true;
		}
		/**
		 * clears the message queue and deregisters this instance of the PullMessageSender 
		 */
		private synchronized void doClose()
		{
			if (messageQueue.isEmpty() == false)
			{
				ServerLogger.get().warn("PullMessageSender has pending messages at the time of shutdown. Message count: "
						+ messageQueue.size());
			}
			
			messageQueue.clear();
			
			deregisterPullMessageSender(this);
		}
		/**
		 * clears the message queue
		 */
		private synchronized void doClear() {
			messageQueue.clear();
		}
		/**
		 * checks to see if current instance of PullMessageSender is registered
		 * @return true if registered, false if not
		 */
		private synchronized boolean doIsOpen() {
			return registered;
		}
		@Override
		public MessagePipelineType getType() {
			return MessagePipelineType.PULL;
		}
		@Override
		public void send(ServerMessage msg) 
		{
			if (registered == false)
				throw new IllegalStateException("Sender not open");
			
			doEnqueue(msg);
		}
		@Override
		public int getMessageCount() {
			return doSize();
		}
		@Override
		public ServerMessage[] dequeue(int count) {
			return doDequeue(count);
		}
		@Override
		public ServerMessage[] dequeueAll() {
			return doDequeue(-1);
		}

		@Override
		public void open(AuthToken receiverToken) {
			doOpen((ServerAuthenticationToken)receiverToken);
		}
		
		@Override
		public void close() {
			doClose();
		}

		@Override
		public boolean isOpen() {
			return doIsOpen();
		}

		@Override
		public void clearPendingMessages() {
			doClear();
		}
	}

	
	private MessagePipeline() 
	{
		type = MessagePipelineType.PULL;
		token2Pull = new HashMap<>();
		pull2Token = new HashMap<>();
	}
	/**
	 * Creates a two way association between an Auth Token and a PullMessageSender
	 * @param token ServerAuthenticationToken object to be associated with the message sender
	 * @param sender PullMessageSender object to be associated with the ServerAuthenticationToken
	 */
	private synchronized void registerPullMessageSender(ServerAuthenticationToken token, PullMessageSender sender) 
	{
		if (token2Pull.containsKey(token))
			throw new IllegalArgumentException("Multiple senders for session token " + token.getUuid());
			
		token2Pull.put(token, sender);
		pull2Token.put(sender, token);
	}
	/**
	 * Deletes the registered PullMessageSender and its associated token 
	 * @param sender PullMessageSender object to be deleted 
	 */
	private synchronized void deregisterPullMessageSender(PullMessageSender sender)
	{
		if (pull2Token.containsKey(sender) == false)
			throw new IllegalArgumentException("Sender not registered");
		
		ServerAuthenticationToken token = pull2Token.get(sender);
		
		token2Pull.remove(sender);
		pull2Token.remove(token);
	}
	/**
	 * Creates a new pull message sender
	 * @return PullMessageSender object
	 */
	public synchronized AbstractMesageSender createSender()
	{
		switch (type)
		{
		case PULL:
			return new PullMessageSender();
		case PUSH:
			throw new IllegalArgumentException("Sender type " + type + " not implemented");
		default:
			return null;
		}
	}
	/**
	 * Pulls all queued messages off of the queue and returns an array of the dequeued messages
	 * @param token ServerAuthenticationToken needed to get correct PullMessageSender instance
	 * @return Array of ServerMesssage objects that were removed from teh queue
	 */
	public synchronized ServerMessage[] getQueuedMessages(ServerAuthenticationToken token)
	{
		PullMessageSender sender = token2Pull.get(token);
		if (sender == null)
			throw new IllegalArgumentException("Session token has no message sender. ID: " + token.getUuid());
		
		// return all the queued messages
		return sender.dequeueAll();
	}
}
