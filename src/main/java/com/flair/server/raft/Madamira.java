package com.flair.server.raft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.flair.server.utilities.ServerLogger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * An example class that shows how MADAMIRA can be called through its API.
 *
 */
public class Madamira 
{
	private String url;
	private InputStream iInputStream;
	private int PORT;
	private HttpClientBuilder clientBuilder;
	private CloseableHttpClient closeableHttpClient;

	public Madamira(int PORT, String url, InputStream iInputStream) 
	{
		this.iInputStream = iInputStream;
		this.url = url;
		this.PORT = PORT;
		clientBuilder = HttpClientBuilder.create();
		closeableHttpClient = clientBuilder.build();
	}
	public String run() 
	{
			StringBuilder sbr = new StringBuilder();
			try 
			{
				HttpPost httppost = new HttpPost(url+Integer.toString(PORT));
				InputStreamEntity reqEntity =
				new InputStreamEntity(iInputStream, -1);
				reqEntity.setContentType("application/xml");
				reqEntity.setChunked(true);
				httppost.setEntity(reqEntity);
				HttpResponse response=null;
				HttpEntity resEntity=null;
				try 
				{
					response = closeableHttpClient.execute(httppost);
					resEntity = response.getEntity();
				} 
				catch(HttpHostConnectException ex) 
				{
					ServerLogger.get().error(ex, "Caught HttpHostConnectException " + ex.getMessage() + " returing null");
					return null;
				}
				if (resEntity != null) 
				{
					InputStream responseBody = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(responseBody, "utf8"));
					String line = null;
					while ((line = reader.readLine()) != null) 
					{
						sbr.append(line+"\n");
					}
					reader.close();
				}
				EntityUtils.consume(resEntity);
				closeableHttpClient.close();
			} 
			catch(IOException ioe) 
			{
				ServerLogger.get().error(ioe, "caught IOException " + ioe.getMessage() + " returning null");
				return null;
			}
			return sbr.toString();		
	}
	// runs client standalone
	public static String lemmatize(int port, String url, InputStream inputStreamIn) 
	{
		Madamira client = new Madamira(port, url, inputStreamIn);
		return 	client.run();
	}
}