package com.flair.server.raft;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

import type.UnitTest;

@Category(UnitTest.class)
public class RaftUnitTest
{
    private Raft raft;
    private String sourceName;
    private String path;
    private boolean exists;
    private int textScore;
    

    @Before
    public void setUp()
    {
        path = this.getClass().getClassLoader().getResource("").getPath();
        raft = new Raft();
    }
    @Test
    public void testModelExistsTrue() 
    {
        sourceName = "testFiles/Arabic.pdf";
        exists = false;
        exists = raft.modelExists(sourceName);
        Assert.assertTrue(exists);
    }
    @Test 
    public void testModelExistsFalse() 
    {
        sourceName = "fakeName";
        exists = true;
        exists = raft.modelExists(sourceName);
        Assert.assertFalse(exists);
    }
    /* @Test 
    public void testEmptyScoreText()
    {
        textScore = -1;
        try
        {
            textScore = raft.ScoreText("");
        }
        catch (UnsupportedEncodingException e)
        {
            Assert.fail("testEmptyScoreText() Failed with an UnsupportedEncodingException on " + e.getMessage());
        }
        catch (FileNotFoundException e) 
        {
            Assert.fail("testEmptyScoreText() Failed with an FileNotFoundException on " + e.getMessage());
        }
        catch (IOException e)
        {
            Assert.fail("testEmptyScoreText() Failed with an IOException on " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Assert.fail("testEmptyScoreText() Failed with an ClassNotFoundException on " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Assert.fail("testEmptyScoreText() Failed with an InterruptedException on " + e.getMessage());
        }
        catch (Exception e)
        {
            Assert.fail("testEmptyScoreText() Failed with a Exception on " + e.getMessage());
        }
        finally 
        {
            assertTrue("Raft failed to set default score in testEmptyScoreText(), score is " + textScore, textScore == 0);
        }
    } */
}