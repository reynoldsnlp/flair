package com.flair.server.raft;

import java.io.File;
import java.io.Writer;

import com.flair.server.utilities.ServerLogger;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Raft {

	private int wekaSalt;
	public Raft(){
		wekaSalt = 0;
	}

	/*
	public static void main (String[] args) throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception {
		String webText = args[0];
		//run the line below in your terminal to run the server
		//java -Xmx2500m -Xms2500m -XX:NewRatio=3 -jar MADAMIRA-release-20170403-2.1.jar -s -msaonly
		
		//this is for testing, there is a test.txt file included in which you can put arabic text and then change the argument for start to webText
		//String fileName = "C:\\Users\\maste\\eclipse-workspace\\RAFT\\test.txt";
		//String webText = Processor.ReadFile(fileName);
		//System.out.println(webText);
		
		int score = ScoreText(webText);
		System.out.println(score);
		//BuildModel();
		//TestModel();
		//return score;
	}*/
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
		weka.resetInputFileName();
		returnValue = weka.ScoreFeatures(featureData);
		//weka.clearFiles();
		//cleanRaft(weka.getSalt());
		return returnValue;
	}
	public int getSalt(){
		return wekaSalt;
	}
	public void cleanRaft(int salt){
		File folder = new File("");
		File fList[] = folder.listFiles();
		// Searchs .lck
		for (int i = 0; i < fList.length; i++) {
			File file = fList[i];
			String fileStr = file.toString();
    		if (fileStr.contains("" + salt)) {
				File f = new File(fileStr);
				if(file.delete())
					ServerLogger.get().info(fileStr + " deleted");
				else	
				ServerLogger.get().error(fileStr + " not deleted ");
    		}
		}
	}
	/*
	public static void TestModel( ) {
		Weka weka = new Weka("model.arff");
		weka.TestModel();
	}
	
	public static void BuildModel() throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception {
		String corpusDir = "C:\\Users\\maste\\Desktop\\RAFT\\arabicScraperOutput";
		String modelDir = "C:\\Users\\maste\\Desktop\\RAFT";
		File dir = new File(corpusDir);
		File[] directoryListing = dir.listFiles();
		String corpusItemText = "";
		String difficulty = "";
		String body = "";
		String features = "";
		String trainingDataString = "";
		String writeLine = "";
		trainingDataString += (Weka.GetArffHeader());
		Processor processor;
		int count = 0;
		if (directoryListing != null) {
			for (File child : directoryListing) {
				features = "";
				difficulty = "";
				body = "";
				corpusItemText = Processor.ReadFileContents(child);
				difficulty = Processor.GetTagContents(corpusItemText, "DIFFICULTY");
				body = Processor.GetTagContents(corpusItemText, "BODY");
				if (body != "" && difficulty != "" && difficulty !="?") {
					difficulty = difficulty.replaceAll("/+", "");
					processor = new Processor(body);
					features = processor.getResult();
					if (features.length() > 0) {
	//					System.out.println(difficulty);
	//					System.out.println(body);
						System.out.println(child.getName());
						writeLine = features + difficulty + ".0";
						System.out.println(writeLine);
						trainingDataString += (writeLine + "\r\n");
					}
				}
				count++;
				// Do something with child
				if (count > 0) {
					//break;
				}
			}
		}
		System.out.println(trainingDataString);
		String modelFileName = modelDir + "\\model.arff";
		File modelFile = new File(modelFileName);
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(modelFile), "UTF8"));
			writer.write(trainingDataString);
			writer.close();
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("UNSUPPORTED ENCODING");
			e.printStackTrace();
		}
		catch(IOException e) {
			System.out.println("COULD NOT WRITE TO FILE ");
			e.printStackTrace();
		}
	}
	*/

}
