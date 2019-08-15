package com.flair.server.utilities.cg3parser.model;

import java.util.List;

public class SurfaceFormLine {
    private String surfaceForm;
    private List<String> staticTags;

    public SurfaceFormLine(String surfaceForm, List<String> staticTags) {
        this.surfaceForm = surfaceForm;
        this.staticTags = staticTags;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public List<String> getStaticTags() {
        return staticTags;
    }
}
