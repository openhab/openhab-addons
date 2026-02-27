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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.SHELLY_API_TIMEOUT_MS;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.*;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.addBluThing;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
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
import org.openhab.core.io.net.http.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebSocket(maxIdleTime = Integer.MAX_VALUE)
public class Shelly2RpcSocket implements WriteCallback {
    private final Logger logger = LoggerFactory.getLogger(Shelly2RpcSocket.class);
    private final Gson gson = new Gson();

    private volatile String thingName = "";
    private volatile String deviceIp = "";
    private final boolean inbound;
    private final ShellyThingTable thingTable;

    // All access must be guarded by "this"
    private @Nullable Session session;

    // All access must be guarded by "this"
    private final List<String> sendQueue = new ArrayList<>();

    // All access must be guarded by "this"
    private @Nullable Shelly2RpctInterface websocketHandler;

    private final WebSocketClient client;

    /**
     * Regular constructor for Thing and Discover handler
     *
     * @param thingName Thing/Service name
     * @param thingTable
     * @param deviceIp IP address for the device
     */
    public Shelly2RpcSocket(String thingName, ShellyThingTable thingTable, String deviceIp,
            WebSocketClient webSocketClient) {
        this.thingName = thingName;
        this.deviceIp = deviceIp;
        this.thingTable = thingTable;
        this.client = webSocketClient;
        inbound = false;
    }

    /**
     * Constructor called from Servlet handler
     *
     * @param thingTable
     * @param inbound
     */
    public Shelly2RpcSocket(ShellyThingTable thingTable, boolean inbound, WebSocketClient webSocketClient) {
        this.thingTable = thingTable;
        this.inbound = inbound;
        this.client = webSocketClient;
    }

    /**
     * Add listener for inbound messages implementing Shelly2RpctInterface
     *
     * @param interfacehandler
     */
    public synchronized void addMessageHandler(Shelly2RpctInterface interfacehandler) {
        this.websocketHandler = interfacehandler;
    }

    /**
     * Connect outbound Web Socket.
     *
     * @throws ShellyApiException
     *             NOTE: sendQueue is NOT preserved across reconnects; it is cleared on any disconnect/close/error.
     */
    public void connect() throws ShellyApiException {
        String deviceIp = this.deviceIp;
        if (deviceIp.isBlank()) {
            throw new ShellyApiException(thingName + ": Device IP not set");
        }

        // Prepare connect
        URI uri;
        try {
            uri = new URI("ws://" + deviceIp + SHELLYRPC_ENDPOINT);
        } catch (URISyntaxException e) {
            throw new ShellyApiException("Invalid URI: " + e.getMessage(), e);
        }
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader(HttpHeaders.HOST, deviceIp);
        request.setHeader("Origin", "http://" + deviceIp);
        request.setHeader("Pragma", "no-cache");
        request.setHeader("Cache-Control", "no-cache");

        if (logger.isTraceEnabled()) {
            logger.trace("{}: Connect WebSocket, URI={}", thingName, uri);
        }

        // Start connecting the WebSocket session (result will be passed to onConnect()/onError())
        synchronized (this) {
            disconnect(); // for safety

            // Connect async
            try {
                client.connect(this, uri, request);
            } catch (RuntimeException | IOException e) {
                // Keep this if your Jetty version still declares IOException on start()/connect path
                throw new ShellyApiException("Failed to connect WebSocket: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Web Socket is connected, get thing handler and send already queued messages.
     *
     * @param session Newly created WebSocket connection
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (session.getRemoteAddress() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Invalid inbound WebSocket connect (invalid remote ip)", thingName);
            }
            session.close(StatusCode.ABNORMAL, "Invalid remote IP");
            return;
        }

        String deviceIp = this.deviceIp;
        if (deviceIp.isEmpty()) {
            // This is the inbound event web socket
            this.deviceIp = deviceIp = session.getRemoteAddress().getAddress().getHostAddress();
        }

        // It's a bit wasteful to retrieve the ThingInterface even if we already have a handler, but we can't call
        // thingTable.getThing() while holding a lock safely, which is why it's done "in case it's needed" before
        // the lock is acquired.
        ShellyThingInterface thing;
        try {
            thing = thingTable.getThing(deviceIp);
        } catch (IllegalArgumentException e) { // unknown thing
            logger.debug("{}: RPC connection error for {} (unknown/disabled thing? - {}), closing socket", thingName,
                    deviceIp, e.getMessage());
            session.close(StatusCode.SHUTDOWN, "Thing not active");
            return;
        }

        Shelly2RpctInterface handler;
        List<String> queue = null;
        synchronized (this) {
            handler = websocketHandler;
            this.session = session;
            if (handler == null) {
                Shelly2ApiRpc api = (Shelly2ApiRpc) thing.getApi();
                handler = api.getRpcHandler();
                websocketHandler = handler;
            }

            if (!sendQueue.isEmpty()) {
                queue = List.copyOf(sendQueue);
                sendQueue.clear();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{}: WebSocket connected {}<-{}, Idle Timeout={}", thingName, session.getLocalAddress(),
                    session.getRemoteAddress(), session.getIdleTimeout());
        }
        handler.onConnect(deviceIp, true);

        if (queue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Sending {} queued RPC request{}", thingName, queue.size(),
                        queue.size() > 1 ? "s" : "");
            }
            RemoteEndpoint remote = session.getRemote();
            for (String msg : queue) {
                remote.sendString(msg, this);
            }
        }
    }

    /**
     * Send request over WebSocket.
     * Queueing policy (simple):
     * - If not connected: enqueue and return.
     * - If connected: flush queued snapshot (if any), then send this message.
     * - If send fails: this is an error; do NOT requeue.
     *
     * @param str API request message
     */
    public void sendMessage(String str) {
        Session session;
        List<String> queue = null;
        synchronized (this) {
            session = this.session;

            if (session == null || !session.isOpen()) {
                this.sendQueue.add(str);
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: Queued RPC request (no open session): {}", thingName, str);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("{}: Queued RPC request (no open session)", thingName);
                }
                return;
            }

            if (!this.sendQueue.isEmpty()) {
                queue = List.copyOf(this.sendQueue);
                this.sendQueue.clear();
            }
        }

        RemoteEndpoint remote = session.getRemote();
        if (queue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Sending {} queued API request{}", thingName, queue.size(),
                        queue.size() > 1 ? "s" : "");
            }
            for (String queued : queue) {
                remote.sendString(queued, this);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("{}: Sending RPC message {}", thingName, str);
        }
        remote.sendString(str, this);
    }

    /**
     * Close WebSocket session and clean up.
     * <p>
     * Clears {@code sendQueue} (NOT preserved across reconnects).
     */
    public void disconnect() {
        Session session;
        synchronized (this) {
            session = this.session;
            cleanup();// set session=null, clear send queue
        }
        if (session != null && session.isOpen()) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}: Closing WebSocket session ({} -> {})", thingName, session.getLocalAddress(),
                        session.getRemoteAddress());
            }
            session.close(StatusCode.NORMAL, "Socket closed");
        }
    }

    /**
     * Process Inbound WebSocket message
     *
     * @param session WebSocket session
     * @param receivedMessage Textual API message
     */
    @OnWebSocketMessage
    public void onText(Session session, String receivedMessage) {
        Shelly2RpctInterface handler;
        synchronized (this) {
            handler = websocketHandler;
        }
        try {
            Shelly2RpcBaseMessage message = fromJson(gson, receivedMessage, Shelly2RpcBaseMessage.class);
            if (logger.isTraceEnabled()) {
                logger.trace("{}: Inbound RPC message: {}", thingName, receivedMessage);
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
                            logger.debug("{}: Malformed event data: {}", thingName, receivedMessage);
                        } else {
                            for (Shelly2NotifyEvent e : events.params.events) {
                                if (getString(e.event).startsWith(SHELLY2_EVENT_BLUPREFIX)) {
                                    String address = getString(e.blu != null ? e.blu.addr : "").replace(":", "");
                                    if (thingTable.findThing(address) != null) {
                                        // known device
                                        ShellyThingInterface thing = thingTable.getThing(address);
                                        Shelly2ApiRpc api = (Shelly2ApiRpc) thing.getApi();
                                        handler = api.getRpcHandler();
                                        handler.onNotifyEvent(receivedMessage);
                                    } else {
                                        // new device
                                        if (SHELLY2_EVENT_BLUSCAN.equals(e.event)) {
                                            addBluThing(message.src, e.blu, thingTable);
                                        } else {
                                            logger.debug(
                                                    "{}: NotifyEvent {} for unknown BLU device {} or Thing in Inbox",
                                                    message.src, e.event, e.blu.addr);
                                        }
                                    }
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
                    logger.debug("{}: No RPC listener registered for device {}, skip message: {}", thingName,
                            getString(message.src), receivedMessage);
                }
            }
        } catch (ShellyApiException | IllegalArgumentException e) {
            logger.debug("{}: Unable to process Rpc message ({}): {}", thingName, e.getMessage(), receivedMessage);
        }
    }

    public synchronized boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    public boolean isInbound() {
        return inbound;
    }

    /**
     * WebSocket closed, notify thing handler (close initiated by the binding)
     *
     * @param statusCode StatusCode
     * @param reason Textual reason
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode != StatusCode.NORMAL && logger.isTraceEnabled()) {
            logger.trace("{}: RPC connection closed abnormally: {} - {}", thingName, statusCode, getString(reason));
        }

        Shelly2RpctInterface handler;
        synchronized (this) {
            handler = this.websocketHandler;

            // set session=null, clear send queue
            // this also prevents another socket closed issued by thingOffline()->api-close()->close()
            cleanup();
        }

        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        if (handler != null) {
            handler.onClose(statusCode, reason);
        }
    }

    /**
     * Callback for WebSocket error (disconnect initiated by remote).
     *
     * @param cause WebSocket error/Exception
     */
    @OnWebSocketError
    public void onError(Throwable cause) {
        Shelly2RpctInterface websocketHandler;
        synchronized (this) {
            websocketHandler = this.websocketHandler;

            // set session=null, clear send queue
            // this also prevents another socket closed issued by thingOffline()->api-close()->close()
            cleanup();
        }

        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }

    /**
     * Clears session and drops queued messages.
     * Must only be called when session has been/is being closed one way or another.
     */
    private void cleanup() {
        int qLength;
        synchronized (this) {
            this.session = null;

            qLength = sendQueue.size();
            sendQueue.clear();
        }
        if (logger.isDebugEnabled() && qLength > 0) {
            logger.debug("{}: {} queued RPC message{} dropped, because the socket was closed", thingName, qLength,
                    qLength != 1 ? "s were" : " was");
        }
    }

    /**
     * Asynchronous write completed with error
     */
    @Override
    public void writeFailed(@Nullable Throwable x) {
        if (logger.isDebugEnabled()) {
            if (x == null) {
                logger.debug("{}: Sending RPC Message failed", thingName);
            } else {
                logger.debug("{}: Sending RPC Message failed: {}", thingName, x.getMessage(), x);
            }
        }
    }

    /**
     * Asynchronous write completed with success
     */
    @Override
    public void writeSuccess() {
        // Nothing to do
    }

    /**
     * Create and configures a new {@link WebSocketClient}.
     *
     * @param webSocketFactory the {@link WebSocketFactory} to use to create the new client instance.
     * @param consumerName the name for identifying the consumer in the Jetty thread pool.
     *            Must be between 4 and 20 characters long and must contain only the following characters [a-zA-Z0-9-_]
     */
    public static WebSocketClient createWebSocketClient(WebSocketFactory webSocketFactory, String consumerName) {
        WebSocketClient client = webSocketFactory.createWebSocketClient(consumerName);
        client.setConnectTimeout(SHELLY_API_TIMEOUT_MS);
        client.setStopTimeout(1000);
        return client;
    }
}
