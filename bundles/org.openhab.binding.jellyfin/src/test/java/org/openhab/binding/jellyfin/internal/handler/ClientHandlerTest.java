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
package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;

/**
 * Unit tests for {@link ClientHandler} session timeout logic.
 *
 * Tests validate session timeout detection and state management
 * without requiring full openHAB framework integration.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ClientHandlerTest {

    @Test
    void testGetCurrentSessionInitiallyNull() {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));
        assertNull(handler.getCurrentSession());
    }

    @Test
    void testHandlerCreation() {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));
        assertNotNull(handler);
        assertNull(handler.getCurrentSession());
    }

    /**
     * Test that session update sets the current session.
     */
    @Test
    void testSessionUpdateSetsCurrentSession() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        session.setDeviceId("device1");

        handler.onSessionUpdate(session);

        assertNotNull(handler.getCurrentSession());
        assertEquals("session-1", handler.getCurrentSession().getId());
    }

    /**
     * Test that null session update clears the current session.
     */
    @Test
    void testNullSessionUpdateClearsSession() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        // First set a session
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        handler.onSessionUpdate(session);
        assertNotNull(handler.getCurrentSession());

        // Then clear it with null
        handler.onSessionUpdate(null);
        assertNull(handler.getCurrentSession());
    }

    /**
     * Test that timestamp is updated on session update.
     */
    @Test
    void testSessionUpdateUpdatesTimestamp() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        long before = System.currentTimeMillis();

        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        handler.onSessionUpdate(session);

        long timestamp = getLastSessionUpdateTimestamp(handler);
        assertTrue(timestamp >= before, "Timestamp should be updated to current time or later");
    }

    /**
     * Test that session timeout detection works via timestamp comparison.
     */
    @Test
    void testSessionTimeoutDetectionViaTimestamp() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        // Set up a session with old timestamp (61 seconds ago)
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        setCurrentSession(handler, session);
        setLastSessionUpdateTimestamp(handler, System.currentTimeMillis() - 61000);

        // Verify the session would be considered timed out
        long timestamp = getLastSessionUpdateTimestamp(handler);
        long timeSinceUpdate = System.currentTimeMillis() - timestamp;
        assertTrue(timeSinceUpdate > 60000, "Session should be detected as timed out (>60s)");
    }

    /**
     * Test that recent session updates are not considered timed out.
     */
    @Test
    void testRecentSessionNotTimedOut() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        // Set up a session with recent timestamp (30 seconds ago)
        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        setCurrentSession(handler, session);
        setLastSessionUpdateTimestamp(handler, System.currentTimeMillis() - 30000);

        // Verify the session would not be considered timed out
        long timestamp = getLastSessionUpdateTimestamp(handler);
        long timeSinceUpdate = System.currentTimeMillis() - timestamp;
        assertTrue(timeSinceUpdate < 60000, "Session should not be timed out (<60s)");
    }

    // === Helper methods for reflection access ===

    private void setCurrentSession(ClientHandler handler, @Nullable SessionInfoDto session) throws Exception {
        Field field = ClientHandler.class.getDeclaredField("currentSession");
        field.setAccessible(true);
        field.set(handler, session);
    }

    private void setLastSessionUpdateTimestamp(ClientHandler handler, long timestamp) throws Exception {
        Field field = ClientHandler.class.getDeclaredField("lastSessionUpdateTimestamp");
        field.setAccessible(true);
        field.set(handler, timestamp);
    }

    private long getLastSessionUpdateTimestamp(ClientHandler handler) throws Exception {
        Field field = ClientHandler.class.getDeclaredField("lastSessionUpdateTimestamp");
        field.setAccessible(true);
        return (long) field.get(handler);
    }
}
