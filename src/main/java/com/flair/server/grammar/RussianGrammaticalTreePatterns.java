package com.flair.server.grammar;

import edu.stanford.nlp.trees.tregex.TregexPattern;

public class RussianGrammaticalTreePatterns {

    //Tregex for clauses containing 'li' = ли
    private static final String STR_LI = "/^ли$/";
    public static TregexPattern patternLi = TregexPattern.compile(STR_LI);

    //Tregex for clauses containing 'bi' = бы
    private static final String STR_BI = "/^бы$/";
    public static TregexPattern patternBi = TregexPattern.compile(STR_BI);
}
