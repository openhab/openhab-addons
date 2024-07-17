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
public class ClimateState {
    // climate_state
    @SerializedName("is_auto_conditioning_on")
    public int isAutoConditioningOn;

    @SerializedName("is_climate_on")
    public int isClimateOn;

    @SerializedName("is_front_defroster_on")
    public int isFrontDefrosterOn;

    @SerializedName("is_preconditioning")
    public int isPreconditioning;

    @SerializedName("is_rear_defroster_on")
    public int isRearDefrosterOn;

    @SerializedName("seat_heater_left")
    public int seatHeaterLeft;

    @SerializedName("seat_heater_rear_center")
    public int seatHeaterRearCenter;

    @SerializedName("seat_heater_rear_left")
    public int seatHeaterRearLeft;

    @SerializedName("seat_heater_rear_right")
    public int seatHeaterRearRight;

    @SerializedName("seat_heater_right")
    public int seatHeaterRight;

    @SerializedName("side_mirror_heaters")
    public int sideMirrorHeaters;

    @SerializedName("smart_preconditioning")
    public int smartPreconditioning;

    @SerializedName("steering_wheel_heater")
    public int steeringWheelHeater;

    @SerializedName("wiper_blade_heater")
    public int wiperBladeHeater;

    @SerializedName("driver_temp_setting")
    public float driverTempSetting;

    @SerializedName("inside_temp")
    public float insideTemp;

    @SerializedName("outside_temp")
    public float outsideTemp;

    @SerializedName("passenger_temp_setting")
    public float passengerTempSetting;

    @SerializedName("fan_status")
    public float fanStatus;

    @SerializedName("left_temp_direction")
    public int leftTempDirection;

    @SerializedName("max_avail_temp")
    public float maxAvailTemp;

    @SerializedName("min_avail_temp")
    public float minAvailTemp;

    @SerializedName("right_temp_direction")
    public int rightTempDirection;

    private ClimateState() {
    }
}
