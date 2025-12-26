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
package org.openhab.binding.jellyfin.internal.server;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for establishing and maintaining WebSocket connection to Jellyfin server.
 *
 * This task manages the WebSocket lifecycle:
 * - Establishes connection asynchronously via Jetty WebSocketClient
 * - Tracks connection state (DISCONNECTED, CONNECTING, CONNECTED, FAILED)
 * - Logs connection lifecycle events (connect, disconnect, errors)
 * - Delegates message parsing to WebSocketMessageHandler (Task 3)
 * - Provides proper resource cleanup on disposal
 *
 * The task extends AbstractTask to integrate with the existing TaskManager
 * infrastructure while adapting it for persistent connection management.
 * Connection establishment is asynchronous (non-blocking) to avoid tying
 * up the scheduler thread during network operations.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class WebSocketTask extends AbstractTask implements WebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(WebSocketTask.class);

    /** Task ID for the WebSocket task */
    public static final String TASK_ID = "WebSocket";

    /** Default startup delay for the WebSocket task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 0;

    /** Default interval for the WebSocket task in seconds (0 = run once) */
    public static final int DEFAULT_INTERVAL = 0;

    /** Connection timeout in seconds */
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;

    /** Maximum reconnection attempts before fallback to polling */
    private static final int MAX_RECONNECTION_ATTEMPTS = 10;

    /** Initial backoff delay in seconds */
    private static final int INITIAL_BACKOFF_SECONDS = 1;

    /** Maximum backoff delay in seconds (cap) */
    private static final int MAX_BACKOFF_SECONDS = 60;

    private final ApiClient apiClient;
    private final String apiToken;
    private final WebSocketMessageHandler messageHandler;
    private final AtomicReference<ConnectionState> connectionState;
    private final @Nullable Runnable onMaxRetriesExceeded;

    private @Nullable WebSocketClient webSocketClient;
    private @Nullable Session webSocketSession;
    private @Nullable Future<Session> connectFuture;
    private int reconnectionAttempts = 0;
    private int currentBackoffSeconds = INITIAL_BACKOFF_SECONDS;

    /**
     * Connection state for the WebSocket connection.
     */
    public enum ConnectionState {
        /** Not connected, no connection attempt in progress */
        DISCONNECTED,
        /** Connection attempt in progress */
        CONNECTING,
        /** Successfully connected and ready to receive messages */
        CONNECTED,
        /** Connection attempt failed or connection was closed due to error */
        FAILED
    }

    /**
     * Creates a new WebSocketTask.
     *
     * @param apiClient The API client for accessing server configuration
     * @param apiToken The API token for WebSocket authentication
     * @param messageHandler The handler that will process incoming WebSocket messages
     * @param onMaxRetriesExceeded Optional callback invoked when max reconnection attempts exceeded
     */
    public WebSocketTask(ApiClient apiClient, String apiToken, WebSocketMessageHandler messageHandler,
            @Nullable Runnable onMaxRetriesExceeded) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);
        this.apiClient = apiClient;
        this.apiToken = apiToken;
        this.messageHandler = messageHandler;
        this.connectionState = new AtomicReference<>(ConnectionState.DISCONNECTED);
        this.onMaxRetriesExceeded = onMaxRetriesExceeded;
    }

    /**
     * Creates a new WebSocketTask without fallback callback.
     *
     * @param apiClient The API client for accessing server configuration
     * @param apiToken The API token for WebSocket authentication
     * @param messageHandler The handler that will process incoming WebSocket messages
     */
    public WebSocketTask(ApiClient apiClient, String apiToken, WebSocketMessageHandler messageHandler) {
        this(apiClient, apiToken, messageHandler, null);
    }

    @Override
    public void run() {
        if (connectionState.get() == ConnectionState.CONNECTED || connectionState.get() == ConnectionState.CONNECTING) {
            logger.debug("WebSocket already connected or connecting, skipping connection attempt");
            return;
        }

        // Check if max reconnection attempts exceeded
        if (reconnectionAttempts >= MAX_RECONNECTION_ATTEMPTS) {
            logger.warn("WebSocket max reconnection attempts ({}) exceeded, triggering fallback to polling",
                    MAX_RECONNECTION_ATTEMPTS);
            connectionState.set(ConnectionState.FAILED);
            Runnable callback = this.onMaxRetriesExceeded;
            if (callback != null) {
                callback.run();
            }
            return;
        }

        try {
            // Apply exponential backoff delay for retry attempts
            if (reconnectionAttempts > 0) {
                logger.info("WebSocket reconnection attempt {}/{} after {}s backoff", reconnectionAttempts,
                        MAX_RECONNECTION_ATTEMPTS, currentBackoffSeconds);
            }

            connectionState.set(ConnectionState.CONNECTING);
            logger.info("WebSocket connecting to Jellyfin server...");

            // Construct WebSocket URI from API client configuration
            URI webSocketUri = buildWebSocketUri();
            logger.debug("WebSocket URI: {}", maskApiKey(webSocketUri.toString()));

            // Initialize Jetty WebSocket client
            WebSocketClient client = new WebSocketClient();
            this.webSocketClient = client;

            // Start client and connect asynchronously
            client.start();
            Future<Session> future = client.connect(this, webSocketUri);
            this.connectFuture = future;

            logger.debug("WebSocket connection initiated, waiting for callbacks...");

        } catch (Exception e) {
            connectionState.set(ConnectionState.FAILED);
            logger.error("Failed to initiate WebSocket connection: {}", e.getMessage(), e);
            handleConnectionFailure();
        }
    }

    /**
     * Handles connection failure by incrementing retry counter and calculating next backoff delay.
     * Uses exponential backoff: 1→2→4→8→16→32→60s (capped at 60s).
     */
    private void handleConnectionFailure() {
        reconnectionAttempts++;

        if (reconnectionAttempts < MAX_RECONNECTION_ATTEMPTS) {
            // Calculate next backoff delay with exponential growth
            currentBackoffSeconds = Math.min(INITIAL_BACKOFF_SECONDS * (1 << (reconnectionAttempts - 1)),
                    MAX_BACKOFF_SECONDS);
            logger.info("WebSocket will retry in {}s (attempt {}/{})", currentBackoffSeconds, reconnectionAttempts,
                    MAX_RECONNECTION_ATTEMPTS);
        }
    }

    /**
     * Resets reconnection state after successful connection.
     * Called when WebSocket successfully connects.
     */
    private void resetReconnectionState() {
        if (reconnectionAttempts > 0) {
            logger.info("WebSocket connection successful, resetting reconnection state");
        }
        reconnectionAttempts = 0;
        currentBackoffSeconds = INITIAL_BACKOFF_SECONDS;
    }

    /**
     * Gets the current backoff delay in seconds.
     *
     * @return Current backoff delay for testing purposes
     */
    public int getCurrentBackoffSeconds() {
        return currentBackoffSeconds;
    }

    /**
     * Gets the current reconnection attempt count.
     *
     * @return Current attempt count for testing purposes
     */
    public int getReconnectionAttempts() {
        return reconnectionAttempts;
    }

    /**
     * Builds the WebSocket URI from the API client configuration.
     *
     * Format: ws://host:port/socket?api_key=<token>
     * or wss://host:port/socket?api_key=<token> for HTTPS
     *
     * @return The WebSocket URI
     */
    private URI buildWebSocketUri() {
        String baseUri = apiClient.getBaseUri().toString();

        // Convert HTTP(S) scheme to WS(S)
        String webSocketScheme;
        if (baseUri.startsWith("https://")) {
            webSocketScheme = "wss://";
            baseUri = baseUri.substring("https://".length());
        } else if (baseUri.startsWith("http://")) {
            webSocketScheme = "ws://";
            baseUri = baseUri.substring("http://".length());
        } else {
            throw new IllegalArgumentException("Invalid base URI scheme: " + baseUri);
        }

        // Validate authentication token
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalStateException("API token is required for WebSocket authentication");
        }

        // Build WebSocket URI with query parameter authentication
        String webSocketUrl = webSocketScheme + baseUri + "/socket?api_key=" + apiToken;
        return URI.create(webSocketUrl);
    }

    /**
     * Masks the API key in the URI for logging purposes.
     *
     * @param uri The URI containing the API key
     * @return The URI with the API key masked
     */
    private String maskApiKey(String uri) {
        return uri.replaceAll("api_key=[^&]+", "api_key=***");
    }

    /**
     * Gets the current connection state.
     *
     * @return The current connection state
     */
    public ConnectionState getConnectionState() {
        return connectionState.get();
    }

    /**
     * Disposes the WebSocket connection and cleans up resources.
     * Called when the task is being shut down.
     */
    public void dispose() {
        logger.info("WebSocket disposing, closing connection...");

        // Cancel pending connection attempt
        Future<Session> future = this.connectFuture;
        if (future != null && !future.isDone()) {
            future.cancel(true);
            this.connectFuture = null;
        }

        // Close WebSocket session
        Session session = this.webSocketSession;
        if (session != null && session.isOpen()) {
            session.close();
            this.webSocketSession = null;
        }

        // Stop WebSocket client
        WebSocketClient client = this.webSocketClient;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("Error stopping WebSocket client: {}", e.getMessage());
            }
            this.webSocketClient = null;
        }

        connectionState.set(ConnectionState.DISCONNECTED);
        logger.info("WebSocket disposed successfully");
    }

    // ========== WebSocketListener Implementation ==========

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        if (session == null) {
            logger.warn("WebSocket connected with null session");
            return;
        }

        this.webSocketSession = session;
        connectionState.set(ConnectionState.CONNECTED);
        logger.info("WebSocket connected successfully to {}", session.getRemoteAddress());

        // Reset reconnection state on successful connection
        resetReconnectionState();

        // Configure session timeouts
        session.setIdleTimeout(TimeUnit.MINUTES.toMillis(5));
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        if (message == null) {
            logger.debug("WebSocket received null message, ignoring");
            return;
        }

        // Delegate message parsing to handler (Task 3 implementation)
        try {
            messageHandler.handleMessage(message);
        } catch (Exception e) {
            logger.error("Error handling WebSocket message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        // Jellyfin uses text-based JSON messages, binary messages are unexpected
        logger.debug("WebSocket received unexpected binary message, length: {}", len);
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        connectionState.set(ConnectionState.DISCONNECTED);
        logger.info("WebSocket connection closed: {} - {}", statusCode, reason);

        // Clean up session reference
        this.webSocketSession = null;

        // Trigger reconnection logic if not a clean shutdown
        if (statusCode != 1000) { // 1000 = normal closure
            handleConnectionFailure();
        }
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        connectionState.set(ConnectionState.FAILED);
        if (cause != null) {
            logger.error("WebSocket error: {}", cause.getMessage(), cause);
        } else {
            logger.error("WebSocket error: Unknown cause");
        }

        // Trigger reconnection logic
        handleConnectionFailure();
    }
}
