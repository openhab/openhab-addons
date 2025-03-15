/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal.handler;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeathome.internal.FreeAtHomeDiscoveryService;
import org.openhab.binding.freeathome.internal.configuration.FreeAtHomeBridgeHandlerConfiguration;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * The {@link FreeAtHomeBridgeHandler} is responsible for handling the free@home bridge and
 * its main communication.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler implements WebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    private final Map<String, FreeAtHomeDeviceHandler> mapEventListeners = new ConcurrentHashMap<>();

    // Clients for the network communication
    private final HttpClient httpClient;
    private @Nullable WebSocketClient websocketClient = null;
    private final FreeAtHomeWebsocketMonitorThread socketMonitor = new FreeAtHomeWebsocketMonitorThread();
    private @Nullable QueuedThreadPool jettyThreadPool = null;
    private volatile @Nullable Session websocketSession = null;

    private final String sysApUID = "00000000-0000-0000-0000-000000000000";
    private String ipAddress = "";
    private String username = "";
    private String password = "";
    private boolean sendKeepAliveMessage = true;

    private String baseUrl = "";

    private String authField = "";

    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean httpConnectionOK = new AtomicBoolean(false);
    private final Condition websocketSessionEstablished = lock.newCondition();

    int numberOfComponents = 0;

    private static final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 5; // Seconds
    private static final int BRIDGE_WEBSOCKET_TIMEOUT = 90;
    private static final int BRIDGE_WEBSOCKET_KEEPALIVE = 10; // Seconds
    private static final String BRIDGE_URL_GETDEVICELIST = "/rest/devicelist";

    public FreeAtHomeBridgeHandler(Bridge thing, HttpClient client) {
        super(thing);

        httpClient = client;
    }

    /**
     * stub method for handlCommand
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Unknown handle command for the bridge - channellUID {}, command {}", channelUID, command);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(FreeAtHomeDiscoveryService.class);
    }

    /**
     * Method to get the device list
     */
    public List<String> getDeviceDeviceList() throws FreeAtHomeHttpCommunicationException {
        List<String> listOfComponentId = new ArrayList<String>();
        boolean ret = false;

        listOfComponentId.clear();

        String url = baseUrl + BRIDGE_URL_GETDEVICELIST;

        // Perform a simple GET and wait for the response.
        try {
            HttpClient client = httpClient;

            Request req = client.newRequest(url);

            if (req == null) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid request object in getDeviceDeviceList with the URL [ " + url + " ]");
            }

            ContentResponse response = req.send();

            // Get component List
            String componentListString = new String(response.getContent());

            JsonElement jsonTree = JsonParser.parseString(componentListString);

            // check the output
            if (!jsonTree.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid jsonObject in getDeviceDeviceList with the URL [ " + url + " ]");
            }

            JsonObject jsonObject = jsonTree.getAsJsonObject();

            // Get the main object
            JsonElement listOfComponents = jsonObject.get(sysApUID);

            if (listOfComponents == null) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Devices Section is missing in getDeviceDeviceList with the URL [ " + url + " ]");
            }

            JsonArray array = listOfComponents.getAsJsonArray();

            this.numberOfComponents = array.size();

            for (int i = 0; i < array.size(); i++) {
                JsonElement basicElement = array.get(i);

                listOfComponentId.add(basicElement.getAsString());
            }

            ret = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Error to build up the Component list [ {} ]", e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("Error to build up the Component list [ {} ]", e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted in getDeviceList [ " + e.getMessage() + " ]");
        }

        // Scan finished but error. clear the list
        if (!ret) {
            listOfComponentId.clear();
        }

        return listOfComponentId;
    }

    /**
     * Method to send http request to get the device description
     */
    public FreeAtHomeDeviceDescription getFreeatHomeDeviceDescription(String id)
            throws FreeAtHomeHttpCommunicationException {
        FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();

        String url = baseUrl + "/rest/device/" + sysApUID + "/" + id;
        try {
            HttpClient client = httpClient;
            Request req = client.newRequest(url);

            if (req == null) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid request object in getDatapoint with the URL [ " + url + " ]");
            }

            ContentResponse response;
            response = req.send();

            // Get component List
            String deviceString = new String(response.getContent());

            JsonReader reader = new JsonReader(new StringReader(deviceString));
            reader.setLenient(true);
            JsonElement jsonTree = JsonParser.parseReader(reader);

            if (!jsonTree.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "No data is received by getDatapoint with the URL [ " + url + " ]");
            }

            if (!jsonTree.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid jsonObject in getFreeatHomeDeviceDescription with the URL [ " + url + " ]");
            }

            // check the output
            JsonObject jsonObject = jsonTree.getAsJsonObject();

            if (!jsonObject.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Main jsonObject is invalid in getFreeatHomeDeviceDescription with the URL [ " + url + " ]");
            }

            jsonObject = jsonObject.getAsJsonObject(sysApUID);

            if (!jsonObject.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "jsonObject is invalid in getFreeatHomeDeviceDescription with the URL [ " + url + " ]");
            }

            jsonObject = jsonObject.getAsJsonObject("devices");

            if (!jsonObject.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Devices Section is missing in getFreeatHomeDeviceDescription with the URL [ " + url + " ]");
            }

            device = new FreeAtHomeDeviceDescription(jsonObject, id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("No communication possible to get device list - Communication interrupt [ {} ]",
                    e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("No communication possible to get device list - Communication interrupt [ {} ]",
                    e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted in getDeviceList [ " + e.getMessage() + " ]");
        }

        return device;
    }

    /**
     * Method to get datapoint values for devices
     */
    public String getDatapoint(String deviceId, String channel, String datapoint)
            throws FreeAtHomeHttpCommunicationException {
        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {
            Request req = httpClient.newRequest(url);

            logger.debug("Get datapoint by url: {}", url);

            if (req == null) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid request object in getDatapoint with the URL [ " + url + " ]");
            }

            ContentResponse response = req.send();

            if (response.getStatus() != 200) {
                throw new FreeAtHomeHttpCommunicationException(response.getStatus(), response.getReason());
            }

            String deviceString = new String(response.getContent());

            JsonReader reader = new JsonReader(new StringReader(deviceString));
            reader.setLenient(true);
            JsonElement jsonTree = JsonParser.parseReader(reader);

            if (!jsonTree.isJsonObject()) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "No data is received by getDatapoint with the URL [ " + url + " ]");
            }

            JsonObject jsonObject = jsonTree.getAsJsonObject();

            jsonObject = jsonObject.getAsJsonObject(sysApUID);
            JsonArray jsonValueArray = jsonObject.getAsJsonArray("values");

            JsonElement element = jsonValueArray.get(0);
            String value = element.getAsString();

            if (value.isEmpty()) {
                value = "0";
            }

            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication timout or execution interrupted [ " + e.getMessage() + " ]");
        } catch (JsonParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Invalid JSON file is received by getDatapoint with the URL [ " + e.getMessage() + " ]");
        }
    }

    /**
     * Method to set datapoint values in channels
     */
    public boolean setDatapoint(String deviceId, String channel, String datapoint, String valueString)
            throws FreeAtHomeHttpCommunicationException {
        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {
            Request req = httpClient.newRequest(url);

            if (req == null) {
                throw new FreeAtHomeHttpCommunicationException(0,
                        "Invalid request object in getDatapoint with the URL [ " + url + " ]");
            }

            req.content(new StringContentProvider(valueString));
            req.method(HttpMethod.PUT);

            logger.debug("Set datapoint by url: {} value: {}", url, valueString);

            ContentResponse response = req.send();

            if (response.getStatus() != 200) {
                throw new FreeAtHomeHttpCommunicationException(response.getStatus(), response.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        }

        return true;
    }

    /**
     * Method to process socket events
     */
    public void setDatapointOnWebsocketFeedback(String receivedText) {
        JsonReader reader = new JsonReader(new StringReader(receivedText));
        reader.setLenient(true);
        JsonElement jsonTree = JsonParser.parseReader(reader);

        // check the output
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();

            jsonObject = jsonObject.getAsJsonObject(sysApUID);
            jsonObject = jsonObject.getAsJsonObject("datapoints");

            Set<String> keys = jsonObject.keySet();

            Iterator<String> iter = keys.iterator();

            while (iter.hasNext()) {
                String eventDatapointID = iter.next();

                JsonElement element = jsonObject.get(eventDatapointID);
                String value = element.getAsString();

                String[] parts = eventDatapointID.split("/");

                FreeAtHomeDeviceHandler deviceHandler = mapEventListeners.get(parts[0]);

                if (deviceHandler != null) {
                    deviceHandler.onDeviceStateChanged(eventDatapointID, value);
                    logger.debug("Socket event processed: event-datapoint-ID {} value {}", eventDatapointID, value);
                } else {
                    logger.debug("Socket event not processed: event-datapoint-ID {} value {}", eventDatapointID, value);
                }
            }
        }
    }

    public void markDeviceRemovedOnWebsocketFeedback(String receivedText) {
        JsonReader reader = new JsonReader(new StringReader(receivedText));
        reader.setLenient(true);
        JsonElement jsonTree = JsonParser.parseReader(reader);

        // check the output
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();

            jsonObject = jsonObject.getAsJsonObject(sysApUID);
            JsonArray jsonArray = jsonObject.getAsJsonArray("devicesRemoved");

            for (JsonElement element : jsonArray) {
                FreeAtHomeDeviceHandler deviceHandler = mapEventListeners.get(element.getAsString());

                if (deviceHandler != null) {
                    deviceHandler.onDeviceRemoved();

                    logger.debug("Device removal processed");
                }
            }
        }
    }

    public void registerDeviceStateListener(String deviceID, FreeAtHomeDeviceHandler deviceHandler) {
        mapEventListeners.put(deviceID, deviceHandler);
    }

    public void unregisterDeviceStateListener(String deviceID) {
        mapEventListeners.remove(deviceID);
    }

    /**
     * Establishes an HTTP connection to the free@home system.
     * This method sets up authentication and tests the connection by making a request.
     *
     * @return true if the HTTP connection is successfully established, false otherwise.
     */
    public boolean openHttpConnection() {
        logger.debug("Attempting to open HTTP connection to free@home system");

        try {
            // Set up authentication for the HTTP client
            AuthenticationStore auth = httpClient.getAuthenticationStore();
            URI baseUri = new URI(baseUrl);
            auth.addAuthenticationResult(new BasicAuthentication.BasicResult(baseUri, username, password));

            // Construct the URL for the device list (used as a test endpoint)
            String testUrl = baseUrl + BRIDGE_URL_GETDEVICELIST;
            logger.debug("Test URL for HTTP connection: {}", testUrl);

            // Create and send a test request
            Request request = httpClient.newRequest(testUrl);
            if (request == null) {
                throw new IllegalStateException("Failed to create HTTP request");
            }

            // Send the request and get the response
            ContentResponse response = request.send();

            // Check the response status
            int statusCode = response.getStatus();
            logger.debug("HTTP response status code: {}", statusCode);

            if (statusCode == HttpStatus.OK_200) {
                logger.info("HTTP connection to free@home system established successfully");
                httpConnectionOK.set(true);
                return true;
            } else {
                logger.warn("HTTP connection failed. Status code: {}, Reason: {}", statusCode, response.getReason());
                httpConnectionOK.set(false);
                return false;
            }

        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax for base URL: {}", baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("HTTP connection attempt was interrupted", e);
        } catch (TimeoutException e) {
            logger.error("HTTP connection attempt timed out", e);
        } catch (ExecutionException e) {
            logger.error("Error executing HTTP request", e);
        } catch (Exception e) {
            logger.error("Unexpected error while opening HTTP connection", e);
        }

        httpConnectionOK.set(false);
        return false;
    }

    /**
     * Method to connect the websocket session
     */
    public boolean connectWebsocketSession() {
        boolean ret = false;

        URI uri = URI.create("ws://" + ipAddress + "/fhapi/v1/api/ws");

        String authString = username + ":" + password;

        // create base64 encoder
        Base64.Encoder bas64Encoder = Base64.getEncoder();

        // Encoding string using encoder object
        String authStringEnc = bas64Encoder.encodeToString(authString.getBytes());

        authField = "Basic " + authStringEnc;

        WebSocketClient localWebsocketClient = websocketClient;

        try {
            // Start socket client
            if (localWebsocketClient != null) {
                localWebsocketClient.setMaxTextMessageBufferSize(8 * 1024);
                localWebsocketClient.setMaxIdleTimeout(BRIDGE_WEBSOCKET_TIMEOUT * 60 * 1000);
                localWebsocketClient.setConnectTimeout(BRIDGE_WEBSOCKET_TIMEOUT * 60 * 1000);
                localWebsocketClient.start();
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setHeader("Authorization", authField);
                request.setTimeout(BRIDGE_WEBSOCKET_TIMEOUT, TimeUnit.MINUTES);
                localWebsocketClient.connect(this, uri, request);

                logger.debug("Websocket connection to SysAP is OK, timeout: {}", BRIDGE_WEBSOCKET_TIMEOUT);

                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            logger.debug("Error by opening Websocket connection [{}]", e.getMessage());

            if (localWebsocketClient != null) {
                try {
                    localWebsocketClient.stop();

                    ret = false;
                } catch (Exception e1) {
                    logger.debug("Error by opening Websocket connection [{}]", e1.getMessage());

                    ret = false;
                }
            } else {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * Method to close the websocket connection
     */
    public void closeWebSocketConnection() {
        socketMonitor.interrupt();

        QueuedThreadPool localThreadPool = jettyThreadPool;

        if (localThreadPool != null) {
            try {
                localThreadPool.stop();
            } catch (Exception e1) {
                logger.debug("Error by closing Websocket connection [{}]", e1.getMessage());
            }
            jettyThreadPool = null;
        }

        WebSocketClient localWebSocketClient = websocketClient;

        if (localWebSocketClient != null) {
            try {
                localWebSocketClient.stop();
            } catch (Exception e2) {
                logger.debug("Error by closing Websocket connection [{}]", e2.getMessage());
            }
            websocketClient = null;
        }
    }

    /**
     * Opens a WebSocket connection to the free@home system.
     * This method initializes the thread pool and WebSocket client if they don't exist.
     *
     * @return true if the WebSocket connection is successfully opened or already exists, false otherwise.
     */
    public boolean openWebSocketConnection() {
        boolean ret = false;

        try {
            // Fetch and log the current Jetty version
            String jettyVersion = org.eclipse.jetty.util.Jetty.VERSION;
            logger.debug("Current Jetty version: {}", jettyVersion);

            // Initialize or retrieve the thread pool
            QueuedThreadPool localThreadPool = jettyThreadPool;

            if (localThreadPool == null) {
                // Create a new thread pool if it doesn't exist
                jettyThreadPool = new QueuedThreadPool();
                localThreadPool = jettyThreadPool;

                if (localThreadPool != null) {
                    // Configure the thread pool
                    localThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
                    localThreadPool.setDaemon(true);
                    localThreadPool.setStopTimeout(0);
                } else {
                    throw new IllegalStateException("Failed to create QueuedThreadPool");
                }
            }

            // Initialize or retrieve the WebSocket client
            WebSocketClient localWebSocketClient = websocketClient;

            if (localWebSocketClient == null) {
                // Create a new WebSocket client if it doesn't exist
                logger.debug("Creating new WebSocketClient with Jetty version {}", jettyVersion);

                try {
                    // Initialize the WebSocket client with the HTTP client, no specific ByteBufferPool (null),
                    // and the configured thread pool
                    // websocketClient = new WebSocketClient(httpClient, null, jettyThreadPool);
                    websocketClient = new WebSocketClient(httpClient);
                    localWebSocketClient = websocketClient;

                    if (localWebSocketClient != null) {
                        // Start the socket monitor thread to maintain the WebSocket connection
                        localWebSocketClient.setExecutor(jettyThreadPool);
                        socketMonitor.start();
                        ret = true;
                    } else {
                        throw new IllegalStateException("WebSocketClient initialization failed");
                    }
                } catch (Exception e) {
                    logger.error("Error creating WebSocketClient: {}", e.getMessage());
                    throw new IllegalStateException("Failed to create WebSocketClient", e);
                }
            } else {
                // WebSocket client already exists
                ret = true;
            }
        } catch (Exception e) {
            logger.error("Error in openWebSocketConnection: {}", e.getMessage());
            ret = false;
        }

        return ret;
    }

    /**
     * Method to initialize the bridge
     */
    @Override
    public void initialize() {
        httpConnectionOK.set(false);

        // load configuration
        FreeAtHomeBridgeHandlerConfiguration locConfig = getConfigAs(FreeAtHomeBridgeHandlerConfiguration.class);

        sendKeepAliveMessage = locConfig.sendKeepAliveMessage;

        ipAddress = locConfig.ipAddress;
        if (ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.ip-address-missing");
            return;
        }

        password = locConfig.password;
        if (password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.password-missing");
            return;
        }

        username = locConfig.username;
        if (username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.username-missing");
            return;
        }

        // build base URL
        baseUrl = "http://" + ipAddress + "/fhapi/v1/api";

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;

            // Open Http connection
            if (!openHttpConnection()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.http-wrongpass-or-ip");

                thingReachable = false;
            }

            // Open the websocket connection for immediate status updates
            if (!openWebSocketConnection()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.not-able-open-websocketconnection");

                thingReachable = false;
            }

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    /**
     * Method to dispose
     */
    @Override
    public void dispose() {
        // let run out the thread
        socketMonitor.interrupt();

        closeWebSocketConnection();

        if (jettyThreadPool != null) {
            try {
                jettyThreadPool.stop();
            } catch (Exception e) {
                logger.error("Error stopping Jetty thread pool: {}", e.getMessage());
            } finally {
                jettyThreadPool = null;
            }
        }
    }

    /**
     * Thread that maintains connection via Websocket.
     */
    private class FreeAtHomeWebsocketMonitorThread extends Thread {

        // initial delay to initiate connection
        private final AtomicInteger reconnectDelay = new AtomicInteger();

        public FreeAtHomeWebsocketMonitorThread() {
        }

        @Override
        public void run() {
            reconnectDelay.set(0);

            try {
                while (!isInterrupted()) {
                    int reconnectCounter = -1;
                    if (httpConnectionOK.get()) {
                        reconnectCounter++;
                        logger.debug("httpConnectionOK {}", httpConnectionOK.get());
                        if (connectSession()) {
                            int aliveCounter = 0;
                            while (isSocketConnectionAlive()) {
                                logger.debug(
                                        "isSocketConnectionAlive is true, aliveCounter {}, reconnectCounter {}, sleeping for {}s",
                                        aliveCounter++, reconnectCounter, BRIDGE_WEBSOCKET_KEEPALIVE);

                                TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_KEEPALIVE);

                                if (sendKeepAliveMessage) {
                                    logger.debug("Sending keep-alive message {}", System.currentTimeMillis());
                                    sendWebsocketKeepAliveMessage("keep-alive");
                                }
                            }
                        }
                        logger.debug("Socket connection closed - isSocketConnectionAlive == false");
                        reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    } else {
                        logger.debug("httpConnectionOK NOT True, attempting to reconnect HTTP");
                        if (reconnectHttp()) {
                            logger.info("HTTP connection re-established");
                        } else {
                            logger.warn("Failed to re-establish HTTP connection, retrying in {} seconds",
                                    BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                            TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("WebSocket monitor thread interrupted", e);
            } catch (IOException e) {
                logger.error("Error in WebSocket communication", e);
            }
        }

        private boolean reconnectHttp() {
            return openHttpConnection();
        }

        private boolean connectSession() throws InterruptedException {
            int delay = reconnectDelay.get();

            if (delay > 0) {
                logger.debug("Delaying (re)connect request by {} seconds.", delay);
                TimeUnit.SECONDS.sleep(delay);
            }

            logger.debug("Server connecting to websocket");

            if (!connectWebsocketSession()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.general-websocket-issue");

                reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);

                return false;
            }

            if (websocketSession == null) {
                lock.lock();
                try {
                    websocketSessionEstablished.await();
                } finally {
                    lock.unlock();
                }
            }

            return true;
        }
    }

    /**
     * Send keep-alive message to SysAp
     */
    public void sendWebsocketKeepAliveMessage(String message) throws IOException {
        Session localSession = websocketSession;

        if (localSession != null) {
            localSession.getRemote().sendString(message);
        }
    }

    /**
     * Get socket alive state
     *
     * @throws InterruptedException
     */
    public boolean isSocketConnectionAlive() throws InterruptedException {
        Session localSession = websocketSession;

        if (localSession == null) {
            logger.error("Socket connection is null");
            return false;
        } else if (!localSession.isOpen()) {
            logger.error("Socket connection is closed");
            return false;
        } else {
            // logger.debug("Socket connection is open");
            return true;
        }

        // return (localSession != null) ? localSession.isOpen() : false;
    }

    /**
     * Socket closed. Report the state
     */
    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        websocketSession = null;
        logger.error("Socket Closed: [ {} ] {}", statusCode, reason);
    }

    /**
     * Socket connected. store the session for later use
     */
    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        Session localSession = session;

        if (localSession != null) {
            websocketSession = localSession;

            localSession.setIdleTimeout(-1);

            logger.debug("Socket Connected - Timeout {} - session: {}", localSession.getIdleTimeout(), session);
        } else {
            logger.debug("Socket Connected - Timeout (invalid) - sesson: (invalid)");
        }

        lock.lock();
        try {
            websocketSessionEstablished.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Error caused. Report the state
     */
    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        websocketSession = null;

        if (cause != null) {
            logger.debug("Socket Error: {}", cause.getLocalizedMessage());
        } else {
            logger.debug("Socket Error: unknown");
        }
    }

    /**
     * Binary message received. It shall not happen with the free@home SysAp
     */
    @Override
    @NonNullByDefault({})
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        logger.debug("Binary message received via websocket");
    }

    /**
     * Text message received. Processing will be started
     */
    @Override
    public void onWebSocketText(@Nullable String message) {
        if (message != null) {
            if (message.toLowerCase(Locale.US).contains("bye")) {
                Session localSession = websocketSession;

                if (localSession != null) {
                    localSession.close(StatusCode.NORMAL, "Thanks");
                }

                logger.debug("Websocket connection closed: {} ", message);
            } else {
                logger.debug("Received websocket text: {} ", message);

                setDatapointOnWebsocketFeedback(message);

                markDeviceRemovedOnWebsocketFeedback(message);
            }
        } else {
            logger.debug("Invalid message string");
        }
    }
}
