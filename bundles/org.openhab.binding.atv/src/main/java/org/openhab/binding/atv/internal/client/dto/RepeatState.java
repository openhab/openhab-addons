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
 * All supported repeat states.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum RepeatState {
    /**
     * Repeat is off.
     */
    Off(0),

    /**
     * Repeat current track or item.
     */
    Track(1),

    /**
     * Repeat all tracks or items.
     */
    All(2);

    private final int value;

    RepeatState(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric value stored for this constant.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static RepeatState fromValue(int value) {
        for (RepeatState v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown RepeatState value: " + value);
    }
}
