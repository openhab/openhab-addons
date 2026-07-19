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

import java.util.Arrays;
import java.util.Objects;

/**
 * Artwork information for currently playing media.
 *
 * @param bytes raw artwork data
 * @param mimetype MIME type of the artwork, e.g. {@code image/jpeg}
 * @param width width of artwork in pixels (or a negative value if unknown)
 * @param height height of artwork in pixels (or a negative value if unknown)
 *
 * @author Dan Cunningham - Initial contribution
 */
public record ArtworkInfo(byte[] bytes, String mimetype, int width, int height) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArtworkInfo other && Arrays.equals(bytes, other.bytes)
                && Objects.equals(mimetype, other.mimetype) && width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes), mimetype, width, height);
    }

    @Override
    public String toString() {
        return "ArtworkInfo[size=" + bytes.length + ", mimetype=" + mimetype + ", width=" + width + ", height=" + height
                + "]";
    }
}
