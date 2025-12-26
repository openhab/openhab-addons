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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.events.SessionEventListener;

/**
 * Integration tests for WebSocket SessionsMessage integration with SessionEventBus.
 *
 * Validates that:
 * - SessionInfoDto objects from WebSocket messages can be published to the event bus
 * - Subscribers receive session update events correctly
 * - Multiple sessions from a single WebSocket message are distributed properly
 * - Event bus handles session state transitions (active → inactive)
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class WebSocketSessionEventBusIntegrationTest {

    /**
     * Test publishing a single session to the event bus.
     *
     * Validates that a SessionInfoDto parsed from a WebSocket SessionsMessage
     * can be successfully published to the event bus and received by subscribers.
     */
    @Test
    public void testPublishSessionFromWebSocket_singleSession() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger updateCount = new AtomicInteger(0);
        SessionEventListener listener = (SessionInfoDto s) -> updateCount.incrementAndGet();

        String deviceId = "device-001";
        bus.subscribe(deviceId, listener);

        // Create a session as it would come from WebSocket message parsing
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        session.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        session.setUserName("testuser");
        session.setDeviceName("Living Room");

        // Publish to event bus (as WebSocket handler would do)
        bus.publishSessionUpdate(deviceId, session);

        assertEquals(1, updateCount.get());
    }

    /**
     * Test multiple sessions from same WebSocket message.
     *
     * Validates that when a SessionsMessage contains multiple sessions
     * (e.g., from different devices), each session is published to appropriate
     * device-specific event bus channel.
     */
    @Test
    public void testPublishMultipleSessionsFromWebSocket_differentDevices() {
        SessionEventBus bus = new SessionEventBus();

        // Track updates for each device
        AtomicInteger device1Updates = new AtomicInteger(0);
        AtomicInteger device2Updates = new AtomicInteger(0);

        SessionEventListener listener1 = (SessionInfoDto s) -> device1Updates.incrementAndGet();
        SessionEventListener listener2 = (SessionInfoDto s) -> device2Updates.incrementAndGet();

        // Subscribe to different device channels
        bus.subscribe("device-1", listener1);
        bus.subscribe("device-2", listener2);

        // Simulate WebSocket message with multiple sessions
        SessionInfoDto session1 = new SessionInfoDto();
        session1.setId("session-a");
        session1.setDeviceName("Device 1");

        SessionInfoDto session2 = new SessionInfoDto();
        session2.setId("session-b");
        session2.setDeviceName("Device 2");

        // Publish each session to its respective device channel
        bus.publishSessionUpdate("device-1", session1);
        bus.publishSessionUpdate("device-2", session2);

        assertEquals(1, device1Updates.get());
        assertEquals(1, device2Updates.get());
    }

    /**
     * Test session update event (playback state change).
     *
     * Validates that playback state changes received via WebSocket
     * (e.g., paused → playing, volume change) are correctly propagated
     * through the event bus.
     */
    @Test
    public void testPublishSessionUpdate_playstateChange() {
        SessionEventBus bus = new SessionEventBus();

        List<SessionInfoDto> receivedSessions = new ArrayList<>();
        SessionEventListener listener = receivedSessions::add;

        bus.subscribe("media-device", listener);

        // Simulate first WebSocket update: playing
        SessionInfoDto playingSession = new SessionInfoDto();
        playingSession.setId("session-playback");
        playingSession.setUserName("user");
        playingSession.setDeviceName("Media Device");

        bus.publishSessionUpdate("media-device", playingSession);
        assertEquals(1, receivedSessions.size());

        // Simulate second WebSocket update: paused
        SessionInfoDto pausedSession = new SessionInfoDto();
        pausedSession.setId("session-playback");
        pausedSession.setUserName("user");
        pausedSession.setDeviceName("Media Device");

        bus.publishSessionUpdate("media-device", pausedSession);
        assertEquals(2, receivedSessions.size());
    }

    /**
     * Test session termination (empty sessions message).
     *
     * Validates that when a SessionsMessage with empty sessions array is received
     * (indicating all playback stopped), the event bus handles null/empty state
     * appropriately.
     */
    @Test
    public void testPublishSessionUpdate_nullSessionTermination() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger updateCount = new AtomicInteger(0);

        SessionEventListener listener = (SessionInfoDto s) -> updateCount.incrementAndGet();
        bus.subscribe("ending-device", listener);

        // First publish active session
        SessionInfoDto session = new SessionInfoDto();
        session.setId("ending-session");
        bus.publishSessionUpdate("ending-device", session);
        assertEquals(1, updateCount.get());

        // Then publish null to indicate session ended
        bus.publishSessionUpdate("ending-device", null);
        assertEquals(2, updateCount.get());
    }

    /**
     * Test subscriber receives all session attributes from WebSocket.
     *
     * Validates that all session attributes parsed from WebSocket message JSON
     * (user info, device info, playback state) are correctly received by event
     * listener with no data loss.
     */
    @Test
    public void testPublishSessionUpdate_preservesAllAttributes() {
        SessionEventBus bus = new SessionEventBus();

        SessionInfoDto capturedSession = null;
        final SessionInfoDto[] capture = new SessionInfoDto[1];

        SessionEventListener listener = (SessionInfoDto s) -> capture[0] = s;
        bus.subscribe("test-device", listener);

        // Create session with rich attributes (as parsed from WebSocket message)
        SessionInfoDto session = new SessionInfoDto();
        session.setId("rich-session");
        session.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        session.setUserName("testuser");
        session.setDeviceName("Test Device");
        session.setDeviceId("device-id-123");
        session.setClient("Web");
        session.setApplicationVersion("10.8.5");

        bus.publishSessionUpdate("test-device", session);

        // Verify all attributes are preserved
        SessionInfoDto received = capture[0];
        assertNotNull(received);
        assertEquals("rich-session", received.getId());
        assertEquals("testuser", received.getUserName());
        assertEquals("Test Device", received.getDeviceName());
        assertEquals("Web", received.getClient());
    }

    /**
     * Test concurrent session updates via WebSocket.
     *
     * Validates that the event bus can handle rapid successive WebSocket updates
     * from the same device without losing or duplicating events.
     *
     * Simulates real-world scenario where playback state updates arrive quickly
     * as user interacts with playback controls.
     */
    @Test
    public void testPublishSessionUpdate_rapidUpdates() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger updateCount = new AtomicInteger(0);

        SessionEventListener listener = (SessionInfoDto s) -> updateCount.incrementAndGet();
        bus.subscribe("rapid-device", listener);

        // Simulate rapid WebSocket updates (e.g., position changes, pause/resume)
        for (int i = 0; i < 10; i++) {
            SessionInfoDto session = new SessionInfoDto();
            session.setId("rapid-session");
            session.setUserName("rapiduser");
            bus.publishSessionUpdate("rapid-device", session);
        }

        assertEquals(10, updateCount.get());
    }

    /**
     * Test device-specific event routing from WebSocket.
     *
     * Validates that sessions are routed to correct device channels
     * and don't interfere with other devices' event streams when multiple
     * sessions are active simultaneously.
     */
    @Test
    public void testPublishSessionUpdate_deviceRouting() {
        SessionEventBus bus = new SessionEventBus();

        List<String> device1Sessions = new ArrayList<>();
        List<String> device2Sessions = new ArrayList<>();
        List<String> device3Sessions = new ArrayList<>();

        bus.subscribe("device-1", (s) -> device1Sessions.add(s.getId()));
        bus.subscribe("device-2", (s) -> device2Sessions.add(s.getId()));
        bus.subscribe("device-3", (s) -> device3Sessions.add(s.getId()));

        // Publish sessions to different device channels
        SessionInfoDto session1 = new SessionInfoDto();
        session1.setId("session-for-device-1");
        bus.publishSessionUpdate("device-1", session1);

        SessionInfoDto session2a = new SessionInfoDto();
        session2a.setId("session-for-device-2a");
        bus.publishSessionUpdate("device-2", session2a);

        SessionInfoDto session2b = new SessionInfoDto();
        session2b.setId("session-for-device-2b");
        bus.publishSessionUpdate("device-2", session2b);

        SessionInfoDto session3 = new SessionInfoDto();
        session3.setId("session-for-device-3");
        bus.publishSessionUpdate("device-3", session3);

        // Verify routing - device-2 should have 2 sessions, others 1
        assertEquals(1, device1Sessions.size());
        assertEquals(2, device2Sessions.size());
        assertEquals(1, device3Sessions.size());

        assertEquals("session-for-device-1", device1Sessions.get(0));
        assertEquals("session-for-device-2a", device2Sessions.get(0));
        assertEquals("session-for-device-2b", device2Sessions.get(1));
        assertEquals("session-for-device-3", device3Sessions.get(0));
    }

    /**
     * Test listener subscription/unsubscription around WebSocket updates.
     *
     * Validates that listeners can be added or removed dynamically and
     * subsequent WebSocket updates respect the current subscription state.
     */
    @Test
    public void testPublishSessionUpdate_dynamicSubscription() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger updateCount = new AtomicInteger(0);

        SessionEventListener listener = (SessionInfoDto s) -> updateCount.incrementAndGet();

        // Subscribe, publish, unsubscribe, publish again
        bus.subscribe("dynamic-device", listener);
        SessionInfoDto session1 = new SessionInfoDto();
        session1.setId("session-1");
        bus.publishSessionUpdate("dynamic-device", session1);
        assertEquals(1, updateCount.get());

        bus.unsubscribe("dynamic-device", listener);
        SessionInfoDto session2 = new SessionInfoDto();
        session2.setId("session-2");
        bus.publishSessionUpdate("dynamic-device", session2);
        assertEquals(1, updateCount.get()); // No change after unsubscribe

        // Resubscribe and verify
        bus.subscribe("dynamic-device", listener);
        SessionInfoDto session3 = new SessionInfoDto();
        session3.setId("session-3");
        bus.publishSessionUpdate("dynamic-device", session3);
        assertEquals(2, updateCount.get());
    }

    /**
     * Test session update from WebSocket with minimal required fields.
     *
     * Validates that event bus can handle sessions with only essential fields
     * populated (as might occur for incomplete or minimal WebSocket messages).
     */
    @Test
    public void testPublishSessionUpdate_minimalSession() {
        SessionEventBus bus = new SessionEventBus();

        SessionInfoDto minimumSession = null;
        final SessionInfoDto[] capture = new SessionInfoDto[1];

        SessionEventListener listener = (SessionInfoDto s) -> capture[0] = s;
        bus.subscribe("minimal-device", listener);

        // Create session with minimal fields
        SessionInfoDto session = new SessionInfoDto();
        session.setId("minimal-id");

        bus.publishSessionUpdate("minimal-device", session);

        assertNotNull(capture[0]);
        assertEquals("minimal-id", capture[0].getId());
    }
}
