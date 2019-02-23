package com.flair.shared.interop;

/*
 * Sends messages to the client
 */
public interface AbstractMesageSender
{
	/**
	 * Returns the type of MessagePipeline Object
	 * @return MessagePipelineType of MessagePipelineObject
	 */
	public MessagePipelineType			getType();
	
	/**
	 * Opens a message pipeline, must be called before an messages are sent
	 * @param recieverToken AuthToken associated with message pipeline 
	 */
	public void							open(AuthToken receiverToken);			// called before any messages are sent
	/**
	 * Adds a message to the pipeline to be sent across the pipeline
	 * @param msg message to be sent
	 */
	public void							send(ServerMessage msg);
	/**
	 * Closes Message pipeline, called when pipeline is no longer needed
	 */
	public void							close();								// called when the sender is no longer needed
	/**
	 * Checks to see if message pipeline is open. Returns true if open, false if not open
	 * @return open status of pipeline
	 */
	public boolean						isOpen();
	/**
	 * Removes any unread messages in the message queue
	 */
	public void							clearPendingMessages();					// removes any unread messages in the message queue
}
