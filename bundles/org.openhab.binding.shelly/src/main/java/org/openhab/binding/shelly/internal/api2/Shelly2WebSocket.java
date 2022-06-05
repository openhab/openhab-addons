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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link Shelly1HttpApi} wraps the Shelly REST API and provides various low level function to access the device api
 * (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class Shelly2WebSocket {
    private final Logger logger = LoggerFactory.getLogger(Shelly2WebSocket.class);
    private final Gson gson = new Gson();

    private String deviceIp = "";
    private boolean inbound = false;

    private @Nullable Session session;
    private @Nullable Shelly2WebSocketInterface websocketHandler;
    private final WebSocketClient client = new WebSocketClient();
    private @Nullable ShellyThingTable thingTable;

    public Shelly2WebSocket() {
        int i = 1;
    }

    public Shelly2WebSocket(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Shelly2WebSocket(ShellyThingTable thingTable, boolean inbound) {
        this.thingTable = thingTable;
        this.inbound = inbound;
    }

    public void addMessageHandler(Shelly2WebSocketInterface interfacehandler) {
        this.websocketHandler = interfacehandler;
    }

    public void connect() throws ShellyApiException {
        try {
            disconnect();
            URI uri = new URI("ws://" + deviceIp + "/rpc");
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(HttpHeaders.HOST, deviceIp);
            request.setHeader("Origin", "http://" + deviceIp);
            request.setHeader("Pragma", "no-cache");
            request.setHeader("Cache-Control", "no-cache");
            request.setHeader("Sec-WebSocket-Key", "w4X+F1orCcE5Ja3x1hcuhQ==");
            request.setHeader("Sec-WebSocket-Version", "13");
            request.setHeader("Sec-WebSocket-Extensions", "x-webkit-deflate-frame");
            // request.setTimeout(SHELLY_API_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            client.start();
            client.connect(this, uri, request);
        } catch (Exception e) {
            throw new ShellyApiException("Unable to initialize WebSocket", e);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        if (deviceIp.isEmpty()) {
            // This is the inbound event web socket
            deviceIp = session.getRemoteAddress().getAddress().getHostAddress();
            logger.debug("Inbound WebSocket created iwth {}", deviceIp);
        }
        if (websocketHandler == null) {
            if (thingTable != null) {
                try {
                    ShellyThingInterface thing = thingTable.getThing(deviceIp);
                    Shelly2RpcApi api = (Shelly2RpcApi) thing.getApi();
                    if (api != null) {
                        websocketHandler = api.getRpc();
                    }
                } catch (IllegalArgumentException e) {
                    logger.debug("Event for unknown device ip ({})", deviceIp);
                }
            }
        }
        if (websocketHandler != null) {
            websocketHandler.onConnect(true);
        }
    }

    public void sendMessage(String str) throws ShellyApiException {
        if (session != null) {
            try {
                session.getRemote().sendString(str);
                return;
            } catch (IOException e) {
                throw new ShellyApiException("Error WebSocketSend failed", e);
            }
        }
        throw new ShellyApiException("Unable to send API request (No WebSocket session)");
    }

    @OnWebSocketMessage
    public void onText(Session session, String receivedMessage) {
        Shelly2WebSocketInterface handler = websocketHandler;
        if (handler != null) {
            try {
                Shelly2RpcBaseMessage message = fromJson(gson, receivedMessage, Shelly2RpcBaseMessage.class);
                logger.trace("{}: Inbound WebSocket message: {}", message.src, receivedMessage);
                if (message.method == null) {
                    message.method = SHELLYRPC_METHOD_NOTIFYFULLSTATUS;
                }

                switch (getString(message.method)) {
                    case SHELLYRPC_METHOD_NOTIFYSTATUS:
                    case SHELLYRPC_METHOD_NOTIFYFULLSTATUS:
                        Shelly2RpcNotifyStatus status = fromJson(gson, receivedMessage, Shelly2RpcNotifyStatus.class);
                        if (status.params == null) {
                            status.params = status.result;
                        }
                        handler.onNotifyStatus(status);
                        return;
                    case SHELLYRPC_METHOD_NOTIFYEVENT:
                        handler.onNotifyEvent(fromJson(gson, receivedMessage, Shelly2RpcNotifyEvent.class));
                        return;
                    default:
                        handler.onMessage(receivedMessage);
                }
            } catch (ShellyApiException e) {
            }
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public boolean isInbound() {
        return inbound;
    }

    public void closeWebsocketSession() throws ShellyApiException {
        disconnect();
        try {
            client.stop();
        } catch (Exception e) {
            throw new ShellyApiException("Unable to close WebSocket session", e);
        }
    }

    private void disconnect() {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    session.disconnect();
                }
                session.close();
            }
        } catch (IOException e) {
            logger.debug("Unable to close WebSocket", e);
        } finally {
            session = null;
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode != StatusCode.NORMAL) {
            logger.debug("WebSocket Connection closed: {} - {}", statusCode, getString(reason));
        }
        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        if (session != null) {
            if (!session.isOpen()) {
                if (session != null) {
                    session.close();
                }
            }
            session = null;
        }
        if (websocketHandler != null) {
            websocketHandler.onClose();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }
}