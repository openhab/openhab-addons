/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.protocol;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link IpControl} class talks to Samsung IP Control, JSON-RPC over HTTPS on port 1516.
 * It is enabled on the TV under Settings, General, Network, Expert Settings, IP Remote.
 *
 * Observed on 2021 and 2022 Frame TV's:
 * <ul>
 * <li>TV on: powerControl is powerOn, artModeControl is artModeOff</li>
 * <li>Art mode: powerControl is powerOn, artModeControl is artModeOn</li>
 * <li>2022 in standby: powerControl is powerOff, but artModeControl stays artModeOn, so check power first</li>
 * <li>2021 in standby: the port refuses connections</li>
 * </ul>
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class IpControl {

    public static final int PORT = 1516;

    private static final int REQUEST_TIMEOUT_S = 3;
    // the user has to press Allow on the TV
    private static final int PAIRING_TIMEOUT_S = 30;

    private static final int ERROR_UNAUTHORIZED = -32010;
    // returned for a token the TV no longer knows
    private static final int ERROR_STALE_TOKEN = -32700;

    private final Logger logger = LoggerFactory.getLogger(IpControl.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final String host;
    private final String url;

    public IpControl(String host, HttpClient httpClient) {
        this.host = host;
        this.httpClient = httpClient;
        this.url = "https://" + host + ":" + PORT + "/";
    }

    /**
     * Request a new access token. The TV must be on and not in art mode, and the user has 30 seconds to press Allow
     * on the TV.
     *
     * @return the access token
     * @throws IpControlException if the TV did not return a token
     */
    public String createAccessToken() throws IpControlException {
        JsonObject result = request("createAccessToken", Map.of(), PAIRING_TIMEOUT_S);
        JsonElement token = result.get("AccessToken");
        if (token == null || !token.isJsonPrimitive() || token.getAsString().isBlank()) {
            throw new IpControlException("No AccessToken returned");
        }
        return token.getAsString();
    }

    /**
     * @return true if the TV is on, which includes art mode
     */
    public boolean isPowerOn(String token) throws IpControlException {
        return "powerOn".equals(getString(request("powerControl", Map.of("AccessToken", token)), "power"));
    }

    /**
     * Note: a 2022 TV in standby still reports art mode on, so check {@link #isPowerOn(String)} first.
     *
     * @return true if the TV is in art mode
     */
    public boolean isArtModeOn(String token) throws IpControlException {
        return "artModeOn".equals(getString(request("artModeControl", Map.of("AccessToken", token)), "artMode"));
    }

    /**
     * Wake the TV from standby. It resumes in the mode it was switched off from (TV or art mode).
     */
    public void powerOn(String token) throws IpControlException {
        request("powerControl", Map.of("AccessToken", token, "power", "powerOn"));
    }

    /**
     * Switch the TV to standby, from TV or art mode
     */
    public void powerOff(String token) throws IpControlException {
        request("powerControl", Map.of("AccessToken", token, "power", "powerOff"));
    }

    private JsonObject request(String method, Map<String, String> params) throws IpControlException {
        return request(method, params, REQUEST_TIMEOUT_S);
    }

    private JsonObject request(String method, Map<String, String> params, int timeout) throws IpControlException {
        JsonObject body = new JsonObject();
        body.addProperty("jsonrpc", "2.0");
        body.addProperty("id", 1);
        body.addProperty("method", method);
        if (!params.isEmpty()) {
            body.add("params", gson.toJsonTree(params));
        }
        String response;
        // the TV handles one connection at a time
        synchronized (this) {
            try {
                ContentResponse reply = httpClient.newRequest(url).method(HttpMethod.POST)
                        .header(HttpHeader.ACCEPT, "application/json").header(HttpHeader.CONNECTION, "close")
                        .content(new StringContentProvider(body.toString()), "application/json")
                        .timeout(timeout, TimeUnit.SECONDS).send();
                response = reply.getContentAsString();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IpControlException(method + " interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                String reason = cause instanceof ConnectException ? "connection refused" : String.valueOf(cause);
                throw new IpControlException(method + ": " + reason, e);
            } catch (TimeoutException e) {
                throw new IpControlException(method + ": no answer", e);
            }
        }
        logger.trace("{}: IP Control {} -> {}", host, method, response);
        return parseResult(method, response);
    }

    /**
     * Parse a JSON-RPC reply
     *
     * @return the result object, empty if the reply has no result object
     * @throws IpControlException if the reply is an error or is not valid JSON
     */
    static JsonObject parseResult(String method, String response) throws IpControlException {
        JsonObject reply;
        try {
            JsonElement element = JsonParser.parseString(response);
            if (!element.isJsonObject()) {
                throw new IpControlException(method + ": unexpected reply " + response);
            }
            reply = element.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IpControlException(method + ": invalid reply", e);
        }
        JsonElement error = reply.get("error");
        if (error == null && reply.has("code") && !reply.has("result")) {
            // some errors are returned at the top level
            error = reply;
        }
        if (error != null) {
            if (!error.isJsonObject()) {
                throw new IpControlException(method + ": " + error);
            }
            JsonObject errorObject = error.getAsJsonObject();
            JsonElement codeElement = errorObject.get("code");
            int code = codeElement != null && codeElement.isJsonPrimitive()
                    && codeElement.getAsJsonPrimitive().isNumber() ? codeElement.getAsInt() : 0;
            String message = getString(errorObject, "message");
            boolean tokenRejected = code == ERROR_UNAUTHORIZED || code == ERROR_STALE_TOKEN;
            throw new IpControlException(method + ": " + message + " (" + code + ")", tokenRejected);
        }
        JsonElement result = reply.get("result");
        return result != null && result.isJsonObject() ? result.getAsJsonObject() : new JsonObject();
    }

    private static String getString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }
}
