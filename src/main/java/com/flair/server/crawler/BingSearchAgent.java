package com.flair.server.crawler;

import java.util.ArrayList;
import java.util.List;
import com.flair.server.crawler.impl.AbstractSearchAgentImplResult;
import com.flair.server.crawler.impl.azure.AzureWebSearch;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

/**
 * Implementation of the Bing Search engine
 *
 * @author shadeMe
 */
class BingSearchAgent extends CachingSearchAgent
{
	private static String PROD_API_KEY;
	private static boolean isKeySet = false;

	private static final int		RESULTS_PER_PAGE	= 100;		// larger numbers will reduce the number of search transactions but will increase the response size
	private static final int		MAX_API_REQUESTS	= 2;

	private static final String[] WEBSITES_RUSSIAN = new String[]{
		"klepa.ru/klep-news",
		"read-ka.cofe.ru",
		"filipoc.ru",
		"unnaturalist.ru",
		"kvantik.com",
		"murzilka.org/izba-chitalnya/interesting/murzilka-i-aeroekspress",
		"bbc.com/russian",
		"lenta.ru",
	};
	private static final String[] WEBSITES_ARABIC = new String[]{
		"Aljazeera.net",
		"bbc.com/arabic",
		"Haybinyakzhan.blogspot.com",
		"Arageek.com",
		"Mawdoo3.com",
		"Hindawi.org",
	};

	private final AzureWebSearch pipeline;

	public BingSearchAgent(Language lang, String query, boolean useRestrictedDomains)
	{
		super(lang, query, useRestrictedDomains, MAX_API_REQUESTS);
		this.pipeline = new AzureWebSearch();

		try{
			if(!isKeySet) {
				ServerLogger.get().info("Setting PROD_API_KEY");
				PROD_API_KEY = System.getenv("BING_API");
				if(PROD_API_KEY == null) throw new Exception("BING_API environment variable not found");
				isKeySet = true;
			}
			else {
				throw new Exception();
			}
		}
		catch(SecurityException ex) {
			ServerLogger.get().error(ex, "caught security exception trying to grab BING_API variable");
		}
		catch(Exception ex) {
			ServerLogger.get().error(ex, "caught exception trying to grab BING_API variable");
		}

		String qPostfix;
		String market;

		switch (lang){
			case ENGLISH:
				qPostfix = " language:en";
				market = "en-US";
				break;
			case GERMAN:
				qPostfix = " language:de";
				market = "de-DE";
				break;
			case RUSSIAN:
				qPostfix = " language:ru";
				market = "ru-RU";
				break;
			case ARABIC:
				qPostfix = " language:ar";
				market = "ar-SA";
				break;
			default:
				throw new IllegalArgumentException("Unsupported language " + lang);
		}

		String totalQueryString = query + qPostfix;

		if(this.useRestrictedDomains){
			String[] websitesToSearch;
			switch(lang){
				case RUSSIAN:
					websitesToSearch = WEBSITES_RUSSIAN;
					break;
				case ARABIC:
					websitesToSearch = WEBSITES_ARABIC;
					break;
				default:
					ServerLogger.get().info("usePresetWebsites was set to true but no list of domains was found for the selected language");
					websitesToSearch = null;
					break;
			}
			if(websitesToSearch != null) totalQueryString += (" " + createSearchDomain(websitesToSearch));
		}

		pipeline.setApiKey(PROD_API_KEY);
		pipeline.setQuery(totalQueryString);
		pipeline.setPerPage(RESULTS_PER_PAGE);
		pipeline.setMarket(market);
	}

	@Override
	protected List<? extends AbstractSearchAgentImplResult> invokeSearchApi()
	{
		List<? extends AbstractSearchAgentImplResult> azureResults = new ArrayList<>();
		if (!noMoreResults)
		{
			try
			{
				// the pipeline can potentially throw a java.net.UnknownHostException, so wrap it in EH to be safe
				pipeline.setPage(nextPage);
				azureResults = pipeline.performSearch();
			} catch (Throwable e)
			{
				ServerLogger.get().error("Bing search API encountered a fatal error. Exception: " + e.getMessage());
				noMoreResults = true;
			}
		}

		return azureResults;
	}

	private static String createSearchDomain(String[] websites){
		StringBuilder output = new StringBuilder();
		output.append("(");
		for(int i = 0; i < websites.length; i++){
			output.append("site:");
			output.append(websites[i]);
			if(i != websites.length - 1) output.append(" OR ");
		}
		output.append(")");
		return output.toString();
	}
}
