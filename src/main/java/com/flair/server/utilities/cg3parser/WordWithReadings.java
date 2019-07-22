package com.flair.server.utilities.cg3parser;

import java.util.ArrayList;
import java.util.List;

public class WordWithReadings {
    //members
    private String surfaceForm;
    private List<String> staticTags = new ArrayList<>();
    private List<CgReading> readings = new ArrayList<>();

    //constructor
    WordWithReadings(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }

    //functions
    void addStaticTag(String tag) {
        staticTags.add(tag);
    }
    void addReading(CgReading reading) {
        readings.add(reading);
    }

    //getters
    public String getSurfaceForm() {
        return surfaceForm;
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
