/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kostalinverter.internal.secondgeneration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private static final int REQUEST_TIMEOUT_MS = 5000;

    public static void executeConfigurationChanges(HttpClient httpClient, String url, String username, String password,
            String dxsId, String value)
            throws InterruptedException, ExecutionException, TimeoutException, NoSuchAlgorithmException {
        String urlLogin = url + "/api/login.json?";
        String salt = "";
        String sessionId = "";

        Logger logger = LoggerFactory.getLogger(SecondGenerationConfigurationHandler.class);

        String getAuthenticateResponse = httpClient.GET(urlLogin).getContentAsString();

        try {
            JsonObject getAuthenticateResponseJsonObject = (JsonObject) JsonParser
                    .parseString(transformJsonResponse(getAuthenticateResponse));

            sessionId = extractSessionId(getAuthenticateResponseJsonObject);

            JsonObject authenticateJsonObject = JsonParser.parseString(getAuthenticateResponse).getAsJsonObject();
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

            Request loginPostJsonResponse = httpClient.POST(urlLogin + "?sessionId=" + sessionId)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            loginPostJsonResponse.header(HttpHeader.CONTENT_TYPE, "application/json");
            loginPostJsonResponse.content(new StringContentProvider(loginPostJsonData));
            ContentResponse loginPostJsonDataContentResponse = loginPostJsonResponse.send();

            String loginPostResponse = new String(loginPostJsonDataContentResponse.getContent(),
                    StandardCharsets.UTF_8);

            JsonObject loginPostJsonObject = (JsonObject) JsonParser
                    .parseString(transformJsonResponse(loginPostResponse));

            sessionId = extractSessionId(loginPostJsonObject);

            // Part for sending data to Inverter
            String postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":" + value + "}]}";

            Request postJsonDataRequest = httpClient.POST(url + "/api/dxs.json?sessionId=" + sessionId)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            postJsonDataRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
            postJsonDataRequest.content(new StringContentProvider(postJsonData));
            postJsonDataRequest.send();
        } catch (JsonIOException getAuthenticateResponseException) {
            logger.debug("Could not read the response: {}", getAuthenticateResponseException.getMessage());
        }
    }

    static String transformJsonResponse(String jsonResponse) {
        // Method transformJsonResponse converts response,due to missing [] in ContentResponse
        // postJsonDataContentResponse.

        int sessionStartPosition = jsonResponse.indexOf("session");
        int statusStartPosition = jsonResponse.indexOf("status");

        StringBuilder transformStringBuilder = new StringBuilder();

        transformStringBuilder.append(jsonResponse);

        transformStringBuilder.insert(sessionStartPosition + 9, '[');
        int roleIdStartPosition = jsonResponse.indexOf("roleId");
        transformStringBuilder.insert(roleIdStartPosition + 11, ']');

        transformStringBuilder.insert(statusStartPosition + 10, '[');
        int codeStartPosition = jsonResponse.indexOf("code");
        transformStringBuilder.insert(codeStartPosition + 11, ']');

        return transformStringBuilder.toString();
    }

    // Method extractSessionId extracts sessionId from JsonObject
    static String extractSessionId(JsonObject extractJsonObjectSessionId) {
        Logger sessionIdLogger = LoggerFactory.getLogger(SecondGenerationConfigurationHandler.class);
        String extractSessionId = "";
        JsonArray extractJsonArraySessionId = extractJsonObjectSessionId.getAsJsonArray("session");

        int size = extractJsonArraySessionId.size();
        if (size > 0) {
            extractSessionId = extractJsonArraySessionId.get(size - 1).getAsJsonObject().get("sessionId").getAsString();
        }
        if ("0".equals(extractSessionId)) {
            sessionIdLogger.debug(" Login Post Json Reponse not OK! , inverter answered with sessionId like: {}",
                    extractSessionId);
        }
        return extractSessionId;
    }

    // Method extractCode extracts code from JsonObject
    static String extractCode(JsonObject extractJsonObjectCode) {
        Logger codeLogger = LoggerFactory.getLogger(SecondGenerationConfigurationHandler.class);
        String extractCode = "";
        JsonArray extractJsonArrayCode = extractJsonObjectCode.getAsJsonArray("status");

        int size = extractJsonArrayCode.size();
        if (size > 0) {
            extractCode = extractJsonArrayCode.get(size - 1).getAsJsonObject().get("code").getAsString();
        }
        if (!"0".equals(extractCode)) {
            codeLogger.debug(" Login Post Json Reponse not OK! , inverter answered with status code like: {}",
                    extractCode);
        }
        return extractCode;
    }
}
