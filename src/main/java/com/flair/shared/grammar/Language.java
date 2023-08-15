/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.shared.grammar;

/**
 * Represents the languages FLAIR supports
 * @author shadeMe
 */
public enum Language
{
    ENGLISH,
    GERMAN,
    RUSSIAN,
    ARABIC,
    PERSIAN,
    TEST,	//useful for unit tests
    ;
    
    public static Language fromString(String lang)
    {
    	if (lang.equalsIgnoreCase(ENGLISH.name()))
    		return ENGLISH;
    	else if (lang.equalsIgnoreCase(GERMAN.name()))
            return GERMAN;
        else if (lang.equalsIgnoreCase(RUSSIAN.name()))
            return RUSSIAN;
        else if (lang.equalsIgnoreCase(ARABIC.name()))
    		return ARABIC;
        else if (lang.equalsIgnoreCase(PERSIAN.name()))
            return PERSIAN;
        else if (lang.equalsIgnoreCase(TEST.name()))
    		return TEST;
    	else
    		throw new RuntimeException("Invalid language string " + lang);
    }
}
