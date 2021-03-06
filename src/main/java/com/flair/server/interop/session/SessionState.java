/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.interop.session;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.flair.server.crawler.SearchResult;
import com.flair.server.crawler.WebSearchAgent;
import com.flair.server.grammar.DefaultVocabularyList;
import com.flair.server.interop.MessagePipeline;
import com.flair.server.parser.AbstractDocument;
import com.flair.server.parser.AbstractDocumentSource;
import com.flair.server.parser.DocumentCollection;
import com.flair.server.parser.DocumentConstructionData;
import com.flair.server.parser.KeywordSearcherInput;
import com.flair.server.parser.KeywordSearcherOutput;
import com.flair.server.parser.SearchResultDocumentSource;
import com.flair.server.parser.StreamDocumentSource;
import com.flair.server.parser.TextSegment;
import com.flair.server.taskmanager.*;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.interop.AbstractMesageSender;
import com.flair.shared.interop.RankableDocumentImpl;
import com.flair.shared.interop.RankableWebSearchResultImpl;
import com.flair.shared.interop.ServerAuthenticationToken;
import com.flair.shared.interop.ServerMessage;
import com.flair.shared.interop.ServerMessage.Type;
import com.flair.shared.interop.UploadedDocument;
import com.flair.shared.interop.UploadedDocumentImpl;

/**
 * Stores the state of a client
 *
 * @author shadeMe
 */
public class SessionState
{
	/**
	 * Represents the state of a pipeline operation
	 */
	static final class OperationState
	{
		public final PipelineOperationType			type;
		public final SearchCrawlParseOperation		searchCrawlParse;
		public final CustomParseOperation			customParse;

		OperationState(SearchCrawlParseOperation op)
		{
			type = op.getType();
			searchCrawlParse = op;
			customParse = null;
		}

		OperationState(CustomParseOperation op)
		{
			type = op.getType();
			searchCrawlParse = null;
			customParse = op;
		}
		/**
		 * Returns the type of pipeline operation to be executed
		 * @return AbstractPipelineOperation object representing pipeline operation
		 */
		public AbstractPipelineOperation get()
		{
			switch (type)
			{
			case SEARCH_CRAWL_PARSE:
				return searchCrawlParse;
			case CUSTOM_PARSE:
				return customParse;
			}

			return null;
		}
	}
	
	/**
	 * Temporary Cache for uploaded Corpus Document
	 */
	static final class TemporaryCache
	{
		static final class CustomCorpus
		{
			Language					lang;
			KeywordSearcherInput		keywords;
			List<CustomCorpusFile>		uploaded;


			public CustomCorpus(Language l, List<String> k)
			{
				lang = l;

				if (k.isEmpty())
					keywords = new KeywordSearcherInput(DefaultVocabularyList.get(lang));
				else
					keywords = new KeywordSearcherInput(k);

				uploaded = new ArrayList<>();
			}
		}

		CustomCorpus			corpusData;
	}
	/**
	 * Represents an uploaded File Document
	 */
	static final class UploadedFileDocumentSource extends StreamDocumentSource
	{
		private final int			id;		// arbitrary identifier

		public UploadedFileDocumentSource(InputStream source, String name, Language lang, int id)
		{
			super(source, name, lang);
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	private final ServerAuthenticationToken		token;
	private final AbstractMesageSender			messagePipeline;
	private OperationState						currentOperation;
	private TemporaryCache						cache;
	private SearchCrawlParseOperation           lastSearchOperation;
	private int                                 lastNumResults;
	private List<String>                        lastKeywords;

	public SessionState(ServerAuthenticationToken tok)
	{
		token = tok;
		messagePipeline = MessagePipeline.get().createSender();
		currentOperation = null;
		cache = new TemporaryCache();

		messagePipeline.open(token);
	}

	/**
	 * Initiates a state operation
	 * @param state Pipeline operation to be executed
	 */
	private void beginOperation(OperationState state)
	{
		if (hasOperation())
			throw new RuntimeException("Previous state not cleared");

		ServerLogger.get().info("Pipeline operation " + state.type + " has begun");
		
		currentOperation = state;
		// clear the message queue just in case any old messages ended up there
		messagePipeline.clearPendingMessages();
		
		// has to be the tail call as the begin operation can trigger the completion event if there are no queued tasks
		currentOperation.get().begin();
	}

	private void beginMoreResultsOperation(OperationState state, SearchCrawlParseJob lastJob){
		if (hasOperation())
			throw new RuntimeException("Previous state not cleared");

		ServerLogger.get().info("Pipeline operation " + state.type + " has begun");

		currentOperation = state;
		// clear the message queue just in case any old messages ended up there
		messagePipeline.clearPendingMessages();

		// has to be the tail call as the begin operation can trigger the completion event if there are no queued tasks
		SearchCrawlParseOperation op = (SearchCrawlParseOperation) currentOperation.get();
		op.beginFromPreviousSCPJob(lastJob);
	}
	
	/**
	 * Either cancels a running operation or clears a finished operation
	 * @param cancel Specifies whether or not to cancel an operation
	 */
	private void endOperation(boolean cancel)
	{
		if (hasOperation() == false)
			throw new RuntimeException("No operation running");

		if (cancel)
			currentOperation.get().cancel();

		ServerLogger.get().info("Pipeline operation " + currentOperation.type + " has ended | Cancelled = " + cancel);
		//cleanRaft();
		currentOperation = null;
	}

	public void cleanRaft(){
		File folder = new File(".");
		File fList[] = folder.listFiles();
		// Searchs unlabeled
		for (int i = 0; i < fList.length; i++) {
			File file = fList[i];
			String fileStr = file.toString();
    		if (fileStr.contains("unlabeled") || fileStr.contains("mada_")) {
				if(file.delete())
					ServerLogger.get().info(fileStr + " deleted");
				else	
				ServerLogger.get().error(fileStr + " not deleted ");
    		}
		}
	}

	/**
	 * Checks to see if there is an operation running
	 * @return Boolean value representing if there is a running operation
	 */
	public synchronized boolean hasOperation() {
		return currentOperation != null;
	}

	/**
	 * Cancels current operation
	 */
	public synchronized void cancelOperation()
	{
		if (hasOperation() == false)
		{
			sendErrorResponse("No active operation to cancel");
			return;
		}

		endOperation(true);
	}

	/**
	 * Creates a document that the client can rank
	 * @param source Object representing a text source to be ranked, Is either a search result or an uploaded document
	 * @return A rankable object
	 */
	private RankableDocumentImpl generateRankableDocument(AbstractDocument source)		//creates a document that the client can rank
	{
		RankableDocumentImpl out = new RankableDocumentImpl();
		final int snippetMaxLen = 100;

		if (source.isParsed() == false)
			throw new IllegalStateException("Document not flagged as parsed");

		out.setLanguage(source.getLanguage());
		if (source.getDocumentSource() instanceof SearchResultDocumentSource)			//sets properties corresponding with a web search result
		{
			SearchResultDocumentSource searchSource = (SearchResultDocumentSource) source.getDocumentSource();
			SearchResult searchResult = searchSource.getSearchResult();

			out.setTitle(searchResult.getTitle());
			out.setUrl(searchResult.getURL());
			out.setDisplayUrl(searchResult.getDisplayURL());
			out.setSnippet(searchResult.getSnippet());
			out.setRank(searchResult.getRank());
			out.setIdentifier(searchResult.getRank());		// ranks don't overlap, so we can use them as ids
		}
		else if (source.getDocumentSource() instanceof UploadedFileDocumentSource)		//sets properties corresponding with an uploaded document
		{
			UploadedFileDocumentSource localSource = (UploadedFileDocumentSource) source.getDocumentSource();

			out.setTitle(localSource.getName());

			String textSnip = source.getText();
			if (textSnip.length() > snippetMaxLen)
				out.setSnippet(textSnip.substring(0, snippetMaxLen) + "...");
			else
				out.setSnippet(textSnip);

			out.setIdentifier(localSource.getId());		// use the id generated earlier
			out.setRank(localSource.getId());			// in the same order the files were uploaded to the server
		}

		out.setText(source.getText());

		for (GrammaticalConstruction itr : source.getSupportedConstructions())		//seems like this grabs the occurences of grammatical construction frequency 
		{																			//so the ranker can reorder based on grammaitcal criteria
			DocumentConstructionData data = source.getConstructionData(itr);
			if (data.hasConstruction())
			{
				out.getConstructions().add(itr);
				out.getRelFrequencies().put(itr, data.getRelativeFrequency());
				out.getFrequencies().put(itr, data.getFrequency());

				ArrayList<RankableDocumentImpl.ConstructionOccurrence> highlights = new ArrayList<>();
				for (com.flair.server.parser.ConstructionOccurrence occr : data.getOccurrences())
					highlights.add(new RankableDocumentImpl.ConstructionOccurrence(occr.getStart(), occr.getEnd(), itr));

				out.getConstOccurrences().put(itr, highlights);
			}
		}

		KeywordSearcherOutput keywordData = source.getKeywordData();
		if (keywordData != null)
		{
			for (String itr : keywordData.getKeywords())							//keeps track of key word occurences so client can reorder based on academic key words
			{
				List<TextSegment> hits = keywordData.getHits(itr);
				for (TextSegment hit : hits)
					out.getKeywordOccurrences().add(new RankableDocumentImpl.KeywordOccurrence(hit.getStart(), hit.getEnd(), itr));
			}

			out.setKeywordCount(keywordData.getTotalHitCount());
			out.setKeywordRelFreq(keywordData.getTotalHitCount() / source.getNumWords());
		}

		//properties set for every parsed document, regardless of type
		out.setRawTextLength(source.getText().length());
		out.setNumWords(source.getNumWords());
		out.setNumSentences(source.getNumSentences());
		out.setNumDependencies(source.getNumDependencies());
		if(source.getLanguage().toString().equals("ARABIC")) 
		{
			ServerLogger.get().info("Source is arabic, setting arabic readability level to be sent to client");
			out.setArabicReadabilityLevel(source.getArabicReadabilityLevel());
			ServerLogger.get().info("Arabic readability level is " + out.getArabicReadabilityLevel().toString());
		}
		else 
		{
			out.setReadabilityLevel(source.getReadabilityLevel());
		}
		out.setReadabilityScore(source.getReadabilityScore());

		return out;
	}

	/**
	 * Converts a generic web search result to a rankable web search result object
	 * @param sr A web search result 
	 * @return A rankable web search object
	 */
	private RankableWebSearchResultImpl generateRankableWebSearchResult(SearchResult sr)
	{
		RankableWebSearchResultImpl out = new RankableWebSearchResultImpl();

		out.setRank(sr.getRank());
		out.setTitle(sr.getTitle());
		out.setLang(sr.getLanguage());
		out.setUrl(sr.getURL());
		out.setDisplayUrl(sr.getDisplayURL());
		out.setSnippet(sr.getSnippet());
		out.setText(sr.getPageText());
		out.setIdentifier(sr.getRank());

		return out;
	}

	/**
	 * 
	 */
	private ArrayList<UploadedDocument> generateUploadedDocs(Iterable<AbstractDocumentSource> source)
	{
		ArrayList<UploadedDocument> out = new ArrayList<>();

		for (AbstractDocumentSource itr : source)
		{
			UploadedFileDocumentSource sdr = (UploadedFileDocumentSource)itr;
			UploadedDocumentImpl u = new UploadedDocumentImpl();
			String snippet = itr.getSourceText();
			if (snippet.length() > 100)
				snippet = snippet.substring(0, 100);

			u.setLanguage(itr.getLanguage());
			u.setTitle(sdr.getName());
			u.setSnippet(snippet);
			u.setText(itr.getSourceText());
			u.setIdentifier(sdr.getId());

			out.add(u);
		}

		return out;
	}

	private void sendMessageToClient(ServerMessage msg)
	{
		ServerLogger.get().info("Sent message to client: " + msg.toString());
		messagePipeline.send(msg);
	}
	
	private synchronized void handleCorpusJobBegin(Iterable<AbstractDocumentSource> source)
	{
		if (hasOperation() == false)
		{
			ServerLogger.get().error("Invalid corpus job begin event");
			return;
		}

		ServerMessage msg = new ServerMessage(token);
		ServerMessage.CustomCorpus d = new ServerMessage.CustomCorpus(generateUploadedDocs(source));
		msg.setCustomCorpus(d);
		msg.setType(ServerMessage.Type.CUSTOM_CORPUS);

		sendMessageToClient(msg);
	}

	private synchronized void handleCrawlComplete(SearchResult sr)
	{
		if (hasOperation() == false)
		{
			ServerLogger.get().error("Invalid crawl complete event");
			return;
		}

		ServerMessage msg = new ServerMessage(token);
		ServerMessage.SearchCrawlParse d = new ServerMessage.SearchCrawlParse(generateRankableWebSearchResult(sr));
		msg.setSearchCrawlParse(d);
		msg.setType(ServerMessage.Type.SEARCH_CRAWL_PARSE);

		sendMessageToClient(msg);
	}

	private synchronized void handleParseComplete(ServerMessage.Type t, AbstractDocument doc)
	{
		if (hasOperation() == false)
		{
			ServerLogger.get().error("Invalid parse complete event for " + t);
			return;
		}

		ServerMessage msg = new ServerMessage(token);
		switch (t)
		{
		case CUSTOM_CORPUS:
			msg.setCustomCorpus(new ServerMessage.CustomCorpus(generateRankableDocument(doc)));
			break;
		case SEARCH_CRAWL_PARSE:
			msg.setSearchCrawlParse(new ServerMessage.SearchCrawlParse(generateRankableDocument(doc)));
			break;
		}

		msg.setType(t);
		sendMessageToClient(msg);
	}

	private synchronized void handleJobComplete(ServerMessage.Type t, DocumentCollection docs)
	{
		if (hasOperation() == false)
		{
			ServerLogger.get().error("Invalid job complete event for " + t);
			return;
		}

		ServerMessage msg = new ServerMessage(token);
		switch (t)
		{
		case CUSTOM_CORPUS:
			msg.setCustomCorpus(new ServerMessage.CustomCorpus(ServerMessage.CustomCorpus.Type.JOB_COMPLETE));
			break;
		case SEARCH_CRAWL_PARSE:
			msg.setSearchCrawlParse(new ServerMessage.SearchCrawlParse(ServerMessage.SearchCrawlParse.Type.JOB_COMPLETE));
			break;
		}

		msg.setType(t);
		sendMessageToClient(msg);

		// reset operation state
		endOperation(false);
	}

	private void sendErrorResponse(String err)
	{
		ServerLogger.get().error(err);

		ServerMessage msg = new ServerMessage(token);
		ServerMessage.Error d = new ServerMessage.Error(err);
		msg.setError(d);
		msg.setType(Type.ERROR);

		sendMessageToClient(msg);
	}

	public synchronized void handleCorpusUpload(List<CustomCorpusFile> corpus)
	{
		ServerLogger.get().info("Received custom corpus from client");

		if (hasOperation())
		{
			sendErrorResponse("Another operation still running");
			return;
		}
		else if (cache.corpusData == null)
		{
			sendErrorResponse("Invalid params for custom corpus");
			return;
		}

		// save the uploaded file for later
		for (CustomCorpusFile itr : corpus)
			cache.corpusData.uploaded.add(itr);
	}

	public synchronized void searchCrawlParse(String query, Language lang, boolean useRestrictedDomains, int numResults, List<String> keywords)
	{		//this is where the search crawl parse begins
		lastNumResults = numResults;
		lastKeywords = keywords;

		ServerLogger.get().info("Begin search-crawl-parse -> Query: " + query + ", Language: " + lang.toString() + ", Use restricted domains: " + useRestrictedDomains + ", Results: " + numResults);
		
		if (hasOperation())
		{
			sendErrorResponse("Another operation still running");
			return;
		}

		KeywordSearcherInput k;
		if (keywords.isEmpty())
			k = new KeywordSearcherInput(DefaultVocabularyList.get(lang));
		else
			k = new KeywordSearcherInput(keywords);

		ServerLogger.get().info("Creating search crawl parse operation");
		SearchCrawlParseOperation op = MasterJobPipeline.get().doSearchCrawlParse(lang, query, useRestrictedDomains, numResults, k);
		lastSearchOperation = op;
		//do we ever get past here?
		ServerLogger.get().info("finished creating search crawl parse operation");
		op.setCrawlCompleteHandler(e -> {
			handleCrawlComplete(e);
		});
		op.setParseCompleteHandler(e -> {
			handleParseComplete(ServerMessage.Type.SEARCH_CRAWL_PARSE, e);
		});
		op.setJobCompleteHandler(e -> {
			handleJobComplete(ServerMessage.Type.SEARCH_CRAWL_PARSE, e);
		});
		ServerLogger.get().info("Calling beginOperation()");
		beginOperation(new OperationState(op));
	}

	public synchronized void moreResults(){
		if (hasOperation())
		{
			sendErrorResponse("Another operation still running");
			return;
		}

		SearchCrawlParseJob lastJob = lastSearchOperation.getJob();
		WebSearchAgent agent = lastJob.getSearchAgent();

		KeywordSearcherInput k;
		if (lastKeywords.isEmpty())
			k = new KeywordSearcherInput(DefaultVocabularyList.get(agent.getLanguage()));
		else
			k = new KeywordSearcherInput(lastKeywords);

		SearchCrawlParseOperation op = MasterJobPipeline.get().doSearchCrawlParse(agent.getLanguage(), agent.getQuery(), agent.getUseRestrictedDomains(), lastNumResults, k);
		lastSearchOperation = op;
		//do we ever get past here?
		ServerLogger.get().info("finished creating search crawl parse operation");
		op.setCrawlCompleteHandler(e -> {
			handleCrawlComplete(e);
		});
		op.setParseCompleteHandler(e -> {
			handleParseComplete(ServerMessage.Type.SEARCH_CRAWL_PARSE, e);
		});
		op.setJobCompleteHandler(e -> {
			handleJobComplete(ServerMessage.Type.SEARCH_CRAWL_PARSE, e);
		});
		ServerLogger.get().info("Calling beginOperation()");
		beginMoreResultsOperation(new OperationState(op), lastJob);
	}

	public synchronized void beginCustomCorpusUpload(Language lang, List<String> keywords)
	{
		ServerLogger.get().info("Begin custom corpus uploading -> Language: " + lang.toString());

		if (hasOperation())
		{
			ServerLogger.get().info("Another operation is in progress - Discarding file");
			return;
		}
		
		// cache params and await the upload servlet
		// discard the previous cache, if any
		cache.corpusData = new TemporaryCache.CustomCorpus(lang, keywords);
		ServerLogger.get().info("Cached corpus parse parameters");
	}

	public synchronized void endCustomCorpusUpload(boolean success)
	{
		ServerLogger.get().info("End custom corpus uploading | Success: " + success);
		
		if (hasOperation())
		{
			sendErrorResponse("Another operation still running");
			return;
		}

		if (success == false)
		{
			// don't begin the parse op
			cache.corpusData = null;
			return;
		}
		
		// begin parsing operation
		List<AbstractDocumentSource> sources = new ArrayList<>();
		try
		{
			int i = 1;		// assign identifiers to the files for later use
			for (CustomCorpusFile itr : cache.corpusData.uploaded)
			{
				sources.add(new UploadedFileDocumentSource(itr.getStream(),
									itr.getFilename(),
									cache.corpusData.lang,
									i));
				i++;
			}
		} catch (Throwable ex)
		{
			sendErrorResponse("Couldn't read custom corpus files. Exception: " + ex.getMessage());
		}


		CustomParseOperation op = MasterJobPipeline.get().doDocumentParsing(cache.corpusData.lang,
																		sources,
																		cache.corpusData.keywords);
		// register event handlers and start the op
		op.setJobBeginHandler(e -> {
			handleCorpusJobBegin(e);
		});
		op.setParseCompleteHandler(e -> {
			handleParseComplete(ServerMessage.Type.CUSTOM_CORPUS, e);
		});
		op.setJobCompleteHandler(e -> {
			handleJobComplete(ServerMessage.Type.CUSTOM_CORPUS, e);
		});

		beginOperation(new OperationState(op));
		
		// reset cache
		cache.corpusData = null;
	}

	public synchronized void release()
	{
		if (hasOperation())
		{
			ServerLogger.get().warn("Pipeline operation is still executing at the time of shutdown. Status: "
					+ currentOperation.get().toString());

			endOperation(true);
		}

		if (messagePipeline.isOpen())
			messagePipeline.close();
	}
}
