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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ChargeState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ChargeState {
    @SerializedName("battery_heater_on")
    public boolean batteryHeaterOn;
    @SerializedName("charge_enable_request")
    public boolean chargeEnableRequest;
    @SerializedName("charge_port_door_open")
    public boolean chargePortDoorOpen;
    @SerializedName("charge_to_max_range")
    public boolean chargeToMaxRange;
    @SerializedName("eu_vehicle")
    public boolean euVehicle;
    @SerializedName("fast_charger_present")
    public boolean fastChargerPresent;
    @SerializedName("managed_charging_active")
    public boolean managedChargingActive;
    @SerializedName("managed_charging_user_canceled")
    public boolean managedChargingUserCanceled;
    @SerializedName("motorized_charge_port")
    public boolean motorizedChargePort;
    @SerializedName("not_enough_power_to_heat")
    public boolean notEnoughPowerToHeat;
    @SerializedName("scheduled_charging_pending")
    public boolean scheduledChargingPending;
    @SerializedName("trip_charging")
    public boolean tripCharging;
    @SerializedName("battery_current")
    public float batteryCurrent;
    @SerializedName("battery_range")
    public float batteryRange;
    @SerializedName("charge_energy_added")
    public float chargeEnergyAdded;
    @SerializedName("charge_miles_added_ideal")
    public float chargeMilesAddedIdeal;
    @SerializedName("charge_miles_added_rated")
    public float chargeMilesAddedRated;
    @SerializedName("aaacharge_rateaa")
    public float chargeRate;
    @SerializedName("est_battery_range")
    public float estBatteryRange;
    @SerializedName("ideal_battery_range")
    public float idealBatteryRange;
    @SerializedName("time_to_full_charge")
    public float timeToFullCharge;
    @SerializedName("battery_level")
    public int batteryLevel;
    @SerializedName("charge_amps")
    public int chargeAmps;
    @SerializedName("charge_current_request")
    public int chargeCurrentRequest;
    @SerializedName("charge_current_request_max")
    public int chargeCurrentRequestMax;
    @SerializedName("charge_limit_soc")
    public int chargeLimitSoc;
    @SerializedName("charge_limit_soc_max")
    public int chargeLimitSocMax;
    @SerializedName("charge_limit_soc_min")
    public int chargeLimitSocMin;
    @SerializedName("charge_limit_soc_std")
    public int chargeLimitSocStd;
    @SerializedName("charger_actual_current")
    public int chargerActualCurrent;
    @SerializedName("charger_phases")
    public int chargerPhases;
    @SerializedName("charger_pilot_current")
    public int chargerPilotCurrent;
    @SerializedName("charger_power")
    public int chargerPower;
    @SerializedName("charger_voltage")
    public int chargerVoltage;
    @SerializedName("max_range_charge_counter")
    public int maxRangeChargeCounter;
    @SerializedName("usable_battery_level")
    public int usableBatteryLevel;
    @SerializedName("charge_port_latch")
    public String chargePortLatch;
    @SerializedName("charging_state")
    public String chargingState;
    @SerializedName("conn_charge_cable")
    public String connChargeCable;
    @SerializedName("fast_charger_brand")
    public String fastChargerBrand;
    @SerializedName("fast_charger_type")
    public String fastChargerType;
    @SerializedName("managed_charging_start_time")
    public String managedChargingStartTime;
    @SerializedName("scheduled_charging_start_time")
    public String scheduledChargingStartTime;
    @SerializedName("user_charge_enable_request")
    public String userChargeEnableRequest;

    ChargeState() {
    }
}
