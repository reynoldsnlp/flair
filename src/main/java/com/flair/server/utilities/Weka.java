package com.flair.server.utilities;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.File;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;

import com.flair.server.utilities.ServerLogger;
import java.lang.NullPointerException;

public class Weka implements Serializable {

	private static final long serialVersionUID = 1L;
	//private String inputFileName;
	private String characterEncoding;
	private String trainingDataFileName;
	private int score;
	private RandomForest rf;
	private Instances trainData;
	private static String arffHeader;
	
	public Weka(String trainingDataFileName) {
		this.trainingDataFileName = trainingDataFileName;
		score = 0;
		constructArffHeader();
		characterEncoding = "UTF8";
	}

	private void constructArffHeader()
	{
		arffHeader = "@relation arabicReadingDifficulty\r\n" + 
				"\r\n" + 
				"@attribute sentence_length NUMERIC\r\n" + 
				"@attribute word_length NUMERIC\r\n" + 
				"@attribute lexical_diversity NUMERIC\r\n" + 
				"@attribute lexical_complexity NUMERIC\r\n" + 
				"@attribute max_lexical_complexity NUMERIC\r\n" + 
				"@attribute p95 NUMERIC\r\n" + 
				"@attribute mean NUMERIC\r\n" + 
				"@attribute median NUMERIC\r\n" + 
				"@attribute noun NUMERIC\r\n" + 
				"@attribute pron NUMERIC\r\n" + 
				"@attribute verb NUMERIC\r\n" + 
				"@attribute prep NUMERIC\r\n" + 
				"@attribute part NUMERIC\r\n" + 
				"@attribute conj NUMERIC\r\n" + 
				"@attribute adv NUMERIC	\r\n" + 
				"@attribute adj NUMERIC\r\n" + 
				"@attribute difficulty {1.0, 2.0, 3.0, 4.0}\r\n" + 
				"\r\n" + 
				"@data \r\n";
	}

	public int ScoreFeatures(String featureData) 
	{
		try
		{
			//import the data to predict, this can include multiple sets of data
			//ServerLogger.get().info("inputFileName: " + inputFileName);
			Instances unlabeled = new Instances(new BufferedReader(new StringReader(getArffHeader() + featureData)));
			unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
			Instances labeled = new Instances(unlabeled);
			ServerLogger.get().info(unlabeled.instance(0).toString());

			ServerLogger.get().info("unlabeled instance at 0 " + unlabeled.instance(0).toString());
			double prediction = rf.classifyInstance(unlabeled.instance(0));		//this is where we use our random forest model
			ServerLogger.get().info("Prediction from RF model:" + prediction);
			labeled.instance(0).setClassValue(prediction);
			//figure out which index we're trying to predict (the last one for us)

 			this.score = ((int) prediction + 1);
			ServerLogger.get().info("Actual score from weka is " + this.score);
		}
		catch(IOException e)
		{
			ServerLogger.get().error(e, "In Weka.java, caught IOException " + e.getMessage() + ", featureData is incomplete");
		}
		catch(Exception e)
		{
			ServerLogger.get().error(e, "In Weka.java, caught Exception " + e.getMessage() + ", check to see if RandomForest loaded model");
		}
		return this.score;
	}
	
	public RandomForest buildRandomForestModel() 
	{
		String path = this.getClass().getClassLoader().getResource("").getPath();
		InputStream input;	
		BufferedReader reader;
		RandomForest rf;
		
		rf = createEmptyForest(100);
		
		try
		{
			ServerLogger.get().info("training data file name " + this.trainingDataFileName);
			input = new FileInputStream(new File(path + trainingDataFileName));
			reader = new BufferedReader(new InputStreamReader(input, characterEncoding));
			trainData = new Instances(reader);
			trainData.setClassIndex(trainData.numAttributes() - 1);
			reader.close();
			rf.buildClassifier(trainData);
		}
		catch(FileNotFoundException ex)
		{
			ServerLogger.get().error(ex, "Caught FileNotFoundException " + ex.getMessage());
		}
		catch(UnsupportedEncodingException ex)
		{
			ServerLogger.get().error(ex, "Caught UnsupportedEncodingException " + ex.getMessage());
		}
		catch(IOException ex)
		{
			ServerLogger.get().error(ex, "Caught IOException " + ex.getMessage());
		}
		catch(Exception ex)
		{
			ServerLogger.get().error(ex, "Caught Exception " + ex.getMessage());
		}
		return rf;
		
	}

	private RandomForest createEmptyForest(int size)
	{
		RandomForest rf = new RandomForest();
		rf.setNumTrees(100);
		return rf;
	}
	
	public RandomForest loadRandomForest(String model) 
	{
		try 
		{
			String pathToResources = this.getClass().getClassLoader().getResource("").getPath();
			ServerLogger.get().info(pathToResources + model);
			rf = (RandomForest) weka.core.SerializationHelper.read(pathToResources + model);
		} 
		catch (Exception ex) 
		{
			ServerLogger.get().error(ex.getMessage() + " Failed to load random forest model, building random forest model");
			rf = buildRandomForestModel();
		}
		return rf;
	}

	public RandomForest getRf() 
	{
		return rf;
	}

	public void setRf(RandomForest rf) 
	{
		this.rf = rf;
	}

	public static String getArffHeader() 
	{
		return arffHeader;
	}
	public void setRandomForest(RandomForest rf) 
	{
		this.rf = rf;
	}
	public RandomForest getRandomForest() 
	{
		return this.rf;
	}

	public String getTrainingDataFileName() 
	{
		return trainingDataFileName;
	}

	public void setTrainingDataFileName(String trainingDataFileName) 
	{
		this.trainingDataFileName = trainingDataFileName;
	}

	public String getCharacterEncoding() 
	{
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) 
	{
		this.characterEncoding = characterEncoding;
	}

	public Instances getTrainData() 
	{
		return trainData;
	}

	public void setTrainData(Instances trainData) 
	{
		this.trainData = trainData;
	}
}
