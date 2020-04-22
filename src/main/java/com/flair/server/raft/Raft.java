/*
package com.flair.server.raft;

import java.io.File;

import com.flair.server.utilities.ServerLogger;
import com.flair.server.utilities.Weka;
//import edu.columbia.ccls.madamira.configuration.OutDoc;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;



public class Raft
{
	private int wekaSalt;

	public Raft () {
		wekaSalt = 0;
	}

	FeatureExtractor featureExtractor = null;
	Boolean we_have_run_score_text = false;
	//method used to be static
	public int ScoreText(String webText) throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception
	{
		we_have_run_score_text = true;
		int returnValue = 0;
		//featureExtractor = new FeatureExtractor(outDoc);
		featureExtractor.createPOSMap();
		//featureExtractor.lemmatizeText();
		featureExtractor.createLemmaList();
		if (featureExtractor.getWordCount() > 0)
		{
			featureExtractor.countSentences();
			featureExtractor.createFrequencies();
			if (featureExtractor.getFrequencies().size() > 0)
			{
				featureExtractor.calcFreq95();
				featureExtractor.calcMean();
				featureExtractor.calcMedian();
				featureExtractor.calcAvgWordLen();
			}
		}
		String featureData = featureExtractor.getResult() + "1.0";
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

	public FeatureExtractor getFeatureExtractor() { return featureExtractor; }
}
*/
