package com.flair.server.parser;


import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.flair.server.utilities.CustomFileReader;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.DocumentReadabilityLevel;

public class DocumentTest 
{
	private Document document;
	private SimpleDocumentSource source; 
	private DocumentReadabilityLevel documentReadabilityLevel;
	private CustomFileReader customFileReader;
	
	private String englishLevel_AText;
	private String englishLevel_BText;
	private String englishLevel_CText;
	
	private String englishSourceNameLevel_A;
	private String englishSourceNameLevel_B;
	private String englishSourceNameLevel_C;
	
	private String germanLevel_AText;
	private String germanLevel_BText;
	private String germanLevel_CText;
	
	private String germanSourceNameLevel_A;
	private String germanSourceNameLevel_B;
	private String germanSourceNameLevel_C;
	
	private String russianLevel_CText;
	private String russianLevel_AText;
	private String russianLevel_BText;
	
	private String russianSourceNameLevel_A;
	private String russianSourceNameLevel_B;
	private String russianSourceNameLevel_C;
		
	private void addConstructions(AbstractDocument document)
	{
		for (GrammaticalConstruction itr : GrammaticalConstruction.getForLanguage(document.getLanguage()))
		{
			document.getConstructionData(itr).addOccurrence(0, 1);
		}
	}
	
	private void setSourceReturn(Language lang, DocumentReadabilityLevel level, SimpleDocumentSource source)
	{
		when(source.getLanguage()).thenReturn(lang);
		if(lang.equals(Language.ENGLISH))
		{
			if(level.equals(DocumentReadabilityLevel.LEVEL_A))
			{
				when(source.getSourceText()).thenReturn(englishLevel_AText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_B))
			{
				when(source.getSourceText()).thenReturn(englishLevel_BText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_C))
			{
				when(source.getSourceText()).thenReturn(englishLevel_CText);
			}	
		}
		else if(lang.equals(Language.GERMAN))
		{
			if(level.equals(DocumentReadabilityLevel.LEVEL_A))
			{
				when(source.getSourceText()).thenReturn(germanLevel_AText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_B))
			{
				when(source.getSourceText()).thenReturn(germanLevel_BText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_C))
			{
				when(source.getSourceText()).thenReturn(germanLevel_CText);
			}	
		}
		else if(lang.equals(Language.RUSSIAN))
		{
			if(level.equals(DocumentReadabilityLevel.LEVEL_A))
			{
				when(source.getSourceText()).thenReturn(russianLevel_AText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_B))
			{
				when(source.getSourceText()).thenReturn(russianLevel_BText);
			}
			else if(level.equals(DocumentReadabilityLevel.LEVEL_C))
			{
				when(source.getSourceText()).thenReturn(russianLevel_CText);
			}	
		}
		
	}
	
	private void testConstructor(Language lang, DocumentReadabilityLevel level, SimpleDocumentSource source)
	{
		try
	    {	
			setSourceReturn(lang, level, source);
	    	document = new Document(source);
	        Assert.assertEquals(lang, document.getLanguage());
            Assert.assertEquals(level, document.getReadabilityLevel());	           
	    }
	    catch(Exception e)
	    {
	    	ServerLogger.get().error(e, "");
	    	Assert.fail();
	    }
	}
	
	@Before 
	public void setUp()
	{
        MockitoAnnotations.initMocks(this);
        
		documentReadabilityLevel = DocumentReadabilityLevel.LEVEL_A;

        englishSourceNameLevel_A = "testFiles/english/enLevel_A.txt";
        englishSourceNameLevel_B = "testFiles/english/enLevel_B.txt";
        englishSourceNameLevel_C = "testFiles/english/enLevel_C.txt";
        
        germanSourceNameLevel_A = "testFiles/german/deLevel_A.txt";
        germanSourceNameLevel_B = "testFiles/german/deLevel_B.txt";
        germanSourceNameLevel_C = "testFiles/german/deLevel_C.txt";
        
        russianSourceNameLevel_A = "testFiles/russian/ruLevel_A.txt";
        russianSourceNameLevel_B = "testFiles/russian/ruLevel_B.txt";
        russianSourceNameLevel_C = "testFiles/russian/ruLevel_C.txt";
        
		customFileReader = new CustomFileReader();
		try 
		{
			//ServerLogger.get().info(customFileReader.getRelativePath() + sourceName);
			englishLevel_AText = customFileReader.readFileToString(customFileReader.getRelativePath(), englishSourceNameLevel_A);
			englishLevel_BText = customFileReader.readFileToString(customFileReader.getRelativePath(), englishSourceNameLevel_B);
			englishLevel_CText = customFileReader.readFileToString(customFileReader.getRelativePath(), englishSourceNameLevel_C);
			
			germanLevel_AText = customFileReader.readFileToString(customFileReader.getRelativePath(), germanSourceNameLevel_A);
			germanLevel_BText = customFileReader.readFileToString(customFileReader.getRelativePath(), germanSourceNameLevel_B);
			germanLevel_CText = customFileReader.readFileToString(customFileReader.getRelativePath(), germanSourceNameLevel_C);
			
			russianLevel_AText = customFileReader.readFileToString(customFileReader.getRelativePath(), russianSourceNameLevel_A);
			russianLevel_BText = customFileReader.readFileToString(customFileReader.getRelativePath(), russianSourceNameLevel_B);
			russianLevel_CText = customFileReader.readFileToString(customFileReader.getRelativePath(), russianSourceNameLevel_C);
		} 
		catch (IOException e) 
		{
			ServerLogger.get().error(e, "");
		}
		source = spy(new SimpleDocumentSource(" ", Language.ENGLISH));
		document = new Document(source, 1.0, documentReadabilityLevel);
	}
	
	 
	@Test 
	public void testCalculateFancyDocLength()
	{
		double fancyDocLength = 10.862780491200215;
		addConstructions(document);
		document.calculateFancyDocLength();
		Assert.assertEquals(fancyDocLength, document.getFancyLength(), .1);
		
	}
	
	@Test 
	public void testEmptyDocFancyDocLength()
	{
		document.calculateFancyDocLength();
		Assert.assertEquals(0, document.getFancyLength(), 0);
	}
	
	@Test 
	public void testEnglishConstructorLevel_A()
	{
		testConstructor(Language.ENGLISH, DocumentReadabilityLevel.LEVEL_A, source);
	}
	@Test 
	public void testEnglishConstructorLevel_B()
	{
		testConstructor(Language.ENGLISH, DocumentReadabilityLevel.LEVEL_B, source);
	}
	@Test 
	public void testEnglishConstructorLevel_C()
	{
		testConstructor(Language.ENGLISH, DocumentReadabilityLevel.LEVEL_C, source);
	}
	@Test 
	public void testGermanConstructorLevel_A()
	{
		testConstructor(Language.GERMAN, DocumentReadabilityLevel.LEVEL_A, source);
	}
	@Test 
	public void testGermanConstructorLevel_B()
	{
		testConstructor(Language.GERMAN, DocumentReadabilityLevel.LEVEL_B, source);
	}
	@Test 
	public void testGermanConstructorLevel_C()
	{
		testConstructor(Language.GERMAN, DocumentReadabilityLevel.LEVEL_C, source);
	}
	@Test 
	public void testRussianConstructorLevel_A()
	{
		testConstructor(Language.RUSSIAN, DocumentReadabilityLevel.LEVEL_A, source);
	}
	@Test 
	public void testRussianConstructorLevel_B()
	{
		testConstructor(Language.RUSSIAN, DocumentReadabilityLevel.LEVEL_B, source);
	}
	@Test 
	public void testRussianConstructorLevel_C()
	{
		testConstructor(Language.RUSSIAN, DocumentReadabilityLevel.LEVEL_C, source);
	}
	
}
