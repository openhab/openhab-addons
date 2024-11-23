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
package org.openhab.binding.ecobee.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EquipmentNotificationType} represents the types of equipment notifications.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum EquipmentNotificationType {

    @SerializedName("hvac")
    HVAC("hvac"),

    @SerializedName("furnaceFilter")
    FURNACE_FILTER("furnaceFilter"),

    @SerializedName("humidifierFilter")
    HUMIDIFIER_FILTER("humidifierFilter"),

    @SerializedName("dehumidifierFilter")
    DEHUNIDIFIER_FILTER("dehumidifierFilter"),

    @SerializedName("ventilator")
    VENTILATOR("ventilator"),

    @SerializedName("ac")
    AC("ac"),

    @SerializedName("airFilter")
    AIR_FILTER("airFilter"),

    @SerializedName("airCleaner")
    AIR_CLEANER("airCleaner"),

    @SerializedName("uvLamp")
    UV_LAMP("uvLamp");

    private final String mode;

    private EquipmentNotificationType(String mode) {
        this.mode = mode;
    }

    public String value() {
        return mode;
    }

    public static EquipmentNotificationType forValue(@Nullable String v) {
        if (v != null) {
            for (EquipmentNotificationType vm : EquipmentNotificationType.values()) {
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
