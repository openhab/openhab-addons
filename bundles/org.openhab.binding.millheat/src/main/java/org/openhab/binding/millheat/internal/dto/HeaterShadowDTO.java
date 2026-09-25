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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The heater half of an AWS IoT Thing shadow state, shared by the {@code Heaters},
 * {@code Sockets} and {@code Floor Heaters} families. Only the fields the binding acts on are
 * mapped; the API reports roughly forty in total.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record HeaterShadowDTO(@SerializedName("operation_mode") @Nullable String operationMode,
        @SerializedName("temperature_normal") @Nullable Double temperatureNormal,
        @SerializedName("temperature_comfort") @Nullable Double temperatureComfort,
        @SerializedName("temperature_sleep") @Nullable Double temperatureSleep,
        @SerializedName("temperature_away") @Nullable Double temperatureAway,
        @SerializedName("temperature_vacation") @Nullable Double temperatureVacation,
        @SerializedName("fan_state") @Nullable String fanState,
        @SerializedName("lock_status") @Nullable String lockStatus,
        @SerializedName("predictive_heating_type") @Nullable String predictiveHeatingType,
        @SerializedName("regulator_type") @Nullable String regulatorType,
        @SerializedName("night_saving_mode_active") @Nullable Boolean nightSavingModeActive,
        @SerializedName("frost_protection_active") @Nullable Boolean frostProtectionActive,
        @SerializedName("max_heater_power") @Nullable Double maxHeaterPower,
        @SerializedName("display_unit") @Nullable String displayUnit) {

    /** Value of {@code operation_mode} that hands control of the device to the API caller. */
    public static final String MODE_CONTROL_INDIVIDUALLY = "control_individually";
    /** Value of {@code operation_mode} that switches the device off. */
    public static final String MODE_OFF = "off";
    /** Value of {@code operation_mode} that follows the room's weekly program. */
    public static final String MODE_WEEKLY_PROGRAM = "weekly_program";

    public boolean fanActive() {
        return "on".equals(fanState);
    }
}
