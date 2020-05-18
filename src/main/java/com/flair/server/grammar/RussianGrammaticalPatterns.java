package com.flair.server.grammar;

import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class RussianGrammaticalPatterns {

    //EXISTENTIALS

    private static final String STR_JEST = "\\bесть\\b";
    public static final Pattern patternJest = Pattern.compile(STR_JEST, CASE_INSENSITIVE);
    private static final String STR_NJET = "\\bнет\\b";
    public static final Pattern patternNjet = Pattern.compile(STR_NJET, CASE_INSENSITIVE);


    //PARTICLES

    private static final String STR_LI = "\\bли\\b";
    public static final Pattern patternLi = Pattern.compile(STR_LI, CASE_INSENSITIVE);
    private static final String STR_BI = "\\bбы\\b";
    public static final Pattern patternBy = Pattern.compile(STR_BI, CASE_INSENSITIVE);
    private static final String STR_NE = "\\bне\\b";
    public static final Pattern patternNe = Pattern.compile(STR_NE, CASE_INSENSITIVE);
    private static final String STR_NI = "\\bни\\b";
    public static final Pattern patternNi = Pattern.compile(STR_NI, CASE_INSENSITIVE);


    //NEGATION ADVERBS

    private static final String STR_NIKOGDA = "\\bникогда\\b";
    public static final Pattern patternNikogda = Pattern.compile(STR_NIKOGDA, CASE_INSENSITIVE);
    private static final String STR_NIKAK = "\\bникак\\b";
    public static final Pattern patternNikak = Pattern.compile(STR_NIKAK, CASE_INSENSITIVE);
    private static final String STR_NIKUDA = "\\bникуда\\b";
    public static final Pattern patternNikuda = Pattern.compile(STR_NIKUDA, CASE_INSENSITIVE);
    private static final String STR_NIGDJE = "\\bнигде\\b";
    public static final Pattern patternNigdje = Pattern.compile(STR_NIGDJE, CASE_INSENSITIVE);
    private static final String STR_NIOTKUDA = "\\bноткуда\\b";
    public static final Pattern patternNiotkuda = Pattern.compile(STR_NIOTKUDA, CASE_INSENSITIVE);
    private static final String STR_NIPOCHJOM = "\\bнипоч[ёе]м\\b";
    public static final Pattern patternNipochjom = Pattern.compile(STR_NIPOCHJOM, CASE_INSENSITIVE);
    private static final String STR_NICHUT = "\\bничуть\\b";
    public static final Pattern patternNichut = Pattern.compile(STR_NICHUT, CASE_INSENSITIVE);
    private static final String STR_NISKOLJKO = "\\bнисколько\\b";
    public static final Pattern patternNiskoljko = Pattern.compile(STR_NISKOLJKO, CASE_INSENSITIVE);
    private static final String STR_NISKOLJECHKO = "\\bнисколечко\\b";
    public static final Pattern patternNiskoljechko = Pattern.compile(STR_NISKOLJECHKO, CASE_INSENSITIVE);


    //REGULAR WORDS

    private static final String STR_NJEKOTORYJ = "\\bнекоторый\\b";
    public static final Pattern patternNjekotoryj = Pattern.compile(STR_NJEKOTORYJ, CASE_INSENSITIVE);
    private static final String STR_LJUBOJ = "\\bлюбой\\b";
    public static final Pattern patternLjuboj = Pattern.compile(STR_LJUBOJ, CASE_INSENSITIVE);
    private static final String STR_MNOGO = "\\bмног(о|ий)\\b";
    public static final Pattern patternMnogo = Pattern.compile(STR_MNOGO, CASE_INSENSITIVE);
    //comparative and superlative
    private static final String STR_BOLJEJE = "\\bболее\\b";
    public static final Pattern patternBoljeje = Pattern.compile(STR_BOLJEJE, CASE_INSENSITIVE);
    private static final String STR_SAMYJ = "\\bсамый\\b";
    public static final Pattern patternSamyj = Pattern.compile(STR_SAMYJ, CASE_INSENSITIVE);
    private static final String STR_SUPERLATIVE_LONG_LEMMAS = "(([^жшщч]ейший)|([жшщч]айший)|(\\b((высший)|(низший)|(лучший)|(худший)|(старший)|(младший))))\\b";
    public static final Pattern patternSuperlativeLongLemmas = Pattern.compile(STR_SUPERLATIVE_LONG_LEMMAS, CASE_INSENSITIVE);
    //partial negation
    private static final String STR_PARTIAL_NEGATION_WORDS = "\\b((вряд)|(редко)|(едва)|(еле)|(еле-еле)|(не совсем)|(навряд)|(наврядли)|(едва-едва)|(нечасто)|(изредка)|(почти не)|(с трудом)|(чуть)|(чуть-чуть))\\b";
    public static final Pattern patternPartialNegationWords = Pattern.compile(STR_PARTIAL_NEGATION_WORDS, CASE_INSENSITIVE);


    //QUESTION WORDS

    private static final String STR_CHTO = "\\bчто\\b";
    public static final Pattern patternChto = Pattern.compile(STR_CHTO, CASE_INSENSITIVE);
    private static final String STR_KTO = "\\bкто\\b";
    public static final Pattern patternKto = Pattern.compile(STR_KTO, CASE_INSENSITIVE);
    private static final String STR_KAK = "\\bкак\\b";
    public static final Pattern patternKak = Pattern.compile(STR_KAK, CASE_INSENSITIVE);
    private static final String STR_POCHJEMU = "\\bпочему\\b";
    public static final Pattern patternPochjemu = Pattern.compile(STR_POCHJEMU, CASE_INSENSITIVE);
    private static final String STR_ZACHJEM = "\\bзачем\\b";
    public static final Pattern patternZachjem = Pattern.compile(STR_ZACHJEM, CASE_INSENSITIVE);
    private static final String STR_GDJE = "\\bгде\\b";
    public static final Pattern patternGdje = Pattern.compile(STR_GDJE, CASE_INSENSITIVE);
    private static final String STR_KOGDA = "\\bкогда\\b";
    public static final Pattern patternKogda = Pattern.compile(STR_KOGDA, CASE_INSENSITIVE);
    private static final String STR_CHJEJ = "\\bчей\\b";
    public static final Pattern patternChjej = Pattern.compile(STR_CHJEJ, CASE_INSENSITIVE);
    private static final String STR_KAKOJ = "\\bкакой\\b";
    public static final Pattern patternKakoj = Pattern.compile(STR_KAKOJ, CASE_INSENSITIVE);
    private static final String STR_KUDA = "\\bкуда\\b";
    public static final Pattern patternKuda = Pattern.compile(STR_KUDA, CASE_INSENSITIVE);
    private static final String STR_KAKOV = "\\bкаков\\b";
    public static final Pattern patternKakov = Pattern.compile(STR_KAKOV, CASE_INSENSITIVE);

    private static final String STR_TAG_QUESTION = ",\\s*(не)?\\s+так(\\s+ли)?\\?";
    public static final Pattern patternTagQuestion = Pattern.compile(STR_TAG_QUESTION, CASE_INSENSITIVE);


    //VERB FORMS

    //reflexive verbs
    private static final String STR_REFLEXIVE_VERB = "(ся|сь)\\b";
    public static final Pattern patternReflexiveVerb = Pattern.compile(STR_REFLEXIVE_VERB, CASE_INSENSITIVE);
    //irregular verbs
    private static final String STR_IRREGULAR_PAST_VERB = "(жечь|шибить|расти|\\bидти|йти)(\\b|ся\\b|сь\\b)";
    public static final Pattern patternIrregularPastVerb = Pattern.compile(STR_IRREGULAR_PAST_VERB, CASE_INSENSITIVE);
    private static final String STR_IRREGULAR_NONPAST_VERB = "(хотеть|бежать|есть|дать|чтить)(\\b|ся\\b|сь\\b)";
    public static final Pattern patternIrregularNonpastVerb = Pattern.compile(STR_IRREGULAR_NONPAST_VERB, CASE_INSENSITIVE);


    //CONJUNCTIONS

    private static final String STR_JESLI = "\\bесли\\b";
    public static final Pattern patternJesli = Pattern.compile(STR_JESLI, CASE_INSENSITIVE);


    //PUNCTUATION

    private static final String STR_QUESTION_MARK = "\\?";
    public static final Pattern patternQuestionMark = Pattern.compile(STR_QUESTION_MARK, CASE_INSENSITIVE);


    //SENTENCE STRUCTURES

    //this pattern is not necessarily complete
    /*public static final String labelQuestionWordMainClause = "questionWordMainClause";
    public static final SemgrexPattern patternQuestionWordMainClause = SemgrexPattern.compile(String.format("{tag:SCONJ}=%s [ <advmod {$} | !< {} ]", labelQuestionWordMainClause));*/

    public static final String labelVerbNoSubject = "verbNoSubject";
    public static final SemgrexPattern patternVerbNoSubject = SemgrexPattern.compile(String.format("{tag:VERB}=%s [ !>nsubj {} ]", labelVerbNoSubject));

    public static final String labelObjectOfVerbNoPreposition = "labelObjectOfVerbNoPreposition"; //TODO
    public static final SemgrexPattern patternObjectOfVerbNoPreposition = SemgrexPattern.compile(String.format("{}=%s [ <obl {tag:VERB} & !>case {tag:ADP}]", labelObjectOfVerbNoPreposition));
}
