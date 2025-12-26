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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayerStateInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.events.SessionEventBus;

/**
 * Integration tests for ClientHandler event bus integration.
 * Verifies that SessionEventListener is properly implemented.
 *
 * Note: Full framework integration tests (with Thing/Bridge setup) are complex
 * and require extensive mocking. These tests verify the core event listener logic.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ClientHandlerEventIntegrationTest {

    @Test
    void testClientHandlerImplementsSessionEventListener() {
        // Verify ClientHandler implements the SessionEventListener interface
        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));
        assertNotNull(handler, "ClientHandler should be created");

        // Verify handler is an instance of SessionEventListener
        org.openhab.binding.jellyfin.internal.events.SessionEventListener listener = handler;
        assertNotNull(listener, "ClientHandler should implement SessionEventListener");
    }

    @Test
    void testOnSessionUpdateWithNullSession() {
        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));

        // Call onSessionUpdate with null - should not throw exception
        handler.onSessionUpdate(null);

        // Verify current session is cleared
        assertNull(handler.getCurrentSession(), "Current session should be null after null update");
    }

    @Test
    void testOnSessionUpdateWithValidSession() {
        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));

        // Create test session
        SessionInfoDto session = createTestSession("test-device-123");

        // Call onSessionUpdate - should not throw exception
        handler.onSessionUpdate(session);

        // Verify current session is set
        assertNotNull(handler.getCurrentSession(), "Current session should be set after update");
        assertEquals("test-session-id", handler.getCurrentSession().getId(), "Session ID should match");
    }

    @Test
    void testEventBusSubscriptionAndPublication() {
        String deviceId = "test-device-456";
        SessionEventBus eventBus = new SessionEventBus();

        // Create handler and subscribe manually (simulating what initialize() does)
        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));
        eventBus.subscribe(deviceId, handler);

        // Verify subscription
        assertEquals(1, eventBus.getListenerCount(deviceId), "Event bus should have 1 listener");

        // Publish session update
        SessionInfoDto session = createTestSession(deviceId);
        eventBus.publishSessionUpdate(deviceId, session);

        // Verify session was received (current session should be updated)
        assertNotNull(handler.getCurrentSession(), "Handler should receive session update");
        assertEquals("test-session-id", handler.getCurrentSession().getId(), "Session ID should match");

        // Unsubscribe (simulating what dispose() does)
        eventBus.unsubscribe(deviceId, handler);
        assertEquals(0, eventBus.getListenerCount(deviceId), "Event bus should have no listeners after unsubscribe");
    }

    @Test
    void testMultipleSessionUpdates() {
        String deviceId = "test-device-789";
        SessionEventBus eventBus = new SessionEventBus();

        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));
        eventBus.subscribe(deviceId, handler);

        // First update
        SessionInfoDto session1 = createTestSession(deviceId);
        session1.getNowPlayingItem().setName("Movie 1");
        eventBus.publishSessionUpdate(deviceId, session1);
        assertEquals("Movie 1", handler.getCurrentSession().getNowPlayingItem().getName(),
                "First movie name should be set");

        // Second update
        SessionInfoDto session2 = createTestSession(deviceId);
        session2.getNowPlayingItem().setName("Movie 2");
        eventBus.publishSessionUpdate(deviceId, session2);
        assertEquals("Movie 2", handler.getCurrentSession().getNowPlayingItem().getName(),
                "Second movie name should be set");

        eventBus.unsubscribe(deviceId, handler);
    }

    @Test
    void testOfflineNotification() {
        String deviceId = "test-device-offline";
        SessionEventBus eventBus = new SessionEventBus();

        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));
        eventBus.subscribe(deviceId, handler);

        // First, send a session update
        SessionInfoDto session = createTestSession(deviceId);
        eventBus.publishSessionUpdate(deviceId, session);
        assertNotNull(handler.getCurrentSession(), "Session should be set");

        // Now send null session (offline)
        eventBus.publishSessionUpdate(deviceId, null);
        assertNull(handler.getCurrentSession(), "Session should be cleared on offline notification");

        eventBus.unsubscribe(deviceId, handler);
    }

    @Test
    void testExceptionHandlingInListener() {
        String deviceId = "test-device-exception";
        SessionEventBus eventBus = new SessionEventBus();

        ClientHandler handler = new ClientHandler(org.mockito.Mockito.mock(org.openhab.core.thing.Thing.class));
        eventBus.subscribe(deviceId, handler);

        // Create session with minimal fields (may trigger edge cases)
        SessionInfoDto session = new SessionInfoDto();
        session.setId("test-session");
        session.setDeviceId(deviceId);
        // Deliberately leave NowPlayingItem null

        // This should not throw exception - should be handled gracefully
        eventBus.publishSessionUpdate(deviceId, session);

        // Event bus should still be functional
        assertEquals(1, eventBus.getListenerCount(deviceId), "Event bus should still have listener after exception");

        eventBus.unsubscribe(deviceId, handler);
    }

    /**
     * Helper method to create a test session with playing media.
     */
    private SessionInfoDto createTestSession(String deviceId) {
        SessionInfoDto session = new SessionInfoDto();
        session.setId("test-session-id");
        session.setDeviceId(deviceId);

        BaseItemDto item = new BaseItemDto();
        item.setId(java.util.UUID.randomUUID());
        item.setName("Test Movie");
        item.setRunTimeTicks(36000000000L); // 1 hour

        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(false);
        playState.setPositionTicks(18000000000L); // 30 minutes

        session.setNowPlayingItem(item);
        session.setPlayState(playState);

        return session;
    }
}
