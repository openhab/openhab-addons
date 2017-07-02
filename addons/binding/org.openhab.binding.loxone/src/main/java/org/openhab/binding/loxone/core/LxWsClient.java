/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.loxone.core.LxServer.OfflineReason;
import org.openhab.binding.loxone.core.LxServerEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Websocket client facilitating communication with Loxone Miniserver.
 * This client is implemented as a state machine, according to guidelines in Loxone API documentation.
 * It uses jetty websocket client and creates one own thread to send keep-alive messages to the Miniserver.
 *
 * @author Pawel Pieczul - initial commit
 *
 */
class LxWsClient {
    private final InetAddress host;
    private final int port;
    private final String user;
    private final String password;
    private final int debugId;
    private long keepAlivePeriod = 240; // 4 minutes, server timeout is 5 minutes
    private long connectTimeout = 4; // 4 seconds to wait for connection response
    private int maxBinMsgSize = 3 * 1024; // 3 MB
    private int maxTextMsgSize = 512; // 512 KB

    private ScheduledFuture<?> timeout = null;
    private LxWebSocket socket = null;
    private WebSocketClient wsClient = null;
    private BlockingQueue<LxServerEvent> queue;
    private ClientState state = ClientState.IDLE;
    private Logger logger = LoggerFactory.getLogger(LxWsClient.class);

    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager
            .getScheduledPool(LxWsClient.class.getName());

    private static final String SOCKET_URL = "/ws/rfc6455";
    private static final String CMD_ACTION = "jdev/sps/io/";
    private static final String CMD_GET_KEY = "jdev/sys/getkey";
    private static final String CMD_AUTHENTICATE = "authenticate/";
    private static final String CMD_KEEPALIVE = "keepalive";
    private static final String CMD_ENABLE_UPDATES = "jdev/sps/enablebinstatusupdate";
    private static final String CMD_GET_APP_CONFIG = "data/LoxAPP3.json";

    /**
     * Internal state of the websocket client.
     *
     * @author Pawel Pieczul - initial commit
     *
     */
    private enum ClientState {
        /**
         * Waiting for connection request
         */
        IDLE,
        /**
         * Connection requested, waiting for confirmation
         */
        CONNECTING,
        /**
         * Connection confirmed and established
         */
        CONNECTED,
        /**
         * Waiting for authentication
         */
        AUTHENTICATING,
        /**
         * Waiting for Miniserver's configuration
         */
        UPDATING_CONFIGURATION,
        /**
         * Ready to send commands and receive state updates
         */
        RUNNING,
        /**
         * Received internal request to shutdown
         */
        CLOSING
    }

    /**
     * Type of a binary message received from the Miniserver
     *
     * @author Pawel Pieczul
     *
     */
    enum MessageType {
        /**
         * Text message - jetty websocket client will pass it on automatically to a callback
         */
        TEXT_MESSAGE,
        /**
         * Binary file
         */
        BINARY_FILE,
        /**
         * A set of value states for controls that changed their state
         */
        EVENT_TABLE_OF_VALUE_STATES,
        /**
         * A set of text states for controls that changed their state
         */
        EVENT_TABLE_OF_TEXT_STATES,
        EVENT_TABLE_OF_DAYTIMER_STATES,
        OUT_OF_SERVICE_INDICATOR,
        /**
         * Response to keepalive request message
         */
        KEEPALIVE_RESPONSE,
        EVENT_TABLE_OF_WEATHER_STATES,
        /**
         * Unknown header
         */
        UNKNOWN
    }

    /**
     * A header of a binary message received from Loxone Miniserver on a websocket connection.
     *
     * @author Pawel Pieczul - initial commit
     *
     */
    private class LxWsBinaryHeader {
        @SuppressWarnings("unused")
        int length;
        @SuppressWarnings("unused")
        boolean estimated;
        MessageType type = MessageType.UNKNOWN;

        /**
         * Create header from binary buffer at a given offset
         *
         * @param buffer
         *            buffer with received message
         * @param offset
         *            offset in bytes at which header is expected
         */
        LxWsBinaryHeader(byte[] buffer, int offset) throws IndexOutOfBoundsException {
            if (buffer.length - offset != 8) {
                throw new IndexOutOfBoundsException();
            }
            if (buffer[offset] != 0x03) {
                return;
            }
            switch (buffer[offset + 1]) {
                case 0:
                    type = MessageType.TEXT_MESSAGE;
                    break;
                case 1:
                    type = MessageType.BINARY_FILE;
                    break;
                case 2:
                    type = MessageType.EVENT_TABLE_OF_VALUE_STATES;
                    break;
                case 3:
                    type = MessageType.EVENT_TABLE_OF_TEXT_STATES;
                    break;
                case 4:
                    type = MessageType.EVENT_TABLE_OF_DAYTIMER_STATES;
                    break;
                case 5:
                    type = MessageType.OUT_OF_SERVICE_INDICATOR;
                    break;
                case 6:
                    type = MessageType.KEEPALIVE_RESPONSE;
                    break;
                case 7:
                    type = MessageType.EVENT_TABLE_OF_WEATHER_STATES;
                    break;
                default:
                    type = MessageType.UNKNOWN;
                    break;
            }
            estimated = ((buffer[offset + 2] & 0x01) != 0);
            length = ByteBuffer.wrap(buffer, offset + 3, 4).getInt();
        }
    }

    /**
     * Create websocket client object
     *
     * @param debugId
     *            instance of the client used for debugging purposes only
     * @param queue
     *            message queue to communicate with its master {@link LxServer}, must be already initialized
     * @param host
     *            Miniserver's host address
     * @param port
     *            Miniserver's web services port
     * @param user
     *            user to authenticate
     * @param password
     *            password to authenticate
     */
    LxWsClient(int debugId, BlockingQueue<LxServerEvent> queue, InetAddress host, int port, String user,
            String password) {
        this.debugId = debugId;
        this.queue = queue;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    /**
     * Connect the websocket.
     * Attempts to connect to the websocket on a remote Miniserver.
     *
     * @return
     *         true if connection request initiated correctly, false if not
     */
    boolean connect() {
        logger.trace("[{}] connect() websocket", debugId);
        if (state != ClientState.IDLE) {
            close("Attempt to connect a websocket in non-idle state: " + state.toString());
            return false;
        }

        synchronized (state) {
            socket = new LxWebSocket();
            wsClient = new WebSocketClient();

            try {
                wsClient.start();

                URI target = new URI("ws://" + host.getHostAddress() + ":" + port + SOCKET_URL);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setSubProtocols("remotecontrol");

                wsClient.connect(socket, target, request);
                setClientState(ClientState.CONNECTING);
                startResponseTimeout();

                logger.debug("[{}] Connecting to server : {} ", debugId, target);
                return true;

            } catch (Exception e) {
                setClientState(ClientState.IDLE);
                close("Connection to websocket failed : " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Disconnect from the websocket with provided reason. This method is called from {@link LxServer} level and also
     * from unsuccessful connect attempt.
     * After calling this method, client is ready to perform a new connection request with {@link #connect()}.
     *
     * @param reason
     *            text describing reason for disconnection
     */
    private void disconnect(String reason) {
        logger.trace("[{}] disconnect() websocket : {}", debugId, reason);
        synchronized (this) {
            if (wsClient != null) {
                try {
                    close(reason);
                    wsClient.stop();
                    wsClient = null;
                } catch (Exception e) {
                    logger.debug("[{}] Failed to stop websocket client, message = {}", debugId, e.getMessage());
                }
            } else {
                logger.debug("[{}] Attempt to disconnect websocket client, but wsClient == null", debugId);
            }
        }
    }

    /**
     * Disconnect from the websocket.
     * After calling this method, client is ready to perform a new connection request with {@link #connect()}.
     */
    void disconnect() {
        disconnect("Disconnecting websocket client");
    }

    /**
     * Close websocket session from within {@link LxWsClient}, without stopping the client.
     * To close session from {@link LxServer} level, use {@link #disconnect()}
     *
     * @param reason
     *            reason for closing the websocket
     */
    private void close(String reason) {
        logger.trace("[{}] close() websocket", debugId);
        synchronized (state) {
            stopResponseTimeout();
            if (socket != null) {
                if (socket.session != null) {
                    if (state != ClientState.IDLE) {
                        logger.debug("[{}] Closing websocket session, reason : {}", debugId, reason);
                        setClientState(ClientState.CLOSING);
                    }
                    socket.session.close(StatusCode.NORMAL, reason);
                } else {
                    logger.debug("[{}] Closing websocket, but no session, reason : {}", debugId, reason);
                    setClientState(ClientState.IDLE);
                }
            } else {
                logger.debug("[{}] Closing websocket, but socket = null", debugId);
            }
        }
    }

    /**
     * Notify {@link LxServer} about server going offline and close websocket session from within {@link LxWsClient},
     * without stopping the client.
     * To close session from {@link LxServer} level, use {@link #disconnect()}
     *
     * @param reasonCode
     *            reason code for server going offline
     * @param reasonText
     *            reason text (description) for server going offline
     */
    private void notifyAndClose(OfflineReason reasonCode, String reasonText) {
        notifyMaster(EventType.SERVER_OFFLINE, reasonCode, reasonText);
        close(reasonText);
    }

    /**
     * Update configuration parameter(s) in runtime.
     * Calling this method will not interrupt existing services, changes will take effect when new values are used next
     * time.
     *
     * @param keepAlivePeriod
     *            new period between keep alive messages, in seconds
     * @param connectTimeout
     *            Time to wait for websocket connect response from the Miniserver
     * @param maxBinMsgSize
     *            maximum binary message size of websocket client (in kB)
     * @param maxTextMsgSize
     *            maximum text message size of websocket client (in kB)
     */
    void update(int keepAlivePeriod, int connectTimeout, int maxBinMsgSize, int maxTextMsgSize) {
        if (keepAlivePeriod > 0 && this.keepAlivePeriod != keepAlivePeriod) {
            logger.debug("[{}] Changing keepAlivePeriod to {}", debugId, keepAlivePeriod);
            this.keepAlivePeriod = keepAlivePeriod;
        }
        if (connectTimeout > 0 && this.connectTimeout != connectTimeout) {
            logger.debug("[{}] Changing connectTimeout to {}", debugId, connectTimeout);
            this.connectTimeout = connectTimeout;
        }
        if (maxBinMsgSize > 0 && this.maxBinMsgSize != maxBinMsgSize) {
            logger.debug("[{}] Changing maxBinMsgSize to {}", debugId, maxBinMsgSize);
            this.maxBinMsgSize = maxBinMsgSize;
        }
        if (maxTextMsgSize > 0 && this.maxTextMsgSize != maxTextMsgSize) {
            logger.debug("[{}] Changing maxTextMsgSize to {}", debugId, maxTextMsgSize);
            this.maxTextMsgSize = maxTextMsgSize;
        }
    }

    /**
     * Sends an action to a Loxone Miniserver's control.
     *
     * @param id
     *            identifier of the control
     * @param operation
     *            identifier of the operation
     * @throws IOException
     *             when communication error with Miniserver occurs
     * @throws JsonSyntaxException
     *             when server returns data which are not understood
     */
    void sendAction(LxUuid id, String operation) throws IOException {
        String command = CMD_ACTION + id.getOriginalString() + "/" + operation;
        logger.debug("[{}] Sending command {}", debugId, command);
        socket.sendString(command);
    }

    /**
     * Sets a new websocket client state
     *
     * @param state
     *            new state to set
     */
    private void setClientState(LxWsClient.ClientState state) {
        logger.debug("[{}] changing client state to: {}", debugId, state.toString());
        this.state = state;
    }

    /**
     * Start a timer to wait for a Miniserver response to an action sent from the binding.
     * When timer expires, connection is removed and server error is reported. Further connection attempt can be made
     * later by the upper layer.
     * If a previous time is running, it will be stopped before a new timer is started.
     */
    private void startResponseTimeout() {
        synchronized (state) {
            stopResponseTimeout();
            timeout = SCHEDULER.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (state) {
                        logger.debug("[{}] Miniserver response timeout", debugId);
                        notifyMaster(EventType.SERVER_OFFLINE, OfflineReason.COMMUNICATION_ERROR,
                                "Miniserver response timeout occured");
                        disconnect();
                    }
                }
            }, connectTimeout, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops scheduled timeout waiting for a Miniserver response
     */
    private void stopResponseTimeout() {
        logger.trace("[{}] stopping response timeout in state {}", debugId, state.toString());
        synchronized (state) {
            if (timeout != null) {
                timeout.cancel(true);
                timeout = null;
            }
        }
    }

    /**
     * Sends an event to {@link LxServer} object
     *
     * @param event
     *            event that happened
     * @param reason
     *            reason for the event (applicable to server OFFLINE event}
     * @param object
     *            additional data for the event (text message for OFFLINE event, data for state changes)
     */
    private void notifyMaster(EventType event, OfflineReason reason, Object object) {
        if (reason == null) {
            reason = OfflineReason.NONE;
        }
        LxServerEvent sync = new LxServerEvent(event, reason, object);
        try {
            queue.put(sync);
        } catch (InterruptedException e) {
            logger.debug("[{}] Interrupted queue operation", debugId);
        }
    }

    /**
     * Implementation of jetty websocket client
     *
     * @author Pawel Pieczul - initial commit
     *
     */
    @WebSocket
    public class LxWebSocket {
        Session session;
        private ScheduledFuture<?> keepAlive = null;
        private LxWsBinaryHeader header = null;

        @OnWebSocketConnect
        public void onConnect(Session session) {
            synchronized (state) {
                if (state != ClientState.CONNECTING) {
                    logger.debug("[{}] Unexpected connect received on websocket in state {}", debugId,
                            state.toString());
                    return;
                }

                if (timeout != null) {
                    timeout.cancel(true);
                    timeout = null;
                }

                WebSocketPolicy policy = session.getPolicy();
                policy.setMaxBinaryMessageSize(maxBinMsgSize * 1024);
                policy.setMaxTextMessageSize(maxTextMsgSize * 1024);

                logger.debug("[{}] Websocket connected (maxBinMsgSize={}, maxTextMsgSize={})", debugId,
                        policy.getMaxBinaryMessageSize(), policy.getMaxTextMessageSize());
                this.session = session;
                setClientState(ClientState.CONNECTED);

                try {
                    sendString(CMD_GET_KEY);
                } catch (IOException e) {
                    notifyAndClose(OfflineReason.COMMUNICATION_ERROR,
                            "Message send failure on recently connected websocket");
                }
            }
            keepAlive = SCHEDULER.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    synchronized (state) {
                        if (state == ClientState.CLOSING || state == ClientState.IDLE
                                || state == ClientState.CONNECTING) {
                            stopKeepAlive();
                        } else {
                            try {
                                logger.debug("[{}] sending keepalive message", debugId);
                                sendString(CMD_KEEPALIVE);
                            } catch (IOException e) {
                                logger.debug("[{}] error sending keepalive message", debugId);
                            }
                        }
                    }
                }
            }, keepAlivePeriod, keepAlivePeriod, TimeUnit.SECONDS);
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            synchronized (state) {
                logger.debug("[{}] Websocket connection in state {} closed with code {} reason : {}", debugId,
                        state.toString(), statusCode, reason);
                stopKeepAlive();
                if (state == ClientState.CLOSING) {
                    session = null;
                } else if (state != ClientState.IDLE) {
                    OfflineReason reasonCode;
                    if (statusCode == 1001) {
                        reasonCode = OfflineReason.IDLE_TIMEOUT;
                    } else if (statusCode == 4003) {
                        reasonCode = OfflineReason.TOO_MANY_FAILED_LOGIN_ATTEMPTS;
                    } else {
                        reasonCode = OfflineReason.COMMUNICATION_ERROR;
                    }
                    notifyMaster(EventType.SERVER_OFFLINE, reasonCode, reason);
                }
                setClientState(ClientState.IDLE);
            }
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            logger.debug("[{}] Websocket error : {}", debugId, error.getMessage());
        }

        @OnWebSocketMessage
        public void onBinaryMessage(byte data[], int offset, int length) {

            if (logger.isTraceEnabled()) {
                String s = Hex.encodeHexString(data);
                logger.trace("[{}] Binary message: length {}: {}", debugId, length, s);
            }
            synchronized (state) {
                if (state != ClientState.RUNNING) {
                    return;
                }

                try {
                    // websocket will receive header and data in turns as two separate binary messages
                    if (header == null) {
                        // header expected now
                        header = new LxWsBinaryHeader(data, offset);
                        switch (header.type) {
                            // following header types precede data in next message
                            case BINARY_FILE:
                            case EVENT_TABLE_OF_VALUE_STATES:
                            case EVENT_TABLE_OF_TEXT_STATES:
                            case EVENT_TABLE_OF_DAYTIMER_STATES:
                            case EVENT_TABLE_OF_WEATHER_STATES:
                                break;
                            // other header types have no data and next message will be header again
                            default:
                                header = null;
                                break;
                        }
                    } else {
                        // data expected now
                        switch (header.type) {
                            case EVENT_TABLE_OF_VALUE_STATES:
                                stopResponseTimeout();
                                while (length > 0) {
                                    LxWsStateUpdateEvent event = new LxWsStateUpdateEvent(true, data, offset);
                                    offset += event.getSize();
                                    length -= event.getSize();
                                    notifyMaster(EventType.STATE_UPDATE, null, event);
                                }
                                break;
                            case EVENT_TABLE_OF_TEXT_STATES:
                                while (length > 0) {
                                    LxWsStateUpdateEvent event = new LxWsStateUpdateEvent(false, data, offset);
                                    offset += event.getSize();
                                    length -= event.getSize();
                                    notifyMaster(EventType.STATE_UPDATE, null, event);
                                }
                                break;
                            case KEEPALIVE_RESPONSE:
                            case TEXT_MESSAGE:
                            default:
                                break;
                        }
                        // header will be next
                        header = null;
                    }
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("[{}] malformed binary message received, discarded", debugId);
                }
            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            if (logger.isTraceEnabled()) {
                String trace = msg;
                if (trace.length() > 100) {
                    trace = msg.substring(0, 100);
                }
                logger.trace("[{}] received message in state {}: {}", debugId, state.toString(), trace);
            }
            synchronized (state) {
                try {
                    LxJsonResponse resp;
                    switch (state) {
                        case IDLE:
                        case CONNECTING:
                            logger.debug("[{}] Unexpected message received by websocket in state {}", debugId,
                                    state.toString());
                            break;
                        case CONNECTED:
                            // expecting a key to hash credentials
                            resp = new Gson().fromJson(msg, LxJsonResponse.class);
                            if (resp.LL.Code == 420) {
                                notifyAndClose(OfflineReason.AUTHENTICATION_TIMEOUT,
                                        "Timeout on authentication procedure, response : " + resp.LL.value);
                            } else if (resp.LL.control.equals(CMD_GET_KEY) && resp.LL.Code == 200) {
                                String credentials = hashCredentials(resp.LL.value);
                                if (credentials != null) {
                                    sendString(CMD_AUTHENTICATE + credentials);
                                    setClientState(ClientState.AUTHENTICATING);
                                    startResponseTimeout();
                                } else {
                                    notifyAndClose(OfflineReason.INTERNAL_ERROR, "Error creating credentials");
                                }
                            }
                            break;
                        case AUTHENTICATING:
                            resp = new Gson().fromJson(msg, LxJsonResponse.class);
                            if (resp.LL.Code == 401) {
                                notifyAndClose(OfflineReason.UNAUTHORIZED, "Websocket credentials unauthorized.");
                            } else if (resp.LL.Code == 420) {
                                notifyAndClose(OfflineReason.AUTHENTICATION_TIMEOUT,
                                        "Timeout on authentication procedure, response : " + resp.LL.value);
                            } else if (resp.LL.Code == 200) {
                                logger.debug("[{}] Websocket authentication successfull.", debugId);
                                sendString(CMD_GET_APP_CONFIG);
                                setClientState(ClientState.UPDATING_CONFIGURATION);
                                startResponseTimeout();
                            }
                            break;
                        case UPDATING_CONFIGURATION:
                            LxJsonApp3 config = new Gson().fromJson(msg, LxJsonApp3.class);
                            if (config != null) {
                                logger.debug("[{}] Received configuration from server", debugId);
                                notifyMaster(EventType.RECEIVED_CONFIG, null, config);
                                sendString(CMD_ENABLE_UPDATES);
                                setClientState(ClientState.RUNNING);
                                startResponseTimeout();
                                notifyMaster(EventType.SERVER_ONLINE, null, null);
                            } else {
                                notifyAndClose(OfflineReason.INTERNAL_ERROR, "Error processing received configuration");
                            }
                            break;
                        case RUNNING:
                        case CLOSING:
                        default:
                            break;
                    }
                } catch (IOException e) {
                    notifyAndClose(OfflineReason.COMMUNICATION_ERROR,
                            "Communication error when processing message : " + e.getMessage());
                }
            }
        }

        /**
         * Stops keep alive thread and ceases sending keep alive messages to the Miniserver
         */
        private void stopKeepAlive() {
            logger.trace("[{}] stopping keepalives in state {}", debugId, state.toString());
            if (keepAlive != null) {
                keepAlive.cancel(true);
                keepAlive = null;
            }
        }

        /**
         * Sends a string command to the Miniserver
         *
         * @param string
         *            command to send to the Miniserver
         * @throws IOException
         *             exception when communication error occurs
         */
        private void sendString(String string) throws IOException {
            synchronized (state) {
                if (session != null && state != ClientState.IDLE && state != ClientState.CONNECTING
                        && state != ClientState.CLOSING) {
                    logger.debug("[{}] sending command: {}", debugId, string);
                    session.getRemote().sendString(string);
                } else {
                    logger.debug("[{}] NOT sending command, state {}: {}", debugId, state.toString(), string);
                }
            }
        }

        /**
         * Hash user name and password according to the algorithm required by the Miniserver
         *
         * @param hashKeyHex
         *            string with hash key received from the Miniserver in hex characters
         * @return
         *         hashed credentials to send to the Miniserver for authentication
         */
        private String hashCredentials(String hashKeyHex) {
            if (user == null || password == null || hashKeyHex == null) {
                return null;
            }
            try {
                byte[] hashKeyBytes = Hex.decodeHex(hashKeyHex.toCharArray());
                SecretKeySpec signKey = new SecretKeySpec(hashKeyBytes, "HmacSHA1");
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signKey);
                String data = user + ":" + password;
                byte[] rawData = mac.doFinal(data.getBytes());
                byte[] hexData = new Hex().encode(rawData);
                return new String(hexData, "UTF-8");
            } catch (DecoderException | NoSuchAlgorithmException | InvalidKeyException
                    | UnsupportedEncodingException e) {
                logger.debug("[{}] Error encrypting credentials: {}", debugId, e.getMessage());
                return null;
            }
        }
    }
}
