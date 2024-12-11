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

import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleConfig} is a data structure to capture
 * vehicle configuration variables sent by the Tesla Vehicle
 *
 * @author Dan Cunningham - Initial contribution
 */
public class VehicleConfig {

    @SerializedName("can_accept_navigation_requests")
    public boolean canAcceptNavigationRequests;
    @SerializedName("can_actuate_trunks")
    public boolean canActuateTrunks;
    @SerializedName("car_special_type")
    public String carSpecialType;
    @SerializedName("car_type")
    public String carType;
    @SerializedName("charge_port_type")
    public String chargePortType;
    @SerializedName("ece_restrictions")
    public boolean eceRestrictions;
    @SerializedName("eu_vehicle")
    public boolean euVehicle;
    @SerializedName("exterior_color")
    public String exteriorColor;
    @SerializedName("has_air_suspension")
    public boolean hasAirSuspension;
    @SerializedName("has_ludicrous_mode")
    public boolean hasLudicrousMode;
    @SerializedName("motorized_charge_port")
    public boolean motorizedChargePort;
    @SerializedName("plg")
    public boolean plg;
    @SerializedName("rear_seat_heaters")
    public int rearSeatHeaters;
    @SerializedName("rear_seat_type")
    public int rearSeatType;
    @SerializedName("rhd")
    public boolean rhd;
    @SerializedName("roof_color")
    public String roofColor;
    @SerializedName("seat_type")
    public int seatType;
    @SerializedName("spoiler_type")
    public String spoilerType;
    @SerializedName("sun_roof_installed")
    public int sunRoofInstalled;
    @SerializedName("third_row_seats")
    public String thirdRowSeats;
    @SerializedName("timestamp")
    public Long timestamp;
    @SerializedName("trim_badging")
    public String trimBadging;
    @SerializedName("use_range_badging")
    public boolean useRangeBadging;
    @SerializedName("wheel_type")
    public String wheelType;

    public ThingTypeUID identifyModel() {
        switch (carType) {
            case "models":
            case "models2":
                return TeslaBindingConstants.THING_TYPE_MODELS;
            case "modelx":
                return TeslaBindingConstants.THING_TYPE_MODELX;
            case "model3":
                return TeslaBindingConstants.THING_TYPE_MODEL3;
            case "modely":
                return TeslaBindingConstants.THING_TYPE_MODELY;
            default:
                return TeslaBindingConstants.THING_TYPE_VEHICLE;
        }
    }
}
