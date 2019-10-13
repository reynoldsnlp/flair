package com.flair.server.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NaiveSubstringKeywordSearcherTest
{
	private KeywordSearcherInput keywordSearcherInput;
	private NaiveSubstringKeywordSearcher keywordSearcher;
	private KeywordSearcherOutput output;
	@Mock
	private AbstractDocument document;
	private String documentText;
	private List<String> keywords;	
	private NaiveSubstringKeywordSearcherFactory factory;
	
	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		factory = new NaiveSubstringKeywordSearcherFactory();
		keywordSearcher = (NaiveSubstringKeywordSearcher) factory.create();
		keywords = new ArrayList<String>();
		keywords.add("a");
		keywords.add("big");
		keywords.add("potato");
		keywordSearcherInput = new KeywordSearcherInput(keywords);
		documentText = "This is\na sentence about a big potato. The big-potato is not a small potato. The potato\n is not medium sized.";
		//a = 3
		//big = 2
		//potato = 4
		Mockito.when(document.getText()).thenReturn(documentText);
	}
	
	@Test 
	public void testNullInputSearch()
	{
		try
		{
			keywordSearcher.search(null, keywordSearcherInput);
		}
		catch(IllegalArgumentException ex)
		{
			try
			{
				keywordSearcher.search(document, null);
			}
			catch(IllegalArgumentException e)
			{
				return;
			}
		}
		Assert.fail("failed to throw exception on null input");
	}
	
	@Test 
	public void testValidBoundarySearch()
	{
		output = keywordSearcher.search(document, keywordSearcherInput);
		Assert.assertEquals(9, output.getTotalHitCount(), 0);
		Assert.assertEquals(3, output.getHitCount("a"), 0);
		Assert.assertEquals(2, output.getHitCount("big"), 0);
		Assert.assertEquals(4, output.getHitCount("potato"), 0);
	}
	
	@Test 
	public void testInvalidBoundarySearch()
	{
		documentText = "Thiisasentenceabouta bigpotato.Thebig-potatoisnot asmall potatoThe potatois not medium sized.";
		Mockito.when(document.getText()).thenReturn(documentText);
		output = keywordSearcher.search(document, keywordSearcherInput);
		Assert.assertEquals(0, output.getTotalHitCount(), 0);
	}
	
}
