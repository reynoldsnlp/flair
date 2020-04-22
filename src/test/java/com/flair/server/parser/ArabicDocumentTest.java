/*
package com.flair.server.parser;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import com.flair.server.raft.Raft;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;

import edu.columbia.ccls.madamira.configuration.OutDoc;
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
    private SimpleDocumentSource defaultConstructorSource; 
    private SimpleDocumentSource emptySource;
    private SimpleDocumentSource notArabicSource;
    @Mock
    private Raft raft;
    private ArabicDocument arabicDocument;
    private ArabicDocument defaultConstructorDocument;		//used for constructor logic tests
    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        source = new SimpleDocumentSource(sourceText, Language.ARABIC);
        defaultConstructorSource = new SimpleDocumentSource(sourceText + "!" + sourceText, Language.ARABIC);
        emptySource = new SimpleDocumentSource(" ", Language.ARABIC);

        notArabicSource = new SimpleDocumentSource("This is not arabic text", Language.ENGLISH);
        arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_1;
        readabilityScore = 3;
        arabicDocument = spy(new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData, raft));
    }
    
    @Test
    public void testCalculateReadabilityScore()
    {
        try
        {
            OutDoc outDoc = MadamiraAPI.getInstance().run(sourceText);
			when(raft.ScoreText(outDoc)).thenReturn(1);
            Assert.assertEquals(1.0, arabicDocument.calculateReadabilityScore());
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
            OutDoc outDoc = new MadamiraAPI().getInstance().run("");
            when(raft.ScoreText(outDoc)).thenThrow(new Exception());
            Assert.assertEquals(0.0, arabicDocument.calculateReadabilityScore());
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

    private void testArabicConstructor(int level, SimpleDocumentSource source)
    {
    	try
    	{
    	    OutDoc outDoc = new MadamiraAPI().getInstance().run(source.getSourceText());
    		when(raft.ScoreText(outDoc)).thenReturn(level);
            defaultConstructorDocument = spy(new ArabicDocument(source, raft));
            Assert.assertEquals((double) level, defaultConstructorDocument.getReadabilityScore(), .1);
            
            if (level > 3)
            {
            	Assert.assertEquals(ArabicDocumentReadabilityLevel.LEVEL_4, defaultConstructorDocument.getArabicReadabilityLevel());
            }
            else if (level > 2)
            {
            	Assert.assertEquals(ArabicDocumentReadabilityLevel.LEVEL_3, defaultConstructorDocument.getArabicReadabilityLevel());
            }
            else if (level > 1)
            {
            	Assert.assertEquals(ArabicDocumentReadabilityLevel.LEVEL_2, defaultConstructorDocument.getArabicReadabilityLevel());
            }
            else if (level > 0)
            {
            	Assert.assertEquals(ArabicDocumentReadabilityLevel.LEVEL_1, defaultConstructorDocument.getArabicReadabilityLevel());
            }
    	}
    	catch(Exception e)
    	{
    		ServerLogger.get().error(e, "");
    		Assert.fail();
    	}
    }
    
    @Test 
    public void testArabicConstructorLevel4()
    {
    	testArabicConstructor(4, defaultConstructorSource);
    }
    
    @Test 
    public void testArabicConstructorLevel3()
    {
    	testArabicConstructor(3, defaultConstructorSource);
    }
    
    @Test 
    public void testArabicConstructorLevel2()
    {
    	testArabicConstructor(2, defaultConstructorSource);
    }
    
    @Test 
    public void testArabicConstructorLevel1()
    {
    	testArabicConstructor(1, defaultConstructorSource);
    }
    
    @Test
    public void testEmptyTextArabicConstructor()
    {
    	testArabicConstructor(-10, emptySource);
    }
    @Test
    public void testEnglishTextArabicConstructor()
    {
    	boolean caughtException = false;
    	try
    	{
    		defaultConstructorDocument = new ArabicDocument(notArabicSource);
    	}
    	catch(IllegalArgumentException e)
    	{
    		caughtException = true;
    	}
    	Assert.assertTrue(caughtException);
    }
}
*/
