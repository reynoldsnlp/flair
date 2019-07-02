package com.flair.server.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class StreamToString {
    /** converts an InputStream object to a String containing the same data */
    public static String convertStreamToString(InputStream is) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        try {
            while (reader.ready()) {
                builder.append(reader.readLine());
                builder.append('\n');
            }
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        /*//using magical Java byte voodoo
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";*/
    }
}
