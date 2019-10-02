package com.flair.server.grammar;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import java.util.regex.Pattern;

public class RussianGrammaticalPatterns {

    //PARTICLES

    //regex for the interrogative particle 'ли'
    private static final String STR_LI = "\\bли\\b";
    public static Pattern patternLi = Pattern.compile(STR_LI, CASE_INSENSITIVE);
    //regex for conditional particle 'бы'
    private static final String STR_BI = "\\bбы\\b";
    public static Pattern patternBi = Pattern.compile(STR_BI, CASE_INSENSITIVE);
    //regex for conditional particle 'не'
    private static final String STR_NE = "\\bне\\b";
    public static Pattern patternNe = Pattern.compile(STR_NE, CASE_INSENSITIVE);


    //QUESTION WORDS

    //regex for interrogative 'как'
    private static final String STR_KAK = "\\bкак\\b";
    public static Pattern patternKak = Pattern.compile(STR_KAK, CASE_INSENSITIVE);


    //VERB FORMS

    //regex for reflexive verbs
    private static final String STR_REFLEXIVE_VERB = "(ся|сь)\\b";
    public static Pattern patternReflexiveVerb = Pattern.compile(STR_REFLEXIVE_VERB, CASE_INSENSITIVE);


    //SENTENCE STRUCTURES

}
