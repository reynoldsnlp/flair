package com.flair.server.interop.services;

import java.util.ArrayList;

import com.flair.server.interop.session.SessionManager;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;
import com.flair.shared.interop.AuthToken;
import com.flair.shared.interop.ServerAuthenticationToken;
import com.flair.shared.interop.services.WebRankerService;

/**
 * Implementation of the WebRankerService interface
 */
public class WebRankerServiceImpl extends AbstractRemoteService implements WebRankerService
{
	@Override		//initiates searchCrawlParse operation
	public void beginWebSearch(AuthToken token,
							Language lang,
							String query,
							boolean useRestrictedDomains,
							int numResults,
							ArrayList<String> keywords)
	{
		try{
			ServerAuthenticationToken authToken = validateToken(token);
			SessionManager.get().getSessionState(authToken).searchCrawlParse(query, lang, useRestrictedDomains, numResults, keywords);
		}
		catch (NullPointerException ex){
			ServerLogger.get().error(ex, "NullPointerException in beginWebSearch:");
		}
	}

	@Override
	public void moreResultsWebSearch(AuthToken token){
		try{
			ServerAuthenticationToken authToken = validateToken(token);
			SessionManager.get().getSessionState(authToken).moreResults();
		}
		catch (NullPointerException ex){
			ServerLogger.get().error(ex, "NullPointerException in beginWebSearch:");
		}
	}

	@Override
	public void beginCorpusUpload(AuthToken token, Language lang, ArrayList<String> keywords)
	{
		ServerAuthenticationToken authToken = validateToken(token);
		SessionManager.get().getSessionState(authToken).beginCustomCorpusUpload(lang, keywords);
	}

	@Override
	public void endCorpusUpload(AuthToken token, boolean success)
	{
		ServerAuthenticationToken authToken = validateToken(token);
		SessionManager.get().getSessionState(authToken).endCustomCorpusUpload(success);
	}

	@Override
	public void cancelCurrentOperation(AuthToken token)
	{
		ServerAuthenticationToken authToken = validateToken(token);
		SessionManager.get().getSessionState(authToken).cancelOperation();
	}
}
