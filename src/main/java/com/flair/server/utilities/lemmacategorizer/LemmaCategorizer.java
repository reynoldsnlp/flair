package com.flair.server.utilities.lemmacategorizer;

import java.util.HashMap;
import java.util.Map;

public abstract class LemmaCategorizer {
	public static final int NULL_CATEGORY = -1;
	protected Map<String, Integer> lemmaToCategory = new HashMap<>();

	public int getCategory(String lemma) { return lemmaToCategory.getOrDefault(lemma, NULL_CATEGORY); }
}
