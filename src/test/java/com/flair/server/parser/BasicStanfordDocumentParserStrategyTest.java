package com.flair.server.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.flair.server.parser.BasicStanfordDocumentParserStrategy;
import com.flair.shared.grammar.Language;

public class BasicStanfordDocumentParserStrategyTest 
{
	private BasicStanfordDocumentParserStrategy basicParserStrategy;
	private StanfordDocumentParserStrategyFactory englishStanfordParserFactory;
	private StanfordDocumentParserStrategyFactory germanStanfordParserFactory;
	private StanfordDocumentParserStrategyFactory russianStanfordParserFactory;
	private StanfordDocumentParserStrategyFactory arabicStanfordParserFactory;

	private void factoryTest(AbstractParsingStrategy strategy, AbstractParsingStrategyFactory factory)
	{
		basicParserStrategy = null;
		basicParserStrategy = (BasicStanfordDocumentParserStrategy) factory.create();
		Assert.assertNotNull(basicParserStrategy);
		Assert.assertEquals(strategy.getClass(), basicParserStrategy.getClass());
	}
	
	@Before
	public void setUp()
	{
		englishStanfordParserFactory = new StanfordDocumentParserStrategyFactory(Language.ENGLISH);
		germanStanfordParserFactory = new StanfordDocumentParserStrategyFactory(Language.GERMAN);
		russianStanfordParserFactory = new StanfordDocumentParserStrategyFactory(Language.RUSSIAN);
		arabicStanfordParserFactory = new StanfordDocumentParserStrategyFactory(Language.ARABIC);
	}
	
	@Test 
	public void testEnglishParserFactory()
	{
		factoryTest(new StanfordDocumentParserEnglishStrategy(), englishStanfordParserFactory);	
	}
	
	@Test 
	public void testGermanParserFactory()
	{
		factoryTest(new StanfordDocumentParserGermanStrategy(), germanStanfordParserFactory);	
	}
	
	@Test 
	public void testRussianParserFactory()
	{
		factoryTest(new StanfordDocumentParserRussianStrategy(), russianStanfordParserFactory);	
	}
	
	@Test 
	public void testArabicParserFactory()
	{
		factoryTest(new StanfordDocumentParserArabicStrategy(), arabicStanfordParserFactory);	
	}
	
	
	
}