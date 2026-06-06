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
package org.openhab.binding.atmofrance.internal.api.dto;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum PollenIndex {
    ABSENT(9, 0, "#dddddd"),
    VERY_LOW(0, 1, "#50f0e6"),
    LOW(1, 2, "#50ccaa"),
    MODERATE(2, 3, "#f0e641"),
    HIGH(3, 4, "#ff5050"),
    VERY_HIGH(4, 5, "#960032"),
    EXTREMELY_HIGH(5, 6, "#872181"),
    UNKNOWN(-1, -1, "#b3b3b3");

    public static final EnumSet<PollenIndex> AS_SET = EnumSet.allOf(PollenIndex.class);

    public final int value;
    private final int apiValue;
    public final String color;

    PollenIndex(int value, int apiValue, String color) {
        this.value = value;
        this.apiValue = apiValue;
        this.color = color;
    }

    public static PollenIndex valueOf(int apiValue) {
        for (PollenIndex index : AS_SET) {
            if (index.apiValue == apiValue) {
                return index;
            }
        }
        return UNKNOWN;
    }

    public static String stateColor(int stateValue) {
        for (PollenIndex index : AS_SET) {
            if (index.value == stateValue) {
                return index.color;
            }
        }
        return UNKNOWN.color;
    }
}
