package com.flair.server.raft;

import java.io.File;
import java.io.Writer;
import java.net.UnknownHostException;
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
		try {
			webText = webText.trim().replaceAll("&", "+");
			webText = webText.trim().replaceAll("(\\s)+", "$1");
			body = webText;
		} catch (NullPointerException e) {
			ServerLogger.get().error(e, e.getMessage());
			body = null;
		}
		Random r = new Random();
		taskSalt = r.nextInt(10000000); // gives a random number to salt our file names with
		lemmaFreqListMap = new TreeMap<>();
		frequencies = new ArrayList<>();
		wordCount = 0;
		tokenCount = 0;
		sentCount = 0;
		freq95 = 0;
		validConstructions = new String[10];
		validConstructions[0] = "adv";
		validConstructions[1] = "adj";
		validConstructions[2] = "noun";
		validConstructions[3] = "pron";
		validConstructions[4] = "verb";
		validConstructions[5] = "conj";
		validConstructions[6] = "prep";
		validConstructions[7] = "part";
		validConstructions[8] = "interj";
		validConstructions[9] = "abbrev";

		exceptionCaught = false;
	}

	private Document madaOutput;
	private String body;
	private TreeMap<String, Integer> lemmaFreqListMap; // maps a string "LEMMA:::POS" to its frequency within the text
	private TreeMap<String, Integer> POSList; // keeps a count for each POS tag
	private TreeMap<String, Integer> freqListMap; // Arabic frequency list
	private ArrayList<Integer> frequencies;
	private String validConstructions[];
	private String input;
	private String output;

	private int wordCount; // number of words in the body
	private int tokenCount; // number of tokens in the body
	private int sentCount; // number of sentences in the body
	private double avgSentLen; // average sentence length
	private double lexDiv; // lexical diversity (# lemmas/ # of words)
	private double lemmaComplexity; // lexical complexity (# tokens / # words)
	private double maxLemmaComplexity; // lexical complexity (# tokens / # words)
	private int freq95; // frequency of last word of the 95th percentile
	private double mean;
	private double median;
	private double avgWordLen;
	private int taskSalt;
	private boolean exceptionCaught;

	private String madamiraTop = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<madamira_input xmlns=\"urn:edu.columbia.ccls.madamira.configuration:0.1\">\r\n"
			+ "	<madamira_configuration>\r\n"
			+ "        <preprocessing sentence_ids=\"false\" separate_punct=\"true\" input_encoding=\"UTF8\"/>\r\n"
			+ "        <overall_vars output_encoding=\"UTF8\" dialect=\"MSA\" output_analyses=\"TOP\" morph_backoff=\"NONE\"/>\r\n"
			+ "        <requested_output>\r\n"
			+ "            <req_variable name=\"PREPROCESSED\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"STEM\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"GLOSS\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"LEMMA\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"DIAC\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"ASP\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"CAS\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"ENC0\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"ENC1\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"ENC2\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"GEN\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"MOD\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"NUM\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"PER\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"POS\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PRC0\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"PRC1\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"PRC2\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"PRC3\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"STT\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"VOX\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"BW\" value=\"false\" />\r\n"
			+ "            <req_variable name=\"SOURCE\" value=\"false\" />\r\n"
			+ "			<req_variable name=\"NER\" value=\"false\" />\r\n"
			+ "			<req_variable name=\"BPC\" value=\"false\" />\r\n" + "        </requested_output>\r\n"
			+ "	</madamira_configuration>\r\n" + "    <in_doc id=\"ExampleDocument\">";

	private String madamiraBottom = "</in_doc>\r\n" + "</madamira_input>";

	public int getSalt() {
		return taskSalt;
	}

	/**
	 * Creates XML input string for Madamira out of the body, saves it to
	 * madamiraInput, sends it off to be lemmatized and saves output to
	 * madamiraOutput.
	 */
	public void lemmatizeText() {
		if (body != null) {
			input = "/tmp/mada_input" + taskSalt + ".txt";
			output = "/tmp/mada_output" + taskSalt + ".txt";
			InputStream inputStream;
			StringBuilder inputBuilder = new StringBuilder();
			inputBuilder.append(madamiraTop + "\n\n");
			String[] bodyStrings = body.split("\n");
			int segCount = 0;
			for (String s : bodyStrings) {
				inputBuilder.append(
						"<in_seg id=\"BODY_" + Integer.toString(segCount) + "\">" + makeArabicOnly(s) + "</in_seg>\n");
				segCount++;
			}
			inputBuilder.append("\n\n" + madamiraBottom);
			inputStream = new ByteArrayInputStream(inputBuilder.toString().getBytes(StandardCharsets.UTF_8));
			String outputString;
			outputString = Madamira.lemmatize(8223, "http://mada_image:", inputStream);
			if (outputString == null) {
				ServerLogger.get().error("failed to connect to mada_image, now trying to connect on localhost");
				outputString = Madamira.lemmatize(8223, "http://localhost:", inputStream);
			}
			if (outputString == null) {
				ServerLogger.get().error("failed to connect to localhost, make sure that madamira server is running");
				madaOutput = new Document(""); // creates a new empty document
			} else {
				// ServerLogger.get().info("outputString : \n" + outputString);
				madaOutput = Jsoup.parse(outputString);
			}
		} else {
			ServerLogger.get().error("Body is null, creating new empy document");
			madaOutput = new Document(""); // creates a new empty document
		}
	}

	/**
	 * Uses JSOUP to to extract words, lemmas and pos tags from the Madamira output
	 * and then assembles TreeMap lemmas and TreeMap lemmaFreqListMap. Calculates
	 * wordCount and lexDiv.
	 */
	public void createLemmaList() {
		if (madaOutput == null) {
			ServerLogger.get().error("madaOutput is null");
			return;
		}
		Elements words = madaOutput.getElementsByTag("word");
		int wCount = 0; // word count (excluding punc, latin, and digit)
		int tCount = 0; // token count
		maxLemmaComplexity = 0;
		boolean includeTokens;
		for (Element word : words) {
			includeTokens = false; // default to false, then include this batch of tokens if the corresponding
									// lemma will also be included
			String l = new String(); // lemma (value)
			Elements analysis = word.getElementsByTag("analysis");
			for (Element item : analysis) {
				Elements morphs = item.getElementsByAttribute("lemma");
				for (Element morph : morphs) {
					Attributes morphAttributes = morph.attributes();
					String pos = morphAttributes.get("pos");
					l = morphAttributes.get("lemma");
					// l = makeArabicOnly(morphAttributes.get("lemma"));
					// l = normalize(l);
					if (!pos.equals("punc") && !pos.equals("latin") && !pos.equals("digit")) {
						includeTokens = true;
						wCount++;
						addToPOSMap(pos);
						// proper nouns appear to be messing up frequency data for easier texts
						if (!pos.equals("noun_prop")) {
							addToLemmaFreqListMap(l + ":::" + pos);
						}
					}
				}
			}
			// if we included the lemma, count the number of tokens which make up the lemma
			if (includeTokens == true) {
				Elements tokenized = word.getElementsByTag("tokenized");
				for (Element tokenList : tokenized) {
					Elements tokens = tokenList.getElementsByAttribute("tok");
					// set the highest number of tokens in a lemma
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

	public void createPOSMap() {
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

		POSList = map;
	}

	public void addToPOSMap(String key) {
		if (key == null)
			return;

		if (POSList == null) {
			createPOSMap();
			addToPOSMap(key);
			return;
		}
		int count = 0;
		int len = validConstructions.length;
		boolean valueAdded = false;
		while (count < len && valueAdded == false) {
			if (validConstructions[count].equals(key)) {
				addKey(key);
				valueAdded = true;
			}
			count++;
		}
	}

	public void addKey(String key) {
		if (POSList == null || key == null)
			return;

		if (!POSList.containsKey(key)) {
			POSList.put(key, 0);
		}
		POSList.put(key, POSList.get(key) + 1);
	}

	// Used to create running totals in FreqListMap
	public void addToLemmaFreqListMap(String key) {
		if (key == null)
			return;

		if (!lemmaFreqListMap.containsKey(key))
			lemmaFreqListMap.put(key, 1);
		else
			lemmaFreqListMap.put(key, lemmaFreqListMap.get(key) + 1);
	}

	// extracts only the arabic text from the "lemma" given by Madamira
	public String makeArabicOnly(String word) {
		if (word == null)
			return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == ' ') {
				sb.append(' ');
			} else if (word.charAt(i) >= 1536 && word.charAt(i) <= 1791) {
				sb.append(word.charAt(i));
			} else if (word.charAt(i) == 46) {
				sb.append(word.charAt(i)); // keeps periods
			}

		}
		return sb.toString();
	}

	// calculates the number of sentences and the average sentence length
	public void countSentences() {
		// Counts a sentence at new lines and after punctuation
		for (int i = 1; i < body.length(); i++) {
			char c = body.charAt(i);
			char p = body.charAt(i - 1);
			if (isEndPunct(c) && !isEndPunct(p) || c == '\n' && p != '\n' && !isEndPunct(p)
					|| i == body.length() - 1 && p != '\n' && !isEndPunct(p))
				sentCount++;
		}

		avgSentLen = (double) wordCount / (double) sentCount;
	}

	public boolean isEndPunct(char c) {
		if (c == '.' || c == '!' || c == '\u061f')
			return true;
		return false;
	}

	public void createFrequencies() {
		freqListMap = readFreqList("freqList.txt");
		for (Map.Entry<String, Integer> entry : lemmaFreqListMap.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				if (freqListMap.get(entry.getKey()) != null)
					frequencies.add(freqListMap.get(entry.getKey()));
			}
		}

		frequencies.sort(null);
	}

	/**
	 * helper for createFrequencies() reads freqList in from file and returns it
	 */
	public TreeMap<String, Integer> readFreqList(String freqList) 
	{
		exceptionCaught = false;
		TreeMap<String, Integer> freqListMap = new TreeMap<>();
		String charSet = "UTF8";
		try 
		{
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream input = classLoader.getResourceAsStream(freqList);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, charSet));

			String str;
			while ((str = reader.readLine()) != null) 
			{
				String regex = ":::";
				String[] data = str.split(regex);
				if(data.length > 2)
					freqListMap.put(data[0] + ":::" + data[1], Integer.parseInt(data[2]));
			}

			reader.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			//We will only get here if we specify an unsupported encoding to the InpusStreamReader
			ServerLogger.get().error(e, "Unsupported encoding : " + charSet + " while reading " + freqList);
			exceptionCaught = true;
			return new TreeMap<>();
		} 
		catch (NullPointerException e) 
		{
			ServerLogger.get().error(e, "File : " + freqList + " does not exist");
			exceptionCaught = true;
			return new TreeMap<>();
		} 
		catch (IOException e) 
		{
			ServerLogger.get().error(e, "");
			exceptionCaught = true;
			return new TreeMap<>();
		}

		return freqListMap;
	}

	/**
	 * Sorts the entries in the lemmaFreqListMap into a sorted List to find the 95th
	 * percentile, Then finds that lemma's frequency in the freqList
	 */
	public void calcFreq95() {
		int index95 = (int) (frequencies.size() * .95);
		if (index95 > 0) {
			freq95 = frequencies.get(index95);
		} else {
			freq95 = 0;
		}
	}

	// calculates the mean frequency
	public void calcMean() {
		int total = 0;
		for (int frequency : frequencies)
			total += frequency;
		mean = (double) total / (double) frequencies.size();
	}

	// calculates the median frequency
	public void calcMedian() {
		if (frequencies.size() % 2 == 0) {
			int middleRight = frequencies.size() / 2;
			int middleLeft = middleRight - 1;
			median = (frequencies.get(middleRight) + frequencies.get(middleLeft)) / 2;
		} else {
			int middle = frequencies.size() / 2;
			median = Math.floor(frequencies.get(middle));
		}
	}

	public void calcAvgWordLen() {
		int chars = 0;
		for (int i = 0; i < body.length(); i++) {
			if (!Character.isWhitespace(body.charAt(i)))
				chars++;
		}
		if(wordCount > 0)
			avgWordLen = (double) chars / (double) wordCount;
		else 
			avgWordLen = 0;
	}

	// returns a string result of all the desired stats
	public String getResult() {
		StringBuilder sb = new StringBuilder();
		if (wordCount > 0 && frequencies.size() > 0) {
			// sb.append((double) wordCount);
			// sb.append(",");
			// sb.append((double) sentCount);
			// sb.append(",");
			sb.append(avgSentLen);
			sb.append(",");
			sb.append(avgWordLen);
			sb.append(",");
			sb.append(lexDiv); // type token ratio = sqrt it to get root type token ratio
			sb.append(",");
			sb.append(lemmaComplexity); // type token ratio = sqrt it to get root type token ratio
			sb.append(",");
			sb.append(maxLemmaComplexity); // type token ratio = sqrt it to get root type token ratio
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
			// sb.append((double) POSList.get("interj") / (double) wordCount);
			// sb.append(",");
			// sb.append((double) POSList.get("abbrev") / (double) wordCount);
			// sb.append(",");
			// which count, false idafa
			// look at maximums of word length
			// look at stuff like dialogue indicators to see if there's a lot of dialogue
			// (might be indicitave of lower levels)
		}

		// System.out.println(sb.toString());
		return sb.toString();
	}

	public ArrayList<Integer> getFrequencies() 
	{
		return frequencies;
	}

	public void setFrequencies(ArrayList<Integer> frequencies)
	{
		this.frequencies = frequencies;
	}
	public void setWordCount(int wordCount) 
	{
		this.wordCount = wordCount;
	}
	public int getWordCount() 
	{
		return wordCount;
	}

	public Document getMadaOutput() 
	{
		return madaOutput;
	}

	public void setMadaOutput(Document madaOutput) 
	{
		this.madaOutput = madaOutput;
	}

	public int getTokenCount() 
	{
		return tokenCount;
	}

	public TreeMap<String, Integer> getPOSList() 
	{
		return POSList;
	}

	public void setPOSList(TreeMap<String, Integer> pOSList) 
	{
		POSList = pOSList;
	}

	public TreeMap<String, Integer> getLemmaFreqListMap() 
	{
		return lemmaFreqListMap;
	}

	public void setLemmaFreqListMap(TreeMap<String, Integer> lemmaFreqListMap) 
	{
		this.lemmaFreqListMap = lemmaFreqListMap;
	}

	public int getSentCount() 
	{
		return sentCount;
	}

	public boolean isExceptionCaught() 
	{
		return exceptionCaught;
	}

	public void setExceptionCaught(boolean exceptionCaught) 
	{
		this.exceptionCaught = exceptionCaught;
	}

	public int getFreq95() 
	{
		return freq95;
	}

	public void setFreq95(int freq95) 
	{
		this.freq95 = freq95;
	}

	public double getMean() 
	{
		return mean;
	}

	public void setMean(double mean) 
	{
		this.mean = mean;
	}

	public double getMedian() 
	{
		return median;
	}

	public void setMedian(double median) 
	{
		this.median = median;
	}

	public double getAvgWordLen() 
	{
		return avgWordLen;
	}

	public void setAvgWordLen(double avgWordLen) 
	{
		this.avgWordLen = avgWordLen;
	}

	public double getAvgSentLen() {
		return avgSentLen;
	}

	public void setAvgSentLen(double avgSentLen) {
		this.avgSentLen = avgSentLen;
	}

	public double getLexDiv() {
		return lexDiv;
	}

	public void setLexDiv(double lexDiv) {
		this.lexDiv = lexDiv;
	}
}
