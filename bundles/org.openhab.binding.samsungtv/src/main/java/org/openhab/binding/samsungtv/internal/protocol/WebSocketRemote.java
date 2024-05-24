/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.*;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Websocket class for remote control
 *
 * @author Arjan Mels - Initial contribution
 * @author Nick Waterton - changes to sendKey(), some refactoring
 */
@NonNullByDefault
class WebSocketRemote extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketRemote.class);

    private static Gson gson = new Gson();

    private String host = "";
    private String className = "";
    private boolean mouseEnabled = false;

    @SuppressWarnings("unused")
    @NonNullByDefault({})
    public static class JSONMessage {
        String event;

        static class App {
            String appId;
            String name;
            int app_type;

            public String getAppId() {
                return Optional.ofNullable(appId).orElse("");
            }

            public String getName() {
                return Optional.ofNullable(name).orElse("");
            }

            public int getAppType() {
                return Optional.ofNullable(app_type).orElse(2);
            }
        }

        static class Data {
            String update_type;
            App[] data;
            String id;
            String token;
        }

        // data is sometimes a json object, sometimes a string or number
        JsonElement data;
        Data newData;

        static class Params {
            String params;

            static class Data {
                String appId;
            }

            Data data;
        }

        Params params;

        public String getEvent() {
            return Optional.ofNullable(event).orElse("");
        }

        public Data getData() {
            return Optional.ofNullable(data).map(a -> gson.fromJson(a, Data.class)).orElse(new Data());
        }

        public String getDataAsString() {
            return Optional.ofNullable(data).map(a -> a.toString()).orElse("");
        }

        public App[] getAppData() {
            return Optional.ofNullable(getData()).map(a -> a.data).orElse(new App[0]);
        }

        public String getToken() {
            return Optional.ofNullable(getData()).map(a -> a.token).orElse("");
        }

        public String getUpdateType() {
            return Optional.ofNullable(getData()).map(a -> a.update_type).orElse("");
        }

        public String getAppId() {
            return Optional.ofNullable(params).map(a -> a.data).map(a -> a.appId).orElse("");
        }
    }

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketRemote(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
        this.host = remoteControllerWebSocket.host;
        this.className = this.getClass().getSimpleName();
    }

    @Override
    public void onWebSocketError(@Nullable Throwable error) {
        super.onWebSocketError(error);
    }

    @Override
    public void onWebSocketText(@Nullable String msgarg) {
        if (msgarg == null) {
            return;
        }
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONMessage jsonMsg = remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);
            if (jsonMsg == null) {
                return;
            }
            switch (jsonMsg.getEvent()) {
                case "ms.channel.connect":
                    logger.debug("{}: Remote channel connected. Token = {}", host, jsonMsg.getToken());
                    if (!jsonMsg.getToken().isBlank()) {
                        this.remoteControllerWebSocket.callback.putConfig(WEBSOCKET_TOKEN, jsonMsg.getToken());
                        // try opening additional websockets
                        try {
                            this.remoteControllerWebSocket.openConnection();
                        } catch (RemoteControllerException e) {
                            logger.warn("{}: {}: Error ({})", host, className, e.getMessage());
                        }
                    }
                    getApps();
                    break;
                case "ms.channel.clientConnect":
                    logger.debug("{}: Another Remote client has connected", host);
                    break;
                case "ms.channel.clientDisconnect":
                    logger.debug("{}: Other Remote client has disconnected", host);
                    break;
                case "ms.channel.timeOut":
                    logger.warn("{}: Remote Control Channel Timeout, SendKey/power commands are not available", host);
                    break;
                case "ms.channel.unauthorized":
                    logger.warn("{}: Remote Control is not authorized, please allow access on your TV", host);
                    break;
                case "ms.remote.imeStart":
                    // Keyboard input start enable
                    break;
                case "ms.remote.imeDone":
                    // keyboard input enabled
                    break;
                case "ms.remote.imeUpdate":
                    // keyboard text selected (base64 format) is in data.toString()
                    // retrieve with getDataAsString()
                    break;
                case "ms.remote.imeEnd":
                    // keyboard selection completed
                    break;
                case "ms.remote.touchEnable":
                    logger.debug("{}: Mouse commands enabled", host);
                    mouseEnabled = true;
                    break;
                case "ms.remote.touchDisable":
                    logger.debug("{}: Mouse commands disabled", host);
                    mouseEnabled = false;
                    break;
                // note: the following 3 do not work on >2020 TV's
                case "ed.edenTV.update":
                    logger.debug("{}: edenTV update: {}", host, jsonMsg.getUpdateType());
                    if ("ed.edenApp.update".equals(jsonMsg.getUpdateType())) {
                        remoteControllerWebSocket.updateCurrentApp();
                    }
                    break;
                case "ed.apps.launch":
                    logger.debug("{}: App launch: {}", host,
                            "200".equals(jsonMsg.getDataAsString()) ? "successfull" : "failed");
                    if ("200".equals(jsonMsg.getDataAsString())) {
                        remoteControllerWebSocket.getAppStatus("");
                    }
                    break;
                case "ed.edenApp.get":
                    break;
                case "ed.installedApp.get":
                    handleInstalledApps(jsonMsg);
                    break;
                default:
                    logger.debug("{}: WebSocketRemote Unknown event: {}", host, msg);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: {}: Error ({}) in message: {}", host, className, e.getMessage(), msg);
        }
    }

    private void handleInstalledApps(JSONMessage jsonMsg) {
        remoteControllerWebSocket.apps.clear();
        Arrays.stream(jsonMsg.getAppData()).forEach(a -> remoteControllerWebSocket.apps.put(a.getName(),
                remoteControllerWebSocket.new App(a.getAppId(), a.getName(), a.getAppType())));
        remoteControllerWebSocket.updateCurrentApp();
        remoteControllerWebSocket.listApps();
    }

    void getApps() {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONSourceApp("ed.installedApp.get")));
    }

    @NonNullByDefault({})
    static class JSONSourceApp {
        public JSONSourceApp(String event) {
            this(event, "");
        }

        public JSONSourceApp(String event, String appId) {
            params.event = event;
            if (!appId.isBlank()) {
                params.data.appId = appId;
            }
        }

        public JSONSourceApp(String appName, boolean deepLink) {
            this(appName, deepLink, null);
        }

        public JSONSourceApp(String appName, boolean deepLink, String metaTag) {
            params.data.appId = appName;
            params.data.action_type = deepLink ? "DEEP_LINK" : "NATIVE_LAUNCH";
            params.data.metaTag = metaTag;
        }

        static class Params {
            static class Data {
                String appId;
                String action_type;
                String metaTag;
            }

            String event = "ed.apps.launch";
            String to = "host";
            Data data = new Data();
        }

        String method = "ms.channel.emit";
        Params params = new Params();
    }

    public void sendSourceApp(String appName, boolean deepLink, @Nullable String metaTag) {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONSourceApp(appName, deepLink, metaTag)));
    }

    @NonNullByDefault({})
    class JSONRemoteControl {
        public JSONRemoteControl(String action, String value) {
            switch (action) {
                case "Move":
                    params.Cmd = action;
                    // {"x": x, "y": y, "Time": str(duration)}
                    params.Position = remoteControllerWebSocket.gson.fromJson(value, location.class);
                    params.TypeOfRemote = "ProcessMouseDevice";
                    break;
                case "MouseClick":
                    params.Cmd = value;
                    params.TypeOfRemote = "ProcessMouseDevice";
                    break;
                case "Click":
                case "Press":
                case "Release":
                    params.Cmd = action;
                    params.DataOfCmd = value;
                    params.Option = "false";
                    params.TypeOfRemote = "SendRemoteKey";
                    break;
                case "End":
                    params.TypeOfRemote = "SendInputEnd";
                    break;
                case "Text":
                    params.Cmd = Utils.b64encode(value);
                    params.DataOfCmd = "base64";
                    params.TypeOfRemote = "SendInputString";
                    break;
            }
        }

        class location {
            int x;
            int y;
            String Time;
        }

        class Params {
            String Cmd;
            String DataOfCmd;
            location Position;
            String Option;
            String TypeOfRemote;
        }

        String method = "ms.remote.control";
        Params params = new Params();
    }

    void sendKeyData(String action, String key) {
        if (!mouseEnabled && ("Move".equals(action) || "MouseClick".equals(action))) {
            logger.warn("{}: Mouse actions are not enabled for this app", host);
            return;
        }
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONRemoteControl(action, key)));
    }
}
