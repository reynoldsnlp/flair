package com.flair.server.raft;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RaftTest {
    private Raft raft;
    private String path;
    private String textSource;
    private String sourceName;


    public static String readFileToString(String path, String fileName) throws IOException 
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
		System.out.println(returnStr);
		return returnStr;
	}
    @Before
    public void setUp() 
    {
        path = this.getClass().getClassLoader().getResource("").getPath();
        raft = new Raft();
        sourceName = "testFiles/Arabic.pdf";
        try
        { 
            textSource = readFileToString(path, sourceName);
        }
        catch(IOException ex) 
        {
            textSource = "تقع مدينة عَطْبَرَة في ولاية نهر النيل في اتجاه الشمال، وتبعد عن العاصمة الخرطوم بحوالي 310 كيلو متر وعن مدينة الدامر حاضرة الولاية بحوالي 10 كيلو متر وعن ميناء بورتسودان في الشرق 611 كيلو متر، وجنوباً عن وادي حلفا بحوالي 474 كيلومتر. تقع المدينة على الضفة الشمالية لنهر عطبرة والضفة الشرقية لنهر النيل.";
        }

    }

    @Test
    public void testModelExists() {
        boolean exists = false;
        exists = raft.modelExists(sourceName);
        assertTrue(exists);

    }

    @Test
    public void testScoreText() throws FileNotFoundException, ClassNotFoundException, UnsupportedEncodingException,
            IOException, InterruptedException, Exception 
    {
        double score = raft.ScoreText(textSource);
        System.out.println("text score == " + score);
        System.out.flush();
        assertTrue("Make sure that MADAMIRA server is running", score > 0);
    }
}