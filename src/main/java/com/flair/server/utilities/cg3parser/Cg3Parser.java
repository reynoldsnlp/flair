package com.flair.server.utilities.cg3parser;

import com.drew.lang.annotations.Nullable;
import com.flair.server.utilities.ServerLogger;
import com.flair.server.utilities.cg3parser.model.CgReading;
import com.flair.server.utilities.cg3parser.model.SurfaceFormLine;
import com.flair.server.utilities.cg3parser.model.WordWithReadings;
import java.util.ArrayList;
import java.util.List;

public class Cg3Parser {
    //members
    private String source;
    private boolean parsed = false;
    private List<CgTokenizer.CgToken> tokenList;
    private int tokenIndex = 0;
    private List<WordWithReadings> allReadings = null;

    //constructor
    public Cg3Parser(String source) {
        this.source = source;
    }

    //custom exception
    private static class CgSyntaxError extends Exception {
        CgSyntaxError(String message) {
            super(message);
        }
    }

    //functions

    public @Nullable List<WordWithReadings> parse() {
        //if we've already parsed this text, return what we got
        if(parsed) return allReadings;
        //tokenize the input
        CgTokenizer tokenizer = new CgTokenizer(source);
        tokenList = tokenizer.tokenize();
        if(tokenList == null) {
            ServerLogger.get().error("Token list not created");
            parsed = true;
            return null;
        }
        System.out.println("Token list created (section in progress)");
        //get and save our result
        try {
            allReadings = parseWordWithReadingsList();
        } catch (CgSyntaxError e) {
            ServerLogger.get().error("Cg3Parser syntax error: " + e.toString());
            return null;
        } finally {
            parsed = true;
        }
        return allReadings;
    }

    private boolean checkTokenIndex(boolean willThrow) throws CgSyntaxError {
        if(willThrow && tokenIndex >= tokenList.size()) throw new CgSyntaxError("Expected a token, but there were no more");
        return (tokenIndex < tokenList.size());
    }

    //RECURSIVE DESCENT PARSER FUNCTIONS

    private List<WordWithReadings> parseWordWithReadingsList() throws CgSyntaxError {
        if(!checkTokenIndex(false)) return new ArrayList<>();
        CgTokenizer.CgToken nextToken = tokenList.get(tokenIndex);
        if(nextToken.isSurfaceForm()) {
            //found a surface form
            WordWithReadings reading = parseWordWithReadings();
            List<WordWithReadings> readingsList = parseWordWithReadingsList();
            readingsList.add(0, reading);
            return readingsList;
        }
        else { //lambda
            return new ArrayList<>();
        }
    }

    private WordWithReadings parseWordWithReadings() throws CgSyntaxError {
        SurfaceFormLine surfaceFormLine = parseSurfaceFormLine();
        List<CgReading> cgReadingList = parseCgReadingList();
        WordWithReadings word = new WordWithReadings(surfaceFormLine.getSurfaceForm());
        for(String staticTag: surfaceFormLine.getStaticTags()) {
            word.addStaticTag(staticTag);
        }
        for(CgReading reading: cgReadingList) {
            word.addReading(reading);
        }
        return word;
    }

    private SurfaceFormLine parseSurfaceFormLine() throws CgSyntaxError {
        //"<surface form>"
        checkTokenIndex(true);
        CgTokenizer.CgToken nextToken = tokenList.get(tokenIndex++);
        if(!nextToken.isSurfaceForm()) {
            throw new CgSyntaxError("parseSurfaceFormLine expected a surface form. Instead, it received: " + nextToken.toString());
        }
        //StringList
        List<String> staticTags = parseStringList();
        return new SurfaceFormLine(nextToken.getValue(), staticTags);
    }

    private List<CgReading> parseCgReadingList() throws CgSyntaxError {
        if(!checkTokenIndex(false)) return new ArrayList<>();
        CgTokenizer.CgToken nextToken = tokenList.get(tokenIndex);
        if(nextToken.isWhitespace()) {
            CgReading reading = parseCgReading();
            int whitespaceLevel = reading.getIndentationLevel();
            //LOOK AHEAD
            if(checkTokenIndex(false)) {
                CgTokenizer.CgToken queuedToken = tokenList.get(tokenIndex);
                if (queuedToken.isWhitespace()) {
                    if (queuedToken.length() > whitespaceLevel) {
                        throw new CgSyntaxError("Indentation invalid: " + queuedToken.toString());
                    } else if (queuedToken.length() == whitespaceLevel) {
                        List<CgReading> readingList = parseCgReadingList();
                        readingList.add(0, reading);
                        return readingList;
                    }
                    //else(queuedToken.length() < whitespaceLevel)
                    //lambda
                }
            }
            //if we get here, it was either not whitespace or the wrong length
            List<CgReading> toReturn = new ArrayList<>(1);
            toReturn.add(reading);
            return toReturn;
        }
        else { //lambda
            return new ArrayList<>();
        }
    }

    private CgReading parseCgReading() throws CgSyntaxError {
        //whitespace
        checkTokenIndex(true);
        CgTokenizer.CgToken nextToken = tokenList.get(tokenIndex++);
        if(!nextToken.isWhitespace()) {
            throw new CgSyntaxError("Expected a whitespace token, but found: " + nextToken.toString());
        }
        int indentationLevel = nextToken.length();
        //dictionary form
        checkTokenIndex(true);
        nextToken = tokenList.get(tokenIndex++);
        if(!nextToken.isDictionaryForm()) {
            throw new CgSyntaxError("Expected a dictionary form token, but found: " + nextToken.toString());
        }
        String dictionaryForm = nextToken.getValue();
        //string list
        List<String> tags = parseStringList(); //TODO: FIX
        //construct the CgReading
        CgReading toReturn = new CgReading(dictionaryForm, indentationLevel);
        for(String tag: tags) {
            toReturn.addTag(tag);
        }
        //LOOK AHEAD
        if(checkTokenIndex(false)) {
            CgTokenizer.CgToken queuedToken = tokenList.get(tokenIndex);
            if(queuedToken.isWhitespace()){
                if(queuedToken.length() > indentationLevel) {
                    List<CgReading> readingList = parseCgReadingList();
                    for(CgReading subReading: readingList) {
                        toReturn.addSubReading(subReading);
                    }
                }
                //else(queuedToken.length() <= indentationLevel)
                //lambda
            }
        }
        return toReturn;
    }

    private List<String> parseStringList() throws CgSyntaxError {
        if(!checkTokenIndex(false)) return new ArrayList<>();
        CgTokenizer.CgToken nextToken = tokenList.get(tokenIndex);
        if(nextToken.isWhitespace() || nextToken.isDictionaryForm()) return new ArrayList<>();
        //TODO: this doesn't handle the spaces vs tabs correctly
        //we have a proper word to use
        tokenIndex++;
        List<String> stringList = parseStringList();
        stringList.add(0, nextToken.getValue());
        return stringList;
    }
}
