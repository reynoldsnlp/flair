package com.flair.server.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Class to convert HFST output to CG input
 */
public class CgConv {
    //constants
    private static final String CG_CONV_EXE = "/cg-conv.exe";

    //functions

    public static String hfstToCg(String hfstString) throws IOException {
        //set up arguments
        ProcessBuilder pb = new ProcessBuilder(CG_CONV_EXE, "-f", "--in-fst", "-C", "--out-cg");
        Process process;
        try {
            //start the executable
            process = pb.start();
            //grab the streams
            BufferedOutputStream converterInput = new BufferedOutputStream(process.getOutputStream());
            BufferedInputStream converterOutput = new BufferedInputStream(process.getInputStream());
            BufferedInputStream errorOutput = new BufferedInputStream(process.getErrorStream());
            //put data in
            converterInput.write(hfstString.getBytes());
            converterInput.close();
            //forward output data
            String outputString = StreamToString.convertStreamToString(converterOutput);
            String errorString = StreamToString.convertStreamToString(errorOutput);
            return outputString;
        } catch (IOException e) {
            e.printStackTrace(); //TODO: where do we put this error?
            throw new IOException("cg-conv failed. See log for details");
        }
    }
}
