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

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Relay implementation for stream functionality.
 *
 * <p>
 * {@code playUrl} additionally requires the {@link FeatureName#PlayUrl} feature to be
 * {@link FeatureState#Available}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class StreamRelay extends BaseRelay<Stream> implements Stream {

    private final Features features;

    /**
     * Creates a new relay stream instance.
     *
     * @param guard device guard blocking calls after close
     * @param features features interface used to check {@link FeatureName#PlayUrl} availability
     */
    public StreamRelay(Guard guard, Features features) {
        super(new Relayer<>(Stream.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
        this.features = Objects.requireNonNull(features, "features");
    }

    @Override
    public void close() {
        guard.requireNotBlocked("close");
        relayer.relay(Capability.STREAM_CLOSE).close();
    }

    @Override
    public CompletableFuture<Void> playUrl(String url, Map<String, Object> options) {
        guard.requireNotBlocked("playUrl");
        if (!features.inState(FeatureState.Available, FeatureName.PlayUrl)) {
            return CompletableFuture.failedFuture(new NotSupportedError("play_url is not supported"));
        }
        return relayAsync(Capability.STREAM_PLAY_URL, stream -> stream.playUrl(url, options));
    }

    @Override
    public CompletableFuture<Void> streamFile(String source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        guard.requireNotBlocked("streamFile");
        return relayAsync(Capability.STREAM_STREAM_FILE, stream -> stream.streamFile(source, metadata, options));
    }

    @Override
    public CompletableFuture<Void> streamFile(InputStream source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        guard.requireNotBlocked("streamFile");
        return relayAsync(Capability.STREAM_STREAM_BUFFER, stream -> stream.streamFile(source, metadata, options));
    }
}
