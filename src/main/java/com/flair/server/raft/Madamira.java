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


import com.flair.server.utilities.ServerLogger;

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
	private InputStream iInputStream;
	private int PORT;
	public Madamira(int PORT, String url, File iFile, File oFile) {
		this.iFile = iFile;
		this.iInputStream = null;
		this.oFile = oFile;
		this.url = url;
		this.PORT = PORT;
		httpclient = new DefaultHttpClient();
	}

	public Madamira(int PORT, String url, InputStream iInputStream, File oFile) {
		this.iInputStream = iInputStream;
		this.iFile = null;
		this.oFile = oFile;
		this.url = url;
		this.PORT = PORT;
		httpclient = new DefaultHttpClient();
	}
	public String run() {
		if(this.iFile != null) {
			try {
				ServerLogger.get().info("using iFile");
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
					ServerLogger.get().error(ex, "Caught connection exception " + ex.getMessage() + " now returing null");
					return null;
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
				ServerLogger.get().error(ioe, "caught exception " + ioe.getMessage() + " returning null");
				httpclient.getConnectionManager().shutdown();
				return null;
			} finally {
				// When HttpClient instance is no longer needed,
				// shut down the connection manager to ensure
				// immediate deallocation of all system resources
				httpclient.getConnectionManager().shutdown();
			}
			return null;
		}
		else {
			StringBuilder sbr = new StringBuilder();
			try {
				ServerLogger.get().info("using iFileInputStream");
				HttpPost httppost = new HttpPost(url+Integer.toString(PORT));
				InputStreamEntity reqEntity =
				new InputStreamEntity(iInputStream, -1);
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
					ServerLogger.get().error(ex, "Caught connection exception " + ex.getMessage() + " now returing null");
					return null;
				}
				System.out.println(response.getStatusLine());
				if (resEntity != null) {
					InputStream responseBody = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(responseBody, "utf8"));
					String line = null;
					while ((line = reader.readLine()) != null) {
						sbr.append(line+"\n");
					}
					reader.close();
				}
				EntityUtils.consume(resEntity);
			} catch(IOException ioe) {
				ServerLogger.get().error(ioe, "caught exception " + ioe.getMessage() + " returning null");
				httpclient.getConnectionManager().shutdown();
				return null;
			} finally {
				// When HttpClient instance is no longer needed,
				// shut down the connection manager to ensure
				// immediate deallocation of all system resources
				httpclient.getConnectionManager().shutdown();
			}
			return sbr.toString();
		}
		
	}
	// runs client standalone
	public static void lemmatize(int port, String url, String fileIn, String fileOut) {
		Madamira client =
				new Madamira(port, url, new File(fileIn), new File(fileOut));
		client.run();
	}
	// runs client standalone
	public static String lemmatize(int port, String url, InputStream inputStreamIn, String fileOut) {
		Madamira client =
				new Madamira(port, url, inputStreamIn, new File(fileOut));
		return 	client.run();
	}
}