package com.flair.server.utilities;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Madamira
{
	private String url;
	private InputStream inputStream;
	private int PORT;
	private HttpClientBuilder clientBuilder;
	private CloseableHttpClient closeableHttpClient;

	private static Document madaOutput;
	private static String input;
	private static String output;
	private static int taskSalt;

	private static String madamiraTop = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<madamira_input xmlns=\"urn:edu.columbia.ccls.madamira.configuration:0.1\">\r\n"
			+ "	<madamira_configuration>\r\n"
			+ "        <preprocessing sentence_ids=\"false\" separate_punct=\"true\" input_encoding=\"UTF8\"/>\r\n"
			+ "        <overall_vars output_encoding=\"UTF8\" dialect=\"MSA\" output_analyses=\"TOP\" morph_backoff=\"NOAN_ALL\" analyze_only=\"false\"/>\r\n"
			+ "        <requested_output>\r\n"
			+ "            <req_variable name=\"PREPROCESSED\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"STEM\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"GLOSS\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"LEMMA\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"DIAC\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"ASP\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"CAS\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"ENC0\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"ENC1\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"ENC2\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"GEN\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"MOD\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"NUM\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PER\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"POS\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PRC0\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PRC1\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PRC2\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"PRC3\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"STT\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"VOX\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"BW\" value=\"true\" />\r\n"
			+ "            <req_variable name=\"SOURCE\" value=\"true\" />\r\n"
			+ "			<req_variable name=\"NER\" value=\"true\" />\r\n"
			+ "			<req_variable name=\"BPC\" value=\"true\" />\r\n" + "        </requested_output>\r\n"
			+ "	</madamira_configuration>\r\n" + "    <in_doc id=\"ExampleDocument\">";

	private static String madamiraBottom = "</in_doc>\r\n" + "</madamira_input>";

	public Madamira(int PORT, String url, InputStream inputStream)
	{
		this.url = url;
		this.PORT = PORT;
		this.inputStream = inputStream;
		clientBuilder = HttpClientBuilder.create();
		closeableHttpClient = clientBuilder.build();
	}

	/**
	 * Creates XML input string for Madamira out of the body, saves it to
	 * madamiraInput, sends it off to be lemmatized and saves output to
	 * madamiraOutput.
	 */
	public static Document getMadaDocument(String body)
	{
		if (body != null)
		{
			/*try {
				body = body.trim().replaceAll("&", "+");
				body = body.trim().replaceAll("(\\s)+", "$1");
			} catch (NullPointerException e) {
				ServerLogger.get().error(e, e.getMessage());
				body = null;
			}*/

			Random r = new Random();
			taskSalt = r.nextInt(10000000); // gives a random number to salt our file names with
			input = "/tmp/mada_input" + taskSalt + ".txt";
			output = "/tmp/mada_output" + taskSalt + ".txt";
			StringBuilder inputBuilder = new StringBuilder();
			inputBuilder.append(madamiraTop + "\n\n");
			String[] bodyStrings = body.split("\n");
			int segCount = 0;
			for (String s : bodyStrings)
			{
				inputBuilder.append(
						"<in_seg id=\"BODY_" + Integer.toString(segCount) + "\">" + /*makeArabicOnly(s)*/ s + "</in_seg>\n");
				segCount++;
			}
			inputBuilder.append("\n\n" + madamiraBottom);
			InputStream inputStream = new ByteArrayInputStream(inputBuilder.toString().getBytes(StandardCharsets.UTF_8));
			String outputString;

			outputString = runClient(8223, "http://icall.byu.edu:", inputStream);
			if (outputString == null)
			{
				ServerLogger.get().error("failed to connect to mada_image, now trying to connect on localhost");
				outputString = runClient(8223, "http://localhost:", inputStream);
			}

			//checks if localhost result is null as well, if so a blank document is created
			if (outputString == null)
			{
				ServerLogger.get().error("failed to connect to localhost, make sure that madamira server is running");
				madaOutput = new Document(""); // creates a new empty document
			}
			else
			{
				// ServerLogger.get().info("outputString : \n" + outputString);
				madaOutput = Jsoup.parse(outputString);
			}
		}
		else
		{
			ServerLogger.get().error("Body is null, creating new empty document");
			madaOutput = new Document(""); // creates a new empty document
		}
		return madaOutput;
	}

	public String run()
	{
			StringBuilder sbr = new StringBuilder();
			try
			{
				HttpPost httppost = new HttpPost(url+Integer.toString(PORT));
				InputStreamEntity reqEntity = new InputStreamEntity(inputStream, -1);
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
	public static String runClient(int port, String url, InputStream inputStream)
	{
		Madamira client = new Madamira(port, url, inputStream);
		return 	client.run();
	}
}