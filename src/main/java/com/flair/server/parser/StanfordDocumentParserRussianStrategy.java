/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import com.flair.shared.grammar.Language;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;



class StanfordDocumentParserRussianStrategy extends BasicStanfordDocumentParserStrategy {
    
    public StanfordDocumentParserRussianStrategy()
    {
	//pipeline = null;
    }
    
    public void setPipeline(StanfordCoreNLP pipeline)
    {
	assert pipeline != null;
	this.pipeline = pipeline;
    }

    public boolean	isLanguageSupported(Language lang){
        return true;
    }
    public boolean	apply(AbstractDocument docToParse){
        return true;
    }

}