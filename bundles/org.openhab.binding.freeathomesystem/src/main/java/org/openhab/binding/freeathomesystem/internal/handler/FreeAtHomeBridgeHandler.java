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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemDiscoveryService;
import org.openhab.binding.freeathomesystem.internal.configuration.FreeAtHomeBridgeHandlerConfiguration;
import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathomesystem.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
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
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    public ChannelUpdateHandler channelUpdateHandler = new ChannelUpdateHandler();

    // Clients for the network communication
    private HttpClient httpClient;
    private EventSocket socket = new EventSocket();
    private @Nullable WebSocketClient websocketClient = null;
    private FreeAtHomeWebsocketMonitorThread socketMonitor = new FreeAtHomeWebsocketMonitorThread();
    private @Nullable QueuedThreadPool jettyThreadPool = null;

    private String sysApUID = "00000000-0000-0000-0000-000000000000";
    private String ipAddress = "";
    private String username = "";
    private String password = "";

    private String baseUrl = "";

    private String authField = "";

    private AtomicBoolean httpConnectionOK = new AtomicBoolean(false);

    int numberOfComponents = 0;

    private static final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 60;
    private static final int BRIDGE_WEBSOCKET_TIMEOUT = 90;
    private static final int BRIDGE_WEBSOCKET_KEEPALIVE = 50;

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
        return List.of(FreeAtHomeSystemDiscoveryService.class);
    }

    /**
     * Method to get the device list
     */
    public List<String> getDeviceDeviceList() throws FreeAtHomeHttpCommunicationException {
        List<String> listOfComponentId = new ArrayList<String>();
        boolean ret = false;

        listOfComponentId.clear();

        String url = baseUrl + "/rest/devicelist";

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
        if (ret == false) {
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication timout or execution interrupted [ " + e.getMessage() + " ]");
        } catch (JsonParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        } catch (ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            restartHttpConnection();

            throw new FreeAtHomeHttpCommunicationException(0,
                    "Http communication interrupted [ " + e.getMessage() + " ]");
        }

        return true;
    }

    /**
     * Obtait the valid channel update handler
     */
    public ChannelUpdateHandler getChannelUpdateHandler() {
        return channelUpdateHandler;
    }

    /**
     * Method to process socket events
     */
    public void processSocketEvent(String receivedText) {
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

                channelUpdateHandler.updateChannelByDatapointEvent(eventDatapointID, value);

                logger.debug("Socket event processed: event-datapoint-ID {} value {}", eventDatapointID, value);
            }
        }
    }

    /**
     * Method to open Http connection
     */
    public boolean openHttpConnection() {
        boolean ret = false;

        // Configure client
        httpClient.setFollowRedirects(false);
        httpClient.setMaxConnectionsPerDestination(1);
        httpClient.setMaxRequestsQueuedPerDestination(50);

        // Set timeouts
        httpClient.setIdleTimeout(-1);
        httpClient.setConnectTimeout(5000);

        try {
            // Start HttpClient.
            switch (httpClient.getState()) {
                case AbstractLifeCycle.FAILED:
                case AbstractLifeCycle.STOPPED: {
                    httpClient.start();
                    break;
                }
                case AbstractLifeCycle.STARTING:
                case AbstractLifeCycle.STARTED:
                case AbstractLifeCycle.STOPPING: {
                    // nothing to do
                    break;
                }
            }

            logger.debug("Start http client");

            ret = true;
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client");

            logger.debug("Cannot start http client - {}", ex.getMessage());

            ret = false;
        }

        if (ret) {
            // Add authentication credentials and make a check.
            try {
                // Add authentication credentials.
                AuthenticationStore auth = httpClient.getAuthenticationStore();

                URI uri1 = new URI("http://" + ipAddress + "/fhapi/v1");
                auth.addAuthenticationResult(new BasicAuthentication.BasicResult(uri1, username, password));

                String url = "http://" + ipAddress + "/fhapi/v1/api/rest/devicelist";

                Request req = httpClient.newRequest(url);
                ContentResponse res = req.send();

                // check status
                if (res.getStatus() == 200) {
                    // response OK
                    httpConnectionOK.set(true);

                    ret = true;

                    logger.debug("HTTP connection to SysAP is OK");
                } else {
                    // response NOK, set error
                    httpConnectionOK.set(false);

                    ret = false;

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/comm-error.wrong-credentials");
                }
            } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot open http connection, wrong passord");

                logger.debug("Cannot open http connection {}", ex.getMessage());

                ret = false;
            }
        }

        return ret;
    }

    /**
     * Method to close Http connection
     */
    public boolean closeHttpConnection() {
        boolean ret = false;

        try {
            // Stop HttpClient.
            switch (httpClient.getState()) {
                case AbstractLifeCycle.FAILED:
                case AbstractLifeCycle.STOPPING:
                case AbstractLifeCycle.STOPPED: {
                    break;
                }
                case AbstractLifeCycle.STARTING:
                case AbstractLifeCycle.STARTED: {
                    httpClient.stop();
                    break;
                }
            }

            logger.debug("Stop http client");

            ret = true;
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot stop http client");

            logger.debug("Cannot stop http client - {}", ex.getMessage());

            ret = false;
        }

        return ret;
    }

    /**
     * Method to restart Http connection
     */
    public boolean restartHttpConnection() {
        boolean ret = false;

        ret = closeHttpConnection();

        if (ret) {
            ret = openHttpConnection();
        }

        return ret;
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
                localWebsocketClient.connect(socket, uri, request);

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
        }

        WebSocketClient localWebSocketClient = websocketClient;

        if (localWebSocketClient != null) {
            try {
                localWebSocketClient.stop();
            } catch (Exception e2) {
                logger.debug("Error by closing Websocket connection [{}]", e2.getMessage());
            }
        }
    }

    /**
     * Method to open the websocket connection
     */
    public boolean openWebSocketConnection() {
        boolean ret = false;

        QueuedThreadPool localThreadPool = jettyThreadPool;

        if (localThreadPool == null) {
            jettyThreadPool = new QueuedThreadPool();

            localThreadPool = jettyThreadPool;

            if (localThreadPool != null) {
                localThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
                localThreadPool.setDaemon(true);
                localThreadPool.setStopTimeout(0);

                ret = true;
            }
        }

        WebSocketClient localWebSocketClient = websocketClient;

        if (localWebSocketClient == null) {
            websocketClient = new WebSocketClient(httpClient);

            localWebSocketClient = websocketClient;

            if (localWebSocketClient != null) {
                localWebSocketClient.setExecutor(jettyThreadPool);

                ret = true;
            } else {
                ret = false;
            }
        } else {
            ret = true;
        }

        // set bridge for the socket event handler
        if (ret == true) {
            socket.setBridge(this);

            socketMonitor.start();
        }

        return ret;
    }

    /**
     * Method to re-initialize the bridge
     *
     * @author Andras Uhrin
     *
     */
    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    /**
     * Method to initialize the bridge
     *
     *
     */
    @Override
    public void initialize() {
        scheduler.execute(() -> {
            boolean thingReachable = true;

            httpConnectionOK.set(false);

            // load configuration
            FreeAtHomeBridgeHandlerConfiguration locConfig = getConfigAs(FreeAtHomeBridgeHandlerConfiguration.class);

            ipAddress = locConfig.ipAddress;
            password = locConfig.password;
            username = locConfig.username;

            // build base URL
            baseUrl = "http://" + ipAddress + "/fhapi/v1/api";

            // Open Http connection
            if (!openHttpConnection()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.http-wrongpass-or-ip");

                logger.debug("Cannot open http connection");

                thingReachable = false;
            }

            // Open the websocket connection for immediate status updates
            if (!openWebSocketConnection()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.not-able-open-websocketconnection");

                logger.debug("Cannot open websocket connection");

                thingReachable = false;
            }

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);

        // load configuration
        FreeAtHomeBridgeHandlerConfiguration locConfig = getConfigAs(FreeAtHomeBridgeHandlerConfiguration.class);

        ipAddress = locConfig.ipAddress;
        password = locConfig.password;
        username = locConfig.username;
    }

    /**
     * Method to dispose
     */
    @Override
    public void dispose() {
        // let run out the thread
        socketMonitor.interrupt();
    }

    /**
     * Thread that maintains connection via Websocket.
     */
    private class FreeAtHomeWebsocketMonitorThread extends Thread {

        // initial delay to initiate connection
        private AtomicInteger reconnectDelay = new AtomicInteger();

        public FreeAtHomeWebsocketMonitorThread() {
        }

        @Override
        public void run() {
            // set initial connect delay to 0
            reconnectDelay.set(0);

            try {
                while (!isInterrupted()) {
                    if (httpConnectionOK.get()) {
                        if (connectSession()) {
                            while (!socket.isSocketClosed()) {
                                TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_KEEPALIVE);

                                logger.debug("Sending keep-alive message {}", System.currentTimeMillis());
                                socket.sendKeepAliveMessage("keep-alive");
                            }

                            logger.debug("Socket connection closed");
                            reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                        }
                    } else {
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("Thread interrupted [{}]", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.general-websocket-issue");
            } catch (IOException e) {
                logger.debug("Keep-alive not succesfull [{}]", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error.websocket-keep-alive-error");
            }
        }

        private boolean connectSession() throws InterruptedException {
            int delay = reconnectDelay.get();

            if (delay > 0) {
                logger.debug("Delaying (re)connect request by {} seconds.", reconnectDelay);
                TimeUnit.SECONDS.sleep(delay);
            }

            logger.debug("Server connecting to websocket");

            socket.resetEventSocket();

            if (!connectWebsocketSession()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Problem in websocket connection");

                logger.debug("Problem in websocket connection, trying to reconnect");

                reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);

                return false;
            }

            return true;
        }
    }
}
