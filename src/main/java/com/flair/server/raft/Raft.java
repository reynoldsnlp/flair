package com.flair.server.raft;

import java.io.File;
import java.io.Writer;

import com.flair.server.utilities.ServerLogger;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;



public class Raft {

	private int wekaSalt;
	public Raft(){
		wekaSalt = 0;
	}
	
	//method used to be static
	public int ScoreText(String webText) throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception {
		int returnValue = 0;
		Processor processor = new Processor(webText);
		String featureData = processor.getResult() + "1.0";
		String model = "model.arff";
		
		//Path currentRelativePath = Paths.get("");
		//String s = this.getClass().getClassLoader().getResource("").getPath();
		//ServerLogger.get().info("Current relative path in Raft is: " + s);
		//ServerLogger.get().info("Model location -> " + model);

		//model = s + model;

		Weka weka = new Weka(model);	
		weka.setSalt(processor.getSalt());
		wekaSalt = weka.getSalt();
		try {
			weka.setRandomForest(weka.loadRandomForest("RandomForest.model"));
		} catch (Exception e) {
			ServerLogger.get().error(e.getMessage() + " Failed to set random forest model");
		}
		weka.resetInputFileName();
		returnValue = weka.ScoreFeatures(featureData);
		return returnValue;
	}
	public int getSalt(){
		return wekaSalt;
	}

	public boolean modelExists(String model) { 
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

}
