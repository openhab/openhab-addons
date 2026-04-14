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
package org.openhab.binding.jellyfin.internal.util.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;

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

    private SessionInfoDto createSessionWithPlayback(String sessionId, String deviceId) {
        SessionInfoDto session = createSession(sessionId, deviceId);
        BaseItemDto playingItem = new BaseItemDto();
        playingItem.setName("Test Song");
        session.setNowPlayingItem(playingItem);
        return session;
    }

    // --- Regression tests: prefix-related device ID merging (issue #17674) ---

    /**
     * Regression: Jellyfin Android creates two sessions for one device — a short background-service
     * device ID (prefix of the full device ID) and the full device ID for the main UI session.
     * When the short-ID session carries NowPlayingItem but the long-ID session does not,
     * the ClientHandler (subscribed to the long ID) must receive the playing session.
     */
    @Test
    void updateSessions_prefixDeviceId_playingSessionPromotedToCanonicalId() {
        String shortId = "93dbb268d5ccc56d";
        String longId = "93dbb268d5ccc56dd700fbdb6af146b3b3c70644e708ad24";

        eventBus.subscribe(longId, session -> receivedUpdates.add(new SessionUpdateRecord(longId, session)));

        Map<String, SessionInfoDto> sessions = new HashMap<>();
        sessions.put("session-short", createSessionWithPlayback("session-short", shortId));
        sessions.put("session-long", createSession("session-long", longId)); // no NowPlayingItem
        sessionManager.updateSessions(sessions);

        // The long-ID listener must have been invoked exactly once with NowPlayingItem present
        assertEquals(1, receivedUpdates.size());
        assertEquals(longId, receivedUpdates.get(0).deviceId());
        assertNotNull(receivedUpdates.get(0).session());
        assertNotNull(receivedUpdates.get(0).session().getNowPlayingItem(),
                "NowPlayingItem must be promoted from the prefix session to the canonical ID");
    }

    @Test
    void updateSessions_prefixDeviceId_shortIdNotDispatchedSeparately() {
        String shortId = "93dbb268d5ccc56d";
        String longId = "93dbb268d5ccc56dd700fbdb6af146b3b3c70644e708ad24";

        List<String> shortIdReceived = new ArrayList<>();
        eventBus.subscribe(shortId, session -> shortIdReceived.add(shortId));
        eventBus.subscribe(longId, session -> receivedUpdates.add(new SessionUpdateRecord(longId, session)));

        Map<String, SessionInfoDto> sessions = new HashMap<>();
        sessions.put("session-short", createSessionWithPlayback("session-short", shortId));
        sessions.put("session-long", createSession("session-long", longId));
        sessionManager.updateSessions(sessions);

        // Short ID must not be dispatched separately when a canonical (longer) ID exists
        assertTrue(shortIdReceived.isEmpty(),
                "Short prefix ID must not be dispatched separately when a canonical long ID is present");
    }

    @Test
    void updateSessions_prefixDeviceId_longIdAlreadyPlayingNotOverwritten() {
        String shortId = "93dbb268d5ccc56d";
        String longId = "93dbb268d5ccc56dd700fbdb6af146b3b3c70644e708ad24";

        eventBus.subscribe(longId, session -> receivedUpdates.add(new SessionUpdateRecord(longId, session)));

        Map<String, SessionInfoDto> sessions = new HashMap<>();
        // Both have NowPlayingItem — long-ID session data must not be replaced
        sessions.put("session-short", createSessionWithPlayback("session-short", shortId));
        sessions.put("session-long", createSessionWithPlayback("session-long", longId));
        sessionManager.updateSessions(sessions);

        assertEquals(1, receivedUpdates.size());
        assertEquals(longId, receivedUpdates.get(0).deviceId());
        // The received session must be the original long-ID session (session-long), not the short one
        assertEquals("session-long", receivedUpdates.get(0).session().getId());
    }

    @Test
    void updateSessions_unrelatedDeviceIds_dispatchedIndependently() {
        String idA = "device-a-123";
        String idB = "device-b-456";

        eventBus.subscribe(idA, session -> receivedUpdates.add(new SessionUpdateRecord(idA, session)));
        eventBus.subscribe(idB, session -> receivedUpdates.add(new SessionUpdateRecord(idB, session)));

        Map<String, SessionInfoDto> sessions = new HashMap<>();
        sessions.put("session-a", createSessionWithPlayback("session-a", idA));
        sessions.put("session-b", createSession("session-b", idB));
        sessionManager.updateSessions(sessions);

        assertEquals(2, receivedUpdates.size());
        assertTrue(receivedUpdates.stream().anyMatch(r -> idA.equals(r.deviceId())));
        assertTrue(receivedUpdates.stream().anyMatch(r -> idB.equals(r.deviceId())));
    }

    // --- Regression tests: stale knownCanonicalDeviceIds after serialNumber migration (issue #17674) ---

    /**
     * Regression: after ClientDiscoveryService migrates serialNumber from fullId to shortId,
     * ServerHandler.updateClientList() refreshes knownCanonicalDeviceIds to [shortId] before calling
     * updateSessions(). This test verifies that a short-ID session then reaches the short-ID subscriber
     * (i.e. it is no longer silently re-routed to the stale full ID).
     */
    @Test
    void updateSessions_shortIdDispatchedToSubscriber_whenKnownIdsReflectsMigratedConfig() {
        String shortId = "93dbb268d5ccc56d";

        // Post-migration: ClientHandler is subscribed on the short ID
        eventBus.subscribe(shortId, session -> receivedUpdates.add(new SessionUpdateRecord(shortId, session)));

        // Post-migration: knownCanonicalDeviceIds refreshed to [shortId] (current config value)
        sessionManager.updateKnownDeviceIds(List.of(shortId));

        // Server reports a session under the short ID only
        Map<String, SessionInfoDto> sessions = new HashMap<>();
        sessions.put("session-1", createSession("session-1", shortId));
        sessionManager.updateSessions(sessions);

        // Short-ID subscriber must receive the session update
        assertEquals(1, receivedUpdates.size(), "Short-ID subscriber must receive the session update");
        assertEquals(shortId, receivedUpdates.get(0).deviceId());
        assertNotNull(receivedUpdates.get(0).session());
    }

    /**
     * Regression guard: with stale knownCanonicalDeviceIds={fullId}, a session arriving under shortId
     * gets re-routed to fullId — the shortId subscriber receives nothing. This documents the pre-fix
     * behaviour. After the fix, ServerHandler.updateClientList() always refreshes knownCanonicalDeviceIds
     * before calling updateSessions(), so this stale state can no longer persist across poll cycles.
     */
    @Test
    void updateSessions_staleKnownDeviceIds_shortIdSubscriberReceivesNoUpdate() {
        String shortId = "93dbb268d5ccc56d";
        String fullId = "93dbb268d5ccc56dd700fbdb6af146b3b3c70644e708ad24";

        // Post-migration: ClientHandler is subscribed on the short ID
        eventBus.subscribe(shortId, session -> receivedUpdates.add(new SessionUpdateRecord(shortId, session)));

        // STALE: knownCanonicalDeviceIds still holds the pre-migration full ID
        sessionManager.updateKnownDeviceIds(List.of(fullId));

        // Server reports a session under the short ID only
        Map<String, SessionInfoDto> sessions = new HashMap<>();
        sessions.put("session-1", createSession("session-1", shortId));
        sessionManager.updateSessions(sessions);

        // With stale full-ID routing, the short-ID session is dispatched to the (nonexistent)
        // full-ID subscriber — the short-ID subscriber receives nothing
        assertTrue(receivedUpdates.isEmpty(),
                "With stale knownCanonicalDeviceIds, short-ID subscriber must not receive the update "
                        + "— it is dispatched to the full-ID subscriber instead");
    }

    /**
     * Record class to capture session update events for verification.
     */
    private record SessionUpdateRecord(String deviceId, SessionInfoDto session) {
    }
}
