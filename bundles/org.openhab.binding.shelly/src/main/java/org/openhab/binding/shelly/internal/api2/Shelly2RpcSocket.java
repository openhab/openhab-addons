/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.addBluThing;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
// Shelly devices may remain silent for very long periods.
// Use effectively infinite idle timeout to avoid unintended disconnects.
@WebSocket(maxIdleTime = Integer.MAX_VALUE)
public class Shelly2RpcSocket implements WriteCallback {
    private final Logger logger = LoggerFactory.getLogger(Shelly2RpcSocket.class);
    private final Gson gson = new Gson();
    private final Object stateLock = new Object();
    private final boolean inbound;

    private volatile String thingName = "";
    private volatile String deviceIp = "";

    // All access to the following ones must be guarded by "stateLock"
    private @Nullable Session session;
    private final List<String> sendQueue = new ArrayList<>();
    private @Nullable Shelly2RpctInterface websocketHandler;
    private volatile @Nullable WebSocketClient client;
    private final @Nullable ShellyThingTable thingTable;

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
        inbound = false;
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
        synchronized (stateLock) {
            this.websocketHandler = interfacehandler;
        }
    }

    /**
     * Connect outbound Web Socket
     *
     * @throws ShellyApiException
     */
    public void connect() throws ShellyApiException {
        try {
            // Ensure any existing session is disconnected
            disconnect();

            String deviceIp = this.deviceIp;
            if (deviceIp.isBlank()) {
                throw new ShellyApiException("Device IP not set");
            }

            URI uri = new URI("ws://" + deviceIp + SHELLYRPC_ENDPOINT);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(HttpHeaders.HOST, deviceIp);
            request.setHeader("Origin", "http://" + deviceIp);
            request.setHeader("Pragma", "no-cache");
            request.setHeader("Cache-Control", "no-cache");

            if (logger.isTraceEnabled()) {
                logger.trace("{}: Connect WebSocket, URI={}", thingName, uri);
            }

            // Create a new client, assign client inside lock
            WebSocketClient newClient = new WebSocketClient();
            synchronized (stateLock) {
                this.client = newClient;
            }

            // Start and connect outside lock
            newClient.start();
            newClient.setConnectTimeout(5000);
            newClient.setStopTimeout(3000);
            newClient.connect(this, uri, request);

        } catch (URISyntaxException e) {
            throw new ShellyApiException("Invalid URI: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ShellyApiException("Failed to connect WebSocket: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ShellyApiException("Failed to start WebSocket", e);
        }
    }

    /**
     * WebSocket is connected, init handler and send queued messages
     *
     * @param session Newly created WebSocket connection
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        Shelly2RpctInterface handler;
        synchronized (stateLock) {
            handler = websocketHandler;
            this.session = session;

            if (session.getRemoteAddress() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: Invalid inbound WebSocket connect (invalid remote ip)", thingName);
                }
                session.close(StatusCode.ABNORMAL, "Invalid remote IP");
                this.session = null;
                return;
            }

            if (deviceIp.isEmpty()) {
                // This is the inbound event web socket
                deviceIp = session.getRemoteAddress().getAddress().getHostAddress();
            }
        }

        if (handler == null) {
            // TODO: This is a bit messy, I don't like to call getThing() while holding a lock, but it's not
            // easy to avoid. Make sure that getThing is sure to return in a timely manner even when thread-safe
            ShellyThingInterface thing = null;
            try {
                ShellyThingTable thingTable = this.thingTable;
                if (thingTable != null) {
                    thing = thingTable.getThing(deviceIp);
                }
            } catch (IllegalArgumentException e) { // unknown thing
                logger.debug("{}:RPC Connection error for {} (unknown/disabled thing? - {}), closing socket", thingName,
                        deviceIp, e.getMessage());
                session.close(StatusCode.SHUTDOWN, "Thing not active"); // TODO: validate
                return;
            }

            if (thing != null) {
                Shelly2ApiRpc api;
                synchronized (stateLock) {
                    api = (Shelly2ApiRpc) thing.getApi();
                    handler = api.getRpcHandler();
                    websocketHandler = handler;
                }
            }
        }
        if (handler == null) {
            logger.debug("{}:RPC Connection error for {} (unknown/disabled thing?", thingName, deviceIp);
            return;
        }
        handler.onConnect(deviceIp, true);

        if (logger.isDebugEnabled()) {
            logger.debug("{}: WebSocket connected {}<-{}, Idle Timeout={}", thingName, session.getLocalAddress(),
                    session.getRemoteAddress(), session.getIdleTimeout());
        }

        List<String> queue;
        synchronized (stateLock) {
            queue = List.copyOf(sendQueue);
            sendQueue.clear();
        }
        if (!queue.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Sending {} queued API request{}", thingName, queue.size(),
                        queue.size() > 1 ? "s" : "");
            }
            RemoteEndpoint remote = session.getRemote();
            for (String message : queue) {
                remote.sendString(message, this);
            }
        }
    }

    /**
     * Asynchronous send request over WebSocket (called by asyncApiRequest()
     *
     * @param str API request message
     * @throws ShellyApiException
     */
    public void sendMessage(String message) throws ShellyApiException {
        Session session;
        List<String> queue = null;
        synchronized (stateLock) {
            session = this.session;
            if (session == null || !session.isOpen()) {
                this.sendQueue.add(message);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: Queued outbound RPC Message (no session) {}", thingName, message);
                }
                return;
            }

            if (!this.sendQueue.isEmpty()) {
                queue = List.copyOf(this.sendQueue);
                this.sendQueue.clear();
            }
        }

        RemoteEndpoint remote = session.getRemote();
        if (queue != null && session.isOpen()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Sending {} queued RPC Messages via WebSocket {}", thingName, queue.size(),
                        queue.size() > 1 ? "s" : "");
            }
            for (String data : queue) {
                remote.sendString(data, this);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("{}: Send RPC Message via WebSocket {}", thingName, message);
        }
        remote.sendString(message, this);
    }

    /**
     * Close WebSocket session
     */
    public void disconnect() {
        WebSocketClient clientToStop;
        Session sessionToClose;

        // Grab references under lock
        synchronized (stateLock) {
            sessionToClose = this.session;
            this.session = null;

            clientToStop = this.client;
            this.client = null;
        }

        // Close session outside lock
        if (sessionToClose != null) {
            if (logger.isTraceEnabled() && sessionToClose.isOpen()) {
                logger.trace("{}: Disconnecting WebSocket ({} -> {})", thingName, sessionToClose.getLocalAddress(),
                        sessionToClose.getRemoteAddress());
            }
            try {
                sessionToClose.close(StatusCode.NORMAL, "Socket closed");
            } catch (WebSocketException e) {
                // Ignore intentional close
            }
        }

        // Stop client outside lock
        if (clientToStop != null) {
            try {
                clientToStop.stop();
            } catch (WebSocketException | InterruptedException e) {
                // expected, ignore
            } catch (Exception e) {
                if (!(e.getCause() instanceof InterruptedException) && logger.isDebugEnabled()) {
                    logger.debug("{}: Unable to stop WebSocketClient", thingName, e);
                }
            }
        }
    }

    /**
     * Inbound WebSocket message
     *
     * @param session WebSocket session
     * @param receivedMessage Textual API message
     */
    @OnWebSocketMessage
    public void onText(Session session, String receivedMessage) {
        final Shelly2RpctInterface handler;
        synchronized (stateLock) {
            handler = websocketHandler;
        }
        try {
            Shelly2RpcBaseMessage message = fromJson(gson, receivedMessage, Shelly2RpcBaseMessage.class);
            if (logger.isTraceEnabled()) {
                logger.trace("{}: Inbound Rpc message: {}", thingName, receivedMessage);
            }
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
                        Shelly2RpcNotifyEvent events = fromJson(gson, receivedMessage, Shelly2RpcNotifyEvent.class);
                        events.src = message.src;
                        if (events.params == null || events.params.events == null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("{}: Malformed event data: {}", thingName, receivedMessage);
                            }
                        } else {
                            for (Shelly2NotifyEvent event : events.params.events) {
                                if (getString(event.event).startsWith(SHELLY2_EVENT_BLUPREFIX)) {
                                    handleBluEvent(event, message, receivedMessage);
                                } else {
                                    handler.onNotifyEvent(receivedMessage);
                                }
                            }
                        }
                        break;
                    default:
                        handler.onMessage(receivedMessage);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: No Rpc listener registered for device {}, skip message: {}", thingName,
                            getString(message.src), receivedMessage);
                }
            }
        } catch (ShellyApiException | IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Unable to process Rpc message ({}): {}", thingName, e.getMessage(), receivedMessage);
            }
        }
    }

    private void handleBluEvent(Shelly2NotifyEvent e, Shelly2RpcBaseMessage message, String receivedMessage)
            throws ShellyApiException {
        ShellyThingTable thingTable = this.thingTable;
        if (thingTable == null) { // make the compiler happy
            return;
        }

        String address = getString(e.blu != null ? e.blu.addr : "").replace(":", "");
        if (thingTable.findThing(address) != null) {
            // known device
            ShellyThingInterface thing = thingTable.getThing(address);
            Shelly2ApiRpc api = (Shelly2ApiRpc) thing.getApi();
            Shelly2RpctInterface bluHandler = api.getRpcHandler();
            bluHandler.onNotifyEvent(receivedMessage);
        } else {
            // new device
            if (SHELLY2_EVENT_BLUSCAN.equals(e.event)) {
                addBluThing(message.src, e.blu, thingTable);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: NotifyEvent {} for unknown BLU device {} or Thing in Inbox", message.src, e.event,
                            e.blu != null ? e.blu.addr : "");
                }
            }
        }
    }

    public boolean isConnected() {
        synchronized (stateLock) {
            Session session = this.session;
            return session != null && session.isOpen();
        }
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
        synchronized (stateLock) {
            if (this.session == null) {
                return; // already closed, possible recursion
            }
        }
        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }

        if (statusCode != StatusCode.NORMAL && logger.isTraceEnabled()) {
            logger.trace("{}: Rpc connection closed: {} - {}", thingName, statusCode, getString(reason));
        }
        disconnect();
        Shelly2RpctInterface websocketHandler;
        synchronized (stateLock) {
            websocketHandler = this.websocketHandler;
        }
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
        Shelly2RpctInterface websocketHandler;
        synchronized (stateLock) {
            websocketHandler = this.websocketHandler;
        }
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }

    @Override
    public void writeSuccess() {
        logger.trace("{}: RPC Message sent", thingName);
    }

    @Override
    public void writeFailed(@Nullable Throwable exception) {
        Throwable e = exception;
        if (e != null) {
            if (e instanceof AsynchronousCloseException || e.getCause() instanceof AsynchronousCloseException
                    || e instanceof ClosedChannelException || e.getCause() instanceof ClosedChannelException) {
                return;
            }
        }

        String m = e != null ? getString(e.getMessage()) : "general";
        logger.debug("{}: Sending RPC Message failed: {}", thingName, m);
    }
}
