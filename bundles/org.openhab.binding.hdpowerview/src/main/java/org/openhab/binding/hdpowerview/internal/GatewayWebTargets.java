/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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
import org.openhab.binding.hdpowerview.internal.dto.gen3.Info;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Scene;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadeEvent;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadePosition;
import org.openhab.binding.hdpowerview.internal.dto.requests.ShadeMotion;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.handler.GatewayBridgeHandler;
import org.openhab.core.thing.Thing;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * JAX-RS targets for communicating with an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GatewayWebTargets implements Closeable, HostnameVerifier {

    private static final String IDS = "ids";
    private static final int SLEEP_SECONDS = 360;
    private static final Set<Integer> HTTP_OK_CODES = Set.of(HttpStatus.OK_200, HttpStatus.NO_CONTENT_204);
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(GatewayWebTargets.class);
    private final Gson jsonParser = new Gson();

    private final String shades;
    private final String scenes;
    private final String sceneActivate;
    private final String shadeMotion;
    private final String shadePositions;
    private final String shadeSingle;
    private final String shadeStop;
    private final String info;
    private final String register;
    private final String shadeEvents;
    private final HttpClient httpClient;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final GatewayBridgeHandler hubHandler;
    private final String ipAddress;

    private boolean registered;
    private boolean closing;

    private @Nullable SseEventSource shadeEventSource;
    private @Nullable ScheduledFuture<?> sseQuietCheck;

    /**
     * Initialize the web targets
     *
     * @param httpClient the HTTP client (the binding)
     * @param ipAddress the IP address of the server (the hub)
     */
    public GatewayWebTargets(GatewayBridgeHandler hubHandler, HttpClient httpClient, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory, String ipAddress) {
        this.ipAddress = ipAddress;
        this.httpClient = httpClient;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.hubHandler = hubHandler;

        String base = "http://" + ipAddress + "/";
        String home = base + "home/";
        shades = home + "shades";
        scenes = home + "scenes";
        sceneActivate = home + "scenes/%d/activate";
        shadeMotion = home + "shades/%d/motion";
        shadePositions = home + "shades/positions";
        shadeSingle = home + "shades/%d";
        shadeStop = home + "shades/stop";
        shadeEvents = home + "shades/events?sse=true";
        info = base + "gateway/info";

        /*
         * Hunter Douglas keeps a statistical count of systems (e.g. openHAB, Home Assistant, Amazon etc.) that are
         * using their Generation 3 REST API to connect to their gateways. So we are asked to register with the gateway
         * on startup. => So do not change the 'openhab.org' tag below !!
         */
        register = home + "integration/openhab.org";
    }

    /**
     * Issue a command to activate a scene.
     *
     * @param sceneId the scene to be activated.
     * @throws HubProcessingException if any error occurs.
     */
    public void activateScene(int sceneId) throws HubProcessingException {
        invoke(HttpMethod.PUT, String.format(sceneActivate, sceneId), null, null);
    }

    @Override
    public void close() throws IOException {
        closing = true;
        sseClose();
    }

    /**
     * Register the binding with the hub (if not already registered).
     *
     * @throws HubProcessingException if any error occurs.
     */
    public boolean gatewayRegister() throws HubProcessingException {
        if (!registered) {
            invoke(HttpMethod.PUT, register, null, null);
            registered = true;
        }
        return registered;
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
            Info result = jsonParser.fromJson(json, Info.class);
            if (result == null) {
                throw new HubProcessingException("getInformation(): missing response");
            }
            return Map.of( //
                    Thing.PROPERTY_FIRMWARE_VERSION, result.getFwVersion(), //
                    Thing.PROPERTY_SERIAL_NUMBER, result.getSerialNumber());
        } catch (JsonParseException e) {
            throw new HubProcessingException("getInformation(): JsonParseException");
        }
    }

    /**
     * Get the list of scenes.
     *
     * @return the list of scenes.
     * @throws HubProcessingException if any error occurs.
     */
    public List<Scene> getScenes() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, scenes, null, null);
        try {
            return List.of(jsonParser.fromJson(json, Scene[].class));
        } catch (JsonParseException e) {
            throw new HubProcessingException("getScenes() JsonParseException");
        }
    }

    /**
     * Get the data for a single shade.
     *
     * @param shadeId the id of the shade to get.
     * @return the shade.
     * @throws HubProcessingException if any error occurs.
     */
    public Shade getShade(int shadeId) throws HubProcessingException {
        String json = invoke(HttpMethod.GET, String.format(shadeSingle, shadeId), null, null);
        try {
            Shade result = jsonParser.fromJson(json, Shade.class);
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
    public List<Shade> getShades() throws HubProcessingException {
        String json = invoke(HttpMethod.GET, shades, null, null);
        try {
            return List.of(jsonParser.fromJson(json, Shade[].class));
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
        Request request = httpClient.newRequest(url).method(method).header("Connection", "close").accept("*/*")
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
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
        if (!HTTP_OK_CODES.contains(statusCode)) {
            throw new HubProcessingException(String.format("HTTP %d error", statusCode));
        }
        String jsonResponse = response.getContentAsString();
        if (logger.isTraceEnabled()) {
            logger.trace("invoke() response JSON:{}", jsonResponse);
        }
        if (method == HttpMethod.GET && jsonResponse.isEmpty()) {
            throw new HubProcessingException("Empty response entity");
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
        String json = jsonParser.toJson(new ShadeMotion(ShadeMotion.Type.JOG));
        invoke(HttpMethod.PUT, String.format(shadeMotion, shadeId), null, json);
    }

    /**
     * Issue a command to move a shade.
     *
     * @param shadeId the shade to be moved.
     * @param shade DTO with the new position.
     * @throws HubProcessingException if any error occurs.
     */
    public void moveShade(int shadeId, Shade shade) throws HubProcessingException {
        invoke(HttpMethod.PUT, shadePositions, Query.of(IDS, Integer.toString(shadeId)), jsonParser.toJson(shade));
    }

    /**
     * Called when the SSE event channel has not received any events for a long time. This could mean that the event
     * source socket has dropped. So restart the SSE connection.
     */
    public void onSseQuiet() {
        if (!closing) {
            sseReOpen();
        }
    }

    /**
     * Handle SSE errors. For the time being just log them, because the framework should automatically recover itself.
     *
     * @param e the error that was thrown.
     */
    private void onSseError(Throwable e) {
        if (!closing) {
            logger.debug("onSseShadeError() {}", e.getMessage(), e);
        }
    }

    /**
     * Handle inbound shade SSE events.
     *
     * @param sseEvent the inbound event.
     */
    private void onSSeEvent(InboundSseEvent sseEvent) {
        if (closing) {
            return;
        }
        ScheduledFuture<?> task = sseQuietCheck;
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        sseQuietCheck = hubHandler.getScheduler().schedule(this::onSseQuiet, SLEEP_SECONDS, TimeUnit.SECONDS);
        if (sseEvent.isEmpty()) {
            return;
        }
        String json = sseEvent.readData();
        if (json == null) {
            return;
        }
        logger.trace("onSseShadeEvent() json:{}", json);
        ShadeEvent shadeEvent = jsonParser.fromJson(json, ShadeEvent.class);
        if (shadeEvent != null) {
            ShadePosition positions = shadeEvent.getCurrentPositions();
            hubHandler
                    .onShadeEvent(new Shade().setId(shadeEvent.getId()).setShadePosition(positions).setPartialState());
        }
    }

    /**
     * Close the SSE links.
     */
    private synchronized void sseClose() {
        logger.debug("sseClose() called");
        ScheduledFuture<?> task = sseQuietCheck;
        if (task != null) {
            task.cancel(true);
            sseQuietCheck = null;
        }
        SseEventSource source;
        source = this.shadeEventSource;
        if (source != null) {
            source.close();
            this.shadeEventSource = null;
        }
    }

    /**
     * Open the SSE links.
     */
    public synchronized void sseOpen() {
        sseClose();

        logger.debug("sseOpen() called");
        Client client = clientBuilder.sslContext(httpClient.getSslContextFactory().getSslContext())
                .hostnameVerifier(null).hostnameVerifier(this).readTimeout(0, TimeUnit.SECONDS).build();

        try {
            // open SSE channel for shades
            SseEventSource shadeEventSource = eventSourceFactory.newSource(client.target(shadeEvents));
            shadeEventSource.register(this::onSSeEvent, this::onSseError);
            shadeEventSource.open();
            this.shadeEventSource = shadeEventSource;
        } catch (Exception e) {
            // SSE documentation does not say what exceptions may be thrown, so catch everything
            logger.warn("sseOpen() {}", e.getMessage(), e);
        }
    }

    /**
     * Reopen the SSE links. If the event source already exists, try first to simply close and re-open it, but if that
     * fails, then completely destroy and re-create it.
     */
    private synchronized void sseReOpen() {
        logger.debug("sseReOpen() called");
        SseEventSource shadeEventSource = this.shadeEventSource;
        if (shadeEventSource != null) {
            try {
                if (shadeEventSource.isOpen()) {
                    shadeEventSource.close();
                }
                if (!shadeEventSource.isOpen()) {
                    shadeEventSource.open();
                }
                return;
            } catch (Exception e) {
                // SSE documentation does not say what exceptions may be thrown, so catch everything
                logger.warn("sseReOpen() {}", e.getMessage(), e);
            }
        }
        sseOpen();
    }

    /**
     * Issue a stop command to a shade.
     *
     * @param shadeId the shade to be stopped.
     * @throws HubProcessingException if any error occurs.
     */
    public void stopShade(int shadeId) throws HubProcessingException {
        invoke(HttpMethod.PUT, shadeStop, Query.of(IDS, Integer.toString(shadeId)), null);
    }

    /**
     * HostnameVerifier method implementation that validates the host name when opening SSE connections.
     *
     * @param hostName the host name to be verified.
     * @param sslSession (not used).
     * @return true if the host name matches our own.
     */
    @Override
    public boolean verify(@Nullable String hostName, @Nullable SSLSession sslSession) {
        return this.ipAddress.equals(hostName);
    }
}
