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

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.DeviceState;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.dto.MediaType;
import org.openhab.binding.atv.internal.client.dto.Playing;

/**
 * Implementation of the metadata interface for RAOP.
 *
 * <p>
 * What is playing is derived from the active stream's {@link PlaybackInfo} (idle when
 * nothing is streaming).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopMetadata implements Metadata, CapabilitySource {

    private final RaopPlaybackManager playbackManager;

    /**
     * Creates a new metadata instance.
     *
     * @param playbackManager playback manager holding the stream state
     */
    public RaopMetadata(RaopPlaybackManager playbackManager) {
        this.playbackManager = playbackManager;
    }

    @Override
    public CompletableFuture<Playing> playing() {
        PlaybackInfo playbackInfo = playbackManager.playbackInfo();
        if (playbackInfo == null) {
            return CompletableFuture.completedFuture(
                    Playing.builder().deviceState(DeviceState.Idle).mediaType(MediaType.Unknown).build());
        }

        MediaMetadata metadata = playbackInfo.metadata();
        Integer totalTime = metadata.duration() != null && !metadata.duration().isZero()
                ? (int) (metadata.duration().toMillis() / 1000)
                : null;
        return CompletableFuture.completedFuture(Playing.builder().deviceState(DeviceState.Playing)
                .mediaType(MediaType.Music).title(metadata.title()).artist(metadata.artist()).album(metadata.album())
                .position((int) playbackInfo.position()).totalTime(totalTime).build());
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.METADATA_PLAYING);
    }
}
