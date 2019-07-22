package com.flair.server.utilities.cg3parser;

import com.drew.lang.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

class CgTokenizer {
    //members
    private String source;
    private int lineCount = 0;
    private int charIndex = 0;
    private List<CgToken> tokenList = new ArrayList<>();

    //constructor
    CgTokenizer(String source) {
        this.source = source;
    }

    class CgToken extends Throwable {
        //members
        private String value;
        private int lineNumber;
        //constructor
        CgToken(String value, int lineNumber) {
            this.value = value;
            this.lineNumber = lineNumber;
        }

        //functions
        int length() {
            return value.length();
        }
        boolean isWhitespace() {
            for(int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if(!Character.isWhitespace(c)) {
                    return false;
                }
            }
            return true;
        }
        boolean isSurfaceForm() {
            int len = value.length();
            return (len >= 4 &&
                    value.substring(0, 2).equals("\"<") &&
                    value.substring(len-2, len).equals(">\""));
        }
        public boolean isDictionaryForm() {
            int len = value.length();
            return (len >= 2 &&
                    value.substring(0, 1).equals("\"") &&
                    value.substring(len-1, len).equals("\""));
        }
        
        //generated
        @Override
        public String toString() {
            return "CgToken{" + value + ", line " + lineNumber + '}';
        }
        //getters
        public String getValue() {
            return value;
        }
        public int getLineNumber() {
            return lineNumber;
        }
    }

    //functions

    @Nullable List<CgToken> tokenize() {
        try {
            while (charIndex < source.length()) {
                char c = source.charAt(charIndex);
                switch (c) {
                    case '\n':
                        lineCount++;
                    case '\r':
                        charIndex++;
                        break;
                    case '"':
                        quoteToken();
                        break;
                    default:
                        if(Character.isWhitespace(c)) indentToken();
                        else tagToken();
                }
            }
        } catch(CgToken badToken) {
            System.out.println("Could not tokenize readings. Bad token:");
            System.out.println(badToken.toString());
            System.out.println(tokenList.size() + " tokens already saved.");
            return null;
        }
        return tokenList;
    }

    private void quoteToken() throws CgToken {
        if(++charIndex >= source.length()) throw new CgToken("\"", lineCount);
        int startLineNum = lineCount;
        StringBuilder quoteBlockBuilder = new StringBuilder("\"");
        boolean properClose = false;
        while(charIndex < source.length()) {
            char c = source.charAt(charIndex++);
            quoteBlockBuilder.append(c);
            if(c == '"') {
                properClose = true;
                break;
            }
            else if(c == '\\') {
                if(charIndex < source.length() && source.charAt(charIndex) == '"') {
                    quoteBlockBuilder.append(source.charAt(charIndex++));
                }
            }
            else if(c == '\n') throw new CgToken("\\n", lineCount);
        }
        CgToken newToken = new CgToken(quoteBlockBuilder.toString(), startLineNum);
        if(!properClose) throw newToken;
        tokenList.add(newToken);
    }

    private void indentToken() {
        StringBuilder indentBuilder = new StringBuilder();
        while(charIndex < source.length()) {
            char c = source.charAt(charIndex);
            if(c == '\n' || c == '\r' || !Character.isWhitespace(c)) break;
            charIndex++;
            indentBuilder.append(c);
        }
        String value = indentBuilder.toString();
        if(value.isEmpty()) return;
        tokenList.add(new CgToken(value, lineCount));
    }

    private void tagToken() {
        StringBuilder builder = new StringBuilder();
        while(charIndex < source.length()) {
            char c = source.charAt(charIndex);
            if(Character.isWhitespace(c)) break;
            charIndex++;
            builder.append(c);
        }
        tokenList.add(new CgToken(builder.toString(), lineCount));
    }
}
