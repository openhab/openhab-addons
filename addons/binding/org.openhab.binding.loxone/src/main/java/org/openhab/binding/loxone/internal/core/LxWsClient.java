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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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

    private LxResponseCfgApi cfgApi;
    private ScheduledFuture<?> timeout;
    private LxErrorCode offlineCode;
    private String offlineReason;

    private final LxWebSocket socket;
    private final WebSocketClient wsClient;
    private final Lock webSocketLock = new ReentrantLock();
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
    public class LxResponseCfgApi {
        public String snr;
        public String version;
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

        socket = new LxWebSocket();
        wsClient = new WebSocketClient();
    }

    /**
     * Connect the websocket.
     * Attempts to connect to the websocket on a remote Miniserver.
     *
     * @return true if connection request initiated correctly, false if not
     */
    public boolean connect() {
        logger.debug("[{}] connect() websocket", debugId);
        webSocketLock.lock();
        try {
            offlineCode = null;
            offlineReason = null;

            /*
             * Try to read CfgApi structure from the miniserver. It contains serial number and SW version. If it can't
             * be read this is not a fatal issue, we will assume most recent SW version running.
             */
            String message = socket.httpGet(CMD_CFG_API);
            if (message != null) {
                LxResponse resp = socket.getResponse(message);
                if (resp != null) {
                    String anotherJson = resp.getValueAsString();
                    cfgApi = DEFAULT_GSON.fromJson(anotherJson, LxResponseCfgApi.class);
                } else {
                    logger.debug("[{}] Http get null or error in reponse for API config request.", debugId);
                    cfgApi = null;
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
                logger.debug("[{}] Connecting to server : {} ", debugId, target);
                wsClient.connect(socket, target, request);
                return true;
            } catch (Exception e) {
                logger.debug("[{}] Error starting websocket client: {}", debugId, e.getMessage());
                try {
                    wsClient.stop();
                } catch (Exception e2) {
                    logger.debug("[{}] Error stopping websocket client: {}", debugId, e2.getMessage());
                }
                return false;
            }
        } finally {
            webSocketLock.unlock();
        }
    }

    /**
     * Disconnect websocket session - initiated from this end.
     * 
     * @param code   error code for disconnecting the websocket
     * @param reason reason for disconnecting the websocket
     */
    public void disconnect(LxErrorCode code, String reason) {
        logger.trace("[{}] disconnect the websocket: {}, {}", debugId, code, reason);
        webSocketLock.lock();
        try {
            // in case the disconnection happens from both connection ends, store and pass only the first reason
            if (offlineCode == null) {
                offlineCode = code;
                offlineReason = reason;
            }
            stopResponseTimeout();
            if (socket.session != null) {
                socket.session.close(StatusCode.NORMAL, reason);
            } else {
                logger.debug("[{}] Disconnecting websocket, but no session, reason : {}", debugId, reason);
                handlerApi.setOffline(LxErrorCode.COMMUNICATION_ERROR, reason);
            }
            wsClient.stop();
        } catch (Exception e) {
            logger.debug("[{}] Exception disconnecting the websocket: ", e.getMessage());
        } finally {
            webSocketLock.unlock();
        }
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
        logger.debug("[{}] Miniserver response timeout", debugId);
        disconnect(LxErrorCode.COMMUNICATION_ERROR, "Miniserver response timeout occured");
    }

    /**
     * Stops scheduled timeout waiting for a Miniserver response
     * The caller must take care of thread synchronization.
     */
    private void stopResponseTimeout() {
        logger.trace("[{}] stopping response timeout", debugId);
        if (timeout != null) {
            timeout.cancel(true);
            timeout = null;
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
        private String awaitingCommand;
        private LxResponse awaitedResponse;
        private boolean syncRequest;
        private final Lock responseLock = new ReentrantLock();
        private final Condition responseAvailable = responseLock.newCondition();
        private boolean awaitingConfiguration = false;

        @OnWebSocketConnect
        public void onConnect(Session session) {
            webSocketLock.lock();
            try {
                WebSocketPolicy policy = session.getPolicy();
                policy.setMaxBinaryMessageSize(maxBinMsgSize * 1024);
                policy.setMaxTextMessageSize(maxTextMsgSize * 1024);

                logger.debug("[{}] Websocket connected (maxBinMsgSize={}, maxTextMsgSize={})", debugId,
                        policy.getMaxBinaryMessageSize(), policy.getMaxTextMessageSize());
                this.session = session;
                security = LxWsSecurity.create(securityType, cfgApi != null ? cfgApi.version : null, debugId,
                        handlerApi, socket, user, password);
                security.authenticate((result, details) -> {
                    if (result == LxErrorCode.OK) {
                        authenticated();
                    } else {
                        disconnect(result, details);
                    }
                });
            } finally {
                webSocketLock.unlock();
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            String reasonToPass;
            LxErrorCode codeToPass;
            webSocketLock.lock();
            try {
                logger.debug("[{}] Websocket connection closed with code {} reason : {}", debugId, statusCode, reason);
                if (security != null) {
                    security.cancel();
                }
                stopKeepAlive();
                session = null;
                // This callback is called when connection is terminated by either end.
                // If there is already a reason for disconnection, pass it unchanged.
                // Otherwise try to interpret the remote end reason.
                if (offlineCode != null) {
                    codeToPass = offlineCode;
                    reasonToPass = offlineReason;
                } else {
                    codeToPass = LxErrorCode.getErrorCode(statusCode);
                    reasonToPass = reason;
                }

            } finally {
                webSocketLock.unlock();
            }

            // Release any requester waiting for message response
            responseLock.lock();
            try {
                if (awaitedResponse != null) {
                    awaitedResponse.subResponse = null;
                }
                responseAvailable.signalAll();
            } finally {
                responseLock.unlock();
            }
            handlerApi.setOffline(codeToPass, reasonToPass);
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            logger.debug("[{}] Websocket error : {}", debugId, error.getMessage());
            // We do nothing. This callback may be called at various connection stages and indicates something wrong
            // with the connection mostly on the protocol level. It will be caught by other activities - connection will
            // be closed of timeouts will detect its inactivity.
        }

        @OnWebSocketMessage
        public void onBinaryMessage(byte data[], int msgOffset, int msgLength) {
            int offset = msgOffset;
            int length = msgLength;
            if (logger.isTraceEnabled()) {
                String s = Hex.encodeHexString(data);
                logger.trace("[{}] Binary message: length {}: {}", debugId, length, s);
            }
            webSocketLock.lock();
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
                                Double value = ByteBuffer.wrap(data, offset + 16, 8).order(ByteOrder.LITTLE_ENDIAN)
                                        .getDouble();
                                handlerApi.updateStateValue(new LxUuid(data, offset), value);
                                offset += 24;
                                length -= 24;
                            }
                            break;
                        case EVENT_TABLE_OF_TEXT_STATES:
                            while (length > 0) {
                                // unused today at (offset + 16): iconUuid
                                int textLen = ByteBuffer.wrap(data, offset + 32, 4).order(ByteOrder.LITTLE_ENDIAN)
                                        .getInt();
                                String value = new String(data, offset + 36, textLen);
                                int size = 36 + (textLen % 4 > 0 ? textLen + 4 - (textLen % 4) : textLen);
                                handlerApi.updateStateValue(new LxUuid(data, offset), value);
                                offset += size;
                                length -= size;
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
                webSocketLock.unlock();
            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            webSocketLock.lock();
            try {
                if (logger.isTraceEnabled()) {
                    String trace = msg;
                    if (trace.length() > 100) {
                        trace = msg.substring(0, 100);
                    }
                    logger.trace("[{}] received message: {}", debugId, trace);
                }
                if (awaitingConfiguration) {
                    awaitingConfiguration = false;
                    stopResponseTimeout();
                    handlerApi.setMiniserverConfig(msg, cfgApi);
                    handlerApi.setOnline();
                    if (sendCmdWithResp(CMD_ENABLE_UPDATES, false, false) == null) {
                        disconnect(LxErrorCode.COMMUNICATION_ERROR, "Failed to enable state updates.");
                    }
                } else {
                    processResponse(msg);
                }
            } finally {
                webSocketLock.unlock();
            }
        }

        /**
         * Parse received message into a response structure. Check basic correctness of the response.
         *
         * @param msg received response message
         * @return parsed response message
         */
        LxResponse getResponse(String msg) {
            try {
                LxResponse resp = DEFAULT_GSON.fromJson(msg, LxResponse.class);
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
         * Request can be synchronous or asynchronous. There is always a response expected to the command, and it is a
         * standard command response as defined in {@link LxResponse}. Such commands are the majority of commands
         * used for performing actions on the controls and for executing authentication procedure.
         * A synchronous command must not be sent from the websocket thread (from websocket callback methods) or it will
         * cause a deadlock.
         * An asynchronous command request returns immediately, but the returned value will not contain valid data in
         * the subResponse structure until a response is received. Asynchronous request can be sent from the websocket
         * thread. There can be only one command sent which awaits response per websocket connection,
         * whether this is synchronous or asynchronous command (this seems how Loxone Miniserver behaves, as it does not
         * have any unique identifier to match commands to responses).
         * For synchronous commands this is ensured naturally, for asynchronous the caller must manage it.
         * If this method is called before a response to the previous command is received, it will return error and not
         * send the command.
         *
         * @param command command to send to the Miniserver
         * @param sync    true is synchronous request, false if ansynchronous
         * @param encrypt true if command can be encrypted (does not mean it will)
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
         * The request is asynchronous and no response is expected (but it can arrive). It can be used to send commands
         * from the websocket thread or commands for which the responses are not following the standard format defined
         * in {@link LxResponse}.
         * If the caller expects the non-standard response it should manage its reception and the response timeout.
         *
         * @param command command to send to the Miniserver
         * @param encrypt true if command can be encrypted (does not mean it will)
         * @return true if command was sent (no information if it was received by the remote end)
         */
        private boolean sendCmdNoResp(String command, boolean encrypt) {
            webSocketLock.lock();
            try {
                if (session != null) {
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
                    logger.debug("[{}] NOT sending command: {}", debugId, command);
                    return false;
                }
            } finally {
                webSocketLock.unlock();
            }
        }

        /**
         * Process a Miniserver's response to a command. The response is in plain text format as received from the
         * websocket, but is expected to follow the standard format defined in {@link LxResponse}.
         * If there is a thread waiting for the response (on a synchronous command request), the thread will be
         * released. Otherwise the response will be copied into the response object provided to the asynchronous
         * requester when the command was sent.
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
            webSocketLock.lock();
            try {
                awaitingConfiguration = true;
                if (sendCmdNoResp(CMD_GET_APP_CONFIG, false)) {
                    startResponseTimeout();
                    startKeepAlive();
                } else {
                    disconnect(LxErrorCode.INTERNAL_ERROR, "Error sending get config command.");
                }
            } finally {
                webSocketLock.unlock();
            }
        }

        /**
         * Start keep alive thread. The thread will periodically send keep alive messages until {@link #stopKeepAlive()}
         * is called or a connection terminates.
         */
        private void startKeepAlive() {
            keepAlive = SCHEDULER.scheduleWithFixedDelay(() -> {
                webSocketLock.lock();
                try {
                    stopKeepAlive();
                    logger.debug("[{}] sending keepalive message", debugId);
                    if (!sendCmdNoResp(CMD_KEEPALIVE, false)) {
                        logger.debug("[{}] error sending keepalive message", debugId);
                    }
                } finally {
                    webSocketLock.unlock();
                }
            }, keepAlivePeriod, keepAlivePeriod, TimeUnit.SECONDS);
        }

        /**
         * Stops keep alive thread and ceases sending keep alive messages to the Miniserver
         * The caller must take care of thread synchronization.
         */
        private void stopKeepAlive() {
            logger.trace("[{}] stopping keepalives", debugId);
            if (keepAlive != null) {
                keepAlive.cancel(true);
                keepAlive = null;
            }
        }
    }
}
