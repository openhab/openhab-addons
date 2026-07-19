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

/**
 * All supported media types.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum MediaType {
    /**
     * Media type is not known.
     *
     * This can be either the case that nothing is playing or the app does
     * not report a valid media type.
     */
    Unknown(0),

    /**
     * Media type is video.
     */
    Video(1),

    /**
     * Media type is music.
     */
    Music(2),

    /**
     * Media type is a TV show.
     */
    TV(3);

    private final int value;

    MediaType(int value) {
        this.value = value;
    }

    /**
     * Returns the stored numeric value.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static MediaType fromValue(int value) {
        for (MediaType v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown MediaType value: " + value);
    }
}
