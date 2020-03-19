package com.flair.server.raft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.InetSocketAddress;

import com.flair.server.utilities.Madamira;
import com.flair.server.utilities.CustomFileReader;
import com.flair.server.utilities.ServerLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import junit.framework.Assert;
import type.UnitTest;

@Category(UnitTest.class)

public class MadamiraUnitTest 
{
    /*private Madamira madamiraWithConnection;
    private Madamira madamiraNoConnection;
    private String url;
    private String badUrl;
    private String inputString;
    private String inputFileName;
    private int port;
    private int badPort;
    private CustomFileReader customFileReader;
    private InputStream inputStream;
    private MadamiraServerThread serverThread;

    private class MadamiraServerThread extends Thread
    {
        private HttpServer server;
        private boolean goodResponse;

        public MadamiraServerThread()
        {
            goodResponse = true;
            try
            {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            }
            catch(IOException e)
            {
                ServerLogger.get().error(e,"");
                Assert.fail();
            }
        }
        @Override
        public void run()
        {
            server.createContext("/", new RootHandler());
            server.start();
        }
        public void killServer()
        {
           server.stop(1); 
        }
        class RootHandler implements HttpHandler 
        {
            @Override

            public void handle(HttpExchange he) throws IOException 
            {
                try
                {
                    OutputStream responseStream = he.getResponseBody();
                    if(goodResponse)
                    {
                        he.sendResponseHeaders(200, 0);
                        responseStream.write("This is a server response".getBytes());
                        responseStream.flush();
                        responseStream.close();
                    }
                    else
                    {
                        he.close();
                    }
                }
                catch(IOException e)
                {
                    ServerLogger.get().error(e, "Server could not write client request back to client on port " + port);
                    Assert.fail();
                }
            }
        }
        public void setGoodResponse(boolean goodResponse) {
            this.goodResponse = goodResponse;
        }
    }

    @Before
    public void setUp()
    {
        inputFileName = "testFiles/madamiraResponse.xml";
        customFileReader = new CustomFileReader();
        try
        {
            inputString = customFileReader.readFileToString(customFileReader.getRelativePath(), inputFileName);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            inputString = "";
        } 
        url = "http://localhost:";
        port = 9999;
        inputStream = new ByteArrayInputStream(inputString.getBytes());
        madamiraWithConnection = new Madamira(port, url, inputStream);

        badUrl = "http://ThisDoesNotExist:";
        badPort = 1;
        madamiraNoConnection = new Madamira(badPort, badUrl, inputStream);

        serverThread = new MadamiraServerThread();
        serverThread.start();
    }
    @Test
    public void testRunNoConnection()
    {

        Assert.assertNull(madamiraNoConnection.run());
    }
    @Test
    public void testRunWithConnection()
    {
        serverThread.setGoodResponse(true);
        Assert.assertTrue(madamiraWithConnection.run().length() > 0);
    }
    @Test 
    public void testRunIOException()
    {
        serverThread.setGoodResponse(false);
        Assert.assertNull(madamiraWithConnection.run());
    }
    @After
    public void tearDown()
    {
        serverThread.killServer();
    }
*/
}