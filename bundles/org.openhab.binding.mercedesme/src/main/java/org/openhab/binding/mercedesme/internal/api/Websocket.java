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
package org.openhab.binding.mercedesme.internal.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;

/**
 * {@link Websocket} as socket endpoint to communicate with Mercedes
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Remove loop cpaturing scheduler thread
 */
@WebSocket
@NonNullByDefault
public class Websocket extends RestApi {
    // WebSocket idle/read timeout - matches Mercedes-Me App (OkHttp readTimeout(6s)): the connection is
    // considered dead if no frame (incl. pong) is received within this time
    private static final int WS_IDLE_TIMEOUT_MS = 6 * 1000;
    // standard runtime of Websocket: randomized between 1 and 3 minutes (anti-bot pattern, avoids a fixed,
    // easily fingerprinted lifetime - same idea as AccountHandler.nextRefreshSeconds())
    private static final long WS_RUNTIME_MIN_MS = 60 * 1000L;
    private static final long WS_RUNTIME_MAX_MS = 3 * 60 * 1000L;
    // addon time of 1 minute for a new send command
    private static final int ADDON_MESSAGE_TIME_MS = 60 * 1000;
    // ping cadence - matches Mercedes-Me App (OkHttp pingInterval(6s)); also drives the general check loop
    private static final int PING_INTERVAL_MS = 6 * 1000;
    // additional 5 minutes after keep alive
    private static final int KEEP_ALIVE_ADDON = 5 * 60 * 1000;
    // max reconnect attempts before falling back to full re-authorization - matches Mercedes-Me App
    // (ReconnectableSocketConnection maxRetries=5)
    private static final int MAX_RECONNECT_RETRIES = 5;
    // delay between reconnect attempts - matches Mercedes-Me App (ReconnectableSocketConnection
    // retryDelayInMillis=5000)
    private static final int RECONNECT_DELAY_MS = 5000;

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final AccountHandler accountHandler;
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService("mercedesme-websocket", null);

    private @Nullable ScheduledFuture<?> refresher;
    private @Nullable WebSocketClient webSocketClient;
    private @Nullable Session session;
    private @Nullable Instant pingSentAt;
    private List<ClientMessage> commandQueue = new ArrayList<>();
    private Instant runTill = Instant.now();
    private WebsocketState state = WebsocketState.STOPPED;
    private boolean keepAlive = false;
    private boolean disposed = true;
    // set right before a deliberate (idle-timeout) close so onClosedSession doesn't try to reconnect
    private boolean intentionalClose = false;
    private int reconnectAttempts = 0;

    public enum WebsocketState {
        STOPPED,
        DISCONNECTED,
        CONNECTED,
        STARTED
    }

    public Websocket(AccountHandler atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l,
            Storage<String> store) {
        super(atrl, hc, ac, l, store);
        accountHandler = atrl;
    }

    /**
     * Regular update call from AccountHandler to refresh data according to refreshInterval
     */
    public void websocketUpdate() {
        scheduler.execute(this::doRefresh);
    }

    /**
     * If proto update is received an acknowledge message needs to be sent to the server to avoid repeating this message
     * again and again.
     */
    public void sendAcknowledgeMessage(ClientMessage message) {
        Session localSession = session;
        if (localSession != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                localSession.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
            } catch (IOException e) {
                logger.warn("Error sending acknowledge {} : {}", message.getAllFields(), e.getMessage());
            }
        }
    }

    /**
     * Add command to the command queue to be sent to the server. Immediately request refresh to send command as soon as
     * possible.
     *
     * @param command to be sent
     */
    public void websocketAddCommand(ClientMessage command) {
        commandQueue.add(command);
        // add time to execute command and websocket can cover updates
        runTill = Instant.now().plusMillis(ADDON_MESSAGE_TIME_MS);
        scheduler.execute(this::doRefresh);
    }

    /**
     * Dispose websocket in case of disposed AccountHandler. Cleanup stored files and stop web socket client.
     */
    public void websocketDispose(boolean disposed) {
        this.disposed = disposed;
        if (disposed) {
            runTill = Instant.MIN;
            keepAlive = false;
            ScheduledFuture<?> localRefresher = refresher;
            if (localRefresher != null) {
                localRefresher.cancel(false);
            }
            refresher = null;
            scheduler.execute(this::stop);
        }
    }

    /**
     * Set keep alive mode for web socket connection. If keep alive is set to true the web socket will not be closed
     *
     * @param alive
     */
    public void websocketKeepAlive(boolean alive) {
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
            if (logger.isTraceEnabled()) {
                logger.trace("Send Message {}", message.getAllFields());
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                Session localSession = session;
                if (localSession != null) {
                    localSession.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
                    logger.trace("Send Message {} done", message.getAllFields());
                    return true;
                } else {
                    logger.warn("Cannot send message {} - no session available", message.getAllFields());
                    return false;
                }
            } catch (IOException e) {
                logger.warn("Error sending message {} : {}", message.getAllFields(), e.getMessage());
            }
        }
        return false;
    }

    /**
     * Start the web socket client and connect to the server. If the web socket client is already running it will not
     */
    private void start() {
        if (!disposed && webSocketClient == null) {
            WebSocketClient localWebSocketClient = new WebSocketClient(httpClient);
            try {
                localWebSocketClient.setMaxIdleTimeout(WS_IDLE_TIMEOUT_MS);
                ClientUpgradeRequest request = getClientUpgradeRequest();
                String websocketURL = Utils.getWebsocketServer(config.region);
                logger.trace("Websocket start {} max message size {}", websocketURL,
                        localWebSocketClient.getMaxBinaryMessageSize());
                runTill = Instant.now().plusMillis(nextRuntimeMillis());
                localWebSocketClient.start();
                localWebSocketClient.connect(this, new URI(websocketURL), request);
                webSocketClient = localWebSocketClient;
                state = WebsocketState.STARTED;
            } catch (Exception e) {
                // catch Exceptions of start stop and declare communication error
                accountHandler.handleWebsocketError(e);
            }
        }
    }

    /**
     * Don't act like a bot! A fixed WebSocket lifetime would be an easy fingerprint, so the standard runtime
     * is randomized between WS_RUNTIME_MIN_MS and WS_RUNTIME_MAX_MS (1-3 minutes) for every connect.
     *
     * @return randomized runtime in milliseconds
     */
    private long nextRuntimeMillis() {
        return WS_RUNTIME_MIN_MS + (long) (Math.random() * (WS_RUNTIME_MAX_MS - WS_RUNTIME_MIN_MS));
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
        if (disposed) {
            logger.trace("Refresh: Websocket disposed - state {}", state);
            return;
        }

        switch (state) {
            case CONNECTED:
                handleConnectedState();
                break;
            default:
                logger.trace("Refresh: Websocket needs to be started - state {}", state);
                scheduler.execute(this::start);
                break;
        }
    }

    private void handleConnectedState() {
        logger.trace("Refresh: Websocket fine - state {}", state);
        if (sendMessage()) {
            // add additional runtime to execute and finish command
            runTill = runTill.plusMillis(ADDON_MESSAGE_TIME_MS);
        }
        sendPing();
        if (keepAlive || Instant.now().isBefore(runTill)) {
            // doRefresh is called by AccountHandler, websocket endpoint onConnect and addCommand. To avoid
            // multiple future calls cancel the current running or future schedule calls.
            ScheduledFuture<?> localRefresher = refresher;
            if (localRefresher != null) {
                localRefresher.cancel(false);
                refresher = scheduler.schedule(this::doRefresh, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
            }
        } else {
            logger.debug("Websocket run time is over - disconnect");
            // this is a deliberate close (idle timeout reached) - don't let onClosedSession reconnect
            intentionalClose = true;
            scheduler.execute(this::stop);
        }
    }

    /**
     * Request to disconnect the web socket session. This will close the session normally with a status code of 1000
     */
    private void disconnect() {
        Session localSession = session;
        if (localSession != null) {
            // close session normally
            localSession.close(1000, "Client shutdown");
        }
    }

    /**
     * Stop the web socket client and disconnect the session if it is still connected.
     */
    private void stop() {
        Session localSession = session;
        if (localSession != null) {
            logger.trace("Websocket stop - disconnect session first - state {}", state);
            scheduler.execute(this::disconnect);
        } else {
            logger.trace("Websocket stop - state {}", state);
            WebSocketClient localWebsocketClient = webSocketClient;
            if (localWebsocketClient != null) {
                try {
                    localWebsocketClient.stop();
                } catch (Exception e) {
                    logger.warn("Websocket stop exception: {}", e.getMessage());
                }
                localWebsocketClient.destroy();
                webSocketClient = null;
            }
            state = WebsocketState.STOPPED;
        }
    }

    /**
     * Ping the server to keep the connection alive and to check if the connection is still valid.
     * No payload is sent - matches Mercedes-Me App behaviour (OkHttp's automatic pingInterval sends an
     * empty WebSocket ping control frame, not an application-level payload).
     */
    private void sendPing() {
        Session localSession = session;
        if (localSession != null) {
            try {
                pingSentAt = Instant.now();
                localSession.getRemote().sendPing(ByteBuffer.allocate(0));
            } catch (IOException e) {
                logger.warn("Websocket ping failed {}", e.getMessage());
            }
        }
    }

    private void handlePong(Frame frame) {
        Instant sent = pingSentAt;
        if (sent == null) {
            logger.trace("Websocket received pong without matching ping");
        } else {
            pingSentAt = null;
        }
    }

    private void handlePing(Frame frame) {
        Session localSession = session;
        if (localSession != null) {
            ByteBuffer buffer = frame.getPayload();
            try {
                localSession.getRemote().sendPong(buffer);
            } catch (IOException e) {
                logger.warn("Websocket onPing answer exception {}", e.getMessage());
            }
        } else {
            logger.debug("Websocket onPing answer cannot be initiated");
        }
    }

    private ClientUpgradeRequest getClientUpgradeRequest() {
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("Authorization", getToken());
        request.setHeader("X-SessionId", UUID.randomUUID().toString());
        request.setHeader("X-TrackingId", UUID.randomUUID().toString());
        request.setHeader("Ris-Os-Name", Constants.RIS_OS_NAME);
        request.setHeader("Ris-Os-Version", Constants.RIS_OS_VERSION);
        request.setHeader("Ris-Sdk-Version", Utils.getRisSDKVersion(config.region));
        request.setHeader("X-Locale",
                localeProvider.getLocale().getLanguage() + "-" + localeProvider.getLocale().getCountry()); // de-DE
        request.setHeader("User-Agent", Utils.getApplication(config.region));
        request.setHeader("X-Applicationname", Utils.getUserAgent(config.region));
        request.setHeader("Ris-Application-Version", Utils.getRisApplicationVersion(config.region));
        return request;
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
            accountHandler.enqueueMessage(pm);
            logger.trace("Websocket Message {} size {}", pm.getMsgCase(), pm.getAllFields().size());
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
            handlePong(frame);
        } else if (Frame.Type.PING.equals(frame.getType())) {
            handlePing(frame);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        state = WebsocketState.CONNECTED;
        pingSentAt = null;
        reconnectAttempts = 0;
        accountHandler.handleConnected();
        logger.trace("Websocket connected - state {}", state);
        // websocket client is started and connected - time to refresh
        scheduler.execute(this::doRefresh);
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.trace("Disconnected from server. Status {} Reason {}", statusCode, reason);
        onClosedSession(null);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        onClosedSession(t);
    }

    /**
     * Handles both a server-initiated close (onDisconnect) and a transport error (onError). Mirrors the
     * Mercedes-Me App's ReconnectableSocketConnection: unless this was our own deliberate idle-timeout
     * close or the binding is disposed, retry up to MAX_RECONNECT_RETRIES times with RECONNECT_DELAY_MS
     * between attempts before falling back to a full re-authorization.
     */
    private void onClosedSession(@Nullable Throwable throwable) {
        session = null;
        state = WebsocketState.DISCONNECTED;
        pingSentAt = null;
        // stop web socket client for closed session
        scheduler.execute(this::stop);

        boolean skipReconnect = disposed || intentionalClose;
        intentionalClose = false;
        if (skipReconnect) {
            return;
        }

        if (throwable != null) {
            logger.debug("Websocket onClosedSession exception: {}", throwable.getMessage());
        }
        if (reconnectAttempts < MAX_RECONNECT_RETRIES) {
            reconnectAttempts++;
            logger.trace("Websocket reconnect attempt {}/{} in {} ms", reconnectAttempts, MAX_RECONNECT_RETRIES,
                    RECONNECT_DELAY_MS);
            scheduler.schedule(this::start, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            logger.debug("Websocket max reconnect attempts exceeded - falling back to re-authorization");
            reconnectAttempts = 0;
            if (throwable != null) {
                accountHandler.handleWebsocketError(throwable);
            }
            accountHandler.authorize();
        }
    }
}
