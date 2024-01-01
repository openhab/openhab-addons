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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Websocket class to retrieve artmode status (on o.a. the Frame TV's)
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
class WebSocketArt extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketArt.class);

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketArt(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    @NonNullByDefault({})
    private static class JSONMessage {
        String event;

        @NonNullByDefault({})
        static class Data {
            String event;
            String status;
            String value;
        }

        // data is sometimes a json object, sometimes a string representation of a json object for d2d_service_message
        @Nullable
        JsonElement data;
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
                    if (jsonMsg.data != null) {
                        handleD2DServiceMessage(jsonMsg.data.getAsString());
                    } else {
                        logger.debug("Empty d2d_service_message event: {}", msg);
                    }
                    break;
                default:
                    logger.debug("WebSocketArt Unknown event: {}", msg);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: Error ({}) in message: {}", this.getClass().getSimpleName(), e.getMessage(), msg, e);
        }
    }

    private void handleD2DServiceMessage(String msg) {
        JSONMessage.Data data = remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.Data.class);
        if (data.event == null) {
            logger.debug("Unknown d2d_service_message event: {}", msg);
            return;
        } else {
            switch (data.event) {
                case "art_mode_changed":
                    logger.debug("art_mode_changed: {}", data.status);
                    if ("on".equals(data.status)) {
                        remoteControllerWebSocket.callback.powerUpdated(false, true);
                    } else {
                        remoteControllerWebSocket.callback.powerUpdated(true, false);
                    }
                    remoteControllerWebSocket.updateCurrentApp();
                    break;
                case "artmode_status":
                    logger.debug("artmode_status: {}", data.value);
                    if ("on".equals(data.value)) {
                        remoteControllerWebSocket.callback.powerUpdated(false, true);
                    } else {
                        remoteControllerWebSocket.callback.powerUpdated(true, false);
                    }
                    remoteControllerWebSocket.updateCurrentApp();
                    break;
                case "go_to_standby":
                    logger.debug("go_to_standby");
                    remoteControllerWebSocket.callback.powerUpdated(false, false);
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
    }

    @NonNullByDefault({})
    class JSONArtModeStatus {
        public JSONArtModeStatus() {
            Params.Data data = params.new Data();
            data.id = remoteControllerWebSocket.uuid.toString();
            params.data = remoteControllerWebSocket.gson.toJson(data);
        }

        @NonNullByDefault({})
        class Params {
            @NonNullByDefault({})
            class Data {
                String request = "get_artmode_status";
                String id;
            }

            String event = "art_app_request";
            String to = "host";
            String data;
        }

        String method = "ms.channel.emit";
        Params params = new Params();
    }

    void getArtmodeStatus() {
        sendCommand(remoteControllerWebSocket.gson.toJson(new JSONArtModeStatus()));
    }
}
