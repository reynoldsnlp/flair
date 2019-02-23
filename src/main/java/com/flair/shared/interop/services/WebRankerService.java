package com.flair.shared.interop.services;

import java.util.ArrayList;

import com.flair.shared.grammar.Language;
import com.flair.shared.interop.AuthToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/*
 * Interface used by the client domain to perform tasks
 */
@RemoteServiceRelativePath("WebRanker")
public interface WebRankerService extends RemoteService
{
	/**
	 * Begins the web search operation 
	 * @param token Client's AuthToken to validate web serach operation
	 * @param lang Web search language
	 * @param query Web seaarch query 
	 * @param numResults Number of results to be sent to client
	 * @param keywords List of academic keywords 
	 */
	public void			beginWebSearch(AuthToken token,
									Language lang,
									String query,
									int numResults,
									ArrayList<String> keywords);
	/**
	 * Begins the corpus upload operation 
	 * @param token Client's AuthToken to validate web serach operation
	 * @param lang Language of Corpus to be uploaded
	 * @param keywords List of academic keywords
	 */
	public void			beginCorpusUpload(AuthToken token,
										Language lang,
										ArrayList<String> keywords);		// signals the start of the upload operation and caches params
	public void			endCorpusUpload(AuthToken token,
										boolean success);					// signals the end of the uploading process, begins the parsing op if successful

	public void			cancelCurrentOperation(AuthToken token);
}

