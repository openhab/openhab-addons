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

import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VehicleConfig} is a data structure to capture
 * vehicle configuration variables sent by the Tesla Vehicle
 *
 * @author Dan Cunningham - Initial contribution
 */
public class VehicleConfig {
    public boolean can_accept_navigation_requests;
    public boolean can_actuate_trunks;
    public boolean eu_vehicle;
    public boolean has_air_suspension;
    public boolean has_ludicrous_mode;
    public boolean motorized_charge_port;
    public boolean plg;
    public boolean rhd;
    public boolean use_range_badging;
    public int rear_seat_heaters;
    public int rear_seat_type;
    public int sun_roof_installed;
    public long timestamp;
    public String car_special_type;
    public String car_type;
    public String charge_port_type;
    public String exterior_color;
    public String roof_color;
    public String spoiler_type;
    public String third_row_seats;
    public String trim_badging;
    public String wheel_type;

    public ThingTypeUID identifyModel() {
        switch (car_type) {
            case "models":
            case "models2":
                return TeslaBindingConstants.THING_TYPE_MODELS;
            case "modelx":
                return TeslaBindingConstants.THING_TYPE_MODELX;
            case "model3":
                return TeslaBindingConstants.THING_TYPE_MODEL3;
            case "modely":
                return TeslaBindingConstants.THING_TYPE_MODELY;
            default:
                return TeslaBindingConstants.THING_TYPE_VEHICLE;
        }
    }
}
