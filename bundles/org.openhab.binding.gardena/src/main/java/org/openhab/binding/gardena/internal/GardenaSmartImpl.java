/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.DataItemDeserializer;
import org.openhab.binding.gardena.internal.model.dto.Device;
import org.openhab.binding.gardena.internal.model.dto.api.CreateWebSocketRequest;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.api.Location;
import org.openhab.binding.gardena.internal.model.dto.api.LocationDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.LocationResponse;
import org.openhab.binding.gardena.internal.model.dto.api.LocationsResponse;
import org.openhab.binding.gardena.internal.model.dto.api.PostOAuth2Response;
import org.openhab.binding.gardena.internal.model.dto.api.WebSocket;
import org.openhab.binding.gardena.internal.model.dto.api.WebSocketCreatedResponse;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommand;
import org.openhab.binding.gardena.internal.model.dto.command.GardenaCommandRequest;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.util.ThingWebClientUtil;
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
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private Gson gson = new GsonBuilder().registerTypeAdapter(DataItem.class, new DataItemDeserializer()).create();

    private static final String URL_API_HUSQUARNA = "https://api.authentication.husqvarnagroup.dev/v1";
    private static final String URL_API_GARDENA = "https://api.smart.gardena.dev/v1";
    private static final String URL_API_TOKEN = URL_API_HUSQUARNA + "/oauth2/token";
    private static final String URL_API_WEBSOCKET = URL_API_GARDENA + "/websocket";
    private static final String URL_API_LOCATIONS = URL_API_GARDENA + "/locations";
    private static final String URL_API_COMMAND = URL_API_GARDENA + "/command";

    private final String id;
    private final GardenaConfig config;
    private final ScheduledExecutorService scheduler;

    private final Map<String, Device> allDevicesById = new HashMap<>();
    private @Nullable LocationsResponse locationsResponse = null;
    private final GardenaSmartEventListener eventListener;

    private final HttpClient httpClient;
    private final Map<String, GardenaSmartWebSocket> webSockets = new HashMap<>();
    private @Nullable PostOAuth2Response token;
    private boolean initialized = false;
    private final WebSocketClient webSocketClient;

    private final Set<Device> devicesToNotify = ConcurrentHashMap.newKeySet();
    private final Object deviceUpdateTaskLock = new Object();
    private @Nullable ScheduledFuture<?> deviceUpdateTask;
    private final Object newDeviceTasksLock = new Object();
    private final List<ScheduledFuture<?>> newDeviceTasks = new ArrayList<>();

    public GardenaSmartImpl(ThingUID uid, GardenaConfig config, GardenaSmartEventListener eventListener,
            ScheduledExecutorService scheduler, HttpClientFactory httpClientFactory, WebSocketFactory webSocketFactory)
            throws GardenaException {
        this.id = uid.getId();
        this.config = config;
        this.eventListener = eventListener;
        this.scheduler = scheduler;

        String name = ThingWebClientUtil.buildWebClientConsumerName(uid, null);
        httpClient = httpClientFactory.createHttpClient(name);
        httpClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
        httpClient.setIdleTimeout(httpClient.getConnectTimeout());

        name = ThingWebClientUtil.buildWebClientConsumerName(uid, "ws-");
        webSocketClient = webSocketFactory.createWebSocketClient(name);
        webSocketClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
        webSocketClient.setStopTimeout(3000);
        webSocketClient.setMaxIdleTimeout(150000);

        logger.debug("Starting GardenaSmart");
        try {
            httpClient.start();
            webSocketClient.start();

            // initially load access token
            verifyToken();
            LocationsResponse locationsResponse = loadLocations();
            this.locationsResponse = locationsResponse;

            // assemble devices
            if (locationsResponse.data != null) {
                for (LocationDataItem location : locationsResponse.data) {
                    LocationResponse locationResponse = loadLocation(location.id);
                    if (locationResponse.included != null) {
                        for (DataItem<?> dataItem : locationResponse.included) {
                            handleDataItem(dataItem);
                        }
                    }
                }
            }

            for (Device device : allDevicesById.values()) {
                device.evaluateDeviceType();
            }

            startWebsockets();
            initialized = true;
        } catch (GardenaException ex) {
            dispose();
            // pass GardenaException to calling function
            throw ex;
        } catch (Exception ex) {
            dispose();
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Starts the websockets for each location.
     */
    private void startWebsockets() throws Exception {
        LocationsResponse locationsResponse = this.locationsResponse;
        if (locationsResponse != null) {
            for (LocationDataItem location : locationsResponse.data) {
                WebSocketCreatedResponse webSocketCreatedResponse = getWebsocketInfo(location.id);
                Location locationAttributes = location.attributes;
                WebSocket webSocketAttributes = webSocketCreatedResponse.data.attributes;
                if (locationAttributes == null || webSocketAttributes == null) {
                    continue;
                }
                String socketId = id + "-" + locationAttributes.name;
                webSockets.put(location.id, new GardenaSmartWebSocket(this, webSocketClient, scheduler,
                        webSocketAttributes.url, token, socketId, location.id));
            }
        }
    }

    /**
     * Stops all websockets.
     */
    private void stopWebsockets() {
        for (GardenaSmartWebSocket webSocket : webSockets.values()) {
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
                if (content instanceof Fields contentAsFields) {
                    contentProvider = new FormContentProvider(contentAsFields);
                    contentType = "application/x-www-form-urlencoded";
                } else {
                    contentProvider = new StringContentProvider(gson.toJson(content));
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace(">>> {} {}, data: {}", method, url, content == null ? null : gson.toJson(content));
            }

            Request request = httpClient.newRequest(url).method(method).header(HttpHeader.CONTENT_TYPE, contentType)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header(HttpHeader.ACCEPT, "application/vnd.api+json").header(HttpHeader.ACCEPT_ENCODING, "gzip");

            if (!URL_API_TOKEN.equals(url)) {
                verifyToken();
                final PostOAuth2Response token = this.token;
                if (token != null) {
                    request.header("Authorization", token.tokenType + " " + token.accessToken);
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
                        contentResponse.getContentAsString()), status);
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
            logger.debug("Gardena API login using apiSecret, reason: {}",
                    token == null ? "no token available" : "refresh token expired");
            fields.add("grant_type", "client_credentials");
            fields.add("client_secret", config.getApiSecret());
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
        initialized = false;
        synchronized (newDeviceTasksLock) {
            for (ScheduledFuture<?> task : newDeviceTasks) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
            newDeviceTasks.clear();
        }
        synchronized (deviceUpdateTaskLock) {
            devicesToNotify.clear();
            ScheduledFuture<?> task = deviceUpdateTask;
            if (task != null) {
                task.cancel(true);
            }
            deviceUpdateTask = null;
        }
        stopWebsockets();
        try {
            httpClient.stop();
            webSocketClient.stop();
        } catch (Exception e) {
            // ignore
        }
        httpClient.destroy();
        webSocketClient.destroy();
        allDevicesById.clear();
        locationsResponse = null;
    }

    /**
     * Restarts all websockets.
     */
    @Override
    public synchronized void restartWebsockets() {
        logger.debug("Restarting GardenaSmart Webservices");
        stopWebsockets();
        try {
            startWebsockets();
        } catch (Exception ex) {
            // restart binding
            if (logger.isDebugEnabled()) {
                logger.warn("Restarting GardenaSmart Webservices failed! Restarting binding", ex);
            } else {
                logger.warn("Restarting GardenaSmart Webservices failed: {}! Restarting binding", ex.getMessage());
            }
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

            synchronized (newDeviceTasksLock) {
                // remove prior completed tasks from the list
                newDeviceTasks.removeIf(task -> task.isDone());
                // add a new scheduled task to the list
                newDeviceTasks.add(scheduler.schedule(() -> {
                    if (initialized) {
                        Device newDevice = allDevicesById.get(deviceId);
                        if (newDevice != null) {
                            newDevice.evaluateDeviceType();
                            if (newDevice.deviceType != null) {
                                eventListener.onNewDevice(newDevice);
                            }
                        }
                    }
                }, 3, TimeUnit.SECONDS));
            }
        }

        if (device != null) {
            device.setDataItem(dataItem);
        }
    }

    @Override
    public void onWebSocketClose(String id) {
        restartWebsocket(webSockets.get(id));
    }

    @Override
    public void onWebSocketError(String id) {
        restartWebsocket(webSockets.get(id));
    }

    private void restartWebsocket(@Nullable GardenaSmartWebSocket socket) {
        synchronized (this) {
            if (socket != null && !socket.isClosing()) {
                // close socket, if still open
                logger.info("Restarting GardenaSmart Webservice ({})", socket.getSocketID());
                socket.stop();
            } else {
                // if socket is already closing, exit function and do not restart socket
                return;
            }
        }

        try {
            Thread.sleep(3000);
            WebSocketCreatedResponse webSocketCreatedResponse = getWebsocketInfo(socket.getLocationID());
            // only restart single socket, do not restart binding
            WebSocket webSocketAttributes = webSocketCreatedResponse.data.attributes;
            if (webSocketAttributes != null) {
                socket.restart(webSocketAttributes.url);
            }
        } catch (Exception ex) {
            // restart binding on error
            logger.warn("Restarting GardenaSmart Webservice failed ({}): {}, restarting binding", socket.getSocketID(),
                    ex.getMessage());
            eventListener.onError();
        }
    }

    @Override
    public void onWebSocketMessage(String msg) {
        try {
            DataItem<?> dataItem = gson.fromJson(msg, DataItem.class);
            if (dataItem != null) {
                handleDataItem(dataItem);
                Device device = allDevicesById.get(dataItem.getDeviceId());
                if (device != null && device.active) {
                    synchronized (deviceUpdateTaskLock) {
                        devicesToNotify.add(device);

                        // delay the deviceUpdated event to filter multiple events for the same device dataItem property
                        ScheduledFuture<?> task = this.deviceUpdateTask;
                        if (task == null || task.isDone()) {
                            deviceUpdateTask = scheduler.schedule(() -> notifyDevicesUpdated(), 1, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        } catch (GardenaException | JsonSyntaxException ex) {
            logger.warn("Ignoring message: {}", ex.getMessage());
        }
    }

    /**
     * Helper scheduler task to update devices
     */
    private void notifyDevicesUpdated() {
        synchronized (deviceUpdateTaskLock) {
            if (initialized) {
                Iterator<Device> notifyIterator = devicesToNotify.iterator();
                while (notifyIterator.hasNext()) {
                    eventListener.onDeviceUpdated(notifyIterator.next());
                    notifyIterator.remove();
                }
            }
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
