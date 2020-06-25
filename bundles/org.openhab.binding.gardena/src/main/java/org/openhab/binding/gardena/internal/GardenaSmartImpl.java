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
package org.openhab.binding.gardena.internal;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.AbstractTypedContentProvider;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.DataItemDeserializer;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.api.*;
import org.openhab.binding.gardena.internal.model.command.GardenaCommand;
import org.openhab.binding.gardena.internal.model.command.GardenaCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * {@link GardenaSmart} implementation to access Gardena smart system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaSmartImpl implements GardenaSmart, GardenaSmartWebSocketListener {
    private final Logger logger = LoggerFactory.getLogger(GardenaSmartImpl.class);

    private static final long RESTART_DELAY_SECONDS = 10;

    private Gson gson = new GsonBuilder().registerTypeAdapter(DataItem.class, new DataItemDeserializer()).create();

    private static final String URL_API_HUSQUARNA = "https://api.authentication.husqvarnagroup.dev/v1";
    private static final String URL_API_GARDENA = "https://api.smart.gardena.dev/v1";
    private static final String URL_API_TOKEN = URL_API_HUSQUARNA + "/oauth2/token";
    private static final String URL_API_WEBSOCKET = URL_API_GARDENA + "/websocket";
    private static final String URL_API_LOCATIONS = URL_API_GARDENA + "/locations";
    private static final String URL_API_COMMAND = URL_API_GARDENA + "/command";

    private String id;
    private GardenaConfig config;
    private ScheduledExecutorService scheduler;

    private Map<String, Device> allDevicesById = new HashMap<>();
    private LocationsResponse locationsResponse = new LocationsResponse();
    private GardenaSmartEventListener eventListener;

    private HttpClient httpClient;
    private List<GardenaSmartWebSocket> webSockets = new ArrayList<>();
    private PostOAuth2Response token;
    private boolean initialized = false;
    private ScheduledFuture<?> restartScheduledFuture;
    private HttpClientFactory httpClientFactory;
    private WebSocketFactory webSocketFactory;

    private Set<Device> devicesToNotify = Collections.synchronizedSet(new HashSet<>());
    private ScheduledFuture<?> deviceToNotifyFuture;

    @Override
    public void init(String id, GardenaConfig config, GardenaSmartEventListener eventListener,
            ScheduledExecutorService scheduler, HttpClientFactory httpClientFactory, WebSocketFactory webSocketFactory)
            throws GardenaException {
        this.id = id;
        this.config = config;
        this.eventListener = eventListener;
        this.scheduler = scheduler;
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;

        start();
    }

    /**
     * Starts the client, loads the locations and devices.
     */
    private void start() throws GardenaException {
        logger.debug("Starting GardenaSmart");
        try {
            httpClient = httpClientFactory.createHttpClient(id);
            httpClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
            httpClient.setIdleTimeout(httpClient.getConnectTimeout());
            httpClient.start();

            // initially load access token
            verifyToken();
            locationsResponse = loadLocations();

            // assemble devices
            for (LocationDataItem location : locationsResponse.data) {
                LocationResponse locationResponse = loadLocation(location.id);
                if (locationResponse.included != null) {
                    for (DataItem dataItem : locationResponse.included) {
                        handleDataItem(dataItem);
                    }
                }
            }

            for (Device device : allDevicesById.values()) {
                device.evaluateDeviceType();
            }

            startWebsockets();
            initialized = true;
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage(), ex);
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Starts the websockets for each location.
     */
    private void startWebsockets() throws Exception {
        for (LocationDataItem location : locationsResponse.data) {
            WebSocketCreatedResponse webSocketCreatedResponse = getWebsocketInfo(location.id);
            webSockets.add(new GardenaSmartWebSocket(this, webSocketCreatedResponse, config, scheduler,
                    webSocketFactory, token, location.attributes));
        }
    }

    /**
     * Stops all websockets.
     */
    private void stopWebsockets() {
        for (GardenaSmartWebSocket webSocket : webSockets) {
            webSocket.stop();
        }
        webSockets.clear();
    }

    /**
     * Communicates with Gardena smart home system and parses the result.
     */
    private <T> T executeRequest(HttpMethod method, String url, Object content, Class<T> result)
            throws GardenaException {
        try {
            AbstractTypedContentProvider contentProvider = null;
            String contentType = "application/vnd.api+json";
            if (content != null) {
                if (content instanceof Fields) {
                    contentProvider = new FormContentProvider((Fields) content);
                    contentType = "application/x-www-form-urlencoded";
                } else {
                    contentProvider = new StringContentProvider(gson.toJson(content));
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace(">>> {} {}, data: {}", method, url, content == null ? null : gson.toJson(content));
            }

            Request request = httpClient.newRequest(url).method(method).header(HttpHeader.CONTENT_TYPE, contentType)
                    .header(HttpHeader.ACCEPT, "application/vnd.api+json").header(HttpHeader.ACCEPT_ENCODING, "gzip");

            if (!URL_API_TOKEN.equals(url)) {
                verifyToken();
                request.header("Authorization", token.tokenType + " " + token.accessToken);
                request.header("Authorization-provider", token.provider);
                request.header("X-Api-Key", config.getApiKey());
            }

            request.content(contentProvider);
            ContentResponse contentResponse = request.send();
            int status = contentResponse.getStatus();
            if (logger.isTraceEnabled()) {
                logger.trace("<<< status:{}, {}", status, contentResponse.getContentAsString());
            }

            if (status != 200 && status != 204 && status != 201 && status != 202) {
                throw new GardenaException(String.format("Error %s %s", status, contentResponse.getReason()));
            }

            if (result == null) {
                return null;
            }
            return gson.fromJson(contentResponse.getContentAsString(), result);
        } catch (Exception ex) {
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates or refreshes the access token for the Gardena smart system.
     */
    private synchronized void verifyToken() throws GardenaException {
        Fields fields = new Fields();
        fields.add("client_id", config.getApiKey());

        if (token == null || token.isRefreshTokenExpired()) {
            // new token
            logger.debug("Gardena API login using password, reason: {}",
                    token == null ? "no token available" : "refresh token expired");
            fields.add("grant_type", "password");
            fields.add("username", config.getEmail());
            fields.add("password", config.getPassword());
            token = executeRequest(HttpMethod.POST, URL_API_TOKEN, fields, PostOAuth2Response.class);
            token.postProcess();
        } else if (token.isAccessTokenExpired()) {
            // refresh token
            logger.debug("Gardena API login using refreshToken, reason: access token expired");
            fields.add("grant_type", "refresh_token");
            fields.add("refresh_token", token.refreshToken);
            try {
                PostOAuth2Response tempToken = executeRequest(HttpMethod.POST, URL_API_TOKEN, fields,
                        PostOAuth2Response.class);
                token.accessToken = tempToken.accessToken;
                token.expiresIn = tempToken.expiresIn;
                token.postProcess();
            } catch (GardenaException ex) {
                // refresh token issue
                token = null;
                verifyToken();
            }
        } else {
            logger.debug("Gardena API token valid");
        }
        logger.debug("{}", token.toString());
    }

    /**
     * Loads all locations.
     */
    private LocationsResponse loadLocations() throws GardenaException {
        return executeRequest(HttpMethod.GET, URL_API_LOCATIONS, null, LocationsResponse.class);
    }

    /**
     * Loads all devices for a given location.
     */
    private LocationResponse loadLocation(String locationId) throws GardenaException {
        return executeRequest(HttpMethod.GET, URL_API_LOCATIONS + "/" + locationId, null, LocationResponse.class);
    }

    /**
     * Returns the websocket url for a given location.
     */
    private WebSocketCreatedResponse getWebsocketInfo(String locationId) throws GardenaException {
        return executeRequest(HttpMethod.POST, URL_API_WEBSOCKET, new CreateWebSocketRequest(locationId),
                WebSocketCreatedResponse.class);
    }

    /**
     * Stops the client.
     */
    public void dispose() {
        logger.debug("Disposing GardenaSmart");
        if (restartScheduledFuture != null) {
            restartScheduledFuture.cancel(true);
        }
        if (deviceToNotifyFuture != null) {
            deviceToNotifyFuture.cancel(true);
        }
        stopWebsockets();
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                // ignore
            }
            httpClient.destroy();
        }
        locationsResponse = new LocationsResponse();
        allDevicesById.clear();
        initialized = false;
    }

    /**
     * Restarts all websockets.
     */
    private synchronized void restartWebsockets() {
        logger.debug("Restarting GardenaSmart Webservice");
        stopWebsockets();
        try {
            startWebsockets();
            eventListener.onConnectionResumed();
        } catch (Exception ex) {
            eventListener.onConnectionLost();
            logger.warn("Restarting GardenaSmart Webservice failed: {}, try restart in {} seconds", ex.getMessage(),
                    RESTART_DELAY_SECONDS);
            restartScheduledFuture = scheduler.schedule(() -> {
                restartWebsockets();
            }, RESTART_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Sets the dataItem from the websocket event into the correct device.
     */
    private void handleDataItem(final DataItem dataItem) throws GardenaException {
        final String deviceId = dataItem.getDeviceId();
        Device device = allDevicesById.get(deviceId);
        if (device == null && !(dataItem instanceof LocationDataItem)) {
            device = new Device(deviceId);
            allDevicesById.put(device.id, device);

            if (initialized) {
                scheduler.schedule(() -> {
                    Device newDevice = allDevicesById.get(deviceId);
                    newDevice.evaluateDeviceType();
                    if (newDevice.deviceType != null) {
                        eventListener.onNewDevice(newDevice);
                    }
                }, 3, TimeUnit.SECONDS);
            }
        }

        if (device != null) {
            device.setDataItem(dataItem);
        }
    }

    @Override
    public void onWebSocketClose() {
        restartWebsockets();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        restartWebsockets();
    }

    @Override
    public void onWebSocketMessage(String msg) {
        try {
            DataItem dataItem = gson.fromJson(msg, DataItem.class);
            handleDataItem(dataItem);
            Device device = allDevicesById.get(dataItem.getDeviceId());
            if (device != null && device.active) {
                devicesToNotify.add(device);

                // delay the deviceUpdated event to filter multiple events for the same device dataItem property
                if (deviceToNotifyFuture == null) {
                    deviceToNotifyFuture = scheduler.schedule(() -> {
                        deviceToNotifyFuture = null;
                        Iterator<Device> notifyIterator = devicesToNotify.iterator();
                        while (notifyIterator.hasNext()) {
                            eventListener.onDeviceUpdated(notifyIterator.next());
                            notifyIterator.remove();
                        }
                    }, 1, TimeUnit.SECONDS);
                }
            }
        } catch (Exception ex) {
            logger.warn("Ignoring message: {}", ex.getMessage());
        }
    }

    @Override
    public Device getDevice(String deviceId) throws GardenaDeviceNotFoundException {
        Device device = allDevicesById.get(deviceId);
        if (device == null) {
            throw new GardenaDeviceNotFoundException("Device with id " + deviceId + " not found");
        }
        return device;
    }

    @Override
    public void sendCommand(DataItem dataItem, GardenaCommand gardenaCommand) throws GardenaException {
        executeRequest(HttpMethod.PUT, URL_API_COMMAND + "/" + dataItem.id, new GardenaCommandRequest(gardenaCommand),
                null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<Device> getAllDevices() {
        return allDevicesById.values();
    }
}
