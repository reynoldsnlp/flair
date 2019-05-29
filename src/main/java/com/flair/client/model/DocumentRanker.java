package com.flair.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flair.client.model.interfaces.AbstractDocumentRanker;
import com.flair.client.model.interfaces.DocumentRankerInput;
import com.flair.client.model.interfaces.DocumentRankerOutput;
import com.flair.client.utilities.ClientLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.interop.RankableDocument;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;
import com.flair.shared.parser.DocumentReadabilityLevel;

/*
 * Implements language agnostic ranking functionality
 */
public class DocumentRanker implements AbstractDocumentRanker
{
	/**
	 * private data class that keeps track of a documents weight information
	 */
	private static class WeightData
	{
		public final double		weight;
		public double			df;		//document frequency
		public double			idf;

		public WeightData(double w)
		{
			ClientLogger.get().info("Creating WeightData object of weight " + w);
			weight = w;
			df = idf = 0;
		}
	}

	private static class RankerWeights
	{
		public final WeightData			docLevelA;
		public final WeightData			docLevelB;
		public final WeightData			docLevelC;
		public final WeightData			docLevel1;
		public final WeightData			docLevel2;
		public final WeightData			docLevel3;
		public final WeightData			docLevel4;
		public final WeightData			keywords;
		public final Map<GrammaticalConstruction, WeightData> 	gram;
		private Language 				rankerLanguage;

		public RankerWeights(DocumentRankerInput.Rank input)
		{
			ClientLogger.get().info("initializing RankerWeights");
			double maxWeight = input.getMaxWeight();

			rankerLanguage = input.getLanguage();

			

			if(rankerLanguage.toString().equals("ARABIC")) {
				ClientLogger.get().info("initializing arabic doc levels");
				ClientLogger.get().info("docLevel1");
				docLevel1 = new WeightData(input.isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel.LEVEL_1) ? 1 : 0);
				ClientLogger.get().info("docLevel2");
				docLevel2 = new WeightData(input.isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel.LEVEL_2) ? 1 : 0);
				ClientLogger.get().info("docLevel3");
				docLevel3 = new WeightData(input.isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel.LEVEL_3) ? 1 : 0);
				ClientLogger.get().info("docLevel4");
				docLevel4 = new WeightData(input.isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel.LEVEL_4) ? 1 : 0);
				docLevelA = null;
				docLevelB = null;
				docLevelC = null;
			}
			else {
				ClientLogger.get().info("arabic doc levels are null, language is " + rankerLanguage.toString());
				docLevel1 = null;
				docLevel2 = null;
				docLevel3 = null;
				docLevel4 = null;
				docLevelA = new WeightData(input.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_A) ? 1 : 0);
				docLevelB = new WeightData(input.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_B) ? 1 : 0);
				docLevelC = new WeightData(input.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_C) ? 1 : 0);
			}
			
			keywords = new WeightData(input.getKeywordWeight() / maxWeight);

			gram = new EnumMap<>(GrammaticalConstruction.class);
			for (GrammaticalConstruction itr : input.getConstructions())
				gram.put(itr, new WeightData(input.getConstructionWeight(itr) / maxWeight));

		}

		private WeightData getDocLevelWeight(RankableDocument doc)
		{
			switch (doc.getReadabilityLevel())
			{
			case LEVEL_A:
				ClientLogger.get().info("Got docLevelA");
				return docLevelA;
			case LEVEL_B:
				ClientLogger.get().info("Got docLevelB");
				return docLevelB;
			case LEVEL_C:
				ClientLogger.get().info("Got docLevelC");
				return docLevelC;
			default:
				return null;
			}
		}

		private WeightData getArabicDocLevelWeight(RankableDocument doc)
		{
			switch (doc.getArabicReadabilityLevel())
			{
			case LEVEL_1:
				ClientLogger.get().info("Got docLevel1");
				return docLevel1;
			case LEVEL_2:
				ClientLogger.get().info("Got docLevel2");
				return docLevel2;
			case LEVEL_3:
				ClientLogger.get().info("Got docLevel3");
				return docLevel3;
			case LEVEL_4:
				ClientLogger.get().info("Got docLevel4");
				return docLevel4;
			default:
				return null;
			}
		}

		public boolean isDocLevelFiltered(RankableDocument doc) {
			ClientLogger.get().info("isDocLevelFiltered");
			if(docLevel1 != null)
				return getArabicDocLevelWeight(doc).weight == 0;
			else
				return getDocLevelWeight(doc).weight == 0;
		}

		private void incrementDocLevelDf(RankableDocument doc) {
			if(docLevel1 != null){
				ClientLogger.get().info("incrementDocLevelDf for " + doc.getArabicReadabilityLevel());
				getArabicDocLevelWeight(doc).df += 1;
			}
			else {
				ClientLogger.get().info("incrementDocLevelDf for " + doc.getReadabilityLevel());
				getDocLevelWeight(doc).df += 1;
			}
		}

		private void incrementGramDf(RankableDocument doc)
		{
			for (GrammaticalConstruction itr : doc.getConstructions()) {
				gram.get(itr).df += 1;
			}
		}

		private void incrementKeywordDf(RankableDocument doc)
		{
			if (doc.getKeywordCount() > 0)
				keywords.df += 1;
		}

		private void calcIdf(WeightData w, double docCount)
		{
			ClientLogger.get().info("calcIdf");
			if (w.df != 0)
				w.idf = Math.log((docCount) / w.df);
			else
				w.idf = 0;
		}

		public void updateDfIdf(Collection<RankableDocument> docs)
		{
			for (RankableDocument doc : docs)
			{
				// update the df scores of all weights
				incrementDocLevelDf(doc);
				incrementKeywordDf(doc);
				incrementGramDf(doc);
			}

			// update idfs
			int docCount = docs.size() + 1;

			if(rankerLanguage.toString().equals("ARABIC")) {
				calcIdf(docLevel1, docCount);
				calcIdf(docLevel2, docCount);
				calcIdf(docLevel3, docCount);
				calcIdf(docLevel4, docCount);
			}
			else {
				calcIdf(docLevelA, docCount);
				calcIdf(docLevelB, docCount);
				calcIdf(docLevelC, docCount);
			}

			calcIdf(keywords, docCount);

			for (WeightData itr : gram.values())
				calcIdf(itr, docCount);
		}

		public WeightData getConstructionWeight(GrammaticalConstruction val) {
			return gram.get(val);
		}
	}

	private static class RankerScores
	{
		class Score
		{
			public double s;

			Score() {
				s = 0d;
			}
		}

		public final Map<RankableDocument, Score>	scores;

		public RankerScores(Iterable<RankableDocument> input)
		{
			scores = new HashMap<>();

			for (RankableDocument itr : input)
				scores.put(itr, new Score());
		}
	}

	private static class RankOperationOutput implements DocumentRankerOutput.Rank
	{
		public final Language					lang;
		public final List<RankableDocument>		docs;
		public final RankerWeights				weights;
		public int								numFiltered;

		RankOperationOutput(Language l, List<RankableDocument> d, RankerWeights w)
		{
			lang = l;
			docs = d;
			weights = w;
			numFiltered = 0;
		}

		@Override
		public Collection<RankableDocument> getRankedDocuments() {
			return docs;
		}

		@Override
		public double getDocLevelDf(DocumentReadabilityLevel level)
		{
			switch (level)
			{
			case LEVEL_A:
				ClientLogger.get().info("for doc level A, df is " + weights.docLevelA.df);
				ClientLogger.get().info("idf is " + weights.docLevelA.idf);
				return weights.docLevelA.df;
			case LEVEL_B:
				ClientLogger.get().info("for doc level B, df is " + weights.docLevelB.df);
				ClientLogger.get().info("idf is " + weights.docLevelB.idf);
				return weights.docLevelB.df;
			case LEVEL_C:
				ClientLogger.get().info("for doc level C, df is " + weights.docLevelC.df);
				ClientLogger.get().info("idf is " + weights.docLevelC.idf);
				return weights.docLevelC.df;
			default:
				return 0;
			}
		}

		@Override
		public double getArabicDocLevelDf(ArabicDocumentReadabilityLevel level)
		{
			ClientLogger.get().info("getArabicDocLevelDf()");
			switch (level)
			{
			case LEVEL_1:
				if(weights.docLevel1 == null)
					return 0;
				ClientLogger.get().info("for doc level 1, df is " + weights.docLevel1.df);
				//ClientLogger.get().info("idf is " + weights.docLevelA.idf);
				return weights.docLevel1.df;
			case LEVEL_2:
				if(weights.docLevel2 == null)
					return 0;
				ClientLogger.get().info("for doc level 2, df is " + weights.docLevel2.df);
				//ClientLogger.get().info("idf is " + weights.docLevelB.idf);
				return weights.docLevel2.df;
			case LEVEL_3:
				if(weights.docLevel3 == null)
					return 0;
				ClientLogger.get().info("for doc level 3, df is " + weights.docLevel3.df);
				//ClientLogger.get().info("idf is " + weights.docLevelC.idf);
				return weights.docLevel3.df;
			case LEVEL_4:
				if(weights.docLevel4 == null)
					return 0;
				ClientLogger.get().info("for doc level 4, df is " + weights.docLevel4.df);
				//ClientLogger.get().info("idf is " + weights.docLevelC.idf);
				return weights.docLevel4.df;
			default:
				return 0;
			}
		}

		@Override
		public double getConstructionDf(GrammaticalConstruction gram)
		{
			WeightData w = weights.getConstructionWeight(gram);
			if (w == null)
				return 0;
			else
				return w.df;
		}

		@Override
		public double getConstructionWeight(GrammaticalConstruction gram)
		{
			WeightData w = weights.getConstructionWeight(gram);
			if (w == null)
				return 0;
			else
				return w.weight;
		}

		@Override
		public double getKeywordWeight() {
			return weights.keywords.weight;
		}

		@Override
		public boolean isConstructionWeighted(GrammaticalConstruction gram) {
			return getConstructionWeight(gram) != 0;
		}

		@Override
		public boolean isKeywordWeighted() {
			return weights.keywords.weight != 0;
		}
		
		@Override
		public int getNumFilteredDocuments() {
			return numFiltered;
		}
	
		@Override
		public Language getLanguage() {
			return lang;
		}
	}

	private static final double			LENGTH_PARAM_MULTIPLIER = 10;		// ### what's this?
	private static final double			TF_NORM_MULTIPLIER = 1.7;			// ### what's this?


	private boolean isDocConstructionFiltered(DocumentRankerInput.Rank input, RankableDocument doc)
	{
		for (GrammaticalConstruction itr : doc.getConstructions())
		{
			if (input.hasConstructionSlider(itr) && input.isConstructionEnabled(itr) == false)
				return true;
		}

		return false;
	}

	@Override
	public DocumentRankerOutput.Rank rerank(DocumentRankerInput.Rank input)
	{
		ClientLogger.get().info("DocumentRankerOutput.Rank rerank()");
		RankerWeights weights = new RankerWeights(input);
		RankOperationOutput out = new RankOperationOutput(input.getLanguage(), new ArrayList<>(), weights);

		double lengthParam = input.getDocLengthWeight() / LENGTH_PARAM_MULTIPLIER;
		double avDocLenAccum = -1;

		// perform filtering
		for (RankableDocument itr : input.getDocuments())
		{
			if (input.isDocumentFiltered(itr) ||
				weights.isDocLevelFiltered(itr) ||
				isDocConstructionFiltered(input, itr))
			{
				out.numFiltered++;
				ClientLogger.get().info("number of filtered documents " + out.numFiltered);
				continue;
			}

			out.docs.add(itr);
		}

		// update df+idf of weights
		RankerScores scores = new RankerScores(out.docs);
		weights.updateDfIdf(out.docs);

		// update avg. doc length
		for (RankableDocument itr : out.docs)
			avDocLenAccum += itr.getNumWords();

		if (out.docs.size() > 0)
			avDocLenAccum /= out.docs.size();
		else
			avDocLenAccum = 0;

		// calculate scores
		final double avgDocLen = avDocLenAccum;
		class BoolWrapper {
			boolean b;

			public BoolWrapper(boolean b) {
				this.b = b;
			}
		}
		final BoolWrapper hasGramScore = new BoolWrapper(weights.keywords.weight != 0);
		for (RankableDocument itr : out.docs)
		{
			RankerScores.Score score = scores.scores.get(itr);
			// accumulate gram construction score
			weights.gram.forEach((g, w) -> {
				if (w.weight != 0 && w.df > 0)
				{
					hasGramScore.b = true;
					if (itr.hasConstruction(g))
					{
						double tf = itr.getConstructionFreq(g);
						double idf = w.idf;
						double tfNorm = ((TF_NORM_MULTIPLIER + 1) * tf) / (tf + TF_NORM_MULTIPLIER * (1 - lengthParam + lengthParam * (itr.getNumWords() / avgDocLen)));
						double gramScore = tfNorm * idf;

						score.s += gramScore * w.weight;
					}
				}
			});

			// accumulate keyword score
			{
				double tf = itr.getKeywordCount();
				double idf = weights.keywords.idf;
				double tfNorm = ((TF_NORM_MULTIPLIER + 1) * tf) / (tf + TF_NORM_MULTIPLIER * (1 - lengthParam + lengthParam * (itr.getNumWords() / avgDocLen)));
				double gramScore = tfNorm * idf;

				score.s += gramScore * weights.keywords.weight;
			}
		}

		// sort docs by weight
		if (hasGramScore.b)
		{
			Collections.sort(out.docs, (a, b) -> {
				return -Double.compare(scores.scores.get(a).s, scores.scores.get(b).s);
			});
		}
		else if (lengthParam != 0)
		{
			Collections.sort(out.docs, (a, b) -> {
				return Integer.compare((int)a.getNumWords(), (int)b.getNumWords());
			});
		}
		else
		{
			Collections.sort(out.docs, (a, b) -> {
				return Integer.compare(a.getRank(), b.getRank());
			});
		}

		return out;
	}
}
