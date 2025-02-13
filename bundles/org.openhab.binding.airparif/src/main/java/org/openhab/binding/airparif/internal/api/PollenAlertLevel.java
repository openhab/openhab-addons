/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.api;

import java.util.EnumSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum PollenAlertLevel {
    @SerializedName("0")
    NONE(0, "#3a8b2f"),
    @SerializedName("1")
    LOW(1, "#f9a825"),
    @SerializedName("2")
    AVERAGE(2, "#ef6c00"),
    @SerializedName("3")
    HIGH(3, "#b71c1c"),
    UNKNOWN(-1, "#b3b3b3");

    public static final EnumSet<PollenAlertLevel> AS_SET = EnumSet.allOf(PollenAlertLevel.class);

    public final int riskLevel;
    public final String color;

    PollenAlertLevel(int riskLevel, String color) {
        this.riskLevel = riskLevel;
        this.color = color;
    }

    public static PollenAlertLevel valueOf(int ordinal) {
        return Objects
                .requireNonNull(AS_SET.stream().filter(pal -> pal.riskLevel == ordinal).findFirst().orElse(UNKNOWN));
    }
}
