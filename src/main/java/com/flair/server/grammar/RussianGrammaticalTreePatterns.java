package com.flair.server.grammar;

import edu.stanford.nlp.trees.tregex.TregexPattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import java.util.regex.Pattern;

public class RussianGrammaticalTreePatterns {

    //PARTICLES

    //regex for the interrogative particle 'ли'
    private static final String STR_LI = "\\bли\\b";
    public static Pattern patternLi = Pattern.compile(STR_LI, CASE_INSENSITIVE);
    //regex for conditional particle 'бы'
    private static final String STR_BI = "\\bбы\\b";
    public static Pattern patternBi = Pattern.compile(STR_BI, CASE_INSENSITIVE);


    //QUESTION WORDS

    //regex for interrogative 'как'
    private static final String STR_KAK = "\\bкак\\b";
    public static Pattern patternKak = Pattern.compile(STR_KAK, CASE_INSENSITIVE);


    //VERB FORMS

    //regex for reflexive verbs
    private static final String STR_REFLEXIVE_VERB = "(ся|сь)\\b";
    public static Pattern patternReflexiveVerb = Pattern.compile(STR_REFLEXIVE_VERB, CASE_INSENSITIVE);


    //PREPOSITIONS

    //regex for preposition 'в' or 'во', which governs nominative, accusative, prepositional
    private static final String STR_V = "\\b(в|во)\\b";
    public static Pattern patternV = Pattern.compile(STR_V, CASE_INSENSITIVE);
    //regex for preposition 'за', which governs nominative, accusative, instrumental
    private static final String STR_ZA = "\\bза\\b";
    public static Pattern patternZa = Pattern.compile(STR_ZA, CASE_INSENSITIVE);
    //regex for preposition 'без', which governs genitive
    private static final String STR_BEZ = "\\bбез\\b";
    public static Pattern patternBez = Pattern.compile(STR_BEZ, CASE_INSENSITIVE);
    //regex for preposition 'ввиду', which governs genitive
    private static final String STR_VVIDU = "\\bввиду\\b";
    public static Pattern patternVvidu = Pattern.compile(STR_VVIDU, CASE_INSENSITIVE);
    //regex for preposition 'вдоль', which governs genitive
    private static final String STR_VDOLJ = "\\bвдоль\\b";
    public static Pattern patternVdolj = Pattern.compile(STR_VDOLJ, CASE_INSENSITIVE);
    //regex for preposition 'вместо', which governs genitive
    private static final String STR_VMESTO = "\\bвместо\\b";
    public static Pattern patternVmesto = Pattern.compile(STR_VMESTO, CASE_INSENSITIVE);
    //regex for preposition 'вне', which governs genitive
    private static final String STR_VNE = "\\bвне\\b";
    public static Pattern patternVne = Pattern.compile(STR_VNE, CASE_INSENSITIVE);
    //regex for preposition 'внутри', which governs genitive
    private static final String STR_VNUTRI = "\\bвнутри\\b";
    public static Pattern patternVnutri = Pattern.compile(STR_VNUTRI, CASE_INSENSITIVE);
    //regex for preposition 'внутрь	', which governs genitive
    private static final String STR_VNUTRJ = "\\bвнутрь\\b";
    public static Pattern patternVnutrj = Pattern.compile(STR_VNUTRJ, CASE_INSENSITIVE);
    //regex for preposition 'возле', which governs genitive
    private static final String STR_VOZLE = "\\bвозле\\b";
    public static Pattern patternVozle = Pattern.compile(STR_VOZLE, CASE_INSENSITIVE);
    //regex for preposition 'впереди', which governs genitive
    private static final String STR_VPEREDI = "\\bвпереди\\b";
    public static Pattern patternVperedi = Pattern.compile(STR_VPEREDI, CASE_INSENSITIVE);
    //regex for preposition 'вследствие', which governs genitive
    private static final String STR_VSLEDSTVIE = "\\bвследствие\\b";
    public static Pattern patternVsledstvie = Pattern.compile(STR_VSLEDSTVIE, CASE_INSENSITIVE);
    //regex for preposition 'вокруг', which governs genitive
    private static final String STR_VOKRUG = "\\bвокруг\\b";
    public static Pattern patternVokrug = Pattern.compile(STR_VOKRUG, CASE_INSENSITIVE);
    //regex for preposition 'для', which governs genitive
    private static final String STR_DLJA = "\\bдля\\b";
    public static Pattern patternDlja = Pattern.compile(STR_DLJA, CASE_INSENSITIVE);
    //regex for preposition 'до', which governs genitive
    private static final String STR_DO = "\\bдо\\b";
    public static Pattern patternDo = Pattern.compile(STR_DO, CASE_INSENSITIVE);
    //regex for prepositions 'из' 'из' and 'из', which govern genitive
    private static final String STR_IZ = "\\bиз(\\b|-за\\b|-под\\b)";
    public static Pattern patternIz = Pattern.compile(STR_IZ, CASE_INSENSITIVE);
    //regex for preposition 'кроме', which governs genitive
    private static final String STR_KROME = "\\bкроме\\b";
    public static Pattern patternKrome = Pattern.compile(STR_KROME, CASE_INSENSITIVE);
    //regex for preposition 'между', which governs genitive, instrumental
    private static final String STR_MEZHDU = "\\bмежду\\b";
    public static Pattern patternMezhdu = Pattern.compile(STR_MEZHDU, CASE_INSENSITIVE);
    //regex for prepositions 'мимо' and 'помимо', which govern genitive
    private static final String STR_MIMO = "(\\b|\\bпо)мимо\\b";
    public static Pattern patternMimo = Pattern.compile(STR_MIMO, CASE_INSENSITIVE);
    //regex for preposition 'напротив', which governs genitive
    private static final String STR_NAPROTIV = "\\напротив\\b";
    public static Pattern patternNaprotiv = Pattern.compile(STR_NAPROTIV, CASE_INSENSITIVE);
    //regex for preposition 'насчет', which governs genitive
    private static final String STR_NASCHET = "\\bнасчет\\b";
    public static Pattern patternNaschet = Pattern.compile(STR_NASCHET, CASE_INSENSITIVE);
    //regex for preposition 'около', which governs genitive
    private static final String STR_OKOLO = "\\bоколо\\b";
    public static Pattern patternOkolo = Pattern.compile(STR_OKOLO, CASE_INSENSITIVE);
    //regex for preposition 'от' or 'ото', which governs genitive
    private static final String STR_OT = "\\bот(\\b|о\\b)";
    public static Pattern patternOt = Pattern.compile(STR_OT, CASE_INSENSITIVE);


}
