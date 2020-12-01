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
package org.openhab.binding.sony.internal.transports;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.sony.internal.ExpiringMap;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This sony transport will handle communicating over a web socket communication using the REST API. Basically
 * everything is sent as a ScalarWebRequest (that has been serialized via json) and you'll get back a ScalarWebResponse
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyWebSocketTransport extends AbstractSonyTransport {
    /** The looger */
    private final Logger logger = LoggerFactory.getLogger(SonyWebSocketTransport.class);

    /** The expiration (in seconds) during a websocket connect */
    private static final int CONN_EXPIRE_TIMEOUT_SECONDS = 10;

    /** The expiration (in seconds) to get a response from a command */
    private static final int CMD_EXPIRE_TIMEOUT_SECONDS = 30;

    /** The interval (in seconds) to send a ping to the device */
    private static final int PING_SECONDS = 5;

    /** The websocket URI */
    private final URI uri;

    /** The GSON used to serialize/deserialize */
    private final Gson gson;

    /**
     * The map of pending commands [futures] by command id. Any time a command (ScalarWebRequest) is sent, a future is
     * created by that command id. When the respond to that command is received, the corresponding future (by is) is
     * completed.
     */
    private final ExpiringMap<Integer, CompletableFuture<TransportResult>> futures;

    /** The websocket sessions being used */
    private @Nullable Session session;

    /** Current ping number (meaningless really - just an increasing number) */
    private int ping = 0;

    /** The ping task */
    private final @Nullable ScheduledFuture<?> pingTask;

    /**
     * Creates the websocket transport
     * 
     * @param webSocketClient a non-null websocket client
     * @param uri a non-null websocket URI
     * @param gson a non-null GSON
     * @param scheduler a potentially null scheduler
     * @throws InterruptedException if a task was interrupted
     * @throws ExecutionException if a task had an execution exception
     * @throws TimeoutException if we timed out connecting to the websocket
     * @throws IOException if an IO exception occurred
     */
    public SonyWebSocketTransport(final WebSocketClient webSocketClient, final URI uri, final Gson gson,
            final @Nullable ScheduledExecutorService scheduler)
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        super(uri);
        Objects.requireNonNull(webSocketClient, "webSocketClient cannot be null");
        Objects.requireNonNull(uri, "uri cannot be null");
        Objects.requireNonNull(gson, "gson cannot be null");

        this.gson = gson;
        this.uri = uri;

        futures = new ExpiringMap<Integer, CompletableFuture<TransportResult>>(scheduler, CMD_EXPIRE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        futures.addExpireListener((k, v) -> {
            logger.debug("Execution of {} took too long and is being cancelled", k);
            v.cancel(true);
        });

        logger.debug("Starting websocket connection to {}", uri);
        webSocketClient.connect(new WebSocketCallback(), uri, new ClientUpgradeRequest())
                .get(CONN_EXPIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        logger.debug("Websocket connection successful to {}", uri);

        // Setup pinging to prevent connection from timing out due to inactivity
        // Note: this is probably overkill but is an easy thing to do
        if (scheduler == null) {
            logger.debug("No scheduler specified - pinging disabled");
            pingTask = null;
        } else {
            pingTask = scheduler.scheduleWithFixedDelay(() -> {
                final Session localSession = session;
                if (localSession != null) {
                    final RemoteEndpoint remote = localSession.getRemote();

                    final ByteBuffer payload = ByteBuffer.allocate(4).putInt(ping++);
                    try {
                        logger.debug("Pinging {}", uri);
                        remote.sendPing(payload);
                    } catch (final IOException e) {
                        logger.debug("Pinging {} failed: {}", uri, e.getMessage());
                    }
                }
            }, PING_SECONDS, PING_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public String getProtocolType() {
        return SonyTransportFactory.WEBSOCKET;
    }

    @Override
    public void close() {
        futures.close();

        // if there is an old web socket then clean up and destroy
        final Session localSession = session;
        if (localSession != null && !localSession.isOpen()) {
            logger.debug("Closing session {}", uri);
            try {
                localSession.close();
            } catch (final Exception e) {
                logger.debug("Closing of session {} failed: {}", uri, e.getMessage(), e);
            }
        }
        session = null;

        if (pingTask != null) {
            SonyUtil.cancel(pingTask);
        }
    }

    @Override
    public CompletableFuture<TransportResult> execute(final TransportPayload payload,
            final TransportOption... options) {
        if (!(payload instanceof TransportPayloadScalarWebRequest)) {
            throw new IllegalArgumentException(
                    "payload must be a TransportPayloadRequest: " + payload.getClass().getName());
        }

        final Session localSession = session;
        if (localSession == null) {
            return CompletableFuture.completedFuture(new TransportResultScalarWebResult(new ScalarWebResult(
                    HttpStatus.INTERNAL_SERVER_ERROR_500, "No session established yet - wait for it to be connected")));
        }

        final ScalarWebRequest cmd = ((TransportPayloadScalarWebRequest) payload).getRequest();
        final String jsonRequest = gson.toJson(cmd);

        final CompletableFuture<TransportResult> future = new CompletableFuture<>();
        futures.put(cmd.getId(), future);

        logger.debug("Sending {} to {}", jsonRequest, uri);

        // Use the async message (we don't really care about the returned future)
        localSession.getRemote().sendStringByFuture(jsonRequest);
        return future;
    }

    /**
     * The following class provides the callback method to handle websocket events
     */
    @NonNullByDefault
    @WebSocket
    public class WebSocketCallback {
        /**
         * Called when the websocket client has connected
         * 
         * @param session the websocket session (shouldn't be null)
         */
        @OnWebSocketConnect
        public void onConnect(final @Nullable Session session) {
            logger.trace("websocket.onConnect({})", session);
            if (session == null) {
                logger.debug("Connected to a session that was null - weird!");
            } else {
                logger.debug("Connected successfully to server {}", uri);
                SonyWebSocketTransport.this.session = session;
            }
        }

        /**
         * Called when a message was received from the websocket connection
         * 
         * @param message a potentially null (shouldn't really happen), potentially empty (could happen) message
         */
        @OnWebSocketMessage
        public void onMessage(final @Nullable String message) {
            logger.trace("websocket.onMessage({})", message);
            if (message == null || StringUtils.isEmpty(message)) {
                logger.debug("Received an empty message - ignoring");
            } else {
                try {
                    final JsonObject json = gson.fromJson(message, JsonObject.class);
                    if (json.has("id")) {
                        final ScalarWebResult result = gson.fromJson(json, ScalarWebResult.class);
                        final Integer resultId = result.getId();
                        if (resultId == null) {
                            logger.debug("Response from server has an unknown id: {}", message);
                        } else {
                            final CompletableFuture<TransportResult> future = futures.get(resultId);
                            if (future != null) {
                                logger.debug("Response received from server: {}", message);
                                futures.remove(resultId);
                                future.complete(new TransportResultScalarWebResult(result));
                            } else {
                                logger.debug(
                                        "Response received from server but a waiting command wasn't found - ignored: {}",
                                        message);
                            }
                        }
                    } else {
                        final ScalarWebEvent event = gson.fromJson(json, ScalarWebEvent.class);
                        logger.debug("Event received from server: {}", message);
                        fireEvent(event);
                    }
                } catch (final JsonParseException e) {
                    logger.debug("JSON parsing error: {} for {}", e.getMessage(), message, e);
                }
            }
        }

        /**
         * Called when an exception has occurred on the websocket connection
         * 
         * @param t a potentially null (shouldn't happen) error
         */
        @OnWebSocketError
        public void onError(final @Nullable Throwable t) {
            if (t == null) {
                logger.debug("Received a null throwable in onError - ignoring");
            } else {
                logger.trace("websocket.onError({})", t.getMessage(), t);
                if (t instanceof UpgradeException) {
                    final UpgradeException e = (UpgradeException) t;
                    // 404 happens when the individual service has no websocket connection
                    // but there is a websocket server listening for other services
                    if (e.getResponseStatusCode() == HttpStatus.NOT_FOUND_404) {
                        logger.debug("No websocket listening for specific service {}", e.getRequestURI());
                        return;
                    } else if (e.getResponseStatusCode() == 0) {
                        // Weird second exception thrown when you get a connection refused
                        // when using upgrade - ignore this since it was logged below
                        return;
                    }
                }

                // suppress stack trace on connection refused
                if (StringUtils.containsIgnoreCase(t.getMessage(), "connection refused")) {
                    logger.debug("Connection refused for {}: {}", uri, t.getMessage());
                    return;
                }

                // suppress stack trace on connection refused
                if (StringUtils.containsIgnoreCase(t.getMessage(), "idle timeout")) {
                    logger.debug("Idle Timeout for {}: {}", uri, t.getMessage());
                    return;
                }

                logger.debug("Exception occurred during websocket communication for {}: {}", uri, t.getMessage(), t);
                fireOnError(t);
            }
        }

        /**
         * Called when the websocket connection has been closed
         * 
         * @param statusCode the status code for the close
         * @param reason the reason of the close
         */
        @OnWebSocketClose
        public void onClose(final int statusCode, final String reason) {
            logger.trace("websocket.onClose({}, {})", statusCode, reason);

            final Session localSession = session;
            if (localSession != null) {
                logger.debug("Closing session from close event {}", uri);
                localSession.close();
            }
            session = null;
        }
    }
}
