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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link SecondGenerationConfigurationHandler} is responsible for configuration changes,
 * regarded to second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */
@NonNullByDefault
public class SecondGenerationConfigurationHandler {

    public static void executeConfigurationChanges(HttpClient httpClient, String url, String username, String password,
            String dxsId, String value) throws Exception {
        String urlLogin = url + "/api/login.json?";
        String salt = "";
        String sessionId = "";

        Logger logger = LoggerFactory.getLogger(SecondGenerationConfigurationHandler.class);

        String getAuthenticateResponse = httpClient.GET(urlLogin).getContentAsString();
        try {
            JsonObject getAuthenticateResponseJsonObject = (JsonObject) new JsonParser()
                    .parse(transformJsonResponse(getAuthenticateResponse));

            sessionId = extractSessionId(getAuthenticateResponseJsonObject);

            JsonObject authenticateJsonObject = new JsonParser().parse(getAuthenticateResponse.toString())
                    .getAsJsonObject();
            salt = authenticateJsonObject.get("salt").getAsString();

            String saltedPassword = new StringBuilder(password).append(salt).toString();
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");

            byte[] mDigestedPassword = mDigest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder loginPostStringBuilder = new StringBuilder();
            for (int i = 0; i < mDigestedPassword.length; i++) {
                loginPostStringBuilder.append(Integer.toString((mDigestedPassword[i] & 0xff) + 0x100, 16).substring(1));
            }
            String saltedmDigestedPwd = Base64.getEncoder().encodeToString(mDigest.digest(saltedPassword.getBytes()));

            String loginPostJsonData = "{\"mode\":1,\"userId\":\"" + username + "\",\"pwh\":\"" + saltedmDigestedPwd
                    + "\"}";

            Request loginPostJsonResponse = httpClient.POST(urlLogin + "?sessionId=" + sessionId);
            loginPostJsonResponse.header(HttpHeader.CONTENT_TYPE, "application/json");
            loginPostJsonResponse.content(new StringContentProvider(loginPostJsonData));
            ContentResponse loginPostJsonDataContentResponse = loginPostJsonResponse.send();

            String loginPostResponse = new String(loginPostJsonDataContentResponse.getContent());

            JsonObject loginPostJsonObject = (JsonObject) new JsonParser()
                    .parse(transformJsonResponse(loginPostResponse));

            sessionId = extractSessionId(loginPostJsonObject);

            // Part to sending data to Inverter
            String postJsonData = "";

            if (dxsId.contentEquals("16777984")) {
                // Works with inverterName, name will be changed, due to "" around value
                postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":\"" + value + "\"}]}";
            } else {
                // Works not with inverterName, name will not be changed, due to "" around value, but the other
                // configuration options will be changed.
                postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":" + value + "}]}";
            }

            Request postJsonDataRequest = httpClient.POST(url + "/api/dxs.json?sessionId=" + sessionId);
            postJsonDataRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
            postJsonDataRequest.content(new StringContentProvider(postJsonData));

            ContentResponse postJsonDataContentResponse = postJsonDataRequest.send();
            String postResponse = new String(postJsonDataContentResponse.getContent());

            JsonObject postJsonObject = (JsonObject) new JsonParser().parse(transformJsonResponse(postResponse));
            sessionId = extractSessionId(postJsonObject);
        } catch (JsonIOException e) {
            logger.debug("Could not read the response: {}", e.getMessage());
        }
    }

    static String transformJsonResponse(String jsonResponse) {
        // Method transformJsonResponse converts response,due to missing [] in JSON getAuthenticateResponse.

        int sessionStartPosition = jsonResponse.indexOf("session");
        StringBuilder transformStringBuilder = new StringBuilder();

        transformStringBuilder.append(jsonResponse);

        transformStringBuilder.insert(sessionStartPosition + 9, '[');
        int codeStartPosition = jsonResponse.indexOf("roleId");
        transformStringBuilder.insert(codeStartPosition + 11, ']');

        String transformJsonObject = transformStringBuilder.toString();

        return transformJsonObject;
    }

    static String extractSessionId(JsonObject extractJsonObject) throws Exception {
        // Method extractSessionId extracts sessionId from JsonObject
        String extractSessionId = "";
        JsonArray extractJsonArray = extractJsonObject.getAsJsonArray("session");

        int size = extractJsonArray.size();
        if (size > 0) {
            extractSessionId = extractJsonArray.get(size - 1).getAsJsonObject().get("sessionId").getAsString();
        }
        return extractSessionId;
    }
}
