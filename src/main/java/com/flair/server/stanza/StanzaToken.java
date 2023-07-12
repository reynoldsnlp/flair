package com.flair.server.stanza;

import java.util.ArrayList;
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
    private static final Pattern featsPattern = Pattern.compile("[^=]*=([^|]*)\\|?");
    private ArrayList<String> featsList = new ArrayList<String>();
    private int head;
    private String deprel;
    private String misc;
    private Integer start;
    private static final Pattern startPattern = Pattern.compile("start_char=([0-9]+)");
    private Integer end;
    private static final Pattern endPattern = Pattern.compile("end_char=([0-9]+)");

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

    public ArrayList<String> getFeats() {
        if (feats == null) {
            return featsList;
        } else if (!featsList.isEmpty()) {
            return featsList;
        } else {
            Matcher featsMatcher = featsPattern.matcher(feats);
            while (featsMatcher.find()) {
                featsList.add(featsMatcher.group(1));
            }
            return featsList;
        }
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

    public Integer getStart() {
        if (start != null) {
            return start;
        }
        try {
            if (misc != null) {
                Matcher startMatcher = startPattern.matcher(misc);
                startMatcher.find();
                String startStr = startMatcher.group(1);
                start = Integer.parseInt(startStr);
                return start;
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
        if (end != null) {
            return end;
        }
        try {
            if (misc != null) {
                Matcher endMatcher = endPattern.matcher(misc);
                endMatcher.find();
                String endStr = endMatcher.group(1);
                end = Integer.parseInt(endStr);
                return end;
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
