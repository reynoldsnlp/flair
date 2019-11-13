package com.flair.client.presentation.widgets;

import com.flair.client.localization.CommonLocalizationTags;
import com.flair.client.localization.DefaultLocalizationProviders;
import com.flair.client.localization.LocalizedComposite;
import com.flair.client.localization.LocalizedFieldType;
import com.flair.client.localization.annotations.LocalizedCommonField;
import com.flair.client.localization.annotations.LocalizedField;
import com.flair.client.localization.interfaces.LocalizationBinder;
import com.flair.client.model.interfaces.DocumentRankerOutput;
import com.flair.client.model.interfaces.DocumentRankerOutput.Rank;
import com.flair.client.presentation.interfaces.AbstractRankerSettingsPane;
import com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleEnglish;
import com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleGerman;
import com.flair.client.presentation.widgets.sliderbundles.ConstructionSliderBundleRussian;
import com.flair.client.utilities.ClientLogger;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.flair.shared.interop.ConstructionSettingsProfile;
import com.flair.shared.interop.ConstructionSettingsProfileImpl;
import com.flair.shared.parser.ArabicDocumentReadabilityLevel;
import com.flair.shared.parser.DocumentReadabilityLevel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import gwt.material.design.client.ui.MaterialBadge;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCardTitle;
import gwt.material.design.client.ui.MaterialCheckBox;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialRow;

public class RankerSettingsPane extends LocalizedComposite implements AbstractRankerSettingsPane
{
	public interface ShowHideHandler {
		public void handle(boolean visible);
	}
	
	private static RankerSettingsPaneUiBinder uiBinder = GWT.create(RankerSettingsPaneUiBinder.class);

	interface RankerSettingsPaneUiBinder extends UiBinder<Widget, RankerSettingsPane>
	{
	}
	
	private static RankerSettingsPaneLocalizationBinder localeBinder = GWT.create(RankerSettingsPaneLocalizationBinder.class);
	interface RankerSettingsPaneLocalizationBinder extends LocalizationBinder<RankerSettingsPane> {}
	
	static enum LocalizationTags
	{
		FILTERED,
	}
	
	private static final int				PANEL_WIDTH = 400;
	
	@UiField
	MaterialRow								pnlSettingsContainerUI;
	@UiField
	MaterialLabel							lblDocCountUI;
	@UiField
	@LocalizedField(type=LocalizedFieldType.TEXT_BUTTON)
	MaterialButton							btnVisualizeUI;
	@UiField
	@LocalizedField(type=LocalizedFieldType.TEXT_BUTTON)
	MaterialButton							btnExportSettingsUI;
	@UiField
	@LocalizedField
	MaterialCardTitle						lblTextCharacteristicsUI;
	@UiField
	DocumentLengthConfigPane				pnlDocLengthUI;
	@UiField
	@LocalizedField
	MaterialLabel							lblTextLevelUI;
	@UiField
	MaterialCheckBox						chkTextLevelAor1UI;
	@UiField
	MaterialBadge							bdgTextLevelAor1CountUI;
	@UiField
	MaterialCheckBox						chkTextLevelBor2UI;
	@UiField
	MaterialBadge							bdgTextLevelBor2CountUI;
	@UiField
	MaterialCheckBox						chkTextLevelCor3UI;
	@UiField
	MaterialBadge							bdgTextLevelCor3CountUI;
	@UiField
	KeywordWeightSlider						sldKeywordsUI;
	@UiField
	@LocalizedField
	MaterialCardTitle						lblConstructionsUI;
	@UiField
	ConstructionSliderBundleEnglish			bdlEnglishSlidersUI;
	@UiField
	ConstructionSliderBundleGerman			bdlGermanSlidersUI;
	@UiField
	ConstructionSliderBundleRussian			bdlRussianSlidersUI;
	@UiField
	@LocalizedField
	MaterialCardTitle						lblLanguageUseUI;
	@UiField
	@LocalizedCommonField(tag=CommonLocalizationTags.RESET_ALL, type=LocalizedFieldType.TEXT_BUTTON)
	MaterialButton							btnResetAllUI;
	@UiField
	MaterialCheckBox						chkTextLevel4UI;
	@UiField
	MaterialBadge							bdgTextLevel4CountUI;
	/*
	@UiField
	MaterialBadge							bdgTextLevel1CountUI;
	@UiField
	MaterialCheckBox						chkTextLevel2;
	@UiField
	MaterialBadge							bdgTextLevel2CountUI;
	@UiField
	MaterialCheckBox						chkTextLevel3;
	@UiField
	MaterialBadge							bdgTextLevel3CountUI;
	@UiField
	MaterialCheckBox						chkTextLevel4;
	@UiField
	MaterialBadge							bdgTextLevel4CountUI;	
	*/
	
	State				state;
	ShowHideHandler		showhideHandler;
	boolean				visible;
	
	private final class State
	{
		Language					sliderLanguage;
		DocumentRankerOutput.Rank	rankData;
		EventHandler				changeHandler;
		EventHandler				visualizeHandler;
		EventHandler				exportHandler;
		EventHandler				resetHandler;

		
		State()
		{
			sliderLanguage = Language.ENGLISH;
			rankData = null;
			changeHandler = visualizeHandler = exportHandler = resetHandler = null;
		}
		private void showArabicLevels() 
		{
			chkTextLevelAor1UI.setText("Level 1");
			chkTextLevelBor2UI.setText("Level 2");
			chkTextLevelCor3UI.setText("Level 3");

			chkTextLevel4UI.setVisible(true);
			bdgTextLevel4CountUI.setVisible(true);
		}
		private void showDefaultLevels()
		{
			ClientLogger.get().info("showDefaultLevels()");
			
			chkTextLevelAor1UI.setText("A1-A2");
			chkTextLevelBor2UI.setText("B1-B2");
			chkTextLevelCor3UI.setText("C1-C2");

			chkTextLevel4UI.setVisible(false);
			bdgTextLevel4CountUI.setVisible(false);
		}
		private void onSettingChange()
		{
			ClientLogger.get().info("onSettingChange()");
			if (changeHandler != null)
				changeHandler.handle();
		}
		
		private void onVisualize()
		{
			if (visualizeHandler != null)
				visualizeHandler.handle();
		}
		
		private void onExport()
		{
			if (exportHandler != null)
				exportHandler.handle();
		}
		
		private void onReset()
		{
			if (resetHandler != null)
				resetHandler.handle();
		}
		
		private void hideSliderBundles()
		{
			bdlEnglishSlidersUI.setVisible(false);
			bdlGermanSlidersUI.setVisible(false);
			bdlRussianSlidersUI.setVisible(false);
		}
		
		public void resetUI()
		{
			ClientLogger.get().info("Calling resetUI");
			switch(sliderLanguage) { //TODO: switch out which cases are listed explicitly and which are under default
				case ARABIC:
					ClientLogger.get().info("Language is " + sliderLanguage.toString());
					showArabicLevels();
					hideSliderBundles();
					lblConstructionsUI.setVisible(false);
					break;
				default:
					ClientLogger.get().info("Language is " + sliderLanguage.toString());
					showDefaultLevels();
					hideSliderBundles();
					if (getSliderBundle() != null) {
						getSliderBundle().setVisible(true);
					}
					else{
						ClientLogger.get().error("Slider bundle not found");
					}
					lblConstructionsUI.setVisible(true);
			}
			/*
			hideSliderBundles();
			lblConstructionsUI.setVisible(false);
			*/

		}
		
		public void init(DocumentRankerOutput.Rank rankerData)
		{
			rankData = rankerData;
			reloadUI();
		}
		
		public void reloadUI()
		{
			ClientLogger.get().info("reloadUI");
			if (rankData == null)
				return;

			final int resultCount = rankData.getRankedDocuments().size();
			//getting the number of each level result
			int levelAor1 = 0;
			int levelBor2 = 0;
			int levelCor3 = 0;
			int level4 = 0;

			if(sliderLanguage.toString().equals("ARABIC")) {
				levelAor1 = (int)rankData.getArabicDocLevelDf(ArabicDocumentReadabilityLevel.LEVEL_1);
				levelBor2 = (int)rankData.getArabicDocLevelDf(ArabicDocumentReadabilityLevel.LEVEL_2);
				levelCor3 = (int)rankData.getArabicDocLevelDf(ArabicDocumentReadabilityLevel.LEVEL_3);
				level4 = (int)rankData.getArabicDocLevelDf(ArabicDocumentReadabilityLevel.LEVEL_4);
			}
			else {
				levelAor1 = (int)rankData.getDocLevelDf(DocumentReadabilityLevel.LEVEL_A);
				levelBor2 = (int)rankData.getDocLevelDf(DocumentReadabilityLevel.LEVEL_B);
				levelCor3 = (int)rankData.getDocLevelDf(DocumentReadabilityLevel.LEVEL_C);
			}
			
			
			lblDocCountUI.setText(resultCount + " " + getLocalizedString(DefaultLocalizationProviders.COMMON.toString(),
																		CommonLocalizationTags.RESULTS.toString()) +
								" (" + rankData.getNumFilteredDocuments() + " " + getLocalizedString(LocalizationTags.FILTERED.toString()) + ")");
			
			//setting the text level count
			bdgTextLevelAor1CountUI.setText(levelAor1 + " / " + resultCount);
			bdgTextLevelBor2CountUI.setText(levelBor2 + " / " + resultCount);
			bdgTextLevelCor3CountUI.setText(levelCor3 + " / " + resultCount);
			if(sliderLanguage.toString().equals("ARABIC")) 
				bdgTextLevel4CountUI.setText(level4 + " / " + resultCount);

			
			
			LanguageSpecificConstructionSliderBundle current = getSliderBundle();
			current.forEachWeightSlider(s -> {
				int df = (int)rankData.getConstructionDf(s.getGram());
				s.setResultCount(df, resultCount);
			});
		}
		
		public void setSliderBundle(Language lang)
		{
			hideSliderBundles();
			sliderLanguage = lang;
			
			getSliderBundle().setVisible(false);	//should be true to show out lblsliders
			getSliderBundle().refreshLocale();
		}
		
		public LanguageSpecificConstructionSliderBundle getSliderBundle()
		{
			switch (sliderLanguage)
			{
				case ARABIC:
				case ENGLISH:
					return bdlEnglishSlidersUI;
				case GERMAN:
					return bdlGermanSlidersUI;
				case RUSSIAN:
					return bdlRussianSlidersUI;
				default:
					return null;
			}
		}
		
		public void resetAll()
		{
			pnlDocLengthUI.resetState(false);
			sldKeywordsUI.resetState(false);
			getSliderBundle().resetState(false);
			
			chkTextLevelAor1UI.setValue(true, false);
			chkTextLevelBor2UI.setValue(true, false);
			chkTextLevelCor3UI.setValue(true, false);
			chkTextLevel4UI.setValue(true, false);
			
			onReset();
			onSettingChange();
		}
		
		public void setChangeHandler(EventHandler h) {
			changeHandler = h;
		}
		
		public void setVisualizeHandler(EventHandler h) {
			visualizeHandler = h;
		}
		
		public void setExportHandler(EventHandler h) {
			exportHandler = h;
		}
		
		public void setResetHandler(EventHandler h) {
			resetHandler = h;
		}
	}
	
	private void setPanelLeft(double l) {
		pnlSettingsContainerUI.setLeft(l);
	}
	
	private void setContainerVisible(boolean visible)
	{
		this.visible = visible;
		setPanelLeft(visible ? 0 : -PANEL_WIDTH);
	}
	
	private void initHandlers()
	{
		btnVisualizeUI.addClickHandler(e -> state.onVisualize());
		btnExportSettingsUI.addClickHandler(e -> state.onExport());
		
		pnlDocLengthUI.setWeightChangeHandler((v) -> state.onSettingChange());
		
		sldKeywordsUI.setWeightChangeHandler((w, v) -> state.onSettingChange());
		sldKeywordsUI.setToggleHandler((w, v) -> state.onSettingChange());
		
		btnResetAllUI.addClickHandler(e -> state.resetAll());
		
		chkTextLevelAor1UI.addValueChangeHandler(e -> state.onSettingChange());
		chkTextLevelBor2UI.addValueChangeHandler(e -> state.onSettingChange());
		chkTextLevelCor3UI.addValueChangeHandler(e -> state.onSettingChange());
		chkTextLevel4UI.addValueChangeHandler(e -> state.onSettingChange());
	}
	
	private void initUI()
	{
		ClientLogger.get().info("Calling initUI");
		pnlSettingsContainerUI.setWidth(PANEL_WIDTH + "px");
		state.resetUI();
		hide();
	}
	
	public RankerSettingsPane()
	{
		initWidget(uiBinder.createAndBindUi(this));
		initLocale(localeBinder.bind(this));
		
		this.state = new State();
		showhideHandler = null;
		visible = false;

		ClientLogger.get().info("Constructing RankerSettingsPane");

		initHandlers();
		initUI();
	}
	
	@Override
	public void setLocale(Language lang)
	{
		super.setLocale(lang);
		state.reloadUI();
	}

	@Override
	public void setSliderBundle(Language lang) {
		state.setSliderBundle(lang);
	}

	@Override
	public void updateSettings(Rank rankData) {
		state.init(rankData);
	}

	@Override
	public void show()
	{
		setContainerVisible(true);
		
		if (showhideHandler != null)
			showhideHandler.handle(visible);
	}
	
	@Override
	public void hide()
	{
		setContainerVisible(false);
		
		if (showhideHandler != null)
			showhideHandler.handle(visible);
	}

	@Override
	public void refresh() 
	{
		state.resetUI();
		state.reloadUI();
	}

	public void setShowHideEventHandler(ShowHideHandler handler) {
		showhideHandler = handler;
	}

	@Override
	public void setSettingsChangedHandler(EventHandler handler)
	{
		// update here as the sliders themselves aren't be available during the construction of the panel
		updateSlider(bdlEnglishSlidersUI);
		updateSlider(bdlGermanSlidersUI);
		updateSlider(bdlRussianSlidersUI);

		state.setChangeHandler(handler);
	}

	private void updateSlider(LanguageSpecificConstructionSliderBundle slider){
		slider.forEachWeightSlider(s -> {
			s.setWeightChangeHandler((w, v) -> state.onSettingChange());
			s.setToggleHandler((w, v) -> state.onSettingChange());
			s.refreshLocale();
		});
	}

	@Override
	public void setVisualizeHandler(EventHandler handler) {
		state.setVisualizeHandler(handler);
	}

	@Override
	public void setExportSettingsHandler(EventHandler handler) {
		state.setExportHandler(handler);
	}

	@Override
	public LanguageSpecificConstructionSliderBundle getSliderBundle() {
		return state.getSliderBundle();
	}

	@Override
	public DocumentLengthConfigPane getLengthConfig() {
		return pnlDocLengthUI;
	}

	@Override
	public KeywordWeightSlider getKeywordSlider() {
		return sldKeywordsUI;
	}

	@Override
	public boolean isDocLevelEnabled(DocumentReadabilityLevel level)	//Checks to see if we are goingo to display a certain doc level
	{	
		switch (level)
		{
		case LEVEL_A:
			return chkTextLevelAor1UI.getValue();
		case LEVEL_B:
			return chkTextLevelBor2UI.getValue();
		case LEVEL_C:
			return chkTextLevelCor3UI.getValue();
		default:
			return false;
		}
	}

	@Override
	public boolean isArabicDocLevelEnabled(ArabicDocumentReadabilityLevel level)	//Checks to see if we are goingo to display a certain doc level
	{	
		switch (level)
		{
		case LEVEL_1:
			ClientLogger.get().info("Level_1 enabled : " + chkTextLevelAor1UI.getValue());
			return chkTextLevelAor1UI.getValue();
		case LEVEL_2:
			ClientLogger.get().info("Level_2 enabled : " + chkTextLevelBor2UI.getValue());
			return chkTextLevelBor2UI.getValue();
		case LEVEL_3:
			ClientLogger.get().info("Level_3 enabled : " + chkTextLevelCor3UI.getValue());
			return chkTextLevelCor3UI.getValue();
		case LEVEL_4:
			ClientLogger.get().info("Level_4 enabled : " + chkTextLevel4UI.getValue());
			return chkTextLevel4UI.getValue();
		default:
			return false;
		}
	}

	public int getWidth() {
		return PANEL_WIDTH;
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setResetAllHandler(EventHandler handler) {
		state.setResetHandler(handler);
	}

	@Override
	public void applySettingsProfile(ConstructionSettingsProfile profile, boolean fireEvents)
	{
		chkTextLevelAor1UI.setValue(profile.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_A), false);
		chkTextLevelBor2UI.setValue(profile.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_B), false);
		chkTextLevelCor3UI.setValue(profile.isDocLevelEnabled(DocumentReadabilityLevel.LEVEL_C), false);
		
		pnlDocLengthUI.setWeight(profile.getDocLengthWeight(), false);
		sldKeywordsUI.setEnabled(profile.isKeywordsEnabled(), false);
		sldKeywordsUI.setWeight(profile.getKeywordsWeight(), false);
		
		for (GrammaticalConstruction itr : GrammaticalConstruction.getForLanguage(getSliderBundle().getLanguage()))
		{
			GrammaticalConstructionWeightSlider slider = getSliderBundle().getWeightSlider(itr);
			if (slider != null && profile.hasConstruction(itr))
			{
				slider.setWeight(profile.getConstructionWeight(itr), false);
				slider.setEnabled(profile.isConstructionEnabled(itr), false);
			}
		}
		
		if (fireEvents)
			state.onSettingChange();
	}

	@Override
	public ConstructionSettingsProfile generateSettingsProfile()
	{
		ConstructionSettingsProfileImpl out = new ConstructionSettingsProfileImpl();
		
		out.setLanguage(getSliderBundle().getLanguage());
		out.setDocLengthWeight(pnlDocLengthUI.getWeight());
		out.setKeywordsData(sldKeywordsUI.isEnabled(), sldKeywordsUI.getWeight());
		out.setDocLevelEnabled(DocumentReadabilityLevel.LEVEL_A, chkTextLevelAor1UI.getValue());
		out.setDocLevelEnabled(DocumentReadabilityLevel.LEVEL_B, chkTextLevelBor2UI.getValue());
		out.setDocLevelEnabled(DocumentReadabilityLevel.LEVEL_C, chkTextLevelCor3UI.getValue());
		
		for (GrammaticalConstruction itr : GrammaticalConstruction.getForLanguage(getSliderBundle().getLanguage()))
		{
			GrammaticalConstructionWeightSlider slider = getSliderBundle().getWeightSlider(itr);
			if (slider != null)
				out.setGramData(itr, slider.isEnabled(), slider.getWeight());
		}
		
		return out;
	}
}
