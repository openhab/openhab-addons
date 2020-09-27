/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * The {@link VehicleAttributes} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleAttributes {
    // Windows & Doors
    public String door_driver_front;// "CLOSED",
    public String door_driver_rear;// "CLOSED",
    public String door_passenger_front;// "CLOSED",
    public String door_passenger_rear;// "CLOSED",
    public String hood_state;// "CLOSED",
    public String trunk_state;// "CLOSED",
    public String window_driver_front;// "CLOSED",
    public String window_driver_rear;// "CLOSED",
    public String window_passenger_front;// "CLOSED",
    public String window_passenger_rear;// "CLOSED",
    public String sunroof_state;// "CLOSED",
    public String door_lock_state;// "SECURED",
    public String shdStatusUnified;// "CLOSED",

    // Charge Status
    public String chargingHVStatus;// "INVALID",
    public String lastChargingEndReason;// "CHARGING_GOAL_REACHED",
    public String connectorStatus;// "DISCONNECTED",
    public String chargingLogicCurrentlyActive;// "NOT_CHARGING",
    public String chargeNowAllowed;// "NOT_ALLOWED",
    public String charging_status;// "NOCHARGING",
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
    public double kombi_current_remaining_range_fuel;// "67.0",

    public double chargingLevelHv;// "89.0",
    public double soc_hv_percent;// "82.6",
    public double remaining_fuel;// "4",
    public double fuelPercent;// "47",

    // Last Status update
    public String updateTime;// "22.08.2020 12:55:46 UTC",
    public String updateTime_converted;// "22.08.2020 13:55",
    public String updateTime_converted_date;// "22.08.2020",
    public String updateTime_converted_time;// "13:55",
    // Last Trip Update
    public String Segment_LastTrip_time_segment_end_formatted_date;// "22.08.2020",
    public String Segment_LastTrip_time_segment_end_formatted;// "22.08.2020 14:52",
    public String Segment_LastTrip_time_segment_end_formatted_time;// "14:52",
    public String Segment_LastTrip_time_segment_end;// "22.08.2020 14:52:00 UTC",

    // Location
    public float gps_lat;// "50.556164",
    public float gps_lng;// "8.495482",
    public int heading;// "41",

    public String unitOfLength;// "km",
    public String unitOfEnergy;// "kWh",
    public String vehicle_tracking;// "1",
    public String head_unit_pu_software;// "07/16",
    public String check_control_messages;// "",
    public String sunroof_position;// "0",
    public String single_immediate_charging;// "isUnused",
    public String unitOfCombustionConsumption;// "l/100km",
    public String Segment_LastTrip_ratio_electric_driven_distance;// "100",
    public String condition_based_services;// "00003,OK,2021-11,;00017,OK,2021-11,;00001,OK,2021-11,;00032,OK,2021-11,",
    public String updateTime_converted_timestamp;// "1598104546000",
    public String charging_inductive_positioning;// "not_positioned",
    public String lsc_trigger;// "VEHCSHUTDOWN_SECURED",
    public String lights_parking;// "OFF",
    public String prognosisWhileChargingStatus;// "NOT_NEEDED",
    public String head_unit;// "EntryNav",
    public String battery_size_max;// "33200",
    public String charging_connection_type;// "CONDUCTIVE",
    public String unitOfElectricConsumption;// "kWh/100km",
}
