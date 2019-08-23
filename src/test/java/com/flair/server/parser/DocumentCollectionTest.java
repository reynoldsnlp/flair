package com.flair.server.parser;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;

public class DocumentCollectionTest 
{
	private DocumentCollection documentCollection;
	private DocumentCollection germanDocumentCollection;
	private DocumentCollection russianDocumentCollection;
	private DocumentCollection arabicDocumentCollection;
	@Mock
	private DocumentConstructionData documentConstructionData;
	@Mock
	private Document document;
	private ArabicDocument arabicDocument;
	private boolean refresh;
	private int numConstructions;
	private int constructionCount;
	
	@Before
	public void setUp()
	{
        MockitoAnnotations.initMocks(this);
        
        numConstructions = 2;
        documentCollection = new DocumentCollection(Language.ENGLISH);
		
		when(document.getLanguage()).thenReturn(Language.ENGLISH);
		when(documentConstructionData.hasConstruction()).thenReturn(true);
		when(documentConstructionData.getFrequency()).thenReturn(numConstructions);
		when(document.getConstructionData(any(GrammaticalConstruction.class))).thenReturn(documentConstructionData);
	}
	
	@Test 
	public void testAddException()
	{
		boolean threwException = false;
		try
		{
			when(document.getLanguage()).thenReturn(Language.GERMAN);
			documentCollection.add(document, false);
		}
		catch(IllegalArgumentException e)
		{
			threwException = true;
		}
		Assert.assertTrue(threwException);
	}
	
	@Test 
	public void testAddAndRefresh()
	{
		refresh = true;
		documentCollection.add(document, refresh);
		DocumentCollectionConstructionData data = (DocumentCollectionConstructionData) documentCollection.getConstructionData().getData(GrammaticalConstruction.ASPECT_SIMPLE);
		constructionCount = data.getTotalCount();
		Assert.assertEquals("documentCollection construction data is " + constructionCount, numConstructions, constructionCount);
	}
	
	@Test 
	public void testNoRefresh()
	{
		refresh = false;
		documentCollection = new DocumentCollection(Language.ENGLISH);
		documentCollection.add(document, refresh);
		DocumentCollectionConstructionData data = (DocumentCollectionConstructionData) documentCollection.getConstructionData().getData(GrammaticalConstruction.ASPECT_SIMPLE);
		constructionCount = data.getTotalCount();
		Assert.assertEquals("documentCollection construction data is " + constructionCount, 0, constructionCount);
	}
	
}