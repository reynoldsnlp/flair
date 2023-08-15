/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.taskmanager;

import java.util.List;

import com.flair.server.parser.AbstractDocumentKeywordSearcherFactory;
import com.flair.server.parser.AbstractDocumentSource;
import com.flair.server.parser.AbstractParsingStrategyFactory;
import com.flair.server.parser.KeywordSearcherInput;
import com.flair.server.parser.KeywordSearcherType;
import com.flair.server.parser.MasterParsingFactoryGenerator;
import com.flair.server.parser.ParserType;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

/**
 * Job scheduler for the web crawling/local parser frameworks
 * Specifies the language to use
 *
 * @author shadeMe
 */
public final class MasterJobPipeline
{
	private static MasterJobPipeline SINGLETON = null;

	public static MasterJobPipeline get()
	{
		if (SINGLETON == null)
		{
			synchronized (MasterJobPipeline.class)
			{
				if (SINGLETON == null)
					SINGLETON = new MasterJobPipeline();
			}
		}

		return SINGLETON;
	}

	public static void dispose()
	{
		if (SINGLETON != null)
		{
			SINGLETON.shutdown();
			SINGLETON = null;
		}
	}

	private final WebSearchTask.Executor		webSearchExecutor;
	private final WebCrawlTask.Executor			webCrawlExecutor;
	private final DocumentParseTask.Executor	docParseExecutor;

	private final AbstractParsingStrategyFactory	stanfordArabicStrategy;
	private final AbstractParsingStrategyFactory	stanfordEnglishStrategy;
	private final AbstractParsingStrategyFactory	stanfordGermanStrategy;
	private final AbstractParsingStrategyFactory	stanzaPersianStrategy;
	private final AbstractParsingStrategyFactory	stanfordRussianStrategy;


	private final AbstractDocumentKeywordSearcherFactory naiveSubstringSearcher;

	private DocumentParserPool	stanfordParserArabicPool;
	private DocumentParserPool	stanfordParserEnglishPool;
	private DocumentParserPool	stanfordParserGermanPool;
	private DocumentParserPool	stanzaParserPersianPool;
	private DocumentParserPool	stanfordParserRussianPool;


	private MasterJobPipeline()
	{
		ServerLogger.get().info("Initializing MasterJobPipeline");
		this.webSearchExecutor = WebSearchTask.getExecutor();
		this.webCrawlExecutor = WebCrawlTask.getExecutor();
		this.docParseExecutor = DocumentParseTask.getExecutor();

		this.stanfordArabicStrategy = MasterParsingFactoryGenerator.createParsingStrategy(ParserType.STANFORD_CORENLP,
				Language.ARABIC);
		this.stanfordEnglishStrategy = MasterParsingFactoryGenerator.createParsingStrategy(ParserType.STANFORD_CORENLP,
				Language.ENGLISH);
		this.stanfordGermanStrategy = MasterParsingFactoryGenerator.createParsingStrategy(ParserType.STANFORD_CORENLP,
				Language.GERMAN);
		this.stanzaPersianStrategy = MasterParsingFactoryGenerator.createParsingStrategy(ParserType.STANZA,
				Language.PERSIAN);
		this.stanfordRussianStrategy = MasterParsingFactoryGenerator.createParsingStrategy(ParserType.STANFORD_CORENLP,
				Language.RUSSIAN);

		this.naiveSubstringSearcher = MasterParsingFactoryGenerator
				.createKeywordSearcher(KeywordSearcherType.NAIVE_SUBSTRING);

		// lazy initilization
		this.stanfordParserArabicPool = null;
		this.stanfordParserGermanPool = null;
		this.stanfordParserEnglishPool = null;
		this.stanzaParserPersianPool = null;
		this.stanfordParserRussianPool = null;
	}

	private void shutdown()
	{
		webSearchExecutor.shutdown(false);
		webCrawlExecutor.shutdown(false);
		docParseExecutor.shutdown(false);
	}

	/**
	* Selects the correct parsing strategy for the corresponding language
	* @param lang
	* @return language parsing strategy
	*/
	private AbstractParsingStrategyFactory getStrategyForLanguage(Language lang)
	{
		ServerLogger.get().info("getStrategyForLanguage() " + lang);
		switch (lang)
		{
		case ARABIC:
			return stanfordArabicStrategy;
		case ENGLISH:
			return stanfordEnglishStrategy;
		case GERMAN:
			return stanfordGermanStrategy;
		case PERSIAN:
			return stanzaPersianStrategy;
		case RUSSIAN:
			return stanfordRussianStrategy;
		default:
			throw new IllegalArgumentException("Language " + lang + " not supported");
		}
	}

	/**
	 * Checks language parameter and then creates a parser for the specific language being used
	 * @param lang
	 * @return Parser for specific language
	 */
	private DocumentParserPool getParserPoolForLanguage(Language lang)
	{
		ServerLogger.get().info("getParserPoolForLanguage() " + lang);
		switch (lang)
		{
		case ARABIC:
			if (stanfordParserArabicPool == null)
			{
				stanfordParserArabicPool = new DocumentParserPool(
						MasterParsingFactoryGenerator.createParser(ParserType.STANFORD_CORENLP, Language.ARABIC));
			}
			return stanfordParserArabicPool;
		case ENGLISH:
			if (stanfordParserEnglishPool == null)
			{
				stanfordParserEnglishPool = new DocumentParserPool(
						MasterParsingFactoryGenerator.createParser(ParserType.STANFORD_CORENLP, Language.ENGLISH));
			}
			return stanfordParserEnglishPool;
		case GERMAN:
			if (stanfordParserGermanPool == null)
			{
				stanfordParserGermanPool = new DocumentParserPool(
						MasterParsingFactoryGenerator.createParser(ParserType.STANFORD_CORENLP, Language.GERMAN));
			}
			return stanfordParserGermanPool;
		case PERSIAN:
			if (stanzaParserPersianPool == null)
			{
				stanzaParserPersianPool = new DocumentParserPool(
						MasterParsingFactoryGenerator.createParser(ParserType.STANZA, Language.PERSIAN));
			}
			return stanzaParserPersianPool;
		case RUSSIAN:
			if (stanfordParserRussianPool == null)
			{
				stanfordParserRussianPool = new DocumentParserPool(
						MasterParsingFactoryGenerator.createParser(ParserType.STANFORD_CORENLP, Language.RUSSIAN));
			}
			return stanfordParserRussianPool;
		default:
			throw new IllegalArgumentException("Language " + lang + " not supported");
		}
	}

	public SearchCrawlParseOperation doSearchCrawlParse(Language lang,
													String query,
													boolean useRestrictedDomains,
													int numResults,
													KeywordSearcherInput keywords)
	{
		ServerLogger.get().info("Start of doSearchCrawlParse()");
		SearchCrawlParseOperationImpl newOp = null;
		try{
			SearchCrawlParseJobInput jobParams = new SearchCrawlParseJobInput(lang,
			query,
			useRestrictedDomains,
			numResults,
			webSearchExecutor,
			webCrawlExecutor,
			docParseExecutor,
			getParserPoolForLanguage(lang),
			getStrategyForLanguage(lang),
			naiveSubstringSearcher,
			keywords);
			newOp = new SearchCrawlParseOperationImpl(jobParams);
		} catch(Exception ex) {
			ServerLogger.get().info("Failed in doSearchCrawlParse on exception " + ex.getMessage());
			return newOp;
		}

		ServerLogger.get().info("End of doSearchCrawlParse()");
		return newOp;
	}

	public CustomParseOperation doDocumentParsing(Language lang,
														List<AbstractDocumentSource> docsSources,
														KeywordSearcherInput keywords)
	{
		ParseJobInput jobParams = new ParseJobInput(lang,
										docsSources,
										docParseExecutor,
										getParserPoolForLanguage(lang),
										getStrategyForLanguage(lang),
										naiveSubstringSearcher,
										keywords);
		CustomParseOperationImpl newOp = new CustomParseOperationImpl(jobParams);
		return newOp;
	}
}
