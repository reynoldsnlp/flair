package com.flair.client.localization.interfaces;

import com.flair.client.localization.DisplayLanguage;

/*
 * Interface implemented by all localized views
 */
public interface LocalizedUI
{
	public LocalizationProvider		getLocalizationProvider();
	
	public void						setLocale(DisplayLanguage lang);
	public void						refreshLocale();
}
