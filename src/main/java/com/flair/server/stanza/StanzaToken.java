package com.flair.server.stanza;

import java.util.regex.Pattern;

import com.flair.client.utilities.ClientLogger;

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
    private Pattern startMatcher = Pattern.compile("start_char=([0-9]+)");
    private Pattern endMatcher = Pattern.compile("end_char=([0-9]+)");

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
      try {
        String startStr = startMatcher.matcher(misc).group(1);
        int startInt = Integer.parseInt(startStr);
        return startInt;
      }
      catch (IllegalStateException e) {
        ClientLogger.get().error(e, "No start index: misc=" + misc);
        return 0;
      }
    }
    public int getEnd() {
      try {
        String endStr = endMatcher.matcher(misc).group(1);
        int endInt = Integer.parseInt(endStr);
        return endInt;
      }
      catch (IllegalStateException e) {
        ClientLogger.get().error(e, "No end index: misc=" + misc);
        return 0;
      }
    }
    @Override
    public String toString() {
      return "Token [" + id + " \"" + text + "\"]";
    }
  }
