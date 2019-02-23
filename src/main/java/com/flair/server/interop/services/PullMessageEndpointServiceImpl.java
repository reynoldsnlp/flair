package com.flair.server.interop.services;

import com.flair.server.interop.MessagePipeline;
import com.flair.shared.interop.AuthToken;
import com.flair.shared.interop.ServerAuthenticationToken;
import com.flair.shared.interop.ServerMessage;
import com.flair.shared.interop.services.PullMessageEndpointService;

/**
 * Server side implementation of the PullMessageEndpointService class
 */
public class PullMessageEndpointServiceImpl extends AbstractRemoteService implements PullMessageEndpointService
{
	@Override
	public ServerMessage[] dequeueMessages(AuthToken token) 
	{
		ServerAuthenticationToken authToken = validateToken(token);		//calls AbstractRemoteService validateToken method which validates the token associated with a httpRequest Session
		return MessagePipeline.get().getQueuedMessages(authToken);		//returns all of the queued messages on the message pipeline
	}

}
