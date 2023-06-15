/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import com.flair.shared.grammar.Language;

import com.flair.server.stanza.StanzaPipeline;


/**
 * Base strategy for the StanzaDocumentParser class
 * @author shadeMe
 */
abstract class BasicStanzaDocumentParserStrategy implements AbstractParsingStrategy
{
    protected StanzaPipeline		    pipeline;
    
    public BasicStanzaDocumentParserStrategy()
    {
	pipeline = null;
    }
    
    public void setPipeline(StanzaPipeline pipeline)
    {
	assert pipeline != null;
	this.pipeline = pipeline;
    }
}

class StanzaDocumentParserStrategyFactory implements AbstractParsingStrategyFactory
{
    private final Language		lang;

    public StanzaDocumentParserStrategyFactory(Language lang) {
	this.lang = lang;
    }
    
    @Override
    public AbstractParsingStrategy create()
    {
	switch (lang)
	{
        case PERSIAN:
            return new StanzaDocumentParserPersianStrategy();
	    default:
		throw new IllegalArgumentException("Language unsupported: " + lang);
	}
    }
}