package com.integrationtests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import com.flair.server.parser.AbstractDocument;
import com.flair.server.parser.DocumentCollection;
import com.flair.server.parser.KeywordSearcherInput;
import com.flair.server.raft.Raft;
import com.flair.server.utilities.Weka;
import com.flair.server.taskmanager.MasterJobPipeline;
import com.flair.server.taskmanager.SearchCrawlParseOperation;
import com.flair.shared.grammar.Language;
import type.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/*
 * Runs a simple web search operation
 */
@Category(IntegrationTest.class)
public class WebSearchTest
{
	
	public static void processParsedDocs(DocumentCollection dc) {
		// iterate through the document collection like you would a list
		for (AbstractDocument doc : dc) {
			// do things with the parsed document
		}
	}

	@Before
	public void setUp() 
	{
		String pathToResources = this.getClass().getClassLoader().getResource("").getPath();
		System.out.println("pathToResources: " + pathToResources);
		Raft raft = new Raft();
		if(!raft.modelExists("RandomForest.model")){
			Weka randomForest = new Weka("model.arff");	
			try {
				randomForest.setRandomForest(randomForest.buildRandomForestModel());
				weka.core.SerializationHelper.write(pathToResources + "RandomForest.model", randomForest.getRandomForest());
				System.out.println("Wrote RandomForest.model to the server resource folder");
			} catch (Exception e) {
				System.out.println(e.getMessage() + " Failed to build random forest model");
			}
		}
	}

	@Test(timeout = 90000)
	public void englishSearch()
	{
		String query = "Blue Oyster Cult";
		int numResults = 1;
		Language lang = Language.ENGLISH;
		String[] keywords = new String[] {
			"keywords", "to", "highlight"
		};

		/*
		 * The AbstractPipelineOperation (base class of SearchCrawlParseOperation) encapsulates a multi-threaded operation. It can contain multiple sub-operations (a.k.a tasks)
		 * Tasks can be one of three types:
		 * 	Web-Search - Takes the above input and returns a list of search results
		 * 	Web-Crawl - Takes a search result and attempts to retrieve its text
		 * 	Parse - Takes some text and parses its contents to generate an AbstractDocument object
		 * 
		 * All of these tasks execute in parallel and are linked to each other.
		 * When a particular task is complete, it sends out a notification that it's complete.
		 * This notification might contain data about the completed task.
		 */
		SearchCrawlParseOperation operation = MasterJobPipeline.get().doSearchCrawlParse(lang,
				query,
				numResults,
				new KeywordSearcherInput(Arrays.asList(keywords)));
		
		/*
		 * Set up handlers to process the different notifications sent out by the operation
		 * Then, start the operation
		 */
		operation.setCrawlCompleteHandler(sr -> {
			System.out.println("Do something with the search result: " + sr.toString());
		});
		operation.setParseCompleteHandler(d -> {
			System.out.println("Do something with the parsed document: " + d.toString());
		});
		operation.setJobCompleteHandler(dc -> {
			System.out.println("Do something with the document collection: " + dc.toString());
			processParsedDocs(dc);
			
		});
		operation.begin();
		/*
		 * At this point, the execution has started and is executing in the background
		 * Each of these handlers will be triggered in a fixed sequence ***for each search result***
		 * 	(CrawlComplete -> ParseComplete => JobComplete (when all the results have been parsed and the operation was not cancelled)
		 * The final result is returned by the JobComplete handler, which is usually a DocumentCollection of parsed documents
		 * Since the operation executes in parallel, we cannot be sure when it'll end/run to completion
		 * So, we wait for the operation to complete and then proceed. This can be done in the following way:
		 */
		operation.waitForCompletion();
		/*
		 * Any statements following the above call to waitForCompletion() will not be executed until the operation is complete (or was cancelled)
		 * At this point, the JobComplete handle would have executed
		 */
		System.out.println("Operation Complete");
	}

	@Test(timeout = 90000)
	public void arabicSearch()
	{
		String query = "جماعة المحار الازرق";
		int numResults = 1;
		Language lang = Language.ARABIC;
		String[] keywords = new String[] {
			"الكلمات الدالة", "إلى", "تسليط الضوء"
		};

		/*
		 * The AbstractPipelineOperation (base class of SearchCrawlParseOperation) encapsulates a multi-threaded operation. It can contain multiple sub-operations (a.k.a tasks)
		 * Tasks can be one of three types:
		 * 	Web-Search - Takes the above input and returns a list of search results
		 * 	Web-Crawl - Takes a search result and attempts to retrieve its text
		 * 	Parse - Takes some text and parses its contents to generate an AbstractDocument object
		 * 
		 * All of these tasks execute in parallel and are linked to each other.
		 * When a particular task is complete, it sends out a notification that it's complete.
		 * This notification might contain data about the completed task.
		 */
		SearchCrawlParseOperation operation = MasterJobPipeline.get().doSearchCrawlParse(lang,
				query,
				numResults,
				new KeywordSearcherInput(Arrays.asList(keywords)));
		
		/*
		 * Set up handlers to process the different notifications sent out by the operation
		 * Then, start the operation
		 */
		operation.setCrawlCompleteHandler(sr -> {
			System.out.println("Do something with the search result: " + sr.toString());
		});
		operation.setParseCompleteHandler(d -> {
			System.out.println("Do something with the parsed document: " + d.toString());
		});
		operation.setJobCompleteHandler(dc -> {
			System.out.println("Do something with the document collection: " + dc.toString());
			processParsedDocs(dc);
			
		});
		operation.begin();
		/*
		 * At this point, the execution has started and is executing in the background
		 * Each of these handlers will be triggered in a fixed sequence ***for each search result***
		 * 	(CrawlComplete -> ParseComplete => JobComplete (when all the results have been parsed and the operation was not cancelled)
		 * The final result is returned by the JobComplete handler, which is usually a DocumentCollection of parsed documents
		 * Since the operation executes in parallel, we cannot be sure when it'll end/run to completion
		 * So, we wait for the operation to complete and then proceed. This can be done in the following way:
		 */
		operation.waitForCompletion();
		/*
		 * Any statements following the above call to waitForCompletion() will not be executed until the operation is complete (or was cancelled)
		 * At this point, the JobComplete handle would have executed
		 */
		System.out.println("Operation Complete");
	}

	@Test(timeout = 90000)
	public void germamnSearch()
	{
		String query = "Blue Oyster Cult";
		int numResults = 1;
		Language lang = Language.GERMAN;
		String[] keywords = new String[] {
			"Schlüsselwörter", "zu", "Markieren"
		};

		/*
		 * The AbstractPipelineOperation (base class of SearchCrawlParseOperation) encapsulates a multi-threaded operation. It can contain multiple sub-operations (a.k.a tasks)
		 * Tasks can be one of three types:
		 * 	Web-Search - Takes the above input and returns a list of search results
		 * 	Web-Crawl - Takes a search result and attempts to retrieve its text
		 * 	Parse - Takes some text and parses its contents to generate an AbstractDocument object
		 * 
		 * All of these tasks execute in parallel and are linked to each other.
		 * When a particular task is complete, it sends out a notification that it's complete.
		 * This notification might contain data about the completed task.
		 */
		SearchCrawlParseOperation operation = MasterJobPipeline.get().doSearchCrawlParse(lang,
				query,
				numResults,
				new KeywordSearcherInput(Arrays.asList(keywords)));
		
		/*
		 * Set up handlers to process the different notifications sent out by the operation
		 * Then, start the operation
		 */
		operation.setCrawlCompleteHandler(sr -> {
			System.out.println("Do something with the search result: " + sr.toString());
		});
		operation.setParseCompleteHandler(d -> {
			System.out.println("Do something with the parsed document: " + d.toString());
		});
		operation.setJobCompleteHandler(dc -> {
			System.out.println("Do something with the document collection: " + dc.toString());
			processParsedDocs(dc);
			
		});
		operation.begin();
		/*
		 * At this point, the execution has started and is executing in the background
		 * Each of these handlers will be triggered in a fixed sequence ***for each search result***
		 * 	(CrawlComplete -> ParseComplete => JobComplete (when all the results have been parsed and the operation was not cancelled)
		 * The final result is returned by the JobComplete handler, which is usually a DocumentCollection of parsed documents
		 * Since the operation executes in parallel, we cannot be sure when it'll end/run to completion
		 * So, we wait for the operation to complete and then proceed. This can be done in the following way:
		 */
		operation.waitForCompletion();
		/*
		 * Any statements following the above call to waitForCompletion() will not be executed until the operation is complete (or was cancelled)
		 * At this point, the JobComplete handle would have executed
		 */
		System.out.println("Operation Complete");
	}

	@Test(timeout = 120000)
	public void russianSearch()
	{
		String query = "Культ Синей Устрицы";
		int numResults = 1;
		Language lang = Language.RUSSIAN;
		String[] keywords = new String[] {
			"ключевые слова", "в", "основной момент"
		};

		/*
		 * The AbstractPipelineOperation (base class of SearchCrawlParseOperation) encapsulates a multi-threaded operation. It can contain multiple sub-operations (a.k.a tasks)
		 * Tasks can be one of three types:
		 * 	Web-Search - Takes the above input and returns a list of search results
		 * 	Web-Crawl - Takes a search result and attempts to retrieve its text
		 * 	Parse - Takes some text and parses its contents to generate an AbstractDocument object
		 * 
		 * All of these tasks execute in parallel and are linked to each other.
		 * When a particular task is complete, it sends out a notification that it's complete.
		 * This notification might contain data about the completed task.
		 */
		SearchCrawlParseOperation operation = MasterJobPipeline.get().doSearchCrawlParse(lang,
				query,
				numResults,
				new KeywordSearcherInput(Arrays.asList(keywords)));
		
		/*
		 * Set up handlers to process the different notifications sent out by the operation
		 * Then, start the operation
		 */
		operation.setCrawlCompleteHandler(sr -> {
			System.out.println("Do something with the search result: " + sr.toString());
		});
		operation.setParseCompleteHandler(d -> {
			System.out.println("Do something with the parsed document: " + d.toString());
		});
		operation.setJobCompleteHandler(dc -> {
			System.out.println("Do something with the document collection: " + dc.toString());
			processParsedDocs(dc);
			
		});
		operation.begin();
		/*
		 * At this point, the execution has started and is executing in the background
		 * Each of these handlers will be triggered in a fixed sequence ***for each search result***
		 * 	(CrawlComplete -> ParseComplete => JobComplete (when all the results have been parsed and the operation was not cancelled)
		 * The final result is returned by the JobComplete handler, which is usually a DocumentCollection of parsed documents
		 * Since the operation executes in parallel, we cannot be sure when it'll end/run to completion
		 * So, we wait for the operation to complete and then proceed. This can be done in the following way:
		 */
		operation.waitForCompletion();
		/*
		 * Any statements following the above call to waitForCompletion() will not be executed until the operation is complete (or was cancelled)
		 * At this point, the JobComplete handle would have executed
		 */
		System.out.println("Operation Complete");
	}

}
