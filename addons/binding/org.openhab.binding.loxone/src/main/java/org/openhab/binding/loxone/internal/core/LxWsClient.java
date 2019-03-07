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
package org.openhab.binding.loxone.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
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
import org.openhab.binding.loxone.internal.LxBindingConfiguration;
import org.openhab.binding.loxone.internal.LxServerHandler;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.controls.LxControl;
import org.openhab.binding.loxone.internal.core.LxResponse.LxSubResponse;
import org.openhab.binding.loxone.internal.core.LxServerEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Websocket client facilitating communication with Loxone Miniserver.
 * This client is implemented as a state machine, according to guidelines in Loxone API documentation.
 * It uses jetty websocket client and creates one own thread to send keep-alive messages to the Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxWsClient {
    public static final Gson DEFAULT_GSON = new Gson();

    private final LxServerHandlerApi handlerApi;
    private final InetAddress host;
    private final int port;
    private final String user;
    private final String password;
    private final LxWsSecurityType securityType;
    private final int debugId;

    private long keepAlivePeriod = 240; // 4 minutes, server timeout is 5 minutes
    private long responseTimeout = 4; // 4 seconds to wait for Miniserver response
    private int maxBinMsgSize = 3 * 1024; // 3 MB
    private int maxTextMsgSize = 512; // 512 KB

    private String swVersion;
    private String macAddress;
    private ScheduledFuture<?> timeout;
    private LxWebSocket socket;
    private WebSocketClient wsClient;
    private ClientState state = ClientState.IDLE;

    private final Gson gson;
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
     * A sub-response value structure that is received as a response to config API HTTP request sent to the Miniserver.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxResponseCfgApi {
        String snr;
        String version;
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
         * @param buffer buffer with received message
         * @param offset offset in bytes at which header is expected
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
     * @param debugId    instance of the client used for debugging purposes only
     * @param handlerApi API to the thing handler
     * @param cfg        Miniserver configuration
     * @throws UnknownHostException when host can't be resolved or reached
     */
    public LxWsClient(int debugId, LxServerHandlerApi handlerApi, LxBindingConfiguration cfg)
            throws UnknownHostException {
        this.debugId = debugId;
        this.handlerApi = handlerApi;
        host = InetAddress.getByName(cfg.host);
        port = cfg.port;
        user = cfg.user;
        password = cfg.password;
        securityType = LxWsSecurityType.getType(cfg.authMethod);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LxUuid.class, LxUuid.DESERIALIZER);
        builder.registerTypeAdapter(LxControl.class, LxControl.DESERIALIZER);
        gson = builder.create();

        if (cfg.keepAlivePeriod > 0 && cfg.keepAlivePeriod != keepAlivePeriod) {
            logger.debug("[{}] Changing keepAlivePeriod to {}", debugId, cfg.keepAlivePeriod);
            keepAlivePeriod = cfg.keepAlivePeriod;
        }
        if (cfg.responseTimeout > 0 && cfg.responseTimeout != responseTimeout) {
            logger.debug("[{}] Changing responseTimeout to {}", debugId, cfg.responseTimeout);
            responseTimeout = cfg.responseTimeout;
        }
        if (cfg.maxBinMsgSize > 0 && cfg.maxBinMsgSize != maxBinMsgSize) {
            logger.debug("[{}] Changing maxBinMsgSize to {}", debugId, cfg.maxBinMsgSize);
            maxBinMsgSize = cfg.maxBinMsgSize;
        }
        if (cfg.maxTextMsgSize > 0 && cfg.maxTextMsgSize != maxTextMsgSize) {
            logger.debug("[{}] Changing maxTextMsgSize to {}", debugId, cfg.maxTextMsgSize);
            maxTextMsgSize = cfg.maxTextMsgSize;
        }
    }

    /**
     * Connect the websocket.
     * Attempts to connect to the websocket on a remote Miniserver.
     *
     * @return true if connection request initiated correctly, false if not
     */
    public boolean connect() {
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
                LxResponse resp = socket.getResponse(message);
                if (resp != null) {
                    LxResponseCfgApi cfgApi = resp.getValueAs(LxResponseCfgApi.class);
                    if (cfgApi != null) {
                        swVersion = cfgApi.version;
                        macAddress = cfgApi.snr;
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
     * Disconnect from the websocket with provided reason. This method is called from {@link LxServerHandler} level and
     * also from unsuccessful connect attempt.
     * After calling this method, client is ready to perform a new connection request with {@link #connect()}.
     *
     * @param reason text describing reason for disconnection
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
    public void disconnect() {
        disconnect("Disconnecting websocket client");
    }

    /**
     * Close websocket session from within {@link LxWsClient}, without stopping the client.
     * To close session from {@link LxServerHandler} level, use {@link #disconnect()}
     *
     * @param reason reason for closing the websocket
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
     * Notify {@link LxServerHandler} about server going offline and close websocket session from within
     * {@link LxWsClient},
     * without stopping the client.
     * To close session from {@link LxServerHandler} level, use {@link #disconnect()}
     *
     * @param reasonCode reason code for server going offline
     * @param reasonText reason text (description) for server going offline
     */
    private void notifyAndClose(LxErrorCode reasonCode, String reasonText) {
        notifyMaster(EventType.SERVER_OFFLINE, reasonCode, reasonText);
        close(reasonText);
    }

    /**
     * Sends an action to a Loxone Miniserver's control.
     *
     * @param id        identifier of the control
     * @param operation identifier of the operation
     * @throws IOException when communication error with Miniserver occurs
     */
    public void sendAction(LxUuid id, String operation) throws IOException {
        String command = CMD_ACTION + id.getOriginalString() + "/" + operation;
        logger.debug("[{}] Sending command {}", debugId, command);
        LxResponse response = socket.sendCmdWithResp(command, true, true);
        if (response == null) {
            throw new IOException("Error sending command " + command);
        }
        if (!response.isResponseOk()) {
            throw new IOException("Received response is not ok to command " + command);
        }
    }

    /**
     * Returns {@link Gson} object so it can be reused without creating a new instance.
     *
     * @return Gson object for reuse
     */
    Gson getGson() {
        return gson;
    }

    /**
     * Sets a new websocket client state.
     * The caller must take care of thread synchronization.
     *
     * @param state new state to set
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
        timeout = SCHEDULER.schedule(this::responseTimeout, responseTimeout, TimeUnit.SECONDS);
    }

    /**
     * Called when response timeout occurred.
     */
    private void responseTimeout() {
        stateMachineLock.lock();
        try {
            logger.debug("[{}] Miniserver response timeout", debugId);
            notifyMaster(EventType.SERVER_OFFLINE, LxErrorCode.COMMUNICATION_ERROR,
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
     * Sends an event to a thing handler thread object
     *
     * @param event  event that happened
     * @param reason reason for the event (applicable to server OFFLINE event}
     * @param object additional data for the event (text message for OFFLINE event, data for state changes)
     */
    private void notifyMaster(EventType event, LxErrorCode reason, Object object) {
        LxErrorCode localReason;
        if (reason == null) {
            localReason = LxErrorCode.OK;
        } else {
            localReason = reason;
        }
        LxServerEvent sync = new LxServerEvent(event, localReason, object);
        handlerApi.sendEvent(sync);
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
        private String awaitingCommand;
        private LxResponse awaitedResponse;
        private boolean syncRequest;
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

                security = LxWsSecurity.create(securityType, swVersion, debugId, handlerApi, socket, user, password);
                security.authenticate((result, details) -> {
                    if (result == LxErrorCode.OK) {
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
                        awaitedResponse.subResponse = null;
                        responseAvailable.signalAll();
                    } finally {
                        responseLock.unlock();
                    }
                    notifyMaster(EventType.SERVER_OFFLINE, LxErrorCode.getErrorCode(statusCode), reason);
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
                            LxConfig config = gson.fromJson(msg, LxConfig.class);
                            if (config.msInfo != null) {
                                config.msInfo.swVersion = swVersion;
                                config.msInfo.macAddress = macAddress;
                            }
                            config.finalize(handlerApi);
                            logger.debug("[{}] Received configuration from server", debugId);
                            notifyMaster(EventType.RECEIVED_CONFIG, null, config);
                            setClientState(ClientState.RUNNING);
                            notifyMaster(EventType.SERVER_ONLINE, null, null);
                            if (sendCmdWithResp(CMD_ENABLE_UPDATES, false, false) == null) {
                                notifyAndClose(LxErrorCode.COMMUNICATION_ERROR, "Failed to enable state updates.");
                            }
                        } catch (JsonParseException e) {
                            logger.debug("[{}] Exception JSON parsing: {}", debugId, e.getMessage());
                            notifyAndClose(LxErrorCode.INTERNAL_ERROR, "Error processing received configuration");
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

        LxResponse getResponse(String msg) {
            try {
                LxResponse resp = gson.fromJson(msg, LxResponse.class);
                if (!resp.isResponseOk()) {
                    logger.debug("[{}] Miniserver response is not ok: {}", debugId, msg);
                    return null;
                }
                return resp;
            } catch (JsonParseException e) {
                logger.debug("[{}] Miniserver response JSON parsing error: {}, {}", debugId, msg, e.getMessage());
                return null;
            }
        }

        /**
         * Returns {@link Gson} object so it can be reused without creating a new instance.
         *
         * @return Gson object for reuse
         */
        Gson getGson() {
            return LxWsClient.this.getGson();
        }

        /**
         * Send a HTTP GET request and return server's response.
         *
         * @param request request content
         * @return response received
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
         * standard command response as defined in {@link LxSubResponse}. Such commands are the majority of commands
         * used for performing actions on the controls and for executing authentication procedure.
         * A synchronous command must not be sent from the websocket thread or it will cause a deadlock.
         * An asynchronous command request returns immediately, but the returned value will not contain valid data until
         * the response is received. Asynchronous request can be sent from the websocket thread.
         * There can be only one command sent which awaits response per websocket connection,
         * whether this is synchronous or asynchronous command (this is how Loxone Miniserver behaves).
         * For synchronous commands this is ensured naturally, for asynchronous the caller must manage it.
         * If this method is called before a response to the previous command is received, it will return error and not
         * send the command.
         *
         * @param command command to send to the Miniserver
         * @param sync    true is synchronous request, false if ansynchronous
         * @param encrypt true if command can be encrypted
         * @return response received (for sync command) or to be received (for async), null if error occurred
         */
        LxResponse sendCmdWithResp(String command, boolean sync, boolean encrypt) {
            responseLock.lock();
            try {
                if (awaitedResponse != null || awaitingCommand != null) {
                    logger.warn("[{}] Command not sent, previous command not finished: {}", debugId, command);
                    return null;
                }
                if (!sendCmdNoResp(command, encrypt)) {
                    return null;
                }
                LxResponse resp = new LxResponse();
                awaitingCommand = command;
                awaitedResponse = resp;
                syncRequest = sync;
                if (sync) {
                    if (!responseAvailable.await(responseTimeout, TimeUnit.SECONDS)) {
                        awaitedResponse = null;
                        awaitingCommand = null;
                        responseTimeout();
                        return null;
                    }
                    awaitedResponse = null;
                    awaitingCommand = null;
                }
                return resp;
            } catch (InterruptedException e) {
                logger.debug("[{}] Interrupted waiting for response: {}", debugId, command);
                awaitedResponse = null;
                awaitingCommand = null;
                return null;
            } finally {
                responseLock.unlock();
            }
        }

        /**
         * Sends a command to the Miniserver and encrypts it if command can be encrypted and encryption is available.
         * The request is asynchronous and no response is expected. It can be used to send commands from the websocket
         * thread or commands for which the responses are not following the standard format defined in
         * {@link LxSubResponse}.
         * If the caller expects the non-standard response it should manage its reception and the response timeout.
         *
         * @param command command to send to the Miniserver
         * @param encrypt true if command can be encrypted
         * @return true if command was sent (no information if it was received)
         */
        private boolean sendCmdNoResp(String command, boolean encrypt) {
            stateMachineLock.lock();
            try {
                if (session != null && state != ClientState.IDLE && state != ClientState.CONNECTING
                        && state != ClientState.CLOSING) {
                    String encrypted = encrypt ? security.encrypt(command) : command;
                    logger.debug("[{}] Sending encrypted string: {}", debugId, command);
                    logger.debug("[{}] Encrypted: {}", debugId, encrypted);
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
         * websocket, but is expected to follow the standard format defined in {@link LxSubResponse}.
         * If there is a thread waiting for the response (on a synchronous command request), the thread will be
         * released.
         * Only one requester is expected to wait for the response at a time - commands must be sent sequentially - a
         * command can be sent only after a response to the previous command was received, whether it was sent
         * synchronously or asynchronously.
         * If the received message is encrypted, it will be decrypted before processing.
         *
         * @param message websocket message with the response
         */
        private void processResponse(String message) {
            LxResponse resp = getResponse(message);
            if (resp == null) {
                return;
            }
            logger.debug("[{}] Response: {}", debugId, message.trim());
            String control = resp.getCommand().trim();
            control = security.decryptControl(control);
            // for some reason the responses to some commands starting with jdev begin with dev, not jdev
            // this seems to be a bug in the Miniserver
            if (control.startsWith("dev/")) {
                control = "j" + control;
            }
            responseLock.lock();
            try {
                if (awaitedResponse == null || awaitingCommand == null) {
                    logger.warn("[{}] Received response, but awaiting none.", debugId);
                    return;
                }
                if (!awaitingCommand.equals(control)) {
                    logger.warn("[{}] Waiting for another response: {}", debugId, awaitingCommand);
                }
                awaitedResponse.subResponse = resp.subResponse;
                if (syncRequest) {
                    logger.debug("[{}] Releasing command sender with response: {}, {}", debugId, control,
                            resp.getResponseCodeNumber());
                    responseAvailable.signal();
                } else {
                    logger.debug("[{}] Reponse to asynchronous request: {}, {}", debugId, control,
                            resp.getResponseCodeNumber());
                    awaitedResponse = null;
                    awaitingCommand = null;
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
                    notifyAndClose(LxErrorCode.INTERNAL_ERROR, "Error sending get config command.");
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
