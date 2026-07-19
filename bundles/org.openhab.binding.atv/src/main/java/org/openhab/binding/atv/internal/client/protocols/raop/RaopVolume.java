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
package org.openhab.binding.atv.internal.client.protocols.raop;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Volume level conversions between percentage and dBFS used by AirPlay/RAOP.
 *
 * <p>
 * AirPlay uses {@code -144.0} dB as the muted volume.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopVolume {

    /** dBFS value AirPlay uses for a muted receiver. */
    public static final double DBFS_MUTE = -144.0;
    /** Lowest non-muted dBFS level. */
    public static final double DBFS_MIN = -30.0;
    /** Highest dBFS level. */
    public static final double DBFS_MAX = 0.0;
    /** Lowest percentage level. */
    public static final double PERCENTAGE_MIN = 0.0;
    /** Highest percentage level. */
    public static final double PERCENTAGE_MAX = 100.0;

    private RaopVolume() {
    }

    /**
     * Converts a percentage level (0.0-100.0) to dBFS.
     *
     * <p>
     * AirPlay uses -144.0 as muted volume, so 0.0 is re-mapped to that.
     *
     * @throws IllegalArgumentException if the level is out of range
     */
    public static double pctToDbfs(double level) {
        // Only an exact 0.0 is treated as the muted level.
        if (level == 0.0) {
            return DBFS_MUTE;
        }
        return mapRange(level, PERCENTAGE_MIN, PERCENTAGE_MAX, DBFS_MIN, DBFS_MAX);
    }

    /**
     * Converts a dBFS level to a percentage (0.0-100.0).
     *
     * <p>
     * AirPlay uses -144.0 as "muted", but everything below -30.0 is treated as muted
     * to be a bit defensive.
     *
     * @throws IllegalArgumentException if the level is above {@link #DBFS_MAX}
     */
    public static double dbfsToPct(double level) {
        if (level < DBFS_MIN) {
            return PERCENTAGE_MIN;
        }
        return mapRange(level, DBFS_MIN, DBFS_MAX, PERCENTAGE_MIN, PERCENTAGE_MAX);
    }

    /**
     * Maps a value in one range to another.
     *
     * @throws IllegalArgumentException if a range is invalid or the value is outside the
     *             input range
     */
    public static double mapRange(double value, double inMin, double inMax, double outMin, double outMax) {
        if (inMax - inMin <= 0.0) {
            throw new IllegalArgumentException("invalid input range");
        }
        if (outMax - outMin <= 0.0) {
            throw new IllegalArgumentException("invalid output range");
        }
        if (value < inMin || value > inMax) {
            throw new IllegalArgumentException("input value out of range");
        }
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
