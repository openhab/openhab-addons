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

import javax.naming.CommunicationException;

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

    private Logger logger = LoggerFactory.getLogger(ZWaveJSClient.class);
    private int bufferSize = 1048576 * 2; // 2 MiB
    private static final int RECONNECT_INTERVAL_MINUTES = 2;
    private static final String BINDING_SHUTDOWN_MESSAGE = "Binding shutdown";

    private final WebSocketClient wsClient;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(BindingConstants.BINDING_ID);
    private volatile @Nullable Session session;
    private final Set<ZwaveEventListener> listeners = new CopyOnWriteArraySet<>();
    private @Nullable Future<?> sessionFuture;
    private @Nullable ScheduledFuture<?> keepAliveFuture;
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private final Gson gson;
    private final Object sendLock = new Object();
    private String uri = "";

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
     * Starts the WebSocket connection to the Z-Wave JS Webservice.
     *
     * @param uri the URI of the WebSocket server
     * 
     * @throws CommunicationException if there is an error during communication
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    public void start(String uri) throws CommunicationException, InterruptedException {
        logger.debug("Connecting to Z-Wave JS Webservice");
        this.uri = uri;
        try {
            sessionFuture = wsClient.connect(this, new URI(uri));
        } catch (IOException | URISyntaxException e) {
            throw new CommunicationException("Failed to connect to Z-Wave JS Webservice: " + e.getMessage());
        }
    }

    /**
     * Stops the WebSocket connection to the Z-Wave JS Webservice.
     */
    public void stop() {
        logger.debug("Disconnecting from Z-Wave JS Webservice");
        Session localSession = this.session;
        if (localSession != null) {
            stopKeepAlive();
            try {
                localSession.close(StatusCode.NORMAL, BINDING_SHUTDOWN_MESSAGE);
            } catch (Exception e) {
                logger.debug("Error while closing websocket communication: {} ({})", e.getClass().getName(),
                        e.getMessage());
            }
            session = null;
        }

        Future<?> localSessionFuture = sessionFuture;
        if (localSessionFuture != null) {
            localSessionFuture.cancel(true);
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
        logger.debug("onWebSocketClose({}, '{}')", statusCode, reason);

        try {
            for (ZwaveEventListener listener : listeners) {
                listener.onConnectionError(String.format("Connection closed: %d, %s", statusCode, reason));
            }
        } catch (Exception e) {
            logger.warn("Error invoking event listener on close", e);
        }
        stopKeepAlive();
        session = null;
        sessionFuture = null;
        if (statusCode == StatusCode.NORMAL && BINDING_SHUTDOWN_MESSAGE.equals(reason)) {
            logger.debug("Z-Wave JS Webservice closed normally");
            return;
        }
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null) {
            return;
        }
        logger.info("Scheduling reconnect to Z-Wave JS Webservice every {} minutes", RECONNECT_INTERVAL_MINUTES);
        this.reconnectFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                start(this.uri);
                ScheduledFuture<?> future = this.reconnectFuture;
                if (future != null) {
                    future.cancel(false);
                    this.reconnectFuture = null;
                }
            } catch (CommunicationException | InterruptedException e) {
                // silently ignore as the thing state is already updated when the connection is lost
                logger.debug("Error while reconnecting to Z-Wave JS Webservice: {}", e.getMessage());
            }
        }, RECONNECT_INTERVAL_MINUTES, RECONNECT_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void stopKeepAlive() {
        ScheduledFuture<?> keepAliveFuture = this.keepAliveFuture;
        if (keepAliveFuture != null) {
            keepAliveFuture.cancel(true);
        }
        this.keepAliveFuture = null;
    }

    @Override
    public void onWebSocketConnect(@NonNullByDefault({}) Session session) {
        logger.debug("onWebSocketConnect('{}')", session);
        this.session = session;
        if (session != null) {
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
                    logger.warn("Problem starting periodic Ping. {}", e.getMessage());
                }
            }, 25, 25, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onWebSocketError(@NonNullByDefault({}) Throwable cause) {
        Throwable localThrowable = (cause != null) ? cause
                : new IllegalStateException("Null Exception passed to onWebSocketError");
        logger.debug("Error during websocket communication: {}", localThrowable.getMessage());

        Session localSession = session;
        if (localSession != null) {
            stopKeepAlive();
            try {
                // Close the session with an error status code
                localSession.close(StatusCode.SERVER_ERROR, "Error: " + localThrowable.getMessage());
            } catch (Exception e) {
                logger.warn("Error while closing websocket session: {}", e.getMessage());
            }

            session = null;
        }

        notifyListenersOnError(String.format("Error: %s", localThrowable.getMessage()));
    }

    @Override
    public void onWebSocketBinary(@NonNullByDefault({}) byte[] payload, int offset, int len) {
        throw new UnsupportedOperationException("Unimplemented method 'onWebSocketBinary'");
    }

    @Override
    public void onWebSocketText(@NonNullByDefault({}) String message) {
        if (message.contains("\"event\":\"statistics updated\"")) {
            return;
        }

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
        try {
            for (ZwaveEventListener listener : listeners) {
                listener.onEvent(baseEvent);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error invoking event listener on websockettext: {}", e.toString());
            } else {
                logger.warn("Error invoking event listener on websockettext");
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
            logger.debug("SEND | {}", commandAsJson);
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

    /**
     * Disposes the client by clearing the connection and scheduled futures.
     */
    public void dispose() {
        logger.debug("Disposing ZWaveJSClient");
        stop(); // Ensure the connection is stopped

        ScheduledFuture<?> localReconnectFuture = reconnectFuture;
        if (localReconnectFuture != null) {
            localReconnectFuture.cancel(true);
            reconnectFuture = null;
        }

        stopKeepAlive();

        Future<?> localSessionFuture = sessionFuture;
        if (localSessionFuture != null) {
            if (!localSessionFuture.isDone()) {
                localSessionFuture.cancel(true);
            }
            sessionFuture = null;
        }

        listeners.clear();
        logger.debug("ZWaveJSClient disposed");
    }
}
