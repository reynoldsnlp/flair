package com.flair.server.parser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import static org.junit.Assert.*;

public class StanfordDocumentParserRussianStrategyTest {

	private final String stringToParse = "когда мужчина собирается приехать сюда?";

	//constants
	private static final String RUSSIAN_POS_MODEL       = "edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger";
	private static final String RUSSIAN_DEPPARSE_MODEL  = "edu/stanford/nlp/models/parser/nndep/nndep.rus.model.wiki.txt.gz";
	//members
	private StanfordCoreNLP pipeline;
	private Properties pipelineProps;

	@Before
	public void setUp() {
		pipelineProps = new Properties();
		//RUSSIAN
		pipelineProps.put("annotators", "tokenize, ssplit, pos, depparse");
		pipelineProps.put("tokenize.language", "en");
		//pipelineProps.put("depparse.language", "russian");
		pipelineProps.setProperty("pos.model", RUSSIAN_POS_MODEL);
		pipelineProps.setProperty("depparse.model", RUSSIAN_DEPPARSE_MODEL);
		//pipelineProps.setProperty("depparse.language","russian");

		pipeline = new StanfordCoreNLP(pipelineProps);
	}

	@Test
	public void applySimulation() {
		Annotation docAnnotation = new Annotation(stringToParse);
		pipeline.annotate(docAnnotation);
		List<CoreMap> sentences = docAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap itr : sentences) {
			if (itr.size() > 0) {
				SemanticGraph graph = itr.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
				List<CoreLabel> words = itr.get(CoreAnnotations.TokensAnnotation.class);
			}
		}
	}

	@After
	public void tearDown() {
		pipeline = null;
		pipelineProps = null;
	}
}