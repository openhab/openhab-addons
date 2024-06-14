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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class DetailedInformation {
    public String vin = "";
    public String vehiclename = "";
    public String vehiclestate = "";
    public double odometer;

    // charge_state
    public int battery_level;
    public int usable_battery_level;
    public float battery_range;
    public float est_battery_range;
    public boolean charge_enable_request = false;
    public float charge_energy_added;
    public int charge_limit_soc;
    public boolean charge_port_door_open = false;
    public float charge_rate;
    public int charger_power;
    public int charger_voltage;
    public String chargingstate = "";
    public float time_to_full_charge;
    public boolean scheduled_charging_pending = false;
    public String scheduled_charging_start_time = " ";

    // climate_state
    public boolean is_auto_conditioning_on;
    public boolean is_climate_on;
    public boolean is_front_defroster_on;
    public boolean is_preconditioning;
    public boolean is_rear_defroster_on;
    public int seat_heater_left;
    public int seat_heater_rear_center;
    public int seat_heater_rear_left;
    public int seat_heater_rear_right;
    public int seat_heater_right;
    public boolean side_mirror_heaters;
    public boolean smart_preconditioning;
    public boolean steering_wheel_heater;
    public boolean wiper_blade_heater;
    public float driver_temp_setting;
    public float inside_temp;
    public float outside_temp;
    public float passenger_temp_setting;
    public float fan_status;
    public int left_temp_direction;
    public float max_avail_temp;
    public float min_avail_temp;
    public int right_temp_direction;

    // drive state
    public int heading;
    public float latitude;
    public float longitude;
    public String shift_state = "";
    public float power;
    public float speed = 0;

    // vehicle_state
    public boolean locked;
    public boolean sentry_mode;
    public boolean valet_mode;
    public String software_update_status = "";
    public String software_update_version = "";
    public boolean fd_window;
    public boolean fp_window;
    public boolean rd_window;
    public boolean rp_window;
    public String sun_roof_state = "";
    public int sun_roof_percent_open;
    public boolean homelink_nearby;
    public double tpms_pressure_fl;
    public double tpms_pressure_fr;
    public double tpms_pressure_rl;
    public double tpms_pressure_rr;
    public boolean df;
    public boolean dr;
    public boolean pf;
    public boolean pr;
    public boolean ft;
    public boolean rt;

    private DetailedInformation() {
    }

    public static DetailedInformation parse(String response) {
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject chargeStateJsonObject = jsonObject.get("charge_state").getAsJsonObject();
        JsonObject climateStateJsonObject = jsonObject.get("climate_state").getAsJsonObject();
        JsonObject driveStateJsonObject = jsonObject.get("drive_state").getAsJsonObject();
        JsonObject vehicleStateJsonObject = jsonObject.get("vehicle_state").getAsJsonObject();
        DetailedInformation detailedInformation = new DetailedInformation();

        detailedInformation.vin = jsonObject.get("vin").getAsString();
        detailedInformation.vehiclename = jsonObject.get("name").getAsString();
        detailedInformation.vehiclestate = jsonObject.get("state").getAsString();
        detailedInformation.odometer = jsonObject.get("odometer").getAsDouble();

        // data from chargeState
        detailedInformation.battery_level = chargeStateJsonObject.get("battery_level").getAsInt();
        detailedInformation.usable_battery_level = chargeStateJsonObject.get("usable_battery_level").getAsInt();
        detailedInformation.battery_range = chargeStateJsonObject.get("battery_range").getAsFloat();
        detailedInformation.est_battery_range = chargeStateJsonObject.get("est_battery_range").getAsFloat();
        detailedInformation.charge_enable_request = "1"
                .equals(chargeStateJsonObject.get("charge_enable_request").getAsString());
        detailedInformation.charge_energy_added = chargeStateJsonObject.get("charge_energy_added").getAsFloat();
        detailedInformation.charge_limit_soc = chargeStateJsonObject.get("charge_limit_soc").getAsInt();
        detailedInformation.charge_port_door_open = "1"
                .equals(chargeStateJsonObject.get("charge_port_door_open").getAsString());
        // if car is not charging, these might be NULL
        if (!chargeStateJsonObject.get("charge_rate").isJsonNull()) {
            detailedInformation.charge_rate = chargeStateJsonObject.get("charge_rate").getAsFloat();
            detailedInformation.charger_power = chargeStateJsonObject.get("charger_power").getAsInt();
        } else {
            detailedInformation.charge_rate = 0;
            detailedInformation.charger_power = 0;
        }
        detailedInformation.charger_voltage = chargeStateJsonObject.get("charger_voltage").getAsInt();
        detailedInformation.chargingstate = chargeStateJsonObject.get("charging_state").getAsString();
        detailedInformation.time_to_full_charge = chargeStateJsonObject.get("time_to_full_charge").getAsFloat();
        detailedInformation.scheduled_charging_pending = "1"
                .equals(chargeStateJsonObject.get("scheduled_charging_pending").getAsString());
        // if car has no scheduled charge, these might be NULL
        if (!chargeStateJsonObject.get("scheduled_charging_start_time").isJsonNull()) {
            detailedInformation.scheduled_charging_start_time = chargeStateJsonObject
                    .get("scheduled_charging_start_time").getAsString();
        }

        // data from climateState
        detailedInformation.is_auto_conditioning_on = "1"
                .equals(climateStateJsonObject.get("is_auto_conditioning_on").getAsString());
        detailedInformation.is_climate_on = "1".equals(climateStateJsonObject.get("is_climate_on").getAsString());
        detailedInformation.is_front_defroster_on = "1"
                .equals(climateStateJsonObject.get("is_front_defroster_on").getAsString());
        detailedInformation.is_preconditioning = "1"
                .equals(climateStateJsonObject.get("is_preconditioning").getAsString());
        detailedInformation.is_rear_defroster_on = "1"
                .equals(climateStateJsonObject.get("is_rear_defroster_on").getAsString());
        detailedInformation.seat_heater_left = climateStateJsonObject.get("seat_heater_left").getAsInt();
        detailedInformation.seat_heater_rear_center = climateStateJsonObject.get("seat_heater_rear_center").getAsInt();
        detailedInformation.seat_heater_rear_left = climateStateJsonObject.get("seat_heater_rear_left").getAsInt();
        detailedInformation.seat_heater_rear_right = climateStateJsonObject.get("seat_heater_rear_right").getAsInt();
        detailedInformation.seat_heater_right = climateStateJsonObject.get("seat_heater_right").getAsInt();
        detailedInformation.side_mirror_heaters = "1"
                .equals(climateStateJsonObject.get("side_mirror_heaters").getAsString());
        detailedInformation.smart_preconditioning = "1"
                .equals(climateStateJsonObject.get("smart_preconditioning").getAsString());
        detailedInformation.steering_wheel_heater = "1"
                .equals(climateStateJsonObject.get("steering_wheel_heater").getAsString());
        detailedInformation.wiper_blade_heater = "1"
                .equals(climateStateJsonObject.get("wiper_blade_heater").getAsString());
        detailedInformation.driver_temp_setting = climateStateJsonObject.get("driver_temp_setting").getAsFloat();
        detailedInformation.inside_temp = climateStateJsonObject.get("inside_temp").getAsFloat();
        detailedInformation.outside_temp = climateStateJsonObject.get("outside_temp").getAsFloat();
        detailedInformation.passenger_temp_setting = climateStateJsonObject.get("passenger_temp_setting").getAsFloat();
        detailedInformation.fan_status = climateStateJsonObject.get("fan_status").getAsFloat();
        detailedInformation.left_temp_direction = climateStateJsonObject.get("left_temp_direction").getAsInt();
        detailedInformation.max_avail_temp = climateStateJsonObject.get("max_avail_temp").getAsFloat();
        detailedInformation.min_avail_temp = climateStateJsonObject.get("min_avail_temp").getAsFloat();
        detailedInformation.right_temp_direction = climateStateJsonObject.get("right_temp_direction").getAsInt();

        // data from driveState
        detailedInformation.heading = driveStateJsonObject.get("heading").getAsInt();
        detailedInformation.latitude = driveStateJsonObject.get("latitude").getAsFloat();
        detailedInformation.longitude = driveStateJsonObject.get("longitude").getAsFloat();
        detailedInformation.power = driveStateJsonObject.get("power").getAsFloat();
        // if car is parked, these will be NULL
        if (!driveStateJsonObject.get("shift_state").isJsonNull()) {
            detailedInformation.shift_state = driveStateJsonObject.get("shift_state").getAsString();
        } else {
            detailedInformation.shift_state = "N/A";
        }
        if (!driveStateJsonObject.get("speed").isJsonNull()) {
            detailedInformation.speed = driveStateJsonObject.get("speed").getAsFloat();
        } else {
            detailedInformation.speed = 0;
        }

        // data from vehicleState
        detailedInformation.locked = "1".equals(vehicleStateJsonObject.get("locked").getAsString());
        detailedInformation.sentry_mode = "1".equals(vehicleStateJsonObject.get("sentry_mode").getAsString());
        detailedInformation.valet_mode = "1".equals(vehicleStateJsonObject.get("valet_mode").getAsString());
        detailedInformation.software_update_status = vehicleStateJsonObject.get("software_update_status").getAsString();
        detailedInformation.software_update_version = vehicleStateJsonObject.get("software_update_version")
                .getAsString();
        detailedInformation.fd_window = "1".equals(vehicleStateJsonObject.get("fd_window").getAsString());
        detailedInformation.fp_window = "1".equals(vehicleStateJsonObject.get("fp_window").getAsString());
        detailedInformation.rd_window = "1".equals(vehicleStateJsonObject.get("rd_window").getAsString());
        detailedInformation.rp_window = "1".equals(vehicleStateJsonObject.get("rp_window").getAsString());
        // not all cars have a sun roof
        if (!vehicleStateJsonObject.get("sun_roof_state").isJsonNull()) {
            detailedInformation.sun_roof_state = vehicleStateJsonObject.get("sun_roof_state").getAsString();
            detailedInformation.sun_roof_percent_open = vehicleStateJsonObject.get("sun_roof_percent_open").getAsInt();
        } else {
            detailedInformation.sun_roof_state = "Not fitted";
            detailedInformation.sun_roof_percent_open = 0;
        }
        detailedInformation.homelink_nearby = "1".equals(vehicleStateJsonObject.get("homelink_nearby").getAsString());
        detailedInformation.tpms_pressure_fl = vehicleStateJsonObject.get("tpms_pressure_fl").getAsDouble();
        detailedInformation.tpms_pressure_fr = vehicleStateJsonObject.get("tpms_pressure_fr").getAsDouble();
        detailedInformation.tpms_pressure_rl = vehicleStateJsonObject.get("tpms_pressure_rl").getAsDouble();
        detailedInformation.tpms_pressure_rr = vehicleStateJsonObject.get("tpms_pressure_rr").getAsDouble();
        detailedInformation.df = "1".equals(vehicleStateJsonObject.get("df").getAsString());
        detailedInformation.dr = "1".equals(vehicleStateJsonObject.get("dr").getAsString());
        detailedInformation.pf = "1".equals(vehicleStateJsonObject.get("pf").getAsString());
        detailedInformation.pr = "1".equals(vehicleStateJsonObject.get("pr").getAsString());
        detailedInformation.ft = "1".equals(vehicleStateJsonObject.get("ft").getAsString());
        detailedInformation.rt = "1".equals(vehicleStateJsonObject.get("rt").getAsString());
        return detailedInformation;
    }
}
