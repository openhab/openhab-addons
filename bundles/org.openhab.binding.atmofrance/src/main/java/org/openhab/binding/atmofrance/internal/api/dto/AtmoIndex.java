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

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum AtmoIndex {
    @SerializedName("0")
    ABSENT(9, "#dddddd"),
    @SerializedName("1")
    GOOD(0, "#50f0e6"),
    @SerializedName("2")
    AVERAGE(1, "#50ccaa"),
    @SerializedName("3")
    DEGRADED(2, "#f0e641"),
    @SerializedName("4")
    BAD(3, "#ff5050"),
    @SerializedName("5")
    VERY_BAD(4, "#960032"),
    @SerializedName("6")
    EXTREMELY_BAD(5, "#872181"),
    @SerializedName("7")
    EVENT(7, "#888888"),
    UNKNOWN(-1, "#b3b3b3");

    public static final EnumSet<AtmoIndex> AS_SET = EnumSet.allOf(AtmoIndex.class);

    public final int value;
    public final String color;

    AtmoIndex(int value, String color) {
        this.value = value;
        this.color = color;
    }

    public static AtmoIndex valueOf(int ordinal) {
        for (AtmoIndex index : AS_SET) {
            if (index.value == ordinal) {
                return index;
            }
        }
        return UNKNOWN;
    }
}
