/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

import java.util.StringTokenizer;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;
import com.flair.shared.parser.DocumentReadabilityLevel;

/**
 * Represents a text document that's parsed by the NLP Parser
 *
 * @author shadeMe
 */
class Document implements AbstractDocument
{
	private final AbstractDocumentSource				source;
	private final double								readabilityScore;
	private final DocumentReadabilityLevel				readabilityLevel;	// calculate from the readability score
	private final ConstructionDataCollection			constructionData;

	private int											numCharacters;
	private int											numSentences;
	private int											numDependencies;
	private int											numWords;
	private int											numTokens;		// number of words essentially (kinda), later substituted with no of words (without punctuation)

	private double										avgWordLength;		// doesn't include punctuation
	private double										avgSentenceLength;
	private double										avgTreeDepth;

	private double										fancyDocLength;
	private KeywordSearcherOutput						keywordData;

	private boolean parsed;

	public Document(AbstractDocumentSource source, double readabilityScore,
			DocumentReadabilityLevel readabilityLevel)
	{
		this.source = source;
		this.readabilityScore = readabilityScore;
		this.readabilityLevel = readabilityLevel;
		this.constructionData = new ConstructionDataCollection(this.source.getLanguage(), new DocumentConstructionDataFactory(this));
	}
	public Document(AbstractDocumentSource parent)
	{
		ServerLogger.get().info("Creating document");
		assert parent != null;

		source = parent;
		constructionData = new ConstructionDataCollection(parent.getLanguage(), new DocumentConstructionDataFactory(this));

		String pageText = source.getSourceText();
		StringTokenizer tokenizer = new StringTokenizer(pageText, " ");

		// calculate readability score, level, etc
		numTokens = tokenizer.countTokens();
		int whitespaceCount = 0;
		for (int i = 0; i < pageText.length(); i++)
		{
			if (Character.isWhitespace(pageText.charAt(i)))
				whitespaceCount++;
		}
		numCharacters = pageText.length() - whitespaceCount;
		tokenizer = new StringTokenizer(pageText, "[.!?]");
		numSentences = tokenizer.countTokens();
		numDependencies = 0;

		double readabilityScoreCalc = 0;
		double readabilityLevelThreshold_A = 0;
		double readabilityLevelThreshold_B = 0;

		/**
		 * The switch statements below classify the readability threshHolds
		 * Readability thresholds are hard coded here
		 */
		switch (source.getLanguage())
		{
		case ARABIC:
			throw new IllegalArgumentException("Invalid document language, use the ArabicDocument object for arabic text");
		case ENGLISH:
			readabilityScoreCalc = Math.ceil(4.71 * ((double) numCharacters / (double) numTokens)
					+ 0.5 * (numTokens / (double) numSentences) - 21.43);
			readabilityLevelThreshold_A = 7;
			readabilityLevelThreshold_B = 12;
			break;
		case GERMAN:
			readabilityScoreCalc = Math
					.ceil(((double) numCharacters / (double) numTokens) + (numTokens / (double) numSentences));
			readabilityLevelThreshold_A = 10;
			readabilityLevelThreshold_B = 20;
			break;
		case PERSIAN:  //TODO implement real Flesch-Kincaid coefficients
			readabilityScoreCalc = Math
					.ceil(((double) numCharacters / (double) numTokens) + (numTokens / (double) numSentences));
			readabilityLevelThreshold_A = 10;
			readabilityLevelThreshold_B = 20;
			break;
		case RUSSIAN:  //TODO implement real Flesch-Kincaid coefficients
			readabilityScoreCalc = Math
					.ceil(((double) numCharacters / (double) numTokens) + (numTokens / (double) numSentences));
			readabilityLevelThreshold_A = 10;
			readabilityLevelThreshold_B = 20;
			break;
		default:
			throw new IllegalArgumentException("Invalid document language");
		}

		if (numSentences != 0 && numCharacters != 0)		//If num sentences && num characters != 0, we use the readability score we calculated
			readabilityScore = readabilityScoreCalc;
		else												//else we use a negative score, this ensures that we either dont use the document or its is ranked as easiest
			readabilityScore = -10.0;
															// Below we determine DocumentReadabilityLevel tag for the client
		if (readabilityScore < readabilityLevelThreshold_A)
			readabilityLevel = DocumentReadabilityLevel.LEVEL_A;
		else if (readabilityLevelThreshold_A <= readabilityScore && readabilityScore <= readabilityLevelThreshold_B)
			readabilityLevel = DocumentReadabilityLevel.LEVEL_B;
		else
			readabilityLevel = DocumentReadabilityLevel.LEVEL_C;

		avgWordLength = avgSentenceLength = avgTreeDepth = fancyDocLength = 0;
		keywordData = null;
		parsed = false;
	}

	@Override
	public Language getLanguage() {
		return source.getLanguage();
	}

	@Override
	public String getText() {
		return source.getSourceText();
	}

	@Override
	public String getDescription() {
		return "{" + source.getDescription() + " | S[" + numSentences + "], C[" + numCharacters + "]" + "}";
	}

	@Override
	public DocumentConstructionData getConstructionData(GrammaticalConstruction type) {
		return (DocumentConstructionData) constructionData.getData(type);
	}

	public void calculateFancyDocLength()
	{
		double sumOfPowers = 0.0;
		double squareRoot = 0.0;
		// iterate through the construction data set and calc
		for (GrammaticalConstruction itr : getSupportedConstructions())
		{
			DocumentConstructionData data = getConstructionData(itr);
			if (data.hasConstruction())
			{
				sumOfPowers += Math.pow(data.getWeightedFrequency(), 2);
			}
		}

		if (sumOfPowers > 0)
			squareRoot = Math.sqrt(sumOfPowers);

		fancyDocLength = squareRoot;
	}

	@Override
	public double getReadabilityScore() {
		return readabilityScore;
	}

	@Override
	public DocumentReadabilityLevel getReadabilityLevel() {
		return readabilityLevel;
	}

	@Override
	public ArabicDocumentReadabilityLevel getArabicReadabilityLevel() {
		ServerLogger.get().info("Arabic readability not supported by this class");
		return null;
	}

	@Override
	public int getNumCharacters() {
		return numCharacters;
	}

	@Override
	public int getNumSentences() {
		return numSentences;
	}

	@Override
	public int getNumDependencies() {
		return numDependencies;
	}

	@Override
	public int getNumWords() {
		return numWords;
	}

	@Override
	public int getNumTokens() {
		return numTokens;
	}

	@Override
	public double getAvgWordLength() {
		return avgWordLength;
	}

	@Override
	public double getAvgSentenceLength() {
		return avgSentenceLength;
	}

	@Override
	public double getAvgTreeDepth() {
		return avgTreeDepth;
	}

	@Override
	public int getLength() {
		return numWords;
	}

	@Override
	public double getFancyLength() {
		return fancyDocLength;
	}

	@Override
	public void setNumCharacters(int value) {
		numCharacters = value;
	}

	@Override
	public void setNumSentences(int value) {
		numSentences = value;
	}

	@Override
	public void setNumDependencies(int value) {
		numDependencies = value;
	}

	@Override
	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	@Override
	public void setNumTokens(int numTokens) {
		this.numTokens = numTokens;
	}

	@Override
	public void setAvgWordLength(double value) {
		avgWordLength = value;
	}

	@Override
	public void setAvgSentenceLength(double value) {
		avgSentenceLength = value;
	}

	@Override
	public void setAvgTreeDepth(double value) {
		avgTreeDepth = value;
	}

	@Override
	public void setLength(int value) {
		numWords = value;
	}

	@Override
	public boolean isParsed() {
		return parsed;
	}

	@Override
	public void flagAsParsed() {
		if (parsed)
			throw new IllegalStateException("Document already flagged as parsed");

		parsed = true;
		calculateFancyDocLength();
	}

	@Override
	public AbstractDocumentSource getDocumentSource() {
		return source;
	}

	@Override
	public int compareTo(AbstractDocument t) {
		return source.compareTo(t.getDocumentSource());
	}

	@Override
	public KeywordSearcherOutput getKeywordData() {
		return keywordData;
	}

	@Override
	public void setKeywordData(KeywordSearcherOutput data) {
		keywordData = data;
	}

	@Override
	public Iterable<GrammaticalConstruction> getSupportedConstructions() {
		return GrammaticalConstruction.getForLanguage(getLanguage());
	}
	@Override
	public String toString(){
		return "Document : " + getDescription() +
		"\nAverage Sentence Length : " + getAvgSentenceLength() +
		"\nAverage Word Length : " + getAvgWordLength() +
		"\nLength : " + getLength() +
		"\nNumber of Dependencies : " + getNumDependencies() +
		"\nNumber of Words : " + getNumWords() +
		"\nNumber of Tokens : " + getNumTokens() +
		"\nNumber of Sentences : " + getNumSentences() +
		"\nNumber of Characters : " + getNumCharacters() +
		"\nIs Parsed : " + isParsed() + "\n";

	}
}

class DocumentFactory implements AbstractDocumentFactory
{
	@Override
	public AbstractDocument create(AbstractDocumentSource source) {
		//this is called after crawl step
		ServerLogger.get().info("DocumentFactory.create()");
		return new Document(source);
	}
}
