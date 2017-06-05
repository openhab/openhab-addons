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
    private int maxBinMsgSize = 3 * 1024; // 3 MB
    private int maxTextMsgSize = 512; // 512 KB

    private LxWebSocket socket = null;
    private LxWsBinaryHeader header = null;
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

        if (state != ClientState.IDLE) {
            close("Attempt to connect a websocket in non-idle state: " + state.toString());
            return false;
        }

        socket = new LxWebSocket();
        wsClient = new WebSocketClient();

        try {
            wsClient.start();

            URI target = new URI("ws://" + host.getHostAddress() + ":" + port + SOCKET_URL);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("remotecontrol");

            wsClient.connect(socket, target, request);
            state = ClientState.CONNECTING;

            logger.debug("[{}] Connecting to server : {} ", debugId, target);
            return true;

        } catch (Exception e) {
            state = ClientState.IDLE;
            close("Connection to websocket failed : " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnect from the websocket.
     * After calling this method, client is ready to perform a new connection request with {@link #connect()}.
     */
    void disconnect() {
        if (wsClient != null) {
            try {
                close("Disconnecting websocket client");
                wsClient.stop();
                wsClient = null;
            } catch (Exception e) {
                logger.debug("[{}] Failed to stop websocket client, message = {}", debugId, e.getMessage());
            }
        }
    }

    /**
     * Close websocket session from within {@link LxWsClient}, without stopping the client.
     * To close session from {@link LxServer} level, use {@link #disconnect()}
     *
     * @param reason
     *            reason for closing the websocket
     */
    private void close(String reason) {
        if (socket != null) {
            synchronized (socket) {
                if (socket.session != null) {
                    if (state != ClientState.IDLE) {
                        logger.debug("[{}] Closing websocket session, reason : {}", debugId, reason);
                        state = ClientState.CLOSING;
                    }
                    socket.session.close(StatusCode.NORMAL, reason);
                }
            }
        }
    }

    /**
     * Update configuration parameter(s) in runtime.
     * Calling this method will not interrupt existing services, changes will take effect when new values are used next
     * time.
     *
     * @param keepAlivePeriod
     *            new period between keep alive messages, in seconds
     * @param maxBinMsgSize
     *            maximum binary message size of websocket client (in kB)
     * @param maxTextMsgSize
     *            maximum text message size of websocket client (in kB)
     */
    void update(int keepAlivePeriod, int maxBinMsgSize, int maxTextMsgSize) {
        if (keepAlivePeriod > 0) {
            this.keepAlivePeriod = keepAlivePeriod;
        }
        if (maxBinMsgSize > 0) {
            this.maxBinMsgSize = maxBinMsgSize;
        }
        if (maxTextMsgSize > 0) {
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
        String command = CMD_ACTION + id.toString() + "/" + operation;
        logger.debug("[{}] Sending command {}", debugId, command);
        socket.sendString(command);
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

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("[{}] Websocket connection in state {} closed with code {} reason : {}", debugId,
                    state.toString(), statusCode, reason);

            if (keepAlive != null) {
                keepAlive.cancel(true);
                keepAlive = null;
            }
            if (state == ClientState.CLOSING) {
                synchronized (this) {
                    session = null;
                }
            } else if (state != ClientState.IDLE) {
                LxServer.OfflineReason reasonCode;
                if (statusCode == 1001) {
                    reasonCode = LxServer.OfflineReason.IDLE_TIMEOUT;
                } else if (statusCode == 4003) {
                    reasonCode = LxServer.OfflineReason.TOO_MANY_FAILED_LOGIN_ATTEMPTS;
                } else {
                    reasonCode = LxServer.OfflineReason.COMMUNICATION_ERROR;
                }
                notifyMaster(EventType.SERVER_OFFLINE, reasonCode, reason);
            }
            state = ClientState.IDLE;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {

            if (state != ClientState.CONNECTING) {
                logger.debug("[{}] Unexpected connect received on websocket in state {}", debugId, state.toString());
                return;
            }

            WebSocketPolicy policy = session.getPolicy();
            policy.setMaxBinaryMessageSize(maxBinMsgSize * 1024);
            policy.setMaxTextMessageSize(maxTextMsgSize * 1024);

            logger.debug("[{}] Websocket connected (maxBinMsgSize={}, maxTextMsgSize={})", debugId,
                    policy.getMaxBinaryMessageSize(), policy.getMaxTextMessageSize());
            this.session = session;
            state = ClientState.CONNECTED;

            try {
                sendString(CMD_GET_KEY);
            } catch (IOException e) {
                notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.COMMUNICATION_ERROR, null);
                close("Message send failure on recently connected websocket");
            }
            keepAlive = SCHEDULER.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.debug("[{}] sending keepalive message", debugId);
                        sendString(CMD_KEEPALIVE);
                    } catch (IOException e) {
                        logger.debug("[{}] error sending keepalive message", debugId);
                    }
                }
            }, keepAlivePeriod, keepAlivePeriod, TimeUnit.SECONDS);
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            logger.debug("[{}] Websocket error : {}", debugId, error.getMessage());
            // in states in which websocket is connected, connection will be closed
            // if error is not recoverable
            if (state == ClientState.CONNECTING) {
                notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.COMMUNICATION_ERROR,
                        "Error when connecting to websocket : " + error.getMessage());
                close(error.getMessage());
            }
        }

        @OnWebSocketMessage
        public void onBinaryMessage(byte data[], int offset, int length) {

            if (logger.isTraceEnabled()) {
                String s = Hex.encodeHexString(data);
                logger.trace("[{}] Binary message: length {}: {}", debugId, length, s);
            }

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

        @OnWebSocketMessage
        public void onMessage(String msg) {
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
                            notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.AUTHENTICATION_TIMEOUT, null);
                            close("Timeout on authentication procedure, response : " + resp.LL.value);
                        } else if (resp.LL.control.equals(CMD_GET_KEY) && resp.LL.Code == 200) {
                            String credentials = hashCredentials(resp.LL.value);
                            if (credentials != null) {
                                sendString(CMD_AUTHENTICATE + credentials);
                                state = ClientState.AUTHENTICATING;
                            } else {
                                notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.INTERNAL_ERROR, null);
                                close("Error creating credentials");
                            }
                        }
                        break;
                    case AUTHENTICATING:
                        resp = new Gson().fromJson(msg, LxJsonResponse.class);
                        if (resp.LL.Code == 401) {
                            notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.UNAUTHORIZED, null);
                            close("Websocket credentials unauthorized.");
                        } else if (resp.LL.Code == 420) {
                            notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.AUTHENTICATION_TIMEOUT, null);
                            close("Timeout on authentication procedure, response : " + resp.LL.value);
                        } else if (resp.LL.Code == 200) {
                            logger.debug("[{}] Websocket authentication successfull.", debugId);
                            sendString(CMD_GET_APP_CONFIG);
                            state = ClientState.UPDATING_CONFIGURATION;
                        }
                        break;
                    case UPDATING_CONFIGURATION:
                        LxJsonApp3 config = new Gson().fromJson(msg, LxJsonApp3.class);
                        if (config != null) {
                            logger.debug("[{}] Received configuration from server", debugId);
                            notifyMaster(EventType.RECEIVED_CONFIG, null, config);
                            sendString(CMD_ENABLE_UPDATES);
                            state = ClientState.RUNNING;
                            notifyMaster(EventType.SERVER_ONLINE, null, null);
                        } else {
                            notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.INTERNAL_ERROR, null);
                            close("Error processing received configuration");
                        }
                        break;
                    case RUNNING:
                    case CLOSING:
                    default:
                        break;
                }
            } catch (IOException e) {
                notifyMaster(EventType.SERVER_OFFLINE, LxServer.OfflineReason.COMMUNICATION_ERROR, null);
                close("Communication error when processing message : " + e.getMessage());
            }
        }

        private void notifyMaster(EventType event, LxServer.OfflineReason reason, Object object) {
            if (reason == null) {
                reason = LxServer.OfflineReason.NONE;
            }
            LxServerEvent sync = new LxServerEvent(event, reason, object);
            try {
                queue.put(sync);
            } catch (InterruptedException e) {
                logger.debug("[{}] Interrupted queue operation", debugId);
            }
        }

        private void sendString(String string) throws IOException {
            synchronized (this) {
                if (session != null) {
                    session.getRemote().sendString(string);
                }
            }
        }

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
