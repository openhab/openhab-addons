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

import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;

import org.openhab.binding.webthings.internal.dto.CompleteThingDTO;
import org.openhab.binding.webthings.internal.config.WebThingsConnectorConfiguration;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedThingDTO;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
     * get all openHAB Things via REST API
     * @return ThingDTO list
     * @throws IOException 
     */
    public static List<ThingDTO> getAllOpenhabThings() throws IOException, JsonSyntaxException{
        //List<Thing> allThings = new ArrayList<Thing>();

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/things";
        }else{
            destUri = "http://" + openhabIp + "/rest/things";
        }
        
        URL destUrl = new URL(destUri);
        HttpURLConnection httpUrlConnection = setConnection(destUrl, "GET");

        // Set Header
        httpUrlConnection.setRequestProperty("Accept", "application/json");

        //sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(httpUrlConnection);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        // Convert string into ThingDTOs list
        Gson g = new Gson();
        List<ThingDTO> allThings = g.fromJson(apiResponse, new TypeToken<List<ThingDTO>>(){}.getType());

        closeConnection(httpUrlConnection);
        return allThings;
    }

    /**
     * get all openHAB Things via REST API
     * @return CompleteThingDTO list
     * @throws IOException
     */
    public static List<CompleteThingDTO> getAllCompleteOpenhabThings() throws IOException, JsonSyntaxException{
        //List<Thing> allThings = new ArrayList<Thing>();

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/things";
        }else{
            destUri = "http://" + openhabIp + "/rest/things";
        }
        
        URL destUrl = new URL(destUri);
        HttpURLConnection httpUrlConnection = setConnection(destUrl, "GET");

        // Set Header
        httpUrlConnection.setRequestProperty("Accept", "application/json");

        //sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(httpUrlConnection);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        // Convert string into ThingDTOs list
        Gson g = new Gson();
        List<CompleteThingDTO> allThings = g.fromJson(apiResponse, new TypeToken<List<CompleteThingDTO>>(){}.getType());

        closeConnection(httpUrlConnection);
        return allThings;
    }

    /**
     * get single openHAB Thing via REST API
     * @param thingUID Thing to get
     * @return ThingDTO
     * @throws IOException 
     * @throws FileNotFoundException Thing does not exist
     */
    public static ThingDTO getOpenhabThing(String thingUID) throws IOException, FileNotFoundException{
        //List<Thing> allThings = new ArrayList<Thing>();
        if(thingUID.contains("_")){
            thingUID = thingUID.replace("_", "%3A");
        }else if(thingUID.contains(":")){
            thingUID = thingUID.replace(":", "%3A");
        }

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/things/" + thingUID.replace(":", "%3A");
        }else{
            destUri = "http://" + openhabIp + "/rest/things/"+ thingUID.replace(":", "%3A");
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
        ThingDTO thingDTO = g.fromJson(apiResponse, ThingDTO.class);

        closeConnection(httpUrlConnection);
        return thingDTO;
    }

    /**
     * get single openHAB Thing via REST API
     * @param thingUID Thing to get
     * @return EnrichedThingDTO
     * @throws IOException
     * @throws FileNotFoundException Thing does not exist
     */
    public static EnrichedThingDTO getEnrichedOpenhabThing(String thingUID) throws IOException, FileNotFoundException{
        //List<Thing> allThings = new ArrayList<Thing>();
        if(thingUID.contains("_")){
            thingUID = thingUID.replace("_", "%3A");
        }else if(thingUID.contains(":")){
            thingUID = thingUID.replace(":", "%3A");
        }

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/things/" + thingUID.replace(":", "%3A");
        }else{
            destUri = "http://" + openhabIp + "/rest/things/"+ thingUID.replace(":", "%3A");
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
        EnrichedThingDTO thingDTO = g.fromJson(apiResponse, EnrichedThingDTO.class);

        closeConnection(httpUrlConnection);
        return thingDTO;
    }

    /**
     * Get single openHAB Item via REST API
     * @param itemUID Item to get
     * @return ItemDTO
     * @throws IOException
     * @throws FileNotFoundException Item does not exist
     */
    public static ItemDTO getOpenhabItem(String itemUID) throws IOException, FileNotFoundException{
        //List<Thing> allThings = new ArrayList<Thing>();
        if(itemUID.contains("_")){
            itemUID = itemUID.replace("_", "_");
        }else if(itemUID.contains(":")){
            itemUID = itemUID.replace(":", "_");
        }

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/items/" + itemUID.replace(":", "%3A");
        }else{
            destUri = "http://" + openhabIp + "/rest/items/"+ itemUID.replace(":", "%3A");
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
        ItemDTO itemDTO = g.fromJson(apiResponse, ItemDTO.class);

        closeConnection(httpUrlConnection);
        return itemDTO;
    }

    /**
     * Get all openHAB Item via REST API
     * @return List of ItemDTOs
     * @throws IOException
     */
    public static List<ItemDTO> getAllOpenhabItems() throws IOException, JsonSyntaxException{
        //List<Thing> allThings = new ArrayList<Thing>();

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/items";
        }else{
            destUri = "http://" + openhabIp + "/rest/items";
        }
        
        URL destUrl = new URL(destUri);
        HttpURLConnection httpUrlConnection = setConnection(destUrl, "GET");

        // Set Header
        httpUrlConnection.setRequestProperty("Accept", "application/json");

        //sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(httpUrlConnection);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        // Convert string into ThingDTOs list
        Gson g = new Gson();
        List<ItemDTO> allThings = g.fromJson(apiResponse, new TypeToken<List<ItemDTO>>(){}.getType());

        closeConnection(httpUrlConnection);
        return allThings;
    }

    /**
     * Get single openHAB Item via REST API
     * @param itemUID Item to get
     * @return EnrichedItemDTO
     * @throws IOException
     * @throws FileNotFoundException Item does not exist
     */
    public static EnrichedItemDTO getEnrichedOpenhabItem(String itemUID) throws IOException, FileNotFoundException{
        //List<Thing> allThings = new ArrayList<Thing>();
        if(itemUID.contains("_")){
            itemUID = itemUID.replace("_", "_");
        }else if(itemUID.contains(":")){
            itemUID = itemUID.replace(":", "_");
        }

        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/items/" + itemUID.replace(":", "%3A");
        }else{
            destUri = "http://" + openhabIp + "/rest/items/"+ itemUID.replace(":", "%3A");
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
        EnrichedItemDTO itemDTO = g.fromJson(apiResponse, EnrichedItemDTO.class);

        closeConnection(httpUrlConnection);
        return itemDTO;
    }

    /**
     * Update openHAB item
     * @param value Request
     * @param item Item to be updated
     * @throws IOException
     */
    public static void updateOpenhabItem(String value, String item) throws IOException, FileNotFoundException{
        String destUri;
        if(openhabIp.substring(openhabIp.length()-1).equals("\\")){
            destUri = "http://" + openhabIp + "rest/items/" + item;
        }else{
            destUri = "http://" + openhabIp + "/rest/items/"+ item;
        }
        
        URL destUrl = new URL(destUri);
        HttpURLConnection connectionCreateThing = setConnection(destUrl, "POST");

        // Set Header
        connectionCreateThing.setRequestProperty("Content-Type", "text/plain");
        connectionCreateThing.setRequestProperty("Accept", "application/json");

        sendBodyToServer(value, connectionCreateThing);
        readAnswerFromServer(connectionCreateThing);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();
        
        closeConnection(connectionCreateThing);
    }

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
     * Update a WebThing Property via Rest API %%% Work in Progress %%%
     * @param body
     * @param config
     * @throws IOException
     */
    public static void updateWebThing(JsonObject body, WebThingsConnectorConfiguration config) throws IOException{  
        URL urlCreateThing = new URL(serverUrl +"/things/" + config.id + "/properties/on");
        HttpURLConnection connectionCreateThing = setConnection(urlCreateThing, "PUT");

        // Set Header
        connectionCreateThing.setRequestProperty("Content-Type", "application/json");
        connectionCreateThing.setRequestProperty("Accept", "application/json");
        connectionCreateThing.setRequestProperty("Authorization", "Bearer " + token);

        sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(connectionCreateThing);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();

        closeConnection(connectionCreateThing);
    }

    /**
     * Update a WebThing Property via Rest API %%% Work in Progress %%%
     * @param body
     * @param config
     * @throws IOException
     */
    public static void updateWebThing(String body, WebThingsConnectorConfiguration config) throws IOException{     
        URL urlCreateThing = new URL("https://" + serverUrl +"/things/" + config.id + "/properties/on");
        HttpURLConnection connectionCreateThing = setConnection(urlCreateThing, "PUT");

        // Set Header
        connectionCreateThing.setRequestProperty("Content-Type", "application/json");
        connectionCreateThing.setRequestProperty("Accept", "application/json");
        connectionCreateThing.setRequestProperty("Authorization", "Bearer " + token);

        sendBodyToServer(body, connectionCreateThing);
        readAnswerFromServer(connectionCreateThing);

        // Get Thing Id by JSON Body
        apiResponse = stringBuilder.toString();

        closeConnection(connectionCreateThing);
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

    // Send JSON Body to Server
    private static void sendBodyToServer(String jsonBody, HttpURLConnection connection) throws IOException {
        // Send JSON Body to Server
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.write(jsonBody.getBytes(Charset.forName("UTF-8")));
        os.close();
        connection.getResponseMessage();
    }

    // Send JSON Body to Server
    private static void sendBodyToServer(JsonObject jsonBody, HttpURLConnection connection) throws IOException {
        // Send JSON Body to Server
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        //os.writeBytes(URLEncoder.encode(jsonBody.toString(),"UTF-8"));
        os.write(jsonBody.toString().getBytes("UTF-8"));
        os.close();
        connection.getResponseMessage();
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
