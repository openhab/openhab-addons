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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.DeviceState;
import org.openhab.binding.atv.internal.client.dto.MediaType;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.dto.RepeatState;
import org.openhab.binding.atv.internal.client.dto.ShuffleState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.Command;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.CommandInfo;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.PlaybackState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.RepeatMode;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.ShuffleMode;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemMetadataOuterClass.ContentItemMetadata;

/**
 * Builds {@link Playing} instances from an MRP player state, including the position
 * estimation from {@code elapsedTimeTimestamp} and playback rate. The wall clock is
 * injectable so tests can freeze time.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class MrpPlaying {

    /** Seconds between the Unix epoch (1970) and the Cocoa epoch (2001). */
    private static final long COCOA_EPOCH_OFFSET_SECONDS = Duration
            .between(Instant.parse("1970-01-01T00:00:00Z"), Instant.parse("2001-01-01T00:00:00Z")).getSeconds();

    private MrpPlaying() {
    }

    private static Instant cocoaToInstant(double cocoaTime) {
        double unixSeconds = cocoaTime + COCOA_EPOCH_OFFSET_SECONDS;
        long seconds = (long) Math.floor(unixSeconds);
        long nanos = (long) ((unixSeconds - seconds) * 1_000_000_000L);
        return Instant.ofEpochSecond(seconds, nanos);
    }

    /**
     * Builds a {@link Playing} instance from a player state.
     *
     * @param state the player state to snapshot
     * @param clock wall clock used for position estimation
     * @return the immutable playing snapshot
     */
    static Playing buildPlayingInstance(PlayerStateManager.PlayerState state, Clock clock) {
        Playing.Builder builder = Playing.builder();

        // Type of media that is currently playing
        MediaType mediaType = MediaType.Unknown;
        @Nullable
        ContentItemMetadata metadata = state.metadata();
        if (metadata != null) {
            if (metadata.getMediaType() == ContentItemMetadata.MediaType.Audio) {
                mediaType = MediaType.Music;
            } else if (metadata.getMediaType() == ContentItemMetadata.MediaType.Video) {
                mediaType = MediaType.Video;
            }
        }
        builder.mediaType(mediaType);

        // Device state, e.g. playing or paused
        PlaybackState.@Nullable Enum playbackState = state.playbackState();
        DeviceState deviceState;
        if (playbackState == null) {
            deviceState = DeviceState.Idle;
        } else {
            deviceState = switch (playbackState) {
                case Playing -> DeviceState.Playing;
                case Paused -> DeviceState.Paused;
                case Stopped -> DeviceState.Stopped;
                case Interrupted -> DeviceState.Loading;
                case Seeking -> DeviceState.Seeking;
                default -> DeviceState.Paused;
            };
        }
        builder.deviceState(deviceState);

        builder.title((String) state.metadataField("title"));
        builder.artist((String) state.metadataField("trackArtistName"));
        builder.album((String) state.metadataField("albumName"));
        builder.genre((String) state.metadataField("genre"));

        // Total play time in seconds
        @Nullable
        Object duration = state.metadataField("duration");
        if (duration != null) {
            double value = ((Number) duration).doubleValue();
            if (!Double.isNaN(value)) {
                builder.totalTime((int) value);
            }
        }

        builder.position(position(state, deviceState, clock));

        // Shuffle state
        @Nullable
        CommandInfo shuffleInfo = state.commandInfo(Command.ChangeShuffleMode);
        ShuffleState shuffle;
        if (shuffleInfo == null || shuffleInfo.getShuffleMode() == ShuffleMode.Enum.Off) {
            shuffle = ShuffleState.Off;
        } else if (shuffleInfo.getShuffleMode() == ShuffleMode.Enum.Albums) {
            shuffle = ShuffleState.Albums;
        } else {
            shuffle = ShuffleState.Songs;
        }
        builder.shuffle(shuffle);

        // Repeat mode
        @Nullable
        CommandInfo repeatInfo = state.commandInfo(Command.ChangeRepeatMode);
        RepeatState repeat;
        if (repeatInfo == null) {
            repeat = RepeatState.Off;
        } else if (repeatInfo.getRepeatMode() == RepeatMode.Enum.One) {
            repeat = RepeatState.Track;
        } else if (repeatInfo.getRepeatMode() == RepeatMode.Enum.All) {
            repeat = RepeatState.All;
        } else {
            repeat = RepeatState.Off;
        }
        builder.repeat(repeat);

        builder.hash(state.itemIdentifier());
        builder.seriesName((String) state.metadataField("seriesName"));

        @Nullable
        Object seasonNumber = state.metadataField("seasonNumber");
        if (seasonNumber != null) {
            builder.seasonNumber(((Number) seasonNumber).intValue());
        }
        @Nullable
        Object episodeNumber = state.metadataField("episodeNumber");
        if (episodeNumber != null) {
            builder.episodeNumber(((Number) episodeNumber).intValue());
        }
        builder.contentIdentifier((String) state.metadataField("contentIdentifier"));
        @Nullable
        Object itunesStoreIdentifier = state.metadataField("iTunesStoreIdentifier");
        if (itunesStoreIdentifier != null) {
            builder.iTunesStoreIdentifier(((Number) itunesStoreIdentifier).longValue());
        }

        return builder.build();
    }

    private static @Nullable Integer position(PlayerStateManager.PlayerState state, DeviceState deviceState,
            Clock clock) {
        @Nullable
        Object elapsedTimestamp = state.metadataField("elapsedTimeTimestamp");

        // Without a reference time there is nothing to estimate from; an explicit 0.0
        // timestamp is treated the same as a missing one
        if (elapsedTimestamp == null || ((Number) elapsedTimestamp).doubleValue() == 0.0) {
            return null;
        }

        @Nullable
        Object elapsedTimeField = state.metadataField("elapsedTime");
        double elapsedTime = elapsedTimeField == null ? 0.0 : ((Number) elapsedTimeField).doubleValue();

        Instant reference = cocoaToInstant(((Number) elapsedTimestamp).doubleValue());
        double diff = Duration.between(reference, clock.instant()).toNanos() / 1_000_000_000.0;

        @Nullable
        Object playbackRateField = state.metadataField("playbackRate");
        double playbackRate = playbackRateField == null ? 0.0 : ((Number) playbackRateField).doubleValue();

        if (deviceState == DeviceState.Playing && Math.abs(playbackRate) > 1e-9) {
            return (int) (elapsedTime + diff);
        }
        return (int) elapsedTime;
    }
}
