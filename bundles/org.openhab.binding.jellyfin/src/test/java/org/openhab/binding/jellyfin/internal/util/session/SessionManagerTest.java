/**
 * Copyright (C) 2010-2025 openHAB.org and the original author(s)
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrik Gfeller - Initial contribution
 */
package org.openhab.binding.jellyfin.internal.util.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;

/**
 * Unit tests for {@link SessionManager}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class SessionManagerTest {
    private SessionEventBus eventBus;
    private SessionManager sessionManager;
    private List<SessionUpdateRecord> receivedUpdates;

    @BeforeEach
    void setUp() {
        eventBus = new SessionEventBus();
        sessionManager = new SessionManager(eventBus);
        receivedUpdates = new ArrayList<>();
    }

    @Test
    void updateSessions_publishesEventsForActiveSessions() {
        // Subscribe to receive updates
        eventBus.subscribe("device-1", session -> receivedUpdates.add(new SessionUpdateRecord("device-1", session)));
        eventBus.subscribe("device-2", session -> receivedUpdates.add(new SessionUpdateRecord("device-2", session)));

        // Create new sessions
        Map<String, SessionInfoDto> newSessions = new HashMap<>();
        SessionInfoDto session1 = createSession("session-1", "device-1");
        SessionInfoDto session2 = createSession("session-2", "device-2");
        newSessions.put("session-1", session1);
        newSessions.put("session-2", session2);

        // Update sessions
        sessionManager.updateSessions(newSessions);

        // Verify events were published
        assertEquals(2, receivedUpdates.size());
        assertTrue(receivedUpdates.stream().anyMatch(r -> "device-1".equals(r.deviceId()) && r.session() != null));
        assertTrue(receivedUpdates.stream().anyMatch(r -> "device-2".equals(r.deviceId()) && r.session() != null));
    }

    @Test
    void updateSessions_detectsOfflineDevices() {
        // Subscribe to receive updates
        eventBus.subscribe("device-1", session -> receivedUpdates.add(new SessionUpdateRecord("device-1", session)));
        eventBus.subscribe("device-2", session -> receivedUpdates.add(new SessionUpdateRecord("device-2", session)));

        // First update: both devices active
        Map<String, SessionInfoDto> firstUpdate = new HashMap<>();
        firstUpdate.put("session-1", createSession("session-1", "device-1"));
        firstUpdate.put("session-2", createSession("session-2", "device-2"));
        sessionManager.updateSessions(firstUpdate);
        receivedUpdates.clear();

        // Second update: only device-1 active, device-2 went offline
        Map<String, SessionInfoDto> secondUpdate = new HashMap<>();
        secondUpdate.put("session-1", createSession("session-1", "device-1"));
        sessionManager.updateSessions(secondUpdate);

        // Verify device-2 received null (offline notification)
        assertEquals(2, receivedUpdates.size());
        assertTrue(receivedUpdates.stream().anyMatch(r -> "device-1".equals(r.deviceId()) && r.session() != null));
        assertTrue(receivedUpdates.stream().anyMatch(r -> "device-2".equals(r.deviceId()) && r.session() == null));
    }

    @Test
    void getSessions_returnsDefensiveCopy() {
        // Add sessions
        Map<String, SessionInfoDto> newSessions = new HashMap<>();
        newSessions.put("session-1", createSession("session-1", "device-1"));
        sessionManager.updateSessions(newSessions);

        // Get sessions twice
        Map<String, SessionInfoDto> sessions1 = sessionManager.getSessions();
        Map<String, SessionInfoDto> sessions2 = sessionManager.getSessions();

        // Verify defensive copy (different instances)
        assertNotSame(sessions1, sessions2);

        // Verify external modification doesn't affect internal state
        sessions1.clear();
        assertEquals(1, sessionManager.getSessions().size());
    }

    @Test
    void updateSessions_ignoresNullDeviceId() {
        // Subscribe to receive updates
        eventBus.subscribe("device-1", session -> receivedUpdates.add(new SessionUpdateRecord("device-1", session)));

        // Create sessions with one having null device ID
        Map<String, SessionInfoDto> newSessions = new HashMap<>();
        SessionInfoDto session1 = createSession("session-1", "device-1");
        SessionInfoDto session2 = new SessionInfoDto(); // No device ID
        session2.setId("session-2");
        newSessions.put("session-1", session1);
        newSessions.put("session-2", session2);

        // Update sessions
        sessionManager.updateSessions(newSessions);

        // Verify only valid device received event
        assertEquals(1, receivedUpdates.size());
        assertEquals("device-1", receivedUpdates.get(0).deviceId());
    }

    @Test
    void updateSessions_ignoresBlankDeviceId() {
        // Subscribe to receive updates
        eventBus.subscribe("device-1", session -> receivedUpdates.add(new SessionUpdateRecord("device-1", session)));

        // Create sessions with one having blank device ID
        Map<String, SessionInfoDto> newSessions = new HashMap<>();
        SessionInfoDto session1 = createSession("session-1", "device-1");
        SessionInfoDto session2 = new SessionInfoDto();
        session2.setId("session-2");
        session2.setDeviceId("   "); // Blank device ID
        newSessions.put("session-1", session1);
        newSessions.put("session-2", session2);

        // Update sessions
        sessionManager.updateSessions(newSessions);

        // Verify only valid device received event
        assertEquals(1, receivedUpdates.size());
        assertEquals("device-1", receivedUpdates.get(0).deviceId());
    }

    @Test
    void clear_removesAllSessions() {
        // Add sessions
        Map<String, SessionInfoDto> newSessions = new HashMap<>();
        newSessions.put("session-1", createSession("session-1", "device-1"));
        newSessions.put("session-2", createSession("session-2", "device-2"));
        sessionManager.updateSessions(newSessions);

        // Verify sessions exist
        assertEquals(2, sessionManager.getSessions().size());

        // Clear
        sessionManager.clear();

        // Verify empty
        assertTrue(sessionManager.getSessions().isEmpty());
    }

    @Test
    void clear_resetsOfflineDetection() {
        // Subscribe to receive updates
        eventBus.subscribe("device-1", session -> receivedUpdates.add(new SessionUpdateRecord("device-1", session)));

        // First update: device-1 active
        Map<String, SessionInfoDto> firstUpdate = new HashMap<>();
        firstUpdate.put("session-1", createSession("session-1", "device-1"));
        sessionManager.updateSessions(firstUpdate);
        receivedUpdates.clear();

        // Clear state
        sessionManager.clear();

        // Second update: device-1 active again (should NOT send offline notification)
        Map<String, SessionInfoDto> secondUpdate = new HashMap<>();
        secondUpdate.put("session-1", createSession("session-1", "device-1"));
        sessionManager.updateSessions(secondUpdate);

        // Verify only one update (active), no offline notification
        assertEquals(1, receivedUpdates.size());
        assertEquals("device-1", receivedUpdates.get(0).deviceId());
        assertTrue(receivedUpdates.get(0).session() != null);
    }

    @Test
    void updateSessions_replacesAllSessions() {
        // First update
        Map<String, SessionInfoDto> firstUpdate = new HashMap<>();
        firstUpdate.put("session-1", createSession("session-1", "device-1"));
        firstUpdate.put("session-2", createSession("session-2", "device-2"));
        sessionManager.updateSessions(firstUpdate);
        assertEquals(2, sessionManager.getSessions().size());

        // Second update with completely different sessions
        Map<String, SessionInfoDto> secondUpdate = new HashMap<>();
        secondUpdate.put("session-3", createSession("session-3", "device-3"));
        sessionManager.updateSessions(secondUpdate);

        // Verify only new session exists
        Map<String, SessionInfoDto> currentSessions = sessionManager.getSessions();
        assertEquals(1, currentSessions.size());
        assertTrue(currentSessions.containsKey("session-3"));
    }

    // Helper methods

    private SessionInfoDto createSession(String sessionId, String deviceId) {
        SessionInfoDto session = new SessionInfoDto();
        session.setId(sessionId);
        session.setDeviceId(deviceId);
        return session;
    }

    /**
     * Record class to capture session update events for verification.
     */
    private record SessionUpdateRecord(String deviceId, SessionInfoDto session) {
    }
}
