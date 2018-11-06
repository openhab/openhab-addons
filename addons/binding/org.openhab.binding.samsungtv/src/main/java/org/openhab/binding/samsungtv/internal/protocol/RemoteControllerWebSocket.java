/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * The {@link RemoteControllerWebSocket} is responsible for sending key codes to the
 * Samsung TV via the websocket protocol (for newer TV's).
 *
 * @author Arjan Mels - Initial contribution
 */
public class RemoteControllerWebSocket extends RemoteController implements Listener {

    private final Logger logger = LoggerFactory.getLogger(RemoteControllerWebSocket.class);

    private final static String WS_ENDPOINT_REMOTE_CONTROL = "/api/v2/channels/samsung.remote.control";
    private final static String WS_ENDPOINT_ART = "/api/v2/channels/com.samsung.art-app";
    private final static String WS_ENDPOINT_V2 = "/api/v2";
    private WebSocketClient client;

    UUID uuid = UUID.randomUUID();

    private RemoteControllerWebsocketCallback callback;

    WebSocketRemote webSocketRemote = new WebSocketRemote();
    WebSocketArt webSocketArt = new WebSocketArt();
    WebSocketV2 webSocketV2 = new WebSocketV2();

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
    }

    private boolean isConnected() {
        return webSocketRemote.isConnected();
    }

    @Override
    public void openConnection() throws RemoteControllerException {
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

    /**
     * Websocket base class
     *
     * @author Arjan Mels
     *
     */
    class WebSocketBase extends WebSocketAdapter {
        boolean isConnecting = false;

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            logger.debug("{} connection closed: {} - {}", this.getClass().getSimpleName(), statusCode, reason);
            super.onWebSocketClose(statusCode, reason);
            isConnecting = false;
        }

        @Override
        public void onWebSocketError(Throwable error) {
            logger.error("{} connection error: {}", this.getClass().getSimpleName(), error.getMessage());
            super.onWebSocketError(error);
            isConnecting = false;
        }

        void connect(URI uri) throws RemoteControllerException {
            if (isConnecting || isConnected()) {
                logger.trace("{} already connecting or connected", this.getClass().getSimpleName());
                return;
            }

            try {
                logger.debug("{} connecting to: {}", this.getClass().getSimpleName(), uri);

                isConnecting = true;
                client.connect(this, uri, new ClientUpgradeRequest());
            } catch (Exception e) {
                throw new RemoteControllerException(e);
            }
        }

        @Override
        public void onWebSocketConnect(Session session) {
            logger.debug("{} connection established: {}", this.getClass().getSimpleName(),
                    session.getRemoteAddress().getHostString());
            super.onWebSocketConnect(session);

            isConnecting = false;
        }

        void close() {
            getSession().close();
        }

        void sendCommand(String cmd) {
            try {
                // retry openening connection just in case
                openConnection();

                if (isConnected()) {
                    getRemote().sendString(cmd);
                    logger.trace("{}: sendCommand: {}", this.getClass().getSimpleName(), cmd);
                } else {
                    logger.warn("{} sending command while socket not connected: {}", this.getClass().getSimpleName(),
                            cmd);
                }
            } catch (Exception e) {
                logger.error("{}: cannot send command", this.getClass().getSimpleName(), e);
            }
        }

        @Override
        public void onWebSocketText(String str) {
            logger.trace("{}: onWebSocketText: {}", this.getClass().getSimpleName(), str);
        }

    }

    // temporary storage for source app. Will be used as value for the sourceApp channel when information is complete
    private String currentSourceApp = null;
    // last app in the apps list: used to detect when status information is complete
    String lastApp = null;

    /**
     * Retrieve app status for all apps. In the WebSocketv2 handler the currently running app will be determined
     */
    private void updateCurrentApp() {
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

    /**
     * Websocket class for remote control
     *
     * @author Arjan Mels
     *
     */
    class WebSocketRemote extends WebSocketBase {
        @Override
        public void onWebSocketError(Throwable error) {
            super.onWebSocketError(error);
            callback.connectionError(error);
        }

        @Override
        public void onWebSocketText(String msgarg) {
            String msg = msgarg.replace('\n', ' ');
            super.onWebSocketText(msg);
            try {
                JSONObject json = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(msg);
                switch (json.getAsString("event")) {
                    case "ms.channel.connect":
                        logger.debug("Remote channel connected");
                        getApps();
                        break;
                    case "ms.channel.clientConnect":
                        logger.debug("Remote client connected");
                        break;
                    case "ms.channel.clientDisconnect":
                        logger.debug("Remote client disconnected");
                        break;
                    case "ed.edenTV.update":
                        logger.debug("edenTV update: {}", ((JSONObject) json.get("data")).getAsString("update_type"));
                        updateCurrentApp();
                        break;
                    case "ed.apps.launch":
                        logger.debug("App launched: {}",
                                ((JSONObject) ((JSONObject) json.get("params")).get("data")).get("appId"));
                        break;
                    case "ed.installedApp.get":
                        apps.clear();

                        JSONObject data = (JSONObject) json.get("data");
                        JSONArray array = (JSONArray) data.get("data");

                        for (Object jsonApp : array) {
                            App app = new App(((JSONObject) jsonApp).getAsString("appId"),
                                    ((JSONObject) jsonApp).getAsString("name"),
                                    ((JSONObject) jsonApp).getAsNumber("app_type").intValue());
                            apps.put(app.name, app);
                        }

                        logger.debug("Installed Apps: "
                                + apps.entrySet().stream().map(entry -> entry.getValue().appId + " = " + entry.getKey())
                                        .collect(Collectors.joining(", ")));

                        updateCurrentApp();

                        break;
                    default:
                        logger.debug("WebSocketRemote Unknown event: {}", msg);

                }
            } catch (Exception e) {
                logger.error("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
            }
        }

    }

    /**
     * Websocket class to retrieve app status
     *
     * @author Arjan Mels
     *
     */
    class WebSocketV2 extends WebSocketBase {

        @Override
        public void onWebSocketText(String msgarg) {
            String msg = msgarg.replace('\n', ' ');
            super.onWebSocketText(msg);
            try {
                JSONObject json = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(msg);
                JSONObject result = (JSONObject) json.get("result");
                if (result != null) {
                    if ((currentSourceApp == null || currentSourceApp.isEmpty())
                            && "true".equals(result.getAsString("visible"))) {
                        logger.debug("Running app: {} = {}", result.getAsString("id"), result.getAsString("name"));
                        currentSourceApp = result.getAsString("name");
                        callback.currentAppUpdated(currentSourceApp);
                    }

                    if (lastApp != null && lastApp.equals(result.getAsString("id"))) {
                        if (currentSourceApp == null || currentSourceApp.isEmpty()) {
                            callback.currentAppUpdated("");
                        }
                    }
                } else if (json.getAsString("event") != null) {
                    switch (json.getAsString("event")) {
                        case "ms.channel.connect":
                            logger.debug("Remote channel connected");
                            // update is requested from ed.installedApp.get event: small risk that this websocket is not
                            // yet connected
                            break;
                        case "ms.channel.clientConnect":
                            logger.debug("Remote client connected");
                            break;
                        case "ms.channel.clientDisconnect":
                            logger.debug("Remote client disconnected");
                            break;
                        default:
                            logger.debug("WebSocketRemote Unknown event: {}", msg);

                    }
                }
            } catch (Exception e) {
                logger.error("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
            }
        }

    }

    /**
     * Websocket class to retrieve artmode status (on o.a. the Frame TV's)
     *
     * @author Arjan Mels
     *
     */
    class WebSocketArt extends WebSocketBase {
        @Override
        public void onWebSocketText(String msgarg) {
            String msg = msgarg.replace('\n', ' ');
            super.onWebSocketText(msg);
            try {
                JSONObject json = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(msg);
                switch (json.getAsString("event")) {
                    case "ms.channel.connect":
                        logger.debug("Art channel connected");
                        break;
                    case "ms.channel.ready":
                        logger.debug("Art channel ready");
                        getArtmodeStatus();
                        break;
                    case "ms.channel.clientConnect":
                        logger.debug("Art client connected");
                        break;
                    case "ms.channel.clientDisconnect":
                        logger.debug("Art client disconnected");
                        break;

                    case "d2d_service_message":
                        JSONObject json2 = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                                .parse(json.getAsString("data"));
                        if (json2 == null || json2.getAsString("event") == null) {
                            logger.debug("Empty d2d_service_message event: {}", msg);
                        } else {
                            switch (json2.getAsString("event")) {
                                case "art_mode_changed":
                                    logger.debug("art_mode_changed: {}", json2.getAsString("status"));
                                    if ("on".equals(json2.getAsString("status"))) {
                                        callback.powerUpdated(false, true);
                                    } else {
                                        callback.powerUpdated(true, false);
                                    }
                                    break;
                                case "artmode_status":
                                    logger.debug("artmode_status: {}", json2.getAsString("value"));
                                    if ("on".equals(json2.getAsString("value"))) {
                                        callback.powerUpdated(false, true);
                                    } else {
                                        callback.powerUpdated(true, false);
                                    }
                                    break;
                                case "go_to_standby":
                                    logger.debug("go_to_standby");
                                    callback.powerUpdated(false, false);
                                    break;
                                case "wakeup":
                                    logger.debug("wakeup");
                                    // check artmode status to know complete status before updating
                                    getArtmodeStatus();
                                    break;
                                default:
                                    logger.debug("Unknown d2d_service_message event: {}", msg);
                            }
                        }
                        // ignore;
                        break;
                    default:
                        logger.debug("WebSocketArt Unknown event: {}", msg);
                }

            } catch (Exception e) {
                logger.error("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
            }
        }

    }

    private void getArtmodeStatus() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("request", "get_artmode_status");
        jsonData.put("id", uuid.toString());

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("event", "art_app_request");
        jsonParams.put("to", "host");
        jsonParams.put("data", jsonData.toJSONString());

        JSONObject json = new JSONObject();
        json.put("method", "ms.channel.emit");
        json.put("params", jsonParams);

        webSocketArt.sendCommand(json.toJSONString());
    }

    private void getAppStatus(String id) {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("id", id);

        JSONObject json = new JSONObject();
        json.put("method", "ms.application.get");
        json.put("id", uuid.toString());
        json.put("params", jsonParams);

        webSocketV2.sendCommand(json.toJSONString());
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

        logger.debug("Command successfully sent");
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

    private void sendKeyData(KeyCode key, boolean press) throws RemoteControllerException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("Cmd", press ? "Press" : "Click");
        jsonParams.put("DataOfCmd", key.toString());
        jsonParams.put("Option", "false");
        jsonParams.put("TypeOfRemote", "SendRemoteKey");

        JSONObject json = new JSONObject();
        json.put("method", "ms.remote.control");
        json.put("params", jsonParams);

        webSocketRemote.sendCommand(json.toJSONString());
    }

    public void sendSourceApp(String app) {
        String appName = app;
        App appVal = apps.get(app);
        boolean deepLink = false;
        if (appVal != null) {
            appName = appVal.appId;
            deepLink = appVal.type == 2;
        }

        JSONObject jsonData = new JSONObject();
        jsonData.put("appId", appName);
        jsonData.put("action_type", deepLink ? "DEEP_LINK" : "NATIVE_LAUNCH");

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("event", "ed.apps.launch");
        jsonParams.put("to", "host");
        jsonParams.put("data", jsonData);

        JSONObject json = new JSONObject();
        json.put("method", "ms.channel.emit");
        json.put("params", jsonParams);

        webSocketRemote.sendCommand(json.toJSONString());
    }

    public void sendUrl(String url) {

        JSONObject jsonData = new JSONObject();
        jsonData.put("appId", "org.tizen.browser");
        jsonData.put("action_type", "NATIVE_LAUNCH");
        jsonData.put("metaTag", url);

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("event", "ed.apps.launch");
        jsonParams.put("to", "host");
        jsonParams.put("data", jsonData);

        JSONObject json = new JSONObject();
        json.put("method", "ms.channel.emit");
        json.put("params", jsonParams);

        webSocketRemote.sendCommand(json.toJSONString());
    }

    private void getApps() {
        getInfo("ed.installedApp.get");
    }

    private void getInfo(String str) {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("event", str);
        jsonParams.put("to", "host");

        JSONObject json = new JSONObject();
        json.put("method", "ms.channel.emit");
        json.put("params", jsonParams);

        webSocketRemote.sendCommand(json.toJSONString());
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
        connectWebSockets();
    }

    @Override
    public void lifeCycleFailure(LifeCycle arg0, Throwable throwable) {
        logger.warn("Problem creating websocket client", throwable);
    }

    @Override
    public void lifeCycleStarting(LifeCycle arg0) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle arg0) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle arg0) {
    }

}
