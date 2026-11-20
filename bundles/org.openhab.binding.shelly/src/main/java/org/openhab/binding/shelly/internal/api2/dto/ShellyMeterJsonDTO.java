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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2StatusJsonDTO.Shelly2DeviceStatusTemp;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyMeterJsonDTO} contains the JSON structures of the EM, EM1, PM1 and voltmeter components.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyMeterJsonDTO {

    public static class Shelly2DevConfigVoltmeter {
        public Integer id;
        public String name;
    }

    public static class Shelly2DevConfigEm {
        public Integer id;
        public String name;
        @SerializedName("blink_mode_selector")
        public String blinkModeSelector;
        @SerializedName("phase_selector")
        public String phaseSelector;
        @SerializedName("monitor_phase_sequence")
        public Boolean monitorPhaseSequence;
    }

    public static class Shelly2DevConfigPm1 {
        public Integer id;
        public String name;
    }

    public static class Shelly2DeviceStatusVoltage {
        public @Nullable Integer id;
        public @Nullable Double voltage;
        public @Nullable ArrayList<String> errors;
    }

    public static class Shelly2DeviceStatusEm {
        public @Nullable Integer id;

        @SerializedName("a_current")
        public @Nullable Double aCurrent;
        @SerializedName("a_voltage")
        public @Nullable Double aVoltage;
        @SerializedName("a_act_power")
        public @Nullable Double aActPower;
        @SerializedName("a_aprt_power")
        public @Nullable Double aAprtPower;
        @SerializedName("a_pf")
        public @Nullable Double aPF;

        @SerializedName("b_current")
        public @Nullable Double bCurrent;
        @SerializedName("b_voltage")
        public @Nullable Double bVoltage;
        @SerializedName("b_act_power")
        public @Nullable Double bActPower;
        @SerializedName("b_aprt_power")
        public @Nullable Double bAprtPower;
        @SerializedName("b_pf")
        public @Nullable Double bPF;

        @SerializedName("c_current")
        public @Nullable Double cCurrent;
        @SerializedName("c_voltage")
        public @Nullable Double cVoltage;
        @SerializedName("c_act_power")
        public @Nullable Double cActPower;
        @SerializedName("c_aprt_power")
        public @Nullable Double cAprtPower;
        @SerializedName("c_pf")
        public @Nullable Double cPF;

        @SerializedName("n_current")
        public @Nullable Double nCurrent;

        @SerializedName("total_current")
        public @Nullable Double totalCurrent;
        @SerializedName("total_act_power")
        public @Nullable Double totalActPower;
        @SerializedName("total_aprt_power")
        public @Nullable Double totalAprtPower;

        @SerializedName("a_freq")
        public @Nullable Double aFreq;
        @SerializedName("b_freq")
        public @Nullable Double bFreq;
        @SerializedName("c_freq")
        public @Nullable Double cFreq;
    }

    public static class Shelly2DeviceStatusEmData {
        public @Nullable Integer id;

        @SerializedName("a_total_act_energy")
        public @Nullable Double totalActiveEnergyA; // Total active energy on phase A, Wh
        @SerializedName("b_total_act_energy")
        public @Nullable Double totalActiveEnergyB; // Total active energy on phase B, Wh
        @SerializedName("c_total_act_energy")
        public @Nullable Double totalActiveEnergyC; // Total active energy on phase C, Wh

        @SerializedName("a_total_act_ret_energy")
        public @Nullable Double totalActiveReturnedEnergyA; // Total returned energy on phase A, Wh
        @SerializedName("b_total_act_ret_energy")
        public @Nullable Double totalActiveReturnedEnergyB; // Total returned energy on phase B, Wh
        @SerializedName("c_total_act_ret_energy")
        public @Nullable Double totalActiveReturnedEnergyC; // Total returned energy on phase C, Wh

        @SerializedName("total_act")
        public @Nullable Double totalActiveEnergySum; // Total active energy over all phases, Wh (EMData)
        @SerializedName("total_act_energy")
        public @Nullable Double totalActiveEnergy; // Total active energy of the clamp, Wh (EM1Data)
        @SerializedName("total_act_ret")
        public @Nullable Double totalActiveReturnedEnergySum; // Total returned energy over all phases, Wh
        @SerializedName("total_act_ret_energy")
        public @Nullable Double totalActiveReturnedEnergy; // Total returned energy of the clamp, Wh (EM1Data)
        public String @Nullable [] errors;

        /**
         * Active energy of a single-phase payload, Wh: EMData reports phase A under
         * a_total_act_energy, EM1Data reports the clamp total under total_act_energy.
         */
        public @Nullable Double getTotalActiveEnergy() {
            return totalActiveEnergyA != null ? totalActiveEnergyA : totalActiveEnergy;
        }

        /**
         * Returned energy of a single-phase payload, Wh — same fallback as {@link #getTotalActiveEnergy()}.
         */
        public @Nullable Double getTotalActiveReturnedEnergy() {
            return totalActiveReturnedEnergyA != null ? totalActiveReturnedEnergyA : totalActiveReturnedEnergy;
        }
    }

    public static class Shelly2Pm1Status {
        public Integer id;
        public String source;
        public Boolean output;
        @SerializedName("timer_started_at")
        public Double timerStartetAt;
        @SerializedName("timer_duration")
        public Double timerDuration;
        public @Nullable Double apower;
        public @Nullable Double voltage;
        public @Nullable Double current;
        public @Nullable Double pf;
        public @Nullable Shelly2Energy aenergy;
        public @Nullable Shelly2DeviceStatusTemp temperature;
        public String @Nullable [] errors;
    }

    public static class Shelly2StatusEm1 {
        public @Nullable Integer id;
        public @Nullable Double current;
        public @Nullable Double voltage;
        @SerializedName("act_power")
        public @Nullable Double actPower;
        @SerializedName("aprt_power")
        public @Nullable Double aptrPower;
        public @Nullable Double pf;
        public @Nullable Double freq;
        public @Nullable String calibration;
        public @Nullable ArrayList<String> errors;
    }

    public static class Shelly2Energy {
        // "switch:1":{"id":1,"aenergy":{"total":0.003,"by_minute":[0.000,0.000,0.000],"minute_ts":1619910239}}}}
        public @Nullable Double total;
        @SerializedName("by_minute")
        public Double @Nullable [] byMinute;
        @SerializedName("minute_ts")
        public @Nullable Long minuteTs;
    }
}
