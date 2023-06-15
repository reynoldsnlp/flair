package com.flair.server.stanza;

import java.util.regex.Pattern;

public class StanzaToken {
    private String id;
    private String text;
    private String lemma;
    private String upos;
    private String xpos;
    private String feats;
    private int head;
    private String deprel;
    private String misc;
    private Pattern startMatcher = Pattern.compile("start_char=(\\d+)");
    private Pattern endMatcher = Pattern.compile("end_char=(\\d+)");
  
    public String getId() {
      return id;
    }
    public String getText() {
      return text;
    }
    public String getLemma() {
      return lemma;
    }
    public String getUpos() {
      return upos;
    }
    public String getXpos() {
      return xpos;
    }
    public String getFeats() {
      return feats;
    }
    public int getHead() {
      return head;
    }
    public String getDeprel() {
      return deprel;
    }
    public String getMisc() {
      return misc;
    }
    public int getStart() {
      return Integer.parseInt(startMatcher.matcher(misc).group(1));
    }
    public int getEnd() {
      return Integer.parseInt(endMatcher.matcher(misc).group(1));
    }
    @Override
    public String toString() {
      return "Token [" + id + " \"" + text + "\"]";
    }
  }