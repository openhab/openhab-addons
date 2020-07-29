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
package org.openhab.binding.intesis.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.intesis.internal.IntesisConfiguration;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.AuthenticateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link IntesisHomeHttpApi} wraps the IntesisHome REST API and provides various low level function to access the
 * device api (not
 * cloud api).
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisHomeHttpApi {
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    private final static Logger logger = LoggerFactory.getLogger(IntesisHomeHttpApi.class);

    public IntesisHomeHttpApi(IntesisConfiguration config, HttpClient httpClient) {
    }

    public static @Nullable JsonElement getInfo(String deviceIp, HttpClient httpClient) {
        String response = "";
        String url = "http://" + deviceIp + "/api.cgi";
        try {
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider("{\"command\":\"getinfo\",\"data\":\"\"}"), "application/json");

            // Do request and get response
            ContentResponse contentResponse = request.send();

            response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("HTTP Response for getInfo {}: {}", contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                // toDo
            }
            if (response == null || response.isEmpty() || !response.startsWith("{") && !response.startsWith("[")) {

            }
            if (response != null && !response.isEmpty()) {
                JsonElement infoNode = getData(response).get("info");
                return infoNode;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String getSessionId(String deviceIp, String password, HttpClient httpClient) {
        logger.trace("getSessionId for {}", deviceIp);
        String response = "";
        String sessionId = "";
        String url = "http://" + deviceIp + "/api.cgi";
        try {
            // httpClient.start();
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(
                    "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\"" + password + "\"}}"),
                    "application/json");

            // Do request and get response
            ContentResponse contentResponse = request.send();

            response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("HTTP Response {}: {}", contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                // toDo
            }
            if (response == null || response.isEmpty() || !response.startsWith("{") && !response.startsWith("[")) {

            }
            if (response != null && !response.isEmpty()) {
                JsonElement idNode = getData(response).get("id");
                Gson gson = new Gson();
                AuthenticateData auth = gson.fromJson(idNode, AuthenticateData.class);
                sessionId = auth.sessionID;
                logger.trace("sessionID : {}", sessionId);

                return sessionId;

            }

        } catch (Exception e) {
        }
        return sessionId;
    }

    public static @Nullable JsonElement getRestrictedRequestAll(String deviceIp, String sessionId,
            HttpClient httpClient) {
        String response = "";
        String url = "http://" + deviceIp + "/api.cgi";
        try {
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider("{\"command\":\"getdatapointvalue\",\"data\":{\"sessionID\":\""
                    + sessionId + "\", \"uid\":\"all\"}}"), "application/json");

            // Do request and get response
            ContentResponse contentResponse = request.send();

            response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("HTTP Response for all uid's {}: {}", contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                // toDo
            }
            if (response == null || response.isEmpty() || !response.startsWith("{") && !response.startsWith("[")) {

            }
            if (response != null && !response.isEmpty()) {
                JsonElement dpvalNode = getData(response).get("dpval");
                return dpvalNode;
            }

        } catch (Exception e) {

        }
        return null;
    }

    public static @Nullable JsonElement getRestrictedRequestUID6(String deviceIp, String sessionId,
            HttpClient httpClient) {
        String response = "";
        String url = "http://" + deviceIp + "/api.cgi";
        try {
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider(
                    "{\"command\":\"getdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId + "\", \"uid\":6}}"),
                    "application/json");

            // Do request and get response
            ContentResponse contentResponse = request.send();

            response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("HTTP Response for uid 6 {}: {}", contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                // toDo
            }
            if (response == null || response.isEmpty() || !response.startsWith("{") && !response.startsWith("[")) {

            }
            if (response != null && !response.isEmpty()) {
                JsonElement dpvalNode = getData(response).get("dpval");
                return dpvalNode;
            }

        } catch (Exception e) {

        }
        return null;
    }

    public static void setRestricted(String deviceIp, String sessionId, HttpClient httpClient, int uid, int value) {
        String response = "";
        String url = "http://" + deviceIp + "/api.cgi";
        logger.trace("Sending value {} to uid {}", value, uid);
        try {
            Request request = httpClient.POST(url);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.content(new StringContentProvider("{\"command\":\"setdatapointvalue\",\"data\":{\"sessionID\":\""
                    + sessionId + "\", \"uid\":" + uid + ",\"value\":" + value + "}}"), "application/json");

            logger.trace("HTTP Content to send : {}", request.getContent());

            // Do request and get response
            ContentResponse contentResponse = request.send();

            response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("HTTP Response for setRestricted {}: {}", contentResponse.getStatus(), response);

            // validate response, API errors are reported as Json
            if (contentResponse.getStatus() != HttpStatus.OK_200) {
                // toDo
            }
            if (response == null || response.isEmpty() || !response.startsWith("{") && !response.startsWith("[")) {

            }
        } catch (Exception e) {
        }
    }

    public static JsonObject getData(String response) {
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(response);
        JsonObject details = rootNode.getAsJsonObject();
        // JsonElement successNode = details.get("success");
        JsonElement dataNode = details.get("data");
        JsonObject data = dataNode.getAsJsonObject();
        return data;
    }
}
