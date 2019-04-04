package com.flair.server.raft;


import java.io.File;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.flair.server.utilities.ServerLogger;

import java.util.Map;
import java.util.Random;

import org.jsoup.*;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Processor {
	
	public Processor(String webText) {
		Random r = new Random();
		taskSalt = r.nextInt(10000000);		//gives a random number to salt our file names with
		body = webText;
		lemmatizeText();
		createLemmaList();
		if (wordCount > 0) {
			countSentences();
			createFrequencies();
			if (frequencies.size() > 0) {
				calcFreq95();
				calcMean();
				calcMedian();
				calcAvgWordLen();
			}
		}
	}
	
	private Document madaOutput;
	private String body;
	private TreeMap<String, Integer> lemmaFreqListMap = new TreeMap<>(); // maps a string "LEMMA:::POS" to its frequency within the text
	private TreeMap<String, Integer> POSList = createPOSMap(); //keeps a count for each POS tag
	private TreeMap<String, Integer> freqListMap; // Arabic frequency list
	private ArrayList<Integer> frequencies = new ArrayList<>();
	private String input;
	private String output;

	private int wordCount = 0; //number of tokens in the body
	private int sentCount = 0; //number of sentences in the body
	private double avgSentLen; //average sentence length
	private double lexDiv; //lexical diversity (# lemmas/ # of words)
	private int freq95 = 0; //frequency of last word of the 95th percentile
	private double mean;
	private double median;
	private double avgWordLen;
	private int taskSalt;
	
	public static String GetTagContents(String text, String tagName) {
		String contents = "";
		Document processedText;
		processedText = Jsoup.parse(text, "UTF-8");
		try {
			Element contentsElement = processedText.select(tagName).first();
			contents = contentsElement.ownText();
		} 
		catch(Exception e) {
			contents = "";
		}
		return contents;
	}
	
	public static String ReadFileContents(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF8"));
		    
			String str;
			while((str = reader.readLine()) != null) {
					sb.append(str + " ");				}
			
			reader.close();
			}
		catch(UnsupportedEncodingException e) {
			System.out.println("UNSUPPORTED ENCODING - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(FileNotFoundException e ) {
			System.out.println("FILE NOT FOUND - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
		
	}
	
	public static String ReadFile(String fileName) {
		File file = new File(fileName);
		String contents = ReadFileContents(file);
		return contents;
	}
	
	private String readResourceFile(String fileName) {
		StringBuilder sb = new StringBuilder();
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream input = classLoader.getResourceAsStream(fileName);
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF8"));
		    
			String str;
			while((str = reader.readLine()) != null) {
				sb.append(str + "\n");
			}
			
			reader.close();
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("UNSUPPORTED ENCODING - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(FileNotFoundException e ) {
			System.out.println("FILE NOT FOUND - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/*
	 *Creates XML input string for Madamira out of the body, saves it to madamiraInput, 
	 *sends it off to be lemmatized and saves output to madamiraOutput.
	 */
	private void lemmatizeText() {
		String xmlTop = readResourceFile("/madamira_input_top.txt");
		String xmlBottom = readResourceFile("/madamira_input_bottom.txt");
		input = "mada_input" + taskSalt + ".txt";
		output = "mada_output" + taskSalt + ".txt";
		try {
			File fMadaInput = new File(input);
			Writer writer = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(fMadaInput), "UTF8"));
			writer.write(xmlTop + "\n\n" + body + "\n\n" + xmlBottom);
			writer.close();
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("UNSUPPORTED ENCODING - WRITING MADAMIRA INPUT");
			e.printStackTrace();
		}
		catch(IOException e) {
			System.out.println("COULD NOT WRITE TO FILE - WRITING MADAMIRA INPUT");
			e.printStackTrace();
		}
		
		Madamira.lemmatize(8223, "http://localhost:", input, output);
		madaOutput = Jsoup.parse(ReadFile(output));
	}
	
	/*
	 * Uses JSOUP to to extract words, lemmas and pos tags from the Madamira output and then
	 * assembles TreeMap lemmas and TreeMap lemmaFreqListMap. Calculates wordCount and lexDiv.
	 */
	private void createLemmaList() {
		Elements words = madaOutput.getElementsByTag("word");
		for(Element word : words) {
			String l = new String(); // lemma (value)
			Elements svm_predictions = word.getElementsByTag("svm_prediction");
			for (Element svm : svm_predictions) {
				Elements morphs = svm.getElementsByAttribute("lemma");
				for(Element morph : morphs) {
					Attributes morphAttributes = morph.attributes();
					String pos = morphAttributes.get("pos");
					l = makeArabicOnly(morphAttributes.get("lemma"));
					l = normalize(l);
					addToPOSMap(pos);	
					addToLemmaFreqListMap(l + ":::" + pos);
				}
			}
		}
		wordCount = words.size();
		lexDiv = (double) lemmaFreqListMap.size() / (double) wordCount;
	}
	
	private TreeMap<String, Integer> createPOSMap() {
		TreeMap<String, Integer> map = new TreeMap<>();
		map.put("noun", 0);
		map.put("verb", 0);
		map.put("prep", 0);
		map.put("part", 0);
		map.put("conj", 0);
		map.put("adv", 0);
		map.put("adj", 0);
		
		return map;
	}
	
	private void addToPOSMap(String key) {
		if (key.charAt(0) == 'n')
			POSList.put("noun", POSList.get("noun") + 1);
		else if (key.charAt(0) == 'v')
			POSList.put("verb", POSList.get("verb") + 1);
		else if (key.charAt(0) == 'c')
			POSList.put("conj", POSList.get("conj") + 1);
		else if (key.charAt(2) == 'e')
			POSList.put("prep", POSList.get("prep") + 1);
		else if (key.charAt(2) == 'v')
			POSList.put("adv", POSList.get("adv") + 1);
		else if (key.charAt(2) == 'j')
			POSList.put("adj", POSList.get("adj") + 1);
		else if (key.charAt(3) == 't')
			POSList.put("part", POSList.get("part") + 1);
	}
	
	//Used to create running totals in FreqListMap
	private void addToLemmaFreqListMap(String key) {
		if(!lemmaFreqListMap.containsKey(key)) 
			lemmaFreqListMap.put(key, 1);
		else 
			lemmaFreqListMap.put(key, lemmaFreqListMap.get(key) + 1);
	}
	
	//Strips diacritics from arabic text
	private String normalize(String input) {
		//Remove honorific sign
        input=input.replaceAll("\u0610", "");//ARABIC SIGN SALLALLAHOU ALAYHE WA SALLAM
        input=input.replaceAll("\u0611", "");//ARABIC SIGN ALAYHE ASSALLAM
        input=input.replaceAll("\u0612", "");//ARABIC SIGN RAHMATULLAH ALAYHE
        input=input.replaceAll("\u0613", "");//ARABIC SIGN RADI ALLAHOU ANHU
        input=input.replaceAll("\u0614", "");//ARABIC SIGN TAKHALLUS

        //Remove koranic anotation
        input=input.replaceAll("\u0615", "");//ARABIC SMALL HIGH TAH
        input=input.replaceAll("\u0616", "");//ARABIC SMALL HIGH LIGATURE ALEF WITH LAM WITH YEH
        input=input.replaceAll("\u0617", "");//ARABIC SMALL HIGH ZAIN
        input=input.replaceAll("\u0618", "");//ARABIC SMALL FATHA
        input=input.replaceAll("\u0619", "");//ARABIC SMALL DAMMA
        input=input.replaceAll("\u061A", "");//ARABIC SMALL KASRA
        input=input.replaceAll("\u06D6", "");//ARABIC SMALL HIGH LIGATURE SAD WITH LAM WITH ALEF MAKSURA
        input=input.replaceAll("\u06D7", "");//ARABIC SMALL HIGH LIGATURE QAF WITH LAM WITH ALEF MAKSURA
        input=input.replaceAll("\u06D8", "");//ARABIC SMALL HIGH MEEM INITIAL FORM
        input=input.replaceAll("\u06D9", "");//ARABIC SMALL HIGH LAM ALEF
        input=input.replaceAll("\u06DA", "");//ARABIC SMALL HIGH JEEM
        input=input.replaceAll("\u06DB", "");//ARABIC SMALL HIGH THREE DOTS
        input=input.replaceAll("\u06DC", "");//ARABIC SMALL HIGH SEEN
        input=input.replaceAll("\u06DD", "");//ARABIC END OF AYAH
        input=input.replaceAll("\u06DE", "");//ARABIC START OF RUB EL HIZB
        input=input.replaceAll("\u06DF", "");//ARABIC SMALL HIGH ROUNDED ZERO
        input=input.replaceAll("\u06E0", "");//ARABIC SMALL HIGH UPRIGHT RECTANGULAR ZERO
        input=input.replaceAll("\u06E1", "");//ARABIC SMALL HIGH DOTLESS HEAD OF KHAH
        input=input.replaceAll("\u06E2", "");//ARABIC SMALL HIGH MEEM ISOLATED FORM
        input=input.replaceAll("\u06E3", "");//ARABIC SMALL LOW SEEN
        input=input.replaceAll("\u06E4", "");//ARABIC SMALL HIGH MADDA
        input=input.replaceAll("\u06E5", "");//ARABIC SMALL WAW
        input=input.replaceAll("\u06E6", "");//ARABIC SMALL YEH
        input=input.replaceAll("\u06E7", "");//ARABIC SMALL HIGH YEH
        input=input.replaceAll("\u06E8", "");//ARABIC SMALL HIGH NOON
        input=input.replaceAll("\u06E9", "");//ARABIC PLACE OF SAJDAH
        input=input.replaceAll("\u06EA", "");//ARABIC EMPTY CENTRE LOW STOP
        input=input.replaceAll("\u06EB", "");//ARABIC EMPTY CENTRE HIGH STOP
        input=input.replaceAll("\u06EC", "");//ARABIC ROUNDED HIGH STOP WITH FILLED CENTRE
        input=input.replaceAll("\u06ED", "");//ARABIC SMALL LOW MEEM

        //Remove tatweel
        input=input.replaceAll("\u0640", "");

        //Remove tashkeel
        input=input.replaceAll("\u064B", "");//ARABIC FATHATAN
        input=input.replaceAll("\u064C", "");//ARABIC DAMMATAN
        input=input.replaceAll("\u064D", "");//ARABIC KASRATAN
        input=input.replaceAll("\u064E", "");//ARABIC FATHA
        input=input.replaceAll("\u064F", "");//ARABIC DAMMA
        input=input.replaceAll("\u0650", "");//ARABIC KASRA
        input=input.replaceAll("\u0651", "");//ARABIC SHADDA
        input=input.replaceAll("\u0652", "");//ARABIC SUKUN
        input=input.replaceAll("\u0653", "");//ARABIC MADDAH ABOVE
        input=input.replaceAll("\u0654", "");//ARABIC HAMZA ABOVE
        input=input.replaceAll("\u0655", "");//ARABIC HAMZA BELOW
        input=input.replaceAll("\u0656", "");//ARABIC SUBSCRIPT ALEF
        input=input.replaceAll("\u0657", "");//ARABIC INVERTED DAMMA
        input=input.replaceAll("\u0658", "");//ARABIC MARK NOON GHUNNA
        input=input.replaceAll("\u0659", "");//ARABIC ZWARAKAY
        input=input.replaceAll("\u065A", "");//ARABIC VOWEL SIGN SMALL V ABOVE
        input=input.replaceAll("\u065B", "");//ARABIC VOWEL SIGN INVERTED SMALL V ABOVE
        input=input.replaceAll("\u065C", "");//ARABIC VOWEL SIGN DOT BELOW
        input=input.replaceAll("\u065D", "");//ARABIC REVERSED DAMMA
        input=input.replaceAll("\u065E", "");//ARABIC FATHA WITH TWO DOTS
        input=input.replaceAll("\u065F", "");//ARABIC WAVY HAMZA BELOW
        input=input.replaceAll("\u0670", "");//ARABIC LETTER SUPERSCRIPT ALEF

        return input;
	}
	
	//extracts only the arabic text from the "lemma" given by Madamira
	private String makeArabicOnly(String word) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < word.length(); i++) {
			if (word.charAt(i) >= 1536 && word.charAt(i) <= 1791) {
				sb.append(word.charAt(i));
			}
		}
		return sb.toString();
	}
	
	//calculates the number of sentences and the average sentence length
	private void countSentences() {
		//Counts a sentence at new lines and after punctuation
		for(int i = 1; i < body.length(); i++) {
			char c = body.charAt(i);
			char p = body.charAt(i - 1);
			if(isEndPunct(c) && !isEndPunct(p) || 
				c == '\n' && p != '\n' && !isEndPunct(p) || 
				i == body.length() - 1 && p != '\n' && !isEndPunct(p))
					sentCount++;
		}
			
		avgSentLen = (double) wordCount / (double) sentCount;
	}
	
	private boolean isEndPunct(char c) {
		if(c == '.' || c == '!' || c == '\u061f') 
			return true;
		return false;
	}
	
	private void createFrequencies() {
		freqListMap = readFreqList();
		for (Map.Entry<String, Integer> entry : lemmaFreqListMap.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				if (freqListMap.get(entry.getKey()) != null)
					frequencies.add(freqListMap.get(entry.getKey()));
			}
		}
		
		frequencies.sort(null);
	}
	
	/*
	 * helper for createFrequencies()
	 * reads freqList in from file and returns it
	 */
	private TreeMap<String, Integer> readFreqList() {
		TreeMap<String, Integer> freqListMap = new TreeMap<>();
		try {
				ClassLoader classLoader = getClass().getClassLoader();
				InputStream input = classLoader.getResourceAsStream("freqList.txt");
			
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF8"));
			
				String str;
				while((str = reader.readLine()) != null) {
					String regex = ":::";
					String[] data = str.split(regex);
					freqListMap.put(data[1] + ":::" + data[2], Integer.parseInt(data[0]));
			}
			
			reader.close();
		}
		catch(UnsupportedEncodingException e) {
			System.out.println("UNSUPPORTED ENCODING - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(FileNotFoundException e ) {
			System.out.println("FILE NOT FOUND - READING DOCUMENT");
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return freqListMap;
	}
	
	/*
	 * Sorts the entries in the lemmaFreqListMap into a sorted List to find the 95th percentile,
	 * Then finds that lemma's frequency in the freqList
	 */
	private void calcFreq95() {
		int index95 = (int) (frequencies.size() * .95);
		if (index95 > 0) {
			freq95 = frequencies.get(index95);
		} else {
			freq95 = 0;
		}
	}
	
	// calculates the mean frequency
	private void calcMean() {
		int total = 0;
		for (int frequency : frequencies)
			total += frequency;
		mean = (double) total / (double) frequencies.size();
	}
	
	// calculates the median frequency
	private void calcMedian( ) {
		if (frequencies.size() % 2 == 0) {
			int middleRight = frequencies.size() / 2;
			int middleLeft = middleRight - 1;
			median = (frequencies.get(middleRight) + frequencies.get(middleLeft)) / 2;
		}
		else {
			int middle = frequencies.size() / 2;
			median = Math.floor(frequencies.get(middle));
		}
	}
	
	private void calcAvgWordLen() {
		int chars = 0;
		for (int i = 0; i < body.length(); i++) {
			if (!Character.isWhitespace(body.charAt(i)))
				chars++;
		}
		avgWordLen = (double) chars / (double) wordCount;
	}
	
	//returns a string result of all the desired stats
	public String getResult() {
		StringBuilder sb = new StringBuilder();
		if (wordCount > 0 && frequencies.size() > 0) {
			//sb.append((double) wordCount);
			//sb.append(",");
			//sb.append((double) sentCount);
			//sb.append(",");
			sb.append(avgSentLen);
			sb.append(",");
			sb.append(lexDiv);
			sb.append(",");
			sb.append(freq95);
			sb.append(",");
			sb.append(mean);
			sb.append(",");
			sb.append(median);
			sb.append(",");
			sb.append((double) POSList.get("noun") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("verb") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("prep") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("part") / (double) wordCount); 
			sb.append(",");
			sb.append((double) POSList.get("conj") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("adv") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("adj") / (double) wordCount);
			sb.append(",");
			sb.append(avgWordLen);
			sb.append(",");
		}
		clearFiles();
		//System.out.println(sb.toString());
		return sb.toString();
	}

	public void clearFiles(){
		File inputFile = new File(input);
		File outputFile = new File(output);
		if(inputFile.delete())
			ServerLogger.get().info(input + " deleted");
		else	
			ServerLogger.get().error(input + " not deleted ");

		if(outputFile.delete())
			ServerLogger.get().info(output + " deleted");
		else	
			ServerLogger.get().error(output + " not deleted ");
	}
	
}
