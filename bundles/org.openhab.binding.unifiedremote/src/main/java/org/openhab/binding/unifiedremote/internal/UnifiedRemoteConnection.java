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
package org.openhab.binding.unifiedremote.internal;

import java.io.Closeable;
import java.net.NoRouteToHostException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link UnifiedRemoteConnection} Handles Remote Server Communications
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteConnection implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(UnifiedRemoteConnection.class);
    private final String url;
    private final HttpClient http = new HttpClient();
    private final JsonParser jsonParser = new JsonParser();
    private final int WEB_CLIENT_PORT = 9510;
    private final String CONNECTION_ID_HEADER = "UR-Connection-ID";
    private @Nullable String connectionID;
    private @Nullable String connectionGUID;

    public UnifiedRemoteConnection(String host) {
        url = "http://" + host + ":" + WEB_CLIENT_PORT + "/client/";
    }

    public boolean authenticate() {
        logger.debug("Initializing connection to {}", url);
        ContentResponse response = null;
        connectionGUID = "web-" + UUID.randomUUID().toString();
        try {
            if (!http.isStarted())
                http.start();
            response = http.GET(getPath("connect"));
            JsonObject responseBody = jsonParser.parse(response.getContentAsString()).getAsJsonObject();
            connectionID = responseBody.get("id").getAsString();

            String password = UUID.randomUUID().toString();
            JsonObject authPayload = new JsonObject();
            authPayload.addProperty("Action", 0);
            authPayload.addProperty("Request", 0);
            authPayload.addProperty("Version", 10);
            authPayload.addProperty("Password", password);
            authPayload.addProperty("Platform", "web");
            authPayload.addProperty("Source", connectionGUID);
            request(authPayload);

            JsonObject capabilitiesPayload = new JsonObject();
            JsonObject capabilitiesInnerPayload = new JsonObject();
            capabilitiesInnerPayload.addProperty("Actions", true);
            capabilitiesInnerPayload.addProperty("Sync", true);
            capabilitiesInnerPayload.addProperty("Grid", true);
            capabilitiesInnerPayload.addProperty("Fast", false);
            capabilitiesInnerPayload.addProperty("Loading", true);
            capabilitiesInnerPayload.addProperty("Encryption2", true);
            capabilitiesPayload.add("Capabilities", capabilitiesInnerPayload);
            capabilitiesPayload.addProperty("Action", 1);
            capabilitiesPayload.addProperty("Request", 1);
            capabilitiesPayload.addProperty("Source", connectionGUID);
            request(capabilitiesPayload);
            return true;
        } catch (NoRouteToHostException e) {
            logger.debug("Host not found, server should be offline: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error authenticating: {}", e.getMessage());
            return false;
        }
    }

    public ContentResponse execRemoteAction(String remoteID, String name, @Nullable JsonElement values)
            throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject payload = new JsonObject();
        JsonObject runInnerPayload = new JsonObject();
        JsonObject extrasInnerPayload = new JsonObject();
        if (values != null) {
            extrasInnerPayload.add("Values", values);
            runInnerPayload.add("Extras", extrasInnerPayload);
        }
        runInnerPayload.addProperty("Name", name);
        payload.addProperty("ID", remoteID);
        payload.addProperty("Action", 7);
        payload.addProperty("Request", 7);
        payload.add("Run", runInnerPayload);
        payload.addProperty("Source", connectionGUID);
        return request(payload);
    }

    public ContentResponse keepAlive() throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject payload = new JsonObject();
        payload.addProperty("KeepAlive", true);
        payload.addProperty("Source", connectionGUID);
        return request(payload);
    }

    private ContentResponse request(JsonObject content)
            throws InterruptedException, ExecutionException, TimeoutException {
        Request request = http.POST(getPath("request"));
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        if (connectionID != null)
            request.header(CONNECTION_ID_HEADER, connectionID);
        String stringContent = content.toString();
        logger.debug("[Request Payload {} ]", stringContent);
        request.content(new StringContentProvider(stringContent, "utf-8"));
        return request.send();
    }

    private String getPath(String path) {
        return url + path;
    }

    @Override
    public void close() {
        if (http.isStarted()) {
            try {
                http.stop();
            } catch (Exception e) {
            }
        }
    }
}
