/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.io.*;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.flair.server.raft.Raft;
import com.flair.server.utilities.Madamira;
import com.flair.server.utilities.ServerLogger;
import com.flair.server.utilities.Weka;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.parser.DocumentReadabilityLevel;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;


//import org.apache.cxf.common.i18n.Exception;
import edu.columbia.ccls.madamira.configuration.OutDoc;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Represents an Arabic text document that's parsed by the Stanford Parser and is given a readability score by RAFT
 * 
 * @author mjbriggs
 */
public class ArabicDocument implements AbstractDocument
{
	private final AbstractDocumentSource				source;
	private final double								readabilityScore;
	private final ArabicDocumentReadabilityLevel		arabicReadabilityLevel;	// calculate from the readability score
	private final ConstructionDataCollection			constructionData;

	private	String										pageText;
	private TreeMap<String, Integer> 					lemmaFreqListMap; // maps a string "LEMMA:::POS" to its frequency within the text
	private TreeMap<String, Integer> 					POSList; // keeps a count for each POS tag
	private TreeMap<String, Integer> 					freqListMap; // Arabic frequency list
	private ArrayList<Integer> frequencies;
	private String 										validConstructions[];

	private Document 									madaOutput;

	private int											numCharacters;
	private int											numSentences;
	private int											numDependencies;
	private int											numWords;
	private int											numTokens;		// number of words essentially (kinda), later substituted with number of words (without punctuation)

	private double										avgWordLength;		// doesn't include punctuation
	private double										avgSentenceLength;
	private double										avgTreeDepth;

	private double 										lexicalDiversity;
	private double										lemmaComplexity;
	private double										maxLemmaComplexity;
	private int											freq95;
	private double										mean;
	private double										median;

	private int											numNouns;
	private int											numPronouns;
	private int											numVerbs;
	private int											numPrepositions;
	private int											numParticles;
	private int											numConjunctions;
	private int											numAdverbs;
	private int											numAdjectives;

	private double										fancyDocLength;	
	private KeywordSearcherOutput						keywordData;

	private boolean 									parsed;

	private int 										taskSalt;
	private boolean 									exceptionCaught;

	public ArabicDocument(AbstractDocumentSource source, double readabilityScore,
	ArabicDocumentReadabilityLevel arabicReadabilityLevel, ConstructionDataCollection constructionData)
	{
		this.source = source;
		this.readabilityScore = readabilityScore;
		this.arabicReadabilityLevel = arabicReadabilityLevel;
		this.constructionData = constructionData;
	}

	public ArabicDocument(AbstractDocumentSource parent)
	{
        ServerLogger.get().info("Creating arabic document");
		assert parent != null;
        
		source = parent;
		constructionData = new ConstructionDataCollection(parent.getLanguage(), new DocumentConstructionDataFactory(this));

		pageText = source.getSourceText();

		double readabilityScoreCalc = 0;

		double readabilityLevelThreshold_1 = 0;
		double readabilityLevelThreshold_2 = 0;
		double readabilityLevelThreshold_3 = 0;

		lemmaFreqListMap = new TreeMap<>();
		frequencies = new ArrayList<>();

		numSentences = 0;

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

		/**
		 * The switch statements below classify the readability threshHolds 
		 * Readability thresholds are hard coded here 
		 */
		switch (source.getLanguage())
		{
			case ARABIC:
			readabilityScoreCalc = calculateReadabilityScore();
			if(readabilityScoreCalc == 0.0)
			{
				recalculateFeatures();
				ServerLogger.get().error("RAFT document analysis failed on " + getDescription() + ", now using default readability score");
				readabilityScoreCalc = Math
					.ceil(((double) numCharacters / (double) numTokens) + (numTokens / (double) numSentences));
				readabilityScoreCalc /= 10;
			}
			readabilityLevelThreshold_1 = 1.1;
			readabilityLevelThreshold_2 = 2.1;
			readabilityLevelThreshold_3 = 3.1;
			break;
		default:
			throw new IllegalArgumentException("Invalid document language, tried to use a non arabic language with an arabic document object");
		}

		if (numSentences != 0 && numCharacters != 0)		//If num sentences && num characters != 0, we use the readability score we calculated
			readabilityScore = readabilityScoreCalc;
		else												//else we use a negative score, this ensures that we either don't use the document or its is ranked as easiest
			readabilityScore = -10.0;
															// Below we determine DocumentReadabilityLevel tag for the client
		
		if (readabilityScore < readabilityLevelThreshold_1)		
			arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_1;
		else if (readabilityLevelThreshold_1 <= readabilityScore && readabilityScore <= readabilityLevelThreshold_2)		
		arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_2;
		else if (readabilityLevelThreshold_2 <= readabilityScore && readabilityScore <= readabilityLevelThreshold_3)
		arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_3;
		else 
			arabicReadabilityLevel = ArabicDocumentReadabilityLevel.LEVEL_4;
		
		ServerLogger.get().info("arabicReadabilityLevel = " + arabicReadabilityLevel.toString() + " for readability score " + readabilityScore);

		avgWordLength = avgSentenceLength = avgTreeDepth = fancyDocLength = 0;
		keywordData = null;
		parsed = false;
	}

	private void recalculateFeatures() {
		StringTokenizer tokenizer = new StringTokenizer(pageText, " ");

		// calculate readability score, level, etc
		numTokens = tokenizer.countTokens();
		int whitespaceCount = 0;
		for (int i = 0; i < pageText.length(); i++)
		{
			if (Character.isWhitespace(pageText.charAt(i)))
				whitespaceCount++;
		}
		numCharacters = pageText.length() - whitespaceCount;
		tokenizer = new StringTokenizer(pageText, "[.!?]");
		numSentences = tokenizer.countTokens();
		numDependencies = 0;
	}


	public double calculateReadabilityScore()
	{
		double readabilityLevel;
		readabilityLevel = 0;
		try
		{
			readabilityLevel = scoreText();	//throws a bunch of exceptions so just catch the most general case
		}
		catch(Exception ex)
		{
			ServerLogger.get().error(ex, "caught exception " + ex.toString() + " in calculateReadabilityScore, now setting score to 0");
			return 0;
		}
		return readabilityLevel;
	}

	private int scoreText() throws IOException, FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException, Exception
	{
		Boolean we_have_run_score_text = true;
		int returnValue = 0;
		createPOSMap();

		madaOutput = Madamira.getMadaDocument(pageText);

		createLemmaList();
		if (numWords > 0)
		{
			countSentences();
			createFrequencies();
			if (frequencies.size() > 0)
			{
				calcFreq95();
				calcMean();
				calcMedian();
				calcAvgWordLen();
			}
		}
		String featureData = getFeatureData() + "1.0";
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

	private String getFeatureData()
	{
		StringBuilder sb = new StringBuilder();
		if (numWords > 0 && frequencies.size() > 0)
		{
			// sb.append((double) numWords);
			// sb.append(",");
			// sb.append((double) sentCount);
			// sb.append(",");
			sb.append(avgSentenceLength);
			sb.append(",");
			sb.append(avgWordLength);
			sb.append(",");
			sb.append(lexicalDiversity); // type token ratio = sqrt it to get root type token ratio
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
			sb.append((double) numNouns / (double) numWords);
			sb.append(",");
			sb.append((double) numPronouns / (double) numWords);
			sb.append(",");
			sb.append((double) numVerbs / (double) numWords);
			sb.append(",");
			sb.append((double) numPrepositions / (double) numWords);
			sb.append(",");
			sb.append((double) numParticles / (double) numWords);
			sb.append(",");
			sb.append((double) numConjunctions / (double) numWords);
			sb.append(",");
			sb.append((double) numAdverbs / (double) numWords);
			sb.append(",");
			sb.append((double) numAdjectives / (double) numWords);
			sb.append(",");
			// sb.append((double) POSList.get("interj") / (double) numWords);
			// sb.append(",");
			// sb.append((double) POSList.get("abbrev") / (double) numWords);
			// sb.append(",");
			// which count, false idafa
			// look at maximums of word length
			// look at stuff like dialogue indicators to see if there's a lot of dialogue
			// (might be indicitave of lower levels)
		}

		// System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * Uses JSOUP to to extract words, lemmas and pos tags from the Madamira output
	 * and then assembles TreeMap lemmas and TreeMap lemmaFreqListMap. Calculates
	 * wordCount and lexDiv.
	 */
	public void createLemmaList()
	{
		if (madaOutput == null)
		{
			ServerLogger.get().error("madaOutput is null");
			return;
		}
		Elements words = madaOutput.getElementsByTag("word");
		int wCount = 0; // word count (excluding punc, latin, and digit)
		int tCount = 0; // token count
		maxLemmaComplexity = 0;
		boolean includeTokens;
		for (Element word : words)
		{
			includeTokens = false; // default to false, then include this batch of tokens if the corresponding
			// lemma will also be included
			String l = new String(); // lemma (value)
			Elements analysis = word.getElementsByTag("analysis");
			for (Element item : analysis)
			{
				Elements morphs = item.getElementsByTag("morph_feature_set");
				for (Element morph : morphs)
				{
					Attributes morphAttributes = morph.attributes();
					String pos = morphAttributes.get("pos");
					l = makeArabicOnly(morphAttributes.get("lemma"));
					if (!pos.equals("punc") && !pos.equals("latin") && !pos.equals("digit"))
					{
						includeTokens = true;
						wCount++;
						addToPOSMap(pos);
						// proper nouns appear to be messing up frequency data for easier texts
						if (!pos.equals("noun_prop"))
						{
							addToLemmaFreqListMap(l + ":::" + pos);
						}
					}
				}
			}
			// if we included the lemma, count the number of tokens which make up the lemma
			if (includeTokens == true)
			{
				Elements tokenized = word.getElementsByTag("tokenized");
				for (Element tokenList : tokenized)
				{
					Elements tokens = tokenList.getElementsByAttribute("tok");
					// set the highest number of tokens in a lemma
					if (tokens.size() > maxLemmaComplexity)
					{
						maxLemmaComplexity = tokens.size();
					}
					tCount += tokens.size();
				}
			}
		}
		numWords = wCount;
		numTokens = tCount;
		lexicalDiversity = (double) lemmaFreqListMap.size() / (double) numWords;
		lemmaComplexity = (double) numTokens / (double) numWords;
	}

	public void addToPOSMap(String key)
	{
		if (key == null)
			return;

		if (POSList == null)
		{
			createPOSMap();
			addToPOSMap(key);
			return;
		}
		int count = 0;
		int len = validConstructions.length;
		boolean valueAdded = false;
		while (count < len && valueAdded == false)
		{
			if (validConstructions[count].equals(key))
			{
				addKey(key);
				valueAdded = true;
			}
			count++;
		}
	}

	public void createPOSMap()
	{
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

	public void addKey(String key)
	{
		if (POSList == null || key == null)
			return;

		if (!POSList.containsKey(key))
		{
			POSList.put(key, 0);
		}
		POSList.put(key, POSList.get(key) + 1);
	}

	// Used to create running totals in FreqListMap
	public void addToLemmaFreqListMap(String key)
	{
		if (key == null)
			return;

		if (!lemmaFreqListMap.containsKey(key))
			lemmaFreqListMap.put(key, 1);
		else
			lemmaFreqListMap.put(key, lemmaFreqListMap.get(key) + 1);
	}

	// calculates the number of sentences and the average sentence length
	public void countSentences()
	{
		// Counts a sentence at new lines and after punctuation
		for (int i = 1; i < pageText.length(); i++)
		{
			char c = pageText.charAt(i);
			char p = pageText.charAt(i - 1);
			if (isEndPunct(c) && !isEndPunct(p) || c == '\n' && p != '\n' && !isEndPunct(p)
					|| i == pageText.length() - 1 && p != '\n' && !isEndPunct(p))
				numSentences++;
		}

		avgSentenceLength = (double) numWords / (double) numSentences;
	}

	public boolean isEndPunct(char c)
	{
		if (c == '.' || c == '!' || c == '\u061f')
			return true;
		return false;
	}

	public void createFrequencies()
	{
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
	public void calcFreq95()
	{
		int index95 = (int) (frequencies.size() * .95);
		if (index95 > 0)
		{
			freq95 = frequencies.get(index95);
		}
		else
		{
			freq95 = 0;
		}
	}

	// calculates the mean frequency
	public void calcMean()
	{
		int total = 0;
		for (int frequency : frequencies)
			total += frequency;
		mean = (double) total / (double) frequencies.size();
	}

	// calculates the median frequency
	public void calcMedian()
	{
		if (frequencies.size() % 2 == 0)
		{
			int middleRight = frequencies.size() / 2;
			int middleLeft = middleRight - 1;
			median = (frequencies.get(middleRight) + frequencies.get(middleLeft)) / 2;
		}
		else
		{
			int middle = frequencies.size() / 2;
			median = Math.floor(frequencies.get(middle));
		}
	}

	public void calcAvgWordLen()
	{
		int chars = 0;
		String normalizedBody = normalize(pageText);
		for (int i = 0; i < normalizedBody.length(); i++) {
			if (!Character.isWhitespace(normalizedBody.charAt(i)))
				chars++;
		}
		if(numWords > 0)
			avgWordLength = (double) chars / (double) numWords;
		else
			avgWordLength = 0;
	}

	private static String makeArabicOnly(String word)
	{
		if (word == null)
			return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == ' ')
			{
				sb.append(' ');
			}
			else if (word.charAt(i) >= 1536 && word.charAt(i) <= 1791)
			{
				sb.append(word.charAt(i));
			}
			else if (word.charAt(i) == 46)
			{
				sb.append(word.charAt(i)); // keeps periods
			}

		}
		return sb.toString();
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

	@Override
	public Language getLanguage() 
	{
		return source.getLanguage();
	}

	@Override
	public String getText() 
	{
		return source.getSourceText();
	}

	@Override
	public String getDescription() 
	{
		return "{" + source.getDescription() + " | S[" + numSentences + "], C[" + numCharacters + "]" + "}";
	}

	@Override
	public DocumentConstructionData getConstructionData(GrammaticalConstruction type) 
	{
		return (DocumentConstructionData) constructionData.getData(type);
	}

	//consider getting rid of this function all together, it isn't used for Arabic since 
	//we do not use grammatical constructions for our arabic functionality 
	public void calculateFancyDocLength()
	{
		double sumOfPowers = 0.0;
		double squareRoot = 0.0;
		// iterate through the construction data set and calc
		for (GrammaticalConstruction itr : getSupportedConstructions())
		{
			DocumentConstructionData data = getConstructionData(itr);
			if (data.hasConstruction())
			{
				sumOfPowers += Math.pow(data.getWeightedFrequency(), 2);
			}
		}

		if (sumOfPowers > 0)
			squareRoot = Math.sqrt(sumOfPowers);

		fancyDocLength = squareRoot;
	}

	public Document getMadaOutput() {
		return madaOutput;
	}

	public String getPageText() {
		return pageText;
	}

	public double getLexicalDiversity() {
		return lexicalDiversity;
	}

	public double getLemmaComplexity() {
		return lemmaComplexity;
	}

	public double getMaxLemmaComplexity() {
		return maxLemmaComplexity;
	}

	public int getFreq95() {
		return freq95;
	}

	public double getMean() {
		return mean;
	}

	public double getMedian() {
		return median;
	}

	public int getNumNouns() {
		return numNouns;
	}

	public int getNumPronouns() {
		return numPronouns;
	}

	public int getNumVerbs() {
		return numVerbs;
	}

	public int getNumPrepositions() {
		return numPrepositions;
	}

	public int getNumParticles() {
		return numParticles;
	}

	public int getNumConjunctions() {
		return numConjunctions;
	}

	public int getNumAdverbs() {
		return numAdverbs;
	}

	public int getNumAdjectives() {
		return numAdjectives;
	}

	public double getFancyDocLength() {
		return fancyDocLength;
	}

	public int getTaskSalt() {
		return taskSalt;
	}

	public boolean isExceptionCaught() {
		return exceptionCaught;
	}

	@Override
	public double getReadabilityScore() 
	{
		return readabilityScore;
	}

	@Override
	public DocumentReadabilityLevel getReadabilityLevel() 
	{
		return null;
	}

	@Override
	public ArabicDocumentReadabilityLevel getArabicReadabilityLevel() 
	{
		return arabicReadabilityLevel;
	}

	@Override
	public int getNumCharacters() 
	{
		return numCharacters;
	}

	@Override
	public int getNumSentences() 
	{
		return numSentences;
	}

	@Override
	public int getNumDependencies() 
	{
		return numDependencies;
	}

	@Override
	public int getNumWords()
	{
		return numWords;
	}

	@Override
	public int getNumTokens()
	{
		return numTokens;
	}

	@Override
	public double getAvgWordLength() 
	{
		return avgWordLength;
	}

	@Override
	public double getAvgSentenceLength() 
	{
		return avgSentenceLength;
	}

	@Override
	public double getAvgTreeDepth() 
	{
		return avgTreeDepth;
	}

	@Override
	public int getLength() 
	{
		return numWords;
	}

	@Override
	public double getFancyLength() 
	{
		return fancyDocLength;
	}

	public void setLexicalDiversity(double lexicalDiversity) {
		this.lexicalDiversity = lexicalDiversity;
	}

	public void setLemmaComplexity(double lemmaComplexity) {
		this.lemmaComplexity = lemmaComplexity;
	}

	public void setMaxLemmaComplexity(double maxLemmaComplexity) {
		this.maxLemmaComplexity = maxLemmaComplexity;
	}

	public void setFreq95(int freq95) {
		this.freq95 = freq95;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public void setNumNouns(int numNouns) {
		this.numNouns = numNouns;
	}

	public void setNumPronouns(int numPronouns) {
		this.numPronouns = numPronouns;
	}

	public void setNumVerbs(int numVerbs) {
		this.numVerbs = numVerbs;
	}

	public void setNumPrepositions(int numPrepositions) {
		this.numPrepositions = numPrepositions;
	}

	public void setNumParticles(int numParticles) {
		this.numParticles = numParticles;
	}

	public void setNumConjunctions(int numConjunctions) {
		this.numConjunctions = numConjunctions;
	}

	public void setNumAdverbs(int numAdverbs) {
		this.numAdverbs = numAdverbs;
	}

	public void setNumAdjectives(int numAdjectives) {
		this.numAdjectives = numAdjectives;
	}

	public void setFancyDocLength(double fancyDocLength) {
		this.fancyDocLength = fancyDocLength;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	@Override
	public void setNumCharacters(int value) 
	{
		numCharacters = value;
	}

	@Override
	public void setNumSentences(int value) 
	{
		numSentences = value;
	}

	@Override
	public void setNumDependencies(int value) 
	{
		numDependencies = value;
	}

	@Override
	public void setNumWords(int numWords) 
	{
		this.numWords = numWords;
	}

	@Override
	public void setNumTokens(int numTokens)
	{
		this.numTokens = numTokens;
	}

	@Override
	public void setAvgWordLength(double value) 
	{
		avgWordLength = value;
	}

	@Override
	public void setAvgSentenceLength(double value) 
	{
		avgSentenceLength = value;
	}

	@Override
	public void setAvgTreeDepth(double value) 
	{
		avgTreeDepth = value;
	}

	@Override
	public void setLength(int value) 
	{
		numWords = value;
	}

	@Override
	public boolean isParsed() 
	{
		return parsed;
	}

	@Override
	public void flagAsParsed() 
	{
		if (parsed)
			throw new IllegalStateException("Document already flagged as parsed");

		parsed = true;
		calculateFancyDocLength();
	}

	@Override
	public AbstractDocumentSource getDocumentSource() 
	{
		return source;
	}

	@Override
	public int compareTo(AbstractDocument t) 
	{
		return source.compareTo(t.getDocumentSource());
	}

	@Override
	public KeywordSearcherOutput getKeywordData() 
	{
		return keywordData;
	}

	@Override
	public void setKeywordData(KeywordSearcherOutput data) 
	{
		keywordData = data;
	}

	@Override
	public Iterable<GrammaticalConstruction> getSupportedConstructions() 
	{
		return GrammaticalConstruction.getForLanguage(getLanguage());
	}
	@Override 
	public String toString()
	{
		return "Document : " + getDescription() + 
		"\nAverage Sentence Length : " + getAvgSentenceLength() + 
		"\nAverage Word Length : " + getAvgWordLength() + 
		"\nLength : " + getLength() + 
		"\nNumber of Dependencies : " + getNumDependencies() +
		"\nNumber of Words : " + getNumWords() + 
		"\nNumber of Tokens : " + getNumTokens() +
		"\nNumber of Sentences : " + getNumSentences() +
		"\nNumber of Characters : " + getNumCharacters() + 
		"\nIs Parsed : " + isParsed() + "\n";
	}
}

class ArabicDocumentFactory implements AbstractDocumentFactory
{
	@Override
	public AbstractDocument create(AbstractDocumentSource source) {
		return new ArabicDocument(source);
	}
}