package com.flair.client.localization;

import java.util.HashSet;
import java.util.Set;
import com.flair.client.localization.interfaces.LocalizedUI;
import com.flair.shared.utilities.GenericEventSource;

/*
 * Manages the locale state of active views
 */
public class LocalizationEngine
{
	public static class DisplayLanguageChanged
	{
		public DisplayLanguage newLang;
	}
	
	private static final LocalizationEngine	INSTANCE = new LocalizationEngine();
	public static LocalizationEngine get() {
		return INSTANCE;
	}
	
	private DisplayLanguage									currentLang;
	private final Set<LocalizedUI>						activeLocalizedViews;
	private final GenericEventSource<DisplayLanguageChanged>	langChangeListeners;
	
	private LocalizationEngine()
	{
		this.currentLang = DisplayLanguage.ENGLISH;
		this.activeLocalizedViews = new HashSet<>();
		this.langChangeListeners = new GenericEventSource<>();
	}
	
	private void refreshActiveViews()
	{
		for (LocalizedUI itr : activeLocalizedViews)
			itr.setLocale(currentLang);
	}
	
	private void notifyLanguageChange()
	{
		DisplayLanguageChanged e = new DisplayLanguageChanged();
		e.newLang = currentLang;
		langChangeListeners.raiseEvent(e);
	}
	
	public DisplayLanguage getLanguage() {
		return currentLang;
	}
	
	public void setLanguage(DisplayLanguage lang)
	{
		if (lang != currentLang)
		{
			currentLang = lang;
			refreshActiveViews();
			notifyLanguageChange();
		}
	}
	
	public void registerLocalizedView(LocalizedUI view) {
		activeLocalizedViews.add(view);
	}
	
	public void deregisterLocalizedView(LocalizedUI view) {
		activeLocalizedViews.remove(view);
	}
	
	public void addLanguageChangeHandler(GenericEventSource.EventHandler<DisplayLanguageChanged> handler) {
		langChangeListeners.addHandler(handler);
	}
	
	public String getLocalizedString(String provider, String tag) {
		return LocalizationStringTable.get().getLocalizedString(provider, tag, currentLang);
	}
}
