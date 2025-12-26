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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
     * Test that session update immediately makes handler consider session active.
     * This verifies the observable behavior (session availability) rather than
     * internal timestamp state. The actual timeout detection (60s) is validated
     * by the scheduled checkSessionTimeout() monitor task in integration tests.
     */
    @Test
    void testSessionUpdateMakesSessionAvailable() {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");

        // Verify no session initially
        assertNull(handler.getCurrentSession());

        // Update session
        handler.onSessionUpdate(session);

        // Verify session is now available (not timed out)
        assertNotNull(handler.getCurrentSession());
        assertEquals("session-1", handler.getCurrentSession().getId());
    }
}
