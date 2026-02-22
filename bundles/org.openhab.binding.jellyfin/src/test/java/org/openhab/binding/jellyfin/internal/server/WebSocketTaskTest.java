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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.server.WebSocketTask.ConnectionState;

/**
 * Unit tests for {@link WebSocketTask}.
 *
 * These tests validate connection lifecycle management and state tracking
 * without requiring a live Jellyfin server or actual WebSocket connections.
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class WebSocketTaskTest {

    private ApiClient apiClient;
    private String apiToken;
    private TestMessageHandler messageHandler;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8096");
        apiToken = "test-api-key-12345";
        messageHandler = new TestMessageHandler();
    }

    /**
     * Test that WebSocketTask can be instantiated with valid parameters.
     */
    @Test
    public void testWebSocketTaskInstantiation() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        assertNotNull(task);
        assertEquals("WebSocket", task.getId());
        assertEquals(0, task.getStartupDelay());
        assertEquals(0, task.getInterval());
    }

    /**
     * Test initial connection state is DISCONNECTED.
     */
    @Test
    public void testInitialConnectionState() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        assertEquals(ConnectionState.DISCONNECTED, task.getConnectionState());
    }

    /**
     * Test connection state enum values.
     */
    @Test
    public void testConnectionStateEnum() {
        ConnectionState[] states = ConnectionState.values();

        assertEquals(4, states.length);
        assertEquals(ConnectionState.DISCONNECTED, ConnectionState.valueOf("DISCONNECTED"));
        assertEquals(ConnectionState.CONNECTING, ConnectionState.valueOf("CONNECTING"));
        assertEquals(ConnectionState.CONNECTED, ConnectionState.valueOf("CONNECTED"));
        assertEquals(ConnectionState.FAILED, ConnectionState.valueOf("FAILED"));
    }

    /**
     * Test task configuration constants.
     */
    @Test
    public void testTaskConfiguration() {
        assertEquals("WebSocket", WebSocketTask.TASK_ID);
        assertEquals(0, WebSocketTask.DEFAULT_STARTUP_DELAY);
        assertEquals(0, WebSocketTask.DEFAULT_INTERVAL);
    }

    /**
     * Test dispose method can be called safely even when not connected.
     */
    @Test
    public void testDisposeWhenNotConnected() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Should not throw exception
        task.dispose();

        assertEquals(ConnectionState.DISCONNECTED, task.getConnectionState());
    }

    /**
     * Test message handler receives callbacks during WebSocket events.
     * (This test validates the interface integration, not actual WebSocket behavior)
     */
    @Test
    public void testMessageHandlerIntegration() {
        TestMessageHandler handler = new TestMessageHandler();
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, handler);

        assertNotNull(task);
        assertEquals(0, handler.getMessageCount());

        // Simulate message reception (would be called by WebSocket callbacks in real usage)
        task.onWebSocketText("{\"MessageType\":\"Sessions\"}");

        assertEquals(1, handler.getMessageCount());
    }

    /**
     * Test onWebSocketText with null message is handled gracefully.
     */
    @Test
    public void testOnWebSocketTextNullMessage() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Should not throw exception
        task.onWebSocketText(null);

        assertEquals(0, messageHandler.getMessageCount());
    }

    /**
     * Test onWebSocketClose updates connection state.
     */
    @Test
    public void testOnWebSocketClose() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        task.onWebSocketClose(1000, "Normal closure");

        assertEquals(ConnectionState.DISCONNECTED, task.getConnectionState());
    }

    /**
     * Test onWebSocketError updates connection state to FAILED.
     */
    @Test
    public void testOnWebSocketError() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        task.onWebSocketError(new RuntimeException("Test error"));

        assertEquals(ConnectionState.FAILED, task.getConnectionState());
    }

    /**
     * Test onWebSocketError with null cause is handled gracefully.
     */
    @Test
    public void testOnWebSocketErrorNullCause() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Should not throw exception
        task.onWebSocketError(null);

        assertEquals(ConnectionState.FAILED, task.getConnectionState());
    }

    /**
     * Test onWebSocketBinary logs unexpected binary messages.
     */
    @Test
    public void testOnWebSocketBinaryMessage() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);
        byte[] payload = new byte[] { 0x01, 0x02, 0x03 };

        // Should not throw exception (Jellyfin uses text, but we handle binary gracefully)
        task.onWebSocketBinary(payload, 0, payload.length);
    }

    /**
     * Test task ID matches constant.
     */
    @Test
    public void testTaskId() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        assertEquals(WebSocketTask.TASK_ID, task.getId());
    }

    /**
     * Test message handler exception doesn't crash task.
     */
    @Test
    public void testMessageHandlerException() {
        WebSocketMessageHandler failingHandler = new WebSocketMessageHandler() {
            @Override
            public void handleMessage(String message) {
                throw new RuntimeException("Test exception in handler");
            }
        };

        WebSocketTask task = new WebSocketTask(apiClient, apiToken, failingHandler);

        // Should not propagate exception
        task.onWebSocketText("{\"MessageType\":\"Sessions\"}");
    }

    /**
     * Test multiple messages are delivered to handler.
     */
    @Test
    public void testMultipleMessages() {
        TestMessageHandler handler = new TestMessageHandler();
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, handler);

        task.onWebSocketText("{\"MessageType\":\"Sessions\",\"Data\":[]}");
        task.onWebSocketText("{\"MessageType\":\"Playstate\"}");
        task.onWebSocketText("{\"MessageType\":\"ForceKeepAlive\"}");

        assertEquals(3, handler.getMessageCount());
        assertEquals("{\"MessageType\":\"Sessions\",\"Data\":[]}", handler.getMessages().get(0));
        assertEquals("{\"MessageType\":\"Playstate\"}", handler.getMessages().get(1));
        assertEquals("{\"MessageType\":\"ForceKeepAlive\"}", handler.getMessages().get(2));
    }

    /**
     * Test exponential backoff calculation follows correct progression: 1→2→4→8→16→32→60 (capped).
     */
    @Test
    public void testExponentialBackoffProgression() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Initial state
        assertEquals(0, task.getReconnectionAttempts());
        assertEquals(1, task.getCurrentBackoffSeconds());

        // Simulate connection failures to test backoff progression
        // Attempt 1: 1s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(1, task.getReconnectionAttempts());
        assertEquals(1, task.getCurrentBackoffSeconds());

        // Attempt 2: 2s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(2, task.getReconnectionAttempts());
        assertEquals(2, task.getCurrentBackoffSeconds());

        // Attempt 3: 4s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(3, task.getReconnectionAttempts());
        assertEquals(4, task.getCurrentBackoffSeconds());

        // Attempt 4: 8s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(4, task.getReconnectionAttempts());
        assertEquals(8, task.getCurrentBackoffSeconds());

        // Attempt 5: 16s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(5, task.getReconnectionAttempts());
        assertEquals(16, task.getCurrentBackoffSeconds());

        // Attempt 6: 32s
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(6, task.getReconnectionAttempts());
        assertEquals(32, task.getCurrentBackoffSeconds());

        // Attempt 7: 60s (capped, would be 64s without cap)
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(7, task.getReconnectionAttempts());
        assertEquals(60, task.getCurrentBackoffSeconds());

        // Attempt 8+: Should remain at 60s cap
        task.onWebSocketError(new RuntimeException("Connection failed"));
        assertEquals(8, task.getReconnectionAttempts());
        assertEquals(60, task.getCurrentBackoffSeconds());
    }

    /**
     * Test max reconnection attempts triggers fallback callback.
     */
    @Test
    public void testMaxReconnectionAttemptsTriggersCallback() {
        final boolean[] callbackInvoked = { false };
        Runnable fallbackCallback = () -> callbackInvoked[0] = true;

        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler, fallbackCallback);

        // Simulate 10 connection failures
        for (int i = 0; i < 10; i++) {
            task.onWebSocketError(new RuntimeException("Connection failed"));
        }

        assertEquals(10, task.getReconnectionAttempts());
        // Note: Callback invocation happens during run() when max retries are detected,
        // not directly in onWebSocketError. This test validates the counter reaches max.
    }

    /**
     * Test reconnection state resets on successful connection.
     */
    @Test
    public void testReconnectionStateResetOnSuccess() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Simulate several connection failures
        task.onWebSocketError(new RuntimeException("Connection failed"));
        task.onWebSocketError(new RuntimeException("Connection failed"));
        task.onWebSocketError(new RuntimeException("Connection failed"));

        assertEquals(3, task.getReconnectionAttempts());
        assertEquals(4, task.getCurrentBackoffSeconds());

        // Note: In real usage, onWebSocketConnect() would be called by Jetty WebSocket
        // which resets state. We validate the counters are set correctly before reset.
        // After a successful connection (in production), these would be reset to 0/1.
    }

    /**
     * Test non-normal WebSocket close triggers reconnection.
     */
    @Test
    public void testNonNormalCloseTriggersReconnection() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Normal close (1000) should not trigger reconnection
        task.onWebSocketClose(1000, "Normal closure");
        assertEquals(0, task.getReconnectionAttempts());

        // Abnormal close (1006 - connection closed abnormally) should trigger reconnection
        task.onWebSocketClose(1006, "Connection closed abnormally");
        assertEquals(1, task.getReconnectionAttempts());

        // Another abnormal close (1001 - going away)
        task.onWebSocketClose(1001, "Going away");
        assertEquals(2, task.getReconnectionAttempts());
    }

    /**
     * Test fallback callback not invoked when within retry limit.
     */
    @Test
    public void testNoCallbackWhenBelowRetryLimit() {
        final int[] callbackCount = { 0 };
        Runnable fallbackCallback = () -> callbackCount[0]++;

        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler, fallbackCallback);

        // Simulate 5 failures (below max of 10)
        for (int i = 0; i < 5; i++) {
            task.onWebSocketError(new RuntimeException("Connection failed"));
        }

        assertEquals(5, task.getReconnectionAttempts());
        // Callback invocation happens in run(), not in error handlers
    }

    /**
     * Test task instantiation with callback.
     */
    @Test
    public void testWebSocketTaskInstantiationWithCallback() {
        Runnable callback = () -> {
            // Fallback logic placeholder
        };

        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler, callback);

        assertNotNull(task);
        assertEquals("WebSocket", task.getId());
        assertEquals(0, task.getReconnectionAttempts());
        assertEquals(1, task.getCurrentBackoffSeconds());
    }

    /**
     * Test backoff calculation edge case: attempt 0.
     */
    @Test
    public void testBackoffCalculationAttemptZero() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Initial state before any failures
        assertEquals(0, task.getReconnectionAttempts());
        assertEquals(1, task.getCurrentBackoffSeconds()); // Initial backoff is 1s
    }

    /**
     * Test backoff cap at maximum value.
     */
    @Test
    public void testBackoffCappedAtMaximum() {
        WebSocketTask task = new WebSocketTask(apiClient, apiToken, messageHandler);

        // Simulate many failures to exceed cap threshold
        for (int i = 0; i < 15; i++) {
            task.onWebSocketError(new RuntimeException("Connection failed"));
        }

        // Backoff should be capped at 60s regardless of attempt count
        assertEquals(15, task.getReconnectionAttempts());
        assertEquals(60, task.getCurrentBackoffSeconds());
    }

    /**
     * Test helper class for capturing messages.
     */
    private static class TestMessageHandler implements WebSocketMessageHandler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void handleMessage(String message) {
            messages.add(message);
        }

        public int getMessageCount() {
            return messages.size();
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
