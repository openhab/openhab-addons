/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.concurrent.CountDownLatch;

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
@WebSocket(maxIdleTime = Integer.MAX_VALUE)
public class Shelly2RpcSocket {
    private final Logger logger = LoggerFactory.getLogger(Shelly2RpcSocket.class);
    private final Gson gson = new Gson();

    private String thingName = "";
    private String deviceIp = "";
    private boolean inbound = false;
    private CountDownLatch connectLatch = new CountDownLatch(1);

    private @Nullable Session session;
    private @Nullable Shelly2RpctInterface websocketHandler;
    private WebSocketClient client = new WebSocketClient();
    private @Nullable ShellyThingTable thingTable;

    public Shelly2RpcSocket() {
    }

    /**
     * Regular constructor for Thing and Discover handler
     *
     * @param thingName Thing/Service name
     * @param thingTable
     * @param deviceIp IP address for the device
     */
    public Shelly2RpcSocket(String thingName, @Nullable ShellyThingTable thingTable, String deviceIp) {
        this.thingName = thingName;
        this.deviceIp = deviceIp;
        this.thingTable = thingTable;
    }

    /**
     * Constructor called from Servlet handler
     *
     * @param thingTable
     * @param inbound
     */
    public Shelly2RpcSocket(ShellyThingTable thingTable, boolean inbound) {
        this.thingTable = thingTable;
        this.inbound = inbound;
    }

    /**
     * Add listener for inbound messages implementing Shelly2RpctInterface
     *
     * @param interfacehandler
     */
    public void addMessageHandler(Shelly2RpctInterface interfacehandler) {
        this.websocketHandler = interfacehandler;
    }

    /**
     * Connect outbound Web Socket
     *
     * @throws ShellyApiException
     */
    public void connect() throws ShellyApiException {
        try {
            disconnect(); // for safety

            URI uri = new URI("ws://" + deviceIp + "/rpc");
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(HttpHeaders.HOST, deviceIp);
            request.setHeader("Origin", "http://" + deviceIp);
            request.setHeader("Pragma", "no-cache");
            request.setHeader("Cache-Control", "no-cache");

            logger.debug("{}: Connect WebSocket, URI={}", thingName, uri);
            client = new WebSocketClient();
            connectLatch = new CountDownLatch(1);
            client.start();
            client.setConnectTimeout(5000);
            client.setStopTimeout(0);
            client.connect(this, uri, request);
        } catch (Exception e) {
            throw new ShellyApiException("Unable to initialize WebSocket", e);
        }
    }

    /**
     * Web Socket is connected, lookup thing and create connectLatch to synchronize first sendMessage()
     *
     * @param session Newly created WebSocket connection
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        try {
            if (session.getRemoteAddress() == null) {
                logger.debug("{}: Invalid inbound WebSocket connect", thingName);
                session.close(StatusCode.ABNORMAL, "Invalid remote IP");
                return;
            }
            this.session = session;
            if (deviceIp.isEmpty()) {
                // This is the inbound event web socket
                deviceIp = session.getRemoteAddress().getAddress().getHostAddress();
            }
            if (websocketHandler == null) {
                if (thingTable != null) {
                    ShellyThingInterface thing = thingTable.getThing(deviceIp);
                    Shelly2ApiRpc api = (Shelly2ApiRpc) thing.getApi();
                    websocketHandler = api.getRpcHandler();
                }
            }
            connectLatch.countDown();

            logger.debug("{}: WebSocket connected {}<-{}, Idle Timeout={}", thingName, session.getLocalAddress(),
                    session.getRemoteAddress(), session.getIdleTimeout());
            if (websocketHandler != null) {
                websocketHandler.onConnect(deviceIp, true);
                return;
            }
        } catch (IllegalArgumentException e) { // unknown thing
            // debug is below
        }

        if (websocketHandler == null && thingTable != null) {
            logger.debug("Rpc: Unable to handle connection from {} (unknown/disabled thing), closing socket", deviceIp);
            session.close(StatusCode.SHUTDOWN, "Thing not active");
        }
    }

    /**
     * Send request over WebSocket
     *
     * @param str API request message
     * @throws ShellyApiException
     */
    @SuppressWarnings("null")
    public void sendMessage(String str) throws ShellyApiException {
        if (session != null) {
            try {
                connectLatch.await();
                session.getRemote().sendString(str);
                return;
            } catch (IOException | InterruptedException e) {
                throw new ShellyApiException("Error RpcSend failed", e);
            }
        }
        throw new ShellyApiException("Unable to send API request (No Rpc session)");
    }

    /**
     * Close WebSocket session
     */
    public void disconnect() {
        try {
            if (session != null) {
                Session s = session;
                if (s.isOpen()) {
                    logger.debug("{}: Disconnecting WebSocket ({} -> {})", thingName, s.getLocalAddress(),
                            s.getRemoteAddress());
                    s.disconnect();
                }
                s.close(StatusCode.NORMAL, "Socket closed");
                session = null;
            }
            if (client.isStarted()) {
                client.stop();
            }
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) {
                logger.debug("{}: Unable to close socket - interrupted", thingName); // e.g. device was rebooted
            } else {
                logger.debug("{}: Unable to close socket", thingName, e);
            }
        }
    }

    /**
     * Inbound WebSocket message
     *
     * @param session WebSpcket session
     * @param receivedMessage Textial API message
     */
    @OnWebSocketMessage
    public void onText(Session session, String receivedMessage) {
        try {
            Shelly2RpctInterface handler = websocketHandler;
            Shelly2RpcBaseMessage message = fromJson(gson, receivedMessage, Shelly2RpcBaseMessage.class);
            logger.trace("{}: Inbound Rpc message: {}", thingName, receivedMessage);
            if (handler != null) {
                if (thingName.isEmpty()) {
                    thingName = getString(message.src);
                }
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
            } else {
                logger.debug("{}: No Rpc listener registered for device {}, skip message: {}", thingName,
                        getString(message.src), receivedMessage);
            }
        } catch (ShellyApiException | IllegalArgumentException | NullPointerException e) {
            logger.debug("{}: Unable to process Rpc message: {}", thingName, receivedMessage, e);
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public boolean isInbound() {
        return inbound;
    }

    /**
     * Web Socket closed, notify thing handler
     *
     * @param statusCode StatusCode
     * @param reason Textual reason
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode != StatusCode.NORMAL) {
            logger.trace("{}: Rpc connection closed: {} - {}", thingName, statusCode, getString(reason));
        }
        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        disconnect();
        if (websocketHandler != null) {
            websocketHandler.onClose(statusCode, reason);
        }
    }

    /**
     * WebSocket error handler
     *
     * @param cause WebSocket error/Exception
     */
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
