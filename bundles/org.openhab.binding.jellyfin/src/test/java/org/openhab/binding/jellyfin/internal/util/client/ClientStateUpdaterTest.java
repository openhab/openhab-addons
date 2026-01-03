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

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayerStateInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link ClientStateUpdater}.
 *
 * Verifies channel state calculations from session data including metadata mapping,
 * playback state, position/duration conversions, and null/blank value handling.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class ClientStateUpdaterTest {

    @Test
    void calculateChannelStatesReturnsNullStatesWhenSessionMissing() {
        Map<String, State> states = ClientStateUpdater.calculateChannelStates(null);

        // 18 channels: 10 read-only status + 1 media-control + 1 media-stop + 6 extended controls
        assertEquals(18, states.size());
        states.forEach((id, state) -> assertEquals(UnDefType.NULL, state, "Expected NULL for channel " + id));
    }

    @Test
    void calculateChannelStatesPopulatesMetadataAndPlayback() {
        BaseItemDto playingItem = new BaseItemDto();
        playingItem.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        playingItem.setName("Pilot");
        playingItem.setSeriesName("My Show");
        playingItem.setSeasonName("Season One");
        playingItem.setParentIndexNumber(2);
        playingItem.setIndexNumber(5);
        playingItem.setGenres(Arrays.asList("Action", "Drama"));
        playingItem.setType(BaseItemKind.EPISODE);
        playingItem.setRunTimeTicks(30_000_000L);

        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(false);
        playState.setPositionTicks(15_000_000L);

        SessionInfoDto session = new SessionInfoDto();
        session.setId("session-1");
        session.setNowPlayingItem(playingItem);
        session.setPlayState(playState);

        Map<String, State> states = ClientStateUpdater.calculateChannelStates(session);

        assertEquals(PlayPauseType.PLAY, states.get(Constants.MEDIA_CONTROL_CHANNEL));
        assertEquals(new PercentType(50), states.get(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL));
        assertEquals(new DecimalType(2), states.get(Constants.PLAYING_ITEM_SECOND_CHANNEL));
        assertEquals(new DecimalType(3), states.get(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL));
        assertEquals(new StringType(playingItem.getId().toString()), states.get(Constants.PLAYING_ITEM_ID_CHANNEL));
        assertEquals(new StringType("Pilot"), states.get(Constants.PLAYING_ITEM_NAME_CHANNEL));
        assertEquals(new StringType("My Show"), states.get(Constants.PLAYING_ITEM_SERIES_NAME_CHANNEL));
        assertEquals(new StringType("Season One"), states.get(Constants.PLAYING_ITEM_SEASON_NAME_CHANNEL));
        assertEquals(new DecimalType(2), states.get(Constants.PLAYING_ITEM_SEASON_CHANNEL));
        assertEquals(new DecimalType(5), states.get(Constants.PLAYING_ITEM_EPISODE_CHANNEL));
        assertEquals(new StringType("Action, Drama"), states.get(Constants.PLAYING_ITEM_GENRES_CHANNEL));
        assertEquals(new StringType(BaseItemKind.EPISODE.toString()), states.get(Constants.PLAYING_ITEM_TYPE_CHANNEL));
    }

    @Test
    void calculateChannelStatesHandlesPauseAndBlankValues() {
        BaseItemDto playingItem = new BaseItemDto();
        playingItem.setName("");
        playingItem.setSeriesName("");
        playingItem.setSeasonName("");
        playingItem.setGenres(Arrays.asList());

        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(true);
        playState.setPositionTicks(null);

        SessionInfoDto session = new SessionInfoDto();
        session.setNowPlayingItem(playingItem);
        session.setPlayState(playState);

        Map<String, State> states = ClientStateUpdater.calculateChannelStates(session);

        assertEquals(PlayPauseType.PAUSE, states.get(Constants.MEDIA_CONTROL_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_SECOND_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_ID_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_NAME_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_SERIES_NAME_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_SEASON_NAME_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_SEASON_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_EPISODE_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_GENRES_CHANNEL));
        assertEquals(UnDefType.NULL, states.get(Constants.PLAYING_ITEM_TYPE_CHANNEL));
    }
}
