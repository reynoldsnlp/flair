package com.flair.server.utilities.cg3parser.model;

import java.util.ArrayList;
import java.util.List;

public class WordWithReadings {
    //members
    private String surfaceForm;
    private List<String> staticTags = new ArrayList<>();
    private List<CgReading> readings = new ArrayList<>();
    private int wordIndex;

    //constructor
    public WordWithReadings(String surfaceForm, int index) {
        this.surfaceForm = surfaceForm;
        wordIndex = index;
    }

    //functions
    public void addStaticTag(String tag) {
        staticTags.add(tag);
    }
    public void addReading(CgReading reading) {
        readings.add(reading);
    }

    //getters
    public String getSurfaceForm() {
        return surfaceForm;
    }
    public int getIndex() {
        return wordIndex;
    }
    public List<String> getStaticTags() {
        return staticTags;
    }
    public List<CgReading> getReadings() {
        return readings;
    }
    public boolean hasStaticTags() {
        return !staticTags.isEmpty();
    }

}
