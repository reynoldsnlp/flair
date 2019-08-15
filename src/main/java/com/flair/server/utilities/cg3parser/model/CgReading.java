package com.flair.server.utilities.cg3parser.model;

import java.util.ArrayList;
import java.util.List;

public class CgReading {
    //members
    private String baseForm;
    private int indentationLevel;
    private List<String> tags = new ArrayList<>();
    private List<CgReading> subReadings = new ArrayList<>();

    //constructor
    public CgReading(String baseForm, int indentationLevel) {
        this.baseForm = baseForm;
        this.indentationLevel = indentationLevel;
    }

    //functions
    public void addTag(String tag) {
        tags.add(tag);
    }
    public void addSubReading(CgReading subReading) {
        subReadings.add(subReading);
    }

    //getters
    public String getBaseForm() {
        return baseForm;
    }
    public int getIndentationLevel() {
        return indentationLevel;
    }
    public List<String> getTags() {
        return tags;
    }
    public List<CgReading> getSubReadings() {
        return subReadings;
    }
    public boolean hasSubreadings() {
        return !subReadings.isEmpty();
    }
}
