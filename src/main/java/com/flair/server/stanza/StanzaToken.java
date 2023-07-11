package com.flair.server.stanza;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flair.server.utilities.ServerLogger;

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
    private Pattern startPattern = Pattern.compile("start_char=([0-9]+)");
    private Pattern endPattern = Pattern.compile("end_char=([0-9]+)");

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
            if (misc != null) {
                Matcher startMatcher = startPattern.matcher(misc);
                startMatcher.find();
                String startStr = startMatcher.group(1);
                int startInt = Integer.parseInt(startStr);
                return startInt;
            } else {
                ServerLogger.get().warn("misc is null:" + this.toString());
                return 0;
            }
        } catch (IllegalStateException e) {
            ServerLogger.get().error(e, "No start index: misc=" + misc);
            return 0;
        }
    }

    public int getEnd() {
        try {
            if (misc != null) {
                Matcher endMatcher = endPattern.matcher(misc);
                endMatcher.find();
                String endStr = endMatcher.group(1);
                int endInt = Integer.parseInt(endStr);
                return endInt;
            } else {
                ServerLogger.get().warn("misc is null:" + this.toString());
                return 0;
            }
        } catch (IllegalStateException e) {
            ServerLogger.get().error(e, "No end index: misc=" + misc);
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Token [" + id + " \"" + text + "\"]";
    }

    // TODO add toJson() method
}
