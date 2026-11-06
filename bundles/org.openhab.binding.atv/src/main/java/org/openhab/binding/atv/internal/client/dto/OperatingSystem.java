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
 * Operating system on device.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum OperatingSystem {
    /**
     * Operating system is not known.
     */
    Unknown(0),

    /**
     * Operating system is Apple TV Software (pre-tvOS).
     */
    Legacy(1),

    /**
     * Operating system is tvOS.
     */
    TvOS(2),

    /**
     * Operating system is AirPortOS, used by AirPort Express devices. This is not an official
     * name; no official name is published for it.
     */
    AirPortOS(3),

    /**
     * Operating system is macOS.
     */
    MacOS(4);

    private final int value;

    OperatingSystem(int value) {
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
    public static OperatingSystem fromValue(int value) {
        for (OperatingSystem v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown OperatingSystem value: " + value);
    }
}
