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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.net.util.Base64;
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
 *
 */

@NonNullByDefault
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    public @Nullable static FreeAtHomeBridgeHandler freeAtHomeSystemHandler = null;

    public ChannelUpdateHandler channelUpdateHandler = new ChannelUpdateHandler();

    // Clients for the network communication
    private HttpClient httpClient = new HttpClient();
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

    private final int BRIDGE_WEBSOCKET_RECONNECT_DELAY = 30;

    public FreeAtHomeBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.warn("Unknown handle command for the bridge - channellUID {}, command {}", channelUID, command);
    }

    public String getDatapoint(@Nullable String deviceId, @Nullable String channel, @Nullable String datapoint) {

        if ((null == deviceId) || (null == channel) || (null == datapoint)) {
            return new String("0");
        }

        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {
            Request req = httpClient.newRequest(url);
            if (null != req) {
                ContentResponse response = req.send();

                String deviceString = new String(response.getContent());

                JsonReader reader = new JsonReader(new StringReader(deviceString));

                reader.setLenient(true);

                JsonParser parser = new JsonParser();

                JsonElement jsonTree = parser.parse(reader);

                // check the output
                if (null != jsonTree) {
                    if (jsonTree.isJsonObject()) {
                        JsonObject jsonObject = jsonTree.getAsJsonObject();

                        jsonObject = jsonObject.getAsJsonObject(sysApUID);
                        JsonArray jsonValueArray = jsonObject.getAsJsonArray("values");

                        JsonElement element = jsonValueArray.get(0);
                        String value = element.getAsString();

                        return value;
                    }
                }
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by getDatapoint [{}]", e.getMessage());
            return new String("0");
        }

        return new String("0");
    }

    public boolean setDatapoint(@Nullable String deviceId, @Nullable String channel, @Nullable String datapoint,
            String valueString) {

        if ((null == deviceId) || (null == channel) || (null == datapoint)) {
            return false;
        }

        String url = baseUrl + "/rest/datapoint/" + sysApUID + "/" + deviceId + "." + channel + "." + datapoint;

        try {
            Request req = httpClient.newRequest(url);
            req.content(new StringContentProvider(valueString));
            req.method(HttpMethod.PUT);
            req.send();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Communication error by getDatapoint [{}]", e.getMessage());
        }

        return true;
    }

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
            }
        }
    }

    public void closeHttpConnection() {
        try {
            httpClient.stop();
        } catch (Exception e1) {
            logger.error("Error by closing Websocket connection [{}]", e1.getMessage());
        }
    }

    public boolean openHttpConnection() {
        boolean ret = false;

        // // Instantiate HttpClient.
        // if (null == httpClient) {
        // httpClient = new HttpClient();
        // }
        //
        // // Check the http client creation and configure it
        // if (null == httpClient) {
        //
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Cannot cannot create http client");
        //
        // logger.error("Cannot create internal httpclient");
        //
        // return false;
        // } else {
        // // Configure HttpClient
        // httpClient.setFollowRedirects(false);
        // }

        httpClient.setFollowRedirects(false);

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

            ret = true;

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot cannot create http client");

            logger.error("Cannot start http connection - {}", e.getMessage());

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
                req.send();

                httpConnectionOK.set(true);

                ret = true;

            } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot open http connection, wrong passord");

                logger.error("Cannot open http connection {}", e.getMessage());
            }
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
        boolean ret = false;

        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);

        authField = "Basic " + authStringEnc;

        if (null == jettyThreadPool) {
            jettyThreadPool = new QueuedThreadPool();
            jettyThreadPool.setName(FreeAtHomeBridgeHandler.class.getSimpleName());
            jettyThreadPool.setDaemon(true);
            jettyThreadPool.setStopTimeout(0);
        }

        if (null == websocketClient) {
            websocketClient = new WebSocketClient();
        }

        websocketClient.setExecutor(jettyThreadPool);

        if (null == socket) {
            socket = new EventSocket();
        }

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
                ret = false;
            }
        } else {
            websocketClient = null;
            ret = false;
        }

        return ret;
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

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

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot open http connection");

            logger.error("Cannot open http connection");

            return;
        }

        // // Create channel update handler
        // channelUpdateHandler = new ChannelUpdateHandler();
        //
        // if (null == channelUpdateHandler) {
        //
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
        // "Cannot create internal structure");
        //
        // logger.error("Cannot create internal structure - ChannelUpdateHandler");
        //
        // return;
        // }

        // Open the websocket connection for immediate status updates
        if (false == openWebSocketConnection()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open websocket connection");

            logger.error("Cannot open websocket connection");

            return;
        }

        // Background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                freeAtHomeSystemHandler = this;
                updateStatus(ThingStatus.ONLINE);

                // try {
                // if (null != socket) {
                // socket.getLatch().await();
                // }
                // } catch (InterruptedException e) {
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No online updates");
                // }
            } else {
                freeAtHomeSystemHandler = this;
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
    }

    public FreeAtHomeDeviceList getDeviceDeviceList() {
        FreeAtHomeSysApDeviceList deviceList = new FreeAtHomeSysApDeviceList(httpClient, ipAddress, sysApUID);
        // FreeAtHomeTestDeviceList deviceList = new FreeAtHomeTestDeviceList(
        // "/Users/andras/Development/openhab-main/testfiles/responsedevice.json",
        // "/Users/andras/Development/openhab-main/testfiles/response_formatted.json", sysApUID);

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
                while (!isInterrupted()) {
                    if (true == httpConnectionOK.get()) {
                        if (connectSession()) {
                            socket.awaitEndCommunication();
                        }
                    } else {
                        TimeUnit.SECONDS.sleep(BRIDGE_WEBSOCKET_RECONNECT_DELAY);
                    }
                }
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

    ChannelUpdateHandler getChannelUpdateHandler() {
        return channelUpdateHandler;
    }
}
