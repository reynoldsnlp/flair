package com.flair.server.grammar;

import edu.stanford.nlp.trees.tregex.TregexPattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import java.util.regex.Pattern;

public class RussianGrammaticalTreePatterns {

    //regex for clauses containing 'ли'
    private static final String STR_LI = "\\bли\\b";
    public static Pattern patternLi = Pattern.compile(STR_LI, CASE_INSENSITIVE);

    //regex for clauses containing 'бы'
    private static final String STR_BI = "\\bбы\\b";
    public static Pattern patternBi = Pattern.compile(STR_BI, CASE_INSENSITIVE);
}
