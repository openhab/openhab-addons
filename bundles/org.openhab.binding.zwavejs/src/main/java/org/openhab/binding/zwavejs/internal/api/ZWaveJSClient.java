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
package org.openhab.binding.zwavejs.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.adapter.InstantAdapter;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ServerInitializeCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.ServerListeningCommand;
import org.openhab.binding.zwavejs.internal.api.dto.messages.BaseMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.VersionMessage;
import org.openhab.binding.zwavejs.internal.api.exception.CommunicationException;
import org.openhab.binding.zwavejs.internal.handler.ZwaveEventListener;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.ToNumberPolicy;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

/**
 * The {@code ZWaveJSClient} class is responsible for managing the WebSocket connection
 * to the Z-Wave JS Webservice. It implements the {@link WebSocketListener} interface to
 * handle WebSocket events such as connection, disconnection, and message reception.
 *
 * <p>
 * This class provides methods to start and stop the WebSocket connection, as well as
 * to add and remove event listeners for Z-Wave events. It also includes functionality
 * to send commands to the Z-Wave JS server.
 *
 * <p>
 * Thread Safety: This class is thread-safe. It uses a {@link CopyOnWriteArraySet} for
 * managing event listeners and ensures that the WebSocket session is accessed in a
 * thread-safe manner.
 *
 * @see WebSocketListener
 * @see WebSocketClient
 * @see BaseMessage
 * @see BaseCommand
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZWaveJSClient implements WebSocketListener {

    private static final int RECONNECT_INTERVAL_MINUTES = 2;
    private static final String BINDING_SHUTDOWN_MESSAGE = "Binding shutdown";

    private final Logger logger = LoggerFactory.getLogger(ZWaveJSClient.class);
    private final WebSocketClient wsClient;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(BindingConstants.BINDING_ID);
    private final Gson gson;

    private final Set<ZwaveEventListener> listeners = new CopyOnWriteArraySet<>();
    private final Object lifecycleLock = new Object();
    private final Object sendLock = new Object();

    private volatile int bufferSize = 1048576 * 2; // 2 MiB
    private volatile @Nullable Session session;
    private @Nullable Future<?> sessionFuture;
    private @Nullable ScheduledFuture<?> keepAliveFuture;
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private volatile String uri = "";
    private volatile boolean shuttingDown = false;

    public ZWaveJSClient(WebSocketClient wsClient) {
        this.wsClient = wsClient;
        RuntimeTypeAdapterFactory<BaseMessage> typeAdapterFactory = RuntimeTypeAdapterFactory.of(BaseMessage.class,
                "type", true);
        typeAdapterFactory.registerSubtype(VersionMessage.class, "version")
                .registerSubtype(ResultMessage.class, "result").registerSubtype(EventMessage.class, "event");

        this.gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .registerTypeAdapter(Instant.class, new InstantAdapter()).registerTypeAdapterFactory(typeAdapterFactory)
                .create();
    }

    /**
     * Initiates a WebSocket connection to the Z-Wave JS Webservice at the specified URI.
     * 
     * <p>
     * This method attempts to establish a connection to the Z-Wave JS server. If a connection
     * or connection attempt is already active, the method returns silently without creating
     * a duplicate connection. If the client is in the process of shutting down, this method
     * returns without attempting to connect.
     * 
     * @param uri the URI of the Z-Wave JS Webservice (e.g., "ws://localhost:3000/")
     * @throws CommunicationException if an {@link IOException} or {@link URISyntaxException}
     *             occurs during the connection attempt, or if the URI is invalid
     * 
     * @see #stop()
     * @see #onWebSocketConnect(Session)
     * @see #onWebSocketError(Throwable)
     */
    public void start(String uri) throws CommunicationException {
        logger.debug("Connecting to Z-Wave JS Webservice");
        synchronized (lifecycleLock) {
            this.uri = uri;
            startLocked();
        }
    }

    private void startLocked() throws CommunicationException {
        if (shuttingDown) {
            return;
        }
        if (hasActiveConnectionOrAttempt()) {
            logger.debug("Skipping connect attempt because a connection or connection attempt is already active");
            return;
        }
        try {
            shuttingDown = false;
            sessionFuture = wsClient.connect(this, new URI(uri));
        } catch (IOException | URISyntaxException e) {
            if (e.getCause() instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new CommunicationException("Failed to connect to Z-Wave JS Webservice (" + uri + ")", e);
        }
    }

    /**
     * Stops the WebSocket connection to the Z-Wave JS Webservice.
     */
    public void stop() {
        logger.debug("Disconnecting from Z-Wave JS Webservice");
        synchronized (lifecycleLock) {
            shuttingDown = true;
            closeSessionLocked(this.session, StatusCode.NORMAL, BINDING_SHUTDOWN_MESSAGE);
            this.session = null;

            stopReconnectJobLocked();
        }
    }

    /**
     * Adds a Z-Wave event listener to the list of listeners.
     *
     * @param listener the Z-Wave event listener to be added
     */
    public void addEventListener(ZwaveEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the specified event listener from the list of listeners.
     *
     * @param listener the event listener to be removed
     */
    public void removeEventListener(ZwaveEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onWebSocketClose(int statusCode, @NonNullByDefault({}) String reason) {
        logger.debug("Closing WebSocket with status code {}, reason '{}'", statusCode, reason);

        for (ZwaveEventListener listener : listeners) {
            try {
                listener.onConnectionError(String.format("Connection closed: %d, %s", statusCode, reason));
            } catch (Exception e) {
                logger.warn("Error invoking event listener on close", e);
            }
        }

        synchronized (lifecycleLock) {
            stopKeepAliveLocked();
            this.session = null;
            stopSessionFutureLocked();
        }

        if (statusCode == StatusCode.NORMAL && BINDING_SHUTDOWN_MESSAGE.equals(reason)) {
            logger.debug("Z-Wave JS Webservice closed normally");
            return;
        }
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        synchronized (lifecycleLock) {
            if (shuttingDown) {
                logger.debug("Not scheduling reconnect because client is shutting down");
                return;
            }
            ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
            if (reconnectFuture != null) {
                return;
            }

            logger.info("Scheduling reconnect to Z-Wave JS Webservice every {} minutes", RECONNECT_INTERVAL_MINUTES);
            this.reconnectFuture = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    synchronized (lifecycleLock) {
                        startLocked();
                    }
                } catch (RuntimeException | CommunicationException e) {
                    // silently ignore as the thing state is already updated when the connection is lost
                    logger.debug("Error while reconnecting to Z-Wave JS Webservice: {}", e.getMessage());
                }
            }, RECONNECT_INTERVAL_MINUTES, RECONNECT_INTERVAL_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void onWebSocketConnect(@NonNullByDefault({}) Session session) {
        synchronized (lifecycleLock) {
            if (shuttingDown) {
                logger.debug("WebSocket connected but client is shutting down, closing session");
                closeSessionLocked(session, StatusCode.SHUTDOWN, BINDING_SHUTDOWN_MESSAGE);
                this.session = null;
                return;
            }
            logger.debug("onWebSocketConnect('{}')", session);
            this.session = session;

            final WebSocketPolicy currentPolicy = session.getPolicy();
            currentPolicy.setInputBufferSize(bufferSize);
            currentPolicy.setMaxTextMessageSize(bufferSize);
            currentPolicy.setMaxBinaryMessageSize(bufferSize);

            keepAliveFuture = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    String data = "Ping";
                    ByteBuffer payload = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));
                    session.getRemote().sendPing(payload);
                } catch (IOException e) {
                    logger.warn("Problem sending periodic Ping: {}", e.getMessage());
                }
            }, 25, 25, TimeUnit.SECONDS);

            stopReconnectJobLocked();
        }
    }

    @Override
    public void onWebSocketError(@NonNullByDefault({}) Throwable cause) {
        Throwable localThrowable = (cause != null) ? cause
                : new IllegalStateException("Null Exception passed to onWebSocketError");
        logger.debug("Error during websocket communication: {}", localThrowable.getMessage());

        synchronized (lifecycleLock) {
            if (this.session != null) {
                stopKeepAliveLocked();
                closeSessionLocked(this.session, StatusCode.SERVER_ERROR, "Error: " + localThrowable.getMessage());
                this.session = null;
            }
        }

        notifyListenersOnError(String.format("Error: %s", localThrowable.getMessage()));
        scheduleReconnect();
    }

    @Override
    public void onWebSocketBinary(@NonNullByDefault({}) byte[] payload, int offset, int len) {
        logger.debug("Ignoring unsupported binary websocket message (offset={}, len={})", offset, len);
    }

    @Override
    public void onWebSocketText(@NonNullByDefault({}) String message) {
        BaseMessage baseEvent = null;
        try {
            baseEvent = gson.fromJson(message, BaseMessage.class);
        } catch (JsonParseException ex) {
            logger.warn("Failed to parse incoming WebSocket message: {}", ex.getMessage());
            logger.trace("RECV | {}", message);
            notifyListenersOnError("Failed to parse message: " + ex.getMessage());
            return;
        }

        if (baseEvent == null || baseEvent.type == null) {
            logger.warn("Received event with unknown or null type.");
            logger.trace("RECV | {}", message);
            notifyListenersOnError("Received event with unknown or null type.");
            return;
        }

        logEventResponse(baseEvent, message);

        // Notify listeners
        for (ZwaveEventListener listener : listeners) {
            try {

                listener.onEvent(baseEvent);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error invoking event listener on websockettext: {}", e.toString(), e);
                } else {
                    logger.warn("Error invoking event listener on websockettext");
                }
            }
        }

        // Special handling for VersionMessage
        if (baseEvent instanceof VersionMessage) {
            // the binding is starting up, perform schema version handshake
            // also start listening to events
            sendCommand(new ServerInitializeCommand());
            sendCommand(new ServerListeningCommand());
        }
    }

    private void logEventResponse(BaseMessage baseEvent, String message) {
        if (baseEvent instanceof ResultMessage resultMessage) {
            if (resultMessage.success && (resultMessage.result != null && resultMessage.result.status != 5)) {
                logger.debug("Received ResultMessage type: {}, success: {}", baseEvent.type, resultMessage.success);
            } else {
                logger.warn("Received ResultMessage type: {}, success: {}, status: {}, error_code: {}, message: {}",
                        baseEvent.type, resultMessage.success,
                        resultMessage.result != null ? resultMessage.result.status : "null", resultMessage.errorCode,
                        resultMessage.message != null ? resultMessage.message : resultMessage.zwaveErrorMessage);
            }
        } else if (baseEvent instanceof EventMessage eventMessage) {
            logger.trace("Received EventMessage, type: {}", eventMessage.event.event);
        } else {
            logger.trace("Received message class type: {}, event type: {}", baseEvent.getClass().getSimpleName(),
                    baseEvent.type);
        }
        logger.trace("RECV | {}", message);
    }

    private void notifyListenersOnError(String errorMsg) {
        for (ZwaveEventListener listener : listeners) {
            try {
                listener.onConnectionError(errorMsg);
            } catch (Exception e) {
                logger.warn("Error invoking event listener on error", e);
            }
        }
    }

    /**
     * Sends a command to the remote endpoint in JSON format.
     * Converts the given {@link BaseCommand} object to a JSON string and sends it
     * using the active session's remote endpoint.
     * 
     * @param command The {@link BaseCommand} object to be sent.
     */
    public void sendCommand(BaseCommand command) {
        String commandAsJson = gson.toJson(command);
        Session session = this.session;
        try {
            if (session == null || !(session.getRemote() instanceof RemoteEndpoint endpoint)) {
                logger.warn("Failed while sending command: {}. Problem with session or remote endpoint",
                        command.getClass());
                return;
            }
            logger.debug("Sending command: {}.", command.getClass().getSimpleName());
            logger.trace("SEND | {}", commandAsJson);
            synchronized (sendLock) {
                endpoint.sendString(commandAsJson);
            }
        } catch (IOException e) {
            logger.warn("IOException while sending command: {}. Error {}", command.getClass().getSimpleName(),
                    e.getMessage());
        }
    }

    public void setBufferSize(int maxMessageSize) {
        bufferSize = maxMessageSize;
    }

    private void closeSessionLocked(@Nullable Session session, int statusCode, String reason) {
        if (session != null) {
            stopKeepAliveLocked();
            try {
                session.close(statusCode, reason);
            } catch (Exception e) {
                logger.warn("Error while closing websocket session: {}", e.getMessage());
            }
        }

        stopSessionFutureLocked();
    }

    private void stopReconnectJobLocked() {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
            this.reconnectFuture = null;
        }
    }

    private void stopSessionFutureLocked() {
        Future<?> sessionFuture = this.sessionFuture;
        if (sessionFuture != null) {
            sessionFuture.cancel(true);
            this.sessionFuture = null;
        }
    }

    private void stopKeepAliveLocked() {
        ScheduledFuture<?> keepAliveFuture = this.keepAliveFuture;
        if (keepAliveFuture != null) {
            keepAliveFuture.cancel(true);
        }
        this.keepAliveFuture = null;
    }

    private boolean hasActiveConnectionOrAttempt() {
        Session session = this.session;
        if (session != null && session.isOpen()) {
            return true;
        }
        Future<?> sessionFuture = this.sessionFuture;
        return sessionFuture != null && !sessionFuture.isDone();
    }

    /**
     * Disposes the client by clearing the connection and scheduled futures.
     */
    public void dispose() {
        logger.debug("Disposing ZWaveJSClient");

        stop();

        listeners.clear();
        logger.debug("ZWaveJSClient disposed");
    }
}
