/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import com.flair.shared.grammar.Language;

/**
 * Represents an abstract document parser
 * @author shadeMe
 */
public abstract class AbstractDocumentParser
{
    protected final AbstractDocumentFactory	    docFactory;
    
    public AbstractDocumentParser(AbstractDocumentFactory factory)
    {
	assert factory != null;
	docFactory = factory;
    }
    
    /**
     * Checks to see if language is supported by the parser
     * @param lang Language to be checked
     * @return Boolean value corresponding with the implementation status of a language, true if supported, false otherwise
     */
    public abstract boolean		    isLanguageSupported(Language lang);

    /**
     * Parses a source document using a specified strategy
     * @param source Document source to be parsed
     * @param strategy Parse strategy to be utilized by the parser
     * @return Parsed document
     */
    public abstract AbstractDocument	    parse(AbstractDocumentSource source, AbstractParsingStrategy strategy);
}
