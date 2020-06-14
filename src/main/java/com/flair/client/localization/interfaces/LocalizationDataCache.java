package com.flair.client.localization.interfaces;

import com.flair.client.localization.DisplayLanguage;

/*
 * Exposes localization data to consumers
 */
public interface LocalizationDataCache
{
	public boolean					hasProvider(String name);
	public LocalizationProvider		getProvider(String name);
	public String					getLocalizedString(String provider, String tag, DisplayLanguage lang);
}
