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
package org.openhab.binding.atv.internal.client.capability;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for streaming media to a device.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Stream {

    /**
     * Closes the connection and releases allocated resources.
     *
     * @throws NotSupportedError if not supported
     */
    default void close() {
        throw new NotSupportedError("close is not supported");
    }

    /**
     * Plays media from a URL on the device.
     *
     * @param url URL to media
     * @param options protocol specific options (e.g. start position)
     * @return future completing when playback has started
     */
    default CompletableFuture<Void> playUrl(String url, Map<String, Object> options) {
        return CompletableFuture.failedFuture(new NotSupportedError("playUrl is not supported"));
    }

    /**
     * Plays media from a URL on the device without options.
     *
     * @param url URL to media
     * @return future completing when playback has started
     */
    default CompletableFuture<Void> playUrl(String url) {
        return playUrl(url, Map.of());
    }

    /**
     * Streams a local or remote file to the device. Supports either local file paths or an HTTP(S) address.
     *
     * @param source local file path or HTTP(S) URL
     * @param metadata media metadata overriding what is extracted from the file (may be {@code null})
     * @param options protocol specific options
     * @return future completing when streaming has finished
     */
    default CompletableFuture<Void> streamFile(String source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        return CompletableFuture.failedFuture(new NotSupportedError("streamFile is not supported"));
    }

    /**
     * Streams a local or remote file to the device without metadata or options.
     *
     * @param source local file path or HTTP(S) URL
     * @return future completing when streaming has finished
     */
    default CompletableFuture<Void> streamFile(String source) {
        return streamFile(source, null, Map.of());
    }

    /**
     * Streams media from an input stream (buffer) to the device.
     *
     * @param source stream providing the media data
     * @param metadata media metadata (may be {@code null}; usually required as it cannot be extracted from a raw
     *            stream)
     * @param options protocol specific options
     * @return future completing when streaming has finished
     */
    default CompletableFuture<Void> streamFile(InputStream source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        return CompletableFuture.failedFuture(new NotSupportedError("streamFile is not supported"));
    }
}
