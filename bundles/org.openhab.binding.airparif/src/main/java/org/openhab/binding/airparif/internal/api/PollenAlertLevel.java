/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum PollenAlertLevel {
    @SerializedName("0")
    NONE(0),
    @SerializedName("1")
    LOW(1),
    @SerializedName("2")
    AVERAGE(2),
    @SerializedName("3")
    HIGH(3),
    UNKNOWN(-1);

    public static final EnumSet<PollenAlertLevel> AS_SET = EnumSet.allOf(PollenAlertLevel.class);

    public final int riskLevel;

    PollenAlertLevel(int riskLevel) {
        this.riskLevel = riskLevel;
    }
}
