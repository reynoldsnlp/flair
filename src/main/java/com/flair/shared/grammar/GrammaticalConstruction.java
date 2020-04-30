/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.shared.grammar;

import org.apache.commons.codec.language.bm.Lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a grammatical construction.
 * IMPORTANT - ORDER-DEPENDENT! ADD NEW ITEMS TO THE END!
 * @author shadeMe
*/
//THIS IS MY COMMENT!
public enum GrammaticalConstruction //TODO: separate lemma-based Constructions used in Russian so they can have their own descriptions in the UI
{
    // (simple) constructions
    EXISTENTIAL_THERE("existentialThere", Language.ENGLISH, Language.RUSSIAN), //Russian: есть and нет
    THERE_IS_ARE("thereIsAre", Language.ENGLISH),
    THERE_WAS_WERE("thereWasWere", Language.ENGLISH),
    
    ATTRIBUTES_PARTICIPLE_1("participle1Attribute", Language.GERMAN),
    ATTRIBUTES_PARTICIPLE_2("participle2Attribute", Language.GERMAN),
    ATTRIBUTES_ADJECTIVE("adjectiveAttribute", Language.GERMAN, Language.RUSSIAN), //Russian: long form adjectives (without "Pred" tag)
    ATTRIBUTES_PREPOSITION("prepositionalAttribute", Language.GERMAN),
    
    CONJUNCTIONS_ADVANCED("advancedConjunctions", Language.ENGLISH),
    CONJUNCTIONS_SIMPLE("simpleConjunctions", Language.ENGLISH),
    
    PREPOSITIONS("prepositions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN, Language.ARABIC),
    PREPOSITIONS_SIMPLE("simplePrepositions", Language.ENGLISH, Language.GERMAN),
    PREPOSITIONS_COMPLEX("complexPrepositions", Language.ENGLISH),
    PREPOSITIONS_ADVANCED("advancedPrepositions", Language.ENGLISH),
    
    // sentence structure
    CLAUSE_SUBORDINATE("subordinateClause", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    CLAUSE_RELATIVE("relativeClause", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    CLAUSE_RELATIVE_REDUCED("relativeClauseReduced", Language.ENGLISH),
    CLAUSE_ADVERBIAL("adverbialClause", Language.ENGLISH, Language.GERMAN), //*** ? verbal adverb? "čitajući" ? "V" and "Adv" tags
    CLAUSE_THAT("thatClause", Language.GERMAN), //TODO: refactor situations of 'dass' to be 'that' //*** ", čto"
    SENTENCE_SIMPLE("simpleSentence", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    SENTENCE_COMPLEX("complexSentence", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    SENTENCE_COMPOUND("compoundSentence", Language.ENGLISH, Language.GERMAN),
    SENTENCE_INCOMPLETE("incompleteSentence", Language.ENGLISH, Language.GERMAN),
    
    OBJECT_DIRECT("directObject", Language.ENGLISH),	 // "give me" //*** in the graph, verb with Acc or negated verb with Gen
    OBJECT_INDIRECT("indirectObject", Language.ENGLISH),	 // "give it toPrep me" //*** in the graph, verb with Dat
    
    PRONOUNS("pronouns", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_PERSONAL("pronounsPersonal", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_RELATIVE("pronounsRelative", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_POSSESSIVE("pronounsPossessive", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // /PRP$ ("", my, your, their)
    PRONOUNS_DEMONSTRATIVE("pronounsDemonstrative", Language.GERMAN, Language.RUSSIAN), // /JJ or PRP... ("", mine, yours, theirs)
    PRONOUNS_REFLEXIVE("pronounsReflexive", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // /PRP + myself, themselves, etc.
    PRONOUNS_INDEFINITE("pronounsIndefinite", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_INTERROGATIVE("pronounsInterrogative", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_SUBJECTIVE("pronounsSubjective", Language.ENGLISH),
    
    // quantifiers
    DETERMINER_SOME("someDet", Language.ENGLISH, Language.GERMAN), //*** njekotorujj / njekotoryj
    DETERMINER_ANY("anyDet", Language.ENGLISH, Language.GERMAN), //*** ljuboj
    DETERMINER_MUCH("muchDet", Language.ENGLISH), //*** mnogo, mnogij (lemmas, not surface form)
    DETERMINER_MANY("manyDet", Language.ENGLISH, Language.GERMAN),
    DETERMINER_A_LOT_OF("aLotOfDet", Language.ENGLISH),
    
    ARTICLES("articles", Language.ENGLISH, Language.GERMAN),
    ARTICLE_THE("theArticle", Language.ENGLISH, Language.GERMAN),
    ARTICLE_A("aArticle", Language.ENGLISH, Language.GERMAN),
    ARTICLE_AN("anArticle", Language.ENGLISH),
    
    NOUNS_ISMUS("ismusNounForms", Language.GERMAN),
    NOUNS_TUR("turNounForms", Language.GERMAN),
    NOUNS_UNG("ungNounForms", Language.GERMAN),
    
    NEGATION_ALL("negAll", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // nobody, nowhere, etc. //*** Pron Neg
    NEGATION_PARTIAL("partialNegation", Language.ENGLISH, Language.GERMAN), // rarely, barely, seldom, hardly, scarcely //TODO: maybe add to russian later
    NEGATION_NO_NOT_NEVER("noNotNever", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), //*** Russian: нет, не, nikogda, ni //TODO
    NEGATION_NT("nt", Language.ENGLISH),
    NEGATION_NOT("not", Language.ENGLISH, Language.RUSSIAN), //Russian: не
    
    QUESTIONS_DIRECT("directQuestions", Language.ENGLISH, Language.GERMAN), //*** ends in a '?'
    QUESTIONS_INDIRECT("indirectQuestions", Language.ENGLISH, Language.GERMAN), //*** (li or tag "Interr") with no '?' //TODO: just 'li'?
    QUESTIONS_YESNO("yesNoQuestions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // direct: "Are you ok?" //*** (has li or no interrogative) and does have a '?'
    QUESTIONS_WH("whQuestions", Language.ENGLISH, Language.GERMAN),// direct: "What do you do?" //*** has "Interr" which is not 'li', and has a '?'
    QUESTIONS_TO_BE("toBeQuestions", Language.ENGLISH),// direct: "What's this?" //TODO: Russian. What.
    QUESTIONS_TO_DO("toDoQuestions", Language.ENGLISH), // direct: "What do you do?"
    QUESTIONS_TO_HAVE("toHaveQuestions", Language.ENGLISH),// direct: "What have you done?"
    QUESTIONS_MODAL("modalQuestions", Language.ENGLISH),// direct: "Should I go?", "What should I do?"
    QUESTIONS_WHAT("what", Language.ENGLISH, Language.GERMAN), //*** čto lemma
    QUESTIONS_WHO("who", Language.ENGLISH, Language.GERMAN), //*** kto
    QUESTIONS_HOW("how", Language.ENGLISH, Language.GERMAN),//*** kak
    QUESTIONS_WHY("why", Language.ENGLISH, Language.GERMAN),//*** počjemu začjem
    QUESTIONS_WHERE("where", Language.ENGLISH, Language.GERMAN),//*** gdje //TODO: add a construction QUESTIONS_WHITHER //*** kuda lemma
    QUESTIONS_WHEN("when", Language.ENGLISH, Language.GERMAN),//*** kogda
    QUESTIONS_WHOSE("whose", Language.ENGLISH, Language.GERMAN),//*** čjej
    QUESTIONS_WHOM("whom", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHICH("which", Language.ENGLISH, Language.GERMAN),//*** kakoj //TODO: add a construction QUESTIONS_WHAT_KIND //*** kakov lemma
    QUESTIONS_TAG("tagQuestions", Language.ENGLISH, Language.GERMAN), // ", isn't it?" //*** ", (nje) tak (li)?" //TODO: come back to this
    
    // conditionals - check first, before tenses
    CONDITIONALS("conditionals", Language.ENGLISH, Language.RUSSIAN),
    CONDITIONALS_REAL("condReal", Language.ENGLISH), //TODO: pick up here next time
    CONDITIONALS_UNREAL("condUnreal", Language.ENGLISH),
    
    // tenses - only if not conditional
    TENSE_PRESENT_SIMPLE("presentSimple", Language.ENGLISH),
    TENSE_PAST_SIMPLE("pastSimple", Language.ENGLISH),
    TENSE_PRESENT_PERFECT_HABEN("presentPerfectHaben", Language.GERMAN),
    TENSE_PRESENT_PERFECT_SEIN("presentPerfectSein", Language.GERMAN),
    TENSE_PAST_PERFECT_HABEN("pastPerfectHaben", Language.GERMAN),
    TENSE_PAST_PERFECT_SEIN("pastPerfectSein", Language.GERMAN),
    TENSE_FUTURE_SIMPLE("futureSimple", Language.ENGLISH, Language.GERMAN),
    TENSE_FUTURE_PERFECT("futurePerfect", Language.ENGLISH, Language.GERMAN),
    
    ASPECT_SIMPLE("simpleAspect", Language.ENGLISH),
    ASPECT_PROGRESSIVE("progressiveAspect", Language.ENGLISH),
    ASPECT_PERFECT("perfectAspect", Language.ENGLISH),
    ASPECT_PERFECT_PROGRESSIVE("perfProgAspect", Language.ENGLISH),
    
    TIME_PRESENT("presentTime", Language.ENGLISH),
    TIME_PAST("pastTime", Language.ENGLISH),
    TIME_FUTURE("futureTime", Language.ENGLISH),
    
    VERBCONST_GOING_TO("goingTo", Language.ENGLISH),
    VERBCONST_USED_TO("usedTo", Language.ENGLISH),
    
    VERBTYP_MAIN("mainVerbs", Language.GERMAN),
    VERBTYP_AUXILIARIES("auxiliaryVerbs", Language.GERMAN),
    VERBTYP_MODAL("modalVerbs", Language.GERMAN),
    
    VERBFORM_TO_INFINITIVE("toInfinitiveForms", Language.ENGLISH, Language.GERMAN),
    VERBFORM_INFINITIVE("infinitiveForms", Language.GERMAN, Language.RUSSIAN),// "I want toPrep do it."
    VERBFORM_PARTICIPLE("participleForms", Language.GERMAN, Language.RUSSIAN),// "I want toPrep do it."
    
    VERB_CLUSTER("verbCluster", Language.GERMAN),
    VERB_BRACKETS("verbBrackets", Language.GERMAN),
    
    MODALS("modals", Language.ENGLISH),// all
    MODALS_SIMPLE("simpleModals", Language.ENGLISH),// can, must, need, may
    MODALS_ADVANCED("advancedModals", Language.ENGLISH),// the others
    MODALS_CAN("can", Language.ENGLISH),// Klasse 6
    MODALS_MUST("must", Language.ENGLISH),// Klasse 6
    MODALS_NEED("need", Language.ENGLISH),// Klasse 6
    MODALS_MAY("may", Language.ENGLISH),// Klasse 6
    MODALS_COULD("could", Language.ENGLISH),// Klasse 10
    MODALS_MIGHT("might", Language.ENGLISH),// Klasse 10
    MODALS_OUGHT("ought", Language.ENGLISH),// Klasse 10
    MODALS_ABLE("able", Language.ENGLISH),// Klasse 10 ("", annotated as JJ)
    MODALS_HAVE_TO("haveTo", Language.ENGLISH),// ??
    
    VERBS_IRREGULAR("irregularVerbs", Language.ENGLISH),// past tense or past participle not ending with -ed
    VERBS_REGULAR("regularVerbs", Language.ENGLISH),// past tense or past participle ending with -ed
    VERBS_PHRASAL("phrasalVerbs", Language.ENGLISH),// phrasal verbs ("", & verbs with prepositions: look atPrep)
    
    IMPERATIVES("imperatives", Language.ENGLISH, Language.GERMAN),// start with a Verb, often end with "!": "Do it yourself!"
    PASSIVE_VOICE_WERDEN("passiveVoiceWerden", Language.GERMAN),
    PASSIVE_VOICE_SEIN("passiveVoiceSein", Language.GERMAN),
    
    ADJECTIVE_POSITIVE("positiveAdj", Language.ENGLISH, Language.GERMAN),// "nice"
    //ADJECTIVE_COMPARATIVE("compartiveAdj", Language.ARABIC),
    ADJECTIVE_COMPARATIVE_SHORT("comparativeAdjShort", Language.ENGLISH),// "nicer"
    ADJECTIVE_SUPERLATIVE_SHORT("superlativeAdjShort", Language.ENGLISH),// "nicest"
    ADJECTIVE_COMPARATIVE_LONG("comparativeAdjLong", Language.ENGLISH),// "more beautiful"
    ADJECTIVE_SUPERLATIVE_LONG("superlativeAdjLong", Language.ENGLISH),// "most beautiful"
    
    ADVERB_POSITIVE("positiveAdv", Language.ENGLISH, Language.GERMAN),// "quickly"
    ADVERB_COMPARATIVE_SHORT("comparativeAdvShort", Language.ENGLISH),// "faster"
    ADVERB_SUPERLATIVE_SHORT("superlativeAdvShort", Language.ENGLISH),// "fastest"
    ADVERB_COMPARATIVE_LONG("comparativeAdvLong", Language.ENGLISH),// "more quickly"
    ADVERB_SUPERLATIVE_LONG("superlativeAdvLong", Language.ENGLISH),// "most quickly"
    
    PARTICLE_PLUS_ADJ_ADV("particleAdjAdv", Language.GERMAN),
    
    CARDINALS("cardinals", Language.GERMAN),
    
    PLURAL_REGULAR("pluralRegular", Language.ENGLISH),
    PLURAL_IRREGULAR("pluralIrregular", Language.ENGLISH),
    NOUNFORMS_ING("ingNounForms", Language.ENGLISH),
	
    TENSE_PRESENT_PROGRESSIVE("presentProgressive", Language.ENGLISH),
    TENSE_PRESENT_PERFECT_PROGRESSIVE("presentPerfProg", Language.ENGLISH),
    TENSE_PAST_PROGRESSIVE("pastProgressive", Language.ENGLISH),
    TENSE_PAST_PERFECT_PROGRESSIVE("pastPerfProg", Language.ENGLISH),
    TENSE_FUTURE_PROGRESSIVE("futureProgressive", Language.ENGLISH),
    TENSE_FUTURE_PERFECT_PROGRESSIVE("futurePerfProg", Language.ENGLISH),
	
    VERBFORM_SHORT("shortVerbForms", Language.ENGLISH), // 's, 're, 'm, 's, 've, 'd??!
    VERBFORM_LONG("longVerbForms", Language.ENGLISH), // is, are, am, has, have, had??!
    VERBFORM_AUXILIARIES_BE_DO_HAVE("auxiliariesBeDoHave", Language.ENGLISH), // be, do, have??! ("", got?), NOT modals!!! + V
    VERBFORM_COPULAR("copularVerbs", Language.ENGLISH), // be, stay, seem, etc. - CHECK the parser
    VERBFORM_ING("ingVerbForms", Language.ENGLISH), // gerund, participle, nouns
    VERBFORM_EMPATHIC_DO("emphaticDo", Language.ENGLISH), // "I do realize it": do/did/VBP followed by /VB
    
    PRONOUNS_POSSESSIVE_ABSOLUTE("pronounsPossessiveAbsolute", Language.ENGLISH), // /JJ or PRP... ("", mine, yours, theirs)
    PASSIVE_VOICE("passiveVoice", Language.ENGLISH),
    TENSE_PRESENT_PERFECT("presentPerfect", Language.ENGLISH),
    TENSE_PAST_PERFECT("pastPerfect", Language.ENGLISH),
    PRONOUNS_OBJECTIVE("pronounsObjective", Language.ENGLISH), // /PRP + me, you, them...
    POSTPOSITION("postposition", Language.GERMAN),


    //*** NEW CONSTRUCTIONS BELOW ***//

    //CASES

    NOUN_NOMINATIVE("nounNominative", Language.RUSSIAN),
    ADJECTIVE_NOMINATIVE("adjectiveNominative", Language.RUSSIAN),
    PRONOUN_NOMINATIVE("pronounNominative", Language.RUSSIAN),
    DETERMINER_NOMINATIVE("determinerNominative", Language.RUSSIAN),
    PREPOSITION_NOMINATIVE("prepositionNominative", Language.RUSSIAN),

    NOUN_ACCUSATIVE("nounAccusative", Language.RUSSIAN),
    ADJECTIVE_ACCUSATIVE("adjectiveAccusative", Language.RUSSIAN),
    PRONOUN_ACCUSATIVE("pronounAccusative", Language.RUSSIAN),
    DETERMINER_ACCUSATIVE("determinerAccusative", Language.RUSSIAN),
    PREPOSITION_ACCUSATIVE("prepositionAccusative", Language.RUSSIAN),

    NOUN_GENITIVE("nounGenitive", Language.RUSSIAN),
    ADJECTIVE_GENITIVE("adjectiveGenitive", Language.RUSSIAN),
    PRONOUN_GENITIVE("pronounGenitive", Language.RUSSIAN),
    DETERMINER_GENITIVE("determinerGenitive", Language.RUSSIAN),
    PREPOSITION_GENITIVE("prepositionGenitive", Language.RUSSIAN),

    NOUN_PREPOSITIONAL("nounPrepositional", Language.RUSSIAN),
    ADJECTIVE_PREPOSITIONAL("adjectivePrepositional", Language.RUSSIAN),
    PRONOUN_PREPOSITIONAL("pronounPrepositional", Language.RUSSIAN),
    DETERMINER_PREPOSITIONAL("determinerPrepositional", Language.RUSSIAN),
    PREPOSITION_PREPOSITIONAL("prepositionPrepositional", Language.RUSSIAN),

    NOUN_DATIVE("nounDative", Language.RUSSIAN),
    ADJECTIVE_DATIVE("adjectiveDative", Language.RUSSIAN),
    PRONOUN_DATIVE("pronounDative", Language.RUSSIAN),
    DETERMINER_DATIVE("determinerDative", Language.RUSSIAN),
    PREPOSITION_DATIVE("prepositionDative", Language.RUSSIAN),

    NOUN_INSTRUMENTAL("nounInstrumental", Language.RUSSIAN),
    ADJECTIVE_INSTRUMENTAL("adjectiveInstrumental", Language.RUSSIAN),
    PRONOUN_INSTRUMENTAL("pronounInstrumental", Language.RUSSIAN),
    DETERMINER_INSTRUMENTAL("determinerInstrumental", Language.RUSSIAN),
    PREPOSITION_INSTRUMENTAL("prepositionInstrumental", Language.RUSSIAN),

    //RUSSIAN PARTICIPLES
    PARTICIPLE_PRESENT_ACTIVE("presentActiveParticiple", Language.RUSSIAN),
    PARTICIPLE_PRESENT_PASSIVE("presentPassiveParticiple", Language.RUSSIAN),
    PARTICIPLE_PAST_ACTIVE("pastActiveParticiple", Language.RUSSIAN),
    PARTICIPLE_PAST_PASSIVE("pastPassiveParticiple", Language.RUSSIAN),

    //RUSSIAN TENSES
    TENSE_PAST("pastTense", Language.RUSSIAN),
    TENSE_NON_PAST("nonpastTense", Language.RUSSIAN),
    TENSE_PRESENT("presentTense", Language.RUSSIAN),
    TENSE_FUTURE("futureTense", Language.RUSSIAN),

    //RUSSIAN VERB FORMS
    VERB_REFLEXIVE("reflexiveVerb", Language.RUSSIAN),

    //ARABIC
    VERBAL_NOUN ("verbalNoun", Language.ARABIC),
    //PARTICIPLES ("participles", Language.ARABIC),
    //PSEUDO_VERBS ("pseudoVerbs", Language.ARABIC),

    //MOOD
    MOOD_INDICATIVE ("verbInd", Language.ARABIC),
    MOOD_SUBJUNCTIVE ("verbSub", Language.ARABIC),
    MOOD_JUSSIVE ("verbJus", Language.ARABIC),
    MOOD_IMPERATIVE ("verbImp", Language.ARABIC),

    //VOICE
    //VOICE_ACTIVE ("verbAct", Language.ARABIC),
    //VOICE_PASSIVE ("verbPas", Language.ARABIC),

    //ASPECT
    ASPECT_PERFECTIVE ("perfective", Language.ARABIC),
    ASPECT_IMPERFECTIVE ("imperfective", Language.ARABIC),

    //NUMBER, PERSON, GENDER
    VERB_DUAL ("verbDu", Language.ARABIC),
    VERB_FEM_PL ("verbFemPl", Language.ARABIC),
    //VERB_1_SINGULAR ("verb1Sg", Language.ARABIC),
    //VERB_1_PLURAL ("verb1Pl", Language.ARABIC),
    //VERB_2_SINGULAR_MASC ("verb2SgM", Language.ARABIC),
    //VERB_2_SINGULAR_FEM ("verb2SgF", Language.ARABIC),
    //VERB_2_DUAL_MASC ("verb2DuM", Language.ARABIC),
    //VERB_2_DUAL_FEM ("verb2DuF", Language.ARABIC),
    //VERB_2_PLURAL_MASC ("verb2PlM", Language.ARABIC),
    //VERB_2_PLURAL_FEM ("verb2PlF", Language.ARABIC),
    //VERB_3_SINGULAR_MASC ("verb3SgM", Language.ARABIC),
    //VERB_3_SINGULAR_FEM ("verb3SgF", Language.ARABIC),
    //VERB_3_DUAL_MASC ("verb3DuM", Language.ARABIC),
    //VERB_3_DUAL_FEM ("verb3DuF", Language.ARABIC),
    //VERB_3_PLURAL_MASC ("verb3PlM", Language.ARABIC),
    //VERB_3_PLURAL_FEM ("verb3PlF", Language.ARABIC),

    NOUN_PL_MASC ("nounPluralMasc", Language.ARABIC),
    NOUN_PL_FEM ("nounPluralFem", Language.ARABIC),
    NOUN_DUAL ("nounDual", Language.ARABIC),

    CASE_NOMINATIVE ("caseNom", Language.ARABIC),
    CASE_ACCUSATIVE ("caseAcc", Language.ARABIC),
    CASE_GENITIVE ("caseGen", Language.ARABIC),

    CONSTRUCT ("construct", Language.ARABIC),

    //QUANTIFIERS("quantifiers", Language.ARABIC),
    //CONJUNCTIONS("conjunctions", Language.ARABIC),
    //PARTICLES_PREPOSITIONS("particlesPrepositions", Language.ARABIC),
    //PARTICLES_OTHER("particlesOther", Language.ARABIC)
    ;
    
	
	static final class Helper
	{
		private static final Map<String, GrammaticalConstruction>	UNIQUE_IDS = new HashMap<>();
		
		private static void registerID(String id, GrammaticalConstruction gram)
	    {
	    	if (UNIQUE_IDS.containsKey(id))
	    		throw new RuntimeException("Grammatical construction ID already registered");
	    	else
	    		UNIQUE_IDS.put(id, gram);
		}
	}
	
    private final String			id;			// unique ID
    private final Set<Language>		langs;		// languages that use the construction
    
	GrammaticalConstruction(String id, Language... languages)
	{
		this.id = id;
		this.langs = new HashSet<>();
		
		Helper.registerID(id, this);
		for (Language itr : languages)
			langs.add(itr);
	}

	@Override
	public String toString() {
		return getID();
	}

	public String getID() {
		return this.id;
	}
	
	public boolean hasLanguage(Language lang) {
		return langs.contains(lang);
	}
	
	public static GrammaticalConstruction lookup(String id) {
		return Helper.UNIQUE_IDS.get(id);
	}
	
	public static Set<GrammaticalConstruction> getForLanguage(Language lang)
	{
		HashSet<GrammaticalConstruction> out = new HashSet<>();
		
		for (GrammaticalConstruction itr : GrammaticalConstruction.values())
		{
			if (itr.hasLanguage(lang))
				out.add(itr);
		}
		
		return out;
	}
}

