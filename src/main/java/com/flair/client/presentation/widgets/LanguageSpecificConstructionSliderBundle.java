package com.flair.client.presentation.widgets;

import com.flair.client.localization.LocalizedComposite;
import com.flair.client.presentation.interfaces.CanReset;
import com.flair.client.presentation.interfaces.GrammaticalConstructionContainer;
import com.flair.shared.grammar.GrammaticalConstruction;
import com.flair.shared.grammar.Language;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/*
 * Base class for language-specific gram const groupings
 */
public abstract class LanguageSpecificConstructionSliderBundle extends LocalizedComposite implements GrammaticalConstructionContainer, CanReset
{
	private final Language		lang;
	private HasWidgets			rootContainer;
	
	public LanguageSpecificConstructionSliderBundle(Language lang)
	{
		this.lang = lang;
		this.rootContainer = null;
	}
	
	public Language getLanguage() {
		return lang;
	}
	
	protected void setRootContainer(HasWidgets w) {
		rootContainer = w;
	}
	
	@Override
	public boolean hasConstruction(GrammaticalConstruction val) {
		return getWeightSlider(val) != null;
	}

	@Override
	public GrammaticalConstructionWeightSlider getWeightSlider(GrammaticalConstruction val)
	{
		if (rootContainer == null)
			throw new RuntimeException("Invalid root container");
		
		// only lookup slider panels
		GrammaticalConstructionWeightSlider out = null;
		for (Widget itr : rootContainer)
		{
			if (itr instanceof GrammaticalConstructionPanel)
			{
				GrammaticalConstructionPanel panel = (GrammaticalConstructionPanel)itr;
				if ((out = panel.getWeightSlider(val)) != null)
					break;
			}
		}
		
		return out;
	}
	
	@Override
	public void resetState(boolean fireEvents)
	{
		for (Widget itr : rootContainer)
		{
			if (itr instanceof CanReset)
			{
				CanReset w = (CanReset)itr;
				w.resetState(fireEvents);
			}
		}
	}
	
	@Override
	public void forEachWeightSlider(ForEachHandler handler)
	{
		for (Widget itr : rootContainer)
		{
			if (itr instanceof GrammaticalConstructionPanel)
			{
				GrammaticalConstructionPanel panel = (GrammaticalConstructionPanel)itr;
				panel.forEachWeightSlider(handler);
			}
		}
	}

	@Override
	public void refreshLocale()
	{
		super.refreshLocale();
		
		// additionally refresh the widget's children
		// shortcut to refresh the locale of all sliders
		forEachWeightSlider(w -> w.refreshLocale());
	}
}
