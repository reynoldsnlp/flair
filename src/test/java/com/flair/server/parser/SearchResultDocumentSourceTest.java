package com.flair.server.parser;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.flair.server.crawler.SearchResult;
import com.flair.server.utilities.CustomFileReader;
import com.flair.shared.grammar.Language;

public class SearchResultDocumentSourceTest
{
	private SearchResultDocumentSource documentSource;
	@Mock
	private SearchResult result;
	@Mock
	private AbstractDocumentSource abstractSource;
	@Mock
	private SearchResultDocumentSource comparisonSource;
	private CustomFileReader fileReader;
	private String resultString;
	
	private void testCompareTo(int comparisonRank, int expected)
	{
		Mockito.when(comparisonSource.getParentRank()).thenReturn(comparisonRank);
		documentSource = new SearchResultDocumentSource(result);
		Assert.assertEquals(expected, documentSource.compareTo(comparisonSource));
	}
	
	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		fileReader = new CustomFileReader();
		try
		{
			resultString = fileReader.readFileToString(fileReader.getRelativePath(), "testFiles/english/GettysburgAddress.txt");
		}
		catch(IOException ex)
		{
			Assert.fail(ex.getMessage());
		}
		Mockito.when(result.getPageText()).thenReturn(resultString);
		Mockito.when(result.getLanguage()).thenReturn(Language.ENGLISH);
		Mockito.when(result.isTextFetched()).thenReturn(true);
		Mockito.when(result.getRank()).thenReturn(2);
	}
		
	@Test 
	public void testTextIsFetched()
	{
		try
		{
			documentSource = new SearchResultDocumentSource(result);
		}
		catch(Exception ex)
		{
			Assert.fail(ex.getMessage());
		}
	}
	
	@Test 
	public void testNoTextFetched()
	{
		Mockito.when(result.isTextFetched()).thenReturn(false);
		try
		{
			documentSource = new SearchResultDocumentSource(result);
		}
		catch(IllegalArgumentException ex)
		{
			return;
		}
		Assert.fail("Failed to catch exception");
	}
	
	@Test 
	public void testInvalidDocumentInstance()
	{
		documentSource = new SearchResultDocumentSource(result);
		try
		{
			documentSource.compareTo(abstractSource);
		}
		catch(IllegalArgumentException ex)
		{
			return;
		}
		Assert.fail("Failed to catch exception");
	}
	
	@Test 
	public void testCompareGreaterRank()
	{
		testCompareTo(3, -1);
	}
	
	@Test 
	public void testCompareSmallerRank()
	{
		testCompareTo(1, 1);
	}
	
	@Test 
	public void testCompareEqualRank()
	{
		testCompareTo(2, 0);
	}
	
}
