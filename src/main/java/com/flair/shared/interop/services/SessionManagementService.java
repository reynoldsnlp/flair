package com.flair.shared.interop.services;

import com.flair.shared.interop.AuthToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/*
 * Interface for session management service 
 */
@RemoteServiceRelativePath("SessionManagement")
public interface SessionManagementService extends RemoteService
{
	/**
	 * Invoked before all other server communications. Initiates a session 
	 * @return Client's AuthToken
	 */
	public AuthToken		beginSession();					// invoked before all other server communications, returns the client's token
	/**
	 * Invoked when the client ends their session, ends a session 
	 * @param token Client's AuthToken 
	 */
	public void				endSession(AuthToken token);	// invoked when the client ends their session
}
