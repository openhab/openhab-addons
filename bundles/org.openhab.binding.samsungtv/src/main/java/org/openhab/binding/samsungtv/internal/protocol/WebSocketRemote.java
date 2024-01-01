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

import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebSocket.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Websocket class for remote control
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
class WebSocketRemote extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketRemote.class);

    @SuppressWarnings("unused")
    @NonNullByDefault({})
    private static class JSONMessage {
        String event;

        @NonNullByDefault({})
        static class App {
            String appId;
            String name;
            int app_type;
        }

        @NonNullByDefault({})
        static class Data {
            String update_type;
            App[] data;

            String id;
            String token;
        }

        Data data;

        @NonNullByDefault({})
        static class Params {
            String params;

            @NonNullByDefault({})
            static class Data {
                String appId;
            }

            Data data;
        }

        Params params;
    }

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketRemote(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    @Override
    public void onWebSocketError(@Nullable Throwable error) {
        super.onWebSocketError(error);
        remoteControllerWebSocket.callback.connectionError(error);
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
            switch (jsonMsg.event) {
                case "ms.channel.connect":
                    logger.debug("Remote channel connected. Token = {}", jsonMsg.data.token);
                    if (jsonMsg.data.token != null) {
                        this.remoteControllerWebSocket.callback.putConfig(SamsungTvConfiguration.WEBSOCKET_TOKEN,
                                jsonMsg.data.token);
                        // try opening additional websockets
                        try {
                            this.remoteControllerWebSocket.openConnection();
                        } catch (RemoteControllerException e) {
                            logger.warn("{}: Error ({})", this.getClass().getSimpleName(), e.getMessage());
                        }
                    }
                    getApps();
                    break;
                case "ms.channel.clientConnect":
                    logger.debug("Remote client connected");
                    break;
                case "ms.channel.clientDisconnect":
                    logger.debug("Remote client disconnected");
                    break;
                case "ed.edenTV.update":
                    logger.debug("edenTV update: {}", jsonMsg.data.update_type);
                    remoteControllerWebSocket.updateCurrentApp();
                    break;
                case "ed.apps.launch":
                    logger.debug("App launched: {}", jsonMsg.params.data.appId);
                    break;
                case "ed.installedApp.get":
                    handleInstalledApps(jsonMsg);
                    break;
                default:
                    logger.debug("WebSocketRemote Unknown event: {}", msg);

            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
        }
    }

    private void handleInstalledApps(JSONMessage jsonMsg) {
        remoteControllerWebSocket.apps.clear();

        for (JSONMessage.App jsonApp : jsonMsg.data.data) {
            App app = remoteControllerWebSocket.new App(jsonApp.appId, jsonApp.name, jsonApp.app_type);
            remoteControllerWebSocket.apps.put(app.name, app);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Installed Apps: {}", remoteControllerWebSocket.apps.entrySet().stream()
                    .map(entry -> entry.getValue().appId + " = " + entry.getKey()).collect(Collectors.joining(", ")));
        }

        remoteControllerWebSocket.updateCurrentApp();
    }

    @NonNullByDefault({})
    static class JSONAppInfo {
        @NonNullByDefault({})
        static class Params {
            String event = "ed.installedApp.get";
            String to = "host";
        }

        String method = "ms.channel.emit";
        Params params = new Params();
    }

    void getApps() {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONAppInfo()));
    }

    @NonNullByDefault({})
    static class JSONSourceApp {
        public JSONSourceApp(String appName, boolean deepLink) {
            this(appName, deepLink, null);
        }

        public JSONSourceApp(String appName, boolean deepLink, String metaTag) {
            params.data.appId = appName;
            params.data.action_type = deepLink ? "DEEP_LINK" : "NATIVE_LAUNCH";
            params.data.metaTag = metaTag;
        }

        @NonNullByDefault({})
        static class Params {
            @NonNullByDefault({})
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

    public void sendSourceApp(String appName, boolean deepLink) {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONSourceApp(appName, deepLink)));
    }

    public void sendSourceApp(String appName, boolean deepLink, String metaTag) {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONSourceApp(appName, deepLink, metaTag)));
    }

    @NonNullByDefault({})
    static class JSONRemoteControl {
        public JSONRemoteControl(boolean press, String key) {
            params.Cmd = press ? "Press" : "Click";
            params.DataOfCmd = key;
        }

        @NonNullByDefault({})
        static class Params {
            String Cmd;
            String DataOfCmd;
            String Option = "false";
            String TypeOfRemote = "SendRemoteKey";
        }

        String method = "ms.remote.control";
        Params params = new Params();
    }

    void sendKeyData(boolean press, String key) {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONRemoteControl(press, key)));
    }
}
