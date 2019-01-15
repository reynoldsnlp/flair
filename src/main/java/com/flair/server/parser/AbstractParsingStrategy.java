/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import com.flair.shared.grammar.Language;

/**
 * Represents an abstract parsing logic
 * @author shadeMe
 */
public interface AbstractParsingStrategy
{
    boolean	isLanguageSupported(Language lang);
    boolean	apply(AbstractDocument docToParse);	    // returns true if successful, false otherwise
}