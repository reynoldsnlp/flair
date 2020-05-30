package com.flair.server.parser;

import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.util.*;

public class StanfordDocumentParserRussianStrategyTest {
	//private final String stringToParse = "мне хочется есть. Ей жарко. Нам придется сказать ей. Он отказался от силы.";
	//private final String stringToParse = "Он отказался от силы. Он вывел себя из системы самодержавия и имперскости, став ее разрушителем. Он дал нам возможность решать свою судьбу. Он дал нам кое что. Порадовать Finale может и тем, что поддерживается большое количество форматов экспорта.";
	//private final String stringToParse = "Он хочет есть. У него есть ручки. \"младший\" и он. Где? Он здес, так ли? где ми этот человек? мой отец. из-за чего ты здесь? в каком ты здании? В каком ты здании? Я приду, когда он придет. куда он делся?";

	//constants
	private static final String RUSSIAN_POS_MODEL = "edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger";
	private static final String RUSSIAN_DEPPARSE_MODEL = "edu/stanford/nlp/models/parser/nndep/nndep.rus.model.wiki.txt.gz";
	//members
	private StanfordDocumentParserRussianStrategy strategy;

	public StanfordDocumentParserRussianStrategyTest() {
		strategy = new StanfordDocumentParserRussianStrategy();
		Properties pipelineProps = new Properties();
		//RUSSIAN
		pipelineProps.put("annotators", "tokenize, ssplit, pos, depparse");
		pipelineProps.put("tokenize.language", "en");
		//pipelineProps.put("depparse.language", "russian");
		pipelineProps.setProperty("pos.model", RUSSIAN_POS_MODEL);
		pipelineProps.setProperty("depparse.model", RUSSIAN_DEPPARSE_MODEL);
		//pipelineProps.setProperty("depparse.language","russian");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);
		strategy.setPipeline(pipeline);
	}

	private AbstractDocument getParsedDocument(String stringToParse){
		AbstractDocument parsedDocument = new DocumentFactory().create(new SimpleDocumentSource(stringToParse, Language.RUSSIAN));
		strategy.apply(parsedDocument);
		return parsedDocument;
	}

	/*@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}*/

	@Test
	public void existentialTherePositiveTest() {
		String stringToParse = "У меня есть кот.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialThereNegativeTest() {
		String stringToParse = "Мне хочется есть.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}
}
