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
package org.openhab.binding.atv.internal.client.dto;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * Container for media (e.g. audio or video) metadata, used when streaming files.
 *
 * <p>
 * All components may be {@code null}.
 *
 * @param title media title
 * @param artist media artist
 * @param album media album
 * @param artwork raw artwork data (JPEG)
 * @param duration media duration
 *
 * @author Dan Cunningham - Initial contribution
 */
public record MediaMetadata(String title, String artist, String album, byte[] artwork, Duration duration) {

    /**
     * Creates metadata containing only title, artist and album.
     *
     * @param title media title
     * @param artist media artist
     * @param album media album
     */
    public MediaMetadata(String title, String artist, String album) {
        this(title, artist, album, null, null);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MediaMetadata other && Objects.equals(title, other.title)
                && Objects.equals(artist, other.artist) && Objects.equals(album, other.album)
                && Arrays.equals(artwork, other.artwork) && Objects.equals(duration, other.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist, album, Arrays.hashCode(artwork), duration);
    }

    @Override
    public String toString() {
        return "MediaMetadata[title=" + title + ", artist=" + artist + ", album=" + album + ", artwork="
                + (artwork == null ? "none" : artwork.length + " bytes") + ", duration=" + duration + "]";
    }
}
