/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.util.Collection;
import java.util.List;

import com.flair.server.grammar.RussianGrammaticalTreePatterns;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;



class StanfordDocumentParserRussianStrategy extends BasicStanfordDocumentParserStrategy {
	private AbstractDocument workingDoc;
	private int tokenCount;
	private int wordCount;
	private int characterCount;
	private int sentenceCount;
	private int depthCount;
	private int dependencyCount;
	private int adjCount;

	private static final String WORD_PATTERN = "[\\p{IsCyrillic}\u0300\u0301]+"; //not sure if this regex is correct for including all number of russian words. EDIT: regex has been changed to handle the two accents over letters

    public StanfordDocumentParserRussianStrategy()
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
					+ " not supported (Strategy language: " + Language.RUSSIAN + ")");
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

	private void inspectSentence(Tree tree, List<CoreLabel> words) {
		if(tree == null){
			ServerLogger.get().info("Received a null tree to inspect sentence in the RussianStrategy");
			return;
		}
		if (words == null || words.isEmpty()) {
			return;
		}
		int numLIs = countMatches(RussianGrammaticalTreePatterns.patternLi, tree);
		int numConditionals = countMatches(RussianGrammaticalTreePatterns.patternBi, tree);
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

					inspectSentence(tree, words);

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
					//inspectSentence(tree, words, dependencies);
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

	/**
	 * Counts the number of matches to a specific TregexPattern within a parse tree
	 * @param pattern TregexPattern to be matched against the Tree
	 * @param tree Dependency tree
	 * @return number of matches to the tregex pattern
	 */
	private int countMatches(TregexPattern pattern, Tree tree) {
		int matches = 0;
		TregexMatcher matcher = pattern.matcher(tree);
		while (matcher.findNextMatchingNode())	//while the matcher can find the next match to the pattern, increment the number of matches
		{
			matches++;
		}
		return matches;
	}

}
