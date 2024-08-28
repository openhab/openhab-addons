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
package org.openhab.binding.teslascope.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class ChargeState {
    @SerializedName("battery_level")
    public int batteryLevel;

    @SerializedName("usable_battery_level")
    public int usableBatteryLevel;

    @SerializedName("battery_range")
    public float batteryRange;

    @SerializedName("est_battery_range")
    public float estBatteryRange;

    @SerializedName("charge_enable_request")
    public int chargeEnableRequest;

    @SerializedName("charge_energy_added")
    public float chargeEnergyAdded;

    @SerializedName("charge_limit_soc")
    public int chargeLimitSoc;

    @SerializedName("charge_port_door_open")
    public int chargePortDoorOpen;

    @SerializedName("charge_rate")
    public float chargeRate;

    @SerializedName("charger_power")
    public int chargerPower;

    @SerializedName("charger_voltage")
    public int chargerVoltage;

    @SerializedName("charging_state")
    public String chargingState = "";

    @SerializedName("time_to_full_charge")
    public float timeToFullCharge;

    @SerializedName("scheduled_charging_pending")
    public int scheduledChargingPending;

    @SerializedName("scheduled_charging_start_time")
    public String scheduledChargingStartTime = " ";

    private ChargeState() {
    }
}
