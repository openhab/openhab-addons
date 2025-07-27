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
package org.openhab.binding.teslapowerwall.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used to read the battery soe.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class MeterAggregates {
    @SerializedName("site")
    public @NonNullByDefault({}) MeterDetails siteMeterDetails;

    @SerializedName("battery")
    public @NonNullByDefault({}) MeterDetails batteryMeterDetails;

    @SerializedName("load")
    public @NonNullByDefault({}) MeterDetails loadMeterDetails;

    @SerializedName("solar")
    public @NonNullByDefault({}) MeterDetails solarMeterDetails;

    public class MeterDetails {
        @SerializedName("last_communication_time")
        public String lastCommunicationTime = "";

        @SerializedName("instant_power")
        public float instantPower;

        @SerializedName("instant_reactive_power")
        public float instantReactivePower;

        @SerializedName("instant_apparent_power")
        public float instantApparentPower;

        @SerializedName("frequency")
        public float frequency;

        @SerializedName("energy_exported")
        public float energyExported;

        @SerializedName("energy_imported")
        public float energyImported;

        @SerializedName("instant_average_voltage")
        public float instantAverageVoltage;

        @SerializedName("instant_average_current")
        public float instantAverageCurrent;

        @SerializedName("i_a_current")
        public float iaCurrent;

        @SerializedName("i_b_current")
        public float ibCurrent;

        @SerializedName("i_c_current")
        public float icCurrent;

        @SerializedName("last_phase_voltage_communication_time")
        public String lastPhaseVoltageCommunicationTime = "";

        @SerializedName("last_phase_power_communication_time")
        public String lastPhasePowerCommunicationTime = "";

        @SerializedName("last_phase_energy_communication_time")
        public String lastPhaseEnergyCommunicationTime = "";

        @SerializedName("timeout")
        public float timeout;

        @SerializedName("instant_total_current")
        public float instantTotalCurrent;
    }

    private MeterAggregates() {
    }
}
