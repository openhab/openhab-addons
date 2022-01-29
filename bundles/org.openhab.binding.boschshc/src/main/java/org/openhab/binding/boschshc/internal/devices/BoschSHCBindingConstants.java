/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoschSHCBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - added Shutter Control, ThermostatHandler
 * @author Christian Oeing - Added WallThermostatHandler
 */
@NonNullByDefault
public class BoschSHCBindingConstants {

    private static final String BINDING_ID = "boschshc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHC = new ThingTypeUID(BINDING_ID, "shc");

    public static final ThingTypeUID THING_TYPE_INWALL_SWITCH = new ThingTypeUID(BINDING_ID, "in-wall-switch");
    public static final ThingTypeUID THING_TYPE_TWINGUARD = new ThingTypeUID(BINDING_ID, "twinguard");
    public static final ThingTypeUID THING_TYPE_WINDOW_CONTACT = new ThingTypeUID(BINDING_ID, "window-contact");
    public static final ThingTypeUID THING_TYPE_MOTION_DETECTOR = new ThingTypeUID(BINDING_ID, "motion-detector");
    public static final ThingTypeUID THING_TYPE_SHUTTER_CONTROL = new ThingTypeUID(BINDING_ID, "shutter-control");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_CLIMATE_CONTROL = new ThingTypeUID(BINDING_ID, "climate-control");
    public static final ThingTypeUID THING_TYPE_WALL_THERMOSTAT = new ThingTypeUID(BINDING_ID, "wall-thermostat");

    // List of all Channel IDs
    // Auto-generated from thing-types.xml via script, don't modify
    public static final String CHANNEL_POWER_SWITCH = "power-switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_RATING = "temperature-rating";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY_RATING = "humidity-rating";
    public static final String CHANNEL_ENERGY_CONSUMPTION = "energy-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION = "power-consumption";
    public static final String CHANNEL_PURITY = "purity";
    public static final String CHANNEL_AIR_DESCRIPTION = "air-description";
    public static final String CHANNEL_PURITY_RATING = "purity-rating";
    public static final String CHANNEL_COMBINED_RATING = "combined-rating";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_LATEST_MOTION = "latest-motion";
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_VALVE_TAPPET_POSITION = "valve-tappet-position";
    public static final String CHANNEL_SETPOINT_TEMPERATURE = "setpoint-temperature";
    public static final String CHANNEL_CHILD_LOCK = "child-lock";
}
