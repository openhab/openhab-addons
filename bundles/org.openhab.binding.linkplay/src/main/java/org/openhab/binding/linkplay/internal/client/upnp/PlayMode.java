/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Repeat/shuffle play modes reported by UPnP AVTransport events.
 * 
 * The API is inconsistent in the name it uses, hence the duplicate mapped values
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum PlayMode {
    NORMAL("-1"), // Off
    REPEAT_ONE("0"),
    REPEATONE("0"),
    REPEAT_ALL("1"),
    REPEATALL("1"),
    REPEAT("1"),
    SHUFFLE("2"),
    SHUFFLE_NOREPEAT("2"),
    RANDOM("2"),
    SHUFFLE_ALL("2");

    private final String mappedMode;

    PlayMode(String mappedMode) {
        this.mappedMode = mappedMode;
    }

    /**
     * Returns the string value expected by the binding channels for this play mode.
     */
    public String getMappedMode() {
        return mappedMode;
    }

    /**
     * Convert a string value from the device into a PlayMode enum.
     *
     * @param value Raw string value from AVTransport event
     * @return corresponding PlayMode or {@code null} if unknown
     */
    public static @Nullable PlayMode fromString(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return PlayMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
