/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.server.grammar.PersianGrammaticalPatterns;
import com.flair.server.stanza.StanzaToken;
import com.flair.server.utilities.ServerLogger;

/**
 * Implementation of Persian language parsing logic for the Stanza parser
 *
 * @author Rob
 * @author Evan
 */

class StanzaDocumentParserPersianStrategy extends BasicStanzaDocumentParserStrategy {
	private AbstractDocument workingDoc;

	// private int dependencyCount; // count dependencies - correspond to token
	// count without punctuation
	private int wordCount; // count words (without numbers, symbols and punctuation)
	private int tokenCount; // count tokens (incl. numbers, symbols and punctuation)
	private int sentenceCount; // count sentences
	private int depthCount; // count tree depthCount
	private int characterCount; // count characters in words

	public StanzaDocumentParserPersianStrategy() {
		workingDoc = null;
		wordCount = tokenCount = sentenceCount = depthCount = characterCount = 0; // = dependencyCount
	}

	private void initializeState(AbstractDocument doc) {
		if (pipeline == null) {
			throw new IllegalStateException("Parser not set");
		} else if (isLanguageSupported(doc.getLanguage()) == false) {
			throw new IllegalArgumentException("Document language " + doc.getLanguage()
					+ " not supported (Strategy language: " + Language.PERSIAN + ")");
		}

		workingDoc = doc;
	}

	private void resetState() {
		wordCount = tokenCount = sentenceCount = depthCount = characterCount = 0; // = dependencyCount
		pipeline = null;
		workingDoc = null;
	}

	private int countSubstr(String substr, String str) {
		// ###TODO can be made faster?
		// the result of split() will contain one more element than the delimiter
		// the "-1" second argument makes it not discard trailing empty strings
		return str.split(Pattern.quote(substr), -1).length - 1;
	}

	/**
	 * addConstructionOccurrence() is the function to record identified grammatical
	 * constructions
	 *
	 * @param type  The target GrammaticalConstruction
	 * @param start The index of the first character of the construction
	 *              (StanzaToken.getStart())
	 * @param end   The index of the last character of the construction
	 *              (StanzaToken.getEnd())
	 **/
	private void addConstructionOccurrence(GrammaticalConstruction type, int start, int end) {
		workingDoc.getConstructionData(type).addOccurrence(start, end);
	}

	private void inspectSentence(List<StanzaToken> sent) {
		if (sent == null || sent.isEmpty()) {
			return;
		}

		for (StanzaToken token : sent) {
			// All logic for adding grammatical constructions goes in this loop.
			// To keep code organized, you may create other functions to call inside this
			// function


			String lemma = token.getLemma();
			// String surface = token.getText();
			if (lemma != null) {

				// lookup grammatical constructions mapped to this lemma
				GrammaticalConstruction[] lemmaConstructions = PersianGrammaticalPatterns.lemmaMap.get(lemma);
				if (lemmaConstructions == null) {
					continue;
				}
				for (GrammaticalConstruction c : lemmaConstructions) {
					addConstructionOccurrence(c, token.getStart(), token.getEnd());
				}
			}

			// Constructions based on upos
			String upos = token.getUpos();
			if (upos != null) {
				if (upos == "ADJ") {
					String xpos = token.getXpos();
					if (xpos != null) {
						if (xpos == "ADJ_INO") {
							addConstructionOccurrence(GrammaticalConstruction.VERBFORM_ADJ, token.getStart(), token.getEnd());
						}
					}
				}
				if (upos == "ADP") {
					addConstructionOccurrence(GrammaticalConstruction.PREPOSITIONS, token.getStart(), token.getEnd());
				}
				if (upos == "ADV") {
					addConstructionOccurrence(GrammaticalConstruction.ADVERB_PERSIAN, token.getStart(), token.getEnd());
					ArrayList<String> feats = token.getFeats();
					if (feats != null) {
						if (feats.contains("Neg")) {
							addConstructionOccurrence(GrammaticalConstruction.ADVERB_NEGATIVE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Loc")) {
							addConstructionOccurrence(GrammaticalConstruction.ADVERB_LOCATIONAL, token.getStart(), token.getEnd());
						}
						if (feats.contains("Tem")) {
							addConstructionOccurrence(GrammaticalConstruction.ADVERB_TEMPORAL, token.getStart(), token.getEnd());
						}
					}
				}
				if (upos == "AUX") {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_AUX, token.getStart(), token.getEnd());
				}
				if (upos == "CCONJ") {
					addConstructionOccurrence(GrammaticalConstruction.SENTENCE_COMPOUND, token.getStart(), token.getEnd());
				}
				if (upos == "DET") {
					addConstructionOccurrence(GrammaticalConstruction.DETERMINER_OTHER, token.getStart(), token.getEnd());
				}
				if (upos == "PRON") {
					ArrayList<String> feats = token.getFeats();
					if (feats != null) {
						if (feats.contains("Dem")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_DEMONSTRATIVE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Ind")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_INDEFINITE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Int")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_INTERROGATIVE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Neg")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_NEGATIVE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Rcp")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_RECIPROCAL, token.getStart(), token.getEnd());
						}
						if (feats.contains("Rel")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_RELATIVE, token.getStart(), token.getEnd());
						}
						if (feats.contains("Tot")) {
							addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_TOT, token.getStart(), token.getEnd());
						}
					}
				}
				if (upos == "SCONJ") {
					addConstructionOccurrence(GrammaticalConstruction.SENTENCE_COMPLEX, token.getStart(), token.getEnd());
				}
				if (upos == "VERB") {
					ArrayList<String> feats = token.getFeats();
					if (feats != null) {
						if (feats.contains("Neg")) {
							addConstructionOccurrence(GrammaticalConstruction.VERBFORM_NEG, token.getStart(), token.getEnd());
						}
					}
				// TODO add more upos-pased constructions here
				}
			}
			// Constructions based on feats
			ArrayList<String> feats = token.getFeats();
			if (feats != null) {
				if (feats.contains("Sing")) {					
					if (upos == "AUX") {
					addConstructionOccurrence(GrammaticalConstruction.SINGULAR_VERB_PERSIAN, token.getStart(), token.getEnd());
					}
					if (upos == "NOUN") {
						addConstructionOccurrence(GrammaticalConstruction.SINGULAR_PERSIAN, token.getStart(), token.getEnd());
					}
					if (upos == "VERB") {
						addConstructionOccurrence(GrammaticalConstruction.SINGULAR_VERB_PERSIAN, token.getStart(), token.getEnd());
					}				
				}
				if (feats.contains("Mood=Imp")) {
					addConstructionOccurrence(GrammaticalConstruction.MOOD_IMP, token.getStart(), token.getEnd());
				}
				if (feats.contains("Mood=Sub")) {
					addConstructionOccurrence(GrammaticalConstruction.MOOD_SUB, token.getStart(), token.getEnd());
				}
				//possibly add check for null upos
				if (feats.contains("Person=1")) {
					if (upos == "VERB") {
						addConstructionOccurrence(GrammaticalConstruction.FIRST_PERSON_PERSIAN, token.getStart(), token.getEnd());
					}
					else if (upos == "PRON") {
						addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_PERSONAL, token.getStart(), token.getEnd());	
					}
				}
				if (feats.contains("Person=2")) {
					if (upos == "VERB") {
						addConstructionOccurrence(GrammaticalConstruction.SECOND_PERSON_PERSIAN, token.getStart(), token.getEnd());
					}
					else if (upos == "PRON") {
						addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_PERSONAL, token.getStart(), token.getEnd());	
					}
				}
				if (feats.contains("Person=3")) {
					if (upos == "VERB") {
						addConstructionOccurrence(GrammaticalConstruction.THIRD_PERSON_PERSIAN, token.getStart(), token.getEnd());
					}
					else if (upos == "PRON") {
						addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_PERSONAL, token.getStart(), token.getEnd());	
					}
				}
				if (feats.contains("VerbForm=Part")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_PRESENT_PARTICIPLE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("VerbForm=Inf")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_INFINITIVE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("VerbForm=Fin")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_FINITE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Tense=Past")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_PAST_SIMPLE, token.getStart(), token.getEnd());
				}
				if (feats.contains("Tense=Pres")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_PRESENT_SIMPLE, token.getStart(), token.getEnd());
				}
				if (feats.contains("Tense=Fut")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_FUTURE_SIMPLE, token.getStart(), token.getEnd());
				}
				if (feats.contains("Number=Plur")) {
					if (upos == "AUX") {
						addConstructionOccurrence(GrammaticalConstruction.PLURAL_VERB_PERSIAN, token.getStart(), token.getEnd());
					}
					if (upos == "NOUN") {
						addConstructionOccurrence(GrammaticalConstruction.PLURAL_PERSIAN, token.getStart(), token.getEnd());
					}
					if (upos == "VERB") {
						addConstructionOccurrence(GrammaticalConstruction.PLURAL_VERB_PERSIAN, token.getStart(), token.getEnd());
					}
				}
				if (feats.contains("Card")) {
					addConstructionOccurrence(GrammaticalConstruction.NUMBERS_CARDINAL_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Ord")) {
					addConstructionOccurrence(GrammaticalConstruction.NUMBERS_ORDINAL_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Reflex")) {
					addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_REFLEXIVE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Degree=Pos")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_POSITIVE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Degree=Cmp")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_COMPARATIVE_PERSIAN, token.getStart(), token.getEnd());
				}
				if (feats.contains("Degree=Sup")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_SUPERLATIVE_PERSIAN, token.getStart(), token.getEnd());
				}
				// TODO add more feats-based constructions here
			}
		}
	}

	@Override
	public boolean isLanguageSupported(Language lang) {
		return lang == Language.PERSIAN;
	}

	@Override
	public boolean apply(AbstractDocument docToParse) {

		assert docToParse != null;
		try {
			initializeState(docToParse);

			List<List<StanzaToken>> sentences = pipeline.process(docToParse, "fa");

			for (List<StanzaToken> sent : sentences) {

				if (sent.size() > 0) {
					sentenceCount++;

					// changed: only count words (no punctuation)
					for (StanzaToken token : sent) {
						tokenCount++;
						String tokenUpos = token.getUpos();
						if (tokenUpos == null) {
							continue;
						} else if (!tokenUpos.matches("PUNCT|SYM|X")) {
							wordCount++;
							characterCount += token.getText().length();
						}
					}

					// extract gram.structures
					inspectSentence(sent);
				}
			}

			// update doc properties
			workingDoc.setAvgSentenceLength((double) wordCount / (double) sentenceCount);
			workingDoc.setAvgTreeDepth((double) depthCount / (double) sentenceCount);
			workingDoc.setAvgWordLength((double) characterCount / (double) wordCount);
			workingDoc.setLength(wordCount);
			// workingDoc.setNumDependencies(dependencyCount);
			workingDoc.setNumWords(wordCount);
			workingDoc.setNumTokens(tokenCount);
			workingDoc.setNumSentences(sentenceCount);
			workingDoc.setNumCharacters(characterCount);
			workingDoc.flagAsParsed();
		} finally {
			resetState();
		}

		return true;
	}
}
