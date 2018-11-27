/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import org.openhab.binding.loxone.internal.core.LxJsonResponse.LxJsonCfgApi;
import org.openhab.binding.loxone.internal.core.LxJsonResponse.LxJsonSubResponse;
import org.openhab.binding.loxone.internal.core.LxServer.Configuration;
import org.openhab.binding.loxone.internal.core.LxServerEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Websocket client facilitating communication with Loxone Miniserver.
 * This client is implemented as a state machine, according to guidelines in Loxone API documentation.
 * It uses jetty websocket client and creates one own thread to send keep-alive messages to the Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxWsClient {
    private final Configuration configuration;
    private final InetAddress host;
    private final int port;
    private final String user;
    private final String password;
    private final int debugId;
    private long keepAlivePeriod = 240; // 4 minutes, server timeout is 5 minutes
    private long connectTimeout = 4; // 4 seconds to wait for connection response
    private int maxBinMsgSize = 3 * 1024; // 3 MB
    private int maxTextMsgSize = 512; // 512 KB

    private String swVersion;
    private String macAddress;
    private LxWsSecurityType securityType;
    private final Gson gson = new Gson();
    private ScheduledFuture<?> timeout;
    private LxWebSocket socket;
    private WebSocketClient wsClient;
    private BlockingQueue<LxServerEvent> queue;
    private ClientState state = ClientState.IDLE;
    private final Lock stateMachineLock = new ReentrantLock();
    private final Logger logger = LoggerFactory.getLogger(LxWsClient.class);

    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager
            .getScheduledPool(LxWsClient.class.getName());

    private static final String SOCKET_URL = "/ws/rfc6455";
    private static final String CMD_ACTION = "jdev/sps/io/";
    private static final String CMD_KEEPALIVE = "keepalive";
    private static final String CMD_ENABLE_UPDATES = "jdev/sps/enablebinstatusupdate";
    private static final String CMD_GET_APP_CONFIG = "data/LoxAPP3.json";
    private static final String CMD_CFG_API = "jdev/cfg/api";

    /**
     * Internal state of the websocket client.
     *
     * @author Pawel Pieczul - initial contribution
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
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxWsBinaryHeader {
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
            // These fields are not used today , but left it for future reference
            // estimated = ((buffer[offset + 2] & 0x01) != 0);
            // length = ByteBuffer.wrap(buffer, offset + 3, 4).getInt();
        }
    }

    /**
     * Create websocket client object
     *
     * @param debugId
     *            instance of the client used for debugging purposes only
     * @param queue
     *            message queue to communicate with its master {@link LxServer}, must be already initialized
     * @param configuration
     *            configuration object for getting and setting custom properties
     * @param securityType
     *            type of authentication/encryption method to use
     * @param host
     *            Miniserver's host address
     * @param port
     *            Miniserver's web services port
     * @param user
     *            user to authenticate
     * @param password
     *            password to authenticate
     */
    LxWsClient(int debugId, BlockingQueue<LxServerEvent> queue, Configuration configuration,
            LxWsSecurityType securityType, InetAddress host, int port, String user, String password) {
        this.debugId = debugId;
        this.queue = queue;
        this.configuration = configuration;
        this.securityType = securityType;
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
        stateMachineLock.lock();
        try {
            if (state != ClientState.IDLE) {
                close("Attempt to connect a websocket in non-idle state: " + state);
                return false;
            }

            socket = new LxWebSocket();
            wsClient = new WebSocketClient();

            String message = socket.httpGet(CMD_CFG_API);
            if (message != null) {
                LxJsonSubResponse response = socket.getSubResponse(message);
                if (response != null && response.code == 200 && response.value != null) {
                    try {
                        LxJsonCfgApi cfgApi = gson.fromJson(response.value.getAsString(), LxJsonCfgApi.class);
                        swVersion = cfgApi.version;
                        macAddress = cfgApi.snr;
                    } catch (JsonSyntaxException | NumberFormatException e) {
                        logger.debug("[{}] Error parsing API config response: {}, {}", debugId, response,
                                e.getMessage());
                    }
                } else {
                    logger.debug("[{}] Http get null or error in reponse for API config request.", debugId);
                }
            } else {
                logger.debug("[{}] Http get failed for API config request.", debugId);
            }

            try {
                wsClient.start();

                URI target = new URI("ws://" + host.getHostAddress() + ":" + port + SOCKET_URL);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                request.setSubProtocols("remotecontrol");

                startResponseTimeout();
                wsClient.connect(socket, target, request);
                setClientState(ClientState.CONNECTING);

                logger.debug("[{}] Connecting to server : {} ", debugId, target);
                return true;
            } catch (Exception e) {
                setClientState(ClientState.IDLE);
                close("Connection to websocket failed : " + e.getMessage());
                return false;
            }
        } finally {
            stateMachineLock.unlock();
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
        stateMachineLock.lock();
        try {
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
        } finally {
            stateMachineLock.unlock();
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
        stateMachineLock.lock();
        try {
            stopResponseTimeout();
            if (socket != null) {
                if (socket.session != null) {
                    if (state != ClientState.IDLE) {
                        logger.debug("[{}] Closing websocket session, reason : {}", debugId, reason);
                        setClientState(ClientState.CLOSING);
                    } else {
                        logger.debug("[{}] Closing websocket, state already IDLE.", debugId);
                    }
                    socket.session.close(StatusCode.NORMAL, reason);
                } else {
                    logger.debug("[{}] Closing websocket, but no session, reason : {}", debugId, reason);
                    setClientState(ClientState.IDLE);
                }
            } else {
                logger.debug("[{}] Closing websocket, but socket = null", debugId);
            }
        } finally {
            stateMachineLock.unlock();
        }
    }

    /**
     * Notify {@link LxServer} about server going offline and close websocket session from within
     * {@link LxWsClient},
     * without stopping the client.
     * To close session from {@link LxServer} level, use {@link #disconnect()}
     *
     * @param reasonCode
     *            reason code for server going offline
     * @param reasonText
     *            reason text (description) for server going offline
     */
    private void notifyAndClose(LxOfflineReason reasonCode, String reasonText) {
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
     * @return
     *         true if action was executed by the Miniserver
     */
    boolean sendAction(LxUuid id, String operation) {
        String command = CMD_ACTION + id.getOriginalString() + "/" + operation;
        logger.debug("[{}] Sending command {}", debugId, command);
        LxJsonSubResponse response = socket.sendCmdWithResp(command, true, true);
        if (response == null) {
            logger.debug("[{}] Error sending command {}", debugId, command);
            return false;
        }
        if (response.code != 200) {
            logger.debug("[{}] Received error response {} to command {}", debugId, response.code, command);
            return false;
        }
        return true;
    }

    /**
     * Returns {@link Gson} object so it can be reused without creating a new instance.
     *
     * @return
     *         Gson object for reuse
     */
    Gson getGson() {
        return gson;
    }

    /**
     * Sets a new websocket client state.
     * The caller must take care of thread synchronization.
     *
     * @param state
     *            new state to set
     */
    private void setClientState(LxWsClient.ClientState state) {
        logger.debug("[{}] changing client state to: {}", debugId, state);
        this.state = state;
    }

    /**
     * Start a timer to wait for a Miniserver response to an action sent from the binding.
     * When timer expires, connection is removed and server error is reported. Further connection attempt can be made
     * later by the upper layer.
     * If a previous timer is running, it will be stopped before a new timer is started.
     * The caller must take care of thread synchronization.
     */
    private void startResponseTimeout() {
        stopResponseTimeout();
        timeout = SCHEDULER.schedule(this::responseTimeout, connectTimeout, TimeUnit.SECONDS);
    }

    /**
     * Called when response timeout occurred.
     */
    private void responseTimeout() {
        stateMachineLock.lock();
        try {
            logger.debug("[{}] Miniserver response timeout", debugId);
            notifyMaster(EventType.SERVER_OFFLINE, LxOfflineReason.COMMUNICATION_ERROR,
                    "Miniserver response timeout occured");
            disconnect();
        } finally {
            stateMachineLock.unlock();
        }
    }

    /**
     * Stops scheduled timeout waiting for a Miniserver response
     * The caller must take care of thread synchronization.
     */
    private void stopResponseTimeout() {
        logger.trace("[{}] stopping response timeout in state {}", debugId, state);
        if (timeout != null) {
            timeout.cancel(true);
            timeout = null;
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
    private void notifyMaster(EventType event, LxOfflineReason reason, Object object) {
        LxOfflineReason localReason;
        if (reason == null) {
            localReason = LxOfflineReason.NONE;
        } else {
            localReason = reason;
        }
        LxServerEvent sync = new LxServerEvent(event, localReason, object);
        try {
            queue.put(sync);
        } catch (InterruptedException e) {
            logger.debug("[{}] Interrupted queue operation", debugId);
        }
    }

    /**
     * Implementation of jetty websocket client
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    @WebSocket
    public class LxWebSocket {
        Session session;
        private ScheduledFuture<?> keepAlive;
        private LxWsBinaryHeader header;
        private LxWsSecurity security;
        private boolean syncRequest;
        private LxJsonSubResponse commandResponse;
        private final Lock responseLock = new ReentrantLock();
        private final Condition responseAvailable = responseLock.newCondition();

        @OnWebSocketConnect
        public void onConnect(Session session) {
            stateMachineLock.lock();
            try {
                if (state != ClientState.CONNECTING) {
                    logger.debug("[{}] Unexpected connect received on websocket in state {}", debugId, state);
                    return;
                }

                WebSocketPolicy policy = session.getPolicy();
                policy.setMaxBinaryMessageSize(maxBinMsgSize * 1024);
                policy.setMaxTextMessageSize(maxTextMsgSize * 1024);

                logger.debug("[{}] Websocket connected (maxBinMsgSize={}, maxTextMsgSize={})", debugId,
                        policy.getMaxBinaryMessageSize(), policy.getMaxTextMessageSize());
                this.session = session;
                setClientState(ClientState.CONNECTED);

                security = LxWsSecurity.create(securityType, swVersion, debugId, configuration, socket, user, password);
                security.authenticate((result, details) -> {
                    if (result == LxOfflineReason.NONE) {
                        authenticated();
                    } else {
                        notifyAndClose(result, details);
                    }
                });
            } finally {
                stateMachineLock.unlock();
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            stateMachineLock.lock();
            try {
                logger.debug("[{}] Websocket connection in state {} closed with code {} reason : {}", debugId, state,
                        statusCode, reason);
                if (security != null) {
                    security.cancel();
                }
                stopKeepAlive();
                if (state == ClientState.CLOSING) {
                    session = null;
                } else if (state != ClientState.IDLE) {
                    responseLock.lock();
                    try {
                        commandResponse = null;
                        responseAvailable.signalAll();
                    } finally {
                        responseLock.unlock();
                    }
                    notifyMaster(EventType.SERVER_OFFLINE, LxOfflineReason.getReason(statusCode), reason);
                }
                setClientState(ClientState.IDLE);
            } finally {
                stateMachineLock.unlock();
            }
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            logger.debug("[{}] Websocket error : {}", debugId, error.getMessage());
        }

        @OnWebSocketMessage
        public void onBinaryMessage(byte data[], int msgOffset, int msgLength) {
            int offset = msgOffset;
            int length = msgLength;
            if (logger.isTraceEnabled()) {
                String s = Hex.encodeHexString(data);
                logger.trace("[{}] Binary message: length {}: {}", debugId, length, s);
            }
            stateMachineLock.lock();
            try {
                if (state != ClientState.RUNNING) {
                    return;
                }
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
            } finally {
                stateMachineLock.unlock();
            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            stateMachineLock.lock();
            try {
                if (logger.isTraceEnabled()) {
                    String trace = msg;
                    if (trace.length() > 100) {
                        trace = msg.substring(0, 100);
                    }
                    logger.trace("[{}] received message in state {}: {}", debugId, state, trace);
                }
                switch (state) {
                    case IDLE:
                    case CONNECTING:
                        logger.debug("[{}] Unexpected message received by websocket in state {}", debugId, state);
                        break;
                    case CONNECTED:
                    case RUNNING:
                        processResponse(msg);
                        break;
                    case UPDATING_CONFIGURATION:
                        try {
                            stopResponseTimeout();
                            LxJsonApp3 config = gson.fromJson(msg, LxJsonApp3.class);
                            if (config.msInfo != null) {
                                config.msInfo.swVersion = swVersion;
                                config.msInfo.macAddress = macAddress;
                            }
                            logger.debug("[{}] Received configuration from server", debugId);
                            notifyMaster(EventType.RECEIVED_CONFIG, null, config);
                            setClientState(ClientState.RUNNING);
                            notifyMaster(EventType.SERVER_ONLINE, null, null);
                            if (sendCmdWithResp(CMD_ENABLE_UPDATES, false, false) == null) {
                                notifyAndClose(LxOfflineReason.COMMUNICATION_ERROR, "Failed to enable state updates.");
                            }
                        } catch (JsonParseException e) {
                            notifyAndClose(LxOfflineReason.INTERNAL_ERROR, "Error processing received configuration");
                        }
                        break;
                    case CLOSING:
                    default:
                        break;
                }
            } finally {
                stateMachineLock.unlock();
            }
        }

        LxJsonSubResponse getSubResponse(String msg) {
            try {
                LxJsonSubResponse subResp = gson.fromJson(msg, LxJsonResponse.class).subResponse;
                if (subResp == null) {
                    logger.debug("[{}] Miniserver response subresponse is null: {}", debugId, msg);
                    return null;
                }
                if (subResp.control == null) {
                    logger.debug("[{}] Miniserver response control is null: {}", debugId, msg);
                    return null;
                }
                return subResp;
            } catch (JsonSyntaxException e) {
                logger.debug("[{}] Miniserver response JSON parsing error: {}, {}", debugId, msg, e.getMessage());
                return null;
            }
        }

        /**
         * Returns {@link Gson} object so it can be reused without creating a new instance.
         *
         * @return
         *         Gson object for reuse
         */
        Gson getGson() {
            return LxWsClient.this.getGson();
        }

        /**
         * Send a HTTP GET request and return server's response.
         *
         * @param request
         *            request content
         * @return
         *         response received
         */
        String httpGet(String request) {
            HttpURLConnection con = null;
            try {
                URL url = new URL("http", host.getHostAddress(), port,
                        request.startsWith("/") ? request : "/" + request);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String l;
                    while ((l = reader.readLine()) != null) {
                        result.append(l);
                    }
                    return result.toString();
                }
            } catch (IOException e) {
                return null;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }

        /**
         * Sends a command to the Miniserver and encrypts it if command can be encrypted and encryption is available.
         * Request can be synchronous or asynchronous. There is always a response expected to the command, that is a
         * standard command response as defined in {@link LxJsonSubResponse}. Such commands are the majority of commands
         * used for performing actions on the controls and for executing authentication procedure.
         * A synchronous command must not be sent from the websocket thread or it will cause a deadlock.
         * An asynchronous command request returns immediately, but the returned value will not contain valid data until
         * the response if received. Asynchronous request can be sent from the websocket thread.
         * There can be only one command sent which awaits response, whether this is synchronous or asynchronous
         * command. For synchronous commands this is ensured naturally, for asynchronous the caller must manage it.
         * If this method is called before a response to the previous command is received, it will return error and not
         * send the command.
         *
         * @param command
         *            command to send to the Miniserver
         * @param sync
         *            true is synchronous request, false if ansynchronous
         * @param encrypt
         *            true if command can be encrypted
         * @return
         *         response received (for sync command) or to be received (for async), null if error occurred
         */
        LxJsonSubResponse sendCmdWithResp(String command, boolean sync, boolean encrypt) {
            responseLock.lock();
            try {
                if (commandResponse != null) {
                    logger.warn("[{}] Command not sent, previous command not finished: {}", debugId, command);
                    return null;
                }
                if (!sendCmdNoResp(command, encrypt)) {
                    return null;
                }
                commandResponse = new LxJsonSubResponse();
                commandResponse.control = command;
                LxJsonSubResponse response = commandResponse;
                syncRequest = sync;
                if (sync) {
                    if (!responseAvailable.await(connectTimeout, TimeUnit.SECONDS)) {
                        commandResponse = null;
                        responseTimeout();
                        return null;
                    }
                    commandResponse = null;
                }
                return response;
            } catch (InterruptedException e) {
                logger.debug("[{}] Interrupted waiting for response: {}", debugId, command);
                commandResponse = null;
                return null;
            } finally {
                responseLock.unlock();
            }
        }

        /**
         * Sends a command to the Miniserver and encrypts it if command can be encrypted and encryption is available.
         * The request is asynchronous and no response is expected. It can be used to send commands from the websocket
         * thread or commands for which the responses are not following the standard format defined in
         * {@link LxJsonSubResponse}.
         * If the caller expects the non-standard response it should manage its reception and the response timeout.
         *
         * @param command
         *            command to send to the Miniserver
         * @param encrypt
         *            true if command can be encrypted
         * @return
         *         true if command was sent (no information if it was received)
         */
        private boolean sendCmdNoResp(String command, boolean encrypt) {
            stateMachineLock.lock();
            try {
                if (session != null && state != ClientState.IDLE && state != ClientState.CONNECTING
                        && state != ClientState.CLOSING) {
                    String encrypted = encrypt ? security.encrypt(command) : command;
                    if (logger.isDebugEnabled()) {
                        // security.encrypt() may return the original string if it did not encrypt
                        if (encrypted.equals(command)) {
                            logger.debug("[{}] Sending string: {}", debugId, command);
                        } else {
                            logger.debug("[{}] Sending encrypted string: {}", debugId, command);
                            logger.debug("[{}] Encrypted: {}", debugId, encrypted);
                        }
                    }
                    try {
                        session.getRemote().sendString(encrypted);
                        return true;
                    } catch (IOException e) {
                        logger.debug("[{}] Error sending command: {}, {}", debugId, command, e.getMessage());
                        return false;
                    }
                } else {
                    logger.debug("[{}] NOT sending command, state {}: {}", debugId, state, command);
                    return false;
                }
            } finally {
                stateMachineLock.unlock();
            }
        }

        /**
         * Process a Miniserver's response to a command. The response is in plain text format as received from the
         * websocket, but is expected to follow the standard format defined in {@link LxJsonSubResponse}.
         * If there is a thread waiting for the response (on a synchronous command request), the thread will be
         * released.
         * Only one requester is expected to wait for the response at a time - commands must be sent sequentially - a
         * command can be sent only after a response to the previous command was received, whether it was sent
         * synchronously or asynchronously.
         * If the received message is encrypted, it will be decrypted before processing.
         *
         * @param message
         *            websocket message with the response
         */
        private void processResponse(String message) {
            LxJsonSubResponse subResp = getSubResponse(message);
            if (subResp == null) {
                return;
            }
            logger.debug("[{}] Response: {}", debugId, message.trim());
            String control = subResp.control.trim();
            control = security.decryptControl(subResp.control);
            // for some reason the responses to some commands starting with jdev begin with dev, not jdev
            // this seems to be a bug in the Miniserver
            if (control.startsWith("dev/")) {
                control = "j" + control;
            }
            responseLock.lock();
            try {
                if (commandResponse == null) {
                    logger.warn("[{}] Received response, but awaiting none.", debugId);
                    return;
                }
                String awaitedControl = commandResponse.control;
                if (awaitedControl == null) {
                    logger.warn("[{}] Malformed awaiting response structure - no control.", debugId);
                    commandResponse = null;
                    return;
                }
                if (!awaitedControl.equals(control)) {
                    logger.warn("[{}] Waiting for another response: {}", debugId, awaitedControl);
                }
                commandResponse.code = subResp.code;
                commandResponse.value = subResp.value;
                if (syncRequest) {
                    logger.debug("[{}] Releasing command sender with response: {}, {}, {}", debugId, control,
                            subResp.code, subResp.value);
                    responseAvailable.signal();
                } else {
                    logger.debug("[{}] Reponse to asynchronous request: {}, {}, {}", debugId, control, subResp.code,
                            subResp.value);
                    commandResponse = null;
                }
            } finally {
                responseLock.unlock();
            }
        }

        /**
         * Perform actions after user authentication is successfully completed.
         * This method sends a request to receive Miniserver configuration.
         */
        private void authenticated() {
            logger.debug("[{}] Websocket authentication successfull.", debugId);
            stateMachineLock.lock();
            try {
                setClientState(ClientState.UPDATING_CONFIGURATION);
                if (sendCmdNoResp(CMD_GET_APP_CONFIG, false)) {
                    startResponseTimeout();
                    startKeepAlive();
                } else {
                    notifyAndClose(LxOfflineReason.INTERNAL_ERROR, "Error sending get config command.");
                }
            } finally {
                stateMachineLock.unlock();
            }
        }

        /**
         * Start keep alive thread. The thread will periodically send keep alive messages until {@link #stopKeepAlive()}
         * is called or a connection terminates.
         */
        private void startKeepAlive() {
            keepAlive = SCHEDULER.scheduleWithFixedDelay(() -> {
                stateMachineLock.lock();
                try {
                    if (state == ClientState.CLOSING || state == ClientState.IDLE || state == ClientState.CONNECTING) {
                        stopKeepAlive();
                    } else {
                        logger.debug("[{}] sending keepalive message", debugId);
                        if (!sendCmdNoResp(CMD_KEEPALIVE, false)) {
                            logger.debug("[{}] error sending keepalive message", debugId);
                        }
                    }
                } finally {
                    stateMachineLock.unlock();
                }
            }, keepAlivePeriod, keepAlivePeriod, TimeUnit.SECONDS);
        }

        /**
         * Stops keep alive thread and ceases sending keep alive messages to the Miniserver
         * The caller must take care of thread synchronization.
         */
        private void stopKeepAlive() {
            logger.trace("[{}] stopping keepalives in state {}", debugId, state);
            if (keepAlive != null) {
                keepAlive.cancel(true);
                keepAlive = null;
            }
        }
    }
}
