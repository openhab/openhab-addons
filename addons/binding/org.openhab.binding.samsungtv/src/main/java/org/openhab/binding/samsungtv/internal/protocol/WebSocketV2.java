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
 * Websocket class to retrieve app status
 *
 * @author Arjan Mels - Initial contribution
 *
 */
class WebSocketV2 extends WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketV2.class);

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketV2(RemoteControllerWebSocket remoteControllerWebSocket) {
        super(remoteControllerWebSocket);
    }

    static class JSONMessage {
        String event;

        static class Result {
            String id;
            String name;
            String visible;
        };

        Result result;
    }

    @Override
    public void onWebSocketText(String msgarg) {
        String msg = msgarg.replace('\n', ' ');
        super.onWebSocketText(msg);
        try {
            JSONMessage jsonMsg = this.remoteControllerWebSocket.gson.fromJson(msg, JSONMessage.class);

            if (jsonMsg.result != null) {
                if ((remoteControllerWebSocket.currentSourceApp == null
                        || remoteControllerWebSocket.currentSourceApp.isEmpty())
                        && "true".equals(jsonMsg.result.visible)) {
                    logger.debug("Running app: {} = {}", jsonMsg.result.id, jsonMsg.result.name);
                    remoteControllerWebSocket.currentSourceApp = jsonMsg.result.name;
                    remoteControllerWebSocket.callback.currentAppUpdated(remoteControllerWebSocket.currentSourceApp);
                }

                if (remoteControllerWebSocket.lastApp != null
                        && remoteControllerWebSocket.lastApp.equals(jsonMsg.result.id)) {
                    if (remoteControllerWebSocket.currentSourceApp == null
                            || remoteControllerWebSocket.currentSourceApp.isEmpty()) {
                        remoteControllerWebSocket.callback.currentAppUpdated("");
                    }
                }
            } else if (jsonMsg.event != null) {
                switch (jsonMsg.event) {
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