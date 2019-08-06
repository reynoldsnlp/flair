package com.flair.server.raft;

import java.io.File;

import com.flair.server.utilities.ServerLogger;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;



public class Raft 
{

	private int wekaSalt;
	public Raft()
	{
		wekaSalt = 0;
	}
	
	//method used to be static
	public int ScoreText(String webText) throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception 
	{
		int returnValue = 0;
		Processor processor = new Processor(webText);
		processor.createPOSMap();
		processor.lemmatizeText();
		processor.createLemmaList();
		if (processor.getWordCount() > 0) 
		{
			processor.countSentences();
			processor.createFrequencies();
			if (processor.getFrequencies().size() > 0) 
			{
				processor.calcFreq95();
				processor.calcMean();
				processor.calcMedian();
				processor.calcAvgWordLen();
			}
		}
		String featureData = processor.getResult() + "1.0";
		String model = "model.arff";
		ServerLogger.get().info("featureData : \n " + featureData);
		Weka weka = new Weka(model);
		try 
		{
			weka.setRandomForest(weka.loadRandomForest("RandomForest.model"));
		} 
		catch (Exception e) 
		{
			ServerLogger.get().error(e.getMessage() + " Failed to set random forest model");
		}
		returnValue = weka.ScoreFeatures(featureData);
		return returnValue;
	}
	public int getSalt()
	{
		return wekaSalt;
	}

	public boolean modelExists(String model) 
	{ 
		File temp;
		boolean exists = false;
      	try
      	{
			String pathToResources = this.getClass().getClassLoader().getResource("").getPath();
			ServerLogger.get().info("Checking that " + pathToResources + model + " exists");
        	temp = new File(pathToResources + model);
          
         	exists = temp.exists();
          
      	} catch (Exception e)
      	{
			ServerLogger.get().error(e, e.getMessage());
		}
		ServerLogger.get().info(model + " exists : " + exists);
		return exists;
	}

	public void buildModel(String modelName) 
	{
		String pathToResources = this.getClass().getClassLoader().getResource("").getPath();

		if(!this.modelExists(modelName))
		{
			Weka randomForest = new Weka("model.arff");	
			try 
			{
				randomForest.setRandomForest(randomForest.buildRandomForestModel());
				weka.core.SerializationHelper.write(pathToResources + modelName, randomForest.getRandomForest());
				ServerLogger.get().info("Wrote " +  modelName + "   to the server resource folder");
			} 
			catch (Exception e) 
			{
				ServerLogger.get().error(e.getMessage() + " Failed to build random forest model");
			}
		}
	}

}
