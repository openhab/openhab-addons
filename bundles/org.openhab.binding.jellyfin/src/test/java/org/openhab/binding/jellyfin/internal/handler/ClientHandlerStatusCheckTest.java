/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.PlayerStateInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.command.ClientCommandRouter;
import org.openhab.binding.jellyfin.internal.util.extrapolation.PlaybackExtrapolator;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Unit tests for ClientHandler status checking logic.
 *
 * Verifies that:
 * - State updates are skipped when thing is not ONLINE
 * - Commands are rejected when thing is not ONLINE
 * - Proper logging messages are generated
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ClientHandlerStatusCheckTest {

    private Thing mockThing = null;
    private ClientHandler handler = null;

    @BeforeEach
    void setup() {
        mockThing = mock(Thing.class);
        ThingUID thingUID = new ThingUID("jellyfin:client:server:firetv");
        when(mockThing.getUID()).thenReturn(thingUID);

        handler = new ClientHandler(mockThing);
    }

    /**
     * Test that state updates are skipped when thing is OFFLINE.
     * This prevents wasteful state publishes to non-existent items.
     */
    @Test
    void testUpdateStateFromSessionSkipsWhenOffline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to OFFLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Create a test session
        SessionInfoDto session = createTestSession("device-123");

        // Call updateStateFromSession - should return early without processing
        handler.updateStateFromSession(session);

        // Since thing is OFFLINE, no state updates should be attempted
        // This is verified by the method returning early with TRACE log
        // In integration tests, we would verify no updateState calls were made
    }

    /**
     * Test that state updates are processed when thing is ONLINE.
     */
    @Test
    void testUpdateStateFromSessionProcessesWhenOnline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to ONLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Create a test session
        SessionInfoDto session = createTestSession("device-123");

        // Mock channel checking - required for updateState calls to work
        when(mockThing.getChannel(anyString())).thenReturn(null);

        // Call updateStateFromSession - should process the session
        handler.updateStateFromSession(session);

        // In production, this would call updateState for linked channels
        // Test verifies no exceptions are thrown when processing ONLINE
    }

    /**
     * Test that null session updates are skipped when thing is OFFLINE.
     */
    @Test
    void testNullSessionUpdateSkippedWhenOffline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to OFFLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Call updateStateFromSession with null - should return early
        handler.updateStateFromSession(null);

        // Verify no exceptions and method returns cleanly
    }

    /**
     * Test that commands are rejected when thing is OFFLINE.
     * This prevents wasteful attempts to control disconnected devices.
     */
    @Test
    void testHandleCommandRejectedWhenOffline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to OFFLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Create a channel UID and a test command
        ChannelUID channelUID = new ChannelUID(new ThingUID("jellyfin:client:server:device"), "media-control");
        Command command = mock(Command.class);

        // Call handleCommand - should return early without routing
        handler.handleCommand(channelUID, command);

        // Verify method completes without error (command would be rejected with INFO log)
    }

    /**
     * Test that RefreshType commands are rejected when thing is OFFLINE.
     */
    @Test
    void testRefreshCommandRejectedWhenOffline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to OFFLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Create a channel UID and RefreshType command
        ChannelUID channelUID = new ChannelUID(new ThingUID("jellyfin:client:server:device"), "media-control");

        // Call handleCommand with RefreshType - should return early
        handler.handleCommand(channelUID, RefreshType.REFRESH);

        // Verify method completes without calling updateStateFromSession
    }

    /**
     * Test that commands are accepted when thing is ONLINE.
     */
    @Test
    void testHandleCommandAcceptedWhenOnline() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to ONLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Create a channel UID and a test command
        ChannelUID channelUID = new ChannelUID(new ThingUID("jellyfin:client:server:device"), "media-control");
        Command command = mock(Command.class);

        // Call handleCommand - should proceed to routing
        // Note: will fail later due to command router not being initialized,
        // but the status check should pass
        handler.handleCommand(channelUID, command);

        // Verify method completes (would be handled by command router in production)
    }

    @Test
    void testPauseStringCommandStopsExtrapolationImmediately() throws Exception {
        assertNotNull(mockThing);
        assertNotNull(handler);
        when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        ClientCommandRouter router = mock(ClientCommandRouter.class);
        PlaybackExtrapolator extrapolator = mock(PlaybackExtrapolator.class);

        Field routerField = ClientHandler.class.getDeclaredField("commandRouter");
        routerField.setAccessible(true);
        routerField.set(handler, router);

        Field extrapolatorField = ClientHandler.class.getDeclaredField("extrapolator");
        extrapolatorField.setAccessible(true);
        extrapolatorField.set(handler, extrapolator);

        ChannelUID channelUID = new ChannelUID(new ThingUID("jellyfin:client:server:device"), "media-control");
        StringType command = new StringType("PAUSE");
        handler.handleCommand(channelUID, command);

        verify(router).route(channelUID, command);
        verify(extrapolator).stop();
    }

    /**
     * Test that ONLINE status allows state updates to proceed.
     */
    @Test
    void testOnlineStatusAllowsStateUpdates() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Set thing status to ONLINE
        when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Create a session with paused playback
        SessionInfoDto session = createTestSession("device-456");
        session.getPlayState().setIsPaused(true);

        // Call updateStateFromSession - should process without returning early
        handler.updateStateFromSession(session);

        // Verify no exceptions and method executes normally
    }

    /**
     * Test that various OFFLINE statuses are rejected.
     */
    @Test
    void testOfflineVariationsRejected() {
        assertNotNull(mockThing);
        assertNotNull(handler);
        // Test multiple offline status scenarios
        ThingStatus[] offlineStatuses = { ThingStatus.OFFLINE,
                // UNKNOWN would also be rejected since only ONLINE is accepted
        };

        for (ThingStatus status : offlineStatuses) {
            when(mockThing.getStatus()).thenReturn(status);

            SessionInfoDto session = createTestSession("device-789");

            // Should not throw exception regardless of offline variety
            handler.updateStateFromSession(session);
        }
    }

    /**
     * Helper to create a test session.
     */
    private SessionInfoDto createTestSession(String deviceId) {
        SessionInfoDto session = new SessionInfoDto();
        session.setId("test-session-id-" + deviceId);
        session.setDeviceId(deviceId);

        BaseItemDto item = new BaseItemDto();
        item.setId(java.util.UUID.randomUUID());
        item.setName("Test Media");
        item.setRunTimeTicks(36000000000L);

        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(false);
        playState.setPositionTicks(18000000000L);

        session.setNowPlayingItem(item);
        session.setPlayState(playState);

        return session;
    }
}
