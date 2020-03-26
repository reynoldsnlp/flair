/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flair.server.utilities.Madamira;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;
import com.flair.shared.grammar.GrammaticalConstruction;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.semgraph.semgrex.Alignment;
import edu.stanford.nlp.util.CoreMap;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class StanfordDocumentParserArabicStrategy extends BasicStanfordDocumentParserStrategy {
	private ArabicDocument workingDoc;
	private int tokenCount;
	private int wordCount;
	private int characterCount;
	private int sentenceCount;
	private int depthCount;
	private int dependencyCount;
	private int adjCount;
    
	private static final String WORD_PATTERN = "[\\u0600-\\u06FF]+"; // TODO add Ёё and U+0300 and U+0301 and more? TODO test

    public StanfordDocumentParserArabicStrategy()
    {

	//pipeline = null;
    }

    public void setPipeline(StanfordCoreNLP pipeline)
    {
    	assert pipeline != null;
    	this.pipeline = pipeline;
    }

    public boolean	isLanguageSupported(Language lang){
        return true;
    }
    private void initializeState(AbstractDocument doc) {
		if (pipeline == null)
		{
			throw new IllegalStateException("Parser not set");
		} else if (isLanguageSupported(doc.getLanguage()) == false)
		{
			throw new IllegalArgumentException("Document language " + doc.getLanguage()
					+ " not supported (Strategy language: " + Language.ARABIC + ")");
		}

		workingDoc = (ArabicDocument) doc;
	}
    
    private void resetState() {
    /*wordCount = tokenCount = dependencyCount = sentenceCount = depthCount = characterCount = goingToFound = 0;
		treeOutput = null;
		wordsOutput = null;
		depsOutput = null;
		conditionalFound = false;
		usedFound = false;
		comparativeMoreFound = false;
		superlativeMostFound = false;*/
		wordCount = tokenCount = characterCount = sentenceCount = dependencyCount = depthCount = 0;
		pipeline = null;
		workingDoc = null;
	}
    
    public boolean	apply(AbstractDocument docToParse){
		assert docToParse != null;
		int attempts = 0; 
		try
		{
			initializeState(docToParse);

			Annotation docAnnotation = new Annotation(workingDoc.getPageText());
			pipeline.annotate(docAnnotation);

			Document madaOutput = workingDoc.getMadaOutput();

			List<CoreMap> stanfordSents = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);

			List<MadaToken> madaTokens = getMadaTokens(madaOutput);
			List<String> tokens = new ArrayList<String>();

			for (MadaToken madaToken: madaTokens) {
				String token = madaToken.getToken().attr("form0").replaceAll("\\+", "");
				tokens.add(Pattern.quote(token)/*
					.replaceAll("^" + "ا",  "ا"+"?")
					.replaceAll("-LRB-", "\\\\(")
					.replaceAll("-RRB-", "\\\\)")*/);
			}

			StringBuilder sb = new StringBuilder();
			for (String token: tokens)
				sb.append(token);

			String joinedMadaTokens = sb.toString();

			Annotation docAnnotation2 = new Annotation(joinedMadaTokens);
			pipeline.annotate(docAnnotation2);

			List<CoreMap> madaSents = docAnnotation2.get(CoreAnnotations.SentencesAnnotation.class);

			for (int i = 0; i < stanfordSents.size(); i++) {

			}


			inspectText(madaTokens, stanfordSents, madaSents);

			/*// update doc properties
			workingDoc.setAvgSentenceLength((double) wordCount / (double) sentenceCount);
			//workingDoc.setAvgTreeDepth((double) depthCount / (double) sentenceCount);
			workingDoc.setAvgWordLength((double) characterCount / (double) wordCount);
			workingDoc.setLength(wordCount);
			workingDoc.setNumDependencies(dependencyCount);
			workingDoc.setNumWords(wordCount);
			workingDoc.setNumTokens(tokenCount);
			workingDoc.setNumSentences(sentenceCount);
			workingDoc.setNumCharacters(characterCount);
			workingDoc.flagAsParsed();*/
		} finally
		{
			resetState();
		}

        return true;
    }

    private List<MadaToken> getMadaTokens(Document madaOutput) {
		//Create list of tuples containing each ATB4MT token and morph_feature_set of the word the token is in
		ArrayList<MadaToken> madaTokens = new ArrayList<MadaToken>();

		String preprocessed = madaOutput.getElementsByTag("preprocessed").text();
		//System.out.println(preprocessed);
/*
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("theText.txt"), "UTF-8"));
			writer.write(preprocessed);
			for(int i = 0; i < madaTokens.size(); i++) {
				//System.out.println(madaTokens.get(i).getValue0().getElementsByTag("form0").toString() + ".............." + stanfordWords.get(i).word());
				String madaToken = madaTokens.get(i).getToken().attr("form0");
				System.out.println(madaToken);
				writer.write("\n" + madaToken);
			}
			System.out.println("theTxt.txt file written");
		}

		catch (IOException e){}*/


		Elements madaWords = madaOutput.getElementsByTag("word");
		for(Element madaWord : madaWords) {
			Elements tokenizeds = madaWord.getElementsByTag("tokenized");
			for (Element tokenized: tokenizeds) {
				Elements toks = tokenized.getElementsByTag("tok");
				System.out.println(madaWord.attr("word"));
				List<Element> analysis = madaWord.getElementsByTag("analysis");
				if (analysis.size() > 0) {
					Element morphFeatureSet = analysis.get(0).getElementsByTag("morph_feature_set").get(0);
					for(Element tok: toks)
						madaTokens.add(new MadaToken(tok, morphFeatureSet));
				}
				else {
					for(Element tok: toks)
						madaTokens.add(new MadaToken(tok, null));
				}

			}
		}

		return madaTokens;
	}

	private List<CoreLabel> getStanfordWords(List<CoreMap> sentences) {
		List<CoreLabel> words = new ArrayList<CoreLabel>();

		for(CoreMap itr: sentences) {
			for (CoreLabel word: itr.get(CoreAnnotations.TokensAnnotation.class)) {
				Character w = word.word().charAt(0);
					words.add(word);
			}
		}

		return words;
	}

    private void inspectText(/*Tree tree,*/ List<MadaToken> madaTokens, List<CoreMap> stanfordSents, List<CoreMap> madaSents/*, Collection<TypedDependency> deps*/) {
		if (madaTokens == null || madaTokens.isEmpty() ||
				stanfordSents == null || stanfordSents.isEmpty() ||
				madaSents == null || madaSents.isEmpty())
			return;

		for(MadaToken madaToken: madaTokens) {
			StringBuilder sb = new StringBuilder(workingDoc.getText());
			String token = madaToken.getToken().attr("form0");
			madaToken.setIndices(sb.toString().indexOf(token));

			for (int i = madaToken.getStartIndex(); i <= madaToken.getEndIndex(); i++)
				sb.setCharAt(i, ' ');

			if (madaToken.getMorphFeatureSet().attr("pos").equals("prep")) {
				addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, madaToken.getStartIndex(), madaToken.getEndIndex());
			}
		}


		/*for (int i = 0; i < stanfordSents.size(); i++) {
			SemanticGraph stanfordSemGraph = stanfordSents.get(i).get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
			SemanticGraph madaSemGraph = madaSents.get(i).get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);


		}*/

		/*String text = workingDoc.getPageText()
				.replaceAll("ّ", "")
				.replaceAll("َ", "")
				.replaceAll("ِ", "")
				.replaceAll("ُ", "")
				.replaceAll("ً", "")
				.replaceAll("ٍ", "")
				.replaceAll("ْ", "")
				.replaceAll("ٌ", "");*/



/*
		String joined = new StringBuilder()
				.append("(")
				.append(String.join(")\\s*(", tokens))
				.append(")")
				.toString();
		System.out.println(joined);

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("tokens.txt"), "UTF-8"));
			writer.write(workingDoc.getPageText());
			writer.write("\n" + joined);
			System.out.println("tokens.txt file written");
			writer.close();
		}

		catch (IOException e){}

		Matcher matcher = Pattern.compile(joined).matcher(text);
		String mask = matcher.replaceAll("+").replaceAll("[^+]", "-");

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("mistakes.txt"), "UTF-8"));
			writer.write(text);
			writer.write(mask);
			writer.close();
		}

		catch (IOException e){}

		System.out.println(matcher.matches());

		for (int i = 0; i < madaTokens.size() ; i++) {
			String pos = madaTokens.get(i).getToken().attr("form5");
			System.out.println(pos);
			if (pos.substring(0,4).equals("PREP")) {
				int start_index = matcher.start(i + 1);
				int end_index = matcher.end(i + 1);
				addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, start_index, end_index);
			}
		}*/

		/**/
		/*
		for (CoreLabel word: words) {
			String POS = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			//System.out.println(POS);
			if (POS.equals("IN")) {
				addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, word.beginPosition(), word.endPosition());
			}
			if (POS.equals("PRP")) {
				addConstructionByIndices(GrammaticalConstruction.PRONOUNS, word.beginPosition(), word.endPosition());
			}
			if (POS.equals("VBG")) {
				addConstructionByIndices(GrammaticalConstruction.VERBAL_NOUN, word.beginPosition(), word.endPosition());
			}
		}*/

	}

	private void addConstructionByIndices(GrammaticalConstruction type, int startIndex, int endIndex){
		workingDoc.getConstructionData(type).addOccurrence(startIndex, endIndex);
	}

}

class MadaToken {
	private Element token;
	private Element morphFeatureSet;
	private int startIndex;
	private int endIndex;

	MadaToken(Element token, Element morphFeatureSet) {
		this.token = token;
		this.morphFeatureSet = morphFeatureSet;
	}

	public Element getToken() {
		return token;
	}

	public Element getMorphFeatureSet() {
		return morphFeatureSet;
	}

	public void setToken(Element token) {
		this.token = token;
	}

	public void setMorphFeatureSet(Element morphFeatureSet) {
		this.morphFeatureSet = morphFeatureSet;
	}

	public void setIndices(int startIndex) {
		this.startIndex = startIndex;
		endIndex = startIndex + token.attr("form0").length() - 1;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return getEndIndex();
	}

}