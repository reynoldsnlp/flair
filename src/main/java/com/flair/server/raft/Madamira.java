package com.flair.server.raft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * An example class that shows how MADAMIRA can be called through its API.
 *
 */
public class Madamira {
	private String url;
	private org.apache.http.client.HttpClient httpclient;
	private File iFile, oFile;
	private int PORT;
	public Madamira(int PORT, String url, File iFile, File oFile) {
		this.iFile = iFile;
		this.oFile = oFile;
		this.url = url;
		this.PORT = PORT;
		httpclient = new DefaultHttpClient();
	}
	public boolean run() {
		try {
			HttpPost httppost = new HttpPost(url+Integer.toString(PORT));
			InputStreamEntity reqEntity =
			new InputStreamEntity(new FileInputStream(iFile), -1);
			reqEntity.setContentType("application/xml");
			reqEntity.setChunked(true);
			// It may be more appropriate to use FileEntity
			// class in this particular instance but we are using
			// a more generic InputStreamEntity to demonstrate
			// the capability to stream out data from any
			// arbitrary source
			// FileEntity entity =
			// new FileEntity(file, "binary/octet-stream");
			httppost.setEntity(reqEntity);
			HttpResponse response=null;
			HttpEntity resEntity=null;
			try {
					response = httpclient.execute(httppost);
					resEntity = response.getEntity();
			} catch(HttpHostConnectException ex) {
				System.out.println(ex.getMessage());
				return false;
			}
			System.out.println(response.getStatusLine());
			if (resEntity != null) {
				InputStream responseBody = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(responseBody, "utf8"));
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(oFile), "utf8"));
				String line = null;
				StringBuilder sbr = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sbr.append(line+"\n");
				}
				reader.close();
				writer.write(sbr.toString());
				writer.flush();
				writer.close();
			}
			EntityUtils.consume(resEntity);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		//System.out.println("Connected");
		return true;
	}
	// runs client standalone
	public static void lemmatize(int port, String url, String fileIn, String fileOut) {
		Madamira client =
				new Madamira(port, url, new File(fileIn), new File(fileOut));
		client.run();
	}
	/*
    public static void lemmatize(String INPUT_FILE, String OUTPUT_FILE) {
    	
        final MADAMIRAWrapper wrapper = new MADAMIRAWrapper();
        JAXBContext jc = null;

        try {
            jc = JAXBContext.newInstance(MADAMIRA_NS);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            // The structure of the MadamiraInput object is exactly similar to the
            // madamira_input element in the XML
            final MadamiraInput input = (MadamiraInput)unmarshaller.unmarshal(
                    new File( INPUT_FILE ) );

            {
                int numSents = input.getInDoc().getInSeg().size();
                String outputAnalysis = input.getMadamiraConfiguration().
                        getOverallVars().getOutputAnalyses();
                String outputEncoding = input.getMadamiraConfiguration().
                        getOverallVars().getOutputEncoding();

                System.out.println("processing " + numSents +
                        " sentences for analysis type = " + outputAnalysis +
                        " and output encoding = " + outputEncoding);
            }

            // The structure of the MadamiraOutput object is exactly similar to the
            // madamira_output element in the XML
            final MadamiraOutput output = wrapper.processString(input);

            {
                int numSents = output.getOutDoc().getOutSeg().size();

                System.out.println("processed output contains "+numSents+" sentences...");
            }


            jc.createMarshaller().marshal(output, new File(OUTPUT_FILE));


        } catch (JAXBException ex) {
            System.out.println("Error marshalling or unmarshalling data: "
                    + ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("MADAMIRA thread interrupted: "
                    +ex.getMessage());
        } catch (ExecutionException ex) {
            System.out.println("Unable to retrieve result of task. " +
                    "MADAMIRA task may have been aborted: "+ex.getCause());
        }

        wrapper.shutdown();
    }
    */
}