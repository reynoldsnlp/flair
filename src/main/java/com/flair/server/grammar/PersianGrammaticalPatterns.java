package com.flair.server.grammar;

import java.util.regex.Pattern;

import com.flair.shared.grammar.GrammaticalConstruction;

import java.util.HashMap;
import java.util.Map;

public class PersianGrammaticalPatterns {

    // Lemma matching
    public static final Map<String, GrammaticalConstruction[]> lemmaMap = createLemmaMap();

    private static HashMap<String, GrammaticalConstruction[]> createLemmaMap() {
        HashMap<String, GrammaticalConstruction[]> lemmas = new HashMap<String, GrammaticalConstruction[]>();
        lemmas.put("کجا", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_KOJA,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("کی", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_KE,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("کدم", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_KODOM,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("چه", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_CHE,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("چطور", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_CHETOR,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("چرا", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_CHERA,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("آیا", new GrammaticalConstruction[] { GrammaticalConstruction.QUESTIONS_AYA,
                GrammaticalConstruction.QUESTIONS_PERSIAN });
        lemmas.put("هر", new GrammaticalConstruction[] { GrammaticalConstruction.DETERMINER_HAR });
        lemmas.put("چند", new GrammaticalConstruction[] { GrammaticalConstruction.DETERMINER_CHAND });
        lemmas.put("هیچ", new GrammaticalConstruction[] { GrammaticalConstruction.DETERMINER_HICH });
        lemmas.put("زیاد", new GrammaticalConstruction[] { GrammaticalConstruction.DETERMINER_ZIAD });
        return lemmas;
    }


    // Adverbs
    private static final String STR_ADV = "ADV";
    public static final Pattern patternAdv = Pattern.compile(STR_ADV);

    // Degree
    private static final String STR_CMP = "Degree=Cmp";
    public static final Pattern patternCmp = Pattern.compile(STR_CMP);
    private static final String STR_POS = "Degree=Pos";
    public static final Pattern patternPos = Pattern.compile(STR_POS);
    private static final String STR_SUP = "Degree=Sup";
    public static final Pattern patternSup = Pattern.compile(STR_SUP);

    // Pronouns
    private static final String STR_PRON = "PRON";
    public static final Pattern patternPron = Pattern.compile(STR_PRON);
    private static final String STR_DEM = "PronType=Dem";
    public static final Pattern patternDem = Pattern.compile(STR_DEM);
    private static final String STR_IND = "PronType=Ind";
    public static final Pattern patternInd = Pattern.compile(STR_IND);
    private static final String STR_INT = "PronType=Int";
    public static final Pattern patternInt = Pattern.compile(STR_INT);
    private static final String STR_NEG_PRON = "PronType=Neg";
    public static final Pattern patternNegPron = Pattern.compile(STR_NEG_PRON);
    private static final String STR_PRS = "PronType=Prs";
    public static final Pattern patternPrs = Pattern.compile(STR_PRS);
    private static final String STR_RCP = "PronType=Rcp";
    public static final Pattern patternRcp = Pattern.compile(STR_RCP);
    private static final String STR_REL = "PronType=Rel";
    public static final Pattern patternRel = Pattern.compile(STR_REL);
    private static final String STR_TOT = "PronType=Tot";
    public static final Pattern patternTot = Pattern.compile(STR_TOT);

    // Mood
    private static final String STR_MOOD_IMP = "Mood=Imp";
    public static final Pattern patternMoodImp = Pattern.compile(STR_MOOD_IMP);
    private static final String STR_MOOD_SUBJ = "Mood=Sub";
    public static final Pattern patternMoodSubj = Pattern.compile(STR_MOOD_SUBJ);

    // Number
    private static final String STR_SING = "Number=Sing";
    public static final Pattern patternSing = Pattern.compile(STR_SING);
    private static final String STR_PLUR = "Number=Plur";
    public static final Pattern patternPlur = Pattern.compile(STR_PLUR);
    private static final String STR_CARD = "NumType=Card";
    public static final Pattern patternCard = Pattern.compile(STR_CARD);
    private static final String STR_ORD = "NumType=Ord";
    public static final Pattern patternOrd = Pattern.compile(STR_ORD);

    // Tense
    private static final String STR_PRES = "Tense=Pres";
    public static final Pattern patternPres = Pattern.compile(STR_PRES);
    private static final String STR_PAST = "Tense=Past";
    public static final Pattern patternPast = Pattern.compile(STR_PAST);
    private static final String STR_FUT = "Tense=Fut";
    public static final Pattern patternFut = Pattern.compile(STR_FUT);

    // Verb Forms
    private static final String STR_AUX = "​V_AUX";
    public static final Pattern patternAux = Pattern.compile(STR_AUX);
    private static final String STR_IMP = "V_IMP";
    public static final Pattern patternImp = Pattern.compile(STR_IMP);
    private static final String STR_V_PASTPART = "V_PP";
    public static final Pattern patternVPP = Pattern.compile(STR_V_PASTPART);
    private static final String STR_V_FIN = "V_FIN";
    public static final Pattern patternVFin = Pattern.compile(STR_V_FIN);
    private static final String STR_V_SUB = "V_SUB";
    public static final Pattern patternVSub = Pattern.compile(STR_V_SUB);

    // decide whether to add all possible sub-trees of verb forms

    // Person
    private static final String STR_FIRST = "Person=1";
    public static final Pattern patternFirst = Pattern.compile(STR_FIRST);
    private static final String STR_SEC = "Person=2";
    public static final Pattern patternSec = Pattern.compile(STR_SEC);
    private static final String STR_THIRD = "Person=3";
    public static final Pattern patternThird = Pattern.compile(STR_THIRD);

    // Polarity
    private static final String STR_NEG_POL = "Polarity=Neg";
    public static final Pattern patternNegPol = Pattern.compile(STR_NEG_POL);
    private static final String STR_POS_POL = "Polarity=Pos";
    public static final Pattern patternPosPol = Pattern.compile(STR_POS_POL);

    // Reflexive
    private static final String STR_REFL = "Reflex=Yes";
    public static final Pattern patternRefl = Pattern.compile(STR_REFL);
    // possibly distinguish between negative verbs and negative pronouns/dets? This
    // would be easier to do in a function in the parser doc

}
