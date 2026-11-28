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
 * All supported device states.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum DeviceState {
    /**
     * Device is idling, i.e. nothing is playing or about to play.
     */
    Idle(0),

    /**
     * Media is being loaded but not yet playing.
     */
    Loading(1),

    /**
     * Media is in paused state.
     */
    Paused(2),

    /**
     * Media is playing.
     */
    Playing(3),

    /**
     * Media is stopped.
     */
    Stopped(4),

    /**
     * Media is seeking, e.g fast forward.
     */
    Seeking(5);

    private final int value;

    DeviceState(int value) {
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
    public static DeviceState fromValue(int value) {
        for (DeviceState v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown DeviceState value: " + value);
    }
}
