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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlayerStateInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Calculates channel states for a Jellyfin client session.
 * Pure utility: callers apply the returned states to their channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class ClientStateUpdater {

    private ClientStateUpdater() {
    }

    public static Map<String, State> calculateChannelStates(@Nullable SessionInfoDto session) {
        Map<String, State> states = new HashMap<>();

        if (session == null) {
            addNullStates(states);
            return states;
        }

        BaseItemDto playingItem = session.getNowPlayingItem();
        PlayerStateInfo playState = session.getPlayState();

        Long runTimeTicks = playingItem != null ? playingItem.getRunTimeTicks() : null;
        Long positionTicks = playState != null ? playState.getPositionTicks() : null;

        // Media control channel
        states.put(Constants.MEDIA_CONTROL_CHANNEL,
                playingItem != null && playState != null && !Boolean.TRUE.equals(playState.getIsPaused())
                        ? PlayPauseType.PLAY
                        : PlayPauseType.PAUSE);

        // Position percentage channel
        if (runTimeTicks != null && runTimeTicks > 0 && positionTicks != null) {
            int percent = (int) Math.round((positionTicks * 100.0) / runTimeTicks);
            states.put(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, new PercentType(percent));
        } else {
            states.put(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, UnDefType.NULL);
        }

        // Position seconds channel
        if (positionTicks != null) {
            long seconds = Math.round(positionTicks / 10_000_000.0);
            states.put(Constants.PLAYING_ITEM_SECOND_CHANNEL, new DecimalType(seconds));
        } else {
            states.put(Constants.PLAYING_ITEM_SECOND_CHANNEL, UnDefType.NULL);
        }

        // Total runtime seconds channel
        if (runTimeTicks != null) {
            long totalSeconds = Math.round(runTimeTicks / 10_000_000.0);
            states.put(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL, new DecimalType(totalSeconds));
        } else {
            states.put(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL, UnDefType.NULL);
        }

        // Item metadata channels
        addStringState(states, Constants.PLAYING_ITEM_ID_CHANNEL, playingItem != null ? playingItem.getId() : null);
        addStringState(states, Constants.PLAYING_ITEM_NAME_CHANNEL, playingItem != null ? playingItem.getName() : null);
        addStringState(states, Constants.PLAYING_ITEM_SERIES_NAME_CHANNEL,
                playingItem != null ? playingItem.getSeriesName() : null);
        addStringState(states, Constants.PLAYING_ITEM_SEASON_NAME_CHANNEL,
                playingItem != null ? playingItem.getSeasonName() : null);

        Integer parentIndexNumber = playingItem != null ? playingItem.getParentIndexNumber() : null;
        if (parentIndexNumber != null) {
            states.put(Constants.PLAYING_ITEM_SEASON_CHANNEL, new DecimalType(parentIndexNumber));
        } else {
            states.put(Constants.PLAYING_ITEM_SEASON_CHANNEL, UnDefType.NULL);
        }

        Integer indexNumber = playingItem != null ? playingItem.getIndexNumber() : null;
        if (indexNumber != null) {
            states.put(Constants.PLAYING_ITEM_EPISODE_CHANNEL, new DecimalType(indexNumber));
        } else {
            states.put(Constants.PLAYING_ITEM_EPISODE_CHANNEL, UnDefType.NULL);
        }

        List<String> genres = playingItem != null ? playingItem.getGenres() : null;
        if (genres != null && !genres.isEmpty()) {
            states.put(Constants.PLAYING_ITEM_GENRES_CHANNEL, new StringType(String.join(", ", genres)));
        } else {
            states.put(Constants.PLAYING_ITEM_GENRES_CHANNEL, UnDefType.NULL);
        }

        if (playingItem != null && playingItem.getType() != null) {
            states.put(Constants.PLAYING_ITEM_TYPE_CHANNEL, new StringType(playingItem.getType().toString()));
        } else {
            states.put(Constants.PLAYING_ITEM_TYPE_CHANNEL, UnDefType.NULL);
        }

        return states;
    }

    private static void addStringState(Map<String, State> states, String channelId, @Nullable Object value) {
        if (value != null) {
            String text = value.toString();
            if (!text.isBlank()) {
                states.put(channelId, new StringType(text));
                return;
            }
        }
        states.put(channelId, UnDefType.NULL);
    }

    private static void addNullStates(Map<String, State> states) {
        states.put(Constants.MEDIA_CONTROL_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_SECOND_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_ID_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_NAME_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_SERIES_NAME_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_SEASON_NAME_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_SEASON_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_EPISODE_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_GENRES_CHANNEL, UnDefType.NULL);
        states.put(Constants.PLAYING_ITEM_TYPE_CHANNEL, UnDefType.NULL);
        // Stop channel (Switch type, command-only)
        states.put(Constants.MEDIA_STOP_CHANNEL, UnDefType.NULL);
        // Phase 11: Extended media controls (command-only)
        states.put(Constants.MEDIA_SHUFFLE_CHANNEL, UnDefType.NULL);
        states.put(Constants.MEDIA_REPEAT_CHANNEL, UnDefType.NULL);
        states.put(Constants.MEDIA_QUALITY_CHANNEL, UnDefType.NULL);
        states.put(Constants.MEDIA_AUDIO_TRACK_CHANNEL, UnDefType.NULL);
        states.put(Constants.MEDIA_SUBTITLE_CHANNEL, UnDefType.NULL);
    }
}
