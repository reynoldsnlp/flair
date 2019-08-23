package com.flair.server.parser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flair.shared.grammar.GrammaticalConstruction;

import junit.framework.Assert;

public class DocumentCollectionConstructionDataTest 
{
	private DocumentCollectionConstructionData collectionConstructions;
	@Mock
	private DocumentCollection collection;
	private GrammaticalConstruction type;
	private int totalDocCount;
	private int occurrencesInCollection;
	private int numDocsWithOccurrences;
	private double expected;
	private double delta;
	
	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		type = GrammaticalConstruction.EXISTENTIAL_THERE; //just a simple construction to fill constructor parameter
		collectionConstructions = new DocumentCollectionConstructionData(type, collection);
	}
	
	@Test 
	public void testCalculateData()
	{
		totalDocCount = 1;
		occurrencesInCollection = 1;
		numDocsWithOccurrences = 1;
		collectionConstructions.calculateData(totalDocCount, occurrencesInCollection, numDocsWithOccurrences);
		
		expected = 1.0;
		delta = .1;
		Assert.assertEquals(expected, collectionConstructions.getAverageCount(), delta);
		
		expected = .3;
		delta = .01;
		Assert.assertEquals(expected, collectionConstructions.getInvertedDocFrequency(), delta);
	}
	
	@Test
	public void testNoDocsCalculateData()
	{
		totalDocCount = 0;
		occurrencesInCollection = 0;
		numDocsWithOccurrences = 0;
		collectionConstructions.calculateData(totalDocCount, occurrencesInCollection, numDocsWithOccurrences);
		
		expected = 0;
		delta = 0;
		Assert.assertEquals(expected, collectionConstructions.getAverageCount(), delta);
		
		expected = 0;
		delta = 0;
		Assert.assertEquals(expected, collectionConstructions.getInvertedDocFrequency(), delta);
	}
}