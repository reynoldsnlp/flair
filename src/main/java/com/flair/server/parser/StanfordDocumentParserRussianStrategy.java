/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.flair.server.grammar.RussianGrammaticalPatterns.*;

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
    private static final String VERB_GRAPH_LABEL = "VERB";

    //TAGS
    private final String NOUN_TAG = "N";
    private final String ADJECTIVE_TAG = "A";
    private final String ADVERB_TAG = "Adv";
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
    private final String IMPERATIVE_TAG = "Imp";
    private final String PASSIVE_TAG = "Pass";
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
    private final String COORDINATE_CLAUSE_TAG = "CC";
    private final String PERSONAL_TAG = "Pers";
    private final String RELATIVE_TAG = "Rel";
    private final String INDEFINITE_TAG = "Indef";
    private final String DEFINITE_TAG = "Def";
    private final String INTERROGATIVE_TAG = "Interr";
    private final String NEGATIVE_TAG = "Neg";
    private final String COMPARATIVE_TAG = "Cmpar";
    private final String PERFECTIVE_TAG = "Perf";
    private final String IMPERFECTIVE_TAG = "Impf";
	private final String THIRD_PLURAL_TAG = "Pl3";
    //


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

    public boolean apply(AbstractDocument docToParse){
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
                    String plainSentence = itr.get(CoreAnnotations.TextAnnotation.class);
                    SemanticGraph graph = itr.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                    List<CoreLabel> words = itr.get(CoreAnnotations.TokensAnnotation.class);
					/*Collection<TypedDependency> dependencies = itr
							.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class)
							.typedDependencies();
							*/

                    inspectSentence(plainSentence, graph, words);

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
        List<CoreLabel> reflexiveVerbs = findMatches(patternReflexiveVerb, verbCoreLabels);
        addConstructionOccurrences(GrammaticalConstruction.VERB_REFLEXIVE_RUSSIAN, reflexiveVerbs);
    }

    private void inspectPrepositions(SemanticGraph graph){
        //extract verbs from the graph
        List<IndexedWord> prepositions = graph.getAllNodesByPartOfSpeechPattern(PREPOSITION_GRAPH_LABEL);
        List<CoreLabel> prepositionCoreLabels = indexedWordsToCoreLabels(prepositions);
        //count constructions
        addConstructionOccurrences(GrammaticalConstruction.PREPOSITIONS, prepositionCoreLabels);
    }

    private void inspectSentence(String plainSentence, SemanticGraph graph, List<CoreLabel> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        String wordsWithLemmas = analyser.runTransducer(indexedWordsToStrings(words));
        //ServerLogger.get().info("Transducer results received");
        String cgForm;
        try {
            cgForm = CgConv.hfstToCg(wordsWithLemmas);
            //ServerLogger.get().info("Transducer results converted to cg3 format");
            //System.out.println("cgForm:\n" + cgForm);
            String finalReadings = VislCg3.runVislCg3(cgForm);
            if(!finalReadings.isEmpty()) {
                //ServerLogger.get().info("Readings have been reduced by the constraint grammar");
                //System.out.println("readings:\n" + finalReadings);
                Cg3Parser parser = new Cg3Parser(finalReadings);
                List<WordWithReadings> readingsList = parser.parse();
                //use the reduced readings to count constructions
                Map<GrammaticalConstruction, List<WordWithReadings>> constructionCounts = countGrammaticalConstructions(readingsList, words, graph);
                saveGrammaticalConstructionsToDocument(constructionCounts, words);
            }
            else {
                ServerLogger.get().info("There was an error using the constraint grammar");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //count things based purely on surface forms, not readings

        //tag questions
        Matcher tagQuestionMatcher = patternTagQuestion.matcher(plainSentence);
        while(tagQuestionMatcher.find()) {
            int fullStart = words.get(0).beginPosition() + tagQuestionMatcher.start(0);
            int fullEnd = fullStart + tagQuestionMatcher.group(0).length();
            addConstructionByIndices(GrammaticalConstruction.QUESTIONS_TAG, fullStart, fullEnd);
        }
        //есть
        List<CoreLabel> positiveExistentials = findMatches(patternJest, words);
        addConstructionOccurrences(GrammaticalConstruction.EXISTENTIAL_THERE, positiveExistentials);
        //нет
        List<CoreLabel> negativeExistentials = findMatches(patternNjet, words);
        addConstructionOccurrences(GrammaticalConstruction.EXISTENTIAL_THERE, negativeExistentials);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negativeExistentials);
        //не
        List<CoreLabel> negationsNe = findMatches(patternNe, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNe);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNe);
        //ни
        List<CoreLabel> negationsNi = findMatches(patternNi, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNi);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNi);
        //никогда
        List<CoreLabel> negationsNikogda = findMatches(patternNikogda, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNikogda);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNikogda);
        //никак
        List<CoreLabel> negationsNikak = findMatches(patternNikak, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNikak);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNikak);
        //никуда
        List<CoreLabel> negationsNikuda = findMatches(patternNikuda, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNikuda);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNikuda);
        //нигде
        List<CoreLabel> negationsNigdje = findMatches(patternNigdje, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNigdje);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNigdje);
        //ниоткуда
        List<CoreLabel> negationsNiotkuda = findMatches(patternNiotkuda, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNiotkuda);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNiotkuda);
        //нипочём
        List<CoreLabel> negationsNipochjom = findMatches(patternNipochjom, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNipochjom);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNipochjom);
        //ничуть
        List<CoreLabel> negationsNichut = findMatches(patternNichut, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNichut);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNichut);
        //нисколько
        List<CoreLabel> negationsNiskoljko = findMatches(patternNiskoljko, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNiskoljko);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNiskoljko);
        //нисколечко
        List<CoreLabel> negationsNiskoljechko = findMatches(patternNiskoljechko, words);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_ALL, negationsNiskoljechko);
        addConstructionOccurrences(GrammaticalConstruction.NEGATION_NO_NOT_NEVER_RUSSIAN, negationsNiskoljechko);

        //бы
        List<CoreLabel> conditionals = findMatches(patternBy, words);
        addConstructionOccurrences(GrammaticalConstruction.CONDITIONALS_RUSSIAN, conditionals);

        inspectVerbs(graph);
        inspectPrepositions(graph);
    }

    private Map<GrammaticalConstruction, List<WordWithReadings>> countGrammaticalConstructions(List<WordWithReadings> wordsWithReadings, List<CoreLabel> words, SemanticGraph graph){
        //variables for the whole sentence
        boolean isComplexSentence = false;
        boolean hasLi = false;
        boolean hasBy = false;
        boolean hasJesli = false;
        boolean hasInterrogative = false;
        boolean hasInterrogativeBesidesLi = false;
        boolean hasQuestionMark = false;
        int sentenceStart = -1;
        int sentenceEnd = -1;
        if(words.size() != 0){
            sentenceStart = words.get(0).beginPosition();
            sentenceEnd = words.get(words.size() - 1).endPosition();
        }

        Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances = new HashMap<>();
        WordWithReadings previousWord = null;
        boolean boljejeEncountered = false;
        boolean samyjEncountered = false;
        for(WordWithReadings word: wordsWithReadings){
            Map<GrammaticalConstruction, Boolean> constructionsToCount = new HashMap<>();

            //recognize which tags and lemmas are present in this word's readings
            for(CgReading reading: word.getReadings()){
                boolean isNoun = false;
                boolean isAdjective = false;
                boolean isAdverb = false;
                boolean isPronoun = false;
                boolean isDeterminer = false;
                boolean isVerb = false;
                boolean isPredicate = false;

                boolean isPerfective = false;
                boolean isImperfective = false;
                boolean isPast = false;
                boolean isPresent = false;
                boolean isFuture = false;
                boolean isInfinitive = false;
                boolean isImperative = false;
                boolean isIrregularPast = false;
                boolean isIrregularNonpast = false;

                boolean isPassive = false;
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
                boolean isNegative = false;
                boolean isComparative = false;

                Set<String> tags = new HashSet<>(reading.getTags());
                //part of speech
                if(tags.contains(NOUN_TAG)) isNoun = true;
                if(tags.contains(ADJECTIVE_TAG)) isAdjective = true;
                if(tags.contains(ADVERB_TAG)) isAdverb = true;
                if(tags.contains(PRONOUN_TAG)) isPronoun = true;
                if(tags.contains(DETERMINER_TAG)) isDeterminer = true;
                if(tags.contains(VERB_TAG)) isVerb = true;
                if(tags.contains(PREDICATE_TAG)) isPredicate = true;
                //aspect
                if(tags.contains(PERFECTIVE_TAG)) isPerfective = true;
                if(tags.contains(IMPERFECTIVE_TAG)) isImperfective = true;
                //tense
                if(tags.contains(PAST_TAG)) isPast = true;
                if(tags.contains(PRESENT_TAG)) isPresent = true;
                if(tags.contains(FUTURE_TAG)) isFuture = true;
                if(tags.contains(INFINITIVE_TAG)) isInfinitive = true;
                if(tags.contains(IMPERATIVE_TAG)) isImperative = true;
                //participles
                if(tags.contains(PASSIVE_TAG)) isPassive = true;
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
                if(tags.contains(RELATIVE_CLAUSE_TAG)){
                    isRelativeClause = true;
                    isComplexSentence = true;
                }
                if(tags.contains(COORDINATE_CLAUSE_TAG)) isComplexSentence = true;
                //types of pronouns
                if(tags.contains(PERSONAL_TAG)) isPersonal = true;
                if(tags.contains(RELATIVE_TAG)) isRelative = true;
                if(tags.contains(POSSESSIVE_TAG)) isPossessive = true;
                if(tags.contains(DEMONSTRATIVE_TAG)) isDemonstrative = true;
                if(tags.contains(REFLEXIVE_TAG)) isReflexive = true;
                //definite vs indefinite
                if(tags.contains(DEFINITE_TAG)) isDefinite = true;
                if(tags.contains(INDEFINITE_TAG)) isIndefinite = true;
                //other
                if(tags.contains(INTERROGATIVE_TAG)) {
                    isInterrogative = true;
                    hasInterrogative = true;
                    String lemma = reading.getBaseForm();
                    if(!isPartialMatch(patternLi, lemma)){
                        hasInterrogativeBesidesLi = true;
                    }
                }
                if(tags.contains(NEGATIVE_TAG)) isNegative = true;
                if(tags.contains(COMPARATIVE_TAG)) isComparative = true;

                //look at the lemma
                String lemma = reading.getBaseForm();
                //particles
                if(isPartialMatch(patternLi, lemma)){
                    hasLi = true;
                }
                if(isPartialMatch(patternBy, lemma)){
                    hasBy = true;
                }
                //superlative long
	            if(isPartialMatch(patternSuperlativeLongLemmas, lemma)){
		            constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_SUPERLATIVE_LONG_RUSSIAN, true);
	            }
                //determiners
                if(isPartialMatch(patternNjekotoryj, lemma)){
                    constructionsToCount.put(GrammaticalConstruction.DETERMINER_SOME_RUSSIAN, true);
                }
                if(isPartialMatch(patternLjuboj, lemma)){
                    constructionsToCount.put(GrammaticalConstruction.DETERMINER_ANY_RUSSIAN, true);
                }
                if(isPartialMatch(patternMnogo, lemma)){
                    constructionsToCount.put(GrammaticalConstruction.DETERMINER_MUCH_RUSSIAN, true);
                }
                //conjunctions
                if(isPartialMatch(patternJesli, lemma)){
                    hasJesli = true;
                }
                //punctuation
                if(isPartialMatch(patternQuestionMark, lemma)){
                    hasQuestionMark = true;
                }
                //irregular verbs
                if(isPartialMatch(patternIrregularPastVerb, lemma)){
                    isIrregularPast = true;
                    constructionsToCount.put(GrammaticalConstruction.VERBS_IRREGULAR_PAST, true);
                }
                if(isPartialMatch(patternIrregularNonpastVerb, lemma)){
                    isIrregularNonpast = true;
                    constructionsToCount.put(GrammaticalConstruction.VERBS_IRREGULAR_NONPAST, true);
                }
                if(isIrregularPast || isIrregularNonpast){
                    constructionsToCount.put(GrammaticalConstruction.VERBS_IRREGULAR_RUSSIAN, true);
                }

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
                    if(isPredicate) {
	                    constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_LONG_RUSSIAN, true);
                        if(isComparative){
                            constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_COMPARATIVE_SHORT_RUSSIAN, true);
                        }
                    } else {
                        constructionsToCount.put(GrammaticalConstruction.ADJECTIVE_SHORT_RUSSIAN, true);
                    }
                    //comparative and superlative
                    if(previousWord != null) {
                        GrammaticalConstruction attr = null;
                        if(boljejeEncountered) {
                            attr = GrammaticalConstruction.ADJECTIVE_COMPARATIVE_LONG_RUSSIAN;
                        }
                        else if(samyjEncountered) {
                            attr = GrammaticalConstruction.ADJECTIVE_SUPERLATIVE_LONG_RUSSIAN;
                        }
                        if(attr != null) addSingleConstructionInstance(constructionInstances, attr, previousWord);
                    }
                }
                if(isAdverb){
                    constructionsToCount.put(GrammaticalConstruction.ADVERB_POSITIVE, true);
                }
                if(isPronoun){
                    constructionsToCount.put(GrammaticalConstruction.PRONOUNS, true);
                    if(isPersonal) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_PERSONAL, true);
                    if(isRelative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_RELATIVE, true);
                    if(isPossessive) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_POSSESSIVE, true);
                    if(isDemonstrative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_DEMONSTRATIVE, true);
                    if(isReflexive) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_REFLEXIVE, true);
                    if(isInterrogative) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_INTERROGATIVE, true);
                    if(isDefinite) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_DEFINITE_RUSSIAN, true);
                    if(isIndefinite) constructionsToCount.put(GrammaticalConstruction.PRONOUNS_INDEFINITE_RUSSIAN, true);
                    //negative
                    if(isNegative) {
                        constructionsToCount.put(GrammaticalConstruction.PRONOUNS_NEGATIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.NEGATION_PRONOUNS, true);
                        constructionsToCount.put(GrammaticalConstruction.NEGATION_ALL, true); //this is actually redundant because of the check below
                    }
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
                    if(isInfinitive) constructionsToCount.put(GrammaticalConstruction.VERBFORM_INFINITIVE_RUSSIAN, true);
                    if(isImperative) constructionsToCount.put(GrammaticalConstruction.IMPERATIVES_RUSSIAN, true);
                    //tenses and aspect
                    if(isPast) {
                        if(isPerfective) constructionsToCount.put(GrammaticalConstruction.PAST_PERFECTIVE, true);
                        if(isImperfective) constructionsToCount.put(GrammaticalConstruction.PAST_IMPERFECTIVE, true);
                    }
                    if(isPresent) constructionsToCount.put(GrammaticalConstruction.TENSE_PRESENT, true);
                    if(isFuture) {
                        if(isPerfective) constructionsToCount.put(GrammaticalConstruction.FUTURE_PERFECTIVE, true);
                        if(isImperfective) constructionsToCount.put(GrammaticalConstruction.FUTURE_IMPERFECTIVE, true);
                    }
                    if(isPerfective && isImperfective) constructionsToCount.put(GrammaticalConstruction.ASPECT_BIASPECTUAL, true);

                    //participles
                    if(isPassive){
                        constructionsToCount.put(GrammaticalConstruction.PASSIVE_VOICE, true);
                    }
                    if(isPresentActive) { //TODO: only without adverb tag "Adv"
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PRESENT_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE_RUSSIAN, true);
                    }
                    if(isPresentPassive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PRESENT_PASSIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE_RUSSIAN, true);
                        constructionsToCount.put(GrammaticalConstruction.PASSIVE_VOICE, true);
                    }
                    if(isPastActive) { //TODO: only without adverb tag "Adv"
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PAST_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE_RUSSIAN, true);
                    }
                    if(isPastPassive) {
                        constructionsToCount.put(GrammaticalConstruction.PARTICIPLE_PAST_ACTIVE, true);
                        constructionsToCount.put(GrammaticalConstruction.VERBFORM_PARTICIPLE_RUSSIAN, true);
                        constructionsToCount.put(GrammaticalConstruction.PASSIVE_VOICE, true);
                    }
                    //verbal adverbs
                    if(isAdverb){
                        constructionsToCount.put(GrammaticalConstruction.VERBAL_ADVERB, true);
                        if(isPerfective){
                            constructionsToCount.put(GrammaticalConstruction.VERBAL_ADVERB_PAST, true);
                        }
                        if(isImperfective){
                            constructionsToCount.put(GrammaticalConstruction.VERBAL_ADVERB_PRESENT, true);
                        }
                    }
                }
                if(isSubordinateClause) constructionsToCount.put(GrammaticalConstruction.CLAUSE_SUBORDINATE, true);
                if(isRelativeClause) constructionsToCount.put(GrammaticalConstruction.CLAUSE_RELATIVE_RUSSIAN, true);
                if(isNegative) constructionsToCount.put(GrammaticalConstruction.NEGATION_ALL, true);
            }

            //count this word towards the appropriate constructions
            for(GrammaticalConstruction attr: constructionsToCount.keySet()){
                if(constructionsToCount.get(attr)){
                    addSingleConstructionInstance(constructionInstances, attr, word);
                }
            }

            //look at the surface form
            String surfaceForm = word.getSurfaceForm();
	        if(isPartialMatch(patternPartialNegationWords, surfaceForm)){
		        addSingleConstructionInstance(constructionInstances, GrammaticalConstruction.NEGATION_PARTIAL, word);
		        addSingleConstructionInstance(constructionInstances, GrammaticalConstruction.NEGATION_ALL, word);
	        }

	        //handle things dealing with adjacent words
            boljejeEncountered = isPartialMatch(patternBoljeje, surfaceForm);
            samyjEncountered = isPartialMatch(patternSamyj, surfaceForm);
            //save this word so we still have access to it on the next iteration of the loop
            previousWord = word;
        }

        //sentence level

        if(isComplexSentence){
            addConstructionByIndices(GrammaticalConstruction.SENTENCE_COMPLEX, sentenceStart, sentenceEnd);
        }
        else {
            addConstructionByIndices(GrammaticalConstruction.SENTENCE_SIMPLE, sentenceStart, sentenceEnd);
        }

        if(hasJesli && hasBy){
            addConstructionByIndices(GrammaticalConstruction.CONDITIONALS_UNREAL, sentenceStart, sentenceEnd);
        }

        if(hasQuestionMark){
            addConstructionByIndices(GrammaticalConstruction.QUESTIONS_DIRECT, sentenceStart, sentenceEnd);
            if(hasLi || !hasInterrogative){
                addConstructionByIndices(GrammaticalConstruction.QUESTIONS_YESNO_RUSSIAN, sentenceStart, sentenceEnd);
            }
            if(hasInterrogativeBesidesLi){
                addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WH_RUSSIAN, sentenceStart, sentenceEnd);
            }

	        //question words
	        for(int i = 0; i < 2; i++) { //a question word has to be one of the first two words in the sentence
		        CoreLabel plainWord = words.get(i);
	        	WordWithReadings word = wordsWithReadings.get(i);
	        	boolean isWhat = false;
	        	boolean isWho = false;
	        	boolean isHow = false;
	        	boolean isWhy = false;
	        	boolean isWhere = false;
	        	boolean isWhen = false;
	        	boolean isWhose = false;
	        	boolean isWhich = false;
	        	boolean isWhither = false;
	        	boolean isWhatKind = false;
		        for(CgReading reading: word.getReadings()){
			        String lemma = reading.getBaseForm();
			        isWhat |= isPartialMatch(patternChto, lemma);
			        isWho |= isPartialMatch(patternKto, lemma);
                    isHow |= isPartialMatch(patternKak, lemma);
                    isWhy |= isPartialMatch(patternPochjemu, lemma);
                    isWhy |= isPartialMatch(patternZachjem, lemma);
                    isWhere |= isPartialMatch(patternGdje, lemma);
                    isWhen |= isPartialMatch(patternKogda, lemma);
                    isWhose |= isPartialMatch(patternChjej, lemma);
                    isWhich |= isPartialMatch(patternKakoj, lemma);
                    isWhither |= isPartialMatch(patternKuda, lemma);
                    isWhatKind |= isPartialMatch(patternKakov, lemma);
                }
                if (isWhat || isWho || isHow || isWhy || isWhere || isWhen || isWhose || isWhich || isWhither || isWhatKind) {
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WH_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                }
                if (isWhat)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHAT_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWho)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHO_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isHow)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_HOW_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhy)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHY_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhere)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHERE_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhen)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHEN_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhose)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHOSE_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhich)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHOSE_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhither)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHITHER_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
                else if (isWhatKind)
                    addConstructionByIndices(GrammaticalConstruction.QUESTIONS_WHAT_KIND_RUSSIAN, plainWord.beginPosition(), plainWord.endPosition());
	        }
        }
        else{ //no question mark
            if(hasLi || hasInterrogative){
                //NB: this will recognize situations such as "скажу ему, когда ты придешь" //TODO: FIX THIS (find using a list of verbs that can introduce indirect speech then an interrogative)
                //starter lists
                //telling: сказать говорить
                //knowing: знать объяснить объяснять
                //asking: спрашивать спросить
                addConstructionByIndices(GrammaticalConstruction.QUESTIONS_INDIRECT, sentenceStart, sentenceEnd);
            }
        }

        //using Semgrex

	    //passive voice using 3rd-person plural subjectless verb
	    SemgrexMatcher verbNoSubjectMatcher = patternVerbNoSubject.matcher(graph);
	    while(verbNoSubjectMatcher.find()){
		    IndexedWord subjectlessVerb = verbNoSubjectMatcher.getNode(labelVerbNoSubject);
		    WordWithReadings subjectlessVerbWithReadings = wordsWithReadings.get(subjectlessVerb.index() - 1);
		    for(CgReading reading: subjectlessVerbWithReadings.getReadings()){
		    	List<String> tags = reading.getTags();
			    if(tags.contains(THIRD_PLURAL_TAG)) {
				    addConstructionByIndices(GrammaticalConstruction.PASSIVE_VOICE, subjectlessVerb.beginPosition(), subjectlessVerb.endPosition());
			    	break;
			    }
		    }
	    }

        //preposition things using the graph
        Map<GrammaticalConstruction, List<WordWithReadings>> prepositionConstructionCounts = countPrepositionConstructions(wordsWithReadings, graph);
        constructionInstances.putAll(prepositionConstructionCounts);
        //verb direct objects and indirect objects using the graph
        Map<GrammaticalConstruction, List<WordWithReadings>> verbalObjectConstructionCounts = countVerbalObjectConstructions(wordsWithReadings, graph);
        constructionInstances.putAll(verbalObjectConstructionCounts);

        return constructionInstances;
    }

    private static void addSingleConstructionInstance(Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances,
                                                      GrammaticalConstruction attr, WordWithReadings word){
        //add the WordWithReadings to the list associated with the given construction
        List<WordWithReadings> existingList = constructionInstances.getOrDefault(attr, new LinkedList<>());
        existingList.add(word);
        constructionInstances.put(attr, existingList);
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
                    if(tags.contains(ACCUSATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_ACCUSATIVE, true);
                    if(tags.contains(GENITIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_GENITIVE, true);
                    if(tags.contains(PREPOSITIONAL_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_PREPOSITIONAL, true);
                    if(tags.contains(DATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_DATIVE, true);
                    if(tags.contains(INSTRUMENTAL_TAG)) constructionsToCount.put(GrammaticalConstruction.PREPOSITION_INSTRUMENTAL, true);
                }

                //count this word towards the appropriate constructions
                countWordInConstructions(wordsWithReadings, constructionInstances, preposition, constructionsToCount);
            }
        }
        return constructionInstances;
    }

    private Map<GrammaticalConstruction, List<WordWithReadings>> countVerbalObjectConstructions(List<WordWithReadings> wordsWithReadings, SemanticGraph graph){
        Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances = new HashMap<>();
	    //find all children of verbs that don't have a preposition as their own child; check case tags
        SemgrexMatcher verbObjectMatcher = patternObjectOfVerbNoPreposition.matcher(graph);
        while(verbObjectMatcher.find()){
            IndexedWord objectOfVerb = verbObjectMatcher.getNode(labelObjectOfVerbNoPreposition);
            int objectIndex = objectOfVerb.index() - 1;
            WordWithReadings objectWithReadings = wordsWithReadings.get(objectIndex);

            //recognize which tags are present in this word's readings
            Map<GrammaticalConstruction, Boolean> constructionsToCount = new HashMap<>();
            for(CgReading reading: objectWithReadings.getReadings()) {
                Set<String> tags = new HashSet<>(reading.getTags());
                if(tags.contains(ACCUSATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.VERB_WITH_ACCUSATIVE, true);
                if(tags.contains(GENITIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.VERB_WITH_GENITIVE, true);
                if(tags.contains(DATIVE_TAG)) constructionsToCount.put(GrammaticalConstruction.VERB_WITH_DATIVE, true);
                if(tags.contains(PREPOSITIONAL_TAG)) constructionsToCount.put(GrammaticalConstruction.VERB_WITH_PREPOSITIONAL, true);
                if(tags.contains(INSTRUMENTAL_TAG)) constructionsToCount.put(GrammaticalConstruction.VERB_WITH_INSTRUMENTAL, true);
            }

            //count this word's parent (the verb) towards the appropriate constructions
            IndexedWord verb = graph.getParent(objectOfVerb);
            countWordInConstructions(wordsWithReadings, constructionInstances, verb, constructionsToCount);
        }

        return constructionInstances;
    }

    private void countWordInConstructions(List<WordWithReadings> wordsWithReadings, Map<GrammaticalConstruction, List<WordWithReadings>> constructionInstances, IndexedWord verb, Map<GrammaticalConstruction, Boolean> constructionsToCount) {
        for(GrammaticalConstruction attr: constructionsToCount.keySet()){
            if(constructionsToCount.get(attr)){
                //add the WordWithReadings to the list associated with the given construction
                List<WordWithReadings> existingList = constructionInstances.getOrDefault(attr, new LinkedList<>());
                int prepositionIndex = verb.index() - 1;
                existingList.add(wordsWithReadings.get(prepositionIndex));
                constructionInstances.put(attr, existingList);
            }
        }
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

    /*public static boolean isFullMatch(Pattern pattern, String value){
        Matcher m = pattern.matcher(value);
        return m.matches();
    }*/

    public static boolean isPartialMatch(Pattern pattern, String value){
        Matcher m = pattern.matcher(value);
        return m.find();
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

    private void addConstructionByIndices(GrammaticalConstruction type, int startIndex, int endIndex){
        workingDoc.getConstructionData(type).addOccurrence(startIndex, endIndex);
    }
}

