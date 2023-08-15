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
		ServerLogger.get().info("OCCURRENCE: " + type + ": " + start + "-" + end);
		workingDoc.getConstructionData(type).addOccurrence(start, end);
	}

	private void inspectSentence(List<StanzaToken> sent) {
		if (sent == null || sent.isEmpty()) {
			return;
		}

		Integer intId; // cast id to Integer
		Integer tokStart;
		Integer tokEnd;

		// Handle multi-word tokens
		Integer mwStart = null;
		Integer mwEnd = null;
		Integer mwLastId = null;

		for (StanzaToken token : sent) {
			// All logic for adding grammatical constructions goes in this loop.
			// To keep code organized, you may create other functions to call inside this
			// function

			if (token.getId().contains("-")) { // Multi-word token
				mwStart = token.getStart();
				mwEnd = token.getEnd();
				mwLastId = Integer.parseInt(token.getId().substring(token.getId().lastIndexOf('-') + 1));
				ServerLogger.get()
						.info("MWTOKEN: " + token.toString() + "\t" + mwStart + " - " + mwEnd + " last:\t" + mwLastId);
				continue;
			} else {
				intId = Integer.parseInt(token.getId());
				if (mwLastId != null) {
					if (intId > mwLastId) {
						mwStart = null;
						mwEnd = null;
						mwLastId = null;
						tokStart = token.getStart();
						tokEnd = token.getEnd();
					} else {
						tokStart = mwStart;
						tokEnd = mwEnd;
					}
				} else {
					tokStart = token.getStart();
					tokEnd = token.getEnd();
				}
			}

			String lemma = token.getLemma();
			String upos = token.getUpos();
			String xpos = token.getXpos();
			ArrayList<String> feats = token.getFeats();

			ServerLogger.get()
					.info("TOKEN: " + token.toString() + " " + lemma + " / " + upos + " / " + feats.toString());

			if (lemma != null) {
				// lookup grammatical constructions mapped to this lemma
				GrammaticalConstruction[] lemmaConstructions = PersianGrammaticalPatterns.lemmaMap.get(lemma);
				if ( lemmaConstructions != null ) {
					for (GrammaticalConstruction c : lemmaConstructions) {
						addConstructionOccurrence(c, tokStart, tokEnd);
					}
				}
			}

			/////////////////////////////////
			// Constructions based on upos //
			/////////////////////////////////

			ServerLogger.get().info("UPOS: " + upos + " (" + token.toString() + ")");
			if (upos != null) {
				ServerLogger.get().info("BYTES: " + upos.getBytes() + " (" + token.toString() + upos + ")");
				switch (upos) {
					case "ADJ":
						if (xpos != null && xpos.equals("ADJ_INO")) {
							addConstructionOccurrence(GrammaticalConstruction.VERBFORM_ADJ, tokStart, tokEnd);
						}
						break;
					case "ADP":
						addConstructionOccurrence(GrammaticalConstruction.PREPOSITIONS, tokStart, tokEnd);
						break;
					case "ADV":
						addConstructionOccurrence(GrammaticalConstruction.ADVERB_PERSIAN, tokStart, tokEnd);
						if (feats != null) {
							if (feats.contains("Neg")) {
								addConstructionOccurrence(GrammaticalConstruction.ADVERB_NEGATIVE, tokStart,
										tokEnd);
							}
							if (feats.contains("Loc")) {
								addConstructionOccurrence(GrammaticalConstruction.ADVERB_LOCATIONAL, tokStart,
										tokEnd);
							}
							if (feats.contains("Tem")) {
								addConstructionOccurrence(GrammaticalConstruction.ADVERB_TEMPORAL, tokStart,
										tokEnd);
							}
						}
						break;
					case "AUX":
						addConstructionOccurrence(GrammaticalConstruction.VERBFORM_AUX, tokStart, tokEnd);
						if (!feats.isEmpty()) {
							if (feats.contains("Sing")) {
								addConstructionOccurrence(GrammaticalConstruction.SINGULAR_VERB_PERSIAN, tokStart,
										tokEnd);
							}
							if (feats.contains("Plur")) {
								addConstructionOccurrence(GrammaticalConstruction.PLURAL_VERB_PERSIAN, tokStart,
										tokEnd);
							}
						}
						break;
					case "CCONJ":
						addConstructionOccurrence(GrammaticalConstruction.SENTENCE_COMPOUND, tokStart, tokEnd);
						break;
					case "DET":
						addConstructionOccurrence(GrammaticalConstruction.DETERMINER_OTHER, tokStart, tokEnd);
						break;
					case "NOUN":
						if (!feats.isEmpty()) {
							if (feats.contains("Sing")) {
								addConstructionOccurrence(GrammaticalConstruction.SINGULAR_PERSIAN, tokStart, tokEnd);
							}
							if (feats.contains("Plur")) {
								addConstructionOccurrence(GrammaticalConstruction.PLURAL_PERSIAN, tokStart, tokEnd);
							}
						}
						break;
					case "PRON":
						if (!feats.isEmpty()) {
							if (feats.contains("1") || feats.contains("2") || feats.contains("3")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_PERSONAL, tokStart, tokEnd);
							}
							if (feats.contains("Dem")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_DEMONSTRATIVE, tokStart,
										tokEnd);
							}
							if (feats.contains("Ind")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_INDEFINITE, tokStart,
										tokEnd);
							}
							if (feats.contains("Int")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_INTERROGATIVE, tokStart,
										tokEnd);
							}
							if (feats.contains("Neg")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_NEGATIVE, tokStart,
										tokEnd);
							}
							if (feats.contains("Rcp")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_RECIPROCAL, tokStart,
										tokEnd);
							}
							if (feats.contains("Rel")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_RELATIVE, tokStart,
										tokEnd);
								addConstructionOccurrence(GrammaticalConstruction.CLAUSE_RELATIVE, tokStart,
										tokEnd);
							}
							if (feats.contains("Tot")) {
								addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_TOT, tokStart,
										tokEnd);
							}
						}
						break;
					case "SCONJ":
						addConstructionOccurrence(GrammaticalConstruction.SENTENCE_COMPLEX, tokStart, tokEnd);
						addConstructionOccurrence(GrammaticalConstruction.CLAUSE_SUBORDINATE, tokStart, tokEnd);
						break;
					case "VERB":
						if (feats != null) {
							if (feats.contains("1")) {
								addConstructionOccurrence(GrammaticalConstruction.FIRST_PERSON_PERSIAN, tokStart,
										tokEnd);
							}
							if (feats.contains("2")) {
								addConstructionOccurrence(GrammaticalConstruction.SECOND_PERSON_PERSIAN, tokStart,
										tokEnd);
							}
							if (feats.contains("3")) {
								addConstructionOccurrence(GrammaticalConstruction.THIRD_PERSON_PERSIAN, tokStart,
										tokEnd);
							}
							if (feats.contains("Neg")) {
								addConstructionOccurrence(GrammaticalConstruction.VERBFORM_NEG, tokStart, tokEnd);
							}
							if (feats.contains("Plur")) {
								addConstructionOccurrence(GrammaticalConstruction.PLURAL_VERB_PERSIAN, tokStart,
										tokEnd);
							}
							if (feats.contains("Sing")) {
								addConstructionOccurrence(GrammaticalConstruction.SINGULAR_VERB_PERSIAN, tokStart,
										tokEnd);
							}
						}
						break;
					default:
						ServerLogger.get().info("UPOS SKIPPED: " + token.toString() + upos);
						break;
				}
			} else {
				ServerLogger.get().info("UPOS NULL: " + token.toString());
			}

			//////////////////////////////////
			// Constructions based on feats //
			//////////////////////////////////

			if (!feats.isEmpty()) {
				boolean debugFeats = false;
				if (feats.contains("Imp")) {
					addConstructionOccurrence(GrammaticalConstruction.MOOD_IMP, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Sub")) {
					addConstructionOccurrence(GrammaticalConstruction.MOOD_SUB, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Part")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_PRESENT_PARTICIPLE_PERSIAN, tokStart,
							tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Inf")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_INFINITIVE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Fin")) {
					addConstructionOccurrence(GrammaticalConstruction.VERBFORM_FINITE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Past")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_PAST_SIMPLE, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Pres")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_PRESENT_SIMPLE, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Fut")) {
					addConstructionOccurrence(GrammaticalConstruction.TENSE_FUTURE_SIMPLE, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Card")) {
					addConstructionOccurrence(GrammaticalConstruction.NUMBERS_CARDINAL_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Ord")) {
					addConstructionOccurrence(GrammaticalConstruction.NUMBERS_ORDINAL_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Reflex")) {
					addConstructionOccurrence(GrammaticalConstruction.PRONOUNS_REFLEXIVE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Pos")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_POSITIVE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Cmp")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_COMPARATIVE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if (feats.contains("Sup")) {
					addConstructionOccurrence(GrammaticalConstruction.ADJECTIVE_SUPERLATIVE_PERSIAN, tokStart, tokEnd);
					debugFeats = true;
				}
				if ( !debugFeats ) {
					ServerLogger.get().info("FEATS SKIPPED: " + token.toString() + feats.toString());
				}
				// TODO add more feats-based constructions here
			} else {
				ServerLogger.get().info("FEATS EMPTY: " + token.toString());
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
