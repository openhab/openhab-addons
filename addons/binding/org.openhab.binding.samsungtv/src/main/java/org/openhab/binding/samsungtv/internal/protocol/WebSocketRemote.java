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

import java.util.stream.Collectors;

import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebSocket.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Websocket class for remote control
 *
 * @author Arjan Mels - Initial contribution
 *
 */
class WebSocketRemote extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketBase.class);

    static class JSONMessage {
        String event;

        static class App {
            String appId;
            String name;
            int app_type;
        };

        static class Data {
            String update_type;
            App[] data;
        };

        Data data;

        static class Params {
            String params;

            static class Data {
                String appId;
            };

            Data data;
        };

        Params params;

    }

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketRemote(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    @Override
    public void onWebSocketError(Throwable error) {
        super.onWebSocketError(error);
        this.remoteControllerWebSocket.callback.connectionError(error);
    }

    @Override
    public void onWebSocketText(String msgarg) {
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONMessage jsonMsg = this.remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);
            switch (jsonMsg.event) {
                case "ms.channel.connect":
                    this.remoteControllerWebSocket.logger.debug("Remote channel connected");
                    this.remoteControllerWebSocket.getApps();
                    break;
                case "ms.channel.clientConnect":
                    this.remoteControllerWebSocket.logger.debug("Remote client connected");
                    break;
                case "ms.channel.clientDisconnect":
                    this.remoteControllerWebSocket.logger.debug("Remote client disconnected");
                    break;
                case "ed.edenTV.update":
                    this.remoteControllerWebSocket.logger.debug("edenTV update: {}", jsonMsg.data.update_type);
                    this.remoteControllerWebSocket.updateCurrentApp();
                    break;
                case "ed.apps.launch":
                    this.remoteControllerWebSocket.logger.debug("App launched: {}", jsonMsg.params.data.appId);
                    break;
                case "ed.installedApp.get":
                    this.remoteControllerWebSocket.apps.clear();

                    for (JSONMessage.App jsonApp : jsonMsg.data.data) {
                        App app = this.remoteControllerWebSocket.new App(jsonApp.appId, jsonApp.name, jsonApp.app_type);
                        this.remoteControllerWebSocket.apps.put(app.name, app);
                    }

                    this.remoteControllerWebSocket.logger.debug("Installed Apps: " + this.remoteControllerWebSocket.apps
                            .entrySet().stream().map(entry -> entry.getValue().appId + " = " + entry.getKey())
                            .collect(Collectors.joining(", ")));

                    this.remoteControllerWebSocket.updateCurrentApp();

                    break;
                default:
                    this.remoteControllerWebSocket.logger.debug("WebSocketRemote Unknown event: {}", msg);

            }
        } catch (Exception e) {
            this.remoteControllerWebSocket.logger.error("{}: Error ({}) in message: {}",
                    this.getClass().getSimpleName(), e.getMessage(), msg, e);
        }
    }

}