/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */
package com.flair.server.crawler.impl.azure;

import com.flair.server.crawler.impl.AbstractSearchAgentImplResult;

/**
 * Represents a web page in a Azure web search result
 * @author shadeMe
 */
public class AzureWebSearchResult extends AbstractSearchAgentImplResult
{
    public AzureWebSearchResult(String name, String url, String displayUrl, String snippet) {
	super(name, url, displayUrl, snippet);
    }
}