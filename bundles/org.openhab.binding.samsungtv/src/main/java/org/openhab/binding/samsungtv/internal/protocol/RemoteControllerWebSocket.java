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
package org.openhab.binding.samsungtv.internal.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RemoteControllerWebSocket} is responsible for sending key codes to the
 * Samsung TV via the websocket protocol (for newer TV's).
 *
 * @author Arjan Mels - Initial contribution
 * @author Arjan Mels - Moved websocket inner classes to standalone classes
 */
@NonNullByDefault
public class RemoteControllerWebSocket extends RemoteController implements Listener {

    private final Logger logger = LoggerFactory.getLogger(RemoteControllerWebSocket.class);

    private static final String WS_ENDPOINT_REMOTE_CONTROL = "/api/v2/channels/samsung.remote.control";
    private static final String WS_ENDPOINT_ART = "/api/v2/channels/com.samsung.art-app";
    private static final String WS_ENDPOINT_V2 = "/api/v2";

    // WebSocket helper classes
    private final WebSocketRemote webSocketRemote;
    private final WebSocketArt webSocketArt;
    private final WebSocketV2 webSocketV2;

    // JSON parser class. Also used by WebSocket handlers.
    final Gson gson = new Gson();

    // Callback class. Also used by WebSocket handlers.
    final RemoteControllerWebsocketCallback callback;

    // Websocket client class shared by WebSocket handlers.
    final WebSocketClient client;

    // temporary storage for source app. Will be used as value for the sourceApp channel when information is complete.
    // Also used by Websocket handlers.
    @Nullable
    String currentSourceApp = null;

    // last app in the apps list: used to detect when status information is complete in WebSocketV2.
    @Nullable
    String lastApp = null;

    // timeout for status information search
    private static final long UPDATE_CURRENT_APP_TIMEOUT = 5000;
    private long previousUpdateCurrentApp = 0;

    // UUID used for data exchange via websockets
    final UUID uuid = UUID.randomUUID();

    // Description of Apps
    @NonNullByDefault()
    class App {
        String appId;
        String name;
        int type;

        App(String appId, String name, int type) {
            this.appId = appId;
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    // Map of all available apps
    Map<String, App> apps = new LinkedHashMap<>();

    /**
     * Create and initialize remote controller instance.
     *
     * @param host Host name of the Samsung TV.
     * @param port TCP port of the remote controller protocol.
     * @param appName Application name used to send key codes.
     * @param uniqueId Unique Id used to send key codes.
     * @param remoteControllerWebsocketCallback callback
     * @throws RemoteControllerException
     */
    public RemoteControllerWebSocket(String host, int port, String appName, String uniqueId,
            RemoteControllerWebsocketCallback remoteControllerWebsocketCallback) throws RemoteControllerException {
        super(host, port, appName, uniqueId);

        this.callback = remoteControllerWebsocketCallback;

        WebSocketFactory webSocketFactory = remoteControllerWebsocketCallback.getWebSocketFactory();
        if (webSocketFactory == null) {
            throw new RemoteControllerException("No WebSocketFactory available");
        }

        client = webSocketFactory.createWebSocketClient("samsungtv");

        client.addLifeCycleListener(this);

        webSocketRemote = new WebSocketRemote(this);
        webSocketArt = new WebSocketArt(this);
        webSocketV2 = new WebSocketV2(this);
    }

    @Override
    public boolean isConnected() {
        return webSocketRemote.isConnected();
    }

    @Override
    public void openConnection() throws RemoteControllerException {
        logger.trace("openConnection()");

        if (!(client.isStarted() || client.isStarting())) {
            logger.debug("RemoteControllerWebSocket start Client");
            try {
                client.start();
                client.setMaxBinaryMessageBufferSize(1000000);
                // websocket connect will be done in lifetime handler
                return;
            } catch (Exception e) {
                logger.warn("Cannot connect to websocket remote control interface: {}", e.getMessage(), e);
                throw new RemoteControllerException(e);
            }
        }
        connectWebSockets();
    }

    private void connectWebSockets() {
        logger.trace("connectWebSockets()");

        String encodedAppName = Base64.getUrlEncoder().encodeToString(appName.getBytes());

        String protocol;

        if (SamsungTvConfiguration.PROTOCOL_SECUREWEBSOCKET
                .equals(callback.getConfig(SamsungTvConfiguration.PROTOCOL))) {
            protocol = "wss";
        } else {
            protocol = "ws";
        }

        try {
            String token = (String) callback.getConfig(SamsungTvConfiguration.WEBSOCKET_TOKEN);
            webSocketRemote.connect(new URI(protocol, null, host, port, WS_ENDPOINT_REMOTE_CONTROL,
                    "name=" + encodedAppName + (StringUtil.isNotBlank(token) ? "&token=" + token : ""), null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logger.warn("Problem connecting to remote websocket", e);
        }

        try {
            webSocketArt.connect(new URI(protocol, null, host, port, WS_ENDPOINT_ART, "name=" + encodedAppName, null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logger.warn("Problem connecting to artmode websocket", e);
        }

        try {
            webSocketV2.connect(new URI(protocol, null, host, port, WS_ENDPOINT_V2, "name=" + encodedAppName, null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logger.warn("Problem connecting to V2 websocket", e);
        }
    }

    private void closeConnection() throws RemoteControllerException {
        logger.debug("RemoteControllerWebSocket closeConnection");

        try {
            webSocketRemote.close();
            webSocketArt.close();
            webSocketV2.close();
            client.stop();
        } catch (Exception e) {
            throw new RemoteControllerException(e);
        }
    }

    @Override
    public void close() throws RemoteControllerException {
        logger.debug("RemoteControllerWebSocket close");
        closeConnection();
    }

    /**
     * Retrieve app status for all apps. In the WebSocketv2 handler the currently running app will be determined
     */
    void updateCurrentApp() {
        if (webSocketV2.isNotConnected()) {
            logger.warn("Cannot retrieve current app webSocketV2 is not connected");
            return;
        }

        // update still running and not timed out
        if (lastApp != null && System.currentTimeMillis() < previousUpdateCurrentApp + UPDATE_CURRENT_APP_TIMEOUT) {
            return;
        }

        lastApp = null;
        previousUpdateCurrentApp = System.currentTimeMillis();

        currentSourceApp = null;

        // retrieve last app (don't merge with next loop as this might run asynchronously
        for (App app : apps.values()) {
            lastApp = app.appId;
        }

        for (App app : apps.values()) {
            webSocketV2.getAppStatus(app.appId);
        }
    }

    /**
     * Send key code to Samsung TV.
     *
     * @param key Key code to send.
     * @throws RemoteControllerException
     */
    @Override
    public void sendKey(KeyCode key) throws RemoteControllerException {
        sendKey(key, false);
    }

    public void sendKeyPress(KeyCode key) throws RemoteControllerException {
        sendKey(key, true);
    }

    public void sendKey(KeyCode key, boolean press) throws RemoteControllerException {
        logger.debug("Try to send command: {}", key);

        if (!isConnected()) {
            openConnection();
        }

        try {
            sendKeyData(key, press);
        } catch (RemoteControllerException e) {
            logger.debug("Couldn't send command", e);
            logger.debug("Retry one time...");

            closeConnection();
            openConnection();

            sendKeyData(key, press);
        }
    }

    /**
     * Send sequence of key codes to Samsung TV.
     *
     * @param keys List of key codes to send.
     * @throws RemoteControllerException
     */
    @Override
    public void sendKeys(List<KeyCode> keys) throws RemoteControllerException {
        sendKeys(keys, 300);
    }

    /**
     * Send sequence of key codes to Samsung TV.
     *
     * @param keys List of key codes to send.
     * @param sleepInMs Sleep between key code sending in milliseconds.
     * @throws RemoteControllerException
     */
    public void sendKeys(List<KeyCode> keys, int sleepInMs) throws RemoteControllerException {
        logger.debug("Try to send sequence of commands: {}", keys);

        if (!isConnected()) {
            openConnection();
        }

        for (int i = 0; i < keys.size(); i++) {
            KeyCode key = keys.get(i);
            try {
                sendKeyData(key, false);
            } catch (RemoteControllerException e) {
                logger.debug("Couldn't send command", e);
                logger.debug("Retry one time...");

                closeConnection();
                openConnection();

                sendKeyData(key, false);
            }

            if ((keys.size() - 1) != i) {
                // Sleep a while between commands
                try {
                    Thread.sleep(sleepInMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        logger.debug("Command(s) successfully sent");
    }

    private void sendKeyData(KeyCode key, boolean press) throws RemoteControllerException {
        webSocketRemote.sendKeyData(press, key.toString());
    }

    public void sendSourceApp(String app) {
        String appName = app;
        App appVal = apps.get(app);
        boolean deepLink = false;
        appName = appVal.appId;
        deepLink = appVal.type == 2;

        webSocketRemote.sendSourceApp(appName, deepLink);
    }

    public void sendUrl(String url) {
        String processedUrl = url.replace("/", "\\/");
        webSocketRemote.sendSourceApp("org.tizen.browser", false, processedUrl);
    }

    public List<String> getAppList() {
        ArrayList<String> appList = new ArrayList<>();
        for (App app : apps.values()) {
            appList.add(app.name);
        }
        return appList;
    }

    @Override
    public void lifeCycleStarted(@Nullable LifeCycle arg0) {
        logger.trace("WebSocketClient started");
        connectWebSockets();
    }

    @Override
    public void lifeCycleFailure(@Nullable LifeCycle arg0, @Nullable Throwable throwable) {
        logger.warn("WebSocketClient failure: {}", throwable != null ? throwable.toString() : null);
    }

    @Override
    public void lifeCycleStarting(@Nullable LifeCycle arg0) {
        logger.trace("WebSocketClient starting");
    }

    @Override
    public void lifeCycleStopped(@Nullable LifeCycle arg0) {
        logger.trace("WebSocketClient stopped");
    }

    @Override
    public void lifeCycleStopping(@Nullable LifeCycle arg0) {
        logger.trace("WebSocketClient stopping");
    }
}
