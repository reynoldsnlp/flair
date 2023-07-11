/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.

 */
package com.flair.server.parser;

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
			// String upos = token.getUpos();
			if (lemma != null) {

				if (PersianGrammaticalPatterns.patternQuestionWords.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_PERSIAN): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_PERSIAN, token.getStart(),
							token.getEnd());
				}

				// TODO order the following questions words with most frequent first
				if (PersianGrammaticalPatterns.patternKoja.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_KOJA): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_KOJA, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.patternKe.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_KE): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_KE, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.patternKodom.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_KODOM): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_KODOM, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.patternChe.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_CHE): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_CHE, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.patternChetor.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_CHETOR): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_CHETOR, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.patternChera.matcher(lemma).matches()) {
					ServerLogger.get().info("NEW OCCURRENCE (QUESTIONS_CHERA): " + token.toString());
					addConstructionOccurrence(GrammaticalConstruction.QUESTIONS_CHERA, token.getStart(),
							token.getEnd());
				}
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
						}
						else if (!tokenUpos.matches("PUNCT|SYM|X")) {
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