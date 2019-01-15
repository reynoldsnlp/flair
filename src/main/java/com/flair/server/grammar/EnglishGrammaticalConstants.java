/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.grammar;

import java.util.Arrays;
import java.util.List;

/**
 * Stable constants for the English language grammar
 * @author shadeMe
 */
public class EnglishGrammaticalConstants
{
    public static final List<String> QUESTION_WORDS = Arrays.asList("what", "who","how","why","where","when","whose","whom","which");
    
    public static final List<String> SIMPLE_PREPOSITIONS = Arrays.asList("in", "at", "on", "with", "after", "to");
    
    public static final List<String> NEGATION = Arrays.asList("neither", "nobody", "none", "nothing", "nor", "nowhere"); // "never" is counted along with "n't" and "not" in neg() dependency , "neither...nor" will be counted as 2 negations
    public static final List<String> PARTIAL_NEGATION = Arrays.asList("hardly", "scarcely", "rarely", "seldom", "barely");
    
    public static final List<String> ING_NOUNS = Arrays.asList("thing", "something", "anything", "nothing", "morning", "evening", "everything", "spring", "string", "swing", "darling", "ceiling", "clothing", "sterling", "earring", "viking", "fling"); // longer than 5 characters
    
    // around 40 advanced conjunctions
    public static final List<String> ADVANCED_CONJUNCTIONS = Arrays.asList("finally","nor","yet","though","although","if","while","unless","until","lest","whether","wheras","once","since","till","until","whenever","wherever","besides","further","furthermore","indeed","likewise","incidentally","moreover","however","nevertheless","nonetheless","still","conversely","instead","otherwise","accordingly","namely","consequently","hence","thus","meanwhile","therefore");
    public static final List<String> SIMPLE_CONJUNCTIONS = Arrays.asList("and", "or", "but", "because", "so");
    
    
    public static final List<String> OBJECTIVE_PRONOUNS = Arrays.asList("me", "you", "him", "her", "them", "us", "it");
    public static final List<String> SUBJECTIVE_PRONOUNS = Arrays.asList("I", "you", "he", "she", "they", "we", "it");
    public static final List<String> POSSESSIVE_PRONOUNS = Arrays.asList("my", "your", "his", "her", "their", "our", "its");
    public static final List<String> POSSESSIVE_ABSOLUTE_PRONOUNS = Arrays.asList("mine", "yours", "hers", "theirs", "ours"); // his and its omitted
    public static final List<String> REFLEXIVE_PRONOUNS = Arrays.asList("myself", "yourself", "himself", "herself", "themselves", "ourselves", "itself", "yourselves");
}
