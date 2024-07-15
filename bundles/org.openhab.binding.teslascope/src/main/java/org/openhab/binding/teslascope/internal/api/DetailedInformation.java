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

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class DetailedInformation {
    public String vin = "";
    public String name = "";
    public String state = "";
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
    public String charging_state = "";
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
    public boolean tpms_soft_warning_fl;
    public boolean tpms_soft_warning_fr;
    public boolean tpms_soft_warning_rl;
    public boolean tpms_soft_warning_rr;
    public boolean df;
    public boolean dr;
    public boolean pf;
    public boolean pr;
    public boolean ft;
    public boolean rt;

    private DetailedInformation() {
    }
}
