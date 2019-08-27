package com.flair.server.parser;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KeywordSearcherOutputTest
{
	private KeywordSearcherOutput keywordSearcherOutput;
	private KeywordSearcherInput keywordSearcherInput;
	private List<String> keywords;	
	private String keyword;
	private int start;
	private int end;
	private int expected;
	private double delta;
	
	private void testIllegalArgumentException(String method, String keyword)
	{
		try
		{
			if(method.equalsIgnoreCase("getHits"))
			{
				keywordSearcherOutput.getHits(keyword);
			}
			else if(method.equalsIgnoreCase("addHit"))
			{
				keywordSearcherOutput.addHit(keyword, start, end);
			}
			else if(method.equalsIgnoreCase("getHitCount"))
			{
				keywordSearcherOutput.getHitCount(keyword);
			}
			else 
			{
				Assert.fail(method + " not supported by testIllegalArgumentException");
			}
		}
		catch(IllegalArgumentException ex) 
		{
			return;
		}
		Assert.fail("Failed to catch illegal argument exception");
	}
	
	private void addTwoHits(String keyword)
	{
		try
		{
			keywordSearcherOutput.addHit(keyword, start, end);
			start = 3;
			end = 8;
			keywordSearcherOutput.addHit(keyword, start, end);
		}
		catch(IllegalArgumentException ex)
		{
			Assert.fail(ex.getMessage());
		}
	}
	
	@Before
	public void setUp()
	{
		keywords = new ArrayList<String>();
		keywords.add("word1");
		keywords.add("word2");
		keywords.add("word3");
		keywordSearcherInput = new KeywordSearcherInput(keywords);
		keywordSearcherOutput = new KeywordSearcherOutput(keywordSearcherInput);
		start = 0;
		end = 2;
		delta = 0;
	}
	
	@Test
	public void testAddHit()
	{
		expected = 2;
		keyword = keywords.get(0);
		addTwoHits(keyword);
		Assert.assertEquals(expected, keywordSearcherOutput.getHitMap().get(keyword).size());
	}
	
	@Test
	public void testAddHitIllegalArgument()
	{
		keyword = "notAWord";
		testIllegalArgumentException("addHit", keyword);
	}
	
	@Test 
	public void testGetHitsIllegalArumentException()
	{
		expected = 0;
		keyword = keywords.get(1);
		try
		{
			Assert.assertEquals(expected, keywordSearcherOutput.getHits(keyword).size());
		}
		catch(IllegalArgumentException ex)
		{
			Assert.fail(ex.getMessage());
		}
	}
	
	@Test 
	public void testGetHits()
	{
		keyword = "notAWord";
		testIllegalArgumentException("getHits", keyword);
	}
	
	@Test 
	public void testGetHitCount()
	{
		expected = 2;
		keyword = keywords.get(2);
		addTwoHits(keyword);
		Assert.assertEquals((double) expected, keywordSearcherOutput.getHitCount(keyword), delta);
	}
	
	@Test 
	public void testGetHitCountIllegalArgument()
	{
		keyword = "notAWord";
		testIllegalArgumentException("getHitCount", keyword);
	}
	
	@Test 
	public void testGetTotalHitCount()
	{
		expected = 2 * keywords.size();
		for(String keyword : keywords)
		{
			addTwoHits(keyword);
		}
		Assert.assertEquals((double) expected, keywordSearcherOutput.getTotalHitCount(), delta);
	}

}
