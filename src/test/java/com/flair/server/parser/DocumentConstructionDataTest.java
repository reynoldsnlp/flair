package com.flair.server.parser;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flair.shared.grammar.GrammaticalConstruction;

import junit.framework.Assert;

public class DocumentConstructionDataTest 
{
	private DocumentConstructionData documentConstructionData;
	@Mock 
	private AbstractDocument document;
	private GrammaticalConstruction type;
	private int start;
	private int end;
	private int expected;
	
	@Before 
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		type = GrammaticalConstruction.EXISTENTIAL_THERE;
		documentConstructionData = new DocumentConstructionData(type, document);
	}
	
	@Test 
	public void testAddOccurrence()
	{
		start = 0;
		end = 4;
		expected = 1;
		documentConstructionData.addOccurrence(start, end);
		Assert.assertEquals(expected, documentConstructionData.getFrequency());
	}
	
	@Test 
	public void testAddExistingOccurrence()
	{
		start = 0;
		end = 4;
		expected = 1;
		documentConstructionData.addOccurrence(start, end);
		documentConstructionData.addOccurrence(start, end);
		Assert.assertEquals(expected, documentConstructionData.getFrequency());
	}
}
