/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link LimitNotificationType} represents the types of limit notifications.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum LimitNotificationType {

    @SerializedName("lowTemp")
    LOW_TEMP("lowTemp"),

    @SerializedName("highTemp")
    HIGH_TEMP("highTemp"),

    @SerializedName("lowHumidity")
    LOW_HUMIDITY("lowHumidity"),

    @SerializedName("highHumidity")
    HIGH_HUMIDITY("highHumidity"),

    @SerializedName("auxHeat")
    AUX_HEAT("auxHeat"),

    @SerializedName("auxOutdoor")
    AUX_OUTDOOR("auxOutdoor");

    private final String mode;

    private LimitNotificationType(String mode) {
        this.mode = mode;
    }

    public String value() {
        return mode;
    }

    public static LimitNotificationType forValue(@Nullable String v) {
        if (v != null) {
            for (LimitNotificationType vm : LimitNotificationType.values()) {
                if (vm.mode.equals(v)) {
                    return vm;
                }
            }
        }
        throw new IllegalArgumentException("Invalid vent: " + v);
    }

    @Override
    public String toString() {
        return this.mode;
    }
}
