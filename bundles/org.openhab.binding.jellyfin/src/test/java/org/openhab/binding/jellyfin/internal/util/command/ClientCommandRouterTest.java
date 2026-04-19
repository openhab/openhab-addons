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
package org.openhab.binding.jellyfin.internal.util.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link ClientCommandRouter}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClientCommandRouterTest {

    private static final String SESSION_ID = "test-session-id";
    private static final ThingUID THING_UID = new ThingUID("jellyfin", "client", "test-device");

    @Mock
    @NonNullByDefault({})
    private ServerHandler serverHandler;

    @NonNullByDefault({})
    private ScheduledExecutorService scheduler;

    @NonNullByDefault({})
    private SessionInfoDto session;

    @NonNullByDefault({})
    private ClientCommandRouter router;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        session = new SessionInfoDto();
        session.setId(SESSION_ID);

        router = new ClientCommandRouter(serverHandler, () -> session, scheduler);
    }

    @AfterEach
    void tearDown() {
        router.dispose();
        scheduler.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Media-control channel
    // -------------------------------------------------------------------------

    @Test
    void testPlayCommandSendsUnpause() {
        route(Constants.MEDIA_CONTROL_CHANNEL, PlayPauseType.PLAY);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.UNPAUSE, null);
    }

    @Test
    void testPauseCommandSendsPause() {
        route(Constants.MEDIA_CONTROL_CHANNEL, PlayPauseType.PAUSE);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.PAUSE, null);
    }

    @Test
    void testPauseStringCommandSendsPause() {
        route(Constants.MEDIA_CONTROL_CHANNEL, new StringType("PAUSE"));

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.PAUSE, null);
    }

    @Test
    void testPlayStringCommandSendsUnpause() {
        route(Constants.MEDIA_CONTROL_CHANNEL, new StringType("PLAY"));

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.UNPAUSE, null);
    }

    @Test
    void testNextTrackCommandSendsNextTrack() {
        route(Constants.MEDIA_CONTROL_CHANNEL, NextPreviousType.NEXT);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.NEXT_TRACK, null);
    }

    @Test
    void testPreviousTrackCommandSendsPreviousTrack() {
        route(Constants.MEDIA_CONTROL_CHANNEL, NextPreviousType.PREVIOUS);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.PREVIOUS_TRACK, null);
    }

    @Test
    void testFastForwardCommandSendsFastForward() {
        route(Constants.MEDIA_CONTROL_CHANNEL, RewindFastforwardType.FASTFORWARD);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.FAST_FORWARD, null);
    }

    @Test
    void testRewindCommandSendsRewind() {
        route(Constants.MEDIA_CONTROL_CHANNEL, RewindFastforwardType.REWIND);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.REWIND, null);
    }

    // -------------------------------------------------------------------------
    // Stop channel
    // -------------------------------------------------------------------------

    @Test
    void testMediaStopOnSendsStop() {
        route(Constants.MEDIA_STOP_CHANNEL, OnOffType.ON);

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.STOP, null);
    }

    @Test
    void testMediaStopOffIsIgnored() {
        route(Constants.MEDIA_STOP_CHANNEL, OnOffType.OFF);

        verify(serverHandler, never()).sendPlayStateCommand(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // Seek channels
    // -------------------------------------------------------------------------

    @Test
    void testSeekByPercentConvertsToTicks() {
        BaseItemDto playingItem = new BaseItemDto();
        playingItem.setRunTimeTicks(100_000_000L); // 10 seconds
        session.setNowPlayingItem(playingItem);

        route(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, new PercentType(50));

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.SEEK, 50_000_000L);
    }

    @Test
    void testSeekBySecondsConvertsToTicks() {
        route(Constants.PLAYING_ITEM_SECOND_CHANNEL, new DecimalType(5));

        verify(serverHandler).sendPlayStateCommand(SESSION_ID, PlaystateCommand.SEEK, 50_000_000L);
    }

    @Test
    void testSeekByPercentWithoutSessionIsIgnored() {
        router = new ClientCommandRouter(serverHandler, () -> null, scheduler);

        route(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, new PercentType(50));

        verify(serverHandler, never()).sendPlayStateCommand(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // Play-by-terms channel
    // -------------------------------------------------------------------------

    @Test
    void testPlayByTermsSearchesForMovieFirst() throws Exception {
        UUID movieId = UUID.randomUUID();
        BaseItemDto movie = mockItem(movieId);
        when(serverHandler.searchItem(any(String.class), eq(BaseItemKind.MOVIE), isNull())).thenReturn(movie);

        route(Constants.PLAY_BY_TERMS_CHANNEL, new StringType("Inception"));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverHandler).playItem(any(), any(), idCaptor.capture(), any());
        assertEquals(movieId.toString(), idCaptor.getValue());
    }

    @Test
    void testPlayByTermsFallsBackToEpisode() throws Exception {
        UUID episodeId = UUID.randomUUID();
        BaseItemDto episode = mockItem(episodeId);
        when(serverHandler.searchItem(any(String.class), eq(BaseItemKind.MOVIE), isNull())).thenReturn(null);
        when(serverHandler.searchItem(any(String.class), eq(BaseItemKind.EPISODE), isNull())).thenReturn(episode);

        route(Constants.PLAY_BY_TERMS_CHANNEL, new StringType("Friends"));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverHandler).playItem(any(), any(), idCaptor.capture(), any());
        assertEquals(episodeId.toString(), idCaptor.getValue());
    }

    // -------------------------------------------------------------------------
    // Play-by-id channel
    // -------------------------------------------------------------------------

    @Test
    void testPlayByIdWithValidUuidPlaysItem() throws Exception {
        UUID itemId = UUID.randomUUID();

        route(Constants.PLAY_BY_ID_CHANNEL, new StringType(itemId.toString()));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverHandler).playItem(any(), any(), idCaptor.capture(), any());
        assertEquals(itemId.toString(), idCaptor.getValue());
    }

    @Test
    void testPlayByIdWithInvalidUuidIsIgnored() throws Exception {
        route(Constants.PLAY_BY_ID_CHANNEL, new StringType("not-a-valid-uuid"));

        verify(serverHandler, never()).playItem(any(), any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // Unknown channel
    // -------------------------------------------------------------------------

    @Test
    void testUnknownChannelDoesNotThrow() {
        route("unknown-channel-id", new StringType("value"));
        // No exception expected; no interactions with the server handler
        verifyNoInteractions(serverHandler);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private void route(String channelId, org.openhab.core.types.Command command) {
        router.route(new ChannelUID(THING_UID, channelId), command);
    }

    /**
     * Creates a {@link BaseItemDto} with a fixed {@link UUID}.
     *
     * @param id the item ID to set
     * @return a new {@link BaseItemDto} instance
     */
    private static BaseItemDto mockItem(UUID id) {
        BaseItemDto item = new BaseItemDto();
        item.setId(id);
        return item;
    }
}
