package com.flair.server.utilities;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class VislCg3 {
    //constants
    private static final String VISLCG3_RELATIVE = "vislcg3";
    private static final URL VISLCG3 = VislCg3.class.getClassLoader().getResource(VISLCG3_RELATIVE);
    private static final String DISAMBIGUATOR_RELATIVE = "/disambiguator-ru.cg3";
    private static final File DISAMBIGUATOR_FILE = new File(VislCg3.class.getClassLoader().getResource(DISAMBIGUATOR_RELATIVE).getPath());

    //functions

    public static String runVislCg3(String cgReadings) throws IOException {
        if(VISLCG3 == null) {
            throw new IOException("vislcg3 not found.");
        }
        //set up arguments
        ProcessBuilder pb = new ProcessBuilder(VISLCG3.getPath(), "-g", DISAMBIGUATOR_FILE.getAbsolutePath());
        Process process;
        try {
            //start the executable
            process = pb.start();
            //grab the streams
            BufferedOutputStream grammarInput = new BufferedOutputStream(process.getOutputStream());
            BufferedInputStream grammarOutput = new BufferedInputStream(process.getInputStream());
            BufferedInputStream errorOutput = new BufferedInputStream(process.getErrorStream());
            //put data in
            grammarInput.write(cgReadings.getBytes(StandardCharsets.UTF_8));
            grammarInput.flush();
            grammarInput.close();
            process.waitFor();
            //forward output data
            String outputString = StreamToString.convertStreamToString(grammarOutput);
            grammarOutput.close();
            String errorString = StreamToString.convertStreamToString(errorOutput);
            if(!errorString.isEmpty()) {
                ServerLogger.get().error("vislcg3 error: " + errorString);
            }
            errorOutput.close();
            return outputString;
        } catch (IOException | InterruptedException e) {
            ServerLogger.get().error(e.toString());
            throw new IOException("vislcg3 failed. See log for details");
        }
    }
}
