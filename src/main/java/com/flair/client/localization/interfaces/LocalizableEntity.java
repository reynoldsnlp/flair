package com.flair.client.localization.interfaces;

import com.flair.client.localization.DisplayLanguage;

/*
 * Interface for localizable entities
 */
public interface LocalizableEntity
{
	public void		setLocale(DisplayLanguage lang, LocalizationDataCache data);
}
