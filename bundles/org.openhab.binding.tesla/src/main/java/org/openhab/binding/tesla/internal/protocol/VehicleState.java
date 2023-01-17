/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link VehicleState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class VehicleState {

    public boolean dark_rims;
    public boolean has_spoiler;
    public boolean homelink_nearby;
    public boolean is_user_present;
    public boolean locked;
    public boolean notifications_supported;
    public boolean parsed_calendar_supported;
    public boolean remote_start;
    public boolean remote_start_supported;
    public boolean rhd;
    public boolean sentry_mode;
    public boolean valet_mode;
    public boolean valet_pin_needed;
    public float odometer;
    public int center_display_state;
    public int df;
    public int dr;
    public int ft;
    public int pf;
    public int pr;
    public int rear_seat_heaters;
    public int rt;
    public int seat_type;
    public int sun_roof_installed;
    public int sun_roof_percent_open;
    public String autopark_state;
    public String autopark_state_v2;
    public String autopark_style;
    public String car_version;
    public String exterior_color;
    public String last_autopark_error;
    public String perf_config;
    public String roof_color;
    public String sun_roof_state;
    public String vehicle_name;
    public String wheel_type;

    VehicleState() {
    }
}
