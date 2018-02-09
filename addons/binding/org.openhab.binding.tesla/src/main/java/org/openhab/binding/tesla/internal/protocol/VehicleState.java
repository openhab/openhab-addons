/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link VehicleState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class VehicleState {

    public int df;
    public int dr;
    public int pf;
    public int pr;
    public int ft;
    public int rt;
    public String car_version;
    public boolean locked;
    public int sun_roof_installed;
    public String sun_roof_state;
    public int sun_roof_percent_open;
    public boolean dark_rims;
    public String wheel_type;
    public boolean has_spoiler;
    public String roof_color;
    public String perf_config;
    public String exterior_color;
    public int center_display_state;
    public boolean notifications_supported;
    public boolean parsed_calendar_supported;
    public float odometer;
    public int rear_seat_heaters;
    public boolean remote_start;
    public boolean remote_start_supported;
    public boolean rhd;
    public int seat_type;
    public boolean valet_mode;
    public boolean valet_pin_needed;
    public String vehicle_name;

    VehicleState() {
    }

}
