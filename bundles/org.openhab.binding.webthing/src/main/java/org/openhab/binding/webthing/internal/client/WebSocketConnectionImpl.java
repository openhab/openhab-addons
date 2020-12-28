/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPingPongListener;
import org.jetbrains.annotations.NotNull;
import org.openhab.binding.webthing.internal.client.dto.PropertyStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The WebsocketConnection implementation
 *
 * @author Gregor Roth - Initial contribution
 */
public class WebSocketConnectionImpl implements WebSocketConnection, WebSocketListener, WebSocketPingPongListener {
    private static final BiConsumer<String, Object> EMPTY_PROPERTY_CHANGED_LISTENER = (String propertyName,
            Object value) -> {
    };
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnectionImpl.class);
    private final Gson gson = new Gson();
    private final Duration pingPeriod;
    private final Consumer<String> errorHandler;
    private final ScheduledFuture<?> watchDogHandle;
    private final ScheduledFuture<?> pingHandle;
    private final Map<String, BiConsumer<String, Object>> propertyChangedListeners = new HashMap<>();
    private final AtomicReference<Instant> lastTimeReceived = new AtomicReference<>(Instant.now());
    private final AtomicReference<Session> sessionRef = new AtomicReference<>(null);

    /**
     * constructor
     *
     * @param executor the executor to use
     * @param errorHandler the errorHandler
     * @param pingPeriod the period pings should be sent
     */
    WebSocketConnectionImpl(ScheduledExecutorService executor, Consumer<String> errorHandler, Duration pingPeriod) {
        this.errorHandler = errorHandler;
        this.pingPeriod = pingPeriod;

        // send a ping message are x seconds to validate if the connection is not broken
        this.pingHandle = executor.scheduleWithFixedDelay(this::sendPing, pingPeriod.dividedBy(2).toMillis(),
                pingPeriod.toMillis(), TimeUnit.MILLISECONDS);

        // checks if a message (regular message or pong message) has been received recently. If not, connection is
        // seen as broken
        this.watchDogHandle = executor.scheduleWithFixedDelay(this::checkConnection, pingPeriod.toMillis(),
                pingPeriod.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        var session = sessionRef.getAndSet(null);
        if (session != null) {
            session.close();
        }
        watchDogHandle.cancel(true);
        pingHandle.cancel(true);
    }

    @Override
    public void observeProperty(@NotNull String propertyName, @NotNull BiConsumer<String, Object> listener) {
        propertyChangedListeners.put(propertyName, listener);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        sessionRef.set(session); // save websocket session to be able to send ping
    }

    @Override
    public void onWebSocketPing(ByteBuffer payload) {
    }

    @Override
    public void onWebSocketPong(ByteBuffer payload) {
        lastTimeReceived.set(Instant.now());
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
    }

    @Override
    public void onWebSocketText(String message) {
        try {
            var propertyStatus = gson.fromJson(message, PropertyStatusMessage.class);
            if ((propertyStatus != null) && (propertyStatus.messageType != null)
                    && (propertyStatus.messageType.equals("propertyStatus"))) {
                for (var propertyEntry : propertyStatus.data.entrySet()) {
                    var listener = propertyChangedListeners.getOrDefault(propertyEntry.getKey(),
                            EMPTY_PROPERTY_CHANGED_LISTENER);
                    try {
                        listener.accept(propertyEntry.getKey(), propertyEntry.getValue());
                    } catch (RuntimeException re) {
                        logger.warn("calling property change listener {} failed. {}", listener, re.getMessage());
                    }
                }
            } else {
                logger.debug("Ignoring received message of unknown type: {}", message);
            }
        } catch (JsonSyntaxException se) {
            logger.warn("received invalid message: {}", message);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        onWebSocketError(new IOException("websocket closed by peer. " + Optional.ofNullable(reason).orElse("")));
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        onError(cause.getMessage());
    }

    private void onError(String message) {
        errorHandler.accept(message);
    }

    private void sendPing() {
        var session = sessionRef.get();
        if (session != null) {
            try {
                session.getRemote().sendPing(ByteBuffer.wrap(Instant.now().toString().getBytes()));
            } catch (IOException e) {
                onError("could not send ping " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isAlive() {
        var elapsedSinceLastReceived = Duration.between(lastTimeReceived.get(), Instant.now());
        var thresholdOverdued = pingPeriod.multipliedBy(3);
        var isOverdued = elapsedSinceLastReceived.toMillis() > thresholdOverdued.toMillis();
        return (sessionRef.get() != null) && !isOverdued;
    }

    private void checkConnection() {
        // check if connection is alive (message has been received recently)
        if (!isAlive()) {
            onError("connection seems to be broken (last message received at " + lastTimeReceived.get() + ", "
                    + Duration.between(lastTimeReceived.get(), Instant.now()).getSeconds() + " sec ago)");
        }
    }
}
