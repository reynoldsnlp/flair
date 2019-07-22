/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.flair.server.grammar.RussianGrammaticalPatterns;
import com.flair.server.utilities.CgConv;
import com.flair.server.utilities.HFSTAnalyser;
import com.flair.server.utilities.HFSTAnalyser.TransducerStreamException;
import com.flair.server.utilities.ServerLogger;
import com.flair.server.utilities.VislCg3;
import com.flair.server.utilities.cg3parser.Cg3Parser;
import com.flair.server.utilities.cg3parser.WordWithReadings;
import com.flair.shared.grammar.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.util.CoreMap;

class StanfordDocumentParserRussianStrategy extends BasicStanfordDocumentParserStrategy {
    private static final String RUSSIAN_TRANSDUCER_HFSTOL = "/analyser-gt-desc.hfstol";
    private static HFSTAnalyser analyser;
    private AbstractDocument workingDoc;
    private int tokenCount;
    private int wordCount;
    private int characterCount;
    private int sentenceCount;
    private int depthCount;
    private int dependencyCount;
    private int adjCount;
    private static final String WORD_PATTERN = "[\\p{IsCyrillic}\u0300\u0301]+";

    public StanfordDocumentParserRussianStrategy() {
        //pipeline = null;
        //set up the HFST
        try {
            InputStream russianTransducerStream = this.getClass().getResourceAsStream(RUSSIAN_TRANSDUCER_HFSTOL);
            analyser = new HFSTAnalyser(russianTransducerStream);
        } catch (TransducerStreamException e) {
            ServerLogger.get().error(e, "Russian Strategy could not initialize the HFSTAnalyser");
        }
        //TODO: set up the CG3 (Constraint Grammar)

    }

    public void setPipeline(StanfordCoreNLP pipeline) {
        assert pipeline != null;
        this.pipeline = pipeline;
    }

    public boolean	isLanguageSupported(Language lang){
        return true;
        //return (lang == Language.RUSSIAN);
    }
    private void initializeState(AbstractDocument doc) {
        if (pipeline == null)
        {
            throw new IllegalStateException("Parser not set");
        } else if (!isLanguageSupported(doc.getLanguage()))
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

    public boolean	apply(AbstractDocument docToParse){ //TODO
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
                    SemanticGraph graph = itr.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                    List<CoreLabel> words = itr.get(CoreAnnotations.TokensAnnotation.class);
					/*Collection<TypedDependency> dependencies = itr
							.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class)
							.typedDependencies();
							*/

                    inspectSentence(graph, words);

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

    private static List<CoreLabel> indexedWordsToCoreLabels(List<IndexedWord> indexedWords){
        List<CoreLabel> coreLabels = new ArrayList<>(indexedWords.size());
        for(IndexedWord word : indexedWords){
            coreLabels.add(word.backingLabel());
        }
        return coreLabels;
    }

    private static List<String> indexedWordsToStrings(List<CoreLabel> coreLabels){
        List<String> strings = new ArrayList<>(coreLabels.size());
        for(CoreLabel label : coreLabels){
            strings.add(label.word());
        }
        return strings;
    }

    private void inspectVerbs(SemanticGraph graph, List<CoreLabel> words){
        //extract verbs from the graph
        List<IndexedWord> verbs = graph.getAllNodesByPartOfSpeechPattern("VERB");
        List<CoreLabel> verbCoreLabels = indexedWordsToCoreLabels(verbs);
        //count constructions
        int numReflexives = countMatches(RussianGrammaticalPatterns.patternReflexiveVerb, verbCoreLabels);
        ServerLogger.get().info("NUMBER OF REFLEXIVES FOUND: " + numReflexives);
        //TODO
    }

    private void inspectSentence(SemanticGraph graph, List<CoreLabel> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        String wordsWithLemmas = analyser.runTransducer(indexedWordsToStrings(words));
        ServerLogger.get().info("Transducer results received");
        String cgForm;
        try {
            cgForm = CgConv.hfstToCg(wordsWithLemmas);
            ServerLogger.get().info("Transducer results converted to cg3 format");
            //System.out.println("cgForm:\n" + cgForm);
            String finalReadings = VislCg3.runVislCg3(cgForm);
            if(!finalReadings.isEmpty()) {
                ServerLogger.get().info("Readings have been reduced by the constraint grammar");
                System.out.println("readings:\n" + finalReadings);
                Cg3Parser parser = new Cg3Parser(finalReadings);
                List<WordWithReadings> readingsList = parser.parse();
                //TODO: use the readings
            }
            else {
                ServerLogger.get().info("There was an error using the constraint grammar");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        int numLIs = countMatches(RussianGrammaticalPatterns.patternLi, words);
        int numConditionals = countMatches(RussianGrammaticalPatterns.patternBi, words);
        inspectVerbs(graph, words);
        //TODO: save data to the document
    }

    /**
     * Counts the number of matches to a specific SemgrexPattern within a parse graph
     * @param pattern SemgrexPattern to be matched against the graph
     * @param graph Dependency graph
     * @return number of matches to the SemgrexPattern
     */
    private int countMatches(SemgrexPattern pattern, SemanticGraph graph) {
        int matches = 0;
        SemgrexMatcher m = pattern.matcher(graph);
        while(m.find()){
            matches++;
        }
        return matches;
    }

    /**
     * Counts the number of matches to a specific regex Pattern for individual words
     * @param pattern Pattern to be matched against each word
     * @param words   List of models that contain individual word values
     * @return number of matches to the regex pattern
     */
    private int countMatches(Pattern pattern, List<CoreLabel> words){
        int matches = 0;
        for(CoreLabel word : words){
            final String wordValue = word.value();
            Matcher m = pattern.matcher(wordValue);
            while(m.find()){
                matches++;
            }
        }
        return matches;
    }
}
