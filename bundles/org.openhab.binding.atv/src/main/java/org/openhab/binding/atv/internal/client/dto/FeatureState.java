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
 * State of a particular feature.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum FeatureState {
    /**
     * Feature is supported by device but it is not known if it is available or not.
     */
    Unknown(0),

    /**
     * Device does not support this feature.
     */
    Unsupported(1),

    /**
     * Feature is supported by device but not available now.
     *
     * Pause is for instance unavailable if nothing is playing.
     */
    Unavailable(2),

    /**
     * Feature is supported and available.
     */
    Available(3);

    private final int value;

    FeatureState(int value) {
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
    public static FeatureState fromValue(int value) {
        for (FeatureState v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown FeatureState value: " + value);
    }
}
