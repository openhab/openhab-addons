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
public class SystemStatus {
    @SerializedName("command_source")
    public String commandSource = "";

    @SerializedName("battery_target_power")
    public float batteryTargetPower;

    @SerializedName("battery_target_reactive_power")
    public int batteryTargetReactivePower;

    @SerializedName("nominal_full_pack_energy")
    public int nominalFullPackEnergy;

    @SerializedName("nominal_energy_remaining")
    public int nominalEnergyRemaining;

    @SerializedName("max_power_energy_remaining")
    public int maxPowerEnergyRemaining;

    @SerializedName("max_power_energy_to_be_charged")
    public int maxPowerEnergyToBeCharged;

    @SerializedName("max_charge_power")
    public int maxChargePower;

    @SerializedName("max_discharge_power")
    public int maxDischargePower;

    @SerializedName("max_apparent_power")
    public int maxApparentPower;

    @SerializedName("instantaneous_max_discharge_power")
    public int instantaneousMaxDischargePower;

    @SerializedName("instantaneous_max_charge_power")
    public int instantaneousMaxChargePower;

    @SerializedName("instantaneous_max_apparent_power")
    public int instantaneousMaxApparentPower;

    @SerializedName("hardware_capability_charge_power")
    public int hardwareCapabilityChargePower;

    @SerializedName("hardware_capability_discharge_power")
    public int hardwareCapabilityDischargePower;

    @SerializedName("hardware_capability_adjusted_charge_power")
    public int hardwareCapabilityAdjustedChargePower;

    @SerializedName("grid_services_power")
    public float gridServicesPower;

    @SerializedName("system_island_state")
    public String systemIslandState = "";

    @SerializedName("available_blocks")
    public int availableBlocks;

    @SerializedName("available_charger_blocks")
    public int availableChargerBlocks;

    @SerializedName("battery_blocks")
    public @NonNullByDefault({}) BatteryBlocks[] batteryBlocks;

    @SerializedName("ffr_power_availability_high")
    public float ffrPowerAvailabilityHigh;

    @SerializedName("ffr_power_availability_low")
    public float ffrPowerAvailabilityLow;

    @SerializedName("load_charge_constraint")
    public int loadChargeConstraint;

    @SerializedName("max_sustained_ramp_rate")
    public int maxSustainedRampRate;

    @SerializedName("can_reboot")
    public String canReboot = "";

    @SerializedName("smart_inv_delta_p")
    public int smartInvDeltaP;

    @SerializedName("smart_inv_delta_q")
    public int smartInvDeltaQ;

    @SerializedName("last_toggle_timestamp")
    public String lastToggleTimestamp = "";

    @SerializedName("solar_real_power_limit")
    public float solarRealPowerLimit;

    @SerializedName("score")
    public int score;

    @SerializedName("blocks_controlled")
    public int blocksControlled;

    @SerializedName("primary")
    public boolean primary;

    @SerializedName("auxiliary_load")
    public int auxiliaryLoad;

    @SerializedName("all_enable_lines_high")
    public boolean allEnableLinesHigh;

    @SerializedName("inverter_nominal_usable_power")
    public int inverterNominalUsablePower;

    @SerializedName("system_available_charge_power_design_pf")
    public int systemAvailableChargePowerDesignPf;

    @SerializedName("system_available_discharge_power_design_pf")
    public int systemAvailableDischargePowerDesignPf;

    @SerializedName("system_available_charge_power_unity_pf")
    public int systemAvailableChargePowerUnityPf;

    @SerializedName("system_available_discharge_power_unity_pf")
    public int systemAvailableDischargePowerUnityPf;

    @SerializedName("system_charge_power_capability_design_pf")
    public int systemChargePowerCapabilityDesignPf;

    @SerializedName("system_discharge_power_capability_design_pf")
    public int systemDischargePowerCapabilityDesignPf;

    @SerializedName("system_charge_power_capability_unity_pf")
    public int systemChargePowerCapabilityUnityPf;

    @SerializedName("system_discharge_power_capability_unity_pf")
    public int systemDischargePowerCapabilityUnityPf;

    @SerializedName("system_adjusted_charge_power_capability_design_pf")
    public int systemAdjustedChargePowerCapabilityDesignPf;

    @SerializedName("system_adjusted_charge_power_capability_unity_pf")
    public int systemAdjustedChargePowerCapabilityUnityPf;

    @SerializedName("expected_energy_remaining")
    public int expectedEnergyRemaining;

    public class BatteryBlocks {
        @SerializedName("Type")
        public String type = "";

        @SerializedName("PackagePartNumber")
        public String packagePartNumber = "";

        @SerializedName("PackageSerialNumber")
        public String packageSerialNumber = "";

        @SerializedName("pinv_state")
        public String pinvState = "";

        @SerializedName("pinv_grid_state")
        public String pinvGridState = "";

        @SerializedName("nominal_energy_remaining")
        public float nominalEnergyRemaining;

        @SerializedName("nominal_full_pack_energy")
        public float nominalFullPackEnergy;

        @SerializedName("p_out")
        public float pOut;

        @SerializedName("q_out")
        public float qOut;

        @SerializedName("v_out")
        public float vOut;

        @SerializedName("f_out")
        public float fOut;

        @SerializedName("i_out")
        public float iOut;

        @SerializedName("energy_charged")
        public int energyCharged;

        @SerializedName("energy_discharged")
        public int energyDischarged;

        @SerializedName("off_grid")
        public boolean offGrid;

        @SerializedName("vf_mode")
        public boolean vfMode;

        @SerializedName("wobble_detected")
        public boolean wobbleDetected;

        @SerializedName("charge_power_clamped")
        public boolean chargePowerClamped;

        @SerializedName("backup_ready")
        public boolean backupReady;

        @SerializedName("OpSeqState")
        public String opSeqState = "";

        @SerializedName("version")
        public String version = "";
    }

    private SystemStatus() {
    }
}
