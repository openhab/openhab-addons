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
package org.openhab.binding.gridbox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GridBoxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxBindingConstants {

    private static final String BINDING_ID = "gridbox";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GRIDBOX = new ThingTypeUID(BINDING_ID, "gridbox");

    // List of all Channel ids
    public static final String BATTERY_CAPACITY = "battery-capacity";
    public static final String BATTERY_NOMINAL_CAPACITY = "battery-nominal-capacity";
    public static final String BATTERY_POWER = "battery-power";
    public static final String BATTERY_REMAINING_CHARGE = "battery-remaining-charge";
    public static final String BATTERY_STATE_OF_CHARGE = "battery-state-of-charge";
    public static final String BATTERY_LEVEL = "battery-level";
    public static final String CONSUMPTION = "consumption";
    public static final String DIRECT_CONSUMPTION = "direct-consumption";
    public static final String DIRECT_CONSUMPTION_EV = "direct-consumption-ev";
    public static final String DIRECT_CONSUMPTION_HEAT_PUMP = "direct-consumption-heat-pump";
    public static final String DIRECT_CONSUMPTION_HEATER = "direct-consumption-heater";
    public static final String DIRECT_CONSUMPTION_HOUSEHOLD = "direct-consumption-household";
    public static final String DIRECT_CONSUMPTION_RATE = "direct-consumption-rate";
    public static final String EV_CHARGING_STATION_POWER = "ev-charging-station-power";
    public static final String HEAT_PUMP_POWER = "heat-pump-power";
    public static final String PHOTOVOLTAIC_PRODUCTION = "photovoltaic-production";
    public static final String PRODUCTION = "production";
    public static final String SELF_CONSUMPTION = "self-consumption";
    public static final String SELF_CONSUMPTION_RATE = "self-consumption-rate";
    public static final String SELF_SUFFICIENCY_RATE = "self-sufficiency-rate";
    public static final String SELF_SUPPLY = "self-supply";
    public static final String TOTAL_CONSUMPTION = "total-consumption";
}
