/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */
package com.flair.server.crawler;

import java.util.ArrayList;
import java.util.List;

import com.flair.server.crawler.impl.AbstractSearchAgentImplResult;
import com.flair.server.crawler.impl.faroo.FarooSearch;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

/**
 * Implementation of the Faroo search engine
 * 
 * @author shadeMe
 */
public class FarooSearchAgent extends CachingSearchAgent
{
	private static final String API_KEY = "";

	private static final int	RESULTS_PER_PAGE	= 10;
	private static final int	MAX_API_REQUESTS	= 2;

	public static enum Source
	{
		WEB, NEWS, TOPICS, TRENDS
	}

	private final FarooSearch pipeline;

	public FarooSearchAgent(Language lang, String query)
	{
		super(lang, query, MAX_API_REQUESTS);
		this.pipeline = new FarooSearch();

		switch (lang)
		{
		case ENGLISH:
			pipeline.setLang(FarooSearch.SearchLanguage.ENGLISH);
			break;
		case GERMAN:
			pipeline.setLang(FarooSearch.SearchLanguage.GERMAN);
			break;
		default:
			throw new IllegalArgumentException("Unsupported language " + lang);
		}

		pipeline.setApiKey(API_KEY);
		pipeline.setQuery(query);
		pipeline.setPerPage(RESULTS_PER_PAGE);
	}

	public boolean isTrending() {
		return pipeline.isTrending();
	}

	public void setTrending(boolean trending) {
		pipeline.setTrending(trending);
	}

	public void setSearchSource(Source source)
	{
		switch (source)
		{
		case WEB:
			pipeline.setSource(FarooSearch.SearchSource.WEB);
			break;
		case NEWS:
			pipeline.setSource(FarooSearch.SearchSource.NEWS);
			break;
		case TOPICS:
			pipeline.setSource(FarooSearch.SearchSource.TOPICS);
			break;
		case TRENDS:
			pipeline.setSource(FarooSearch.SearchSource.TRENDS);
			break;
		}
	}

	@Override
	protected List<? extends AbstractSearchAgentImplResult> invokeSearchApi()
	{
		List<? extends AbstractSearchAgentImplResult> azureResults = new ArrayList<>();
		if (noMoreResults == false)
		{
			try
			{
				pipeline.setPage(nextPage);
				azureResults = pipeline.performSearch();
			} catch (Throwable e)
			{
				ServerLogger.get().error("Faroo search API encountered a fatal error. Exception: " + e.getMessage());
				noMoreResults = true;
			}
		}

		return azureResults;
	}
}
