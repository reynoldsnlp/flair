package com.flair.client.model.interfaces;

import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.interop.RankableDocument;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;
import com.flair.shared.parser.DocumentReadabilityLevel;

/*
 * Input data for the RankerLogic class
 */
public interface DocumentRankerInput
{
	public interface Rank
	{
		public Language								getLanguage();			// language of the data set
		public Iterable<GrammaticalConstruction>	getConstructions();		// language-specific grammatical constructions
		
		public double								getMaxWeight();			// used to normalize weight settings
		
		public double								getConstructionWeight(GrammaticalConstruction gram);	// returns the weight of the given construction
		public boolean								isConstructionEnabled(GrammaticalConstruction gram);	// returns true if enabled
		public boolean								hasConstructionSlider(GrammaticalConstruction gram);	// returns false if the construction has no corresponding slider in the interface
		
		public double								getKeywordWeight();
		public boolean								isKeywordEnabled();
		
		public double								getDocLengthWeight();
		public boolean								isDocLevelEnabled(DocumentReadabilityLevel level);		//This detirmines whether or not we display the documents with the corresponding doc level
		public boolean								isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel level);		//This detirmines whether or not we display the documents with the corresponding doc level

		public Iterable<RankableDocument>			getDocuments();
		public boolean								isDocumentFiltered(RankableDocument doc);	// returns true if the document is to be excluded from the result
	}
}
