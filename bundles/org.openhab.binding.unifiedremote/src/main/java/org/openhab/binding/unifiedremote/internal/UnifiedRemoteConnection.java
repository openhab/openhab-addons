/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link UnifiedRemoteConnection} Handles Remote Server Communications
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteConnection {

    private static final int WEB_CLIENT_PORT = 9510;
    private static final int TIMEOUT_SEC = 10;
    private static final String CONNECTION_ID_HEADER = "UR-Connection-ID";
    private static final String MOUSE_REMOTE = "Relmtech.Basic Input";
    private static final String NAVIGATION_REMOTE = "Unified.Navigation";
    private static final String POWER_REMOTE = "Unified.Power";
    private static final String MEDIA_REMOTE = "Unified.Media";
    private static final String MONITOR_REMOTE = "Unified.Monitor";

    private Logger logger = LoggerFactory.getLogger(UnifiedRemoteConnection.class);
    private final String url;
    private HttpClient httpClient;
    private @Nullable String connectionID;
    private @Nullable String connectionGUID;

    public UnifiedRemoteConnection(HttpClient httpClient, String host) {
        this.httpClient = httpClient;
        url = "http://" + host + ":" + WEB_CLIENT_PORT + "/client/";
    }

    public void authenticate() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = null;
        connectionGUID = "web-" + UUID.randomUUID().toString();
        response = httpClient.newRequest(getPath("connect")).method(HttpMethod.GET)
                .timeout(TIMEOUT_SEC, TimeUnit.SECONDS).send();
        JsonObject responseBody = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
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
    }

    public ContentResponse mouseMove(String jsonIntArray)
            throws InterruptedException, ExecutionException, TimeoutException {
        JsonArray cordinates = JsonParser.parseString(jsonIntArray).getAsJsonArray();
        int x = cordinates.get(0).getAsInt();
        int y = cordinates.get(1).getAsInt();
        return this.execRemoteAction("Relmtech.Basic Input", "delta",
                wrapValues(new String[] { "0", Integer.toString(x), Integer.toString(y) }));
    }

    public ContentResponse sendKey(String key) throws InterruptedException, ExecutionException, TimeoutException {
        String remoteID = "";
        String actionName = "";
        String value = null;
        switch (key) {
            case "LEFT_CLICK":
                remoteID = MOUSE_REMOTE;
                actionName = "left";
                break;
            case "RIGHT_CLICK":
                remoteID = MOUSE_REMOTE;
                actionName = "right";
                break;
            case "LOCK":
                remoteID = POWER_REMOTE;
                actionName = "lock";
                break;
            case "UNLOCK":
                remoteID = POWER_REMOTE;
                actionName = "unlock";
                break;
            case "SLEEP":
                remoteID = POWER_REMOTE;
                actionName = "sleep";
                break;
            case "SHUTDOWN":
                remoteID = POWER_REMOTE;
                actionName = "shutdown";
                break;
            case "RESTART":
                remoteID = POWER_REMOTE;
                actionName = "restart";
                break;
            case "LOGOFF":
                remoteID = POWER_REMOTE;
                actionName = "logoff";
                break;
            case "PLAY/PAUSE":
            case "PLAY":
            case "PAUSE":
                remoteID = MEDIA_REMOTE;
                actionName = "play_pause";
                break;
            case "NEXT":
                remoteID = MEDIA_REMOTE;
                actionName = "next";
                break;
            case "PREVIOUS":
                remoteID = MEDIA_REMOTE;
                actionName = "previous";
                break;
            case "STOP":
                remoteID = MEDIA_REMOTE;
                actionName = "stop";
                break;
            case "VOLUME_MUTE":
                remoteID = MEDIA_REMOTE;
                actionName = "volume_mute";
                break;
            case "VOLUME_UP":
                remoteID = MEDIA_REMOTE;
                actionName = "volume_up";
                break;
            case "VOLUME_DOWN":
                remoteID = MEDIA_REMOTE;
                actionName = "volume_down";
                break;
            case "BRIGHTNESS_UP":
                remoteID = MONITOR_REMOTE;
                actionName = "brightness_up";
                break;
            case "BRIGHTNESS_DOWN":
                remoteID = MONITOR_REMOTE;
                actionName = "brightness_down";
                break;
            case "MONITOR_OFF":
                remoteID = MONITOR_REMOTE;
                actionName = "turn_off";
                break;
            case "MONITOR_ON":
                remoteID = MONITOR_REMOTE;
                actionName = "turn_on";
                break;
            case "ESCAPE":
            case "SPACE":
            case "BACK":
            case "LWIN":
            case "CONTROL":
            case "TAB":
            case "MENU":
            case "RETURN":
            case "UP":
            case "DOWN":
            case "LEFT":
            case "RIGHT":
                remoteID = NAVIGATION_REMOTE;
                actionName = "toggle";
                value = key;
                break;
        }
        JsonArray wrappedValues = null;
        if (value != null) {
            wrappedValues = wrapValues(new String[] { value });
        }
        return this.execRemoteAction(remoteID, actionName, wrappedValues);
    }

    public ContentResponse keepAlive() throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject payload = new JsonObject();
        payload.addProperty("KeepAlive", true);
        payload.addProperty("Source", connectionGUID);
        return request(payload);
    }

    private ContentResponse execRemoteAction(String remoteID, String name, @Nullable JsonElement values)
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

    private ContentResponse request(JsonObject content)
            throws InterruptedException, ExecutionException, TimeoutException {
        Request request = httpClient.newRequest(getPath("request")).method(HttpMethod.POST).timeout(TIMEOUT_SEC,
                TimeUnit.SECONDS);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        if (connectionID != null) {
            request.header(CONNECTION_ID_HEADER, connectionID);
        }
        String stringContent = content.toString();
        logger.debug("[Request Payload {} ]", stringContent);
        request.content(new StringContentProvider(stringContent, "utf-8"));
        return request.send();
    }

    private JsonArray wrapValues(String[] commandValues) {
        JsonArray values = new JsonArray();
        for (String value : commandValues) {
            JsonObject valueWrapper = new JsonObject();
            valueWrapper.addProperty("Value", value);
            values.add(valueWrapper);
        }
        return values;
    }

    private String getPath(String path) {
        return url + path;
    }
}
