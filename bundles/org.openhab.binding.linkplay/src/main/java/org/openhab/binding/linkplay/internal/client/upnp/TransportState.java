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
 * Transport state reported by UPnP AVTransport events.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum TransportState {
    PLAYING,
    PAUSED_PLAYBACK,
    STOPPED,
    TRANSITIONING;

    /**
     * Convert a string value from the device into a TransportState enum.
     *
     * @param value String value returned in the AVTransport event
     * @return corresponding TransportState or {@code null} if unknown
     */
    public static @Nullable TransportState fromString(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return TransportState.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
