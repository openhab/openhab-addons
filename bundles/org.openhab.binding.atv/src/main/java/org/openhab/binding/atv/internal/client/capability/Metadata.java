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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.App;
import org.openhab.binding.atv.internal.client.dto.ArtworkInfo;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for retrieving metadata from an Apple TV.
 *
 * <p>
 * All methods throw or complete exceptionally with {@link NotSupportedError} unless overridden
 * by a protocol implementation that supports them.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface Metadata {

    /**
     * Returns a unique identifier for the current device.
     *
     * @return device identifier if available
     * @throws NotSupportedError if not supported
     */
    default Optional<String> deviceId() {
        throw new NotSupportedError("deviceId is not supported");
    }

    /**
     * Returns artwork for what is currently playing (or {@code null} if no artwork is available).
     *
     * <p>
     * The parameters {@code width} and {@code height} make it possible to request artwork of a specific size. This
     * is just a request, the device might impose restrictions and return artwork of a different size. Set both
     * parameters to {@code null} to request the default size. Set one of them and let the other one be {@code null}
     * to keep the original aspect ratio.
     *
     * @param width requested width in pixels or {@code null}
     * @param height requested height in pixels or {@code null}
     * @return future completing with artwork or {@code null} when none is available
     */
    default CompletableFuture<ArtworkInfo> artwork(@Nullable Integer width, @Nullable Integer height) {
        return CompletableFuture.failedFuture(new NotSupportedError("artwork is not supported"));
    }

    /**
     * Returns artwork for what is currently playing using the default width (512 pixels, original aspect ratio).
     *
     * @return future completing with artwork or {@code null} when none is available
     */
    default CompletableFuture<ArtworkInfo> artwork() {
        return artwork(512, null);
    }

    /**
     * Returns a unique identifier for the current artwork.
     *
     * @return artwork identifier, or {@code null} when nothing is playing or the current item has no artwork
     * @throws NotSupportedError if not supported
     */
    default @Nullable String artworkId() {
        throw new NotSupportedError("artworkId is not supported");
    }

    /**
     * Returns what is currently playing.
     *
     * @return future completing with current play state
     */
    default CompletableFuture<Playing> playing() {
        return CompletableFuture.failedFuture(new NotSupportedError("playing is not supported"));
    }

    /**
     * Returns information about the app currently playing something.
     *
     * <p>
     * Do note that this method returns which app is currently playing something and not which app is currently
     * active. If nothing is playing, the corresponding feature will be unavailable.
     *
     * @return app playing media if available
     * @throws NotSupportedError if not supported
     */
    default Optional<App> app() {
        throw new NotSupportedError("app is not supported");
    }
}
