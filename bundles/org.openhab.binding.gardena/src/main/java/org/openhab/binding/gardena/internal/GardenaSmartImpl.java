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
package org.openhab.binding.gardena.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.AbstractTypedContentProvider;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.DataItemDeserializer;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.model.dto.api.CreateWebSocketRequest;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.api.LocationDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.LocationResponse;
import org.openhab.binding.gardena.internal.model.dto.api.LocationsResponse;
import org.openhab.binding.gardena.internal.model.dto.api.PostOAuth2Response;
import org.openhab.binding.gardena.internal.model.dto.api.WebSocketCreatedResponse;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommand;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommandRequest;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * {@link GardenaSmart} implementation to access Gardena smart system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaSmartImpl implements GardenaSmart, GardenaSmartWebSocketListener {
    private final Logger logger = LoggerFactory.getLogger(GardenaSmartImpl.class);

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
    private LocationsResponse locationsResponse;
    private GardenaSmartEventListener eventListener;

    private HttpClient httpClient;
    private List<GardenaSmartWebSocket> webSockets = new ArrayList<>();
    private @Nullable PostOAuth2Response token;
    private boolean initialized = false;
    private WebSocketFactory webSocketFactory;

    private Set<Device> devicesToNotify = ConcurrentHashMap.newKeySet();
    private @Nullable ScheduledFuture<?> deviceToNotifyFuture;
    private @Nullable ScheduledFuture<?> newDeviceFuture;

    public GardenaSmartImpl(String id, GardenaConfig config, GardenaSmartEventListener eventListener,
            ScheduledExecutorService scheduler, HttpClientFactory httpClientFactory, WebSocketFactory webSocketFactory)
            throws GardenaException {
        this.id = id;
        this.config = config;
        this.eventListener = eventListener;
        this.scheduler = scheduler;
        this.webSocketFactory = webSocketFactory;

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
                    for (DataItem<?> dataItem : locationResponse.included) {
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
            dispose();
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Starts the websockets for each location.
     */
    private void startWebsockets() throws Exception {
        for (LocationDataItem location : locationsResponse.data) {
            WebSocketCreatedResponse webSocketCreatedResponse = getWebsocketInfo(location.id);
            String socketId = id + "-" + location.attributes.name;
            webSockets.add(new GardenaSmartWebSocket(this, webSocketCreatedResponse, config, scheduler,
                    webSocketFactory, token, socketId));
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
    private <T> T executeRequest(HttpMethod method, String url, @Nullable Object content, @Nullable Class<T> result)
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
                final PostOAuth2Response token = this.token;
                if (token != null) {
                    request.header("Authorization", token.tokenType + " " + token.accessToken);
                    request.header("Authorization-provider", token.provider);
                }
                request.header("X-Api-Key", config.getApiKey());
            }

            request.content(contentProvider);
            ContentResponse contentResponse = request.send();
            int status = contentResponse.getStatus();
            if (logger.isTraceEnabled()) {
                logger.trace("<<< status:{}, {}", status, contentResponse.getContentAsString());
            }

            if (status != 200 && status != 204 && status != 201 && status != 202) {
                throw new GardenaException(String.format("Error %s %s, %s", status, contentResponse.getReason(),
                        contentResponse.getContentAsString()));
            }

            if (result == null) {
                return (T) null;
            }
            return (T) gson.fromJson(contentResponse.getContentAsString(), result);
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates or refreshes the access token for the Gardena smart system.
     */
    private synchronized void verifyToken() throws GardenaException {
        Fields fields = new Fields();
        fields.add("client_id", config.getApiKey());

        PostOAuth2Response token = this.token;
        if (token == null || token.isRefreshTokenExpired()) {
            // new token
            logger.debug("Gardena API login using password, reason: {}",
                    token == null ? "no token available" : "refresh token expired");
            fields.add("grant_type", "password");
            fields.add("username", config.getEmail());
            fields.add("password", config.getPassword());
            token = executeRequest(HttpMethod.POST, URL_API_TOKEN, fields, PostOAuth2Response.class);
            token.postProcess();
            this.token = token;
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
                this.token = token;
            } catch (GardenaException ex) {
                // refresh token issue
                this.token = null;
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
    @Override
    public void dispose() {
        logger.debug("Disposing GardenaSmart");

        final ScheduledFuture<?> newDeviceFuture = this.newDeviceFuture;
        if (newDeviceFuture != null) {
            newDeviceFuture.cancel(true);
        }

        final ScheduledFuture<?> deviceToNotifyFuture = this.deviceToNotifyFuture;
        if (deviceToNotifyFuture != null) {
            deviceToNotifyFuture.cancel(true);
        }
        stopWebsockets();
        try {
            httpClient.stop();
        } catch (Exception e) {
            // ignore
        }
        httpClient.destroy();
        locationsResponse = new LocationsResponse();
        allDevicesById.clear();
        initialized = false;
    }

    /**
     * Restarts all websockets.
     */
    @Override
    public synchronized void restartWebsockets() {
        logger.debug("Restarting GardenaSmart Webservice");
        stopWebsockets();
        try {
            startWebsockets();
        } catch (Exception ex) {
            logger.warn("Restarting GardenaSmart Webservice failed: {}, restarting binding", ex.getMessage());
            eventListener.onError();
        }
    }

    /**
     * Sets the dataItem from the websocket event into the correct device.
     */
    private void handleDataItem(final DataItem<?> dataItem) throws GardenaException {
        final String deviceId = dataItem.getDeviceId();
        Device device = allDevicesById.get(deviceId);
        if (device == null && !(dataItem instanceof LocationDataItem)) {
            device = new Device(deviceId);
            allDevicesById.put(device.id, device);

            if (initialized) {
                newDeviceFuture = scheduler.schedule(() -> {
                    Device newDevice = allDevicesById.get(deviceId);
                    if (newDevice != null) {
                        newDevice.evaluateDeviceType();
                        if (newDevice.deviceType != null) {
                            eventListener.onNewDevice(newDevice);
                        }
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
    public void onWebSocketError() {
        eventListener.onError();
    }

    @Override
    public void onWebSocketMessage(String msg) {
        try {
            DataItem<?> dataItem = gson.fromJson(msg, DataItem.class);
            if (dataItem != null) {
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
            }
        } catch (GardenaException | JsonSyntaxException ex) {
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
    public void sendCommand(DataItem<?> dataItem, GardenaCommand gardenaCommand) throws GardenaException {
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
