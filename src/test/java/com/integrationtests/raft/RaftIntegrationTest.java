/*
package com.integrationtests.raft;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import com.flair.server.raft.Raft;
import com.flair.server.utilities.CustomFileReader;
import com.ibm.icu.impl.Assert;

import edu.columbia.ccls.madamira.configuration.OutDoc;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import type.IntegrationTest;

@Category(IntegrationTest.class)
public class RaftIntegrationTest 
{
    private Raft raft;
    private String path;
    private String textSource;
    private String sourceName;
    private CustomFileReader fileReader;

    private void testDocument(Raft raft, String documentText, int testScore)
    {
        try
        {
            OutDoc outDoc = new MadamiraAPI().getInstance().run(documentText);
            int documentScore = raft.ScoreText(outDoc);
            System.out.println("text score == " + documentScore);
            System.out.flush();
            assertTrue("Make sure that MADAMIRA server is running", documentScore == testScore);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Caught FileNotFoundException on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
        catch(UnsupportedEncodingException e)
        {
            System.out.println("Caught UnsupportedEncodingException on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
        catch(IOException e)
        {
            System.out.println("Caught IOException on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("Caught ClassNotFoundException on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
        catch(InterruptedException e)
        {
            System.out.println("Caught InterruptedException on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
        catch(Exception e)
        {
            System.out.println("Caught Exception on " + e.getMessage() + " for level " + testScore + " test");
            Assert.fail(e);
        }
    }
    @Before
    public void setUp() 
    {
        raft = new Raft();
        sourceName = "testFiles/Arabic.pdf";
        fileReader = new CustomFileReader();
        path = fileReader.getRelativePath();
        try
        { 
            textSource = fileReader.readFileToString(path, sourceName);
        }
        catch(IOException ex) 
        {
            System.out.println("Caught IOException on " + ex.getMessage() + ", setting a default arabic text");
            textSource = "تقع مدينة عَطْبَرَة في ولاية نهر النيل في اتجاه الشمال، وتبعد عن العاصمة الخرطوم بحوالي 310 كيلو متر وعن مدينة الدامر حاضرة الولاية بحوالي 10 كيلو متر وعن ميناء بورتسودان في الشرق 611 كيلو متر، وجنوباً عن وادي حلفا بحوالي 474 كيلومتر. تقع المدينة على الضفة الشمالية لنهر عطبرة والضفة الشرقية لنهر النيل.";
        }

    }
    //@Test 
    public void testNullText()
    {
        try
        {
            raft.ScoreText(null);
        }
        catch (FileNotFoundException e) 
        {

        }
        catch (UnsupportedEncodingException e) 
        {

        }
        catch (IOException e) 
        {

        }
        catch (ClassNotFoundException e) 
        {
            
        }
        catch (InterruptedException e) 
        {

        }
        catch (Exception e) 
        {
            Assert.fail(e);
        }
    }
    @Test
    public void testScoreText() throws FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException,
            IOException, InterruptedException, Exception 
    {
        OutDoc outDoc = new MadamiraAPI().getInstance().run(textSource);
        double score = raft.ScoreText(outDoc);
        System.out.println("text score == " + score);
        System.out.flush();
        assertTrue("Make sure that MADAMIRA server is running", score > 0);
    }
    //@Test
    public void testLevel1Text() 
    {
        sourceName = "testFiles/arabicLevel1.txt";
        try
        { 
            textSource = fileReader.readFileToString(path, sourceName);
        }
        catch(IOException ex) 
        {
            System.out.println("Caught IOException on " + ex.getMessage() + ", setting a default arabic text");
            textSource = "";
        }
        testDocument(raft, textSource, 1);
    }
}*/
