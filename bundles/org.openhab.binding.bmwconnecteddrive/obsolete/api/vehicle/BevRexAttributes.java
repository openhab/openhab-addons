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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import java.util.List;

/**
 * The {@link BevRexAttributes} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class BevRexAttributes {
    public String unitOfLength;
    public float mileage; // "17236",
    public float beMaxRangeElectricKm; // "209.0",
    public float beRemainingRangeElectric; // "179.0",
    public float beRemainingRangeElectricKm; // "179.0",
    public float beRemainingRangeFuelMile; // "41.0",
    public float beRemainingRangeFuel; // "67.0",
    public float beMaxRangeElectricMile; // "129.0",
    public float beRemainingRangeElectricMile; // "111.0",
    public float beRemainingRangeFuelKm; // "67.0",
    public float kombi_current_remaining_range_fuel; // "67.0",
    public float soc_hv_percent; // "82.6",
    public float chargingLevelHv; // "89.0",
    public float fuelPercent; // "47",
    public float remaining_fuel; // "4",

    public String sunroof_state; // : "CLOSED"
    public String chargingLogicCurrentlyActive;// ; // "NOT_CHARGING",
    public int vehicle_tracking; // ; // "1",
    public String updateTime_converted; // "22.08.2020 13:55",
    public String door_driver_rear; // "CLOSED",
    public String head_unit_pu_software; // "07/16",
    public String door_passenger_rear; // "CLOSED",
    public String door_driver_front; // "CLOSED",
    public String shdStatusUnified; // "CLOSED",
    public String hood_state; // "CLOSED",
    public String charging_status; // "NOCHARGING",
    public String beMaxRangeElectric; // "209.0",
    public String window_driver_rear; // "CLOSED",
    public String lastChargingEndResult; // "SUCCESS",
    public String check_control_messages; // "",
    public String unitOfEnergy; // "kWh",
    public int sunroof_position; // "0",
    public String single_immediate_charging; // "isUnused",
    public String updateTime_converted_time; // "13:55",
    public String chargingHVStatus; // "INVALID",
    public String connectorStatus; // "DISCONNECTED",
    public String chargingSystemStatus; // "NOCHARGING",
    public String unitOfCombustionConsumption; // "l/100km",
    public String window_driver_front; // "CLOSED",
    public String Segment_LastTrip_ratio_electric_driven_distance; // "100",
    public String condition_based_services; // "00003,OK,2021-11,;00017,OK,2021-11,;00001,OK,2021-11,;00032,OK,2021-11,",
    public String window_passenger_front; // "CLOSED",
    public String window_passenger_rear; // "CLOSED",
    public String lastChargingEndReason; // "CHARGING_GOAL_REACHED",
    public String updateTime_converted_date; // "22.08.2020",
    public String door_passenger_front; // "CLOSED",
    public String updateTime_converted_timestamp; // "1598104546000",
    public String charging_inductive_positioning; // "not_positioned",
    public String lsc_trigger; // "VEHCSHUTDOWN_SECURED",
    public String lights_parking; // "OFF",
    public String door_lock_state; // "SECURED",
    public String updateTime; // "22.08.2020 12:55:46 UTC",
    public String prognosisWhileChargingStatus; // "NOT_NEEDED",
    public String head_unit; // "EntryNav",
    public String trunk_state; // "CLOSED",
    public int battery_size_max; // "33200",
    public String charging_connection_type; // "CONDUCTIVE",
    public String unitOfElectricConsumption; // "kWh/100km",
    public String lastUpdateReason; // ": "VEHCSHUTDOWN_SECURED"
    public List<VehicleMessages> vehicleMessages;

    public float gps_lat; // "50.556164",
    public float gps_lng; // "8.495482",
    public float heading; // "41",

    public String Segment_LastTrip_time_segment_end_formatted; // "22.08.2020 14:52",
    public String Segment_LastTrip_time_segment_end; // "22.08.2020 14:52:00 UTC",
    public String Segment_LastTrip_time_segment_end_formatted_date; // "22.08.2020",
    public String Segment_LastTrip_time_segment_end_formatted_time; // "14:52",
}
