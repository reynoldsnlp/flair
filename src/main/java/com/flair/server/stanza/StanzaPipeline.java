package com.flair.server.stanza;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.flair.server.parser.AbstractDocument;
import com.flair.server.utilities.ServerLogger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.text.StringEscapeUtils;

public class StanzaPipeline {

  public List<List<StanzaToken>> process(AbstractDocument doc, String stanzaLang) {  // lang is the Stanza language identifier
    //String stringURL = "http://localhost:8088/analyze";  //until Stanza's servers are fixed
    String stringURL = "http://icall.byu.edu:8088/analyze";
		ServerLogger.get().info("StanzaPipeline: (" + stanzaLang + "): " + doc.getText().substring(0,50) + "...");
    try {
      URL url = new URL(stringURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");
      con.setDoOutput(true);

      // Create JSON object
      String jsonInputString = "{\"text\": \"" + StringEscapeUtils.escapeJava(doc.getText()) + "\", \"lang\": \"" + stanzaLang + "\"}";

      // Send POST request
      con.getOutputStream().write(jsonInputString.getBytes());
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String output;
      StringBuilder response = new StringBuilder();
      while ((output = in.readLine()) != null) {
        response.append(output);
      }
      in.close();
      ServerLogger.get().info("StanzaPipeline: (response): " + response);

      // Parse JSON response into Java object
      Gson gson = new Gson();
      JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
      List<List<StanzaToken>> tokensList = new ArrayList<>();
      for (JsonElement jsonElement : jsonArray) {
        List<StanzaToken> tokens = new ArrayList<>();
        JsonArray jsonArray2 = jsonElement.getAsJsonArray();
        for (JsonElement jsonElement2 : jsonArray2) {
          StanzaToken token = gson.fromJson(jsonElement2, StanzaToken.class);
          tokens.add(token);
        }
        tokensList.add(tokens);
      }
      return tokensList;

    }
    catch (MalformedURLException e) {
      ServerLogger.get().error(e, "Malformed URL: " + stringURL);
    }
    catch (ProtocolException e) {
      ServerLogger.get().error(e, "ProtocolException in StanzaPipeline.");
    }
    catch (IOException e) {
      ServerLogger.get().error(e, "IOException in StanzaPipeline.");
    }
    catch (Exception e) {
      throw e;
    }

    return new ArrayList<>();

  }
}
