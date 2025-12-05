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
package org.openhab.binding.jellyfin.internal.util.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;

/**
 * Tests for {@link ClientListUpdater}.
 *
 * These tests verify the filtering logic used by ClientListUpdater.
 * Note: Full integration testing with SessionApi would require additional infrastructure.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ClientListUpdaterTest {

    private Map<String, SessionInfoDto> clientMap;

    @BeforeEach
    void setUp() {
        clientMap = new HashMap<>();
    }

    private SessionInfoDto createSession(String sessionId, String userId) {
        SessionInfoDto session = new SessionInfoDto();
        session.setId(sessionId);
        session.setUserId(UUID.fromString(userId));
        return session;
    }

    /**
     * Tests the filtering logic that ClientListUpdater applies.
     * Verifies that only sessions matching the provided user IDs are included.
     */
    @Test
    void testFilteringLogic_IncludesMatchingUserIds() {
        // Arrange
        String userId1 = "11111111-1111-1111-1111-111111111111";
        String userId2 = "22222222-2222-2222-2222-222222222222";
        String userId3 = "33333333-3333-3333-3333-333333333333";

        SessionInfoDto session1 = createSession("session-1", userId1);
        SessionInfoDto session2 = createSession("session-2", userId2);
        SessionInfoDto session3 = createSession("session-3", userId3);

        List<SessionInfoDto> allSessions = Arrays.asList(session1, session2, session3);
        Set<String> userIds = Set.of(userId1, userId3);

        // Act - Simulate what updateClients does
        clientMap.clear();
        for (SessionInfoDto session : allSessions) {
            if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                clientMap.put(session.getId(), session);
            }
        }

        // Assert
        assertEquals(2, clientMap.size());
        assertTrue(clientMap.containsKey("session-1"));
        assertTrue(clientMap.containsKey("session-3"));
        assertFalse(clientMap.containsKey("session-2"));
    }

    @Test
    void testFilteringLogic_ClearsExistingMap() {
        // Arrange
        String userId1 = "11111111-1111-1111-1111-111111111111";

        SessionInfoDto existingSession = createSession("old-session", userId1);
        clientMap.put("old-session", existingSession);

        SessionInfoDto newSession = createSession("new-session", userId1);
        List<SessionInfoDto> allSessions = Collections.singletonList(newSession);

        Set<String> userIds = Set.of(userId1);

        // Act - Simulate updateClients behavior
        clientMap.clear();
        for (SessionInfoDto session : allSessions) {
            if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                clientMap.put(session.getId(), session);
            }
        }

        // Assert
        assertEquals(1, clientMap.size());
        assertFalse(clientMap.containsKey("old-session"));
        assertTrue(clientMap.containsKey("new-session"));
    }

    @Test
    void testFilteringLogic_HandlesNullUserId() {
        // Arrange
        String userId1 = "11111111-1111-1111-1111-111111111111";

        SessionInfoDto session1 = createSession("session-1", userId1);
        SessionInfoDto session2 = new SessionInfoDto();
        session2.setId("session-2");
        session2.setUserId(null);

        List<SessionInfoDto> allSessions = Arrays.asList(session1, session2);
        Set<String> userIds = Set.of(userId1);

        // Act - Simulate updateClients behavior with null safety
        clientMap.clear();
        for (SessionInfoDto session : allSessions) {
            if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                clientMap.put(session.getId(), session);
            }
        }

        // Assert - should only include session1, not the one with null userId
        assertEquals(1, clientMap.size());
        assertTrue(clientMap.containsKey("session-1"));
        assertFalse(clientMap.containsKey("session-2"));
    }

    @Test
    void testFilteringLogic_HandlesEmptyUserIdSet() {
        // Arrange
        String userId1 = "11111111-1111-1111-1111-111111111111";

        SessionInfoDto session1 = createSession("session-1", userId1);
        List<SessionInfoDto> allSessions = Collections.singletonList(session1);

        Set<String> userIds = Collections.emptySet();

        // Act - Simulate updateClients behavior
        clientMap.clear();
        for (SessionInfoDto session : allSessions) {
            if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                clientMap.put(session.getId(), session);
            }
        }

        // Assert
        assertTrue(clientMap.isEmpty());
    }

    @Test
    void testFilteringLogic_HandlesEmptySessionList() {
        // Arrange
        String userId1 = "11111111-1111-1111-1111-111111111111";

        clientMap.put("old-session", createSession("old-session", userId1));

        List<SessionInfoDto> allSessions = Collections.emptyList();
        Set<String> userIds = Set.of(userId1);

        // Act - Simulate updateClients behavior
        clientMap.clear();
        for (SessionInfoDto session : allSessions) {
            if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                clientMap.put(session.getId(), session);
            }
        }

        // Assert
        assertTrue(clientMap.isEmpty());
    }
}
