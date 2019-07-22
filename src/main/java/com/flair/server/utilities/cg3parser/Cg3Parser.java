package com.flair.server.utilities.cg3parser;

import com.drew.lang.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Cg3Parser {
    //members
    private String source;
    private boolean parsed = false;
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
        List<CgTokenizer.CgToken> tokenList = tokenizer.tokenize();
        //make a place to store our result
        List<WordWithReadings> parsedReadings = new ArrayList<>();

        //TODO: use the tokens to create readings (recursive descent parser)
        System.out.println("Token list created (section in progress)");
        return null;
    }
}
