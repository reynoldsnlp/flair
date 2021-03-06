/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */
package com.flair.server.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

/**
 * Represents a document source object that encapsulates the contents of a local
 * file
 * 
 * @author shadeMe
 */
public class LocalFileDocumentSource extends AbstractDocumentSource
{
	private final StreamDocumentSource source;
	private final String filePath;

	public LocalFileDocumentSource(File sourceFile, Language lang)
	{
		this(sourceFile, sourceFile.getName(), lang);
	}

	public LocalFileDocumentSource(File sourceFile, String filename, Language lang)
	{
		super(lang);
		filePath = sourceFile.getAbsolutePath();
		if (sourceFile.canRead() == false)
			throw new IllegalArgumentException("Cannot read from source file at " + filePath);
		else if (sourceFile.isFile() == false)
			throw new IllegalArgumentException("Invalid source file at " + filePath);

		try
		{
			FileInputStream sourceFileStream = new FileInputStream(sourceFile);
			if(sourceFileStream.available() == 0)
			{
				sourceFileStream.close();
				throw new IllegalArgumentException("Empty source file at " + filePath);	
			}
			else 
			{
				source = new StreamDocumentSource(sourceFileStream, filename, lang);
			}
		}
		catch (IOException ex)
		{
			throw new IllegalArgumentException("Cannot read from source file at " + filePath
					+ ". Exception: " + ex.getMessage());
		}
	}

	@Override
	public String getSourceText()
	{
		return source.getSourceText();
	}

	@Override
	public String getDescription()
	{
		return "Local File: " + source.getName();
	}

	@Override
	public int compareTo(AbstractDocumentSource t)
	{
		if (t instanceof LocalFileDocumentSource == false)
			throw new IllegalArgumentException("Incompatible source type");

		LocalFileDocumentSource rhs = (LocalFileDocumentSource) t;
		return source.compareTo(rhs.source);
	}
}
