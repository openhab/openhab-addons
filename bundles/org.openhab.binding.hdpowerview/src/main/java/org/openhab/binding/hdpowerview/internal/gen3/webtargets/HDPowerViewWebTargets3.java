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
package org.openhab.binding.hdpowerview.internal.gen3.webtargets;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets.Query;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeMotion;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.gen3.dto.Info3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.Scene3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.SceneEvent3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.ScheduledEvent3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.Shade3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.ShadeEvent3;
import org.openhab.binding.hdpowerview.internal.gen3.dto.ShadePosition3;
import org.openhab.binding.hdpowerview.internal.gen3.handler.HDPowerViewHubHandler3;
import org.openhab.core.thing.Thing;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * JAX-RS targets for communicating with an HD PowerView hub Generation 3.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewWebTargets3 implements Closeable {

    private static final String IDS = "ids";

    // @formatter:off
    public static final Type LIST_SHADES = new TypeToken<ArrayList<Shade3>>() {}.getType();
    public static final Type LIST_SCENES = new TypeToken<ArrayList<Scene3>>() {}.getType();
    public static final Type LIST_EVENTS =new TypeToken<ArrayList<ScheduledEvent3>>() {}.getType();
    // @formatter:on

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets3.class);
    private final String shades;
    private final String scenes;
    private final String sceneActivate;
    private final String shadeMotion;
    private final String shadeStop;
    private final String shadePositions;
    private final String info;
    private final String automations;

    private final String register;
    private final String shadeEvents;
    private final String sceneEvents;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final HDPowerViewHubHandler3 hubHandler;

    private boolean isRegistered;

    private @Nullable SseEventSource shadeEventSource;
    private @Nullable SseEventSource sceneEventSource;

    /**
     * Simple DTO for registering the binding with the hub.
     *
     * @author Andrew Fiddian-Green - Initial contribution
     */
    @SuppressWarnings("unused")
    private static class GatewayRegistration {
        public String todo = "org.openhab.binding.hdpowerview"; // TODO
    }

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public HDPowerViewWebTargets3(HDPowerViewHubHandler3 hubHandler, HttpClient httpClient, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory, String ipAddress) {
        String base = "http://" + ipAddress + "/";
        String home = base + "home/";

        shades = home + "shades";
        scenes = home + "scenes";
        sceneActivate = home + "scenes/%d/activate";
        shadeMotion = home + "shades/%d/motion";
        shadeStop = home + "shades/stop";
        shadePositions = home + "shades/positions";
        automations = home + "automations";
        shadeEvents = home + "shades/events";
        sceneEvents = home + "scenes/events";

        info = base + "gateway/info";
        register = "TBD"; // TODO

        this.httpClient = httpClient;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.hubHandler = hubHandler;
    }

    /**
     * Issue a command a activate a scene.
     *
     * @param sceneId the scene to be activated.
     * @throws HubProcessingException if any error occurs.
     */
    public void activateScene(int sceneId) throws HubProcessingException {
        invoke(HttpMethod.PUT, String.format(sceneActivate, sceneId), null, null);
    }

    /**
     * Issue a calibrate command to a shade.
     *
     * @param shadeId the shade to be calibrated.
     * @throws HubProcessingException if any error occurs.
     */
    public void calibrateShade(int shadeId) throws HubProcessingException {
        String json = gson.toJson(new ShadeMotion(ShadeMotion.Type.CALIBRATE));
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, json);
    }

    @Override
    public void close() throws IOException {
        SseEventSource source;
        source = this.shadeEventSource;
        if (source != null) {
            source.close();
            this.shadeEventSource = null;
        }
        source = this.sceneEventSource;
        if (source != null) {
            source.close();
            this.sceneEventSource = null;
        }
    }

    /**
     * Register the binding with the hub (if not already registered).
     *
     * @throws HubProcessingException if any error occurs.
     */
    private void gatewayRegister() throws HubProcessingException {
        if (!isRegistered) {
            String json = gson.toJson(new GatewayRegistration());
            invoke(HttpMethod.PUT, register, null, json);
            isRegistered = true;
        }
    }

    /**
     * Get hub properties.
     *
     * @return a map containing the hub properties.
     * @throws HubProcessingException if any error occurs.
     */
    public Map<String, String> getInformation() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, info, null, null);
        try {
            Info3 result = gson.fromJson(json, Info3.class);
            if (result == null) {
                throw new HubProcessingException("getInformation(): missing response");
            }
            return Map.of( //
                    Thing.PROPERTY_FIRMWARE_VERSION, result.getFwVersion(), //
                    Thing.PROPERTY_SERIAL_NUMBER, result.getSerialNumber());
        } catch (JsonParseException e) {
            throw new HubProcessingException("getFirmwareVersions(): JsonParseException");
        }
    }

    /**
     * Get the list of scenes.
     *
     * @return the list of scenes.
     * @throws HubProcessingException if any error occurs.
     */
    public List<Scene3> getScenes() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, scenes, null, null);
        try {
            List<Scene3> result = gson.fromJson(json, LIST_SCENES);
            if (result == null) {
                throw new HubProcessingException("getScenes() missing response");
            }
            return result;
        } catch (JsonParseException e) {
            throw new HubProcessingException("getScenes() JsonParseException");
        }
    }

    /**
     * Get the list of scheduled events.
     *
     * @return the list of scheduled events.
     * @throws HubProcessingException if any error occurs.
     */
    public List<ScheduledEvents> getScheduledEvents() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, automations, null, null);
        try {
            List<ScheduledEvents> result = gson.fromJson(json, LIST_EVENTS);
            if (result == null) {
                throw new HubProcessingException("getScheduledEvents() missing response");
            }
            return result;
        } catch (JsonParseException e) {
            throw new HubProcessingException("getScheduledEvents() JsonParseException");
        }
    }

    /**
     * Get the data for a single shade.
     *
     * @param shadeId the id of the shade to get.
     * @return the shade.
     * @throws HubProcessingException if any error occurs.
     */
    public Shade3 getShade(int shadeId) throws HubProcessingException {
        String json = invoke(HttpMethod.GET, shades + Integer.toString(shadeId), null, null);
        try {
            Shade3 result = gson.fromJson(json, Shade3.class);
            if (result == null) {
                throw new HubProcessingException("getShade() missing response");
            }
            return result;
        } catch (JsonParseException e) {
            throw new HubProcessingException("getShade() JsonParseException");
        }
    }

    /**
     * Get the list of shades.
     *
     * @return the list of shades.
     * @throws HubProcessingException if any error occurs.
     */
    public List<Shade3> getShades() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        try {
            List<Shade3> result = gson.fromJson(json, LIST_SHADES);
            if (result == null) {
                throw new HubProcessingException("getShades() missing response");
            }
            return result;
        } catch (JsonParseException e) {
            throw new HubProcessingException("getShades() JsonParseException");
        }
    }

    /**
     * Invoke a call on the hub server to retrieve information or send a command.
     *
     * @param method GET or PUT.
     * @param url the host URL to be called.
     * @param query the HTTP query parameter.
     * @param jsonCommand the request command content (as a JSON string).
     * @return the response content (as a JSON string).
     * @throws HubProcessingException if something goes wrong.
     */
    protected synchronized String invoke(HttpMethod method, String url, @Nullable Query query,
            @Nullable String jsonCommand) throws HubProcessingException {
        if (logger.isTraceEnabled()) {
            if (query != null) {
                logger.trace("invoke() method:{}, url:{}, query:{}", method, url, query);
            } else {
                logger.trace("invoke() method:{}, url:{}", method, url);
            }
            if (jsonCommand != null) {
                logger.trace("invoke() request JSON:{}", jsonCommand);
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
            throw new HubProcessingException(String.format("%s: \"%s\"", e.getClass().getName(), e.getMessage()));
        }
        int statusCode = response.getStatus();
        if (statusCode != HttpStatus.OK_200) {
            logger.warn("invoke() HTTP status:{}, reason:{}", statusCode, response.getReason());
            throw new HubProcessingException(String.format("HTTP %d error", statusCode));
        }
        String jsonResponse = response.getContentAsString();
        if (logger.isTraceEnabled()) {
            logger.trace("invoke() response JSON:{}", jsonResponse);
        }
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            logger.warn("invoke() hub returned no content");
            throw new HubProcessingException("Missing response entity");
        }
        return jsonResponse;
    }

    /**
     * Issue a jog command to a shade.
     *
     * @param shadeId the shade to be jogged.
     * @throws HubProcessingException if any error occurs.
     */
    public void jogShade(int shadeId) throws HubProcessingException {
        String json = gson.toJson(new ShadeMotion(ShadeMotion.Type.JOG));
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, json);
    }

    /**
     * Issue a ccommand to move a shade.
     *
     * @param shadeId the shade to be moved.
     * @param position the new position.
     * @throws HubProcessingException if any error occurs.
     */
    public void moveShade(int shadeId, ShadePosition3 position) throws HubProcessingException {
        invoke(HttpMethod.PUT, shadePositions, Query.of(IDS, Integer.valueOf(shadeId).toString()),
                gson.toJson(position));
    }

    /**
     * Handle inbound SSE events for a scene.
     *
     * @param sseEvent the inbound event.
     */
    private void onSceneEvent(InboundSseEvent sseEvent) {
        String json = sseEvent.readData();
        logger.trace("onSceneEvent() json:{}", json);
        SceneEvent3 sceneEvent = gson.fromJson(json, SceneEvent3.class);
        if (sceneEvent != null) {
            Scene3 scene = sceneEvent.getScene();
            hubHandler.onSceneEvent(scene);
        }
    }

    /**
     * Handle inbound SSE events for a shade.
     *
     * @param sseEvent the inbound event.
     */
    private void onShadeEvent(InboundSseEvent sseEvent) {
        String json = sseEvent.readData();
        logger.trace("onShadeEvent() json:{}", json);
        ShadeEvent3 shadeEvent = gson.fromJson(json, ShadeEvent3.class);
        if (shadeEvent != null) {
            ShadePosition3 positions = shadeEvent.getCurrentPositions();
            hubHandler
                    .onShadeEvent(new Shade3().setId(shadeEvent.getId()).setShadePosition(positions).setPartialState());
        }
    }

    /**
     * Open the SSE subscriptions.
     *
     * @return true if registered for SSE events.
     * @throws HubProcessingException if any error occurs.
     */
    public void openSSE() throws HubProcessingException {
        SseEventSource shadeEventSource = this.shadeEventSource;
        SseEventSource sceneEventSource = this.sceneEventSource;

        if (shadeEventSource == null || !shadeEventSource.isOpen() || sceneEventSource == null
                || !sceneEventSource.isOpen()) {

            try {
                close();
            } catch (IOException e) {
            }

            // register ourself with the gateway (if necessary)
            gatewayRegister();

            SSLContext context = httpClient.getSslContextFactory().getSslContext();
            WebTarget target;

            // open SSE channel for shades
            target = clientBuilder.sslContext(context).build().target(shadeEvents);
            shadeEventSource = eventSourceFactory.newSource(target);
            shadeEventSource.register((event) -> onShadeEvent(event));
            shadeEventSource.open();
            this.shadeEventSource = shadeEventSource;

            // open SSE channel for scenes
            target = clientBuilder.sslContext(context).build().target(sceneEvents);
            sceneEventSource = eventSourceFactory.newSource(target);
            sceneEventSource.register((event) -> onSceneEvent(event));
            sceneEventSource.open();
            this.sceneEventSource = sceneEventSource;
        }
    }

    /**
     * Issue a stop command to a shade.
     *
     * @param shadeId the shade to be stopped.
     * @throws HubProcessingException if any error occurs.
     */
    public void stopShade(int shadeId) throws HubProcessingException {
        invoke(HttpMethod.PUT, shadeStop, Query.of(IDS, Integer.valueOf(shadeId).toString()), null);
    }
}
