package com.flair.server.raft;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.flair.server.utilities.CustomFileReader;

import type.UnitTest;

@Category(UnitTest.class)

public class FeatureExtractorUnitTest
{
    private OldFeatureExtractor nullFeatureExtractor;
    private OldFeatureExtractor emptyFeatureExtractor;
    private OldFeatureExtractor shortFeatureExtractor;
    private OldFeatureExtractor badFeatureExtractor;
    private OldFeatureExtractor normalFeatureExtractor;
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
        nullFeatureExtractor = new OldFeatureExtractor((String) null);
        emptyFeatureExtractor = new OldFeatureExtractor("");
        shortFeatureExtractor = new OldFeatureExtractor("ص");
        badFeatureExtractor = new OldFeatureExtractor("!(*!^@(^)(#{][]|}{;';::,.,./   ( ͡° ͜ʖ ͡°)");
        normalFeatureExtractor = new OldFeatureExtractor(goodText);
        emptyDocument = setDocument("testFiles/emptyMadamiraOutput.xml");
        shortDocument = setDocument("testFiles/shortMadamiraOutput.xml");
        badDocument = setDocument("testFiles/badMadamiraOutput.xml");
        normalDocument = setDocument("testFiles/normalMadamiraOutput.xml");
    }
    @Test 
    public void testNullLemmatizeText()
    {
        nullFeatureExtractor.lemmatizeText();
    }
    @Test 
    public void testemptyLemmatizeText()
    {
        emptyFeatureExtractor.lemmatizeText();
    }
    @Test 
    public void testShortLemmatizeText()
    {
        shortFeatureExtractor.lemmatizeText();
    }
    @Test 
    public void testBadLemmatizeText()
    {
        badFeatureExtractor.lemmatizeText();
    }
    @Test 
    public void testNormalLemmatizeText()
    {
        normalFeatureExtractor.lemmatizeText();
    }
    @Test 
    public void testNullCreateLemmaList()
    {
        nullFeatureExtractor.setMadaOutput(null);
        nullFeatureExtractor.createLemmaList();
    }
    @Test 
    public void testemptyCreateLemmaList()
    {
        emptyFeatureExtractor.setMadaOutput(emptyDocument);
        emptyFeatureExtractor.createLemmaList();
        Assert.assertEquals("empty processor did not count 0 words, word count was " + emptyFeatureExtractor.getWordCount(),
        0, emptyFeatureExtractor.getWordCount());
    }
    @Test 
    public void testShortCreateLemmaList()
    {
        shortFeatureExtractor.setMadaOutput(shortDocument);
        shortFeatureExtractor.createLemmaList();
        Assert.assertEquals("short processor did not count 1 word, word count was " + shortFeatureExtractor.getWordCount(),
        1, shortFeatureExtractor.getWordCount());
    }
    @Test 
    public void testBadCreateLemmaList()
    {
        badFeatureExtractor.setMadaOutput(badDocument);
        badFeatureExtractor.createLemmaList();
        Assert.assertEquals("bad processor did not count 0 words, word count was " + badFeatureExtractor.getWordCount(),
        0, badFeatureExtractor.getWordCount());
    }
    @Test 
    public void testNormalCreateLemmaList()
    {
        normalFeatureExtractor.setMadaOutput(normalDocument);
        normalFeatureExtractor.createLemmaList();
        Assert.assertEquals("Normal processor did not count 11 words, word count was " + normalFeatureExtractor.getWordCount(),
        11, normalFeatureExtractor.getWordCount());
    }
    @Test 
    public void testNullCreatePosList()
    {
        normalFeatureExtractor.setPOSList(null);
        normalFeatureExtractor.createPOSMap();
        Assert.assertNotNull(normalFeatureExtractor.getPOSList());
        Assert.assertEquals(32, normalFeatureExtractor.getPOSList().size());
    }
    @Test 
    public void testOverwriteCreatePosList()
    {
        normalFeatureExtractor.setPOSList(new TreeMap<String, Integer>());
        normalFeatureExtractor.createPOSMap();
        Assert.assertEquals(32, normalFeatureExtractor.getPOSList().size());
    }
    @Test
    public void testAddKeyExistingMap()
    {
        normalFeatureExtractor.createPOSMap();
        normalFeatureExtractor.addKey("adv");
        normalFeatureExtractor.addKey("new");
        Assert.assertEquals("POSList size is " + normalFeatureExtractor.getPOSList().size(), 33, normalFeatureExtractor.getPOSList().size());
        Assert.assertEquals((Integer) 1, normalFeatureExtractor.getPOSList().get("adv"));

    }
    @Test
    public void testAddKeyEmptyMap()
    {
        normalFeatureExtractor.setPOSList(new TreeMap<String, Integer>());
        normalFeatureExtractor.addKey("adv");
        Assert.assertTrue("POSList size is " + normalFeatureExtractor.getPOSList().size(), normalFeatureExtractor.getPOSList().size() == 1);
        Assert.assertTrue(normalFeatureExtractor.getPOSList().get("adv") == 1);

    }
    @Test
    public void testAddKeyNullKey()
    {
        normalFeatureExtractor.setPOSList(new TreeMap<String, Integer>());
        normalFeatureExtractor.addKey(null);
        Assert.assertTrue("POSList size is " + normalFeatureExtractor.getPOSList().size(), normalFeatureExtractor.getPOSList().size() == 0);
    }
    @Test
    public void testAddKeyNullMap()
    {
        normalFeatureExtractor.setPOSList(null);
        normalFeatureExtractor.addKey("adv");
        Assert.assertTrue(normalFeatureExtractor.getPOSList() == null);

    }
    @Test
    public void testEmptyAddToPosMap()
    {
        normalFeatureExtractor.setPOSList(new TreeMap<String, Integer>());
        normalFeatureExtractor.addToPOSMap("adv");
        normalFeatureExtractor.addToPOSMap("conj");
        normalFeatureExtractor.addToPOSMap("abbrev");
        normalFeatureExtractor.addToPOSMap("bad");
        Assert.assertTrue(normalFeatureExtractor.getPOSList().size() == 3);
    }
    @Test
    public void testNullListAddToPosMap()
    {
        normalFeatureExtractor.setPOSList(null);
        normalFeatureExtractor.addToPOSMap("adv");
        normalFeatureExtractor.addToPOSMap("bad");
        Assert.assertTrue("POSList size is " + normalFeatureExtractor.getPOSList().size(), normalFeatureExtractor.getPOSList().size() == 32);
        Assert.assertTrue(normalFeatureExtractor.getPOSList().get("adv") == 1);
        Assert.assertFalse(normalFeatureExtractor.getPOSList().containsKey("bad"));
    }
    @Test
    public void testNullKeyAddToPosMap()
    {
        normalFeatureExtractor.setPOSList(new TreeMap<String, Integer>());
        normalFeatureExtractor.addToPOSMap(null);
        Assert.assertTrue(normalFeatureExtractor.getPOSList().size() == 0);
    }
    @Test
    public void testNullAddToLemmaFreqListMap()
    {
        normalFeatureExtractor.addToLemmaFreqListMap(null);
        assertTrue(normalFeatureExtractor.getLemmaFreqListMap().size() == 0);
    }
    @Test
    public void testAddNewToLemmaFreqListMap()
    {
        TreeMap<String, Integer> testLemmaFreqListMap = new TreeMap<>();
        normalFeatureExtractor.setLemmaFreqListMap(testLemmaFreqListMap);
        normalFeatureExtractor.addToLemmaFreqListMap("lemma1");
        Assert.assertEquals(normalFeatureExtractor.getLemmaFreqListMap().size(), 1);
        Assert.assertEquals((Integer) 1, normalFeatureExtractor.getLemmaFreqListMap().get("lemma1"));
    }
    @Test
    public void testAddExistingToLemmaFreqListMap()
    {
        TreeMap<String, Integer> testLemmaFreqListMap = new TreeMap<>();
        testLemmaFreqListMap.put("lemma1", 0);
        normalFeatureExtractor.setLemmaFreqListMap(testLemmaFreqListMap);
        normalFeatureExtractor.addToLemmaFreqListMap("lemma1");
        normalFeatureExtractor.addToLemmaFreqListMap("lemma1");
        Assert.assertEquals(1, normalFeatureExtractor.getLemmaFreqListMap().size());
        Assert.assertEquals((Integer) 2, normalFeatureExtractor.getLemmaFreqListMap().get("lemma1"));
    }
    @Test
    public void testArabicMakeArabicOnly()
    {
        Assert.assertEquals(goodText, normalFeatureExtractor.makeArabicOnly(goodText));
    }
    @Test
    public void testEnglishMakeArabicOnly()
    {
        Assert.assertEquals("     ", normalFeatureExtractor.makeArabicOnly("none of this should be accepted")); //function keeps whitespace
    }
    @Test
    public void testMixedTextMakeArabicOnly()
    {
        Assert.assertEquals(goodText + "     " + goodText, normalFeatureExtractor.makeArabicOnly(goodText + "none of this should be accepted" + goodText)); //function keeps whitespace
    }
    @Test
    public void testIsEndPunct()
    {
        Assert.assertTrue(normalFeatureExtractor.isEndPunct('.'));
        Assert.assertTrue(normalFeatureExtractor.isEndPunct('!'));
        Assert.assertTrue(normalFeatureExtractor.isEndPunct('\u061f'));    //arabic question mark
        Assert.assertFalse(normalFeatureExtractor.isEndPunct('l'));
        Assert.assertFalse(normalFeatureExtractor.isEndPunct(','));
    }
    @Test
    public void testNormalCountSentences()
    {
        normalFeatureExtractor.countSentences();
        Assert.assertEquals(2, normalFeatureExtractor.getSentCount());
    }
    @Test
    public void testBadCountSentences()
    {
        badFeatureExtractor.countSentences();
        Assert.assertEquals(4, badFeatureExtractor.getSentCount());
    }
    @Test
    public void testEmptyCountSentences()
    {
        emptyFeatureExtractor.countSentences();
        Assert.assertEquals(0, badFeatureExtractor.getSentCount());
    }
    @Test 
    public void testReadFreqList()
    {
        Assert.assertEquals(174777, normalFeatureExtractor.readFreqList("freqList.txt").size());
    }
    @Test 
    public void testEmptyReadFreqList()
    {
        Assert.assertEquals(0, normalFeatureExtractor.readFreqList("").size());
    }
    @Test
    public void testBadFormatReadFreqList()
    {
        Assert.assertEquals(0, normalFeatureExtractor.readFreqList("badFreqList.txt").size());
    }
    @Test
    public void testBadFileNameReadFreqList()
    {
        Assert.assertEquals(0, normalFeatureExtractor.readFreqList("This is not a real file name").size());
        Assert.assertTrue(normalFeatureExtractor.isExceptionCaught());
    }
    @Test
    public void testIOExceptionReadFreqList() 
    {
        Assert.assertEquals(normalFeatureExtractor.readFreqList("/").size(), 0);
        Assert.assertTrue(normalFeatureExtractor.isExceptionCaught());
    } 
    @Test 
    public void test100FrequenciesCalcFreq95()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.calcFreq95();
        Assert.assertEquals(95, normalFeatureExtractor.getFreq95());
    }
    @Test 
    public void test0FrequenciesCalcFreq95()
    {
        ArrayList<Integer> frequencies = new ArrayList<Integer>();
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.calcFreq95();
        Assert.assertEquals(0, normalFeatureExtractor.getFreq95());
    }
    @Test 
    public void testCalcMean()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.calcMean();
        Assert.assertEquals(49.5, normalFeatureExtractor.getMean(), 0);
    }
    @Test
    public void testEvenMedian()
    {
        ArrayList<Integer> frequencies = createFrequencies(100);
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.calcMedian();
        Assert.assertEquals(49, normalFeatureExtractor.getMedian(), 0);
    }
    @Test
    public void testOddMedian()
    {
        ArrayList<Integer> frequencies = createFrequencies(99);
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.calcMedian();
        Assert.assertEquals(49, normalFeatureExtractor.getMedian(), 0);
    }
    @Test
    public void testNormalAvgWordLength()
    {        
        normalFeatureExtractor.setWordCount(11);
        normalFeatureExtractor.calcAvgWordLen();
        Assert.assertEquals(6, normalFeatureExtractor.getAvgWordLen(), .5);
    }
    @Test 
    public void testEmptyAvgWordLength()
    {
        emptyFeatureExtractor.setWordCount(0);
        emptyFeatureExtractor.calcAvgWordLen();
        Assert.assertEquals(0, emptyFeatureExtractor.getAvgWordLen(), 0);
    }
    @Test 
    public void testShortAvgWordLength()
    {
        shortFeatureExtractor.setWordCount(1);
        shortFeatureExtractor.calcAvgWordLen();
        Assert.assertEquals(1, shortFeatureExtractor.getAvgWordLen(), 0);
    }
    @Test 
    public void testBadAvgWordLength()
    {
        badFeatureExtractor.setWordCount(1);
        badFeatureExtractor.calcAvgWordLen();
        Assert.assertEquals(36, badFeatureExtractor.getAvgWordLen(), 0);
    }
    @Test 
    public void testEmptyResult()
    {
        ArrayList<Integer> frequencies = new ArrayList<Integer>();
        normalFeatureExtractor.setFrequencies(frequencies);
        String result = normalFeatureExtractor.getResult();
        Assert.assertEquals(0, result.length());
    }
    @Test 
    public void testNormalResult()
    {
        ArrayList<Integer> frequencies = createFrequencies(1);
        normalFeatureExtractor.setFrequencies(frequencies);
        normalFeatureExtractor.setWordCount(1);
        normalFeatureExtractor.setAvgSentLen(1);
        normalFeatureExtractor.setAvgWordLen(1);
        normalFeatureExtractor.setLexDiv(1);
        normalFeatureExtractor.setFreq95(1);
        normalFeatureExtractor.setMean(1);
        normalFeatureExtractor.setMedian(1);
        normalFeatureExtractor.createPOSMap();
        String result = normalFeatureExtractor.getResult();
        System.out.println(result);
        Assert.assertEquals("1.0,1.0,1.0,0.0,0.0,1,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,", result);
    }

}