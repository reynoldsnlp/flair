package com.flair.server.parser;

import static com.flair.shared.grammar.GrammaticalConstruction.SENTENCE_SIMPLE;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.flair.shared.grammar.Language;

public class ConstructionDataCollectionTest 
{
	private ConstructionDataCollection constructionDataCollection;
	@Mock 
	private AbstractDocumentSource englishSource;
	
	@Before
	public void setUp()
	{
        MockitoAnnotations.initMocks(this);
        when(englishSource.getLanguage()).thenReturn(Language.ENGLISH);
        when(englishSource.getSourceText()).thenReturn("This is a source text sentence");
	}
	
	@Test 
	public void testConstructionDataConstructor()
	{
        constructionDataCollection = new ConstructionDataCollection(Language.ENGLISH, new DocumentConstructionDataFactory(new Document(englishSource)));
        Assert.assertNotNull(constructionDataCollection.getData(SENTENCE_SIMPLE));
	}
	
	@Test 
	public void testEmptyConstructionDataConstructor()
	{
        constructionDataCollection = new ConstructionDataCollection(Language.TEST, new DocumentConstructionDataFactory(new Document(englishSource)));
        Assert.assertNull(constructionDataCollection.getData(SENTENCE_SIMPLE));
	}
}