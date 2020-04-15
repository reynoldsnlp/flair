package com.flair.client.presentation.widgets.sliderbundles;

import com.flair.client.localization.annotations.LocalizedField;
import com.flair.client.localization.interfaces.LocalizationBinder;
import com.flair.client.presentation.widgets.GrammaticalConstructionPanelItem;
import com.flair.client.presentation.widgets.LanguageSpecificConstructionSliderBundle;
import com.flair.shared.grammar.Language;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/*
 * Gram const. weight sliders for Arabic
 */
public class ConstructionSliderBundleArabic extends LanguageSpecificConstructionSliderBundle
{
    private static com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic.ConstructionSliderBundleArabicUiBinder uiBinder = GWT.create(com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic.ConstructionSliderBundleArabicUiBinder.class);

    interface ConstructionSliderBundleArabicUiBinder extends UiBinder<Widget, com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic>
    {
    }

    private static com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic.ConstructionSliderBundleArabicLocalizationBinder localeBinder = GWT.create(com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic.ConstructionSliderBundleArabicLocalizationBinder.class);
    interface ConstructionSliderBundleArabicLocalizationBinder extends LocalizationBinder<com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleArabic> {}


    @UiField
    HTMLPanel									pnlRootUI;

    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlSentencesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlQuestionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlSentenceTypesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlClauseTypesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPartsOfSpeechUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlVerbsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlVerbFormsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlTensesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAspectUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlTimeUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlVoiceUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlTransitiveUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlImperativeUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlNegationUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlArticlesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlQuantifiersUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdjectivesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdverbsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPronounsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlConjunctionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPrepositionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlNounsUI;
    //New Stuff

    public ConstructionSliderBundleArabic()
    {
        super(Language.ARABIC);

        initWidget(uiBinder.createAndBindUi(this));
        initLocale(localeBinder.bind(this));
        setRootContainer(pnlRootUI);
    }
}
