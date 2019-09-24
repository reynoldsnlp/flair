package com.flair.server.utilities;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Class to convert HFST output to CG input
 */
public class CgConv {
    //constants
    private static final String CG_CONV_EXE = "/cg3/bin/cg-conv.exe";
    private static final URL CG_CONV = CgConv.class.getClassLoader().getResource(CG_CONV_EXE);
    private static final int TIMEOUT_MS = 10*1000;

    //functions

    public static String hfstToCg(String hfstString) throws IOException {
        if(CG_CONV == null) {
            throw new IOException("cg-conv.exe not found.");
        }

        /*System.out.println("hfstString:");
        System.out.println(hfstString);*/

        //set up arguments
        ProcessBuilder pb = new ProcessBuilder(CG_CONV.getPath(), "-f");
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
            System.out.println("stringBytes.length = " + stringBytes.length);
            converterInput.write(stringBytes);
            converterInput.flush();
            converterInput.close();
            //set timeout
            ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
            int exitCode = processWithTimeout.waitForProcess(TIMEOUT_MS);
            if(exitCode == Integer.MIN_VALUE) { //timeout!
                process.destroyForcibly();
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
