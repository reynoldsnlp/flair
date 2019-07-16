package com.flair.server.unittests.raft;

import java.io.IOException;
import java.util.TreeMap;

import com.flair.server.raft.Processor;
import com.flair.server.utilities.FileReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.flair.shared.utilities.CustomFileReader;

import type.UnitTest;

@Category(UnitTest.class)

public class ProcessorUnitTest
{
    private Processor nullProcessor;
    private Processor emptyProcessor;
    private Processor shortProcessor;
    private Processor badProcessor;
    private Processor normalProcessor;
    private String goodText;
    private CustomFileReader fileReader;
    private Document emptyDocument;
    private Document shortDocument;
    private Document badDocument;
    private Document normalDocument;

    public Document setDocument(String fileName)
    {
        Document returnDocument;
        String outputString;
        try
        {
            outputString = fileReader.readFileToString(fileReader.getRelativePath(), fileName);
        }
        catch (IOException e)
        {
            outputString = "";
        }
        returnDocument = Jsoup.parse(outputString);
        return returnDocument;
    }

    @Before
    public void setUp()
    {
        fileReader = new CustomFileReader();
        goodText = "هذه جملة اختبار للمعالج العربي. سيقوم المعالج الطوافة بمعالجة هذه الجملة.";
        nullProcessor = new Processor((String) null);
        emptyProcessor = new Processor("");
        shortProcessor = new Processor("ص");
        badProcessor = new Processor("!(*!^@(^)(#{][]|}{;';::,.,./   ( ͡° ͜ʖ ͡°)");
        normalProcessor = new Processor(goodText);
        emptyDocument = setDocument("testFiles/emptyMadamiraOutput.xml");
        shortDocument = setDocument("testFiles/shortMadamiraOutput.xml");
        badDocument = setDocument("testFiles/badMadamiraOutput.xml");
        normalDocument = setDocument("testFiles/normalMadamiraOutput.xml");
    }
    @Test 
    public void testNullLemmatizeText()
    {
        nullProcessor.lemmatizeText();
    }
    @Test 
    public void testemptyLemmatizeText()
    {
        emptyProcessor.lemmatizeText();
    }
    @Test 
    public void testShortLemmatizeText()
    {
        shortProcessor.lemmatizeText();
    }
    @Test 
    public void testBadLemmatizeText()
    {
        badProcessor.lemmatizeText();
    }
    @Test 
    public void testNormalLemmatizeText()
    {
        normalProcessor.lemmatizeText();
    }
    @Test 
    public void testNullCreateLemmaList()
    {
        nullProcessor.setMadaOutput(null);
        nullProcessor.createLemmaList();
    }
    @Test 
    public void testemptyCreateLemmaList()
    {
        emptyProcessor.setMadaOutput(emptyDocument);
        emptyProcessor.createLemmaList();
        Assert.assertTrue("empty processor did not count 0 words, word count was " + emptyProcessor.getWordCount(), 
        emptyProcessor.getWordCount() == 0);
    }
    @Test 
    public void testShortCreateLemmaList()
    {
        shortProcessor.setMadaOutput(shortDocument);
        shortProcessor.createLemmaList();
        Assert.assertTrue("short processor did not count 1 word, word count was " + shortProcessor.getWordCount(), 
        shortProcessor.getWordCount() == 1);
    }
    @Test 
    public void testBadCreateLemmaList()
    {
        badProcessor.setMadaOutput(badDocument);
        badProcessor.createLemmaList();
        Assert.assertTrue("bad processor did not count 0 words, word count was " + badProcessor.getWordCount(), 
        badProcessor.getWordCount() == 0);
    }
    @Test 
    public void testNormalCreateLemmaList()
    {
        normalProcessor.setMadaOutput(normalDocument);
        normalProcessor.createLemmaList();
        Assert.assertTrue("Normal processor did not count 11 words, word count was " + normalProcessor.getWordCount(), 
        normalProcessor.getWordCount() == 11);
    }
    @Test 
    public void testNullCreatePosList()
    {
        nullProcessor.setPOSList(null);
        nullProcessor.createPOSMap();
        Assert.assertTrue(nullProcessor.getPOSList() != null);
        Assert.assertTrue(nullProcessor.getPOSList().size() == 32);
    }
    @Test 
    public void testOverwriteCreatePosList()
    {
        nullProcessor.setPOSList(new TreeMap<String, Integer>());
        nullProcessor.createPOSMap();
        Assert.assertTrue(nullProcessor.getPOSList().size() == 32);
    }
    /* @Test
    public void testGetTagContents()
    {

    } */

}