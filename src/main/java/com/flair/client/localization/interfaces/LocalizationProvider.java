package com.flair.client.localization.interfaces;

import com.flair.client.localization.DisplayLanguage;

/*
 * Provides localization data to consumers
 */
public interface LocalizationProvider
{
	public String			getName();
	
	public void				setLocalizedString(String tag, DisplayLanguage lang, String localizedStr);
	public String			getLocalizedString(String tag, DisplayLanguage lang);
}
