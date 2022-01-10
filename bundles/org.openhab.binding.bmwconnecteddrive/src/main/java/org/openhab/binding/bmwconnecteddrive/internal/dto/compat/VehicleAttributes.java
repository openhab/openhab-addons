/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.compat;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleAttributes} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleAttributes {
    // Windows & Doors
    @SerializedName("door_driver_front")
    public String doorDriverFront;// "CLOSED",
    @SerializedName("door_driver_rear")
    public String doorDriverRear;// "CLOSED",
    @SerializedName("door_passenger_front")
    public String doorPassengerFront;// "CLOSED",
    @SerializedName("door_passenger_rear")
    public String doorPassengerRear;// "CLOSED",
    @SerializedName("hood_state")
    public String hoodState;// "CLOSED",
    @SerializedName("trunk_state")
    public String trunkState;// "CLOSED",
    @SerializedName("window_driver_front")
    public String winDriverFront;// "CLOSED",
    @SerializedName("window_driver_rear")
    public String winDriverRear;// "CLOSED",
    @SerializedName("window_passenger_front")
    public String winPassengerFront;// "CLOSED",
    @SerializedName("window_passenger_rear")
    public String winPassengerRear;// "CLOSED",
    @SerializedName("sunroof_state")
    public String sunroofState;// "CLOSED",
    @SerializedName("door_lock_state")
    public String doorLockState;// "SECURED",
    public String shdStatusUnified;// "CLOSED",

    // Charge Status
    public String chargingHVStatus;// "INVALID",
    public String lastChargingEndReason;// "CHARGING_GOAL_REACHED",
    public String connectorStatus;// "DISCONNECTED",
    public String chargingLogicCurrentlyActive;// "NOT_CHARGING",
    public String chargeNowAllowed;// "NOT_ALLOWED",
    @SerializedName("charging_status")
    public String chargingStatus;// "NOCHARGING",
    public String lastChargingEndResult;// "SUCCESS",
    public String chargingSystemStatus;// "NOCHARGING",
    public String lastUpdateReason;// "VEHCSHUTDOWN_SECURED"

    // Range
    public int mileage;// "17236",
    public double beMaxRangeElectric;// "209.0",
    public double beMaxRangeElectricKm;// "209.0",
    public double beRemainingRangeElectric;// "179.0",
    public double beRemainingRangeElectricKm;// "179.0",
    public double beMaxRangeElectricMile;// "129.0",
    public double beRemainingRangeElectricMile;// "111.0",
    public double beRemainingRangeFuelKm;// "67.0",
    public double beRemainingRangeFuelMile;// "41.0",
    public double beRemainingRangeFuel;// "67.0",
    @SerializedName("kombi_current_remaining_range_fuel")
    public double kombiRemainingRangeFuel;// "67.0",

    public double chargingLevelHv;// "89.0",
    @SerializedName("soc_hv_percent")
    public double socHvPercent;// "82.6",
    @SerializedName("remaining_fuel")
    public double remainingFuel;// "4",
    public double fuelPercent;// "47",

    // Last Status update
    public String updateTime;// "22.08.2020 12:55:46 UTC",
    @SerializedName("updateTime_converted")
    public String updateTimeConverted;// "22.08.2020 13:55",
    @SerializedName("updateTime_converted_date")
    public String updateTimeConvertedDate;// "22.08.2020",
    @SerializedName("updateTime_converted_time")
    public String updateTimeConvertedTime;// "13:55",
    @SerializedName("updateTime_converted_timestamp")
    public String updateTimeConvertedTimestamp;// "1598104546000",

    // Last Trip Update
    @SerializedName("Segment_LastTrip_time_segment_end")
    public String lastTripEnd;// "22.08.2020 14:52:00 UTC",
    @SerializedName("Segment_LastTrip_time_segment_end_formatted")
    public String lastTripEndFormatted;// "22.08.2020 14:52",
    @SerializedName("Segment_LastTrip_time_segment_end_formatted_date")
    public String lastTripEndFormattedDate;// "22.08.2020",
    @SerializedName("Segment_LastTrip_time_segment_end_formatted_time")
    public String lastTripEndFormattedTime;// "14:52",

    // Location
    @SerializedName("gps_lat")
    public float gpsLat;// "43.21",
    @SerializedName("gps_lng")
    public float gpsLon;// "8.765",
    public int heading;// "41",

    public String unitOfLength;// "km",
    public String unitOfEnergy;// "kWh",
    @SerializedName("vehicle_tracking")
    public String vehicleTracking;// "1",
    @SerializedName("head_unit_pu_software")
    public String headunitSoftware;// "07/16",
    @SerializedName("check_control_messages")
    public String checkControlMessages;// "",
    @SerializedName("sunroof_position")
    public String sunroofPosition;// "0",
    @SerializedName("single_immediate_charging")
    public String singleImmediateCharging;// "isUnused",
    public String unitOfCombustionConsumption;// "l/100km",
    @SerializedName("Segment_LastTrip_ratio_electric_driven_distance")
    public String lastTripElectricRation;// "100",
    @SerializedName("condition_based_services")
    public String conditionBasedServices;// "00003,OK,2021-11,;00017,OK,2021-11,;00001,OK,2021-11,;00032,OK,2021-11,",
    @SerializedName("charging_inductive_positioning")
    public String chargingInductivePositioning;// "not_positioned",
    @SerializedName("lsc_trigger")
    public String lscTrigger;// "VEHCSHUTDOWN_SECURED",
    @SerializedName("lights_parking")
    public String lightsParking;// "OFF",
    public String prognosisWhileChargingStatus;// "NOT_NEEDED",
    @SerializedName("head_unit")
    public String headunit;// "EntryNav",
    @SerializedName("battery_size_max")
    public String batterySizeMax;// "33200",
    @SerializedName("charging_connection_type")
    public String chargingConnectionType;// "CONDUCTIVE",
    public String unitOfElectricConsumption;// "kWh/100km",
}
