/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

import com.flair.server.utilities.ServerLogger;
import com.flair.server.stanza.StanzaPipeline;
import com.flair.shared.grammar.Language;

/**
 * Implementation of the AbstractDocumentParser that uses the Stanza parser
 *
 * @author shadeMe
 */
class StanzaDocumentParser extends AbstractDocumentParser
{
	private AbstractDocumentSource				docSource;
	private AbstractDocument					outputDoc;
	private BasicStanzaDocumentParserStrategy	parsingStrategy;

	private final StanzaPipeline				pipeline;
	private final Language						modelLanguage;

	/**
	 * Constructor that generates a stanza parser
	 * @param factory Interface used to create parsed documents
	 * @param modelLang Parsing model language
	 */
	public StanzaDocumentParser(AbstractDocumentFactory factory, Language modelLang)
	{
		super(factory);
		ServerLogger.get().info("After super(factory)");

		docSource = null;
		outputDoc = null;
		parsingStrategy = null;
		modelLanguage = modelLang;

		switch (modelLanguage)
		{
		case PERSIAN:
			break;
		default:
			throw new IllegalArgumentException("Invalid model language: " + modelLanguage + "");
		}
			pipeline = new StanzaPipeline();
			ServerLogger.get().info("Successful construction of StanzaDocumentParser");

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
		else if (strategy instanceof BasicStanzaDocumentParserStrategy == false) {
			throw new IllegalArgumentException(
					strategy.getClass() + " is not subclass of " + BasicStanzaDocumentParserStrategy.class);
		}
		else if (isLanguageSupported(source.getLanguage()) == false) {
			throw new IllegalArgumentException("Document language " + source.getLanguage()
					+ " not supported (Model language: " + modelLanguage + ")");
		}

		docSource = source;
		outputDoc = docFactory.create(source);
		parsingStrategy = (BasicStanzaDocumentParserStrategy) strategy;

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
 * Factory class for the Stanza parser
 *
 * @author shadeMe
 */
class StanzaDocumentParserFactory implements AbstractDocumentParserFactory
{
	private final AbstractDocumentFactory	docFactory;
	private final Language					language;

	public StanzaDocumentParserFactory(AbstractDocumentFactory factory, Language lang)
	{
		ServerLogger.get().info("Creating StanzaDocumentParserFactory, docfactory is " + factory.toString());
		docFactory = factory;
		language = lang;
	}

	@Override
	public AbstractDocumentParser create() {
		return new StanzaDocumentParser(docFactory, language);
	}
}
