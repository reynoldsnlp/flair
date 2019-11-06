package com.flair.server.grammar;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import java.util.regex.Pattern;

public class RussianGrammaticalPatterns {

    //EXISTENTIALS

    private static final String STR_JEST = "\\bесть\\b";
    public static Pattern patternJest = Pattern.compile(STR_JEST, CASE_INSENSITIVE);
    private static final String STR_NJET = "\\bнет\\b";
    public static Pattern patternNjet = Pattern.compile(STR_NJET, CASE_INSENSITIVE);


    //PARTICLES

    private static final String STR_LI = "\\bли\\b";
    public static Pattern patternLi = Pattern.compile(STR_LI, CASE_INSENSITIVE);
    private static final String STR_BI = "\\bбы\\b";
    public static Pattern patternBi = Pattern.compile(STR_BI, CASE_INSENSITIVE);
    private static final String STR_NE = "\\bне\\b";
    public static Pattern patternNe = Pattern.compile(STR_NE, CASE_INSENSITIVE);


    //REGULAR WORDS

    private static final String STR_NJEKOTORYJ = "\\bнекоторый\\b";
    public static Pattern patternNjekotoryj = Pattern.compile(STR_NJEKOTORYJ, CASE_INSENSITIVE);
    private static final String STR_LJUBOJ = "\\bлюбой\\b";
    public static Pattern patternLjuboj = Pattern.compile(STR_LJUBOJ, CASE_INSENSITIVE);
    private static final String STR_MNOGO = "\\bмног(о|ий)\\b";
    public static Pattern patternMnogo = Pattern.compile(STR_MNOGO, CASE_INSENSITIVE);


    //QUESTION WORDS

    private static final String STR_CHTO = "\\bчто\\b";
    public static Pattern patternChto = Pattern.compile(STR_CHTO, CASE_INSENSITIVE);
    private static final String STR_KTO = "\\bкто\\b";
    public static Pattern patternKto = Pattern.compile(STR_KTO, CASE_INSENSITIVE);
    private static final String STR_KAK = "\\bкак\\b";
    public static Pattern patternKak = Pattern.compile(STR_KAK, CASE_INSENSITIVE);
    private static final String STR_POCHJEMU = "\\bпочему\\b";
    public static Pattern patternPochjemu = Pattern.compile(STR_POCHJEMU, CASE_INSENSITIVE);
    private static final String STR_ZACHJEM = "\\bзачем\\b";
    public static Pattern patternZachjem = Pattern.compile(STR_ZACHJEM, CASE_INSENSITIVE);
    private static final String STR_GDJE = "\\bгде\\b";
    public static Pattern patternGdje = Pattern.compile(STR_GDJE, CASE_INSENSITIVE);
    private static final String STR_KOGDA = "\\bкогда\\b";
    public static Pattern patternKogda = Pattern.compile(STR_KOGDA, CASE_INSENSITIVE);
    private static final String STR_CHJEJ = "\\bчей\\b";
    public static Pattern patternChjej = Pattern.compile(STR_CHJEJ, CASE_INSENSITIVE);
    private static final String STR_KAKOJ = "\\bкакой\\b";
    public static Pattern patternKakoj = Pattern.compile(STR_KAKOJ, CASE_INSENSITIVE);
    private static final String STR_KUDA = "\\bкуда\\b";
    public static Pattern patternKuda = Pattern.compile(STR_KUDA, CASE_INSENSITIVE);
    private static final String STR_KAKOV = "\\bкаков\\b";
    public static Pattern patternKakov = Pattern.compile(STR_KAKOV, CASE_INSENSITIVE);


    //VERB FORMS

    //regex for reflexive verbs
    private static final String STR_REFLEXIVE_VERB = "(ся|сь)\\b";
    public static Pattern patternReflexiveVerb = Pattern.compile(STR_REFLEXIVE_VERB, CASE_INSENSITIVE);


    //PUNCTUATION

    private static final String STR_QUESTION_MARK = "\\?";
    public static Pattern patternQuestionMark = Pattern.compile(STR_QUESTION_MARK, CASE_INSENSITIVE);


    //SENTENCE STRUCTURES

}
