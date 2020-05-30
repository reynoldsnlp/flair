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
	public void existentialJestSimplePositiveTest() {
		String stringToParse = "У меня есть кот.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialJestSimpleNegativeTest1() {
		String stringToParse = "Мне хочется есть.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}

	@Test
	public void existentialJestSimpleNegativeTest2() {
		String stringToParse = "Он хочет есть.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}

	@Test
	public void existentialJestComplexPositiveTest1() {
		String stringToParse = "Жуть в том, что «бабский» коллектив ― это и есть жуть.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialJestComplexPositiveTest2() {
		String stringToParse = "Ну что, есть там особые неожиданности?";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialJestComplexPositiveTest3() {
		String stringToParse = "не знаю как будет, но сейчас такое настроение есть.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialJestComplexNegativeTest() {
		String stringToParse = "Есть надо сразу, ибо сметана потом «стекает»";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}

	@Test
	public void existentialNjetSimplePositiveTest1() {
		String stringToParse = "у меня нет кота.";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialNjetSimplePositiveTest2() {
		String stringToParse = "в твоей чашке нет воды";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	@Test
	public void existentialNjetSimpleNegativeTest1() {
		String stringToParse = "нет";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}

	@Test
	public void existentialNjetSimpleNegativeTest2() {
		String stringToParse = "нет, я не приду";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(0, existentialCount);
	}

	@Test
	public void existentialNjetComplexPositiveTest1() { //TODO: FAILS because 'иду' happens to be the parent of 'нет'
		String stringToParse = "Я иду, но у меня нет денег";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}

	//в магазине, где я работаю, нет электричества
	@Test
	public void existentialNjetComplexPositiveTest2() {
		String stringToParse = "на заводе, где я работаю, нет электричества";
		AbstractDocument parsedDocument = getParsedDocument(stringToParse);
		int existentialCount = parsedDocument.getConstructionData(GrammaticalConstruction.EXISTENTIAL_THERE).getFrequency();
		Assert.assertEquals(1, existentialCount);
	}
}
