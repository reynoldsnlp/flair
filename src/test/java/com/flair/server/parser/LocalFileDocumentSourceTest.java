package com.flair.server.parser;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.flair.server.utilities.CustomFileReader;
import com.flair.server.utilities.ServerLogger;
import com.flair.shared.grammar.Language;

public class LocalFileDocumentSourceTest
{
	private String sourceFileName;
	private File sourceFile;
	private Language lang;
	private CustomFileReader fileReader;
//	@Mock
//	private StreamDocumentSource streamMock;
	private LocalFileDocumentSource source;
	
	private void testException(String exceptionMessage)
	{
		try
		{
			source = new LocalFileDocumentSource(sourceFile, lang);
		}
		catch(Exception ex)
		{
			Assert.assertTrue("Exception message is " + ex.getMessage(), ex.getMessage().contains(exceptionMessage));
			return;
		}
		Assert.fail("Failed to catch exception");
	}
	
	@Before 
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		fileReader = new CustomFileReader();
		sourceFileName = fileReader.getRelativePath() + "testFiles/english/GettysburgAddress.txt";
		sourceFile = Mockito.spy(new File(sourceFileName));
		lang = Language.ENGLISH;
	}
	
	@Test 
	public void testValidSource()
	{
		try
		{
			source = new LocalFileDocumentSource(sourceFile, lang);
		}
		catch(Exception ex)
		{
			Assert.fail(ex.getMessage());
		}
		Assert.assertTrue(source.getSourceText().length() > 0);
		Assert.assertTrue("Description is " + source.getDescription(), source.getDescription().contains("GettysburgAddress.txt"));
	}
	
	@Test 
	public void testCannotReadSource()
	{
		Mockito.when(sourceFile.canRead()).thenReturn(false);
		testException("Cannot read from source file at");
	}
	
	@Test 
	public void testSourceIsNotFile()
	{
		Mockito.when(sourceFile.isFile()).thenReturn(false);
		testException("Invalid source file at");
	}
	
	@Test 
	public void testEmptySourceFile()
	{
		sourceFileName = fileReader.getRelativePath() + "testFiles/emptyFile.txt";
		sourceFile = new File(sourceFileName);
		testException("Empty source file at");
	}
	
	@Test 
	public void testIOException()
	{
		sourceFileName = "testFiles/Arabic.pdf";	//our tika set up doesn't read pdf's 
		sourceFile = new File(sourceFileName);
		testException("Cannot read from source file at");
	}

}
