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
package org.openhab.binding.atv.internal.client.protocols.raop;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;

/**
 * Implementation of supported feature functionality for RAOP.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopFeatures implements Features {

    private final RaopPlaybackManager playbackManager;

    /**
     * Creates a new features instance.
     *
     * @param playbackManager playback manager holding the stream state
     */
    public RaopFeatures(RaopPlaybackManager playbackManager) {
        this.playbackManager = playbackManager;
    }

    @Override
    public FeatureInfo getFeature(FeatureName featureName) {
        if (featureName == FeatureName.StreamFile) {
            return new FeatureInfo(FeatureState.Available);
        }

        MediaMetadata metadata = RaopStreamClient.EMPTY_METADATA;
        PlaybackInfo playbackInfo = playbackManager.playbackInfo();
        if (playbackInfo != null) {
            metadata = playbackInfo.metadata();
        }

        switch (featureName) {
            case Title:
                return availability(metadata.title() != null);
            case Artist:
                return availability(metadata.artist() != null);
            case Album:
                return availability(metadata.album() != null);
            case Position:
            case TotalTime:
                return availability(metadata.duration() != null && !metadata.duration().isZero());
            // As far as known, volume controls are always supported
            case SetVolume:
            case Volume:
            case VolumeDown:
            case VolumeUp:
                return new FeatureInfo(FeatureState.Available);
            case Stop:
            case Pause:
                return availability(playbackManager.streamClient() != null);
            default:
                return new FeatureInfo(FeatureState.Unavailable);
        }
    }

    private static FeatureInfo availability(boolean available) {
        return new FeatureInfo(available ? FeatureState.Available : FeatureState.Unavailable);
    }
}
