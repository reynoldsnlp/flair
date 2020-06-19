package com.flair.server.utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Class to convert HFST output to CG input
 */
public class CgConv {
    //constants
    private static final String CG_CONV = "cg-conv";
    private static final int TIMEOUT_MS = 10*1000;
    private static int failureCount = 0;

    //functions

    public static String hfstToCg(String hfstString) throws IOException {
        //set up arguments
        ProcessBuilder pb = new ProcessBuilder(CG_CONV, "-f");
        Process process;
        try {
            //start the executable
            process = pb.start();
            //grab the streams
            BufferedOutputStream converterInput = new BufferedOutputStream(process.getOutputStream());
            BufferedInputStream converterOutput = new BufferedInputStream(process.getInputStream());
            BufferedInputStream errorOutput = new BufferedInputStream(process.getErrorStream());
            //put data in
            byte[] stringBytes = hfstString.getBytes(StandardCharsets.UTF_8);
            //System.out.println("stringBytes.length = " + stringBytes.length);
            converterInput.write(stringBytes);
            converterInput.flush();
            converterInput.close();
            //set timeout
            ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
            int exitCode = processWithTimeout.waitForProcess(TIMEOUT_MS);
            if(exitCode == Integer.MIN_VALUE) { //timeout!
                process.destroyForcibly();
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cg-conv-fail" + ++failureCount + ".txt"), StandardCharsets.UTF_8));
                    out.write(hfstString);
                    out.close();
                } catch (FileNotFoundException e) {
                    ServerLogger.get().error("Could not write to error file");
                }
                throw new InterruptedException("cg-conv timed out after " + TIMEOUT_MS + " milliseconds");
            }
            //forward output data
            String outputString = StreamToString.convertStreamToString(converterOutput);
            converterOutput.close();
            String errorString = StreamToString.convertStreamToString(errorOutput);
            if(!errorString.isEmpty()) {
                ServerLogger.get().error("cg-conv error: " + errorString);
            }
            errorOutput.close();
            return outputString;
        } catch (IOException | InterruptedException e) {
            ServerLogger.get().error(e.toString());
            throw new IOException("cg-conv failed. See log for details");
        }
    }
}
