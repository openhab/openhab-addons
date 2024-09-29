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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link ClimateState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class ClimateState {

    public boolean battery_heater;
    public boolean battery_heater_no_power;
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
    public int fan_status;
    public int left_temp_direction;
    public int max_avail_temp;
    public int min_avail_temp;
    public int right_temp_direction;
    public int seat_heater_rear_left_back;
    public int seat_heater_rear_right_back;

    ClimateState() {
    }
}
