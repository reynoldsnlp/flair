/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */
package com.flair.server.grammar;

import java.util.ArrayList;
import java.util.List;

import com.flair.shared.grammar.Language;

/**
 * Wrapper class for the default keywords for each language
 * 
 * @author shadeMe
 */
public class DefaultVocabularyList
{
	public static List<String> get(Language lang) 
	{
		switch (lang)
		{
		case ENGLISH:
			return EnglishAcademicWordList.getKeywords();
		case GERMAN:
			// ### TODO add an academic word list for german
			return new ArrayList<>();
		case RUSSIAN:
			// ### TODO add an academic word list for russian
			return new ArrayList<>();
		case ARABIC:
			// ### TODO add an academic word list for arabic
			return new ArrayList<>();
		default:
			throw new IllegalArgumentException("Invalid language " + lang);
		}
	}
}
