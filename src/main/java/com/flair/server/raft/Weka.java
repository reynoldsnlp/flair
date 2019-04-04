package com.flair.server.raft;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import java.util.Random;
import weka.classifiers.trees.RandomForest;

import com.flair.server.utilities.ServerLogger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.NullPointerException;

public class Weka {

	public static String GetArffHeader() {
		String arffHeader = "@relation arabicReadingDifficulty\r\n" + 
				"\r\n" + 
				//"@attribute word_count NUMERIC\r\n" + 
				//"@attribute sentence_count NUMERIC\r\n" + 
				"@attribute sentence_length NUMERIC\r\n" + 
				"@attribute lexical_diversity NUMERIC\r\n" + 
				"@attribute p95 NUMERIC\r\n" + 
				"@attribute mean NUMERIC\r\n" + 
				"@attribute median NUMERIC\r\n" + 
				"@attribute noun NUMERIC\r\n" + 
				"@attribute verb NUMERIC\r\n" + 
				"@attribute prep NUMERIC\r\n" + 
				"@attribute part NUMERIC\r\n" + 
				"@attribute conj NUMERIC\r\n" + 
				"@attribute adv NUMERIC	\r\n" + 
				"@attribute adj NUMERIC\r\n"	 + 
				"@attribute word_length NUMERIC\r\n" + 
				"@attribute difficulty {1.0, 2.0, 3.0, 4.0}\r\n" + 
				"\r\n" + 
				"@data \r\n";
		return arffHeader;
	}
	
	public Weka(String trainingDataFileName) {
		this.trainingDataFileName = trainingDataFileName;
		Random r = new Random();
		taskSalt = r.nextInt(10000000);		//gives a random number to salt our file names with
		inputFileName = "unlabeled" + taskSalt + ".arff";
		try {
			this.rf = buildRandomForestModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int taskSalt;
	String featureData;
	String inputFileName;
	String trainingDataFileName;
	int score = 0;
	RandomForest rf;
	Instances trainData;

	public int getSalt(){
		return taskSalt;
	}
	
	public int ScoreFeatures(String featureData) throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception {
		this.featureData = featureData;
		writeInputFile();
    
        //import the data to predict, this can include multiple sets of data
		System.out.println(inputFileName);
		Instances unlabeled = new Instances(new BufferedReader(new FileReader(inputFileName)));
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
        Instances labeled = new Instances(unlabeled);
        System.out.println(unlabeled.instance(0));
        
        double prediction = rf.classifyInstance(unlabeled.instance(0));		//this is where we use our random forest model
    	labeled.instance(0).setClassValue(prediction);
        //figure out which index we're trying to predict (the last one for us)
        
        this.score = ((int) prediction + 1);
        //go through each set of data in the file (currently just one) and predict it
        /*
        double prediction = 0.0;
        for (int i = 0; i < unlabeled.numInstances(); i++) {
        	prediction = rf.classifyInstance(unlabeled.instance(i));
        	labeled.instance(i).setClassValue(prediction);
        	//System.out.println(labeled.classAttribute().value((int) prediction));
        	score = ((int) prediction + 1);
        }
		*/
		//clearFiles();
		return this.score;
	}
	
	public void TestModel() {
        Evaluation evaluation;
		try {
			evaluation = new Evaluation(trainData);
	        int numFolds = 10;
	        evaluation.crossValidateModel(rf, trainData, numFolds, new Random(1));
	          
	         
	        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
	        System.out.println(evaluation.toClassDetailsString());
	        System.out.println("Results For Class -1- ");
	        System.out.println("Precision=  " + evaluation.precision(0));
	        System.out.println("Recall=  " + evaluation.recall(0));
	        System.out.println("F-measure=  " + evaluation.fMeasure(0));
	        System.out.println("Results For Class -2- ");
	        System.out.println("Precision=  " + evaluation.precision(1));
	        System.out.println("Recall=  " + evaluation.recall(1));
	        System.out.println("F-measure=  " + evaluation.fMeasure(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR CROSS VALIDATING");
		}
	}
	
	private RandomForest buildRandomForestModel()  throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception {
		//import the training data
		//ServletContext servletContextLoader = getServletContext();
		Path currentRelativePath = Paths.get("");
		String s = this.getClass().getClassLoader().getResource("").getPath();
		ServerLogger.get().info("Current relative path in Weka is: " + s);

		ClassLoader classLoader;
		InputStream input;	
		BufferedReader reader;


		
		try{
			//input = new FileInputStream(inputFile);
			//classLoader = getClass().getClassLoader();
			//input = classLoader.getResourceAsStream("/model.arff");	
			input = new FileInputStream(new File("/Users/michaelbriggs/Desktop/BYU-ODH/flair/src/main/java/com/flair/server/resources/" + trainingDataFileName));
			//InputStream inputCopy = classLoader.getResourceAsStream(trainingDataFileName);
			//ServerLogger.get().info("Model.arff contents : " + getStringFromInputStream(inputCopy));
			reader = new BufferedReader(new InputStreamReader(input, "UTF8"));
		}
		catch(NullPointerException ex){
			ServerLogger.get().error("Caught NullPointerException");
			ex.printStackTrace();
			ServerLogger.get().error(ex.getMessage());
			RandomForest rf = new RandomForest();
        	rf.setNumTrees(100);
			return rf;
		}
		catch(Exception ex){
			ServerLogger.get().error("Caught Exception");
			ex.printStackTrace();
			ServerLogger.get().error(ex.getMessage());
			RandomForest rf = new RandomForest();
        	rf.setNumTrees(100);
			return rf;
		}
		
		
        trainData = new Instances(reader);
        trainData.setClassIndex(trainData.numAttributes() - 1);
        reader.close();
        
        //train a Random Forest model on the data and return it
		RandomForest rf = new RandomForest();
        rf.setNumTrees(100);
        
        
        rf.buildClassifier(trainData);
		return rf;
		
		//return null;
	}
	
	private void writeInputFile() throws IOException {
		String wekaTop = GetArffHeader();
		File fWekaInput = new File(inputFileName);
		Writer writer = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(fWekaInput)));
		writer.write(wekaTop + featureData);
		writer.close();
	}	
	// convert InputStream to String
	private  String getStringFromInputStream(InputStream is) {	

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	public void clearFiles(){
		File arffFile = new File(inputFileName);
		if(arffFile.delete())
			ServerLogger.get().info(inputFileName + " deleted");
		else	
			ServerLogger.get().error(inputFileName + " not deleted ");

	}
}
