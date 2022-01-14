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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Websocket class to retrieve app status
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
class WebSocketV2 extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketV2.class);

    WebSocketV2(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    @SuppressWarnings("unused")
    @NonNullByDefault({})
    private static class JSONMessage {
        String event;

        @NonNullByDefault({})
        static class Result {
            String id;
            String name;
            String visible;
        }

        @NonNullByDefault({})
        static class Data {
            String id;
            String token;
        }

        @NonNullByDefault({})
        static class Error {
            String code;
            String details;
            String message;
            String status;
        }

        Result result;
        Data data;
        Error error;
    }

    @Override
    public void onWebSocketText(@Nullable String msgarg) {
        if (msgarg == null) {
            return;
        }
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONMessage jsonMsg = this.remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);

            if (jsonMsg.result != null) {
                handleResult(jsonMsg);
                return;
            }
            if (jsonMsg.error != null) {
                logger.debug("WebSocketV2 Error received: {}", msg);
                return;
            }
            if (jsonMsg.event == null) {
                logger.debug("WebSocketV2 Unknown response format: {}", msg);
                return;
            }

            switch (jsonMsg.event) {
                case "ms.channel.connect":
                    logger.debug("V2 channel connected. Token = {}", jsonMsg.data.token);

                    // update is requested from ed.installedApp.get event: small risk that this websocket is not
                    // yet connected
                    break;
                case "ms.channel.clientConnect":
                    logger.debug("V2 client connected");
                    break;
                case "ms.channel.clientDisconnect":
                    logger.debug("V2 client disconnected");
                    break;
                default:
                    logger.debug("V2 Unknown event: {}", msg);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
        }
    }

    private void handleResult(JSONMessage jsonMsg) {
        if ((remoteControllerWebSocket.currentSourceApp == null
                || remoteControllerWebSocket.currentSourceApp.trim().isEmpty())
                && "true".equals(jsonMsg.result.visible)) {
            logger.debug("Running app: {} = {}", jsonMsg.result.id, jsonMsg.result.name);
            remoteControllerWebSocket.currentSourceApp = jsonMsg.result.name;
            remoteControllerWebSocket.callback.currentAppUpdated(remoteControllerWebSocket.currentSourceApp);
        }

        if (remoteControllerWebSocket.lastApp != null && remoteControllerWebSocket.lastApp.equals(jsonMsg.result.id)) {
            if (remoteControllerWebSocket.currentSourceApp == null
                    || remoteControllerWebSocket.currentSourceApp.trim().isEmpty()) {
                remoteControllerWebSocket.callback.currentAppUpdated("");
            }
            remoteControllerWebSocket.lastApp = null;
        }
    }

    @NonNullByDefault({})
    static class JSONAppStatus {
        public JSONAppStatus(String id) {
            this.id = id;
            params.id = id;
        }

        @NonNullByDefault({})
        static class Params {
            String id;
        }

        String method = "ms.application.get";
        String id;
        Params params = new Params();
    }

    void getAppStatus(String id) {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONAppStatus(id)));
    }
}
