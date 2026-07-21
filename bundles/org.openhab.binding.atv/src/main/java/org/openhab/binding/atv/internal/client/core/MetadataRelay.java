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
package org.openhab.binding.atv.internal.client.core;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.dto.App;
import org.openhab.binding.atv.internal.client.dto.ArtworkInfo;
import org.openhab.binding.atv.internal.client.dto.Playing;

/**
 * Relay implementation for retrieving metadata from an Apple TV.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MetadataRelay extends BaseRelay<Metadata> implements Metadata {

    /**
     * Creates a new relay metadata instance.
     *
     * @param guard device guard blocking calls after close
     */
    public MetadataRelay(Guard guard) {
        super(new Relayer<>(Metadata.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
    }

    @Override
    public Optional<String> deviceId() {
        guard.requireNotBlocked("deviceId");
        return relayer.relay(Capability.METADATA_DEVICE_ID).deviceId();
    }

    @Override
    public CompletableFuture<ArtworkInfo> artwork(@Nullable Integer width, @Nullable Integer height) {
        guard.requireNotBlocked("artwork");
        return relayAsync(Capability.METADATA_ARTWORK, metadata -> metadata.artwork(width, height));
    }

    @Override
    public @Nullable String artworkId() {
        guard.requireNotBlocked("artworkId");
        return relayer.relay(Capability.METADATA_ARTWORK_ID).artworkId();
    }

    @Override
    public CompletableFuture<Playing> playing() {
        guard.requireNotBlocked("playing");
        return relayAsync(Capability.METADATA_PLAYING, Metadata::playing);
    }

    @Override
    public Optional<App> app() {
        guard.requireNotBlocked("app");
        return relayer.relay(Capability.METADATA_APP).app();
    }
}
