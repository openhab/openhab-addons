/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;

/**
 * {@link MBWebsocket} as socket endpoint to communicate with Mercedes
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class MBWebsocket {
    // timeout stays unlimited until binding decides to close
    private static final int CONNECT_TIMEOUT_MS = 0;
    // standard runtime of Websocket
    private static final int WS_RUNTIME_MS = 60 * 1000;
    // addon time of 1 minute for a new send command
    private static final int ADDON_MESSAGE_TIME_MS = 60 * 1000;
    // check Socket time elapsed each second
    private static final int CHECK_INTERVAL_MS = 60 * 1000;
    // additional 5 minutes after keep alive
    private static final int KEEP_ALIVE_ADDON = 5 * 60 * 1000;

    private final Logger logger = LoggerFactory.getLogger(MBWebsocket.class);
    private final Map<String, Instant> pingPongMap = new HashMap<>();
    private final AccountHandler accountHandler;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService("mercedesme-websocket", null);

    private Optional<ScheduledFuture<?>> refresher = Optional.empty();
    private Optional<WebSocketClient> webSocketClient = Optional.empty();
    private Optional<Session> session = Optional.empty();
    private List<ClientMessage> commandQueue = new ArrayList<>();
    private Instant runTill = Instant.now();
    private WebsocketState state = WebsocketState.STOPPED;
    private boolean keepAlive = false;
    private boolean disposed = true;

    public enum WebsocketState {
        STOPPED,
        DISCONNECTED,
        CONNECTED,
        STARTED
    }

    public MBWebsocket(AccountHandler accountHandler, HttpClient httpClient) {
        this.accountHandler = accountHandler;
        this.httpClient = httpClient;
    }

    /**
     * Regular update call from AccountHandler to refresh data according to refreshInterval
     */
    public void update() {
        scheduler.execute(this::doRefresh);
    }

    /**
     * If proto update is received an acknowledge message needs to be sent to the server to avoid repeating this message
     * again and again.
     */
    public void sendAcknowledgeMessage(ClientMessage message) {
        session.ifPresent(s -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                s.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
            } catch (IOException e) {
                logger.warn("Error sending acknowledge {} : {}", message.getAllFields(), e.getMessage());
            }
        });
    }

    /**
     * Add command to the command queue to be sent to the server. Immediately request refresh to send command as soon as
     * possible.
     *
     * @param command to be sent
     */
    public void addCommand(ClientMessage command) {
        commandQueue.add(command);
        scheduler.execute(this::doRefresh);
    }

    /**
     * Dispose websocket in case of disposed AccountHandler. Cleanup stored files and stop web socket client.
     */
    public void dispose(boolean disposed) {
        this.disposed = disposed;
        if (disposed) {
            runTill = Instant.MIN;
            keepAlive = false;
            refresher.ifPresent(job -> {
                job.cancel(false);
                refresher = Optional.empty();
            });
            scheduler.execute(this::stop);
        }
    }

    /**
     * Set keep alive mode for web socket connection. If keep alive is set to true the web socket will not be closed
     *
     * @param alive
     */
    public void keepAlive(boolean alive) {
        if (!keepAlive) {
            if (alive) {
                logger.trace("WebSocket - keep alive start");
            }
        } else {
            if (!alive) {
                // after keep alive is finished add 5 minutes to cover e.g. door events after
                // trip is finished
                runTill = Instant.now().plusMillis(KEEP_ALIVE_ADDON);
                logger.trace("Websocket - keep alive stop - run till {}", runTill.toString());
            }
        }
        keepAlive = alive;
    }

    /**
     * Send message to the server if there is a command available in the command queue.
     *
     * @return true if command is successfully submitted, false otherwise
     */
    private boolean sendMessage() {
        if (!commandQueue.isEmpty()) {
            ClientMessage message = commandQueue.remove(0);
            logger.trace("Send Message {}", message.getAllFields());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                if (session.isPresent()) {
                    session.get().getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
                    return true;
                } else {
                    logger.warn("Cannot send message {} - no session available", message.getAllFields());
                    return false;
                }
            } catch (IOException e) {
                logger.warn("Error sending message {} : {}", message.getAllFields(), e.getMessage());
            }
            logger.info("Send Message {} done", message.getAllFields());
        }
        return false;
    }

    /**
     * Start the web socket client and connect to the server. If the web socket client is already running it will not
     */
    private void start() {
        if (!disposed && webSocketClient.isEmpty()) {
            WebSocketClient client = new WebSocketClient(httpClient);
            try {
                client.setMaxIdleTimeout(CONNECT_TIMEOUT_MS);
                ClientUpgradeRequest request = accountHandler.getClientUpgradeRequest();
                String websocketURL = accountHandler.getWSUri();
                if (Constants.JUNIT_TOKEN.equals(request.getHeader("Authorization"))) {
                    // avoid unit test requesting real web socket - simply return
                    return;
                }
                logger.trace("Websocket start {} max message size {}", websocketURL, client.getMaxBinaryMessageSize());
                client.start();
                client.connect(this, new URI(websocketURL), request);
                webSocketClient = Optional.of(client);
                runTill = Instant.now().plusMillis(WS_RUNTIME_MS);
                state = WebsocketState.STARTED;
            } catch (Exception e) {
                // catch Exceptions of start stop and declare communication error
                accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/mercedesme.account.status.websocket-failure");
                logger.warn("Websocket handling exception: {}", e.getMessage());
            }
        }
    }

    /**
     * Performs an update of the web socket connection. If websocket is disposed refresh will not be executed. In case
     * of CONNECTED it will check
     * - if there are commands to be sent
     * - send ping to the server
     * - check if keep alive is set or run time is not over
     * In case of other state it will start the web socket connection.
     */
    private void doRefresh() {
        if (!disposed) {
            if (state == WebsocketState.CONNECTED) {
                logger.trace("Refresh: Websocket fine - state {}", state);
                if (sendMessage()) {
                    // add additional runtime to execute and finish command
                    runTill = runTill.plusMillis(ADDON_MESSAGE_TIME_MS);
                }
                ping();
                if (keepAlive || Instant.now().isBefore(runTill)) {
                    // doRefresh is called by AccountHandler, websocket endpoint onConnect and addCommand. To avoid
                    // multiple future calls cancel the current running or future schedule calls.
                    refresher.ifPresent(job -> {
                        job.cancel(false);
                    });
                    refresher = Optional
                            .of(scheduler.schedule(this::doRefresh, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS));
                } else {
                    // run time is over - disconnect
                    logger.debug("Websocket run time is over - disconnect");
                    scheduler.execute(this::stop);
                }
            } else {
                logger.trace("Refresh: Websocket needs to be started - state {}", state);
                scheduler.execute(this::start);
            }
        } else {
            logger.trace("Refresh: Websocket disposed - state {}", state);
        }
    }

    /**
     * Request to disconnect the web socket session. This will close the session normally with a status code of 1000
     */
    private void disconnect() {
        session.ifPresent(session -> {
            // close session normally
            session.close(1000, "Websocket closed by binding");
        });
    }

    /**
     * Stop the web socket client and disconnect the session if it is still connected.
     */
    private void stop() {
        session.ifPresentOrElse(session -> {
            logger.trace("Websocket stop - disconnect session first - state {}", state);
            scheduler.execute(this::disconnect);
        }, () -> {
            logger.trace("Websocket stop - state {}", state);
            webSocketClient.ifPresent(client -> {
                try {
                    client.stop();
                } catch (Exception e) {
                    logger.warn("Websocket stop exception: {}", e.getMessage());
                }
                client.destroy();
                webSocketClient = Optional.empty();
            });
            state = WebsocketState.STOPPED;
        });
    }

    /**
     * Ping the server to keep the connection alive and to check if the connection is still valid.
     */
    private void ping() {
        logger.trace("Websocket ping {}", Instant.now().toString());
        session.ifPresent(session -> {
            try {
                String pingId = UUID.randomUUID().toString();
                pingPongMap.put(pingId, Instant.now());
                session.getRemote().sendPing(ByteBuffer.wrap(pingId.getBytes()));
            } catch (IOException e) {
                logger.warn("Websocket ping failed {}", e.getMessage());
            }
        });
    }

    /**
     * web socket endpoints
     */

    @OnWebSocketMessage
    public void onByteArray(byte[] blob, int offset, int length) {
        try {
            byte[] message = blob;
            if (offset != 0) {
                int offsetLength = length - offset;
                message = new byte[offsetLength];
                System.arraycopy(blob, offset, message, 0, offsetLength);

            }
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(message);
            logger.trace("WebSocket - Message {}", pm.getMsgCase());
            accountHandler.enqueueMessage(pm);
            /**
             * https://community.openhab.org/t/mercedes-me/136866/12
             * Release Websocket thread as early as possible to avoid exceptions
             *
             * 1. Websocket thread responsible for reading stream into PushMessage and enqueue for
             * AccountHandler.
             * 2. AccountHamdler thread responsible for handling PushMessage. In case of
             * update enqueue PushMessage
             * at VehicleHandöer
             * 3. VehicleHandler responsible to update channels
             */
        } catch (IOException e) {
            logger.warn("IOException decoding message {}", e.getMessage());
        } catch (Error err) {
            logger.warn("Error decoding message {}", err.getMessage());
        }
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        if (Frame.Type.PONG.equals(frame.getType())) {
            ByteBuffer buffer = frame.getPayload();
            byte[] bytes = new byte[frame.getPayloadLength()];
            for (int i = 0; i < frame.getPayloadLength(); i++) {
                bytes[i] = buffer.get(i);
            }
            String payloadString = new String(bytes);
            Instant sent = pingPongMap.remove(payloadString);
            if (sent == null) {
                logger.debug("Websocket received pong without ping {}", payloadString);
            }
        } else if (Frame.Type.PING.equals(frame.getType())) {
            session.ifPresentOrElse((session) -> {
                ByteBuffer buffer = frame.getPayload();
                try {
                    session.getRemote().sendPong(buffer);
                } catch (IOException e) {
                    logger.warn("Websocket onPing answer exception {}", e.getMessage());
                }
            }, () -> {
                logger.debug("Websocket onPing answer cannot be initiated");
            });
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = Optional.of(session);
        state = WebsocketState.CONNECTED;
        pingPongMap.clear();
        accountHandler.updateStatus(ThingStatus.ONLINE);
        logger.trace("Websocket connected - state {}", state);
        // websocket client is started and connected - time to refresh
        scheduler.execute(this::doRefresh);
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        onClosedSession(null);
        logger.trace("Disconnected from server. Status {} Reason {}", statusCode, reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        onClosedSession(t);
    }

    private void onClosedSession(@Nullable Throwable throwable) {
        this.session = Optional.empty();
        state = WebsocketState.DISCONNECTED;
        pingPongMap.clear();
        if (throwable != null) {
            accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/mercedesme.account.status.websocket-failure [\"" + throwable.getMessage() + "\"]");
        }
        // stop web socket client for closed session
        scheduler.execute(this::stop);
    }
}
