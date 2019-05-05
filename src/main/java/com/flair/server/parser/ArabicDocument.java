/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.Exception;
import java.util.StringTokenizer;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.DocumentReadabilityLevel;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;


//import org.apache.cxf.common.i18n.Exception;
import org.jsoup.select.Evaluator.Class;

import com.flair.server.raft.Raft;
import com.flair.server.utilities.ServerLogger;

/**
 * Represents an arabic text document that's parsed by the NLP Parser and is ranked by RAFT
 * 
 * @author mjbriggs
 */
class ArabicDocument implements AbstractDocument
{
	private final AbstractDocumentSource				source;
	private final double								readabilityScore;
	private final DocumentReadabilityLevel				readabilityLevel;	// calculate from the readability score
	private final ArabicDocumentReadabilityLevel		arabicReadabilityLevel;	// calculate from the readability score
	private final ConstructionDataCollection			constructionData;

	private int											numCharacters;
	private int											numSentences;
	private int											numDependencies;
	private int											numWords;
	private int											numTokens;		// number of words essentially (kinda), later substituted with no of words (without punctuation)

	private double										avgWordLength;		// doesn't include punctuation
	private double										avgSentenceLength;
	private double										avgTreeDepth;

	private double										fancyDocLength;	// ### TODO better name needed, formerly "docLenTfIdf"
	private KeywordSearcherOutput						keywordData;

	private boolean parsed;

	private Raft raft;

	public ArabicDocument(AbstractDocumentSource parent)
	{
        ServerLogger.get().info("Creating arabic document");
		assert parent != null;

		raft = new Raft();

		source = parent;
		constructionData = new ConstructionDataCollection(parent.getLanguage(), new DocumentConstructionDataFactory(this));

		String pageText = source.getSourceText();
		StringTokenizer tokenizer = new StringTokenizer(pageText, " ");

		// calculate readability score, level, etc
		numTokens = tokenizer.countTokens();
		int whitespaceCount = 0;
		for (int i = 0; i < pageText.length(); i++)
		{
			if (pageText.charAt(i) == ' ')
				whitespaceCount++;
		}
		numCharacters = pageText.length() - whitespaceCount;
		tokenizer = new StringTokenizer(pageText, "[.!?]");
		numSentences = tokenizer.countTokens();
		numDependencies = 0;

		double readabilityScoreCalc = 0;
		double readabilityLevelThreshold_A = 0;
		double readabilityLevelThreshold_B = 0;

		double readabilityLevelThreshold_1 = 0;
		double readabilityLevelThreshold_2 = 0;
		double readabilityLevelThreshold_3 = 0;

		/**
		 * The switch statements below classify the readability threshHolds 
		 * Readability thresholds are hard coded here 
		 */
		switch (source.getLanguage())
		{
		case ARABIC:
			readabilityScoreCalc = calculateReadabilityScore(source.getSourceText());
			if(readabilityScoreCalc == 0.0){
				ServerLogger.get().error("RAFT document analysis failed on " + getDescription() + 
				", document number " + raft.getSalt() + ", now using default readability score");
				try {
					File failedSourceText = new File("/tmp/source" + raft.getSalt() + ".txt");
					Writer writer = new BufferedWriter(new OutputStreamWriter
							(new FileOutputStream(failedSourceText), "UTF8"));
					writer.write(source.getSourceText());
					writer.close();
				}
				catch(UnsupportedEncodingException e) {
					ServerLogger.get().error("UNSUPPORTED ENCODING - WRITING FAILED SOURCE TEXT");
					e.printStackTrace();
				}
				catch(IOException e) {
					ServerLogger.get().error("COULD NOT WRITE TO FILE - WRITING FAILED SOURCE TEXT");
					e.printStackTrace();
				}
				readabilityScoreCalc = Math
					.ceil(((double) numCharacters / (double) numTokens) + (numTokens / (double) numSentences));
				readabilityLevelThreshold_A = 10;
				readabilityLevelThreshold_B = 20;

				readabilityLevelThreshold_1 = 10;
				readabilityLevelThreshold_2 = 20;
			}
			else{
				readabilityLevelThreshold_A = 1.1;
				readabilityLevelThreshold_B = 2.1;

				readabilityLevelThreshold_1 = 1.1;
				readabilityLevelThreshold_2 = 2.1;
				readabilityLevelThreshold_3 = 3.1;
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid document language, tried to use a non arabic language with an arabic document object");
		}

		if (numSentences != 0 && numCharacters != 0)		//If num sentences && num characters != 0, we use the readability score we calculated
			readabilityScore = readabilityScoreCalc;
		else												//else we use a negative score, this ensures that we either dont use the document or its is ranked as easiest
			readabilityScore = -10.0;
															// Below we detirmine DocumentReadabilityLevel tag for the client
		if (readabilityScore < readabilityLevelThreshold_A)		
			readabilityLevel = DocumentReadabilityLevel.LEVEL_A;
		else if (readabilityLevelThreshold_A <= readabilityScore && readabilityScore <= readabilityLevelThreshold_B)
			readabilityLevel = DocumentReadabilityLevel.LEVEL_B;
		else
			readabilityLevel = DocumentReadabilityLevel.LEVEL_C;

		if (readabilityScore < readabilityLevelThreshold_1)		
			arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_1;
		else if (readabilityLevelThreshold_1 <= readabilityScore && readabilityScore <= readabilityLevelThreshold_2)		
		arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_2;
		else if (readabilityLevelThreshold_2 <= readabilityScore && readabilityScore <= readabilityLevelThreshold_3)
		arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_3;
		else 
			arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_4;
		
		ServerLogger.get().info("arabicReadabilityLevel = " + arabicReadabilityLevel.toString());

		avgWordLength = avgSentenceLength = avgTreeDepth = fancyDocLength = 0;
		keywordData = null;
		parsed = false;
	}

	public double calculateReadabilityScore(String source) { 
		//throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception
		double readabilityLevel = 0;
		try{
			readabilityLevel = raft.ScoreText(source);	//throws a bunch of exceptions so just catch the most general case
			ServerLogger.get().info("For document " + getDescription() + " number is " + raft.getSalt());
		}
		catch(Exception ex){
			ServerLogger.get().error(ex.toString());
			StringWriter errors = new StringWriter();
			ex.printStackTrace(new PrintWriter(errors));
			ServerLogger.get().error(errors.toString());
			return 0;
		}
		return readabilityLevel;
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
				sumOfPowers += Math.pow(data.getWeightedFrequency(), 2);
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

class ArabicDocumentFactory implements AbstractDocumentFactory
{
	@Override
	public AbstractDocument create(AbstractDocumentSource source) {
		return new ArabicDocument(source);
	}
}