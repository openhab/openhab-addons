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
 * Hardware device model.
 *
 * <p>
 * Gen2-Gen4K are Apple TV model names and will be renamed to AppleTVGenX in the future.
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum DeviceModel {
    /**
     * Device model is unknown.
     */
    Unknown(0),

    /**
     * Device model is second generation Apple TV (Apple TV 2).
     */
    Gen2(1),

    /**
     * Device model is third generation Apple TV (Apple TV 3).
     */
    Gen3(2),

    /**
     * Device model is fourth generation Apple TV (Apple TV 4).
     */
    Gen4(3),

    /**
     * Device model is fifth generation Apple TV (Apple TV 4K).
     */
    Gen4K(4),

    /**
     * Device model is HomePod (first generation).
     */
    HomePod(5),

    /**
     * Device model is HomePod Mini (first generation).
     */
    HomePodMini(6),

    /**
     * Device model is AirPort Express (first generation).
     */
    AirPortExpress(7),

    /**
     * Device model is AirPort Express (second generation).
     */
    AirPortExpressGen2(8),

    /**
     * Device model is sixth generation Apple TV (Apple TV 4K gen 2).
     */
    AppleTV4KGen2(9),

    /**
     * Music app (or iTunes) running on a desktop computer.
     */
    Music(10),

    /**
     * Device model is seventh generation Apple TV (Apple TV 4K gen 3).
     */
    AppleTV4KGen3(11),

    /**
     * Device model is HomePod (second generation).
     */
    HomePodGen2(12),

    /**
     * Device model is first generation Apple TV.
     */
    AppleTVGen1(13);

    private final int value;

    DeviceModel(int value) {
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
    public static DeviceModel fromValue(int value) {
        for (DeviceModel v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown DeviceModel value: " + value);
    }
}
