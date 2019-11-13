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

public class ConstructionSliderBundleRussian extends LanguageSpecificConstructionSliderBundle {
    private static ConstructionSliderBundleRussian.ConstructionSliderBundleRussianUiBinder uiBinder = GWT.create(ConstructionSliderBundleRussian.ConstructionSliderBundleRussianUiBinder.class);

    interface ConstructionSliderBundleRussianUiBinder extends UiBinder<Widget, ConstructionSliderBundleRussian>
    {
    }

    private static ConstructionSliderBundleRussian.ConstructionSliderBundleRussianLocalizationBinder localeBinder = GWT.create(ConstructionSliderBundleRussian.ConstructionSliderBundleRussianLocalizationBinder.class);
    interface ConstructionSliderBundleRussianLocalizationBinder extends LocalizationBinder<ConstructionSliderBundleRussian> {}


    @UiField
    HTMLPanel pnlRootUI;

    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem            pnlSentencesUI;
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
    GrammaticalConstructionPanelItem			pnlNegationUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlQuantifiersUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdjectivesUI;
    /*@UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdverbsUI;*/
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPronounsUI;
    /*@UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlConjunctionsUI;*/
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPrepositionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem            pnlNounsUI;

    public ConstructionSliderBundleRussian()
    {
        super(Language.RUSSIAN);

        initWidget(uiBinder.createAndBindUi(this));
        initLocale(localeBinder.bind(this));
        setRootContainer(pnlRootUI);
    }
}
