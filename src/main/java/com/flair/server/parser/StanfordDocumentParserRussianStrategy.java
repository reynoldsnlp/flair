/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.flair.server.grammar.RussianGrammaticalPatterns;
import com.flair.server.utilities.CgConv;
import com.flair.server.utilities.HFSTAnalyser;
import com.flair.server.utilities.HFSTAnalyser.TransducerStreamException;
import com.flair.server.utilities.ServerLogger;
import com.flair.server.utilities.VislCg3;
import com.flair.server.utilities.cg3parser.Cg3Parser;
import com.flair.server.utilities.cg3parser.model.CgReading;
import com.flair.server.utilities.cg3parser.model.WordWithReadings;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
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
    private static final String PREPOSITION_GRAPH_LABEL = "ADP";

    //TAGS
    private final String NOUN_TAG = "N";
    private final String ADJECTIVE_TAG = "A";
    private final String PRONOUN_TAG = "Pron";
    private final String POSSESSIVE_TAG = "Pos";
    private final String DEMONSTRATIVE_TAG = "Dem";
    private final String REFLEXIVE_TAG = "Refl";
    private final String PREPOSITION_TAG = "Pr";
    private final String DETERMINER_TAG = "Det";
    private final String VERB_TAG = "V";
    private final String PREDICATE_TAG = "Pred";
    private final String PAST_TAG = "Pst";
    private final String PRESENT_TAG = "Prs";
    private final String FUTURE_TAG = "Fut";
    private final String INFINITIVE_TAG = "Inf";
    private final String P_PRESENT_ACTIVE_TAG = "PrsAct";
    private final String P_PRESENT_PASSIVE_TAG = "PrsPss";
    private final String P_PAST_ACTIVE_TAG = "PstAct";
    private final String P_PAST_PASSIVE_TAG = "PstPss";
    private final String NOMINATIVE_TAG = "Nom";
    private final String ACCUSATIVE_TAG = "Acc";
    private final String GENITIVE_TAG = "Gen";
    private final String PREPOSITIONAL_TAG = "Loc"; //represents 'locative'
    private final String DATIVE_TAG = "Dat";
    private final String INSTRUMENTAL_TAG = "Ins";
    private final String SUBORDINATE_CLAUSE_TAG = "CS";
    private final String RELATIVE_CLAUSE_TAG = "Rel";
    private final String CC_CLAUSE_TAG = "CC"; //TODO: rename this
    private final String PERSONAL_TAG = "Pers";
    private final String RELATIVE_TAG = "Rel";
    private final String INDEFINITE_TAG = "Indef";
    private final String DEFINITE_TAG = "Def";
    private final String INTERROGATIVE_TAG = "Interr";


    public StanfordDocumentParserRussianStrategy() {
        //pipeline = null;
        //set up the HFST
        try {
            InputStream russianTransducerStream = this.getClass().getResourceAsStream(RUSSIAN_TRANSDUCER_HFSTOL);
            analyser = new HFSTAnalyser(russianTransducerStream);
        } catch (TransducerStreamException e) {
            ServerLogger.get().error(e, "Russian Strategy could not initialize the HFSTAnalyser");
        }
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

    public boolean apply(AbstractDocument docToParse){ //TODO
        assert docToParse != null;
        int attempts = 0;
        try {
            initializeState(docToParse);

            Annotation docAnnotation = new Annotation(workingDoc.getText());
            pipeline.annotate(docAnnotation);

            List<CoreMap> sentences = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap itr : sentences) {
				/*if(attempts % 20 == 0){
					ServerLogger.get().info("Parsing " + docToParse.getDescription() + "...");
				}*/
                if (itr.size() > 0) {
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
                    for (CoreLabel cl : words) {
                        tokenCount++;
                        if (!cl.tag().startsWith("$")) {
                            dependencyCount += 1;
                        }
                        if (cl.value().toLowerCase().matches(WORD_PATTERN)) {
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
        } finally {
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

    private void inspectVerbs(SemanticGraph graph){
        //extract verbs from the graph
        List<IndexedWord> verbs = graph.getAllNodesByPartOfSpeechPattern("VERB");
        List<CoreLabel> verbCoreLabels = indexedWordsToCoreLabels(verbs);
        //count constructions
        List<CoreLabel> reflexiveVerbs = findMatches(RussianGrammaticalPatterns.patternReflexiveVerb, verbCoreLabels);
        addConstructionOccurrences(GrammaticalConstruction.VERB_REFLEXIVE, reflexiveVerbs);
    }

    private void inspectPrepositions(SemanticGraph graph){
        //extract verbs from the graph
        List<IndexedWord> prepositions = graph.getAllNodesByPartOfSpeechPattern(PREPOSITION_GRAPH_LABEL);
        List<CoreLabel> prepositionCoreLabels = indexedWordsToCoreLabels(prepositions);
        //count constructions
        addConstructionOccurrences(GrammaticalConstruction.PREPOSITIONS, prepositionCoreLabels);
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
                //System.out.println("readings:\n" + finalReadings);
                Cg3Parser parser = new Cg3Parser(finalReadings);
                List<WordWithReadings> readingsList = parser.parse();
                //use the reduced readings to count constructions
                Map<GrammaticalConstruction, List<WordWithReadings>> constructionCounts = countGrammaticalConstructions(readingsList);
                Map<GrammaticalConstruction, List<WordWithReadings>> prepositionConstructionCounts = countPrepositionConstructions(readingsList, graph);
                constructionCounts.putAll(prepositionConstructionCounts);
                saveGrammaticalConstructionsToDocument(constructionCounts, words);
                System.out.println("break"); //TODO: remove this
            }
            else {
                ServerLogger.get().info("There was an error using the constraint grammar");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //count things based purely on surface forms, not readings

        //есть
        List<CoreLabel> positiveExistentials = findMatches(RussianGrammaticalPatterns.patternJest, words);
        addConstructionOccurrences(GrammaticalConstruction.EXISTENTIAL_THERE, positiveExistentials);
        //нет
        List<CoreLabel> negativeExistentials = findMatches(RussianGrammaticalPatterns.patternNjet, words);
        addConstructionOccurrences(GrammaticalConstruction.EXISTENTIAL_THERE, negativeExistentials);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER, negativeExistentials);
        //не
        List<CoreLabel> negations = findMatches(RussianGrammaticalPatterns.patternNe, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NOT, negations);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER, negations);
        //ли
        List<CoreLabel> yesNoParticles = findMatches(RussianGrammaticalPatterns.patternLi, words);
        addConstructionOccurrences(GrammaticalConstruction.QUESTIONS_YESNO, yesNoParticles);
        //бы
        List<CoreLabel> conditionals = findMatches(RussianGrammaticalPatterns.patternBi, words);
        addConstructionOccurrences(GrammaticalConstruction.CONDITIONALS, conditionals);

        inspectVerbs(graph);
        inspectPrepositions(graph);
    }

    private Map<GrammaticalConstruction, List<WordWithReadings>> countGrammaticalConstructions(List<WordWithReadings> wordsWithReadings){
        //variables for the whole sentence
        boolean isComplexSentence = false;

        Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances = new HashMap<>();
        for(WordWithReadings word: wordsWithReadings){
            Map<GrammaticalConstruction, Boolean> constructionsToCount = new HashMap<>();

            //recognize which tags are present in this word's readings
            for(CgReading reading: word.getReadings()){
                boolean isNoun = false;
                boolean isAdjective = false;
                boolean isPronoun = false;
                boolean isDeterminer = false;
                boolean isVerb = false;
                boolean isPredicate = false;

                boolean isPast = false;
                boolean isPresent = false;
                boolean isFuture = false;
                boolean isInfinitive = false;

                boolean isPresentActive = false;
                boolean isPresentPassive = false;
                boolean isPastActive = false;
                boolean isPastPassive = false;

                boolean isNominative = false;
                boolean isAccusative = false;
                boolean isGenitive = false;
                boolean isPrepositional = false;
                boolean isDative = false;
                boolean isInstrumental = false;

                boolean isSubordinateClause = false;
                boolean isRelativeClause = false;

                boolean isPersonal = false;
                boolean isRelative = false;
                boolean isPossessive = false;
                boolean isDemonstrative = false;
                boolean isReflexive = false;

                boolean isDefinite = false;
                boolean isIndefinite = false;
                boolean isInterrogative = false;

                Set<String> tags = new HashSet<>(reading.getTags());
                //part of speech
                if(tags.contains(NOUN_TAG)) isNoun = true;
                if(tags.contains(ADJECTIVE_TAG)) isAdjective = true;
                if(tags.contains(PRONOUN_TAG)) isPronoun = true;
                if(tags.contains(DETERMINER_TAG)) isDeterminer = true;
                if(tags.contains(VERB_TAG)) isVerb = true;
                if(tags.contains(PREDICATE_TAG)) isPredicate = true;
                //tense
                if(tags.contains(PAST_TAG)) isPast = true;
                if(tags.contains(PRESENT_TAG)) isPresent = true;
                if(tags.contains(FUTURE_TAG)) isFuture = true;
                if(tags.contains(INFINITIVE_TAG)) isInfinitive = true;
                //participles
                if(tags.contains(P_PRESENT_ACTIVE_TAG)) isPresentActive = true;
                if(tags.contains(P_PRESENT_PASSIVE_TAG)) isPresentPassive = true;
                if(tags.contains(P_PAST_ACTIVE_TAG)) isPastActive = true;
                if(tags.contains(P_PAST_PASSIVE_TAG)) isPastPassive = true;
                //case
                if(tags.contains(NOMINATIVE_TAG)) isNominative = true;
                if(tags.contains(ACCUSATIVE_TAG)) isAccusative = true;
                if(tags.contains(GENITIVE_TAG)) isGenitive = true;
                if(tags.contains(PREPOSITIONAL_TAG)) isPrepositional = true;
                if(tags.contains(DATIVE_TAG)) isDative = true;
                if(tags.contains(INSTRUMENTAL_TAG)) isInstrumental = true;
                //clauses
                if(tags.contains(SUBORDINATE_CLAUSE_TAG)) {
                    isSubordinateClause = true;
                    isComplexSentence = true;
                }
                if(tags.contains(RELATIVE_CLAUSE_TAG)) isRelativeClause = true;
                if(tags.contains(CC_CLAUSE_TAG)) isComplexSentence = true;
                //types of pronouns
                if(tags.contains(PERSONAL_TAG)) isPersonal = true;
                if(tags.contains(RELATIVE_TAG)) isRelative = true;
                if(tags.contains(POSSESSIVE_TAG)) isPossessive = true;
                if(tags.contains(DEMONSTRATIVE_TAG)) isDemonstrative = true;
                if(tags.contains(REFLEXIVE_TAG)) isReflexive = true;
                //definite vs indefinite
                if(tags.contains(DEFINITE_TAG)) isDefinite = true;
                if(tags.contains(INDEFINITE_TAG)) isIndefinite = true;
                if(tags.contains(INTERROGATIVE_TAG)) isInterrogative = true;

                //recognize tag combinations
                if(isNoun){
                    if(isNominative) constructionsToCount.put(GrammaticalConstruction.NOUN_NOMINATIVE, true);
                    if(isAccusative) constructionsToCount.put(GrammaticalConstruction.NOUN_ACCUSATIVE, true);
                    if(isGenitive) constructionsToCount.put(GrammaticalConstruction.NOUN_GENITIVE, true);
                    if(isDative) constructionsToCount.put(GrammaticalConstruction.NOUN_DATIVE, true);
                    if(isPrepositional) constructionsToCount.put(GrammaticalConstruction.NOUN_PREPOSITIONAL, true);
                    if(isInstrumental) constructionsToCount.put(GrammaticalConstruction.NOUN_INSTRUMENTAL, true);
                }
                if(isAdjective){
                    if(isNominative) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_NOMINATIVE, true);
                    if(isAccusative) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_ACCUSATIVE, true);
                    if(isGenitive) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_GENITIVE, true);
                    if(isDative) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_DATIVE, true);
                    if(isPrepositional) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_PREPOSITIONAL, true);
                    if(isInstrumental) constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_INSTRUMENTAL, true);

                    if(!isPredicate) constructionsToCount.put(GrammaticalConstruction.ATTRIBUTES_ADJECTIVE, true);
                }
                if(isPronoun){
                    constructionsToCount.put(GrammaticalConstruction.PRONOUNS, true);
                    if(isPersonal) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_PERSONAL, true);
                    if(isRelative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_RELATIVE, true);
                    if(isPossessive) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_POSSESSIVE, true);
                    if(isDemonstrative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_DEMONSTRATIVE, true);
                    if(isReflexive) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_REFLEXIVE, true);
                    if(isInterrogative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_INTERROGATIVE, true);
                    //nječto, njekto
                    if(isDefinite || isIndefinite) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_INDEFINITE, true);
                    //case
                    if(isNominative) constructionsToCount.put(GrammaticalConstruction.PRONOUN_NOMINATIVE, true);
                    if(isAccusative) constructionsToCount.put(GrammaticalConstruction.PRONOUN_ACCUSATIVE, true);
                    if(isGenitive) constructionsToCount.put(GrammaticalConstruction.PRONOUN_GENITIVE, true);
                    if(isDative) constructionsToCount.put(GrammaticalConstruction.PRONOUN_DATIVE, true);
                    if(isPrepositional) constructionsToCount.put(GrammaticalConstruction.PRONOUN_PREPOSITIONAL, true);
                    if(isInstrumental) constructionsToCount.put(GrammaticalConstruction.PRONOUN_INSTRUMENTAL, true);
                }
                if(isDeterminer){
                    if(isNominative) constructionsToCount.put(GrammaticalConstruction.DETERMINER_NOMINATIVE, true);
                    if(isAccusative) constructionsToCount.put(GrammaticalConstruction.DETERMINER_ACCUSATIVE, true);
                    if(isGenitive) constructionsToCount.put(GrammaticalConstruction.DETERMINER_GENITIVE, true);
                    if(isDative) constructionsToCount.put(GrammaticalConstruction.DETERMINER_DATIVE, true);
                    if(isPrepositional) constructionsToCount.put(GrammaticalConstruction.DETERMINER_PREPOSITIONAL, true);
                    if(isInstrumental) constructionsToCount.put(GrammaticalConstruction.DETERMINER_INSTRUMENTAL, true);
                }
                if(isVerb){
                    if(isInfinitive) constructionsToCount.put(GrammaticalConstruction.VERBFORM_INFINITIVE, true);
                    //tenses
                    if(isPast) constructionsToCount.put(GrammaticalConstruction.TENSE_PAST, true);
                    if(isPresent) {
                        constructionsToCount.put(GrammaticalConstruction.TENSE_PRESENT, true);
                        constructionsToCount.put(GrammaticalConstruction.TENSE_NON_PAST, true);
                    }
                    if(isFuture) {
                        constructionsToCount.put(GrammaticalConstruction.TENSE_FUTURE, true);
                        constructionsToCount.put(GrammaticalConstruction.TENSE_NON_PAST, true);
                    }
                    //participles
                    if(isPresentActive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PRESENT_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE, true);
                    }
                    if(isPresentPassive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PRESENT_PASSIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE, true);
                    }
                    if(isPastActive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PAST_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE, true);
                    }
                    if(isPastPassive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PAST_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE, true);
                    }
                }
                if(isSubordinateClause) constructionsToCount.put(GrammaticalConstruction.CLAUSE_SUBORDINATE, true);
                if(isRelativeClause) constructionsToCount.put(GrammaticalConstruction.CLAUSE_RELATIVE, true);
            }

            //count this word towards the appropriate constructions
            for(GrammaticalConstruction attr: constructionsToCount.keySet()){
                if(constructionsToCount.get(attr)){
                    //add the WordWithReadings to the list associated with the given construction
                    List<WordWithReadings> existingList = constructionInstances.getOrDefault(attr, new LinkedList<>());
                    existingList.add(word);
                    constructionInstances.put(attr, existingList);
                }
            }
        }
        if(isComplexSentence){
            constructionInstances.put(GrammaticalConstruction.SENTENCE_COMPLEX, wordsWithReadings.subList(0,1));
        }
        else {
            constructionInstances.put(GrammaticalConstruction.SENTENCE_SIMPLE, wordsWithReadings.subList(0,1));
        }
        return constructionInstances;
    }

    private Map<GrammaticalConstruction, List<WordWithReadings>> countPrepositionConstructions(List<WordWithReadings> wordsWithReadings, SemanticGraph graph){
        Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances = new HashMap<>();
        //find all prepositions
        List<IndexedWord> prepositions = graph.getAllNodesByPartOfSpeechPattern(PREPOSITION_GRAPH_LABEL);
        for(IndexedWord preposition: prepositions){
            List<SemanticGraphEdge> edqes = graph.getIncomingEdgesSorted(preposition);
            for(SemanticGraphEdge edge: edqes){
                //get the object of the preposition
                IndexedWord objectOfPreposition = edge.getSource();
                int objectIndex = objectOfPreposition.index() - 1;
                WordWithReadings objectWithReadings = wordsWithReadings.get(objectIndex);

                //recognize which tags are present in this word's readings
                Map<GrammaticalConstruction, Boolean> constructionsToCount = new HashMap<>();
                for(CgReading reading: objectWithReadings.getReadings()) {
                    Set<String> tags = new HashSet<>(reading.getTags());
                    if(tags.contains(NOMINATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_NOMINATIVE, true);
                    if(tags.contains(ACCUSATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_ACCUSATIVE, true);
                    if(tags.contains(GENITIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_GENITIVE, true);
                    if(tags.contains(PREPOSITIONAL_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_PREPOSITIONAL, true);
                    if(tags.contains(DATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_DATIVE, true);
                    if(tags.contains(INSTRUMENTAL_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_INSTRUMENTAL, true);
                }

                //count this word towards the appropriate constructions
                for(GrammaticalConstruction attr: constructionsToCount.keySet()){
                    if(constructionsToCount.get(attr)){
                        //add the WordWithReadings to the list associated with the given construction
                        List<WordWithReadings> existingList = constructionInstances.getOrDefault(attr, new LinkedList<>());
                        int prepositionIndex = preposition.index() - 1;
                        existingList.add(wordsWithReadings.get(prepositionIndex));
                        constructionInstances.put(attr, existingList);
                    }
                }
            }
        }
        return constructionInstances;
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
    private List<CoreLabel> findMatches(Pattern pattern, List<CoreLabel> words){
        List<CoreLabel> matches = new LinkedList<>();
        for(CoreLabel word : words) {
            final String wordValue = word.value();
            Matcher m = pattern.matcher(wordValue);
            while (m.find()) {
                matches.add(word);
            }
        }
        return matches;
    }

    private void saveGrammaticalConstructionsToDocument(Map<GrammaticalConstruction, List<WordWithReadings>> constructionsMap, List<CoreLabel> originalLabels) {
        for(GrammaticalConstruction construction: constructionsMap.keySet()){
            List<WordWithReadings> instances = constructionsMap.get(construction);
            //convert the WordWithReadings objects to CoreLabel objects
            List<CoreLabel> labels = new LinkedList<>();
            for(WordWithReadings instance: instances){
                labels.add(originalLabels.get(instance.getIndex()));
            }
            //add the CoreLabel objects to the document object as instances of the appropriate constructions
            addConstructionOccurrences(construction, labels);
        }
    }

    private void addConstructionOccurrences(GrammaticalConstruction type, List<CoreLabel> labels) {
        for(CoreLabel label: labels) {
            int begin = label.beginPosition();
            int end = label.endPosition();
            workingDoc.getConstructionData(type).addOccurrence(begin, end);
        }
    }
}


