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
import java.nio.ByteBuffer;
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

    private String sysapVersion = "";

    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean httpConnectionOK = new AtomicBoolean(false);
    private final Condition websocketSessionEstablished = lock.newCondition();
    private volatile long lastReceivedTime = 0;

    int numberOfComponents = 0;

    private static final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 5; // Seconds
    private static final int BRIDGE_WEBSOCKET_TIMEOUT = 90; // Seconds
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
     * Method to fetch SysApp Version
     */
    public boolean fetchSysapVersion() {
        String url = baseUrl + "/rest/configuration";
        try {
            HttpClient client = httpClient;
            Request req = client.newRequest(url);

            if (req == null) {
                logger.warn("Invalid request object in fetchSysapVersion with the URL [ {} ]", url);
                return false;
            }

            ContentResponse response = req.send();

            if (response.getStatus() != 200) {
                logger.warn("HTTP request failed in fetchSysapVersion with status [{}] and reason [{}]",
                        response.getStatus(), response.getReason());
                return false;
            }

            String configString = new String(response.getContent());

            JsonReader reader = new JsonReader(new StringReader(configString));
            reader.setLenient(true); // Deprecated: use reader.setStrictness(Strictness.LENIENT) in future. Kept for
                                     // backward compatibility with older library versions.
            JsonElement jsonTree = JsonParser.parseReader(reader);

            if (!jsonTree.isJsonObject()) {
                logger.warn("Invalid jsonObject in fetchSysapVersion with the URL [ {} ]", url);
                return false;
            }

            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonObject sysapObject = jsonObject.getAsJsonObject(sysApUID);

            if (sysapObject == null) {
                logger.warn("SysAP object not found in fetchSysapVersion with the URL [ {} ]", url);
                return false;
            }

            JsonObject sysapDetails = sysapObject.getAsJsonObject("sysap");
            if (sysapDetails == null) {
                logger.warn("SysAP details not found in fetchSysapVersion with the URL [ {} ]", url);
                return false;
            }

            JsonElement versionElement = sysapDetails.get("version");
            if (versionElement == null || !versionElement.isJsonPrimitive()) {
                logger.warn("Version not found or invalid in fetchSysapVersion with the URL [ {} ]", url);
                return false;
            }

            sysapVersion = versionElement.getAsString();
            logger.debug("SysAP version fetched: {}", sysapVersion);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Http communication interrupted in fetchSysapVersion: {}", e.getMessage());
            return false;
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Http communication exception in fetchSysapVersion: {}", e.getMessage());
            return false;
        } catch (JsonParseException e) {
            logger.warn("Invalid JSON in fetchSysapVersion: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Unexpected error in fetchSysapVersion: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Method to get the device list
     */
    public List<String> getDeviceDeviceList() throws FreeAtHomeHttpCommunicationException {
        fetchSysapVersion();

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
            reader.setLenient(true); // Deprecated: use reader.setStrictness(Strictness.LENIENT) in future. Kept for
                                     // backward compatibility with older library versions.
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
            reader.setLenient(true); // Deprecated: use reader.setStrictness(Strictness.LENIENT) in future. Kept for
                                     // backward compatibility with older library versions.
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
        reader.setLenient(true); // Deprecated: use reader.setStrictness(Strictness.LENIENT) in future. Kept for
                                 // backward compatibility with older library versions.
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
        reader.setLenient(true); // Deprecated: use reader.setStrictness(Strictness.LENIENT) in future. Kept for
                                 // backward compatibility with older library versions.
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

            if (statusCode == HttpStatus.OK_200) {
                logger.debug("HTTP connection to free@home system established successfullystatus code: {}", statusCode);
                httpConnectionOK.set(true);
                return true;
            } else {
                logger.warn("HTTP connection failed. Status code: {}, Reason: {}", statusCode, response.getReason());
                httpConnectionOK.set(false);
                return false;
            }
        } catch (URISyntaxException e) {
            logger.warn("Invalid URI syntax for base URL: {}", baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("HTTP connection attempt was interrupted", e);
        } catch (TimeoutException e) {
            logger.warn("HTTP connection attempt timed out", e);
        } catch (ExecutionException e) {
            logger.warn("Error executing HTTP request", e);
        } catch (Exception e) {
            logger.warn("Unexpected error while opening HTTP connection", e);
        }

        httpConnectionOK.set(false);
        return false;
    }

    /**
     * Method to connect the WebSocket session.
     * Attempts to establish a WebSocket connection to the SysAP and handles authentication.
     * 
     * @return true if the connection attempt is initiated successfully, false otherwise
     */
    public boolean connectWebsocketSession() {
        boolean ret = false;

        // Create the WebSocket URI using the configured IP address
        URI uri = URI.create("ws://" + ipAddress + "/fhapi/v1/api/ws");

        // Combine username and password for authentication
        String authString = username + ":" + password;

        // Create a Base64 encoder
        Base64.Encoder bas64Encoder = Base64.getEncoder();

        // Encode the authentication string to Base64
        String authStringEnc = bas64Encoder.encodeToString(authString.getBytes());

        // Set the Authorization header value
        authField = "Basic " + authStringEnc;

        WebSocketClient localWebsocketClient = websocketClient;

        try {
            // Start the WebSocket client if it exists
            if (localWebsocketClient != null) {
                localWebsocketClient.setMaxTextMessageBufferSize(8 * 1024);

                // Set the maximum idle timeout for the WebSocket connection.
                // If no activity occurs within this time, the connection will be closed automatically.
                localWebsocketClient.setMaxIdleTimeout(BRIDGE_WEBSOCKET_TIMEOUT * 1000); // milliseconds

                // Set the connection timeout for the WebSocket client.
                // This defines how long the client should wait before considering the connection attempt as failed.
                // Like the idle timeout, the value is converted from seconds to milliseconds.
                localWebsocketClient.setConnectTimeout(BRIDGE_WEBSOCKET_TIMEOUT * 1000); // milliseconds
                localWebsocketClient.start();
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setHeader("Authorization", authField);

                // Set the timeout for the WebSocket upgrade process (i.e., the time allowed for the handshake to
                // complete).
                // Unlike `setConnectTimeout()`, which applies to the lower-level network connection, this timeout
                // applies specifically to the WebSocket upgrade request.
                // The timeout is specified in SECONDS, using `TimeUnit.SECONDS`.
                request.setTimeout(BRIDGE_WEBSOCKET_TIMEOUT, TimeUnit.SECONDS);
                localWebsocketClient.connect(this, uri, request);

                logger.debug("WebSocket connection attempt initiated, timeout: {} seconds", BRIDGE_WEBSOCKET_TIMEOUT);
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
            logger.debug("Current Jetty version: {}", org.eclipse.jetty.util.Jetty.VERSION);

            QueuedThreadPool localThreadPool = jettyThreadPool;

            if (localThreadPool == null) {
                // Create a new thread pool if it doesn't exist
                jettyThreadPool = new QueuedThreadPool();
                localThreadPool = jettyThreadPool;

                if (localThreadPool != null) {
                    localThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
                    localThreadPool.setDaemon(true);
                    localThreadPool.setStopTimeout(0);
                } else {
                    throw new IllegalStateException("Failed to create QueuedThreadPool");
                }
            }

            WebSocketClient localWebSocketClient = websocketClient;

            if (localWebSocketClient == null) {
                // Create a new WebSocket client if it doesn't exist
                logger.debug("Creating new WebSocketClient with Jetty version {}",
                        org.eclipse.jetty.util.Jetty.VERSION);
                localWebSocketClient = new WebSocketClient(httpClient);
                websocketClient = localWebSocketClient;

                if (localWebSocketClient != null) {
                    // Set the executor immediately after creation, before any start
                    localWebSocketClient.setExecutor(localThreadPool);
                    // Do not start the client here; let connectWebsocketSession() handle it, see
                    // localWebsocketClient.start() there
                    socketMonitor.start();
                    ret = true;
                } else {
                    throw new IllegalStateException("WebSocketClient initialization failed");
                }
            } else {
                if (localWebSocketClient.isStarted()) {
                    logger.debug("WebSocketClient is already started, skipping setExecutor()");
                    ret = true; // Client exists and is running, no need to reconfigure
                } else {
                    // Set executor only if the client is not yet started
                    logger.debug("WebSocketClient exists but not started, setting executor");
                    localWebSocketClient.setExecutor(localThreadPool);
                    socketMonitor.start();
                    ret = true;
                }
            }
        } catch (Exception e) {
            logger.warn("Error in openWebSocketConnection: {}", e.getMessage());
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
    @SuppressWarnings("null")
    @Override
    public void dispose() {
        // let run out the thread
        logger.debug("Dispose called, interrupting socket monitor thread");
        socketMonitor.interrupt();

        logger.debug("Closing WebSocket connection");
        closeWebSocketConnection();

        if (jettyThreadPool != null) {
            try {
                logger.debug("Stopping Jetty thread pool");
                jettyThreadPool.stop();
            } catch (Exception e) {
                logger.warn("Error stopping Jetty thread pool: {}", e.getMessage());
            } finally {
                jettyThreadPool = null;
            }
        }
    }

    /**
     * Inner class implementing the WebSocket monitor thread.
     * This thread continuously monitors the WebSocket connection and attempts to reconnect if it fails.
     */
    private class FreeAtHomeWebsocketMonitorThread extends Thread {

        // AtomicInteger to manage the delay (in seconds) before attempting to reconnect the WebSocket.
        // This ensures thread-safe updates to the reconnect delay value across multiple threads.
        private final AtomicInteger reconnectDelay = new AtomicInteger();

        /**
         * Default constructor for the FreeAtHomeWebsocketMonitorThread.
         * Initializes a new instance of the monitor thread without any specific configuration.
         * The reconnectDelay is implicitly initialized to 0 by AtomicInteger's default constructor.
         */
        public FreeAtHomeWebsocketMonitorThread() {
            // No additional initialization required at this point.
            // The reconnectDelay is already set to 0 by default via AtomicInteger.
        }

        /**
         * Main execution method of the monitor thread.
         * Runs a loop that checks the HTTP connection status and manages WebSocket reconnection attempts.
         */
        @Override
        public void run() {
            // Initialize reconnect delay to 0
            reconnectDelay.set(0);
            int reconnectCounter = 0; // Counter for reconnection attempts

            while (!isInterrupted()) {
                try {
                    if (httpConnectionOK.get()) {
                        reconnectCounter++;
                        logger.debug("httpConnectionOK: {}", httpConnectionOK.get());
                        logger.debug("Attempting WebSocket connection, attempt #{}", reconnectCounter);

                        // Attempt to establish the WebSocket connection
                        if (connectSession()) {
                            logger.debug("WebSocket connection established, starting monitoring");
                            int aliveCounter = 0; // Counter for successful alive checks

                            // Inner loop to monitor the active WebSocket connection
                            while (isSocketConnectionAlive()) {
                                logger.debug(
                                        "isSocketConnectionAlive is true, aliveCounter {}, (re)connectCounter {}, sleeping for {}s, SysApp Version {}, jetty {}, lastReceived {}ms ago",
                                        aliveCounter++, reconnectCounter, BRIDGE_WEBSOCKET_KEEPALIVE, sysapVersion,
                                        org.eclipse.jetty.util.Jetty.VERSION,
                                        lastReceivedTime == 0 ? "nothing recieved yet"
                                                : System.currentTimeMillis() - lastReceivedTime);

                                // Sleep for the keep-alive interval to periodically check the connection
                                TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_KEEPALIVE);

                                // Send keep-alive message or ping based on configuration
                                if (sendKeepAliveMessage) {
                                    logger.debug("Sending keep-alive message, System.currentTimeMillis {}ms",
                                            System.currentTimeMillis());
                                    sendWebsocketKeepAliveMessage("keep-alive");
                                } else {
                                    logger.debug("Sending ping message, System.currentTimeMillis {}ms",
                                            System.currentTimeMillis());
                                    sendWebsocketPing();
                                }
                            }
                            logger.debug("Socket connection closed - isSocketConnectionAlive == false");
                        } else {
                            // Log if the connection attempt failed
                            logger.debug("WebSocket connection attempt failed");
                        }

                        // Delay before the next reconnect attempt
                        logger.debug("Delaying (re)connect request by {} seconds", BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    } else {
                        logger.debug("httpConnectionOK NOT True, this should not happen");
                        logger.debug("Retrying in {} seconds", BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    }
                } catch (InterruptedException e) {
                    // Handle thread interruption (e.g., during shutdown)
                    Thread.currentThread().interrupt();
                    logger.debug("WebSocket monitor thread interrupted as expected during shutdown");
                } catch (IOException e) {
                    // Handle IO errors (e.g., from sendWebsocketKeepAliveMessage or sendWebsocketPing)
                    logger.warn("Error in WebSocket communication: {}", e.getMessage());
                    try {
                        // Delay before retrying after an IO error
                        logger.debug("Retrying after IO error in {} seconds", BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.debug("Interrupted during IO error recovery {}", ie.getMessage());
                    }
                }
            }
            // Log when the thread stops
            logger.debug("WebSocket monitor thread stopped");
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

            // Wait for connection to be established or fail with a timeout
            lock.lock();
            try {
                if (websocketSession == null) {
                    boolean established = websocketSessionEstablished.await(BRIDGE_WEBSOCKET_TIMEOUT, TimeUnit.SECONDS);
                    if (!established || websocketSession == null) {
                        logger.trace("WebSocket connection timed out or failed during establishment");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/comm-error.general-websocket-issue");
                        reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        return false;
                    }
                }
            } finally {
                lock.unlock();
            }

            logger.debug("WebSocket session successfully established");
            reconnectDelay.set(0); // Reset delay on successful connection
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
     * Send ping message to SysAp
     */
    public void sendWebsocketPing() throws IOException {
        Session localSession = websocketSession;

        if (localSession != null) {
            localSession.getRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
        }
    }

    /**
     * Get socket alive state
     *
     * @throws InterruptedException
     */
    public boolean isSocketConnectionAlive() {
        Session localSession = websocketSession;

        if (localSession == null) {
            logger.debug("Socket connection is null");
            return false;
        } else if (!localSession.isOpen()) {
            logger.debug("Socket connection is closed");
            return false;
        } else if (lastReceivedTime != 0) {
            long timeSinceLastReceived = System.currentTimeMillis() - lastReceivedTime;
            if (timeSinceLastReceived > BRIDGE_WEBSOCKET_TIMEOUT * 1000) {
                logger.warn("No data received for {} ms, assuming connection is dead", timeSinceLastReceived);
                localSession.close(StatusCode.ABNORMAL, "No data received");
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Socket closed. Report the state
     */
    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        websocketSession = null;
        lastReceivedTime = 0;
        logger.warn("Socket Closed: [ {} ] {}", statusCode, reason);
    }

    /**
     * Socket connected. store the session for later use
     */
    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        Session localSession = session;

        if (localSession != null) {
            websocketSession = localSession;
            localSession.setIdleTimeout(2 * BRIDGE_WEBSOCKET_KEEPALIVE * 1000); // Unit is milliseconds,
                                                                                // sendWebsocketPing() and
                                                                                // sendWebsocketKeepAliveMessage() are
                                                                                // called every
                                                                                // BRIDGE_WEBSOCKET_KEEPALIVE, so the
                                                                                // timeout should be at least 2 times
                                                                                // that
            logger.debug("WebSocket connection to SysAP established successfully, timeout: {} ms",
                    localSession.getIdleTimeout());

            // Fetch the SysAP version after a successful connection establishment
            if (fetchSysapVersion()) {
                logger.debug("SysAP version fetched successfully after reconnect: {}", sysapVersion);
                updateStatus(ThingStatus.ONLINE); // Set the status to ONLINE
            } else {
                logger.warn("Failed to fetch SysAP version after successful reconnect");
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/comm-error.fetch-version-error");
            }

            lastReceivedTime = System.currentTimeMillis();
        } else {
            logger.debug("Socket Connected - Timeout (invalid) - session: (invalid)");
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
        Session localSession = websocketSession;

        // Log the error with details if available
        if (cause != null) {
            logger.warn("WebSocket error occurred: {}", cause.getLocalizedMessage());
        } else {
            logger.warn("WebSocket error occurred: unknown cause");
        }

        // Check and close the session if it is still open
        if (localSession != null && localSession.isOpen()) {
            try {
                localSession.close(StatusCode.ABNORMAL, "Closed due to error");
                logger.debug("WebSocket session closed due to error");
            } catch (Exception e) {
                logger.warn("Failed to close WebSocket session: {}", e.getMessage());
            }
        }

        // Set the session to null to indicate it is no longer valid
        websocketSession = null;

        // Update the thing status to OFFLINE with error details
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "WebSocket connection failed: " + (cause != null ? cause.getMessage() : "unknown error"));
    }

    /**
     * Binary message received. It shall not happen with the free@home SysAp
     */
    @Override
    @NonNullByDefault({})
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        logger.warn("Binary message received via websocket - It shall not happen with the free@home SysAp");
    }

    /**
     * Text message received. Processing will be started
     */
    @Override
    public void onWebSocketText(@Nullable String message) {
        if (message != null) {
            lastReceivedTime = System.currentTimeMillis();
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
