/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 
 */

package com.flair.server.utilities;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException; 
import java.lang.StringBuilder;

/**
 * Simple text file reader
 * 
 * @author mjbriggs
 */

 public class FileReader {
    private Scanner scanner;
    private String fileName;
    private StringBuilder stringBuilder;

    public FileReader(){
        scanner = null;
        fileName = "";
        stringBuilder = new StringBuilder();
    }

    public FileReader(String fileName){
        setFileName(fileName);
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
        try{
            scanner = new Scanner(new File(this.fileName));
        }
        catch(FileNotFoundException e){
            ServerLogger.get().error(this.fileName + " does not exist. Exception: " + e.getMessage());
        }
        stringBuilder = new StringBuilder();
    }

    public String getFileString(){
        if(scanner == null || fileName.equals(""))
            ServerLogger.get().error("Error, file to read has not been set");
        else{
            while (scanner.hasNext()){
                stringBuilder.append(scanner.next());
             }
        }
        return stringBuilder.toString();
    }
 }


