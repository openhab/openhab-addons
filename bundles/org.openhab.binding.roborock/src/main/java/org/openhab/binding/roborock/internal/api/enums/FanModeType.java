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
package org.openhab.binding.roborock.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Container class for Fan Mode enums related to Roborock Vacuums
 *
 * @author Paul Smedley - Initial contribution
 *
 */
@NonNullByDefault
public enum FanModeType {
    SILENT(38, "Silent"),
    STANDARD(60, "Standard"),
    TURBO(75, "Turbo"),
    POWER(77, "Power"),
    FULL(90, "Full"),
    QUIET(101, "Quiet"),
    BALANCED(102, "Balanced"),
    TURBO2(103, "Turbo"),
    MAX(104, "Max"),
    OFF(105, "Off"),
    MAX_PLUS(108, "MaxPlus"),
    CUSTOM(-1, "Custom");

    private final int id;
    private final String description;

    FanModeType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static FanModeType getType(int value) {
        for (FanModeType m : FanModeType.values()) {
            if (m.getId() == value) {
                return m;
            }
        }

        // Default to unknown
        return CUSTOM;
    }

    public String getDescription() {
        return description;
    }
}
