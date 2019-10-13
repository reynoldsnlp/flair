package com.flair.server.parser;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.flair.server.utilities.CustomFileReader;
import com.flair.shared.grammar.Language;

public class SimpleDocumentSourceTest
{
	private SimpleDocumentSource simpleSource;
	@Mock
	private SimpleDocumentSource comparisonSource;
	@Mock
	private AbstractDocumentSource invalidSource;
	private String sourceText;
	private Language lang;
	private CustomFileReader fileReader;
	
	@Before 
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		fileReader = new CustomFileReader();
		try
		{
			sourceText = fileReader.readFileToString(fileReader.getRelativePath(), "testFiles/english/GettysburgAddress.txt");
		}
		catch(IOException ex)
		{
			Assert.fail(ex.getMessage());
		}
		lang = Language.ENGLISH;
	}
	
	@Test 
	public void testEmptySource()
	{
		try
		{
			simpleSource = new SimpleDocumentSource("", lang);
		}
		catch(IllegalArgumentException ex)
		{
			return;
		}
		Assert.fail("Failed to throw exception");
	}
	
	@Test 
	public void testShortDescription()
	{
		String shortString = "short";
		simpleSource = new SimpleDocumentSource(shortString, lang);
		Assert.assertTrue("source description does not contain " + shortString + " description is \n" + simpleSource.getDescription(), simpleSource.getDescription().contains(shortString));
	}
	
	@Test 
	public void testLongDescription()
	{
		simpleSource = new SimpleDocumentSource(sourceText, lang);
		Assert.assertTrue("source description is too short, description is \n" + simpleSource.getDescription(), simpleSource.getDescription().contains("..."));
	}
	
	@Test 
	public void testInvalidCompareInstance()
	{
		simpleSource = new SimpleDocumentSource(sourceText, lang);
		try 
		{
			simpleSource.compareTo(invalidSource);
		}
		catch(IllegalArgumentException ex)
		{
			return;
		}
		Assert.fail("Failed to throw an exception when comparing an invalid source class");
	}
	
	@Test 
	public void testGreaterCompare()
	{
		Mockito.when(comparisonSource.getSourceText()).thenReturn(sourceText + "some more text");
		simpleSource = new SimpleDocumentSource(sourceText, lang);
		Assert.assertTrue("compareTo should return a number greater than 0 but instead returned " + simpleSource.compareTo(comparisonSource), simpleSource.compareTo(comparisonSource) > 0);
	}
	
	@Test 
	public void testSmallerCompare()
	{
		Mockito.when(comparisonSource.getSourceText()).thenReturn("something small");
		simpleSource = new SimpleDocumentSource(sourceText, lang);
		Assert.assertTrue("compareTo should return a number less than 0 but instead returned " + simpleSource.compareTo(comparisonSource), simpleSource.compareTo(comparisonSource) < 0);
	}
	
	@Test 
	public void testEqualCompare()
	{
		Mockito.when(comparisonSource.getSourceText()).thenReturn("text.\n");	//am using simple text example due to pre-processing of source text that occurs
		simpleSource = new SimpleDocumentSource("text", lang);
		Assert.assertTrue("compareTo should return a number equal to 0 but instead returned " + simpleSource.compareTo(comparisonSource), simpleSource.compareTo(comparisonSource) == 0);
	}
	
}
