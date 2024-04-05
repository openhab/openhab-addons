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
package org.openhab.binding.sunsynk.internal;

//import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SunSynkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lee Charlton - Initial contribution
 */
// @NonNullByDefault
public class SunSynkBindingConstants {

    private static final String BINDING_ID = "sunsynk";

    // List of all Thing Type UIDs
    // public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");
    public static final ThingTypeUID BRIDGE_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "sunsynkaccount");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");
    public static final ThingTypeUID THING_TYPE_PLANT = new ThingTypeUID(BINDING_ID, "plant");

    // List of all Channel ids
    // public static final String COMMAND = "command";
    public static final String PLANT_STATUS = "plant-status";

    public static final String CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE = "Interval-1-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE = "Interval-2-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE = "Interval-3-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE = "Interval-4-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE = "Interval-5-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE = "Interval-6-grid-charge";

    public static final String CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE = "Interval-1-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE = "Interval-2-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE = "Interval-3-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE = "Interval-4-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE = "Interval-5-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE = "Interval-6-gen-charge";

    public static final String CHANNEL_BATTERY_INTERVAL_1_TIME = "Interval-1-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_2_TIME = "Interval-2-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_3_TIME = "Interval-3-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_4_TIME = "Interval-4-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_5_TIME = "Interval-5-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_6_TIME = "Interval-6-grid-time";

    public static final String CHANNEL_BATTERY_INTERVAL_1_CAPACITY = "Interval-1-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_2_CAPACITY = "Interval-2-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_3_CAPACITY = "Interval-3-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_4_CAPACITY = "Interval-4-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_5_CAPACITY = "Interval-5-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_6_CAPACITY = "Interval-6-grid-capacity";

    public static final String CHANNEL_BATTERY_INTERVAL_1_POWER_LIMIT = "Interval-1-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_2_POWER_LIMIT = "Interval-2-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_3_POWER_LIMIT = "Interval-3-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_4_POWER_LIMIT = "Interval-4-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_5_POWER_LIMIT = "Interval-5-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_6_POWER_LIMIT = "Interval-6-grid-power-limit";

    public static final String CHANNEL_INVERTER_GRID_POWER = "Inverter-grid-power";
    public static final String CHANNEL_INVERTER_GRID_VOLTAGE = "Inverter-grid-voltage";
    public static final String CHANNEL_INVERTER_GRID_CURRENT = "Inverter-grid-current";

    public static final String CHANNEL_INVERTER_AC_TEMPERATURE = "Inverter-ac-temperature";
    public static final String CHANNEL_INVERTER_DC_TEMPERATURE = "Inverter-dc-temperature";

    public static final String CHANNEL_BATTERY_VOLTAGE = "Battery-grid-voltage";
    public static final String CHANNEL_BATTERY_CURRENT = "Battery-grid-current";
    public static final String CHANNEL_BATTERY_POWER = "Battery-grid-power";
    public static final String CHANNEL_BATTERY_SOC = "Battery-SOC";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "Battery-temperature";

    public static final String CHANNEL_INVERTER_SOLAR_ENERGY_TODAY = "Inverter-solar-energy-today";
    public static final String CHANNEL_INVERTER_SOLAR_ENERGY_TOTAL = "Inverter-solar-energy-total";
    public static final String CHANNEL_INVERTER_SOLAR_POWER_NOW = "Inverter-solar-power-now";

    // Need to do Solar RealTimeData

    // Config data
    public static final String CONFIG_SECRET = "access_token";
    public static final String CONFIG_GATE_SERIAL = "gsn";
    public static final String CONFIG_SERIAL = "sn";
    public static final String CONFIG_NAME = "alias";
    public static final String PROPERTY_SECRET = "access_token";
    public static final String PROPERTY_GATE_SERIAL = "gsn";
    public static final String PROPERTY_SERIAL = "sn";
    public static final String PROPERTY_NAME = "alias";
}
