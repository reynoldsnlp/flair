package com.flair.shared.interop;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.rpc.IsSerializable;

/*
 * Represents a message from the server to the client
 */
public class ServerMessage implements IsSerializable
{
	public enum Type
	{
		ERROR,
		SEARCH_CRAWL_PARSE,
		CUSTOM_CORPUS
	}
	
	public static final class Error implements IsSerializable
	{
		String				message;
		Throwable			exception;
		
		public Error()
		{
			message = "";
			exception = null;
		}
		
		public Error(String message)
		{
			this.message = message;
			exception = null;
		}
		
		public Error(Throwable exception)
		{
			message = exception.toString();
			this.exception = exception;
		}
		
		public Error(String m, Throwable t)
		{
			message = m;
			exception = t;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Throwable getException() {
			return exception;
		}

		public void setException(Throwable exception) {
			this.exception = exception;
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("ERROR: ").append(message);
			if (exception != null)
				sb.append(" | " + exception.toString());
			return sb.toString();
		}
	}
	
	public static final class SearchCrawlParse implements IsSerializable
	{
		public enum Type
		{
			CRAWL_COMPLETE,
			PARSE_COMPLETE,
			JOB_COMPLETE
		}
		
		Type						type;
		RankableWebSearchResult		crawled;
		RankableDocument			parsed;
		
		public SearchCrawlParse()
		{
			type = null;
			crawled = null;
			parsed = null;
		}
		
		public SearchCrawlParse(Type t)
		{
			type = t;
			crawled = null;
			parsed = null;
		}
		
		public SearchCrawlParse(RankableWebSearchResult r)
		{
			type = Type.CRAWL_COMPLETE;
			crawled = r;
			parsed = null;
		}
		
		public SearchCrawlParse(RankableDocument r)
		{
			type = Type.PARSE_COMPLETE;
			crawled = null;
			parsed = r;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}
		
		public RankableWebSearchResult getCrawled() {
			return crawled;
		}

		public void setCrawled(RankableWebSearchResult crawled) {
			this.crawled = crawled;
		}

		public RankableDocument getParsed() {
			return parsed;
		}

		public void setParsed(RankableDocument parsed) {
			this.parsed = parsed;
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("SEARCH-CRAWL-PARSE: ").append(type).append("\n");
			if (type == Type.CRAWL_COMPLETE)
				sb.append("Search Result: " + crawled.getTitle() + ", " + crawled.getDisplayUrl() + " | ID =" + crawled.getIdentifier());
			else if (type == Type.PARSE_COMPLETE)
				sb.append("Parsed Doc: " + parsed.getTitle() + ", " + parsed.getDisplayUrl() + " | ID =" + parsed.getIdentifier());
			return sb.toString();
		}
	}
	
	public static final class CustomCorpus implements IsSerializable
	{
		public enum Type
		{
			UPLOAD_COMPLETE,
			PARSE_COMPLETE,
			JOB_COMPLETE
		}
		
		Type						type;
		ArrayList<UploadedDocument>	uploaded;
		RankableDocument			parsed;
		
		public CustomCorpus()
		{
			type = null;
			uploaded = null;
			parsed = null;
		}
		
		public CustomCorpus(Type t)
		{
			type = t;
			uploaded = null;
			parsed = null;
		}
		
		public CustomCorpus(RankableDocument r)
		{
			type = Type.PARSE_COMPLETE;
			parsed = r;
			uploaded = null;
		}
		
		public CustomCorpus(Collection<UploadedDocument> u)
		{
			type = Type.UPLOAD_COMPLETE;
			parsed = null;
			uploaded = new ArrayList<>(u);
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public RankableDocument getParsed() {
			return parsed;
		}

		public void setParsed(RankableDocument parsed) {
			this.parsed = parsed;
		}

		public ArrayList<UploadedDocument> getUploaded() {
			return uploaded;
		}

		public void setUploaded(ArrayList<UploadedDocument> uploaded) {
			this.uploaded = uploaded;
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("CUSTOM-CORPUS: ").append(type).append("\n");
			if (type == Type.UPLOAD_COMPLETE)
				sb.append("Uploaded files: " + uploaded.size());
			else if (type == Type.PARSE_COMPLETE)
				sb.append("Parsed Doc: " + parsed.getTitle() + " | ID =" + parsed.getIdentifier());
			return sb.toString();
		}
	}
	
	AuthToken			receiverToken;
	Type				type;
	Error				error;
	SearchCrawlParse	searchCrawlParse;
	CustomCorpus		customCorpus;
	
	public ServerMessage()
	{
		receiverToken = null;
		type = null;
		error = null;
		searchCrawlParse = null;
		customCorpus = null;
	}
	
	public ServerMessage(AuthToken t)
	{
		receiverToken = t;
		type = null;
		error = null;
		searchCrawlParse = null;
		customCorpus = null;
	}
	
	public AuthToken getReceiverToken() {
		return receiverToken;
	}
	
	public void setReceiverToken(AuthToken t) {
		receiverToken = t;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public SearchCrawlParse getSearchCrawlParse() {
		return searchCrawlParse;
	}

	public void setSearchCrawlParse(SearchCrawlParse searchCrawlParse) {
		this.searchCrawlParse = searchCrawlParse;
	}

	public CustomCorpus getCustomCorpus() {
		return customCorpus;
	}

	public void setCustomCorpus(CustomCorpus customCorpus) {
		this.customCorpus = customCorpus;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SERVER-MESSAGE: ").append(type).append("\n");
		if (type == Type.ERROR)
			sb.append("\t").append(error.toString());
		else if (type == Type.SEARCH_CRAWL_PARSE)
			sb.append("\t").append(searchCrawlParse.toString());
		else
			sb.append("\t").append(customCorpus.toString());
		
		return sb.toString();
	}
}
