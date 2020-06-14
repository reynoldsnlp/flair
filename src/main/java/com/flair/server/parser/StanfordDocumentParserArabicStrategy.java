/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.util.*;
import com.flair.shared.grammar.Language;
import com.flair.shared.grammar.GrammaticalConstruction;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class StanfordDocumentParserArabicStrategy extends BasicStanfordDocumentParserStrategy {
	private ArabicDocument workingDoc;
	private int tokenCount;
	private int wordCount;
	private int characterCount;
	private int sentenceCount;
	private int depthCount;
	private int dependencyCount;
	private int adjCount;

	private static final String ARABIC_POS_MODEL		= "edu/stanford/nlp/models/pos-tagger/arabic/arabic.tagger";
	private static final String ARABIC_PARSE_MODEL		= "edu/stanford/nlp/models/srparser/arabicSR.ser.gz";
	private static final String ARABIC_SEGMENT_MODEL	= "edu/stanford/nlp/models/segmenter/arabic/arabic-segmenter-atb+bn+arztrain.ser.gz";
    
	private static final String WORD_PATTERN = "[\\u0600-\\u06FF]+"; // TODO add Ёё and U+0300 and U+0301 and more? TODO test

    public StanfordDocumentParserArabicStrategy()
    {

	//pipeline = null;
    }

    public void setPipeline(StanfordCoreNLP pipeline)
    {
    	assert pipeline != null;
    	this.pipeline = pipeline;
    }

    public boolean	isLanguageSupported(Language lang){
        return true;
    }
    private void initializeState(AbstractDocument doc) {
		if (pipeline == null)
		{
			throw new IllegalStateException("Parser not set");
		} else if (isLanguageSupported(doc.getLanguage()) == false)
		{
			throw new IllegalArgumentException("Document language " + doc.getLanguage()
					+ " not supported (Strategy language: " + Language.ARABIC + ")");
		}

		workingDoc = (ArabicDocument) doc;
	}
    
    private void resetState() {
    /*wordCount = tokenCount = dependencyCount = sentenceCount = depthCount = characterCount = goingToFound = 0;
		treeOutput = null;
		wordsOutput = null;
		depsOutput = null;
		conditionalFound = false;
		usedFound = false;
		comparativeMoreFound = false;
		superlativeMostFound = false;*/
		wordCount = tokenCount = characterCount = sentenceCount = dependencyCount = depthCount = 0;
		pipeline = null;
		workingDoc = null;
	}
    
    public boolean	apply(AbstractDocument docToParse){
		assert docToParse != null;
		int attempts = 0; 
		try
		{
			initializeState(docToParse);

			Annotation docAnnotation = new Annotation(workingDoc.getPageText());
			pipeline.annotate(docAnnotation);

			Document madaOutput = workingDoc.getMadaOutput();

			List<CoreMap> stanfordSents = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
			List<CoreLabel> stanfordWords = getStanfordWords(stanfordSents);
			List<MadaToken> madaTokens = getMadaTokens(madaOutput);
			alignTokens(madaTokens, stanfordWords);

			inspectText(madaTokens, stanfordWords);

			/*// update doc properties
			workingDoc.setAvgSentenceLength((double) wordCount / (double) sentenceCount);
			//workingDoc.setAvgTreeDepth((double) depthCount / (double) sentenceCount);
			workingDoc.setAvgWordLength((double) characterCount / (double) wordCount);
			workingDoc.setLength(wordCount);
			workingDoc.setNumDependencies(dependencyCount);
			workingDoc.setNumWords(wordCount);
			workingDoc.setNumTokens(tokenCount);
			workingDoc.setNumSentences(sentenceCount);
			workingDoc.setNumCharacters(characterCount);*/
			workingDoc.flagAsParsed();
		} finally
		{
			resetState();
		}

        return true;
    }

    private List<MadaToken> getMadaTokens(Document madaOutput) {
		//Create list of tuples containing each ATB4MT token and morph_feature_set of the word the token is in
		ArrayList<MadaToken> madaTokens = new ArrayList<MadaToken>();

		String preprocessed = madaOutput.getElementsByTag("preprocessed").text();
		//System.out.println(preprocessed);
/*
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("theText.txt"), "UTF-8"));
			writer.write(preprocessed);
			for(int i = 0; i < madaTokens.size(); i++) {
				//System.out.println(madaTokens.get(i).getValue0().getElementsByTag("form0").toString() + ".............." + stanfordWords.get(i).word());
				String madaToken = madaTokens.get(i).getToken().attr("form0");
				System.out.println(madaToken);
				writer.write("\n" + madaToken);
			}
			System.out.println("theTxt.txt file written");
		}

		catch (IOException e){}*/


		Elements madaWords = madaOutput.getElementsByTag("word");
		for(Element madaWord : madaWords) {
			int wordID = Integer.parseInt(madaWord.attr("id"));
			String word = madaWord.attr("word");
			Elements tokenizeds = madaWord.getElementsByTag("tokenized");
			for (Element tokenized: tokenizeds) {
				Elements toks = tokenized.getElementsByTag("tok");
				List<Element> analysis = madaWord.getElementsByTag("analysis");
				if (analysis.size() > 0) {
					Element morphFeatureSet = analysis.get(0).getElementsByTag("morph_feature_set").get(0);
					for(Element tok: toks)
						madaTokens.add(new MadaToken(tok, morphFeatureSet, word, wordID));
				}
				else {
					for(Element tok: toks)
						madaTokens.add(new MadaToken(tok, null, word, wordID));
				}

			}
		}

		return madaTokens;
	}

	private List<CoreLabel> getStanfordWords(List<CoreMap> sentences) {
		List<CoreLabel> words = new ArrayList<CoreLabel>();

		for(CoreMap itr: sentences) {
			for (CoreLabel word: itr.get(CoreAnnotations.TokensAnnotation.class))
				words.add(word);
		}

		return words;
	}

	private void alignTokens(List<MadaToken> madaTokens, List<CoreLabel> stanfordWords) {
    	int m = 0;
		int s = 0;
    	while (m < madaTokens.size() && s < stanfordWords.size()) {

    		//System.out.println(m);
    		/*if(m > 1035) {
    			System.out.println("1");
			}
    		String mToken = madaTokens.get(m).getTok();
    		String sWord = stanfordWords.get(s).word();*/

			int start = stanfordWords.get(s).beginPosition();
			int end = stanfordWords.get(s).endPosition();

			String sCurrent = normalize(stanfordWords.get(s).word());
			String sTokenAdding = sCurrent;
			int sCounter = 0;

    		String mCurrent = normalize(madaTokens.get(m).getTok());
			String mTokenAdding = mCurrent;
			int mCounter = 0;

    		while (sCurrent.contains(mTokenAdding)) {
    			mCounter++;
    			mCurrent = mTokenAdding;
    			if (m + mCounter < madaTokens.size())
    				mTokenAdding += normalize(madaTokens.get(m + mCounter).getTok());
    			else
    				break;
			}
    		if (sCurrent.equals(mCurrent)) {
    			for (int i = 0; i < mCounter; i++) {
					madaTokens.get(m).setIndices(start, end);
					m++;
				}
    			s++;
    			continue;
			}

			while (mCurrent.contains(sTokenAdding)) {
				sCounter++;
				sCurrent = sTokenAdding;
				if (s + sCounter < stanfordWords.size())
					sTokenAdding += normalize(stanfordWords.get(s + sCounter).word());
				else
					break;
			}
			if (mCurrent.equals(sCurrent)) {
				madaTokens.get(m).setIndices(start, stanfordWords.get(s += sCounter).endPosition());
				m++;
				continue;
			}

			String mNext = "";
			String mNextNext = "";
			if (m < madaTokens.size() - 1)
				mNext = normalize(madaTokens.get(m + 1).getTok());
			if (m < madaTokens.size() - 2)
				mNextNext = normalize(madaTokens.get(m + 2).getTok());

			String sNext = "";
			String sNextNext = "";
			if (s < stanfordWords.size() - 1)
				sNext = normalize(stanfordWords.get(s + 1).word());
			if (s < stanfordWords.size() - 2)
				sNextNext = normalize(stanfordWords.get(s + 2).word());

			int min = getMinimumPenalty(mCurrent + mNext + mNextNext, sCurrent);
			String minType = "mnns";
			int snnm = getMinimumPenalty(sCurrent + sNext + sNextNext, mCurrent);
			if (snnm < min) {
				min = snnm;
				minType = "snnm";
			}
			int mns = getMinimumPenalty(mCurrent + mNext, sCurrent);
			if (mns < min) {
				min = mns;
				minType = "mns";
			}
			int snm = getMinimumPenalty(sCurrent + sNext, mCurrent);
			if (snm < min) {
				min = snm;
				minType = "snm";
			}
			int ms = getMinimumPenalty(mCurrent, sCurrent);
			if (ms < min) {
				min = ms;
				minType = "ms";
			}
			if (min < 4) {
				switch (minType) {
					case "mnns":
						madaTokens.get(m).setIndices(start, end);
						if (++m < madaTokens.size())
							madaTokens.get(m).setIndices(start, end);
						if (++m < madaTokens.size())
							madaTokens.get(m).setIndices(start, end);
						break;
					case "snnm":
						if (++s < madaTokens.size())
							madaTokens.get(m).setIndices(start, stanfordWords.get(s).endPosition());
						break;
					case "mns":
						madaTokens.get(m).setIndices(start, end);
						if (++m < madaTokens.size())
							madaTokens.get(m).setIndices(start, end);
						break;
					case "snm":
						if (++s < stanfordWords.size())
							madaTokens.get(m).setIndices(start, stanfordWords.get(s).endPosition());
						break;
					case "ms":
						madaTokens.get(m).setIndices(start, end);
						break;
				}
				m++;
				s++;
			}
			else {
				Boolean matches = false;
				for (int i = s; i < stanfordWords.size(); i++) {
					for (int j = m; j < madaTokens.size(); j++) {
						if (matches(madaTokens, stanfordWords, j, i)) {
							matches = true;
							m = j;
							break;
						}
					}
					if (matches) {
						s = i;
						break;
					}
				}
			}
		}
	}

	private Boolean matches(List<MadaToken> madaTokens, List<CoreLabel> stanfordWords, int m, int s) {
    	String sCurrent = normalize(stanfordWords.get(s).word());
		String sTokenAdding = sCurrent;
		int sCounter = 0;

		String mCurrent = normalize(madaTokens.get(m).getTok());
		String mTokenAdding = mCurrent;
		int mCounter = 0;

		while (sCurrent.contains(mTokenAdding)) {
			mCounter++;
			mCurrent = mTokenAdding;
			if (m + mCounter < madaTokens.size())
				mTokenAdding += normalize(madaTokens.get(m + mCounter).getTok());
			else
				break;
		}
		if (sCurrent.equals(mCurrent))
			return true;

		while (mCurrent.contains(sTokenAdding)) {
			sCounter++;
			sCurrent = sTokenAdding;
			if (s + sCounter < stanfordWords.size())
				sTokenAdding += normalize(stanfordWords.get(s + sCounter).word());
			else
				break;
		}
		if (mCurrent.equals(sCurrent))
			return true;

		String mNext = "";
		String mNextNext = "";
		if (m < madaTokens.size() - 1)
			mNext = normalize(madaTokens.get(m + 1).getTok());
		if (m < madaTokens.size() - 2)
			mNextNext = normalize(madaTokens.get(m + 2).getTok());

		String sNext = "";
		String sNextNext = "";
		if (s < stanfordWords.size() - 1)
			sNext = normalize(stanfordWords.get(s + 1).word());
		if (s < stanfordWords.size() - 2)
			sNextNext = normalize(stanfordWords.get(s + 2).word());

		int threshold = 4;
		int min = getMinimumPenalty(mCurrent + mNext + mNextNext, sCurrent);
		int snnm = getMinimumPenalty(sCurrent + sNext + sNextNext, mCurrent);
		if (snnm < min)
			min = snnm;
		int mns = getMinimumPenalty(mCurrent + mNext, sCurrent);
		if (mns < min)
			min = mns;
		int snm = getMinimumPenalty(sCurrent + sNext, mCurrent);
		if (snm < min)
			min = snm;
		int ms = getMinimumPenalty(mCurrent, sCurrent);
		if (ms < min)
			min = ms;
		if (min < threshold)
			return true;
		return false;
	}

    private void inspectText(/*Tree tree,*/ List<MadaToken> madaTokens, List<CoreLabel> stanfordWords /*, Collection<TypedDependency> deps*/) {
		if (madaTokens == null || madaTokens.isEmpty() || stanfordWords == null || stanfordWords.isEmpty())
			return;

		for (int i = 0; i < madaTokens.size(); i++) {
			MadaToken madaToken = madaTokens.get(i);
			int start = madaToken.getStartIndex();
			int end = madaToken.getEndIndex();
			if (start == -1 || end == -1)
				continue;

			Element morphFeatureSet = madaToken.getMorphFeatureSet();
			if (morphFeatureSet == null)
				continue;

			String BWwordTag = madaToken.getMorphFeatureSet().attr("bw");
			String BWtokTag = madaToken.getToken().attr("form5"); // Buckwalter tag

			String enc0 = morphFeatureSet.attr("enc0"); // enclictic (object and possesive pronouns)
			String cas = morphFeatureSet.attr("cas"); // nominative, accusative, genitive
			String stt = morphFeatureSet.attr("stt"); // definite, indefinite, construct
			String num = morphFeatureSet.attr("num"); // singular, dual, plural
			String gen = morphFeatureSet.attr("gen"); // masculine, feminine
			String mod = morphFeatureSet.attr("mod"); // indicative, subjunctive, jussive
			String vox = morphFeatureSet.attr("vox"); // active, passive
			String asp = morphFeatureSet.attr("asp"); // perfective, imperfective, command/imperative
			String per = morphFeatureSet.attr("per"); // 1st, 2nd, 3rd
			String prc0 = morphFeatureSet.attr("prc0");
			String prc1 = morphFeatureSet.attr("prc1");
			String prc2 = morphFeatureSet.attr("prc2");
			String prc3 = morphFeatureSet.attr("prc3");
			String pos = morphFeatureSet.attr("pos"); // part of speech

			//VERBS
			if (pos.equals("verb")) {
				if (asp.equals("p"))
					addConstructionByIndices(GrammaticalConstruction.ASPECT_PERFECTIVE, start, end);
				else if (asp.equals("i"))
					addConstructionByIndices(GrammaticalConstruction.ASPECT_IMPERFECTIVE, start, end);
				if (mod.equals("i"))
					addConstructionByIndices(GrammaticalConstruction.MOOD_INDICATIVE, start, end);
				else if (mod.equals("s"))
					addConstructionByIndices(GrammaticalConstruction.MOOD_SUBJUNCTIVE, start, end);
				else if (mod.equals("j"))
					addConstructionByIndices(GrammaticalConstruction.MOOD_JUSSIVE, start, end);
				else if (asp.equals("c"))
					addConstructionByIndices(GrammaticalConstruction.MOOD_IMPERATIVE, start, end);
				if (num.equals("p") && gen.equals("f"))
					addConstructionByIndices(GrammaticalConstruction.VERB_FEM_PL, start, end);
				else if (num.equals("d"))
					addConstructionByIndices(GrammaticalConstruction.VERB_DUAL, start, end);
			}

			//NOUNS
			if (pos.contains("noun") || pos.contains("adj")) {
				if (cas.equals("n"))
					addConstructionByIndices(GrammaticalConstruction.CASE_NOMINATIVE, start, end);
				else if (cas.equals("a"))
					addConstructionByIndices(GrammaticalConstruction.CASE_ACCUSATIVE, start, end);
				else if (cas.equals("g"))
					addConstructionByIndices(GrammaticalConstruction.CASE_GENITIVE, start, end);
				if (BWwordTag.contains("NSUFF_MASC_PL"))
						addConstructionByIndices(GrammaticalConstruction.NOUN_PL_MASC, start, end);
				else if (BWwordTag.contains("NSUFF_FEM_PL"))
						addConstructionByIndices(GrammaticalConstruction.NOUN_PL_FEM, start, end);
				else if (num.equals("d"))
					addConstructionByIndices(GrammaticalConstruction.NOUN_DUAL, start, end);

				if(stt.equals("c")) {
					int new_i = i;
					String new_stt = stt;

					while (new_stt.equals("c")) {
						if (++new_i < madaTokens.size())
							new_stt = madaTokens.get(new_i).getMorphFeatureSet().attr("stt");
					}

					String new_prc0 = madaTokens.get(new_i).getMorphFeatureSet().attr("prc0");
					String new_prc1 = madaTokens.get(new_i).getMorphFeatureSet().attr("prc1");
					String new_prc2 = madaTokens.get(new_i).getMorphFeatureSet().attr("prc2");
					String new_prc3 = madaTokens.get(new_i).getMorphFeatureSet().attr("prc3");
					Boolean valid_prc0 = new_prc0.equals("0") || new_prc0.equals("na") || new_prc0.equals("Al_det");
					Boolean no_prc1 = new_prc1.equalsIgnoreCase("0") || new_prc1.equals("na");
					Boolean no_prc2 = new_prc2.equalsIgnoreCase("0") || new_prc1.equals("na");
					Boolean no_prc3 = new_prc3.equalsIgnoreCase("0") || new_prc1.equals("na");

					if (valid_prc0 && no_prc1 && no_prc2 && no_prc3)
						end = madaTokens.get(new_i).getEndIndex();
					else
						end = madaTokens.get(--new_i).getEndIndex();

					addConstructionByIndices(GrammaticalConstruction.CONSTRUCT_PHRASE, start, end);
				}
			}

			if (BWtokTag.contains("PREP")) {
				addConstructionByIndices(GrammaticalConstruction.PREPOSITIONS, start, end);
			}
			/*if (BWtag.contains("PRON")) {
				addConstructionByIndices(GrammaticalConstruction.PRONOUNS, start, end);
			}*/
		}

		for (CoreLabel word: stanfordWords) {
			String pos = word.tag();
			if(pos.equals("VBG")) {
				addConstructionByIndices(GrammaticalConstruction.VERBAL_NOUN, word.beginPosition(), word.endPosition());
			}
		}
	}

	private void addConstructionByIndices(GrammaticalConstruction type, int startIndex, int endIndex){
		workingDoc.getConstructionData(type).addOccurrence(startIndex, endIndex);
	}

	static int getMinimumPenalty(String x, String y)
	{
		int SUB = 5;
		int INDEL = 1;
		int i, j; // intialising variables

		int m = x.length(); // length of token 1
		int n = y.length(); // length of token 2

		// table for storing optimal
		// substructure answers
		int dp[][] = new int[n + m + 1][n + m + 1];

		for (int[] x1 : dp)
			Arrays.fill(x1, 0);

		// intialising the table
		for (i = 0; i <= (n + m); i++)
		{
			dp[i][0] = i * INDEL;
			dp[0][i] = i * INDEL;
		}

		// calcuting the
		// minimum penalty
		for (i = 1; i <= m; i++)
		{
			for (j = 1; j <= n; j++)
			{
				char xChar = x.charAt(i - 1);
				char yChar = y.charAt(j - 1);

				Boolean ta = (xChar == 'ت' && yChar == 'ة') || (xChar == 'ة' && yChar == 'ت');
				Boolean alef = (xChar == 'ا' && yChar == 'أ') || (xChar == 'أ' && yChar == 'ا');
				Boolean waw = (xChar == 'و' && yChar == 'ؤ') || (xChar == 'ؤ' && yChar == 'و');
				Boolean ya = (xChar == 'ي' && (yChar == 'ئ' || yChar == 'ى')) ||
								(xChar == 'ى' && (yChar == 'ئ'|| yChar == 'ي')) ||
								(xChar == 'ئ' && (yChar == 'ي' || yChar == 'ى'));

				if (xChar == yChar || ta || alef || waw || ya)
				{
					dp[i][j] = dp[i - 1][j - 1];
				}
				else
				{
					dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + SUB ,
							dp[i - 1][j] + INDEL) ,
							dp[i][j - 1] + INDEL );
				}
			}
		}

		/*for(int q = 0; q < m; q++ ) {
			for (int r = 0; r < n; r++) {
				System.out.print(dp[q][r] + " ");
			}
			System.out.print("\n");
		}
		System.out.println("-------------------------------------");*/
		return dp[m][n];
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

class MadaToken {
	private Element token;
	private Element morphFeatureSet = null;
	private String word;
	private String tok;
	private int wordID;
	private int startIndex = -1;
	private int endIndex = -1;

	MadaToken(Element token, Element morphFeatureSet, String word, int wordID) {
		this.token = token;
		tok = token.attr("form0")
				.replaceAll("\\+", "")
				.replaceAll("-LRB-", "(")
				.replaceAll("-RRB-", ")");
		this.morphFeatureSet = morphFeatureSet;
		this.word = word;
		this.wordID = wordID;
	}

	public Element getToken() {
		return token;
	}

	public Element getMorphFeatureSet() {
		return morphFeatureSet;
	}

	public String getTok() { return tok; }

	public String getWord() { return word; }

	public int getWordID() { return wordID; }

	public void setToken(Element token) {
		this.token = token;
	}

	public void setMorphFeatureSet(Element morphFeatureSet) {
		this.morphFeatureSet = morphFeatureSet;
	}

	public void setWord(String word) { this.word = word; }

	public void setWordID(int wordID) { this.wordID = wordID; }

	public void setIndices(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

}