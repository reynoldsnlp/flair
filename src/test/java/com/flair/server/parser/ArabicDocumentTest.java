package com.flair.server.parser;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import com.flair.server.parser.ArabicDocument;
import com.flair.server.parser.SimpleDocumentSource;
import com.flair.server.raft.Raft;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;
import com.flair.server.parser.ConstructionDataCollection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import junit.framework.Assert;

public class ArabicDocumentTest
{



    private String sourceText = " موقعها الجغرافي.";
    private double readabilityScore;
    private ArabicDocumentReadabilityLevel arabicReadabilityLevel;
    private ConstructionDataCollection constructionData;
    private SimpleDocumentSource source; 
    @Mock
    private SimpleDocumentSource defaultConstructorSource; 
    @Mock
    private Raft raft;
    private ArabicDocument arabicDocument;
    private ArabicDocument defaultConstructorDocument;
    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        source = new SimpleDocumentSource(sourceText, Language.ARABIC);
        arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_1;
        readabilityScore = 3;
        arabicDocument = spy(new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData, raft));
        try 
        {
			when(raft.ScoreText(source.getSourceText())).thenReturn(1);
		} 
        catch (Exception e) 
        {
        	ServerLogger.get().error(e, "");
        }

        //arabicDocument = new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData);
    }
    
    @Test
    public void testCalculateReadabilityScore()
    {
        try
        {
            ServerLogger.get().info(arabicDocument.getDocumentSource().getSourceText());
            ServerLogger.get().info("arabicDocument.calculateReadabilityScore(sourceText) == " + arabicDocument.calculateReadabilityScore(source.getSourceText()));
            Assert.assertEquals(1.0, arabicDocument.calculateReadabilityScore(source.getSourceText()));
        }
        catch (Exception e)
        {
            ServerLogger.get().error(e, "Caught " + e.getMessage() + " in testCalculateReadabilityScore");
        }
    }
    
    @Test
    public void testExceptionCalculateReadabilityScore()
    {
        try
        {
            when(raft.ScoreText("")).thenThrow(new Exception());
            Assert.assertEquals(0.0, arabicDocument.calculateReadabilityScore(""));
        }
        catch(Exception e)
        {
            ServerLogger.get().error(e, "Caught exception " + e.getMessage() + " in ArabicDocumentTest");
        }
    }
    
    @Test 
    public void testCalculateFancyDocLength()
    {
        //Will normally return 0 because there are no grammatical constructions supported by arabic, 
        //this test will change when the code is updated (if it is ever updated to support specific constructions)
        ArabicDocument doc = new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData, new Raft());
        doc.calculateFancyDocLength();
        Assert.assertEquals(0.0, doc.getFancyLength());
    }
    
    @Test 
    public void testArabicConstructor()
    {
    	try
    	{
            defaultConstructorSource = new SimpleDocumentSource(sourceText, Language.ARABIC);
    		when(raft.ScoreText(defaultConstructorSource.getSourceText())).thenReturn(5);
            defaultConstructorDocument = spy(new ArabicDocument(defaultConstructorSource, raft));
            //when(defaultConstructorDocument.calculateReadabilityScore(defaultConstructorSource.getSourceText())).thenReturn((double) 5);
            Assert.assertEquals(5.0, defaultConstructorDocument.getReadabilityScore());
    	}
    	catch(Exception e)
    	{
    		ServerLogger.get().error(e, "");
    		Assert.fail();
    	}
    }
}
