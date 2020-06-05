package com.flair.server.utilities.lemmacategorizer;

import com.flair.shared.grammar.GrammaticalConstruction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RussianConjugationClasses extends LemmaCategorizer {
	private static final String VERBS_CONJUGATION_CLASSES_RU = "/verbs_conjugation_classes_ru.tsv";
	private static final int LEMMA_COLUMN_INDEX = 2;
	private static final int CATEGORY_COLUMN_INDEX = 0;
	private static final int TABLE_WIDTH = 3;

	private static final Map<Integer, GrammaticalConstruction> categoryToConstruction = new HashMap<>();
	static {
		categoryToConstruction.put(1, GrammaticalConstruction.VERBS_CONJUGATION_1_RUSSIAN);
		categoryToConstruction.put(2, GrammaticalConstruction.VERBS_CONJUGATION_2_RUSSIAN);
		categoryToConstruction.put(3, GrammaticalConstruction.VERBS_CONJUGATION_3_RUSSIAN);
		categoryToConstruction.put(4, GrammaticalConstruction.VERBS_CONJUGATION_4_RUSSIAN);
		categoryToConstruction.put(5, GrammaticalConstruction.VERBS_CONJUGATION_5_RUSSIAN);
		categoryToConstruction.put(6, GrammaticalConstruction.VERBS_CONJUGATION_6_RUSSIAN);
		categoryToConstruction.put(7, GrammaticalConstruction.VERBS_CONJUGATION_7_RUSSIAN);
		categoryToConstruction.put(8, GrammaticalConstruction.VERBS_CONJUGATION_8_RUSSIAN);
		categoryToConstruction.put(9, GrammaticalConstruction.VERBS_CONJUGATION_9_RUSSIAN);
		categoryToConstruction.put(10, GrammaticalConstruction.VERBS_CONJUGATION_10_RUSSIAN);
		categoryToConstruction.put(11, GrammaticalConstruction.VERBS_CONJUGATION_11_RUSSIAN);
		categoryToConstruction.put(12, GrammaticalConstruction.VERBS_CONJUGATION_12_RUSSIAN);
		categoryToConstruction.put(13, GrammaticalConstruction.VERBS_CONJUGATION_13_RUSSIAN);
		/*categoryToConstruction.put(14, GrammaticalConstruction.VERBS_CONJUGATION_14_RUSSIAN);
		categoryToConstruction.put(15, GrammaticalConstruction.VERBS_CONJUGATION_15_RUSSIAN);
		categoryToConstruction.put(16, GrammaticalConstruction.VERBS_CONJUGATION_16_RUSSIAN);*/
	}

	public static GrammaticalConstruction getConstructionFromCategory(int category) {
		return categoryToConstruction.getOrDefault(category, null);
	}

	public void load() throws IOException {
		InputStream russianConjugationClassesStream = this.getClass().getResourceAsStream(VERBS_CONJUGATION_CLASSES_RU);
		BufferedReader russianConjugationClassesReader = new BufferedReader(new InputStreamReader(russianConjugationClassesStream, StandardCharsets.UTF_8));
		while(russianConjugationClassesReader.ready()) {
			Scanner russianConjugationClassesScanner = new Scanner(russianConjugationClassesReader.readLine());
			russianConjugationClassesScanner.useDelimiter("\t");
			List<String> row = new ArrayList<>(TABLE_WIDTH);
			while(russianConjugationClassesScanner.hasNext()) row.add(russianConjugationClassesScanner.next());
			int category;
			try {
				category = Integer.parseInt(row.get(CATEGORY_COLUMN_INDEX));
			} catch (NumberFormatException e) { category = NULL_CATEGORY; }
			lemmaToCategory.put(row.get(LEMMA_COLUMN_INDEX), category);
		}
	}
}
