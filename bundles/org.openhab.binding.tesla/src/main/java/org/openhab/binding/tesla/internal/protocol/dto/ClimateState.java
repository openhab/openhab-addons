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
 * The {@link ClimateState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ClimateState {

    @SerializedName("allow_cabin_overheat_protection")
    public boolean allowCabinOverheatProtection;
    @SerializedName("auto_seat_climate_left")
    public boolean autoSeatClimateLeft;
    @SerializedName("auto_seat_climate_right")
    public boolean autoSeatClimateRight;
    @SerializedName("battery_heater")
    public boolean batteryHeater;
    @SerializedName("battery_heater_no_power")
    public boolean batteryHeaterNoPower;
    @SerializedName("cabin_overheat_protection")
    public String cabinOverheatProtection;
    @SerializedName("cabin_overheat_protection_actively_cooling")
    public boolean cabinOverheatProtectionActivelyCooling;
    @SerializedName("climate_keeper_mode")
    public String climateKeeperMode;
    @SerializedName("cop_activation_temperature")
    public String copActivationTemperature;
    @SerializedName("defrost_mode")
    public int defrostMode;
    @SerializedName("driver_temp_setting")
    public float driverTempSetting;
    @SerializedName("fan_status")
    public int fanStatus;
    @SerializedName("hvac_auto_request")
    public String hvacAutoRequest;
    @SerializedName("inside_temp")
    public float insideTemp;
    @SerializedName("is_auto_conditioning_on")
    public boolean isAutoConditioningOn;
    @SerializedName("is_climate_on")
    public boolean isClimateOn;
    @SerializedName("is_front_defroster_on")
    public boolean isFrontDefrosterOn;
    @SerializedName("is_preconditioning")
    public boolean isPreconditioning;
    @SerializedName("is_rear_defroster_on")
    public boolean isRearDefrosterOn;
    @SerializedName("left_temp_direction")
    public int leftTempDirection;
    @SerializedName("max_avail_temp")
    public float maxAvailTemp;
    @SerializedName("min_avail_temp")
    public float minAvailTemp;
    @SerializedName("outside_temp")
    public float outsideTemp;
    @SerializedName("passenger_temp_setting")
    public float passengerTempSetting;
    @SerializedName("remote_heater_control_enabled")
    public boolean remoteHeaterControlEnabled;
    @SerializedName("right_temp_direction")
    public int rightTempDirection;
    @SerializedName("seat_heater_left")
    public int seatHeaterLeft;
    @SerializedName("seat_heater_right")
    public int seatHeaterRight;
    @SerializedName("side_mirror_heaters")
    public boolean sideMirrorHeaters;
    @SerializedName("supports_fan_only_cabin_overheat_protection")
    public boolean supportsFanOnlyCabinOverheatProtection;
    @SerializedName("timestamp")
    public long timestamp;
    @SerializedName("wiper_blade_heater")
    public boolean wiperBladeHeater;

    ClimateState() {
    }
}
