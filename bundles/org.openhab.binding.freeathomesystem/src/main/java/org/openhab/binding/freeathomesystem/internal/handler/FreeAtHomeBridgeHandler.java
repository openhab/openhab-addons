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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Iterator;
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
import org.openhab.binding.freeathomesystem.internal.Configuration.FreeAtHomeBridgeHandlerConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * The {@link FreeAtHomeBridgeHandler} is responsible for handling the free@home bridge and
 * its main communication.
 *
 * @author Andras Uhrin - Initial contribution
 * @param <FreeAtHomeSystemHttpQueueResponse>
 *
 */

@NonNullByDefault
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    public @Nullable static FreeAtHomeBridgeHandler freeAtHomeSystemHandler = null;

    public ChannelUpdateHandler channelUpdateHandler = new ChannelUpdateHandler();

    // Clients for the network communication
    private HttpClient httpClient;
    private @Nullable WebSocketClient websocketClient = null;
    private @Nullable EventSocket socket = null;
    private @Nullable FreeAtHomeWebsocketMonitorThread socketMonitor = null;
    private @Nullable QueuedThreadPool jettyThreadPool = null;

    private String sysApUID = "00000000-0000-0000-0000-000000000000";
    private String ipAddress = "192.168.1.1";
    private String username = "";
    private String password = "";

    private String baseUrl = "";

    private String authField = "";

    private AtomicBoolean httpConnectionOK = new AtomicBoolean(false);

    int numberOfComponents = 0;

    private int requestCounter = 0;

    private final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 30;

    public FreeAtHomeBridgeHandler(Bridge thing, HttpClient client) {
        super(thing);

        httpClient = client;
    }

    /**
     * stub method for handlCommand
     *
     * @author Andras Uhrin
     *
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.warn("Unknown handle command for the bridge - channellUID {}, command {}", channelUID, command);
    }

    /**
     * Method to get datapoint for things
     *
     * @author Andras Uhrin
     *
     */
    @SuppressWarnings("deprecation")
    public String getDatapoint(@Nullable String deviceId, @Nullable String channel, @Nullable String datapoint) {

        if ((null == deviceId) || (null == channel) || (null == datapoint)) {
            return new String("0");
        }

        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {
            Request req = httpClient.newRequest(url);

            logger.debug("Get datapoint by url: {}", url);

            requestCounter++;

            if (null != req) {
                ContentResponse response = req.send();

                String deviceString = new String(response.getContent());

                JsonReader reader = new JsonReader(new StringReader(deviceString));

                reader.setLenient(true);

                JsonParser parser = new JsonParser();

                JsonElement jsonTree = parser.parse(reader);

                logger.debug("Communication result [{}]", response.getStatus());

                if (response.getStatus() != 200) {
                    logger.error("Communication error by getDatapoint [{}]", response.getStatus());
                }

                // check the output
                if (null != jsonTree) {
                    if (jsonTree.isJsonObject()) {
                        JsonObject jsonObject = jsonTree.getAsJsonObject();

                        jsonObject = jsonObject.getAsJsonObject(sysApUID);
                        JsonArray jsonValueArray = jsonObject.getAsJsonArray("values");

                        JsonElement element = jsonValueArray.get(0);
                        String value = element.getAsString();

                        if (0 == value.length()) {
                            value = "0";
                        }

                        return value;
                    }
                }
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by getDatapoint [{}]", e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

            return new String("0");
        }

        return new String("0");
    }

    /**
     * Method to set datapoint from things
     *
     * @author Andras Uhrin
     *
     */
    public boolean setDatapoint(@Nullable String deviceId, @Nullable String channel, @Nullable String datapoint,
            String valueString) {

        if ((null == deviceId) || (null == channel) || (null == datapoint)) {
            return false;
        }

        requestCounter++;

        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;
        try {
            Request req = httpClient.newRequest(url);

            req.content(new StringContentProvider(valueString));
            req.method(HttpMethod.PUT);

            logger.debug("Set datapoint by url: {} value: {}", url, valueString);

            ContentResponse response = req.send();

            logger.debug("Communication result [{}]", response.getStatus());

            if (response.getStatus() != 200) {
                logger.error("Communication error by setDatapoint [{}]", response.getStatus());

                restartHttpConnection();
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by setDatapoint [{}]", e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return true;
    }

    /**
     * Method to process socket events
     *
     * @author Andras Uhrin
     *
     */
    @SuppressWarnings("deprecation")
    public void processSocketEvent(String receivedText) {

        JsonReader reader = new JsonReader(new StringReader(receivedText));

        reader.setLenient(true);

        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(reader);

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
     *
     * @author Andras Uhrin
     *
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

            logger.info("Start http client");

            ret = true;

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client");

            logger.error("Cannot start http client - {}", ex.getMessage());

            ret = false;
        }

        if (true == ret) {
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
                if (200 == res.getStatus()) {

                    // response OK
                    httpConnectionOK.set(true);

                    ret = true;

                    logger.info("HTTP connection to SysAP is OK");
                } else {

                    // response NOK, set error
                    httpConnectionOK.set(false);

                    ret = false;

                    logger.warn("Wrong credentials for SysAP");
                }

            } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot open http connection, wrong passord");

                logger.error("Cannot open http connection {}", ex.getMessage());

                ret = false;
            }
        }

        return ret;
    }

    /**
     * Method to close Http connection
     *
     * @author Andras Uhrin
     *
     */
    public boolean closeHttpConnection() {
        boolean ret = false;

        try {
            // Start HttpClient.
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

            logger.info("Stop http client");

            ret = true;

        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot stop http client");

            logger.error("Cannot stop http client - {}", ex.getMessage());

            ret = false;
        }

        return ret;
    }

    /**
     * Method to restart Http connection
     *
     * @author Andras Uhrin
     *
     */
    public boolean restartHttpConnection() {
        boolean ret = false;

        ret = closeHttpConnection();

        if (true == ret) {
            ret = openHttpConnection();
        }

        return ret;
    }

    /**
     * Method to connect the websocket session
     *
     * @author Andras Uhrin
     *
     */
    @SuppressWarnings("null")
    public boolean connectWebsocketSession() {
        boolean ret = false;

        URI uri = URI.create("ws://" + ipAddress + "/fhapi/v1/api/ws");

        try {
            // Start socket client
            if ((null != websocketClient) && (null != socket)) {
                websocketClient.start();
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setHeader("Authorization", authField);
                websocketClient.connect(socket, uri, request);

                logger.debug("Websocket connection to SysAP is OK");

                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            logger.error("Error by opening Websocket connection [{}]", e.getMessage());

            if (null != websocketClient) {
                try {
                    websocketClient.stop();

                    ret = false;
                } catch (Exception e1) {
                    logger.error("Error by opening Websocket connection [{}]", e1.getMessage());

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
     *
     * @author Andras Uhrin
     *
     */
    @SuppressWarnings({ "deprecation", "null" })
    public void closeWebSocketConnection() {

        if (null != socketMonitor) {
            socketMonitor.stop();
        }

        if (null != jettyThreadPool) {
            try {
                jettyThreadPool.stop();
            } catch (Exception e1) {
                logger.error("Error by closing Websocket connection [{}]", e1.getMessage());
            }
        }

        if (null != websocketClient) {
            try {
                websocketClient.stop();
            } catch (Exception e2) {
                logger.error("Error by closing Websocket connection [{}]", e2.getMessage());
            }
        }
    }

    /**
     * Method to open the websocket connection
     *
     * @author Andras Uhrin
     *
     */
    @SuppressWarnings("null")
    public boolean openWebSocketConnection() {
        boolean ret = true;

        String authString = username + ":" + password;

        // create base64 encoder
        Base64.Encoder bas64Encoder = Base64.getEncoder();

        // Encoding string using encoder object
        String authStringEnc = bas64Encoder.encodeToString(authString.getBytes());

        authField = "Basic " + authStringEnc;

        if (null == jettyThreadPool) {
            jettyThreadPool = new QueuedThreadPool();
            jettyThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
            jettyThreadPool.setDaemon(true);
            jettyThreadPool.setStopTimeout(0);
        }

        if (null == websocketClient) {
            websocketClient = new WebSocketClient();

            if (null != websocketClient) {
                websocketClient.setExecutor(jettyThreadPool);
            }
        }

        if (null == socket) {
            socket = new EventSocket();

            if (null != socket) {
                // set bridge for the socket event handler
                socket.setBridge(this);

                if (null == socketMonitor) {
                    socketMonitor = new FreeAtHomeWebsocketMonitorThread();
                    socketMonitor.start();

                    ret = true;
                } else {
                    websocketClient = null;
                    socket = null;
                    jettyThreadPool = null;
                    ret = false;

                    logger.error("Cannot open http connection - socketmonitor");
                }
            }
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
     * @author Andras Uhrin
     *
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        httpConnectionOK.set(false);

        // load configuration
        FreeAtHomeBridgeHandlerConfiguration locConfig = getConfigAs(FreeAtHomeBridgeHandlerConfiguration.class);

        ipAddress = locConfig.ipaddress;
        password = locConfig.password;
        username = locConfig.username;

        // build base URL
        baseUrl = "http://" + ipAddress + "/fhapi/v1/api";

        // Open Http connection
        if (false == openHttpConnection()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open http connection, wrong password or IP address");

            logger.error("Cannot open http connection");

            return;
        }

        // Open the websocket connection for immediate status updates
        if (false == openWebSocketConnection()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open websocket connection");

            logger.error("Cannot open websocket connection");

            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Method to dispose
     *
     * @author Andras Uhrin
     *
     */
    @Override
    public void dispose() {
    }

    /**
     * Method to get the device list
     *
     * @author Andras Uhrin
     *
     */
    public FreeAtHomeDeviceList getDeviceDeviceList() {
        FreeAtHomeSysApDeviceList deviceList = new FreeAtHomeSysApDeviceList(httpClient, ipAddress, sysApUID);
        deviceList.buildComponentList();

        return deviceList;
    }

    /**
     * Thread that maintains connection via Websocket.
     *
     * @author Andras Uhrin
     *
     */
    private class FreeAtHomeWebsocketMonitorThread extends Thread {

        // initial delay to initiate connection
        private AtomicInteger reconnectDelay = new AtomicInteger();

        public FreeAtHomeWebsocketMonitorThread() {
        }

        @Override
        @SuppressWarnings("null")
        public void run() {
            // set initial connect delay to 0
            reconnectDelay.set(0);

            try {
                // while (!isInterrupted()) {
                if (true == httpConnectionOK.get()) {
                    if (connectSession()) {
                        socket.awaitEndCommunication();
                    }
                } else {
                    TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                }
                // }
            } catch (InterruptedException e) {
                // logger.debug("Thread interrupted [{}]", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Problem in websocket connection");
            }
        }

        private boolean connectSession() throws InterruptedException {
            int delay = reconnectDelay.get();

            if (delay > 0) {
                logger.debug("Delaying connect request by {} seconds.", reconnectDelay);
                TimeUnit.SECONDS.sleep(delay);
            }

            logger.debug("Server connecting to websocket");

            if (!connectWebsocketSession()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Problem in websocket connection");

                reconnectDelay.set(BRIDGE_WEBSOCKET_RECONNECT_DELAY);

                return false;
            }

            return true;
        }
    }
}
