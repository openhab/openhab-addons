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
    public double batterylevel;
    public double usable_battery_level;
    public double battery_range;
    public double est_battery_range;
    public int charge_enable_request;
    public double charge_energy_added;
    public double charge_limit_soc;
    public int charge_port_door_open;
    public double charge_rate;
    public double charger_power;
    public double charger_voltage;
    public String chargingstate = "";
    public double time_to_full_charge;

    // drive state
    public int heading;
    public double latitude;
    public double longitude;
    public String shift_state = "";
    public double speed;

    // vehicle_state
    public int locked;
    public int sentry_mode;
    public int valet_mode;
    public String software_update_status = "";
    public String software_update_version = "";
    public int fd_window;
    public int fp_window;
    public int rd_window;
    public int rp_window;
    public String sun_roof_state = "";
    public int sun_roof_percent_open;
    public int homelink_nearby;
    public double tpms_pressure_fl;
    public double tpms_pressure_fr;
    public double tpms_pressure_rl;
    public double tpms_pressure_rr;
    public int df;
    public int dr;
    public int pf;
    public int pr;
    public int ft;
    public int rt;

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
        detailedInformation.batterylevel = chargeStateJsonObject.get("battery_level").getAsDouble();
        detailedInformation.usable_battery_level = chargeStateJsonObject.get("usable_battery_level").getAsDouble();
        detailedInformation.battery_range = chargeStateJsonObject.get("battery_range").getAsDouble();
        detailedInformation.est_battery_range = chargeStateJsonObject.get("est_battery_range").getAsDouble();
        detailedInformation.charge_enable_request = chargeStateJsonObject.get("charge_enable_request").getAsInt();
        detailedInformation.charge_energy_added = chargeStateJsonObject.get("charge_energy_added").getAsDouble();
        detailedInformation.charge_limit_soc = chargeStateJsonObject.get("charge_limit_soc").getAsDouble();
        detailedInformation.charge_port_door_open = chargeStateJsonObject.get("charge_port_door_open").getAsInt();
        detailedInformation.charge_rate = chargeStateJsonObject.get("charge_rate").getAsDouble();
        detailedInformation.charger_power = chargeStateJsonObject.get("charger_power").getAsDouble();
        detailedInformation.charger_voltage = chargeStateJsonObject.get("charger_voltage").getAsDouble();
        detailedInformation.chargingstate = chargeStateJsonObject.get("charging_state").getAsString();
        detailedInformation.time_to_full_charge = chargeStateJsonObject.get("time_to_full_charge").getAsDouble();

        // data from driveState
        detailedInformation.heading = driveStateJsonObject.get("heading").getAsInt();
        detailedInformation.latitude = driveStateJsonObject.get("latitude").getAsDouble();
        detailedInformation.longitude = driveStateJsonObject.get("longitude").getAsDouble();
        // if car is parked, these will be
        if (!driveStateJsonObject.get("shift_state").isJsonNull()) {
            detailedInformation.shift_state = driveStateJsonObject.get("shift_state").getAsString();
        }
        if (!driveStateJsonObject.get("speed").isJsonNull()) {
            detailedInformation.speed = driveStateJsonObject.get("speed").getAsDouble();
        }

        // data from vehicleState
        detailedInformation.locked = vehicleStateJsonObject.get("locked").getAsInt();
        detailedInformation.sentry_mode = vehicleStateJsonObject.get("sentry_mode").getAsInt();
        detailedInformation.valet_mode = vehicleStateJsonObject.get("valet_mode").getAsInt();
        detailedInformation.software_update_status = vehicleStateJsonObject.get("software_update_status").getAsString();
        detailedInformation.software_update_version = vehicleStateJsonObject.get("software_update_version")
                .getAsString();
        detailedInformation.fd_window = vehicleStateJsonObject.get("fd_window").getAsInt();
        detailedInformation.fp_window = vehicleStateJsonObject.get("fp_window").getAsInt();
        detailedInformation.rd_window = vehicleStateJsonObject.get("rd_window").getAsInt();
        detailedInformation.rp_window = vehicleStateJsonObject.get("rp_window").getAsInt();
        // not all cars have a sun roof
        if (!vehicleStateJsonObject.get("sun_roof_state").isJsonNull()) {
            detailedInformation.sun_roof_state = vehicleStateJsonObject.get("sun_roof_state").getAsString();
            detailedInformation.sun_roof_percent_open = vehicleStateJsonObject.get("sun_roof_percent_open").getAsInt();
        }
        detailedInformation.homelink_nearby = vehicleStateJsonObject.get("homelink_nearby").getAsInt();
        detailedInformation.tpms_pressure_fl = vehicleStateJsonObject.get("tpms_pressure_fl").getAsDouble();
        detailedInformation.tpms_pressure_fr = vehicleStateJsonObject.get("tpms_pressure_fr").getAsDouble();
        detailedInformation.tpms_pressure_rl = vehicleStateJsonObject.get("tpms_pressure_rl").getAsDouble();
        detailedInformation.tpms_pressure_rr = vehicleStateJsonObject.get("tpms_pressure_rr").getAsDouble();
        detailedInformation.df = vehicleStateJsonObject.get("df").getAsInt();
        detailedInformation.dr = vehicleStateJsonObject.get("dr").getAsInt();
        detailedInformation.pf = vehicleStateJsonObject.get("pf").getAsInt();
        detailedInformation.pr = vehicleStateJsonObject.get("pr").getAsInt();
        detailedInformation.ft = vehicleStateJsonObject.get("ft").getAsInt();
        detailedInformation.rt = vehicleStateJsonObject.get("rt").getAsInt();
        return detailedInformation;
    }
}
