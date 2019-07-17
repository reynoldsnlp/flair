package com.flair.server.unittests.raft;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.TreeMap;

import com.flair.server.raft.Processor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.flair.server.utilities.CustomFileReader;

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
        normalProcessor.setPOSList(null);
        normalProcessor.createPOSMap();
        Assert.assertTrue(normalProcessor.getPOSList() != null);
        Assert.assertTrue(normalProcessor.getPOSList().size() == 32);
    }
    @Test 
    public void testOverwriteCreatePosList()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.createPOSMap();
        Assert.assertTrue(normalProcessor.getPOSList().size() == 32);
    }
    @Test
    public void testAddKeyExistingMap()
    {
        normalProcessor.createPOSMap();
        normalProcessor.addKey("adv");
        normalProcessor.addKey("new");
        Assert.assertTrue("POSList size is " + normalProcessor.getPOSList().size(), normalProcessor.getPOSList().size() == 33);
        Assert.assertTrue(normalProcessor.getPOSList().get("adv") == 1);

    }
    @Test
    public void testAddKeyEmptyMap()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.addKey("adv");
        Assert.assertTrue("POSList size is " + normalProcessor.getPOSList().size(), normalProcessor.getPOSList().size() == 1);
        Assert.assertTrue(normalProcessor.getPOSList().get("adv") == 1);

    }
    @Test
    public void testAddKeyNullKey()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.addKey(null);
        Assert.assertTrue("POSList size is " + normalProcessor.getPOSList().size(), normalProcessor.getPOSList().size() == 0);
    }
    @Test
    public void testAddKeyNullMap()
    {
        normalProcessor.setPOSList(null);
        normalProcessor.addKey("adv");
        Assert.assertTrue(normalProcessor.getPOSList() == null);

    }
    @Test
    public void testEmptyAddToPosMap()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.addToPOSMap("adv");
        normalProcessor.addToPOSMap("conj");
        normalProcessor.addToPOSMap("abbrev");
        normalProcessor.addToPOSMap("bad");
        Assert.assertTrue(normalProcessor.getPOSList().size() == 3);
    }
    @Test
    public void testNullListAddToPosMap()
    {
        normalProcessor.setPOSList(null);
        normalProcessor.addToPOSMap("adv");
        normalProcessor.addToPOSMap("bad");
        Assert.assertTrue("POSList size is " + normalProcessor.getPOSList().size(), normalProcessor.getPOSList().size() == 32);
        Assert.assertTrue(normalProcessor.getPOSList().get("adv") == 1);
        Assert.assertFalse(normalProcessor.getPOSList().containsKey("bad"));
    }
    @Test
    public void testNullKeyAddToPosMap()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.addToPOSMap(null);
        Assert.assertTrue(normalProcessor.getPOSList().size() == 0);
    }
    @Test
    public void testNullAddToLemmaFreqListMap()
    {
        normalProcessor.addToLemmaFreqListMap(null);
        assertTrue(normalProcessor.getLemmaFreqListMap().size() == 0);
    }
    @Test
    public void testAddNewToLemmaFreqListMap()
    {
        TreeMap<String, Integer> testLemmaFreqListMap = new TreeMap<>();
        normalProcessor.setLemmaFreqListMap(testLemmaFreqListMap);
        normalProcessor.addToLemmaFreqListMap("lemma1");
        Assert.assertEquals(normalProcessor.getLemmaFreqListMap().size(), 1);
        Assert.assertEquals(normalProcessor.getLemmaFreqListMap().get("lemma1"), (Integer) 1);
    }
    @Test
    public void testAddExistingToLemmaFreqListMap()
    {
        TreeMap<String, Integer> testLemmaFreqListMap = new TreeMap<>();
        testLemmaFreqListMap.put("lemma1", 0);
        normalProcessor.setLemmaFreqListMap(testLemmaFreqListMap);
        normalProcessor.addToLemmaFreqListMap("lemma1");
        normalProcessor.addToLemmaFreqListMap("lemma1");
        Assert.assertEquals(normalProcessor.getLemmaFreqListMap().size(), 1);
        Assert.assertEquals(normalProcessor.getLemmaFreqListMap().get("lemma1"), (Integer) 2);
    }
    @Test
    public void testArabicMakeArabicOnly()
    {
        Assert.assertEquals(goodText, normalProcessor.makeArabicOnly(goodText));
    }


}