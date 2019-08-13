package com.flair.server.raft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.flair.server.raft.Weka;
import com.flair.server.utilities.ServerLogger;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class WekaUnitTest 
{
    private Weka weka;
    private RandomForest testForest;
    private String testModel;
    private int emptyForestDescription;

    private void generateTestForest()
    {
        String path = this.getClass().getClassLoader().getResource("").getPath();
		InputStream input;	
		BufferedReader reader;
		
		try
		{
			input = new FileInputStream(new File(path + testModel));
            reader = new BufferedReader(new InputStreamReader(input, "UTF8"));
            Instances trainData = new Instances(reader);
            trainData.setClassIndex(trainData.numAttributes() - 1);
            reader.close();
        
            //train a Random Forest model on the data and return it
            testForest.setNumTrees(100);
            testForest.buildClassifier(trainData);
		}
		catch(Exception ex)
		{
			ServerLogger.get().error(ex, "Caught Exception " + ex.getMessage());
		}
    }

    @Before
    public void setUp()
    {
        testForest = new RandomForest();
        testModel = "model.arff";
        weka = new Weka(testModel);
        emptyForestDescription = 27;
    }

    @Test
    public void testScoreFeatures()
    {
        generateTestForest();
        weka.setRf(testForest);
        Assert.assertEquals(1,weka.ScoreFeatures(
            "1.0,1.0,1.0,0.0,0.0,1,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0"));
    }
    @Test
    public void testIOExceptionScoreFeatures()
    {
        testForest = new RandomForest();
        testForest.setNumTrees(100);
        weka.setRf(testForest);
        Assert.assertEquals(0,weka.ScoreFeatures(
            "1.0,1.0,1.0,0.0,0.0,1,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,"));
    }
    @Test 
    public void testExceptionScoreFeatures()
    {
        testForest = new RandomForest();
        testForest.setNumTrees(100);
        weka.setRf(testForest);
        Assert.assertEquals(0,weka.ScoreFeatures(
            "1.0,1.0,1.0,0.0,0.0,1,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0"));
    }
    @Test 
    public void testBuildRandomForestModel()
    {
        Assert.assertTrue("RandomForest is empty",weka.buildRandomForestModel().toString().length() > emptyForestDescription);
    }
    @Test 
    public void testFileNotFoundExceptionBuildRandomForestModel()
    {
        weka.setTrainingDataFileName("");
        Assert.assertEquals("RandomForest is not empty", emptyForestDescription, weka.buildRandomForestModel().toString().length());
    }
    @Test 
    public void testIOExceptionBuildRandomForestModel()
    {
        weka.setTrainingDataFileName("freqList.txt");//just providing a file that isn't in the expected format
        Assert.assertEquals("RandomForest is not empty", emptyForestDescription, weka.buildRandomForestModel().toString().length());
    }
    @Test 
    public void testUnsupportedCharacterEncodingBuildRandomForestModel()
    {
        weka.setTrainingDataFileName(testModel);
        weka.setCharacterEncoding("Not a real encoding");
        Assert.assertEquals("RandomForest is not empty", emptyForestDescription, weka.buildRandomForestModel().toString().length());
    }
    @Test 
    public void testExceptionBuildRandomForestModel()
    {
        weka.setTrainingDataFileName("badModel.arff");
        Assert.assertEquals("RandomForest is not empty", emptyForestDescription, weka.buildRandomForestModel().toString().length());
    }
    @Test
    public void testLoadExistingRandomForestModel()
    {
        weka.setTrainingDataFileName("");
        Assert.assertTrue("RandomForest is empty",weka.loadRandomForest("RandomForest.model").toString().length() > emptyForestDescription);
    }
    @Test
    public void testLoadNonExistingRandomForestModel()
    {
        weka.setTrainingDataFileName("");
        Assert.assertEquals("RandomForest is not empty", emptyForestDescription, weka.loadRandomForest("RandomForest.fake").toString().length());
    }
}
