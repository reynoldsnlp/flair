package com.flair.server.interop;

import com.flair.shared.interop.ServerMessage;

/*
 * Provides an interface for retrieving messages from the message queue
 */
public interface PullMessageQueue
{
	/**
	 * gets the number of messages in the queue
	 * @return integer representing the number of messages in the queue
	 */
	public int					getMessageCount();				// number of messages in the queue
	/**
	 * removes the corresponding num of messages from the head
	 * @param count number of messages to be removed
	 * @return array of the all the messages that were dequeued
	 */
	public ServerMessage[]		dequeue(int count);				// removes the corresponding num of messages from the head
	/**
	 * removes all the messages in the queue
	 * @return array of the all the messages that were dequeued
	 */
	public ServerMessage[]		dequeueAll();					// removes all the messages in the queue
}
