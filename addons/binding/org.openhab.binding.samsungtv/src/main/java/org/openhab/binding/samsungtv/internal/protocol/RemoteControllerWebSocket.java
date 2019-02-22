/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
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
public class RemoteControllerWebSocket extends RemoteController implements Listener {

    final Logger logger = LoggerFactory.getLogger(RemoteControllerWebSocket.class);

    private final static String WS_ENDPOINT_REMOTE_CONTROL = "/api/v2/channels/samsung.remote.control";
    private final static String WS_ENDPOINT_ART = "/api/v2/channels/com.samsung.art-app";
    private final static String WS_ENDPOINT_V2 = "/api/v2";
    WebSocketClient client;

    private final UUID uuid = UUID.randomUUID();

    RemoteControllerWebsocketCallback callback;

    WebSocketRemote webSocketRemote;
    WebSocketArt webSocketArt;
    WebSocketV2 webSocketV2;

    Gson gson = new Gson();

    /**
     * Create and initialize remote controller instance.
     *
     * @param host                    Host name of the Samsung TV.
     * @param port                    TCP port of the remote controller protocol.
     * @param appName                 Application name used to send key codes.
     * @param uniqueId                Unique Id used to send key codes.
     * @param remoteControllerService
     */

    public RemoteControllerWebSocket(String host, int port, String appName, String uniqueId,
            RemoteControllerWebsocketCallback remoteControllerWebsocketCallback) {
        super(host, port, appName, uniqueId);
        this.client = new WebSocketClient();
        this.client.addLifeCycleListener(this);

        this.callback = remoteControllerWebsocketCallback;

        webSocketRemote = new WebSocketRemote(this);
        webSocketArt = new WebSocketArt(this);
        webSocketV2 = new WebSocketV2(this);

    }

    private boolean isConnected() {
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
                logger.warn("Cannot connect to websocket remote control interface: " + e.getMessage());
                throw new RemoteControllerException(e);
            }
        }
        connectWebSockets();
    }

    private void connectWebSockets() {
        logger.trace("connectWebSockets()");

        try {
            webSocketRemote.connect(new URI("ws", null, host, port, WS_ENDPOINT_REMOTE_CONTROL, "name=openhab", null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logger.warn("Problem connecting to remote websocket", e);
        }
        try {
            webSocketArt.connect(new URI("ws", null, host, port, WS_ENDPOINT_ART, "name=openhab", null));
        } catch (RemoteControllerException | URISyntaxException e) {
            logger.warn("Problem connecting to artmode websocket", e);
        }
        try {
            webSocketV2.connect(new URI("ws", null, host, port, WS_ENDPOINT_V2, "name=openhab", null));
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
    public void close() throws Exception {
        logger.debug("RemoteControllerWebSocket close");
        closeConnection();
    }

    // temporary storage for source app. Will be used as value for the sourceApp channel when information is complete
    String currentSourceApp = null;
    // last app in the apps list: used to detect when status information is complete
    String lastApp = null;

    /**
     * Retrieve app status for all apps. In the WebSocketv2 handler the currently running app will be determined
     */
    void updateCurrentApp() {
        if (webSocketV2.isNotConnected()) {
            logger.warn("Cannot retrieve current app webSocketV2 is not connected");
            return;
        }

        currentSourceApp = null;
        lastApp = null;

        // retrieve last app (don't merge with next loop as this might run asynchronously
        for (App app : apps.values()) {
            lastApp = app.appId;
        }

        for (App app : apps.values()) {
            getAppStatus(app.appId);
        }
    }

    static class JSONArtModeStatus {

        public JSONArtModeStatus(UUID uuid) {
            params.data.id = uuid.toString();
        }

        static class Params {
            static class Data {
                String request = "get_artmode_status";
                String id;
            }

            String event = "art_app_request";
            String to = "host";
            Data data = new Data();
        }

        String method = "ms.channel.emit";
        Params params = new Params();

    }

    void getArtmodeStatus() {
        webSocketArt.sendCommand(gson.toJson(new JSONArtModeStatus(uuid)));
    }

    static class JSONAppStatus {

        public JSONAppStatus(UUID uuid, String id) {
            params.id = uuid.toString();
            this.id = id;
        }

        static class Params {
            String id;

        }

        String method = "ms.application.get";
        String id;
        Params params = new Params();

    }

    private void getAppStatus(String id) {
        webSocketV2.sendCommand(gson.toJson(new JSONAppStatus(uuid, id)));
    }

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

    Map<String, App> apps = new LinkedHashMap<>();

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
     * @param keys      List of key codes to send.
     * @param sleepInMs Sleep between key code sending in milliseconds.
     * @throws RemoteControllerException
     */
    public void sendKeys(List<KeyCode> keys, int sleepInMs) throws RemoteControllerException {
        logger.debug("Try to send sequnce of commands: {}", keys);

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

    static class JSONRemoteControl {

        public JSONRemoteControl(boolean press, String key) {
            params.Cmd = press ? "Press" : "Click";
            params.DataOfCmd = key;
        }

        static class Params {
            String Cmd;
            String DataOfCmd;
            String Option = "false";
            String TypeOfRemote = "SendRemoteKey";

        }

        String method = "ms.remote.control";
        Params params = new Params();

    }

    private void sendKeyData(KeyCode key, boolean press) throws RemoteControllerException {
        webSocketRemote.sendCommand(gson.toJson(new JSONRemoteControl(press, key.toString())));
    }

    static class JSONSourceApp {

        public JSONSourceApp(String appName, boolean deepLink) {
            params.data.appId = appName;
            params.data.action_type = deepLink ? "DEEP_LINK" : "NATIVE_LAUNCH";
        }

        static class Params {
            static class Data {
                String appId;
                String action_type;
            }

            String event = "ed.apps.launch";
            String to = "host";
            Data data = new Data();

        }

        String method = "ms.channel.emit";
        Params params = new Params();

    }

    public void sendSourceApp(String app) {
        String appName = app;
        App appVal = apps.get(app);
        boolean deepLink = false;
        if (appVal != null) {
            appName = appVal.appId;
            deepLink = appVal.type == 2;
        }

        webSocketRemote.sendCommand(gson.toJson(new JSONSourceApp(appName, deepLink)));
    }

    public void sendUrl(String url) {
        webSocketRemote.sendCommand(gson.toJson(new JSONSourceApp("org.tizen.browser", false)));
    }

    static class JSONAppInfo {

        static class Params {
            String event = "ed.installedApp.get";
            String to = "host";
        }

        String method = "ms.channel.emit";
        Params params = new Params();

    }

    void getApps() {
        webSocketRemote.sendCommand(gson.toJson(new JSONAppInfo()));
    }

    public List<String> getAppList() {
        ArrayList<String> appList = new ArrayList<>();
        for (App app : apps.values()) {
            appList.add(app.name);
        }
        return appList;
    }

    @Override
    public void lifeCycleStarted(LifeCycle arg0) {
        logger.trace("WebSocketClient started");
        connectWebSockets();
    }

    @Override
    public void lifeCycleFailure(LifeCycle arg0, Throwable throwable) {
        logger.warn("Problem creating websocket client", throwable);
    }

    @Override
    public void lifeCycleStarting(LifeCycle arg0) {
        logger.trace("WebSocketClient starting");
    }

    @Override
    public void lifeCycleStopped(LifeCycle arg0) {
        logger.trace("WebSocketClient stopped");
    }

    @Override
    public void lifeCycleStopping(LifeCycle arg0) {
        logger.trace("WebSocketClient stopping");
    }

}
