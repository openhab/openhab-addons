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
    public double battery_level;
    public double usable_battery_level;
    public double battery_range;
    public double est_battery_range;
    public boolean charge_enable_request = false;
    public double charge_energy_added;
    public double charge_limit_soc;
    public boolean charge_port_door_open = false;
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
        detailedInformation.battery_level = chargeStateJsonObject.get("battery_level").getAsDouble();
        detailedInformation.usable_battery_level = chargeStateJsonObject.get("usable_battery_level").getAsDouble();
        detailedInformation.battery_range = chargeStateJsonObject.get("battery_range").getAsDouble();
        detailedInformation.est_battery_range = chargeStateJsonObject.get("est_battery_range").getAsDouble();
        detailedInformation.charge_enable_request = "1"
                .equals(chargeStateJsonObject.get("charge_enable_request").getAsString());
        detailedInformation.charge_energy_added = chargeStateJsonObject.get("charge_energy_added").getAsDouble();
        detailedInformation.charge_limit_soc = chargeStateJsonObject.get("charge_limit_soc").getAsDouble();
        detailedInformation.charge_port_door_open = "1"
                .equals(chargeStateJsonObject.get("charge_port_door_open").getAsString());
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
