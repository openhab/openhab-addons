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
package org.openhab.binding.shelly.internal.api2.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyThermostatJsonDTO} includes constants and structures used for the Wall Display Thermostat
 * component's JSON mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyThermostatJsonDTO {

    public static class Shelly2DeviceStatusThermostat {
        public static class Shelly2DeviceStatusThermostatSchedules {
            public @Nullable Boolean enable;
        }

        public @Nullable Boolean enable;
        @SerializedName("target_C")
        public @Nullable Double targetC;
        @SerializedName("current_C")
        public @Nullable Double currentC;
        public @Nullable Boolean output;
        public @Nullable Shelly2DeviceStatusThermostatSchedules schedules;
    }
}
