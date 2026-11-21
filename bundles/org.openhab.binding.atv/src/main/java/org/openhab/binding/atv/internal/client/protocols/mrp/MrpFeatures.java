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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.Command;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CommandInfoOuterClass.CommandInfo;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.Common.PlaybackState;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemMetadataOuterClass.ContentItemMetadata;

/**
 * Implementation of the supported feature API for MRP.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpFeatures implements Features {

    /** Features that are always available. */
    static final List<FeatureName> FEATURES_SUPPORTED = List.of(FeatureName.Down, FeatureName.Home,
            FeatureName.HomeHold, FeatureName.Left, FeatureName.Menu, FeatureName.Right, FeatureName.Select,
            FeatureName.TopMenu, FeatureName.Up, FeatureName.TurnOn, FeatureName.TurnOff, FeatureName.PowerState,
            FeatureName.OutputDevices, FeatureName.AddOutputDevices, FeatureName.RemoveOutputDevices,
            FeatureName.SetOutputDevices);

    /** Features mapped to playback commands. */
    static final Map<FeatureName, Command> FEATURE_COMMAND_MAP = Map.ofEntries(
            Map.entry(FeatureName.Next, Command.NextTrack), Map.entry(FeatureName.Pause, Command.Pause),
            Map.entry(FeatureName.Play, Command.Play), Map.entry(FeatureName.PlayPause, Command.TogglePlayPause),
            Map.entry(FeatureName.Previous, Command.PreviousTrack), Map.entry(FeatureName.Stop, Command.Stop),
            Map.entry(FeatureName.SetPosition, Command.SeekToPlaybackPosition),
            Map.entry(FeatureName.SetRepeat, Command.ChangeRepeatMode),
            Map.entry(FeatureName.SetShuffle, Command.ChangeShuffleMode),
            Map.entry(FeatureName.Shuffle, Command.ChangeShuffleMode),
            Map.entry(FeatureName.Repeat, Command.ChangeRepeatMode),
            Map.entry(FeatureName.SkipForward, Command.SkipForward),
            Map.entry(FeatureName.SkipBackward, Command.SkipBackward));

    /** Features that are available when the corresponding metadata field is set. */
    static final Map<FeatureName, String> FIELD_FEATURES = Map.ofEntries(Map.entry(FeatureName.Title, "title"),
            Map.entry(FeatureName.Artist, "trackArtistName"), Map.entry(FeatureName.Album, "albumName"),
            Map.entry(FeatureName.Genre, "genre"), Map.entry(FeatureName.TotalTime, "duration"),
            Map.entry(FeatureName.SeriesName, "seriesName"), Map.entry(FeatureName.Position, "elapsedTimeTimestamp"),
            Map.entry(FeatureName.SeasonNumber, "seasonNumber"), Map.entry(FeatureName.EpisodeNumber, "episodeNumber"),
            Map.entry(FeatureName.ContentIdentifier, "contentIdentifier"),
            Map.entry(FeatureName.iTunesStoreIdentifier, "iTunesStoreIdentifier"));

    private final PlayerStateManager psm;
    private final MrpAudio audio;

    /**
     * Creates a new features instance.
     *
     * @param psm player state manager providing the current state
     * @param audio audio implementation used for volume feature availability
     */
    public MrpFeatures(PlayerStateManager psm, MrpAudio audio) {
        this.psm = psm;
        this.audio = audio;
    }

    @Override
    public FeatureInfo getFeature(FeatureName featureName) {
        if (FEATURES_SUPPORTED.contains(featureName)) {
            return new FeatureInfo(FeatureState.Available);
        }
        if (featureName == FeatureName.Artwork) {
            @Nullable
            ContentItemMetadata metadata = psm.playing().metadata();
            if (metadata != null && metadata.getArtworkAvailable()) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }

        String fieldName = FIELD_FEATURES.get(featureName);
        if (fieldName != null) {
            boolean available = psm.playing().metadataField(fieldName) != null;
            return new FeatureInfo(available ? FeatureState.Available : FeatureState.Unavailable);
        }

        // Special case for PlayPause emulation. Based on the behavior in the Youtube
        // app, only the "opposite" feature to the current state is available. E.g. if
        // something is playing, then pause will be available but not play.
        if (featureName == FeatureName.PlayPause) {
            PlaybackState.@Nullable Enum playbackState = psm.playing().playbackState();
            if (playbackState == PlaybackState.Enum.Playing && inState(FeatureState.Available, FeatureName.Pause)) {
                return new FeatureInfo(FeatureState.Available);
            }
            if (playbackState == PlaybackState.Enum.Paused && inState(FeatureState.Available, FeatureName.Play)) {
                return new FeatureInfo(FeatureState.Available);
            }
        }

        Command commandId = FEATURE_COMMAND_MAP.get(featureName);
        if (commandId != null) {
            @Nullable
            CommandInfo command = psm.playing().commandInfo(commandId);
            if (command != null && command.getEnabled()) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }

        if (featureName == FeatureName.App) {
            if (psm.client() != null) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }

        if (featureName == FeatureName.VolumeDown || featureName == FeatureName.VolumeUp) {
            if (audio.isAvailable()) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }

        if (featureName == FeatureName.Volume || featureName == FeatureName.SetVolume) {
            if (audio.isAvailable() && audio.isVolumeAbsolute()) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }

        return new FeatureInfo(FeatureState.Unsupported);
    }
}
