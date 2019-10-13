/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.util.Properties;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Implementation of the AbstractDocumentParser that uses the Stanford CoreNLP (shift-reduce) parser
 * 
 * @author shadeMe
 */
class StanfordDocumentParser extends AbstractDocumentParser
{
	private static final String	ENGLISH_SR_PARSER_MODEL	= "edu/stanford/nlp/models/srparser/englishSR.ser.gz";
	private static final String	GERMAN_SR_PARSER_MODEL	= "edu/stanford/nlp/models/srparser/germanSR.ser.gz";
	private static final String	GERMAN_POS_MODEL		= "edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger";
	private static final String RUSSIAN_POS_MODEL       = "edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger";
	private static final String RUSSIAN_DEPPARSE_MODEL  = "edu/stanford/nlp/models/parser/nndep/nndep.rus.model.wiki.txt.gz";
	private static final String ARABIC_POS_MODEL		= "edu/stanford/nlp/models/pos-tagger/arabic/arabic.tagger";
	private static final String ARABIC_PARSE_MODEL		= "edu/stanford/nlp/models/srparser/arabicSR.ser.gz";
	private static final String ARABIC_SEGMENT_MODEL	= "edu/stanford/nlp/models/segmenter/arabic/arabic-segmenter-atb+bn+arztrain.ser.gz";


	// It seems like the russian NLP extension does not use a shift-reduce model, but instead uses a mf model. 
	// I don't if that is a meaningful difference nor do I know if that means we will have to do more work to extend the parser to russian. 

	private AbstractDocumentSource				docSource;
	private AbstractDocument					outputDoc;
	private BasicStanfordDocumentParserStrategy	parsingStrategy;	

	private final StanfordCoreNLP				pipeline;
	private final Language						modelLanguage;

	/**
	 * Constructor that generates a stanford parser
	 * @param factory Interface used to create parsed documents
	 * @param modelLang Parsing model language
	 */
	public StanfordDocumentParser(AbstractDocumentFactory factory, Language modelLang)
	{
		super(factory);
		ServerLogger.get().info("After super(factory)");

		docSource = null;
		outputDoc = null;
		parsingStrategy = null;
		modelLanguage = modelLang;

		Properties pipelineProps = new Properties();
		switch (modelLanguage)
		{
		case ENGLISH:
			// ### TODO update the parsing strategy to support universal deps
			// ### TODO consider using the neural network depparser
			pipelineProps.put("annotators", "tokenize, ssplit, pos, lemma, parse");
			pipelineProps.put("parse.originalDependencies", "true");
			pipelineProps.setProperty("parse.model", ENGLISH_SR_PARSER_MODEL);
			break;
		case GERMAN:
			pipelineProps.put("annotators", "tokenize, ssplit, pos, parse");
			pipelineProps.put("tokenize.language", "de");
			pipelineProps.setProperty("parse.model", GERMAN_SR_PARSER_MODEL);
			pipelineProps.setProperty("pos.model", GERMAN_POS_MODEL);
			break;
		case RUSSIAN:
			pipelineProps.put("annotators", "tokenize, ssplit, pos, depparse");
			pipelineProps.put("tokenize.language", "en");
			//pipelineProps.put("depparse.language", "russian");
			pipelineProps.setProperty("pos.model", RUSSIAN_POS_MODEL);
			pipelineProps.setProperty("depparse.model", RUSSIAN_DEPPARSE_MODEL);
			//pipelineProps.setProperty("depparse.language","russian");
			break;
		case ARABIC:
			pipelineProps.put("annotators", "tokenize, ssplit, pos, parse");
			pipelineProps.put("tokenize.language", "ar");
			pipelineProps.setProperty("ssplit.boundaryTokenRegex", "[.]|[!?]+|[!\\u061F]+");
			pipelineProps.setProperty("pos.model", ARABIC_POS_MODEL);
			pipelineProps.setProperty("parse.model", ARABIC_PARSE_MODEL);
			pipelineProps.setProperty("segment.model", ARABIC_SEGMENT_MODEL);
			break;
		//break;
		default:
			throw new IllegalArgumentException("Invalid model language: " + modelLanguage + "");
		}
			pipeline = new StanfordCoreNLP(pipelineProps);
			ServerLogger.get().info("Successful construction of StanfordDocumentParser");
		
	}

	/**
	 * Nulls the source document, the output document, and the parsing strategy  
	 */
	private void resetState()
	{
		docSource = null;
		outputDoc = null;
		parsingStrategy = null;
	}

	/**
	 * Checks to see if the parser is doing something
	 * @return Boolean value corresponding to the state of the parser, true if busy, false if not
	 */
	private boolean isBusy() {
		return docSource != null || outputDoc != null || parsingStrategy != null;
	}

	/**
	 * Initializes the state of the document parser by intializing the working documents and the parsing strategy
	 * @param source Source document to be parsed
	 * @param strategy Parse strategy to be used by the parser
	 * @return Document to eventually be returned by the parser
	 */
	private AbstractDocument initializeState(AbstractDocumentSource source, AbstractParsingStrategy strategy)		//sets the source doc, output doc and parsing strategy
	{	
		if (isBusy())
		{
			// this could be the case if the previous task timed-out
			ServerLogger.get().warn("Parser did complete its previous task. Resetting...");
		}
		else if (strategy instanceof BasicStanfordDocumentParserStrategy == false) {
			throw new IllegalArgumentException(
					strategy.getClass() + " is not subclass of " + BasicStanfordDocumentParserStrategy.class);
		}
		else if (isLanguageSupported(source.getLanguage()) == false) {
			throw new IllegalArgumentException("Document language " + source.getLanguage()
					+ " not supported (Model language: " + modelLanguage + ")");
		}

		docSource = source;
		outputDoc = docFactory.create(source);
		parsingStrategy = (BasicStanfordDocumentParserStrategy) strategy;		

		return outputDoc;
	}

	@Override
	public AbstractDocument parse(AbstractDocumentSource source, AbstractParsingStrategy strategy)
	{
		AbstractDocument result = null;
		try
		{	//here is where we parse a document
			ServerLogger.get().info("trying to parse document");

			result = initializeState(source, strategy);
			ServerLogger.get().info("state initialized");

			parsingStrategy.setPipeline(pipeline);
			ServerLogger.get().info("pipeline set");

			parsingStrategy.apply(outputDoc);
			ServerLogger.get().info("applying analysis to document");

		} catch (Throwable e) {
			throw e;
		} finally {
			resetState();
		}

		return result;
	}

	@Override
	public boolean isLanguageSupported(Language lang) {
		return modelLanguage == lang;
	}
}

/**
 * Factory class for the Stanford CoreNLP parser
 * 
 * @author shadeMe
 */
class StanfordDocumentParserFactory implements AbstractDocumentParserFactory
{
	private final AbstractDocumentFactory	docFactory;
	private final Language					language;

	public StanfordDocumentParserFactory(AbstractDocumentFactory factory, Language lang)
	{
		ServerLogger.get().info("Creating StanfordDocumentParserFactory, docfactory is " + factory.toString());
		docFactory = factory;
		language = lang;
	}

	@Override
	public AbstractDocumentParser create() {
		return new StanfordDocumentParser(docFactory, language);
	}
}