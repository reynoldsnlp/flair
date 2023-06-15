/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.server.parser;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.server.grammar.PersianGrammaticalPatterns;
import com.flair.server.stanza.StanzaToken;

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
			// To keep code organized, you may create other functions to call inside this function 
			String upos = token.getUpos();
			String surface = token.getText();
			String surface_lower = surface.toLowerCase();
			if (upos != null && surface != null) {

				// These are (broken?) examples of how you would add a construction
				if (PersianGrammaticalPatterns.NEGATION.matcher(surface_lower)) {
					addConstructionOccurrence(GrammaticalConstruction.NEGATION_ALL, token.getStart(),
							token.getEnd());
				} else if (PersianGrammaticalPatterns.PARTIAL_NEGATION.matcher(surface_lower)) {
					addConstructionOccurrence(GrammaticalConstruction.NEGATION_PARTIAL, token.getStart(),
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
						if (!token.getUpos().matches("PUNCT|SYM|X")) {
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
