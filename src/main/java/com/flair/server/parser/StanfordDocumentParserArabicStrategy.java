/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.util.ArrayList;
import java.util.List;

import com.flair.server.raft.Madamira;
import com.flair.server.raft.Raft;
import com.flair.shared.grammar.Language;
import com.flair.shared.grammar.GrammaticalConstruction;

import edu.columbia.ccls.madamira.configuration.MorphFeatureSet;
import edu.columbia.ccls.madamira.configuration.OutDoc;
import edu.columbia.ccls.madamira.configuration.Tok;
import edu.columbia.ccls.madamira.configuration.Word;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class StanfordDocumentParserArabicStrategy extends BasicStanfordDocumentParserStrategy {
	private AbstractDocument workingDoc;
	private int tokenCount;
	private int wordCount;
	private int characterCount;
	private int sentenceCount;
	private int depthCount;
	private int dependencyCount;
	private int adjCount;

	//private List<Word> madamiraWords;
    
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

		workingDoc = doc;
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

			Annotation docAnnotation = new Annotation(workingDoc.getText());
			pipeline.annotate(docAnnotation);

			List<CoreMap> sentences = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
			for (CoreMap itr : sentences)
			{
				/*if(attempts % 20 == 0){
					ServerLogger.get().info("Parsing " + docToParse.getDescription() + "...");
				}*/

				if (itr.size() > 0)
				{
					Tree tree = itr.get(TreeCoreAnnotations.TreeAnnotation.class);
					List<CoreLabel> words = itr.get(CoreAnnotations.TokensAnnotation.class);
					/*Collection<TypedDependency> dependencies = itr
							.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class)
							.typedDependencies();
							*/

					sentenceCount++;
					//dependencyCount += dependencies.size();
					//depthCount += tree.depth();

					// changed: only count words (no punctuation)
					for (CoreLabel cl : words)
					{
						tokenCount++;
						if (!cl.tag().startsWith("$"))
						{
							dependencyCount += 1;
						}		
						if (cl.value().toLowerCase().matches(WORD_PATTERN))
						{
							wordCount++;
							characterCount += cl.value().length();
						}
					}

					// extract gram.structures
					inspectSentence(words);
				}
			}

			// update doc properties
			workingDoc.setAvgSentenceLength((double) wordCount / (double) sentenceCount);
			//workingDoc.setAvgTreeDepth((double) depthCount / (double) sentenceCount);
			workingDoc.setAvgWordLength((double) characterCount / (double) wordCount);
			workingDoc.setLength(wordCount);
			workingDoc.setNumDependencies(dependencyCount);
			workingDoc.setNumWords(wordCount);
			workingDoc.setNumTokens(tokenCount);
			workingDoc.setNumSentences(sentenceCount);
			workingDoc.setNumCharacters(characterCount);
			workingDoc.flagAsParsed();
		} finally
		{
			resetState();
		}

        return true;
    }

    private void inspectSentence(/*Tree tree,*/ List<CoreLabel> words/*, Collection<TypedDependency> deps*/) {
		if (words == null || words.isEmpty())
		{
			return;
		}

	/*	for (CoreLabel word: words) {
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
		}
		*/
		// DO IT THIS WAY!!!
		List <Word> madamiraWords = MadamiraAPI.getInstance().getOutDoc().getOutSeg().get(0).getWordInfo().getWord();
		ArrayList<Pair<List<Tok>, MorphFeatureSet>> pairs = new ArrayList<Pair<List<Tok>, MorphFeatureSet>>();

		for (int i = 0; i < madamiraWords.size() ; i++) {
			Word word = madamiraWords.get(i);

			//TODO: create list of tuples containing ATB tokens, BW pos tag, and madamira morph_feature_set
			//We'll then be able access its indices from the aligned token with stanfordCoreNLP.

			List<Tok> toks = word.getTokenized().get(0).getTok();
			MorphFeatureSet morphFeatureSet = word.getAnalysis().get(0).getMorphFeatureSet();
			pairs.add(new Pair<List<Tok>, MorphFeatureSet>(toks, morphFeatureSet));

			/*String POS = word.getAnalysis().get(0).getMorphFeatureSet().getPos();
			if (POS.equals("prep")) {
				addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, 0, 0);
			}
			if (POS.equals("pron")) {
				addConstructionByIndices(GrammaticalConstruction.PRONOUNS, 0, 0);
			}*/

		}


		//NOT THIS WAY!
		/*ArrayList<MadamiraWord> madamiraWords = new ArrayList<MadamiraWord>();

		Document madaOutput = Raft.getInstance().getFeatureExtractor().getMadaOutput();

		Elements myWords = madaOutput.getElementsByTag("word");
		for (Element word : myWords) {
			Attributes wordAttributes = word.attributes();
			String myWord = wordAttributes.get("word");

			ArrayList<MadamiraWord> tokens_in_this_word = new ArrayList<>();

			Elements schemes = word.getElementsByTag("tokenized");
			for (Element scheme: schemes)
			{
				Elements tokens = scheme.getElementsByTag("tok");
				for (Element token : tokens)
				{
					MadamiraWord madamiraWord = new MadamiraWord();
					String tokenString = token.attributes().get("form0");
					tokenString.replaceAll("/+", "");
					madamiraWord.setToken(tokenString);
					tokens_in_this_word.add(madamiraWord);
				}
			}

			Elements analysis = word.getElementsByTag("analysis");
			for (Element item : analysis)
			{
				Elements morphs = item.getElementsByTag("morph_feature_set");
				for (Element morph : morphs)
				{
					Attributes morphAttributes = morph.attributes();
					*//*for(MadamiraWord madamiraWord : tokens_in_this_word)
					{
						madamiraWord.setPos(morphAttributes.get("pos"));
					}*//*
				}
			}

			for(MadamiraWord token : tokens_in_this_word)
			{
				madamiraWords.add(token);
			}
		}

		for(int i = 0; i < madamiraWords.size(); i++)
		{
			String tok = madamiraWords.get(i).getToken();
			String pos = madamiraWords.get(i).getPos();
			int words_size = words.size();

			if(pos != null) {
				if (pos.equals("prep")) {
					int start = words.get(i).beginPosition();
					int end = words.get(i).endPosition();
					addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, start, end);
				}
				if (pos.equals("pron")) {
					int start = words.get(i).beginPosition();
					int end = words.get(i).endPosition();
					addConstructionByIndices(GrammaticalConstruction.PRONOUNS, start, end);
				}
			}
		}*/
	}

	private void addConstructionByIndices(GrammaticalConstruction type, int startIndex, int endIndex){
		workingDoc.getConstructionData(type).addOccurrence(startIndex, endIndex);
	}
}