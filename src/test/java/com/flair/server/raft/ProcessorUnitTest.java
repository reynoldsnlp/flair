package com.flair.server.raft;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
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

    private ArrayList<Integer> createFrequencies(int size)
    {
        ArrayList<Integer> frequencies = new ArrayList<Integer>();
        for(int i = 0; i < size; i++)
        {
            frequencies.add(i);
        }
        return frequencies;
    }
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
        Assert.assertEquals("empty processor did not count 0 words, word count was " + emptyProcessor.getWordCount(), 
        0, emptyProcessor.getWordCount());
    }
    @Test 
    public void testShortCreateLemmaList()
    {
        shortProcessor.setMadaOutput(shortDocument);
        shortProcessor.createLemmaList();
        Assert.assertEquals("short processor did not count 1 word, word count was " + shortProcessor.getWordCount(), 
        1, shortProcessor.getWordCount());
    }
    @Test 
    public void testBadCreateLemmaList()
    {
        badProcessor.setMadaOutput(badDocument);
        badProcessor.createLemmaList();
        Assert.assertEquals("bad processor did not count 0 words, word count was " + badProcessor.getWordCount(), 
        0, badProcessor.getWordCount());
    }
    @Test 
    public void testNormalCreateLemmaList()
    {
        normalProcessor.setMadaOutput(normalDocument);
        normalProcessor.createLemmaList();
        Assert.assertEquals("Normal processor did not count 11 words, word count was " + normalProcessor.getWordCount(), 
        11, normalProcessor.getWordCount());
    }
    @Test 
    public void testNullCreatePosList()
    {
        normalProcessor.setPOSList(null);
        normalProcessor.createPOSMap();
        Assert.assertNotNull(normalProcessor.getPOSList());
        Assert.assertEquals(32, normalProcessor.getPOSList().size());
    }
    @Test 
    public void testOverwriteCreatePosList()
    {
        normalProcessor.setPOSList(new TreeMap<String, Integer>());
        normalProcessor.createPOSMap();
        Assert.assertEquals(32, normalProcessor.getPOSList().size());
    }
    @Test
    public void testAddKeyExistingMap()
    {
        normalProcessor.createPOSMap();
        normalProcessor.addKey("adv");
        normalProcessor.addKey("new");
        Assert.assertEquals("POSList size is " + normalProcessor.getPOSList().size(), 33, normalProcessor.getPOSList().size());
        Assert.assertEquals((Integer) 1, normalProcessor.getPOSList().get("adv"));

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
        Assert.assertEquals((Integer) 1, normalProcessor.getLemmaFreqListMap().get("lemma1"));
    }
    @Test
    public void testAddExistingToLemmaFreqListMap()
    {
        TreeMap<String, Integer> testLemmaFreqListMap = new TreeMap<>();
        testLemmaFreqListMap.put("lemma1", 0);
        normalProcessor.setLemmaFreqListMap(testLemmaFreqListMap);
        normalProcessor.addToLemmaFreqListMap("lemma1");
        normalProcessor.addToLemmaFreqListMap("lemma1");
        Assert.assertEquals(1, normalProcessor.getLemmaFreqListMap().size());
        Assert.assertEquals((Integer) 2, normalProcessor.getLemmaFreqListMap().get("lemma1"));
    }
    @Test
    public void testArabicMakeArabicOnly()
    {
        Assert.assertEquals(goodText, normalProcessor.makeArabicOnly(goodText));
    }
    @Test
    public void testEnglishMakeArabicOnly()
    {
        Assert.assertEquals("     ", normalProcessor.makeArabicOnly("none of this should be accepted")); //function keeps whitespace
    }
    @Test
    public void testMixedTextMakeArabicOnly()
    {
        Assert.assertEquals(goodText + "     " + goodText, normalProcessor.makeArabicOnly(goodText + "none of this should be accepted" + goodText)); //function keeps whitespace
    }
    @Test
    public void testIsEndPunct()
    {
        Assert.assertTrue(normalProcessor.isEndPunct('.'));
        Assert.assertTrue(normalProcessor.isEndPunct('!'));
        Assert.assertTrue(normalProcessor.isEndPunct('\u061f'));    //arabic question mark
        Assert.assertFalse(normalProcessor.isEndPunct('l'));
        Assert.assertFalse(normalProcessor.isEndPunct(','));
    }
    @Test
    public void testNormalCountSentences()
    {
        normalProcessor.countSentences();
        Assert.assertEquals(2, normalProcessor.getSentCount());
    }
    @Test
    public void testBadCountSentences()
    {
        badProcessor.countSentences();
        Assert.assertEquals(4, badProcessor.getSentCount());
    }
    @Test
    public void testEmptyCountSentences()
    {
        emptyProcessor.countSentences();
        Assert.assertEquals(0, badProcessor.getSentCount());
    }
    @Test 
    public void testReadFreqList()
    {
        Assert.assertEquals(174777, normalProcessor.readFreqList("freqList.txt").size());
    }
    @Test 
    public void testEmptyReadFreqList()
    {
        Assert.assertEquals(0, normalProcessor.readFreqList("").size());
    }
    @Test
    public void testBadFormatReadFreqList()
    {
        Assert.assertEquals(0, normalProcessor.readFreqList("badFreqList.txt").size());
    }
    @Test
    public void testBadFileNameReadFreqList()
    {
        Assert.assertEquals(0, normalProcessor.readFreqList("This is not a real file name").size());
        Assert.assertTrue(normalProcessor.isExceptionCaught());
    }
    @Test
    public void testIOExceptionReadFreqList() 
    {
        Assert.assertEquals(normalProcessor.readFreqList("/").size(), 0);
        Assert.assertTrue(normalProcessor.isExceptionCaught());
    } 
    @Test 
    public void test100FrequenciesCalcFreq95()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.calcFreq95();
        Assert.assertEquals(95, normalProcessor.getFreq95());
    }
    @Test 
    public void test0FrequenciesCalcFreq95()
    {
        ArrayList<Integer> frequencies = new ArrayList<Integer>();
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.calcFreq95();
        Assert.assertEquals(0, normalProcessor.getFreq95());
    }
    @Test 
    public void testCalcMean()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.calcMean();
        Assert.assertEquals(49.5, normalProcessor.getMean(), 0);
    }
    @Test
    public void testEvenMedian()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.calcMedian();
        Assert.assertEquals(49, normalProcessor.getMedian(), 0);
    }
    @Test
    public void testOddMedian()
    {
        ArrayList<Integer> frequencies = createFrequencies(99);
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.calcMedian();
        Assert.assertEquals(49, normalProcessor.getMedian(), 0);
    }
    @Test
    public void testNormalAvgWordLength()
    {        
        normalProcessor.setWordCount(11);
        normalProcessor.calcAvgWordLen();
        Assert.assertEquals(6, normalProcessor.getAvgWordLen(), .5);
    }
    @Test 
    public void testEmptyAvgWordLength()
    {
        emptyProcessor.setWordCount(0);
        emptyProcessor.calcAvgWordLen();
        Assert.assertEquals(0, emptyProcessor.getAvgWordLen(), 0);
    }
    @Test 
    public void testShortAvgWordLength()
    {
        shortProcessor.setWordCount(1);
        shortProcessor.calcAvgWordLen();
        Assert.assertEquals(1, shortProcessor.getAvgWordLen(), 0);
    }
    @Test 
    public void testBadAvgWordLength()
    {
        badProcessor.setWordCount(1);
        badProcessor.calcAvgWordLen();
        Assert.assertEquals(36, badProcessor.getAvgWordLen(), 0);
    }
    @Test 
    public void testEmptyResult()
    {
        ArrayList<Integer> frequencies = new ArrayList<Integer>();
        normalProcessor.setFrequencies(frequencies);
        String result = normalProcessor.getResult();
        Assert.assertEquals(0, result.length());
    }
    @Test 
    public void testNormalResult()
    {
        ArrayList<Integer> frequencies = createFrequencies(1);
        normalProcessor.setFrequencies(frequencies);
        normalProcessor.setWordCount(1);
        normalProcessor.setAvgSentLen(1);
        normalProcessor.setAvgWordLen(1);
        normalProcessor.setLexDiv(1);
        normalProcessor.setFreq95(1);
        normalProcessor.setMean(1);
        normalProcessor.setMedian(1);
        normalProcessor.createPOSMap();
        String result = normalProcessor.getResult();
        System.out.println(result);
        Assert.assertEquals("1.0,1.0,1.0,0.0,0.0,1,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,", result);
    }

}