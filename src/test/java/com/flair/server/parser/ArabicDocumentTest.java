package com.flair.server.parser;

import static org.mockito.ArgumentMatchers.any;
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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockitoAnnotations;

import junit.framework.Assert;

//@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ArabicDocumentTest
{



    private String sourceText = " موقعها الجغرافي.";
    //@Mock
    private double readabilityScore;
    @Mock
    private ArabicDocumentReadabilityLevel arabicReadabilityLevel;
    @Mock
    private ConstructionDataCollection constructionData;
    @Mock
    private SimpleDocumentSource source; // = new SimpleDocumentSource(sourceText, Language.ARABIC);
    @Mock
    private Raft raft;// = new Raft();

    //@InjectMocks
    private ArabicDocument arabicDocument;// = new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData);

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        source = new SimpleDocumentSource(sourceText, Language.ARABIC);
        arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_1;
        readabilityScore = 3;
        arabicDocument = spy(new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData, raft));

        //arabicDocument = new ArabicDocument(source, readabilityScore, arabicReadabilityLevel, constructionData);
    }
    @Test
    public void testCalculateReadabilityScore()
    {
        try
        {
            ServerLogger.get().info(arabicDocument.getDocumentSource().getSourceText());
            when(raft.ScoreText(sourceText)).thenReturn(1);
            ServerLogger.get().info("arabicDocument.calculateReadabilityScore(sourceText) == " + arabicDocument.calculateReadabilityScore(sourceText));
            Assert.assertEquals(1.0, arabicDocument.calculateReadabilityScore(sourceText));
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
}
