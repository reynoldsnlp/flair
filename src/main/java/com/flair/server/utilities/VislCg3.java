package com.flair.server.utilities;

import java.io.*;
import java.net.URL;

public class VislCg3 {
    //constants
    private static final String VISLCG3_EXE = "/cg3/bin/vislcg3.exe";
    private static final URL VISLCG3 = VislCg3.class.getClassLoader().getResource(VISLCG3_EXE);
    private static final String DISAMBIGUATOR_CG3 = "disambiguator-ru.cg3";

    //functions

    public static String runVislCg3(String cgReadings) throws IOException {
        /*cgReadings = "\"<Материал>\"\n" +
                "        \"материал\" N Msc Inan Sg Nom\n" +
                "        \"материал\" N Msc Inan Sg Acc\n" +
                "\"<из>\"\n" +
                "        \"из\" Pr\n" +
                "\"<Википедии>\"\n" +
                "\"<-->\"\n" +
                "        \"--\" PUNCT\n" +
                "\"<свободной>\"\n" +
                "        \"свободный\" A Fem AnIn Sg Gen\n" +
                "        \"свободный\" A Fem AnIn Sg Loc\n" +
                "        \"свободный\" A Fem AnIn Sg Dat\n" +
                "        \"свободный\" A Fem AnIn Sg Ins\n" +
                "\"<энциклопедии>\"\n" +
                "        \"энциклопедия\" N Fem Inan Sg Gen\n" +
                "        \"энциклопедия\" N Fem Inan Sg Loc\n" +
                "        \"энциклопедия\" N Fem Inan Sg Dat\n" +
                "        \"энциклопедия\" N Fem Inan Pl Nom\n" +
                "        \"энциклопедия\" N Fem Inan Pl Acc\n" +
                "\n";*/
        if(VISLCG3 == null) {
            throw new IOException("vislcg3.exe not found.");
        }
        //set up arguments
        ProcessBuilder pb = new ProcessBuilder(VISLCG3.getPath(), "-g", DISAMBIGUATOR_CG3);
        Process process;
        try {
            //start the executable
            process = pb.start();
            //grab the streams
            BufferedOutputStream grammarInput = new BufferedOutputStream(process.getOutputStream());
            BufferedInputStream grammarOutput = new BufferedInputStream(process.getInputStream());
            BufferedInputStream errorOutput = new BufferedInputStream(process.getErrorStream());
            //put data in
            grammarInput.write(cgReadings.getBytes());
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
