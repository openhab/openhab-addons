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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Websocket class to retrieve artmode status (on o.a. the Frame TV's)
 *
 * @author Arjan Mels - Initial contribution
 *
 */
class WebSocketArt extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketBase.class);

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketArt(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    private static class JSONMessage {
        String event;

        static class Data {
            String event;
            String status;
            String value;
        };

        Data data;
    }

    @Override
    public void onWebSocketText(String msgarg) {
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONMessage jsonMsg = this.remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);

            switch (jsonMsg.event) {
                case "ms.channel.connect":
                    logger.debug("Art channel connected");
                    break;
                case "ms.channel.ready":
                    logger.debug("Art channel ready");
                    this.remoteControllerWebSocket.getArtmodeStatus();
                    break;
                case "ms.channel.clientConnect":
                    logger.debug("Art client connected");
                    break;
                case "ms.channel.clientDisconnect":
                    logger.debug("Art client disconnected");
                    break;

                case "d2d_service_message":
                    if (jsonMsg.data == null || jsonMsg.data.event == null) {
                        logger.debug("Empty d2d_service_message event: {}", msg);
                    } else {
                        handleD2DServiceMessage(msg, jsonMsg);
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

    private void handleD2DServiceMessage(String msg, JSONMessage jsonMsg) {
        switch (jsonMsg.data.event) {
            case "art_mode_changed":
                logger.debug("art_mode_changed: {}", jsonMsg.data.status);
                if ("on".equals(jsonMsg.data.status)) {
                    this.remoteControllerWebSocket.callback.powerUpdated(false, true);
                } else {
                    this.remoteControllerWebSocket.callback.powerUpdated(true, false);
                }
                break;
            case "artmode_status":
                logger.debug("artmode_status: {}", jsonMsg.data.value);
                if ("on".equals(jsonMsg.data.value)) {
                    this.remoteControllerWebSocket.callback.powerUpdated(false, true);
                } else {
                    this.remoteControllerWebSocket.callback.powerUpdated(true, false);
                }
                break;
            case "go_to_standby":
                logger.debug("go_to_standby");
                this.remoteControllerWebSocket.callback.powerUpdated(false, false);
                break;
            case "wakeup":
                logger.debug("wakeup");
                // check artmode status to know complete status before updating
                this.remoteControllerWebSocket.getArtmodeStatus();
                break;
            default:
                logger.debug("Unknown d2d_service_message event: {}", msg);
        }
    }

}