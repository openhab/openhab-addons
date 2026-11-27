/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RemehaHeatingBindingConstants} class defines common constants used across the binding.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaHeatingBindingConstants {

    private static final String BINDING_ID = "remehaheating";

    public static final ThingTypeUID THING_TYPE_BOILER = new ThingTypeUID(BINDING_ID, "boiler");

    public static final String CHANNEL_ROOM_TEMPERATURE = "room-temperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_DHW_TEMPERATURE = "dhw-temperature";
    public static final String CHANNEL_DHW_TARGET = "dhw-target";
    public static final String CHANNEL_WATER_PRESSURE = "water-pressure";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_DHW_MODE = "dhw-mode";
    public static final String CHANNEL_WATER_PRESSURE_OK = "water-pressure-ok";
    public static final String CHANNEL_DHW_STATUS = "dhw-status";
    public static final String CHANNEL_THERMAL_MODE = "thermal-mode";
    public static final String CHANNEL_OPERATING_MODE = "operating-mode";
    public static final String CHANNEL_SEASONAL_MODE = "seasonal-mode";
    public static final String CHANNEL_HOLIDAY_ACTIVE = "holiday-active";
    public static final String CHANNEL_ZONE_MODE = "zone-mode";
    public static final String CHANNEL_HEATING_DEMAND = "heating-demand";
    public static final String CHANNEL_NEXT_SETPOINT = "next-setpoint";
    public static final String CHANNEL_NEXT_SWITCH_TIME = "next-switch-time";
    public static final String CHANNEL_FIREPLACE_MODE_ACTIVE = "fireplace-mode-active";
    public static final String CHANNEL_DHW_NEXT_SWITCH_TIME = "dhw-next-switch-time";
    public static final String CHANNEL_DHW_ANTI_LEGIONELLA = "dhw-anti-legionella";
    public static final String CHANNEL_HEATING_CURVE_SLOPE = "heating-curve-slope";
    public static final String CHANNEL_HEATING_CURVE_BASE_SETPOINT = "heating-curve-base-setpoint";
    public static final String CHANNEL_HEATING_CURVE_MAX_FLOW_TEMPERATURE = "heating-curve-max-flow-temperature";

    public static final String CONFIG_EMAIL = "email";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";

    public static final String DHW_MODE_ANTI_FROST = "anti-frost";
    public static final String DHW_MODE_SCHEDULE = "schedule";
    public static final String DHW_MODE_CONTINUOUS_COMFORT = "continuous-comfort";
}
