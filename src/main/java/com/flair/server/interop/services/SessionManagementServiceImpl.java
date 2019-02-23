package com.flair.server.interop.services;

import javax.servlet.http.HttpSession;

import com.flair.server.interop.session.SessionManager;
import com.flair.shared.interop.AuthToken;
import com.flair.shared.interop.ServerAuthenticationToken;
import com.flair.shared.interop.services.SessionManagementService;

public class SessionManagementServiceImpl extends AbstractRemoteService implements SessionManagementService
{
	@Override
	public AuthToken beginSession() 
	{
		// create and init session data
		HttpSession session = getThreadLocalRequest().getSession(true);		//returns current session associated with HTTPServerletRequest object
		return SessionManager.get().addSession(session);					//adds request session to session manager
	}

	@Override
	public void endSession(AuthToken token) 
	{
		ServerAuthenticationToken authToken = validateToken(token);
		SessionManager.get().removeSession(authToken);
	}

}
