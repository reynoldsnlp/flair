package com.flair.server.raft;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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
		webText = webText.trim().replaceAll("&", "+");
		webText = webText.trim().replaceAll("(\\s)+", "$1");
		body = webText;
		Random r = new Random();
		taskSalt = r.nextInt(10000000);		//gives a random number to salt our file names with
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

	private int wordCount = 0; //number of words in the body
	private int tokenCount = 0; //number of tokens in the body
	private int sentCount = 0; //number of sentences in the body
	private double avgSentLen; //average sentence length
	private double lexDiv; //lexical diversity (# lemmas/ # of words)
	private double lemmaComplexity; //lexical complexity (# tokens / # words)
	private double maxLemmaComplexity; //lexical complexity (# tokens / # words)
	private int freq95 = 0; //frequency of last word of the 95th percentile
	private double mean;
	private double median;
	private double avgWordLen;
	private int taskSalt;
	
	private String madamiraTop = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<madamira_input xmlns=\"urn:edu.columbia.ccls.madamira.configuration:0.1\">\r\n" + 
			"	<madamira_configuration>\r\n" + 
			"        <preprocessing sentence_ids=\"false\" separate_punct=\"true\" input_encoding=\"UTF8\"/>\r\n" + 
			"        <overall_vars output_encoding=\"UTF8\" dialect=\"MSA\" output_analyses=\"TOP\" morph_backoff=\"NONE\"/>\r\n" + 
			"        <requested_output>\r\n" + 
			"            <req_variable name=\"PREPROCESSED\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"STEM\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"GLOSS\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"LEMMA\" value=\"true\" />\r\n" + 
			"            <req_variable name=\"DIAC\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"ASP\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"CAS\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"ENC0\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"ENC1\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"ENC2\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"GEN\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"MOD\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"NUM\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"PER\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"POS\" value=\"true\" />\r\n" + 
			"            <req_variable name=\"PRC0\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"PRC1\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"PRC2\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"PRC3\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"STT\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"VOX\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"BW\" value=\"false\" />\r\n" + 
			"            <req_variable name=\"SOURCE\" value=\"false\" />\r\n" + 
			"			<req_variable name=\"NER\" value=\"false\" />\r\n" + 
			"			<req_variable name=\"BPC\" value=\"false\" />\r\n" + 
			"        </requested_output>\r\n" + 
			"	</madamira_configuration>\r\n" + 
			"    <in_doc id=\"ExampleDocument\">";
	
	private String madamiraBottom = "</in_doc>\r\n" + 
			"</madamira_input>";
	
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

	public int getSalt(){
		return taskSalt;
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
	
	
	/** 
	 *Creates XML input string for Madamira out of the body, saves it to madamiraInput, 
	 *sends it off to be lemmatized and saves output to madamiraOutput.
	 */
	private void lemmatizeText() {
		input = "/tmp/mada_input" + taskSalt + ".txt";
		output = "/tmp/mada_output" + taskSalt + ".txt";
		InputStream inputStream;
		StringBuilder inputBuilder = new StringBuilder();
		inputBuilder.append(madamiraTop + "\n\n");
		String [] bodyStrings = body.split("\n");
		int segCount = 0;
		for (String s:bodyStrings) {
			inputBuilder.append("<in_seg id=\"BODY_"+Integer.toString(segCount)+"\">" + makeArabicOnly(s) + "</in_seg>\n");
			segCount++;
		}
		inputBuilder.append("\n\n" + madamiraBottom);
		inputStream = new ByteArrayInputStream(inputBuilder.toString().getBytes(StandardCharsets.UTF_8));
		String outputString = Madamira.lemmatize(8223, "http://localhost:", inputStream, output); //now this file returns a string 
		madaOutput = Jsoup.parse(outputString);
	}
	
	/**
	 * Uses JSOUP to to extract words, lemmas and pos tags from the Madamira output and then
	 * assembles TreeMap lemmas and TreeMap lemmaFreqListMap. Calculates wordCount and lexDiv.
	 */
	private void createLemmaList() {
		Elements words = madaOutput.getElementsByTag("word");
		int wCount = 0; //word count (excluding punc, latin, and digit)
		int tCount = 0; //token count
		maxLemmaComplexity = 0;
		boolean includeTokens;
		for(Element word : words) {
			includeTokens = false; //default to false, then include this batch of tokens if the corresponding lemma will also be included
			String l = new String(); // lemma (value)
			Elements analysis = word.getElementsByTag("analysis");
			for (Element item : analysis) {
				Elements morphs = item.getElementsByAttribute("lemma");
				for(Element morph : morphs) {
					Attributes morphAttributes = morph.attributes();
					String pos = morphAttributes.get("pos");
					l = morphAttributes.get("lemma");
					//l = makeArabicOnly(morphAttributes.get("lemma"));
					//l = normalize(l);
					if (!pos.equals("punc") && !pos.equals("latin") && !pos.equals("digit")) {
						includeTokens = true;
						wCount++;
						addToPOSMap(pos);
						//proper nouns appear to be messing up frequency data for easier texts
						if (!pos.equals("noun_prop")) {
							addToLemmaFreqListMap(l + ":::" + pos);
						}
					}
				}
			}
			//if we included the lemma, count the number of tokens which make up the lemma
			if (includeTokens == true) {
				Elements tokenized = word.getElementsByTag("tokenized");
				for (Element tokenList : tokenized) {
					Elements tokens = tokenList.getElementsByAttribute("tok");
					//set the highest number of tokens in a lemma
					if (tokens.size() > maxLemmaComplexity) {
						maxLemmaComplexity = tokens.size();
					}
					tCount += tokens.size();
				}
			}
		}
		wordCount = wCount;
		tokenCount = tCount;
		lexDiv = (double) lemmaFreqListMap.size() / (double) wordCount;
		lemmaComplexity = (double) tokenCount / (double) wordCount;
	}
	
	private TreeMap<String, Integer> createPOSMap() {
		TreeMap<String, Integer> map = new TreeMap<>();
		map.put("noun", 0);
		map.put("noun_num", 0);
		map.put("noun_quant", 0);
		map.put("noun_prop", 0);
		map.put("pron", 0);
		map.put("pron_dem", 0);
		map.put("pron_exclam", 0);
		map.put("pron_interrog", 0);
		map.put("pron_rel", 0);
		map.put("verb", 0);
		map.put("verb_pseudo", 0);
		map.put("part", 0);
		map.put("part_dem", 0);
		map.put("part_det", 0);
		map.put("part_focus", 0);
		map.put("part_fut", 0);
		map.put("part_interrog", 0);
		map.put("part_neg", 0);
		map.put("part_restrict", 0);
		map.put("part_verb", 0);
		map.put("part_voc", 0);
		map.put("prep", 0);
		map.put("abbrev", 0);
		map.put("conj", 0);
		map.put("conj_sub", 0);
		map.put("interj", 0);
		map.put("adv", 0);
		map.put("adv_interrog", 0);
		map.put("adv_rel", 0);
		map.put("adj", 0);
		map.put("adj_comp", 0);
		map.put("adj_num", 0);
		
		return map;
	}
	
	private void addToPOSMap(String key) {
		if (key.substring(0,3).equals("adv"))
			POSList.put("adv", POSList.get("adv") + 1);
		else if (key.substring(0,3).equals("adj"))
			POSList.put("adj", POSList.get("adj") + 1);
		else if (key.substring(0,4).equals("noun"))
			POSList.put("noun", POSList.get("noun") + 1);
		else if (key.substring(0,4).equals("pron"))
			POSList.put("pron", POSList.get("pron") + 1);
		else if (key.substring(0,4).equals("verb"))
			POSList.put("verb", POSList.get("verb") + 1);
		else if (key.substring(0,4).equals("conj"))
			POSList.put("conj", POSList.get("conj") + 1);
		else if (key.substring(0,4).equals("prep"))
			POSList.put("prep", POSList.get("prep") + 1);
		else if (key.substring(0,4).equals("part"))
			POSList.put("part", POSList.get("part") + 1);
		else if (key.equals("interj"))
			POSList.put("interj", POSList.get("interj") + 1);
		else if (key.equals("abbrev"))
			POSList.put("abbrev", POSList.get("abbrev") + 1);
	}
	
	//Used to create running totals in FreqListMap
	private void addToLemmaFreqListMap(String key) {
		if(!lemmaFreqListMap.containsKey(key)) 
			lemmaFreqListMap.put(key, 1);
		else 
			lemmaFreqListMap.put(key, lemmaFreqListMap.get(key) + 1);
	}
	
	//extracts only the arabic text from the "lemma" given by Madamira
	private String makeArabicOnly(String word) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == ' ') {
				sb.append(' ');
			} else if (word.charAt(i) >= 1536 && word.charAt(i) <= 1791) {
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
	
	/**
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
					freqListMap.put(data[0] + ":::" + data[1], Integer.parseInt(data[2]));
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
	
	/**
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
		String normalizedBody = normalize(body);
		for (int i = 0; i < normalizedBody.length(); i++) {
			if (!Character.isWhitespace(normalizedBody.charAt(i)))
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
			sb.append(avgWordLen);
			sb.append(",");
			sb.append(lexDiv); //type token ratio = sqrt it to get root type token ratio
			sb.append(",");
			sb.append(lemmaComplexity); //type token ratio = sqrt it to get root type token ratio
			sb.append(",");
			sb.append(maxLemmaComplexity); //type token ratio = sqrt it to get root type token ratio
			sb.append(",");
			sb.append(freq95);
			sb.append(",");
			sb.append(mean);
			sb.append(",");
			sb.append(median);
			sb.append(",");
			sb.append((double) POSList.get("noun") / (double) wordCount);
			sb.append(",");
			sb.append((double) POSList.get("pron") / (double) wordCount);
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
//			sb.append((double) POSList.get("interj") / (double) wordCount);
//			sb.append(",");
//			sb.append((double) POSList.get("abbrev") / (double) wordCount);
//			sb.append(",");
			//which count, false idafa
			//look at maximums of word length
			//look at stuff like dialogue indicators to see if there's a lot of dialogue (might be indicitave of lower levels)
		}
		
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
	
}
