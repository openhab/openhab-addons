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
@WebSocket(maxIdleTime = Integer.MAX_VALUE)
public class Shelly2RpcSocket implements WriteCallback {
    private final Logger logger = LoggerFactory.getLogger(Shelly2RpcSocket.class);
    private final Gson gson = new Gson();

    private volatile String thingName = "";
    private volatile String deviceIp = "";
    private final boolean inbound;

    // All access must be guarded by "this"
    private @Nullable Session session;

    // All access must be guarded by "this"
    private final List<String> sendQueue = new ArrayList<>();

    // All access must be guarded by "this"
    private @Nullable Shelly2RpctInterface websocketHandler;

    // All access must be guarded by "this"
    private @Nullable WebSocketClient client;

    private final ShellyThingTable thingTable;

    public Shelly2RpcSocket(String thingName, ShellyThingTable thingTable, String deviceIp) {
        this.thingName = thingName;
        this.deviceIp = deviceIp;
        this.thingTable = thingTable;
        inbound = false;
    }

    public Shelly2RpcSocket(ShellyThingTable thingTable, boolean inbound) {
        this.thingTable = thingTable;
        this.inbound = inbound;
    }

    public synchronized void addMessageHandler(Shelly2RpctInterface interfacehandler) {
        this.websocketHandler = interfacehandler;
    }

    /**
     * Connect outbound Web Socket.
     *
     * NOTE: sendQueue is NOT preserved across reconnects; it is cleared on any disconnect/close/error.
     */
    public void connect() throws ShellyApiException {
        try {
            // Close any previous session/client (also clears queue per requirement).
            disconnect();

            final String ip = this.deviceIp;
            if (ip.isBlank()) {
                throw new IllegalArgumentException(thingName + ": Device IP not set");
            }

            final URI uri = new URI("ws://" + ip + SHELLYRPC_ENDPOINT);

            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(HttpHeaders.HOST, ip);
            request.setHeader("Origin", "http://" + ip);
            request.setHeader("Pragma", "no-cache");
            request.setHeader("Cache-Control", "no-cache");

            if (logger.isTraceEnabled()) {
                logger.trace("{}: Connect WebSocket, URI={}", thingName, uri);
            }

            final WebSocketClient newClient = new WebSocketClient();
            newClient.setConnectTimeout(SHELLY_API_TIMEOUT_MS);
            newClient.setStopTimeout(1000);
            newClient.start();
            synchronized (this) {
                this.client = newClient;
            }

            // Connect (async); errors after this are delivered via onError/onClose
            newClient.connect(this, uri, request);
        } catch (URISyntaxException e) {
            throw new ShellyApiException("Invalid URI: " + e.getMessage(), e);
        } catch (IOException e) {
            // Keep this if your Jetty version still declares IOException on start()/connect path
            throw new ShellyApiException("Failed to connect WebSocket: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            // e.g. client already started/stopped, misuse of lifecycle
            throw new ShellyApiException("WebSocket client state error: " + e.getMessage(), e);
        } catch (Exception e) {
            // last-resort for unexpected runtime issues without catching plain Exception
            throw new ShellyApiException("Failed to start WebSocket: " + e.getMessage(), e);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (session.getRemoteAddress() == null) {
            logger.debug("{}: Invalid inbound WebSocket connect (invalid remote ip)", thingName);
            session.close(StatusCode.ABNORMAL, "Invalid remote IP");
            return;
        }

        String ip = this.deviceIp;
        if (ip.isEmpty()) {
            ip = session.getRemoteAddress().getAddress().getHostAddress();
            this.deviceIp = ip;
        }

        final ShellyThingInterface thing;
        try {
            thing = thingTable.getThing(ip);
        } catch (IllegalArgumentException e) {
            logger.debug("{}: RPC connection error for {} (unknown/disabled thing? - {}), closing socket", thingName,
                    ip, e.getMessage());
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
        handler.onConnect(ip, true);

        if (queue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Sending {} queued API request(s)", thingName, queue.size());
            }
            final RemoteEndpoint remote = session.getRemote();
            try {
                for (String msg : queue) {
                    remote.sendString(msg, this);
                }
            } catch (IllegalStateException | WebSocketException e) {
                logger.debug("{}: Failed to send queued RPC messages on connect, {} discarded: {}", thingName,
                        queue.size(), e.toString());
            }
        }
    }

    /**
     * Send request over WebSocket.
     *
     * Queueing policy (simple):
     * - If not connected: enqueue and return.
     * - If connected: flush queued snapshot (if any), then send this message.
     * - If send fails: this is an error; do NOT requeue.
     */
    public void sendMessage(String str) throws ShellyApiException {
        final Session session;
        List<String> queue = null;
        synchronized (this) {
            session = this.session;

            if (session == null || !session.isOpen()) {
                this.sendQueue.add(str);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: Queued API request (no open session): {}", thingName, str);
                }
                return;
            }

            if (!this.sendQueue.isEmpty()) {
                queue = List.copyOf(this.sendQueue);
                this.sendQueue.clear();
            }
        }

        if (queue != null && logger.isTraceEnabled()) {
            logger.trace("{}: Sending {} queued RPC message{}", thingName, queue.size(), queue.size() != 1 ? "s" : "");
        }
        final RemoteEndpoint remote = session.getRemote();
        try {
            if (queue != null) {
                for (String queued : queue) {
                    remote.sendString(queued, this);
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace("{}: Sending RPC message {}", thingName, str);
            }
            remote.sendString(str, this);

        } catch (IllegalStateException | WebSocketException e) {
            logger.debug("{}: Failed to send RPC message: {}", thingName, e.toString());
            throw new ShellyApiException("Failed to send RPC message: " + e.getMessage(), e);
        }
    }

    /**
     * Close WebSocket session and stop the WebSocketClient.
     * Clears sendQueue (NOT preserved across reconnects).
     */
    public void disconnect() {
        final Session session;
        final WebSocketClient client;
        synchronized (this) {
            session = this.session;
            client = this.client;
        }

        cleanup();// set session+client=null, clear send queue

        try {
            if (session != null && session.isOpen()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: Closing WebSocket session ({} -> {})", thingName, session.getLocalAddress(),
                            session.getRemoteAddress());
                }
                session.close(StatusCode.NORMAL, "Socket closed");
            }
        } catch (IllegalStateException | WebSocketException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Unable to close WebSocket session", thingName, e);
            }
        }

        try {
            if (client != null && client.isRunning()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{}: Stopping WebSocket client", thingName);
                }
                client.stop();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: Unable to stop WebSocket client", thingName, e);
            }
        }
    }

    /**
     * Process inbound Notify message
     *
     * @param session WebSocket
     * @param receivedMessage Inbound event data
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
                            logger.debug("{}: Malformed event data: {}", thingName, receivedMessage);
                        } else {
                            for (Shelly2NotifyEvent e : events.params.events) {
                                if (getString(e.event).startsWith(SHELLY2_EVENT_BLUPREFIX)) {
                                    String address = getString(e.blu != null ? e.blu.addr : "").replace(":", "");
                                    if (thingTable.findThing(address) != null) {
                                        ShellyThingInterface t = thingTable.getThing(address);
                                        Shelly2ApiRpc api = (Shelly2ApiRpc) t.getApi();
                                        handler = api.getRpcHandler();
                                        handler.onNotifyEvent(receivedMessage);
                                    } else {
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
                logger.debug("{}: No Rpc listener registered for device {}, skip message: {}", thingName,
                        getString(message.src), receivedMessage);
            }
        } catch (ShellyApiException | IllegalArgumentException e) {
            logger.debug("{}: Unable to process Rpc message ({}): {}", thingName, e.getMessage(), receivedMessage);
        }
    }

    public synchronized boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    /**
     * Callback handler on regular close (initiated by the binding)
     *
     * @param statusCode
     * @param reason
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode != StatusCode.NORMAL && logger.isTraceEnabled()) {
            logger.trace("{}: Rpc connection closed abnormal: {} - {}", thingName, statusCode, getString(reason));
        }

        final Shelly2RpctInterface handler;
        synchronized (this) {
            handler = this.websocketHandler;
        }

        cleanup(); // set session+client=null, clear send queue

        if (inbound) {
            // Ignore disconnect: Device establishes the socket, sends NotifyxFullStatus and disconnects
            return;
        }
        if (handler != null) {
            handler.onClose(statusCode, reason);
        }
    }

    /**
     * Callback for unexpected close (initiated by remote device)
     *
     * @param cause
     */
    @OnWebSocketError
    public void onError(Throwable cause) {
        final Shelly2RpctInterface handler;
        synchronized (this) {
            handler = this.websocketHandler;
        }
        cleanup(); // set session+client=null, clear send queue

        if (inbound) {
            return;
        }
        if (handler != null) {
            handler.onError(cause);
        }
    }

    /**
     * Clears session/client and drops queued messages.
     * Must be called only while holding synchronized(this).
     */
    private synchronized void cleanup() {
        this.session = null;
        this.client = null;

        int qLength = sendQueue.size();
        sendQueue.clear();
        if (qLength > 0) {
            logger.debug("{}: {} queued RPC messages dropped, because socket is closing", thingName, qLength);
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
}
