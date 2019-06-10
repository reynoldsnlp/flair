package com.flair.server.utilities;

import java.io.InputStream;

public abstract class StreamToString {
    /** converts an InputStream object to a String containing the same data */
    public static String convertStreamToString(InputStream is) {
        //using magical Java byte voodoo
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
