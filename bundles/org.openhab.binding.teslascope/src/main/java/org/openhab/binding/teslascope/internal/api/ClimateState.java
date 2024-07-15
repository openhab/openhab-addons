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
public class ClimateState {
    // climate_state
    public int is_auto_conditioning_on;
    public int is_climate_on;
    public int is_front_defroster_on;
    public int is_preconditioning;
    public int is_rear_defroster_on;
    public int seat_heater_left;
    public int seat_heater_rear_center;
    public int seat_heater_rear_left;
    public int seat_heater_rear_right;
    public int seat_heater_right;
    public int side_mirror_heaters;
    public int smart_preconditioning;
    public int steering_wheel_heater;
    public int wiper_blade_heater;
    public float driver_temp_setting;
    public float inside_temp;
    public float outside_temp;
    public float passenger_temp_setting;
    public float fan_status;
    public int left_temp_direction;
    public float max_avail_temp;
    public float min_avail_temp;
    public int right_temp_direction;

    private ClimateState() {
    }
}
