package com.flair.shared.parser;

/*
 * Readability level of an arabic document
 */
public enum ArabicDocumentReadabilityLevel
{
	LEVEL_1("1.0"),		
	LEVEL_2("2.0"),		
    LEVEL_3("3.0"),		
    LEVEL_4("4.0"),		
	;
	
	private final String		title;
	
	private ArabicDocumentReadabilityLevel(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return title;
	}
}