/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeMove;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeStop;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * JAX-RS targets for communicating with an HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 * @author Jacob Laursen - Add support for scene groups and automations
 */
@NonNullByDefault
public class HDPowerViewWebTargets {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);

    /*
     * the hub returns a 423 error (resource locked) daily just after midnight;
     * which means it is temporarily undergoing maintenance; so we use "soft"
     * exception handling during the five minute maintenance period after a 423
     * error is received
     */
    private final int maintenancePeriod = 300;
    private Instant maintenanceScheduledEnd = Instant.now().minusSeconds(2 * maintenancePeriod);

    private final String base;
    private final String shades;
    private final String sceneActivate;
    private final String scenes;
    private final String sceneCollectionActivate;
    private final String sceneCollections;
    private final String scheduledEvents;

    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    /**
     * private helper class for passing http url query parameters
     */
    private static class Query {
        private final String key;
        private final String value;

        private Query(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static Query of(String key, String value) {
            return new Query(key, value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public HDPowerViewWebTargets(HttpClient httpClient, String ipAddress) {
        base = "http://" + ipAddress + "/api/";
        shades = base + "shades/";
        sceneActivate = base + "scenes";
        scenes = base + "scenes/";

        // Hub v1 only supports "scenecollections". Hub v2 will redirect to "sceneCollections".
        sceneCollectionActivate = base + "scenecollections";
        sceneCollections = base + "scenecollections/";

        scheduledEvents = base + "scheduledevents";
        this.httpClient = httpClient;
    }

    /**
     * Fetches a JSON package that describes all shades in the hub, and wraps it in
     * a Shades class instance
     *
     * @return Shades class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable Shades getShades() throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        return gson.fromJson(json, Shades.class);
    }

    /**
     * Instructs the hub to move a specific shade
     *
     * @param shadeId id of the shade to be moved
     * @param position instance of ShadePosition containing the new position
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public void moveShade(int shadeId, ShadePosition position) throws HubProcessingException, HubMaintenanceException {
        String json = gson.toJson(new ShadeMove(shadeId, position));
        invoke(HttpMethod.PUT, shades + Integer.toString(shadeId), null, json);
    }

    /**
     * Fetches a JSON package that describes all scenes in the hub, and wraps it in
     * a Scenes class instance
     *
     * @return Scenes class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable Scenes getScenes() throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, scenes, null, null);
        return gson.fromJson(json, Scenes.class);
    }

    /**
     * Instructs the hub to execute a specific scene
     *
     * @param sceneId id of the scene to be executed
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public void activateScene(int sceneId) throws HubProcessingException, HubMaintenanceException {
        invoke(HttpMethod.GET, sceneActivate, Query.of("sceneId", Integer.toString(sceneId)), null);
    }

    /**
     * Fetches a JSON package that describes all scene collections in the hub, and wraps it in
     * a SceneCollections class instance
     *
     * @return SceneCollections class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable SceneCollections getSceneCollections()
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, sceneCollections, null, null);
        return gson.fromJson(json, SceneCollections.class);
    }

    /**
     * Instructs the hub to execute a specific scene collection
     *
     * @param sceneCollectionId id of the scene collection to be executed
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public void activateSceneCollection(int sceneCollectionId) throws HubProcessingException, HubMaintenanceException {
        invoke(HttpMethod.GET, sceneCollectionActivate,
                Query.of("sceneCollectionId", Integer.toString(sceneCollectionId)), null);
    }

    /**
     * Fetches a JSON package that describes all scheduled events in the hub, and wraps it in
     * a ScheduledEvents class instance
     *
     * @return ScheduledEvents class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable ScheduledEvents getScheduledEvents()
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, scheduledEvents, null, null);
        return gson.fromJson(json, ScheduledEvents.class);
    }

    /**
     * Enables or disables a scheduled event in the hub.
     * 
     * @param scheduledEventId id of the scheduled event to be enabled or disabled
     * @param enable true to enable scheduled event, false to disable
     * @throws JsonParseException if there is a JSON parsing error
     * @throws JsonSyntaxException if there is a JSON syntax error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public void enableScheduledEvent(int scheduledEventId, boolean enable)
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String uri = scheduledEvents + "/" + scheduledEventId;
        String json = invoke(HttpMethod.GET, uri, null, null);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonObject scheduledEventObject = jsonObject.get("scheduledEvent").getAsJsonObject();
        scheduledEventObject.addProperty("enabled", enable);
        invoke(HttpMethod.PUT, uri, null, jsonObject.toString());
    }

    /**
     * Invoke a call on the hub server to retrieve information or send a command
     *
     * @param method GET or PUT
     * @param url the host url to be called
     * @param query the http query parameter
     * @param jsonCommand the request command content (as a json string)
     * @return the response content (as a json string)
     * @throws HubMaintenanceException
     * @throws HubProcessingException
     */
    private synchronized String invoke(HttpMethod method, String url, @Nullable Query query,
            @Nullable String jsonCommand) throws HubMaintenanceException, HubProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace("API command {} {}", method, url);
            if (jsonCommand != null) {
                logger.trace("JSON command = {}", jsonCommand);
            }
        }
        Request request = httpClient.newRequest(url).method(method).header("Connection", "close").accept("*/*");
        if (query != null) {
            request.param(query.getKey(), query.getValue());
        }
        if (jsonCommand != null) {
            request.header(HttpHeader.CONTENT_TYPE, "application/json").content(new StringContentProvider(jsonCommand));
        }
        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            if (Instant.now().isBefore(maintenanceScheduledEnd)) {
                // throw "softer" exception during maintenance window
                logger.debug("Hub still undergoing maintenance");
                throw new HubMaintenanceException("Hub still undergoing maintenance");
            }
            throw new HubProcessingException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        }
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.LOCKED_423) {
            // set end of maintenance window, and throw a "softer" exception
            maintenanceScheduledEnd = Instant.now().plusSeconds(maintenancePeriod);
            logger.debug("Hub undergoing maintenance");
            throw new HubMaintenanceException("Hub undergoing maintenance");
        }
        if (statusCode != HttpStatus.OK_200) {
            logger.warn("Hub returned HTTP {} {}", statusCode, response.getReason());
            throw new HubProcessingException(String.format("HTTP %d error", statusCode));
        }
        String jsonResponse = response.getContentAsString();
        if ("".equals(jsonResponse)) {
            logger.warn("Hub returned no content");
            throw new HubProcessingException("Missing response entity");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("JSON response = {}", jsonResponse);
        }
        return jsonResponse;
    }

    /**
     * Fetches a JSON package that describes a specific shade in the hub, and wraps it
     * in a Shade class instance
     *
     * @param shadeId id of the shade to be fetched
     * @return Shade class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable Shade getShade(int shadeId)
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades + Integer.toString(shadeId), null, null);
        return gson.fromJson(json, Shade.class);
    }

    /**
     * Instructs the hub to do a hard refresh (discovery on the hubs RF network) on
     * a specific shade's position; fetches a JSON package that describes that shade,
     * and wraps it in a Shade class instance
     *
     * @param shadeId id of the shade to be refreshed
     * @return Shade class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable Shade refreshShadePosition(int shadeId)
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades + Integer.toString(shadeId),
                Query.of("refresh", Boolean.toString(true)), null);
        return gson.fromJson(json, Shade.class);
    }

    /**
     * Instructs the hub to do a hard refresh (discovery on the hubs RF network) on
     * a specific shade's battery level; fetches a JSON package that describes that shade,
     * and wraps it in a Shade class instance
     *
     * @param shadeId id of the shade to be refreshed
     * @return Shade class instance
     * @throws JsonParseException if there is a JSON parsing error
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public @Nullable Shade refreshShadeBatteryLevel(int shadeId)
            throws JsonParseException, HubProcessingException, HubMaintenanceException {
        String json = invoke(HttpMethod.GET, shades + Integer.toString(shadeId),
                Query.of("updateBatteryLevel", Boolean.toString(true)), null);
        return gson.fromJson(json, Shade.class);
    }

    /**
     * Tells the hub to stop movement of a specific shade
     *
     * @param shadeId id of the shade to be stopped
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public void stopShade(int shadeId) throws HubProcessingException, HubMaintenanceException {
        String json = gson.toJson(new ShadeStop(shadeId));
        invoke(HttpMethod.PUT, shades + Integer.toString(shadeId), null, json);
    }
}
