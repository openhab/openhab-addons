/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoundationPreset} represents preset bed positions for a bed side.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum FoundationPreset {
    NOT_AT_PRESET(0),
    FAVORITE(1),
    READ(2),
    WATCH_TV(3),
    FLAT(4),
    ZERO_G(5),
    SNORE(6);

    private final int preset;

    FoundationPreset(final int preset) {
        this.preset = preset;
    }

    public int value() {
        return preset;
    }

    public static FoundationPreset forValue(int value) {
        for (FoundationPreset s : FoundationPreset.values()) {
            if (s.preset == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid preset: " + value);
    }

    public static FoundationPreset convertFromStatus(String currentPositionPreset) {
        FoundationPreset preset;
        switch (currentPositionPreset.toLowerCase()) {
            case "not at preset":
                preset = FoundationPreset.NOT_AT_PRESET;
                break;
            case "favorite":
                preset = FoundationPreset.FAVORITE;
                break;
            case "read":
                preset = FoundationPreset.READ;
                break;
            case "watch tv":
                preset = FoundationPreset.WATCH_TV;
                break;
            case "flat":
                preset = FoundationPreset.FLAT;
                break;
            case "zero g":
                preset = FoundationPreset.ZERO_G;
                break;
            case "snore":
                preset = FoundationPreset.SNORE;
                break;
            default:
                throw new IllegalArgumentException("Unknown preset value: " + currentPositionPreset);
        }
        return preset;
    }

    @Override
    public String toString() {
        return String.valueOf(preset);
    }
}
