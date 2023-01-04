/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPingPongListener;
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
@NonNullByDefault
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
    private final AtomicReference<Optional<Session>> sessionRef = new AtomicReference<>(Optional.empty());

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
        sessionRef.getAndSet(Optional.empty()).ifPresent(Session::close);
        watchDogHandle.cancel(true);
        pingHandle.cancel(true);
    }

    @Override
    public void observeProperty(String propertyName, BiConsumer<String, Object> listener) {
        propertyChangedListeners.put(propertyName, listener);
    }

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        sessionRef.set(Optional.ofNullable(session)); // save websocket session to be able to send ping
    }

    @Override
    public void onWebSocketPing(@Nullable ByteBuffer payload) {
    }

    @Override
    public void onWebSocketPong(@Nullable ByteBuffer payload) {
        lastTimeReceived.set(Instant.now());
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        try {
            if (message != null) {
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
            }
        } catch (JsonSyntaxException se) {
            logger.warn("received invalid message: {}", message);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        onWebSocketError(new IOException("websocket closed by peer. " + Optional.ofNullable(reason).orElse("")));
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        var reason = "";
        if (cause != null) {
            reason = cause.getMessage();
        }
        onError(reason);
    }

    private void onError(@Nullable String message) {
        if (message == null) {
            message = "";
        }
        errorHandler.accept(message);
    }

    private void sendPing() {
        var optionalSession = sessionRef.get();
        if (optionalSession.isPresent()) {
            try {
                optionalSession.get().getRemote().sendPing(ByteBuffer.wrap(Instant.now().toString().getBytes()));
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
        return sessionRef.get().isPresent() && !isOverdued;
    }

    private void checkConnection() {
        // check if connection is alive (message has been received recently)
        if (!isAlive()) {
            onError("connection seems to be broken (last message received at " + lastTimeReceived.get() + ", "
                    + Duration.between(lastTimeReceived.get(), Instant.now()).getSeconds() + " sec ago)");
        }
    }
}
