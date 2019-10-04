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

    private enum Attribute { //TODO: convert these to GrammaticalConstruction
        N_NOMINATIVE, N_ACCUSATIVE, N_GENITIVE, N_PREPOSITIONAL, N_DATIVE, N_INSTRUMENTAL, //noun cases
        A_NOMINATIVE, A_ACCUSATIVE, A_GENITIVE, A_PREPOSITIONAL, A_DATIVE, A_INSTRUMENTAL, //adjective cases
        A_ATTRIBUTE,
        PRO_NOMINATIVE, PRO_ACCUSATIVE, PRO_GENITIVE, PRO_PREPOSITIONAL, PRO_DATIVE, PRO_INSTRUMENTAL, //pronoun cases
        PRO_PERSONAL, PRO_RELATIVE, //pronoun types
        DET_NOMINATIVE, DET_ACCUSATIVE, DET_GENITIVE, DET_PREPOSITIONAL, DET_DATIVE, DET_INSTRUMENTAL, //determiner cases
        V_PAST, V_PRESENT, V_FUTURE, V_INFINITIVE, //verb forms
        P_PRESENT_ACTIVE, P_PRESENT_PASSIVE, P_PAST_ACTIVE, P_PAST_PASSIVE, //participles
        PR_NOMINATIVE, PR_ACCUSATIVE, PR_GENITIVE, PR_PREPOSITIONAL, PR_DATIVE, PR_INSTRUMENTAL, //preposition cases
        CL_SUBORDINATE, CL_RELATIVE, //clause types
        SENT_COMPLEX, SENT_SIMPLE, //sentence types
    }
    private final String NOUN_TAG = "N";
    private final String ADJECTIVE_TAG = "A";
    private final String PRONOUN_TAG = "Pron";
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
        //addConstructionOccurrences(GrammaticalConstruction.PRONOUNS_REFLEXIVE, reflexiveVerbs);
        //TODO: correct the GrammaticalConstruction type passed into addConstructionOccurrences
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
                //use the reduced readings to count attributes
                Map<Attribute, List<WordWithReadings>> attributeCounts = countAttributes(readingsList);
                Map<Attribute, List<WordWithReadings>> prepositionAttributeCounts = countPrepositionAttributes(readingsList, graph);
                attributeCounts.putAll(prepositionAttributeCounts);
                saveAttributesToDocument(attributeCounts, words);
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

    private Map<Attribute, List<WordWithReadings>> countAttributes(List<WordWithReadings> wordsWithReadings){
        //variables for the whole sentence
        boolean isComplexSentence = false;

        Map<Attribute, List<WordWithReadings>> attributeInstances = new HashMap<>();
        for(WordWithReadings word: wordsWithReadings){
            Map<Attribute, Boolean> attributesToCount = new HashMap<>();

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
                //personal v relative
                if(tags.contains(PERSONAL_TAG)) isPersonal = true;
                if(tags.contains(RELATIVE_TAG)) isRelative = true;

                //recognize tag combinations
                if(isNoun){
                    if(isNominative) attributesToCount.put(Attribute.N_NOMINATIVE, true);
                    if(isAccusative) attributesToCount.put(Attribute.N_ACCUSATIVE, true);
                    if(isGenitive) attributesToCount.put(Attribute.N_GENITIVE, true);
                    if(isDative) attributesToCount.put(Attribute.N_DATIVE, true);
                    if(isPrepositional) attributesToCount.put(Attribute.N_PREPOSITIONAL, true);
                    if(isInstrumental) attributesToCount.put(Attribute.N_INSTRUMENTAL, true);
                }
                if(isAdjective){
                    if(isNominative) attributesToCount.put(Attribute.A_NOMINATIVE, true);
                    if(isAccusative) attributesToCount.put(Attribute.A_ACCUSATIVE, true);
                    if(isGenitive) attributesToCount.put(Attribute.A_GENITIVE, true);
                    if(isDative) attributesToCount.put(Attribute.A_DATIVE, true);
                    if(isPrepositional) attributesToCount.put(Attribute.A_PREPOSITIONAL, true);
                    if(isInstrumental) attributesToCount.put(Attribute.A_INSTRUMENTAL, true);

                    if(!isPredicate) attributesToCount.put(Attribute.A_ATTRIBUTE, true);
                }
                if(isPronoun){
                    if(isNominative) attributesToCount.put(Attribute.PRO_NOMINATIVE, true);
                    if(isAccusative) attributesToCount.put(Attribute.PRO_ACCUSATIVE, true);
                    if(isGenitive) attributesToCount.put(Attribute.PRO_GENITIVE, true);
                    if(isDative) attributesToCount.put(Attribute.PRO_DATIVE, true);
                    if(isPrepositional) attributesToCount.put(Attribute.PRO_PREPOSITIONAL, true);
                    if(isInstrumental) attributesToCount.put(Attribute.PRO_INSTRUMENTAL, true);

                    if(isPersonal) attributesToCount.put(Attribute.PRO_PERSONAL, true);
                    if(isRelative) attributesToCount.put(Attribute.PRO_RELATIVE, true);
                }
                if(isDeterminer){
                    if(isNominative) attributesToCount.put(Attribute.DET_NOMINATIVE, true);
                    if(isAccusative) attributesToCount.put(Attribute.DET_ACCUSATIVE, true);
                    if(isGenitive) attributesToCount.put(Attribute.DET_GENITIVE, true);
                    if(isDative) attributesToCount.put(Attribute.DET_DATIVE, true);
                    if(isPrepositional) attributesToCount.put(Attribute.DET_PREPOSITIONAL, true);
                    if(isInstrumental) attributesToCount.put(Attribute.DET_INSTRUMENTAL, true);
                }
                if(isVerb){
                    if(isPast) attributesToCount.put(Attribute.V_PAST, true);
                    if(isPresent) attributesToCount.put(Attribute.V_PRESENT, true);
                    if(isFuture) attributesToCount.put(Attribute.V_FUTURE, true);
                    if(isInfinitive) attributesToCount.put(Attribute.V_INFINITIVE, true);
                    if(isPresentActive) attributesToCount.put(Attribute.P_PRESENT_ACTIVE, true);
                    if(isPresentPassive) attributesToCount.put(Attribute.P_PRESENT_PASSIVE, true);
                    if(isPastActive) attributesToCount.put(Attribute.P_PAST_ACTIVE, true);
                    if(isPastPassive) attributesToCount.put(Attribute.P_PAST_ACTIVE, true);
                }
                if(isSubordinateClause) attributesToCount.put(Attribute.CL_SUBORDINATE, true);
                if(isRelativeClause) attributesToCount.put(Attribute.CL_RELATIVE, true);
            }

            //count this word towards the appropriate attributes
            for(Attribute attr: attributesToCount.keySet()){
                if(attributesToCount.get(attr)){
                    //add the WordWithReadings to the list associated with the given attribute
                    List<WordWithReadings> existingList = attributeInstances.getOrDefault(attr, new LinkedList<>());
                    existingList.add(word);
                    attributeInstances.put(attr, existingList);
                }
            }
        }
        if(isComplexSentence){
            attributeInstances.put(Attribute.SENT_COMPLEX, wordsWithReadings.subList(0,1));
        }
        else {
            attributeInstances.put(Attribute.SENT_SIMPLE, wordsWithReadings.subList(0,1));
        }
        return attributeInstances;
    }

    private Map<Attribute, List<WordWithReadings>> countPrepositionAttributes(List<WordWithReadings> wordsWithReadings, SemanticGraph graph){
        Map<Attribute, List<WordWithReadings>> attributeInstances = new HashMap<>();
        //find all prepositions
        List<IndexedWord> prepositions = graph.getAllNodesByPartOfSpeechPattern(PREPOSITION_GRAPH_LABEL);
        for(IndexedWord preposition: prepositions){
            List<SemanticGraphEdge> edqes = graph.getIncomingEdgesSorted(preposition);
            for(SemanticGraphEdge edge: edqes){
                //get the object of the preposition
                IndexedWord objectOfPreposition = edge.getSource();
                int index = objectOfPreposition.get(CoreAnnotations.IndexAnnotation.class) - 1;
                WordWithReadings objectWithReadings = wordsWithReadings.get(index);

                //recognize which tags are present in this word's readings
                Map<Attribute, Boolean> attributesToCount = new HashMap<>();
                for(CgReading reading: objectWithReadings.getReadings()) {
                    Set<String> tags = new HashSet<>(reading.getTags());
                    if(tags.contains(NOMINATIVE_TAG)) attributesToCount.put(Attribute.PR_NOMINATIVE, true);
                    if(tags.contains(ACCUSATIVE_TAG)) attributesToCount.put(Attribute.PR_ACCUSATIVE, true);
                    if(tags.contains(GENITIVE_TAG)) attributesToCount.put(Attribute.PR_GENITIVE, true);
                    if(tags.contains(PREPOSITIONAL_TAG)) attributesToCount.put(Attribute.PR_PREPOSITIONAL, true);
                    if(tags.contains(DATIVE_TAG)) attributesToCount.put(Attribute.PR_DATIVE, true);
                    if(tags.contains(INSTRUMENTAL_TAG)) attributesToCount.put(Attribute.PR_INSTRUMENTAL, true);
                }

                //count this word towards the appropriate attributes
                for(Attribute attr: attributesToCount.keySet()){
                    if(attributesToCount.get(attr)){
                        //add the WordWithReadings to the list associated with the given attribute
                        List<WordWithReadings> existingList = attributeInstances.getOrDefault(attr, new LinkedList<>());
                        existingList.add(objectWithReadings);
                        attributeInstances.put(attr, existingList);
                    }
                }
            }
        }
        return attributeInstances;
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

    private void saveAttributesToDocument(Map<Attribute, List<WordWithReadings>> attributesMap, List<CoreLabel> originalLabels) {
        for(Attribute attr: attributesMap.keySet()){
            List<WordWithReadings> instances = attributesMap.get(attr);
            //convert the WordWithReadings objects to CoreLabel objects
            List<CoreLabel> labels = new LinkedList<>();
            for(WordWithReadings instance: instances){
                labels.add(originalLabels.get(instance.getIndex()));
            }
            //add the CoreLabel objects to the document object as instances of the appropriate constructions
            switch(attr){
                case A_ATTRIBUTE:
                    addConstructionOccurrences(GrammaticalConstruction.ATTRIBUTES_ADJECTIVE, labels); break;
                case CL_SUBORDINATE:
                    addConstructionOccurrences(GrammaticalConstruction.CLAUSE_SUBORDINATE, labels); break;
                case CL_RELATIVE:
                    addConstructionOccurrences(GrammaticalConstruction.CLAUSE_RELATIVE, labels); break;
                case PRO_NOMINATIVE:
                case PRO_ACCUSATIVE:
                case PRO_GENITIVE:
                case PRO_PREPOSITIONAL:
                case PRO_DATIVE:
                case PRO_INSTRUMENTAL:
                    addConstructionOccurrences(GrammaticalConstruction.PRONOUNS, labels); break;
                case PRO_PERSONAL:
                    addConstructionOccurrences(GrammaticalConstruction.PRONOUNS_PERSONAL, labels); break;
                case PRO_RELATIVE:
                    addConstructionOccurrences(GrammaticalConstruction.PRONOUNS_RELATIVE, labels); break;
                    //TODO: add the other attributes
            }
        }
    }

    private void addConstructionOccurrences(GrammaticalConstruction type, List<CoreLabel> labels) {
        for(CoreLabel label: labels) {
            int begin = label.beginPosition();
            int end = label.endPosition();
            System.out.println("TODO: add construction to the document"); //TODO
            workingDoc.getConstructionData(type).addOccurrence(begin, end);
        }
    }
}
