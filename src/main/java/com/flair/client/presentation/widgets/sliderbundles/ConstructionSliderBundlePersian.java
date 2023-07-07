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
 * Gram const. weight sliders for Persian
 */
public class ConstructionSliderBundlePersian extends LanguageSpecificConstructionSliderBundle
{
    private static ConstructionSliderBundlePersianUiBinder uiBinder = GWT.create(ConstructionSliderBundlePersianUiBinder.class);
    
    interface ConstructionSliderBundlePersianUiBinder extends UiBinder<Widget, ConstructionSliderBundlePersian>
    {
    }
    
    private static ConstructionSliderBundlePersianLocalizationBinder localeBinder = GWT.create(ConstructionSliderBundlePersianLocalizationBinder.class);
    interface ConstructionSliderBundlePersianLocalizationBinder extends LocalizationBinder<ConstructionSliderBundlePersian> {}
    

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
    GrammaticalConstructionPanelItem			pnlTensesAndPersonUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlTensesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPersonUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlMoodUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlImperativeUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPolarityAdjUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlQuantifiersUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdjectivesUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlDegreeUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlAdverbsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPronounsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlNumbersUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPluralityUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlReflexivityUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlConjunctionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlPrepositionsUI;
    @UiField
    @LocalizedField
    GrammaticalConstructionPanelItem			pnlNounsUI;
    
    public ConstructionSliderBundlePersian()
    {
        super(Language.PERSIAN);
        
        initWidget(uiBinder.createAndBindUi(this));
        initLocale(localeBinder.bind(this));
        setRootContainer(pnlRootUI);
    }
}
