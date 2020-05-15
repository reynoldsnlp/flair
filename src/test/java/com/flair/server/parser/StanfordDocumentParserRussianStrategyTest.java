package com.flair.server.parser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

public class StanfordDocumentParserRussianStrategyTest {
	private final String stringToParse = "Он вывел себя из системы самодержавия и имперскости, став ее разрушителем. Он дал нам возможность решать свою судьбу. Он дал нам кое что. Порадовать Finale может и тем, что поддерживается большое количество форматов экспорта.";
	//private final String stringToParse = "Он хочет есть. У него есть ручки. \"младший\" и он. Где? Он здес, так ли? где ми этот человек? мой отец. из-за чего ты здесь? в каком ты здании? В каком ты здании? Я приду, когда он придет. куда он делся?";

	//constants
	private static final String RUSSIAN_POS_MODEL = "edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger";
	private static final String RUSSIAN_DEPPARSE_MODEL = "edu/stanford/nlp/models/parser/nndep/nndep.rus.model.wiki.txt.gz";
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
				String plainSentence = itr.get(CoreAnnotations.TextAnnotation.class);
				SemanticGraph graph = itr.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
				List<CoreLabel> words = itr.get(CoreAnnotations.TokensAnnotation.class);

				//find all verbs
				List<IndexedWord> verbs = graph.getAllNodesByPartOfSpeechPattern("VERB");
				for(IndexedWord verb: verbs){
					List<SemanticGraphEdge> edqes = graph.getOutEdgesSorted(verb); //TODO: DATIVE OBJECTS?
					for(SemanticGraphEdge edge: edqes){
						//get the object of the verb
						IndexedWord objectOfVerb = edge.getSource();
						int objectIndex = objectOfVerb.index() - 1;
						CoreLabel word = words.get(objectIndex + 1);

						System.out.println("break");
					}
				}

				/*SemgrexMatcher questionMatcher = patternQuestionWordMainClause.matcher(graph);
				while(questionMatcher.find()){
					IndexedWord questionWord = questionMatcher.getNode(labelQuestionWordMainClause);
					System.out.println("Child node: \"" + questionWord.value() + "\" with index " + questionWord.index());
				}*/

				/*SemgrexMatcher verbNoSubjectMatcher = patternVerbNoSubject.matcher(graph);
				while(verbNoSubjectMatcher.find()){
					IndexedWord child = verbNoSubjectMatcher.getNode(labelVerbNoSubject);
					System.out.println("Subjectless verb node: \"" + child.value() + "\" with index " + child.index());
				}*/
			}
		}
	}

	@After
	public void tearDown() {
		pipeline = null;
		pipelineProps = null;
	}
}