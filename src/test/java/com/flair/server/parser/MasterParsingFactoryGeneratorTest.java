package com.flair.server.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.flair.shared.grammar.Language;

public class MasterParsingFactoryGeneratorTest
{
	private Language lang;
	private ParserType parserType;
	private KeywordSearcherType keywordSearcherType;
	
	@Before
	public void setUp()
	{
		parserType = ParserType.STANFORD_CORENLP;
		keywordSearcherType = KeywordSearcherType.NAIVE_SUBSTRING;
		lang = Language.ENGLISH;
	}
	
	private void testException(String method)
	{
		parserType = ParserType.TEST;
		keywordSearcherType = KeywordSearcherType.TEST;
		try
		{
			if(method.equalsIgnoreCase("createParsingStrategy"))
			{
				MasterParsingFactoryGenerator.createParsingStrategy(parserType, lang);
			}
			else if(method.equalsIgnoreCase("createParser"))
			{				
				MasterParsingFactoryGenerator.createParser(parserType, lang);
			}
			else if(method.equalsIgnoreCase("createKeywordSearcher"))
			{
				MasterParsingFactoryGenerator.createKeywordSearcher(keywordSearcherType);
			}
		}
		catch(IllegalArgumentException ex)
		{
			return;
		}
		Assert.fail("Failed to catch exception");
	}
	
	@Test
	public void testCreateParser()
	{
		Assert.assertTrue(MasterParsingFactoryGenerator.createParser(parserType, lang).getClass() == StanfordDocumentParserFactory.class);
	}
	
	@Test
	public void testCreateArabicParser()
	{
		lang = Language.ARABIC;
		Assert.assertTrue(MasterParsingFactoryGenerator.createParser(parserType, lang).getClass() == StanfordDocumentParserFactory.class);
	}
	
	@Test 
	public void testCreateInvalidParser()
	{
		testException("createParser");
	}
	
	@Test
	public void testCreateParseStrategy()
	{
		Assert.assertTrue(MasterParsingFactoryGenerator.createParsingStrategy(parserType, lang).getClass() == StanfordDocumentParserStrategyFactory.class);
	}
	
	@Test 
	public void testCreateInvalidParsingStrategy()
	{
		testException("createParsingStrategy");
	}
	
	@Test
	public void testCreateKeyWordSearcher()
	{
		Assert.assertTrue(MasterParsingFactoryGenerator.createKeywordSearcher(keywordSearcherType).getClass() == NaiveSubstringKeywordSearcherFactory.class);
	}
	
	@Test 
	public void testCreateInvalidKeyWordSearcher()
	{
		testException("createKeywordSearcher");
	}
}
