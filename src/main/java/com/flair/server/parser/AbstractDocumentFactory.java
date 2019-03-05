/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

/**
 * Interface used to create AbstractDocument objects
 * @author shadeMe
 */
public interface AbstractDocumentFactory
{
   /**
    * Creates an AbstractDocument object
    * @param source AbstractDocumentSource object
    * @return AbstractDocument object
    */
   public AbstractDocument	create(AbstractDocumentSource source);
}
