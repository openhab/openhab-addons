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
package org.openhab.binding.hdpowerview.internal;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
import org.openhab.binding.hdpowerview.internal.api.Color;
import org.openhab.binding.hdpowerview.internal.api.HubFirmware;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.SurveyData;
import org.openhab.binding.hdpowerview.internal.api.UserData;
import org.openhab.binding.hdpowerview.internal.api.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.api.responses.Repeaters;
import org.openhab.binding.hdpowerview.internal.api.responses.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubShadeTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Abstract class for JAX-RS targets for communicating with an HD PowerView hub.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class HDPowerViewWebTargets {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);

    /*
     * the hub returns a 423 error (resource locked) daily just after midnight;
     * which means it is temporarily undergoing maintenance; so we use "soft"
     * exception handling during the five minute maintenance period after a 423
     * error is received
     */
    private final Duration maintenancePeriod = Duration.ofMinutes(5);
    private Instant maintenanceScheduledEnd = Instant.MIN;

    /*
     * De-serializer target class types
     */
    private final Class<?> sceneClass;
    private final Class<?> shadeDataClass;
    private final Class<?> shadePositionClass;
    private final Class<?> scheduledEventClass;

    protected final Gson gson;
    protected final HttpClient httpClient;

    /*
     * Private helper class for de-serialization of Scene
     */
    private class SceneDeserializer implements JsonDeserializer<Scene> {
        @Override
        public @Nullable Scene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, sceneClass);
        }
    }

    /*
     * Private helper class for de-serialization of ShadeData
     */
    private class ShadeDataDeserializer implements JsonDeserializer<ShadeData> {
        @Override
        public @Nullable ShadeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, shadeDataClass);
        }
    }

    /*
     * Private helper class for de-serialization of ShadePosition
     */
    private class ShadePositionDeserializer implements JsonDeserializer<ShadePosition> {
        @Override
        public @Nullable ShadePosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, shadePositionClass);
        }
    }

    /*
     * Private helper class for de-serialization of ScheduledEvent
     */
    private class ScheduledEventDeserializer implements JsonDeserializer<ScheduledEvent> {
        @Override
        public @Nullable ScheduledEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, scheduledEventClass);
        }
    }

    /**
     * Protected helper class for passing http url query parameters
     */
    protected static class Query {
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

        @Override
        public String toString() {
            return String.format("?%s=%s", key, value);
        }
    }

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public HDPowerViewWebTargets(HttpClient httpClient, String ipAddress, Class<?> sceneClass, Class<?> shadeDataClass,
            Class<?> shadePositionClass, Class<?> scheduledEventClass) {
        this.httpClient = httpClient;
        this.sceneClass = sceneClass;
        this.shadeDataClass = shadeDataClass;
        this.shadePositionClass = shadePositionClass;
        this.scheduledEventClass = scheduledEventClass;
        this.gson = new GsonBuilder()
        // @formatter:off
                .registerTypeAdapter(Scene.class, new SceneDeserializer())
                .registerTypeAdapter(ShadeData.class, new ShadeDataDeserializer())
                .registerTypeAdapter(ShadePosition.class, new ShadePositionDeserializer())
                .registerTypeAdapter(ScheduledEvent.class, new ScheduledEventDeserializer())
        // @formatter:on
                .create();
    }

    /**
     * Get the gson de-serializer.
     *
     * @return the gson de-serializer.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Common protected method to invoke a call on the hub server to retrieve information or send a command
     *
     * @param method GET or PUT
     * @param url the host url to be called
     * @param query the http query parameter
     * @param jsonCommand the request command content (as a json string)
     * @return the response content (as a json string)
     * @throws HubMaintenanceException
     * @throws HubProcessingException
     */
    protected synchronized String invoke(HttpMethod method, String url, @Nullable Query query,
            @Nullable String jsonCommand) throws HubMaintenanceException, HubProcessingException {
        if (logger.isTraceEnabled()) {
            if (query != null) {
                logger.trace("API command {} {}{}", method, url, query);
            } else {
                logger.trace("API command {} {}", method, url);
            }
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HubProcessingException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        } catch (TimeoutException | ExecutionException e) {
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
            maintenanceScheduledEnd = Instant.now().plus(maintenancePeriod);
            logger.debug("Hub undergoing maintenance");
            throw new HubMaintenanceException("Hub undergoing maintenance");
        }
        if (statusCode != HttpStatus.OK_200) {
            logger.warn("Hub returned HTTP {} {}", statusCode, response.getReason());
            throw new HubProcessingException(String.format("HTTP %d error", statusCode));
        }
        String jsonResponse = response.getContentAsString();
        if (logger.isTraceEnabled()) {
            logger.trace("JSON response = {}", jsonResponse);
        }
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            logger.warn("Hub returned no content");
            throw new HubProcessingException("Missing response entity");
        }
        return jsonResponse;
    }

    /**
     * Fetches a JSON package that describes all shades in the hub, and wraps it in
     * a Shades class instance
     *
     * @return Shades class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract Shades getShades()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes all scenes in the hub, and wraps it in
     * a Scenes class instance
     *
     * @return Scenes class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract Scenes getScenes()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package with firmware information for the hub.
     *
     * @return FirmwareVersions class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract HubFirmware getFirmwareVersions()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package with user data information for the hub.
     *
     * @return {@link UserData} class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract UserData getUserData()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Instructs the hub to move a specific shade
     *
     * @param shadeId id of the shade to be moved
     * @param position instance of ShadePosition containing the new position
     * @return ShadeData class instance (with new position)
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData moveShade(int shadeId, ShadePosition position) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to stop movement of a specific shade
     *
     * @param shadeId id of the shade to be stopped
     * @return ShadeData class instance (new position cannot be relied upon)
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData stopShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to jog a specific shade
     *
     * @param shadeId id of the shade to be jogged
     * @return ShadeData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData jogShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to calibrate a specific shade
     *
     * @param shadeId id of the shade to be calibrated
     * @return ShadeData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData calibrateShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to execute a specific scene
     *
     * @param sceneId id of the scene to be executed
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract void activateScene(int sceneId) throws HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes all scene collections in the hub, and wraps it in
     * a SceneCollections class instance
     *
     * @return SceneCollections class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract SceneCollections getSceneCollections()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Instructs the hub to execute a specific scene collection
     *
     * @param sceneCollectionId id of the scene collection to be executed
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract void activateSceneCollection(int sceneCollectionId)
            throws HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes all scheduled events in the hub, and wraps it in
     * a ScheduledEvents class instance
     *
     * @return ScheduledEvents class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract ScheduledEvents getScheduledEvents()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Enables or disables a scheduled event in the hub.
     *
     * @param scheduledEventId id of the scheduled event to be enabled or disabled
     * @param enable true to enable scheduled event, false to disable
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract void enableScheduledEvent(int scheduledEventId, boolean enable)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes all repeaters in the hub, and wraps it in
     * a Repeaters class instance
     *
     * @return Repeaters class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract Repeaters getRepeaters()
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes a specific repeater in the hub, and wraps it
     * in a RepeaterData class instance
     *
     * @param repeaterId id of the repeater to be fetched
     * @return RepeaterData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract RepeaterData getRepeater(int repeaterId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Instructs the hub to identify a specific repeater by blinking
     *
     * @param repeaterId id of the repeater to be identified
     * @return RepeaterData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract RepeaterData identifyRepeater(int repeaterId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Enables or disables blinking for a repeater
     *
     * @param repeaterId id of the repeater for which to be enable or disable blinking
     * @param enable true to enable blinking, false to disable
     * @return RepeaterData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract RepeaterData enableRepeaterBlinking(int repeaterId, boolean enable)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Sets color and brightness for a repeater
     *
     * @param repeaterId id of the repeater for which to set color and brightness
     * @return RepeaterData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract RepeaterData setRepeaterColor(int repeaterId, Color color)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Fetches a JSON package that describes a specific shade in the hub, and wraps it
     * in a Shade class instance
     *
     * @param shadeId id of the shade to be fetched
     * @return ShadeData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData getShade(int shadeId) throws HubInvalidResponseException, HubProcessingException,
            HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to do a hard refresh (discovery on the hubs RF network) on
     * a specific shade's position; fetches a JSON package that describes that shade,
     * and wraps it in a Shade class instance
     *
     * @param shadeId id of the shade to be refreshed
     * @return ShadeData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData refreshShadePosition(int shadeId)
            throws JsonParseException, HubProcessingException, HubMaintenanceException, HubShadeTimeoutException;

    /**
     * Instructs the hub to do a hard refresh (discovery on the hubs RF network) on
     * a specific shade's survey data, which will also refresh signal strength;
     * fetches a JSON package that describes that survey, and wraps it in a Survey
     * class instance
     *
     * @param shadeId id of the shade to be surveyed
     * @return List of SurveyData class instances
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     */
    public abstract List<SurveyData> getShadeSurvey(int shadeId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException;

    /**
     * Instructs the hub to do a hard refresh (discovery on the hubs RF network) on
     * a specific shade's battery level; fetches a JSON package that describes that shade,
     * and wraps it in a Shade class instance
     *
     * @param shadeId id of the shade to be refreshed
     * @return ShadeData class instance
     * @throws HubInvalidResponseException if response is invalid
     * @throws HubProcessingException if there is any processing error
     * @throws HubMaintenanceException if the hub is down for maintenance
     * @throws HubShadeTimeoutException if the shade did not respond to a request
     */
    public abstract ShadeData refreshShadeBatteryLevel(int shadeId) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException;
}
