package com.flair.server.parser;

import static org.mockito.Mockito.when;
import static com.flair.shared.grammar.GrammaticalConstruction.SENTENCE_SIMPLE;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConstructionOccurenceTest 
{
	private ConstructionOccurrence constructionOccurrence;
	@Mock
	private TextSegment textSegment;
	
	@Before
	public void setUp()
	{
        MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void testConstructionOccurenceConstructor()
	{
		int start = 3;
		int end = 42;
		
		when(textSegment.getStart()).thenReturn(start);
		when(textSegment.getEnd()).thenReturn(end);
		
		constructionOccurrence = new ConstructionOccurrence(SENTENCE_SIMPLE, start, end);
		Assert.assertEquals(start, constructionOccurrence.getStart());
		Assert.assertEquals(end, constructionOccurrence.getEnd());
	}
}