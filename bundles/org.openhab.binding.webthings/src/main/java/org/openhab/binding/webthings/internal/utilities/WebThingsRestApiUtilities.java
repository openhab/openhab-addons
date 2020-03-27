/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.webthings.internal.utilities;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link WebThingsRestApiUtilities} is for handling REST API calls
 *
 * @author schneider_sven - Initial contribution
 */
@NonNullByDefault
public class WebThingsRestApiUtilities {
    private static String apiResponse ="";
    private static StringBuilder stringBuilder = new StringBuilder();

    /**
     * get all WebThings via REST API
     * @param link Link to WebThing
     * @param security Security Type
     * @param securityToken Required authorization data
     * @return List of WebThings
     * @throws IOException Invalid link
     */
    public static List<JsonObject> getAllWebThings(String link, String security, String securityToken) throws IOException{
        return getAllWebThings(link, security, securityToken, "");
    }

    /**
     * get all WebThings via REST API
     * @param link Link to WebThing
     * @param security Security Type
     * @param securityToken Required authorization data
     * @param additionalLink Add additional sub-link to get property/actions/events
     * @return List of WebThings
     * @throws IOException Invalid link
     */
    public static List<JsonObject> getAllWebThings(String link, String security, String securityToken, String additionalLink) throws IOException{
        if(!link.contains("http")){
            link = "https://" + link;
        }


        apiResponse = new String();
        String destUri;
        if(link.charAt(link.length() -1) == '/'){
            destUri = link + additionalLink + getSecurityLink(security, securityToken);
        }else{
            destUri = link + "/" + additionalLink + getSecurityLink(security, securityToken);
        }
        
        
        URL destUrl = new URL(destUri);
        HttpURLConnection httpUrlConnection = setConnection(destUrl, "GET");

        // Set Header
        httpUrlConnection.setRequestProperty("Accept", "application/json");

        //sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(httpUrlConnection);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        // Convert string into ThingDTO
        Gson g = new Gson();
        List<JsonObject> things = g.fromJson(apiResponse, new TypeToken<ArrayList<JsonObject>>(){}.getType());

        closeConnection(httpUrlConnection);
        return things;
    }

    /**
     * get single WebThing Thing via REST API
     * @param link Link to WebThing
     * @param security Security Type
     * @param securityToken Required authorization data
     * @return WebThing
     * @throws IOException Thing does not exist
     */
    public static JsonObject getWebThing(String link, String security, String securityToken) throws IOException{
        return getWebThing(link, security, securityToken, "");
    }

    /**
     * get single WebThing Thing/Properties/Actions/Events via REST API
     * @param link Link to WebThing
     * @param security Security Type
     * @param securityToken Required authorization data
     * @param additionalLink Add additional sub-link to get property/actions/events
     * @return WebThing
     * @throws IOException Thing does not exist
     */
    public static JsonObject getWebThing(String link, String security, String securityToken, String additionalLink) throws IOException{
        apiResponse = new String();
        String destUri = "";

        if(additionalLink != null && !additionalLink.isEmpty()){
            destUri = link + additionalLink + getSecurityLink(security, securityToken);;
        } else{
           destUri = link + getSecurityLink(security, securityToken);
        }
        
        URL destUrl = new URL(destUri);
        HttpURLConnection httpUrlConnection = setConnection(destUrl, "GET");

        // Set Header
        httpUrlConnection.setRequestProperty("Accept", "application/json");

        //sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(httpUrlConnection);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        // Convert string into ThingDTO
        Gson g = new Gson();
        JsonObject thing = g.fromJson(apiResponse, JsonObject.class);

        closeConnection(httpUrlConnection);
        return thing;
    }

    /**
     * 
     * @param security
     * @return
     */
    public static String getSecurityLink(String security, String securityToken){
        String securityLink ="";
        switch(security) {
            case "none":
                securityLink = "";
                break;
            case "bearer":
                securityLink = "?jwt=" + securityToken;
                break; 
        }
        return securityLink;
    }

    // Set Connection to Server
    private static HttpURLConnection setConnection(URL url, String requestMethod) throws IOException {
        // Get HTTP Connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set Configuration
        allowRequestMethods("PATCH");
        connection.setRequestMethod(requestMethod);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(true);

        return connection;

    }

    // Allow Other Request Methods e.g. PATCH
    private static void allowRequestMethods(String... methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = (String[]) methodsSet.toArray(new String[0]);

            methodsField.set(null/* static field */, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    // Disconnect from Server
    private static void closeConnection(HttpURLConnection connection) {
        // Disconnect + reset variables
        connection.disconnect();
        apiResponse = "";
        stringBuilder = new StringBuilder();
    }

    // Parser to read answer from Server
    public static String jsonParser(String jsonBody, String tag) {
        JsonObject parser = (JsonObject) new JsonParser().parse(jsonBody);
        String valueWithQuotationMarks = parser.get(tag).toString();
        int stringLength = valueWithQuotationMarks.length();
        String returnValue = valueWithQuotationMarks.substring(1, stringLength - 1);
        return returnValue;
    }


    // Parser to read answer from Server
    public static String jsonParser(String jsonBody, String[] tag) {
        JsonObject parser = new JsonParser().parse(jsonBody).getAsJsonObject();

        for(int i = 0; i < tag.length - 1; i++){
            parser = parser.get(tag[i]).getAsJsonObject();
        }
       
        String valueWithQuotationMarks = parser.get(tag[tag.length-1]).toString();
        int stringLength = valueWithQuotationMarks.length();
        String returnValue = valueWithQuotationMarks.substring(1, stringLength - 1);
        return returnValue;
    }

    // Read whole JSON answer from Server
    public static void readAnswerFromServer(HttpURLConnection connection) throws IOException {
        BufferedReader reader = null;
        String concatLine = null;

        // Get Output Reader and Writer
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        // Read Output Stream
        while ((concatLine = reader.readLine()) != null) {
            stringBuilder.append(concatLine);
        }

        reader = null;
        concatLine = null;
    }
}
