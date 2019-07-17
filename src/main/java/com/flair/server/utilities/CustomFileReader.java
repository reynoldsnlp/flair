package com.flair.server.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CustomFileReader 
{

    public CustomFileReader() {}
    public String readFileToString(String path, String fileName) throws IOException 
    {
		String filePath = path + fileName;
 
		StringBuilder fileData = new StringBuilder(1000);//Constructs a string buffer with no characters in it and the specified initial capacity
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[1024];
		int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) 
        {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		String returnStr = fileData.toString();
		return returnStr;
	}

	public String getRelativePath()
	{
		return this.getClass().getClassLoader().getResource("").getPath();
	}
    
}