package com.flair.server.parser;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class KeywordSearcherInputTest 
{
	private KeywordSearcherInput keywordSearcherInput;
	
	@Before
	public void setUp()
	{
		keywordSearcherInput = new KeywordSearcherInput();
	}
	
	@Test 
	public void testAddKeyword()
	{
		keywordSearcherInput.addKeyword("Word");
		Assert.assertEquals("word", keywordSearcherInput.getKeywords().get(0));
	}
	
	@Test
	public void testAddExistingKeyword()
	{
		keywordSearcherInput.addKeyword("Word");
		keywordSearcherInput.addKeyword("WoRd");
		Assert.assertEquals("word", keywordSearcherInput.getKeywords().get(0));
		Assert.assertEquals(1, keywordSearcherInput.getKeywords().size());
	}
}
