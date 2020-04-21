/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */
package com.flair.shared.grammar;

import java.util.*;

/**
 * Represents a grammatical construction.
 * IMPORTANT - ORDER-DEPENDENT! ADD NEW ITEMS TO THE END!
 * @author shadeMe
*/
public enum GrammaticalConstruction
{
    // (simple) constructions
    EXISTENTIAL_THERE("existentialThere", Language.ENGLISH, Language.RUSSIAN), //Russian: есть and нет
    THERE_IS_ARE("thereIsAre", Language.ENGLISH),
    THERE_WAS_WERE("thereWasWere", Language.ENGLISH),
    
    ATTRIBUTES_PARTICIPLE_1("participle1Attribute", Language.GERMAN),
    ATTRIBUTES_PARTICIPLE_2("participle2Attribute", Language.GERMAN),
    ATTRIBUTES_ADJECTIVE("adjectiveAttribute", Language.GERMAN),
    ATTRIBUTES_PREPOSITION("prepositionalAttribute", Language.GERMAN),
    
    CONJUNCTIONS_ADVANCED("advancedConjunctions", Language.ENGLISH),
    CONJUNCTIONS_SIMPLE("simpleConjunctions", Language.ENGLISH),
    
    PREPOSITIONS("prepositions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    PREPOSITIONS_SIMPLE("simplePrepositions", Language.ENGLISH, Language.GERMAN),
    PREPOSITIONS_COMPLEX("complexPrepositions", Language.ENGLISH),
    PREPOSITIONS_ADVANCED("advancedPrepositions", Language.ENGLISH),
    
    // sentence structure
    CLAUSE_SUBORDINATE("subordinateClause", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    CLAUSE_RELATIVE("relativeClause", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    CLAUSE_RELATIVE_REDUCED("relativeClauseReduced", Language.ENGLISH),
    CLAUSE_ADVERBIAL("adverbialClause", Language.ENGLISH, Language.GERMAN),
    CLAUSE_THAT("thatClause", Language.GERMAN),
    SENTENCE_SIMPLE("simpleSentence", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    SENTENCE_COMPLEX("complexSentence", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    SENTENCE_COMPOUND("compoundSentence", Language.ENGLISH, Language.GERMAN),
    SENTENCE_INCOMPLETE("incompleteSentence", Language.ENGLISH, Language.GERMAN),
    
    OBJECT_DIRECT("directObject", Language.ENGLISH, Language.RUSSIAN),
    OBJECT_INDIRECT("indirectObject", Language.ENGLISH),
    
    PRONOUNS("pronouns", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_PERSONAL("pronounsPersonal", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_RELATIVE("pronounsRelative", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_POSSESSIVE("pronounsPossessive", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // /PRP$ ("", my, your, their)
    PRONOUNS_DEMONSTRATIVE("pronounsDemonstrative", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_REFLEXIVE("pronounsReflexive", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // /PRP + myself, themselves, etc.
    PRONOUNS_INDEFINITE("pronounsIndefinite", Language.GERMAN),
    PRONOUNS_INTERROGATIVE("pronounsInterrogative", Language.GERMAN, Language.RUSSIAN),
    PRONOUNS_SUBJECTIVE("pronounsSubjective", Language.ENGLISH),

    // quantifiers
    DETERMINER_SOME("someDet", Language.ENGLISH, Language.GERMAN),
    DETERMINER_ANY("anyDet", Language.ENGLISH, Language.GERMAN),
    DETERMINER_MUCH("muchDet", Language.ENGLISH),
    DETERMINER_MANY("manyDet", Language.ENGLISH, Language.GERMAN),
    DETERMINER_A_LOT_OF("aLotOfDet", Language.ENGLISH),
    
    ARTICLES("articles", Language.ENGLISH, Language.GERMAN),
    ARTICLE_THE("theArticle", Language.ENGLISH, Language.GERMAN),
    ARTICLE_A("aArticle", Language.ENGLISH, Language.GERMAN),
    ARTICLE_AN("anArticle", Language.ENGLISH),
    
    NOUNS_ISMUS("ismusNounForms", Language.GERMAN),
    NOUNS_TUR("turNounForms", Language.GERMAN),
    NOUNS_UNG("ungNounForms", Language.GERMAN),
    
    NEGATION_ALL("negAll", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // nobody, nowhere, etc.
    NEGATION_PARTIAL("partialNegation", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // rarely, barely, seldom, hardly, scarcely // Russian: вряд, редко, едва, еле, еле-еле, не совсем, навряд, наврядли, едва-едва, нечасто, изредка, почти не, с трудом, чуть, чуть-чуть
    NEGATION_NO_NOT_NEVER("noNotNever", Language.ENGLISH, Language.GERMAN),
    NEGATION_NT("nt", Language.ENGLISH),
    NEGATION_NOT("not", Language.ENGLISH),

    QUESTIONS_DIRECT("directQuestions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // Russian: ends in a '?'
    QUESTIONS_INDIRECT("indirectQuestions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // Russian: ???????
    QUESTIONS_YESNO("yesNoQuestions", Language.ENGLISH, Language.GERMAN), // direct: "Are you ok?"
    QUESTIONS_WH("whQuestions", Language.ENGLISH, Language.GERMAN), // direct: "What do you do?"
    QUESTIONS_TO_BE("toBeQuestions", Language.ENGLISH), // direct: "What's this?"
    QUESTIONS_TO_DO("toDoQuestions", Language.ENGLISH), // direct: "What do you do?"
    QUESTIONS_TO_HAVE("toHaveQuestions", Language.ENGLISH), // direct: "What have you done?"
    QUESTIONS_MODAL("modalQuestions", Language.ENGLISH), // direct: "Should I go?", "What should I do?"
    QUESTIONS_WHAT("what", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHO("who", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_HOW("how", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHY("why", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHERE("where", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHEN("when", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHOSE("whose", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHOM("whom", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_WHICH("which", Language.ENGLISH, Language.GERMAN),
    QUESTIONS_TAG("tagQuestions", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN), // ", isn't it?" //*** ", (nje) tak (li)?" //TODO (, AND ? ARE IMPORTANT)
    
    // conditionals - check first, before tenses
    CONDITIONALS("conditionals", Language.ENGLISH),
    CONDITIONALS_REAL("condReal", Language.ENGLISH),
    CONDITIONALS_UNREAL("condUnreal", Language.ENGLISH, Language.RUSSIAN), //TODO: verify that this is a thing (Rob)
    
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
    VERBFORM_INFINITIVE("infinitiveForms", Language.GERMAN),
    VERBFORM_PARTICIPLE("participleForms", Language.GERMAN),

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
    ADJECTIVE_COMPARATIVE_SHORT("comparativeAdjShort", Language.ENGLISH),// "nicer"
    ADJECTIVE_SUPERLATIVE_SHORT("superlativeAdjShort", Language.ENGLISH),// "nicest"
    ADJECTIVE_COMPARATIVE_LONG("comparativeAdjLong", Language.ENGLISH),// "more beautiful"
    ADJECTIVE_SUPERLATIVE_LONG("superlativeAdjLong", Language.ENGLISH),// "most beautiful"
    
    ADVERB_POSITIVE("positiveAdv", Language.ENGLISH, Language.GERMAN, Language.RUSSIAN),// "quickly"
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
    VERBFORM_EMPHATIC_DO("emphaticDo", Language.ENGLISH), // "I do realize it": do/did/VBP followed by /VB
    
    PRONOUNS_POSSESSIVE_ABSOLUTE("pronounsPossessiveAbsolute", Language.ENGLISH), // /JJ or PRP... ("", mine, yours, theirs)
    PASSIVE_VOICE("passiveVoice", Language.ENGLISH, Language.RUSSIAN),
    TENSE_PRESENT_PERFECT("presentPerfect", Language.ENGLISH),
    TENSE_PAST_PERFECT("pastPerfect", Language.ENGLISH),
    PRONOUNS_OBJECTIVE("pronounsObjective", Language.ENGLISH), // /PRP + me, you, them...
    POSTPOSITION("postposition", Language.GERMAN),


    //*** NEW CONSTRUCTIONS BELOW ***//

    //ADJECTIVES
    ADJECTIVE_LONG_RUSSIAN("adjectiveLongRussian", Language.RUSSIAN), //Russian: long form adjectives (without "Pred" tag)
    ADJECTIVE_SHORT_RUSSIAN("adjectiveShortRussian", Language.RUSSIAN), //Russian: short form adjectives (with "Pred" tag)
    ADJECTIVE_COMPARATIVE_SHORT_RUSSIAN("comparativeAdjShortRussian", Language.RUSSIAN),// Russian: "Cmpar" and "Pred" tags
    ADJECTIVE_COMPARATIVE_LONG_RUSSIAN("comparativeAdjLongRussian", Language.RUSSIAN),// Russian: 'более' then an adjective
    ADJECTIVE_SUPERLATIVE_LONG_RUSSIAN("superlativeAdjLongRussian", Language.RUSSIAN),// Russian: 'самый' then an adjective //TODO: or any lemma that ends in -[^жшщч]ейший or -[жшщч]айший, or any lemma among: высший, низший, лучший, худший, старший, младший

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

    //RUSSIAN VERB FORMS
    VERBS_IRREGULAR_RUSSIAN("irregularVerbsRussian", Language.RUSSIAN),
    VERB_REFLEXIVE("reflexiveVerb", Language.RUSSIAN),
    VERBAL_ADVERB("verbalAdverb", Language.RUSSIAN),
    VERBAL_ADVERB_PRESENT("verbalAdverbPresent", Language.RUSSIAN), //imperfective, "V", and "Adv" tags
    VERBAL_ADVERB_PAST("verbalAdverbPast", Language.RUSSIAN), //perfective, "V", and "Adv" tags
    //TODO make sure localization describing verbal adverbs also uses the word 'gerund'
    VERBFORM_INFINITIVE_RUSSIAN("infinitiveFormsRussian", Language.RUSSIAN),
    VERBFORM_PARTICIPLE_RUSSIAN("participleFormsRussian", Language.RUSSIAN),
    IMPERATIVES_RUSSIAN("imperativesRussian", Language.RUSSIAN),

    //RUSSIAN QUESTION WORDS
    QUESTIONS_YESNO_RUSSIAN("yesNoQuestionsRussian", Language.RUSSIAN), // direct: "Are you ok?" // Russian: (has li or no interrogative) and does have a '?'
    QUESTIONS_WH_RUSSIAN("whQuestionsRussian", Language.RUSSIAN), // direct: "What do you do?" // Russian: has "Interr" which is not 'li', and has a '?'
    QUESTIONS_WHAT_RUSSIAN("whatRussian", Language.RUSSIAN),
    QUESTIONS_WHO_RUSSIAN("whoRussian", Language.RUSSIAN),
    QUESTIONS_HOW_RUSSIAN("howRussian", Language.RUSSIAN),
    QUESTIONS_WHY_RUSSIAN("whyRussian", Language.RUSSIAN),
    QUESTIONS_WHERE_RUSSIAN("whereRussian", Language.RUSSIAN),
    QUESTIONS_WHEN_RUSSIAN("whenRussian", Language.RUSSIAN),
    QUESTIONS_WHOSE_RUSSIAN("whoseRussian", Language.RUSSIAN),
    QUESTIONS_WHICH_RUSSIAN("whichRussian", Language.RUSSIAN),
    QUESTIONS_WHITHER_RUSSIAN("whither", Language.RUSSIAN),
    QUESTIONS_WHAT_KIND_RUSSIAN("whatKind", Language.RUSSIAN),

    //RUSSIAN TENSE AND ASPECT
    ASPECT_BIASPECTUAL("biaspectual", Language.RUSSIAN),
    PAST_PERFECTIVE("pastPerfective", Language.RUSSIAN),
    PAST_IMPERFECTIVE("pastImperfective", Language.RUSSIAN),
    TENSE_PRESENT("presentTense", Language.RUSSIAN),
    FUTURE_IMPERFECTIVE("futureImperfective", Language.RUSSIAN),
    FUTURE_PERFECTIVE("futurePerfective", Language.RUSSIAN),

    //CONJUGATION CLASSES

    //DECLENSION CLASSES

    //IRREGULAR VERBS
    //(ся|сь) may be attached to the end of any infinitive (for the purpose of recognizing these verbs)
    VERBS_IRREGULAR_PAST("verbsIrregularPast", Language.RUSSIAN), // Russian: -жечь, -шибить, -расти, идти, -йти
    VERBS_IRREGULAR_NONPAST("verbsIrregularNonpast", Language.RUSSIAN), // Russian: хотеть бежать есть дать чтить

    //OTHER
    PRONOUNS_NEGATIVE("pronounsNegative", Language.RUSSIAN),
    NEGATION_PRONOUNS("negationPronouns", Language.RUSSIAN),
    OBJECT_INDIRECT_RUSSIAN("indirectObjectRussian", Language.RUSSIAN),
    PRONOUNS_DEFINITE_RUSSIAN("pronounsDefiniteRussian", Language.RUSSIAN),
    PRONOUNS_INDEFINITE_RUSSIAN("pronounsIndefiniteRussian", Language.RUSSIAN),
    DETERMINER_SOME_RUSSIAN("someDetRussian", Language.RUSSIAN),
    DETERMINER_ANY_RUSSIAN("anyDetRussian", Language.RUSSIAN),
    DETERMINER_MUCH_RUSSIAN("muchDetRussian", Language.RUSSIAN),
    NEGATION_NO_NOT_NEVER_RUSSIAN("noNotNeverRussian", Language.RUSSIAN), // нет, не,DETERMINER_PREPOSITIONAL ни, никогда, никак, никуда, нигде, ниоткуда, нипочём, ничуть, нисколько, нисколечко
    CONDITIONALS_RUSSIAN("conditionalsRussian", Language.RUSSIAN),


    ;
    
	
	static final class Helper
	{
		private static final Map<String, GrammaticalConstruction>	UNIQUE_IDS = new HashMap<>();
		
		private static void registerID(String id, GrammaticalConstruction gram)
	    {
	    	if (UNIQUE_IDS.containsKey(id))
	    		throw new RuntimeException("Grammatical construction ID already registered: " + id);
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
        langs.addAll(Arrays.asList(languages));
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

