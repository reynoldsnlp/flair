package com.flair.server.taskmanager;

import com.flair.server.crawler.SearchResult;
import com.flair.server.parser.AbstractDocument;
import com.flair.server.parser.DocumentCollection;
import com.flair.server.utilities.ServerLogger;

/**
 * Search Crawl Parse operation that extends the AbstractPipeLineOperation interface
 */
public interface SearchCrawlParseOperation extends AbstractPipelineOperation
{
	/**
	 * abstraction of a crawl event completion
	 */
	public interface CrawlComplete {
		public void handle(SearchResult result);
	}
	/**
	 * abstraction of a parse event completion
	 */
	public interface ParseComplete {
		public void handle(AbstractDocument result);
	}
	/**
	 * abstraction of a job event completion
	 */
	public interface JobComplete {
		public void handle(DocumentCollection result);
	}
	/**
	 * Sets the handler for a crawl event completion
	 * @param handler Crawl completion event handler
	 */
	public void			setCrawlCompleteHandler(CrawlComplete handler);
	/**
	 * Sets the handler for a parse event completion
	 * @param handler Parse completion event handler
	 */
	public void			setParseCompleteHandler(ParseComplete handler);
	/**
	 * Sets the handler for a job event completion
	 * @param handler Job completion event handler
	 */
	public void			setJobCompleteHandler(JobComplete handler);
}
/**
 * Implementation of SearchCrawlParseOperation interface and extension of generic pipeline operation
 */
class SearchCrawlParseOperationImpl extends BasicPipelineOperation implements SearchCrawlParseOperation
{
	private CrawlComplete		crawlC;
	private ParseComplete		parseC;
	private JobComplete			jobC;
	
	public SearchCrawlParseOperationImpl(SearchCrawlParseJobInput input) 
	{
		super(new SearchCrawlParseJob(input), PipelineOperationType.SEARCH_CRAWL_PARSE);
		
		crawlC = null;
		parseC = null;
		jobC = null;
		ServerLogger.get().info("SearchCrawlParseOperationImpl()");
	}
	
	@Override
	public void begin()
	{
		// register listener
		SearchCrawlParseJob j = (SearchCrawlParseJob)job;
		j.addListener(e -> {
			switch (e.type)
			{
			case JOB_COMPLETE:
				if (jobC != null)
					jobC.handle(e.jobOutput.parsedDocs);
				
				break;
			case PARSE_COMPLETE:
				if (parseC != null)
					parseC.handle(e.parsedDoc);
				
				break;
			case WEB_CRAWL_COMPLETE:
				if (crawlC != null)
					crawlC.handle(e.crawledResult);
				
				break;			
			}
		});;
		
		super.begin();
	}

	@Override
	public void setCrawlCompleteHandler(CrawlComplete handler) {
		crawlC = handler;
	}

	@Override
	public void setParseCompleteHandler(ParseComplete handler) {
		parseC = handler;	
	}

	@Override
	public void setJobCompleteHandler(JobComplete handler) {
		jobC = handler;
	}
}
