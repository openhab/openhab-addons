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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SunSynkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkBindingConstants {

    private static final String BINDING_ID = "sunsynk";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "inverter");

    // List of all Channel ids
    public static final String CHANNEL_BATTERY_INTERVAL_1_GRID_CHARGE = "interval-1-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_2_GRID_CHARGE = "interval-2-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_3_GRID_CHARGE = "interval-3-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_4_GRID_CHARGE = "interval-4-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_5_GRID_CHARGE = "interval-5-grid-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_6_GRID_CHARGE = "interval-6-grid-charge";

    public static final String CHANNEL_BATTERY_INTERVAL_1_GEN_CHARGE = "interval-1-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_2_GEN_CHARGE = "interval-2-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_3_GEN_CHARGE = "interval-3-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_4_GEN_CHARGE = "interval-4-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_5_GEN_CHARGE = "interval-5-gen-charge";
    public static final String CHANNEL_BATTERY_INTERVAL_6_GEN_CHARGE = "interval-6-gen-charge";

    public static final String CHANNEL_BATTERY_INTERVAL_1_TIME = "interval-1-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_2_TIME = "interval-2-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_3_TIME = "interval-3-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_4_TIME = "interval-4-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_5_TIME = "interval-5-grid-time";
    public static final String CHANNEL_BATTERY_INTERVAL_6_TIME = "interval-6-grid-time";

    public static final String CHANNEL_BATTERY_INTERVAL_1_CAPACITY = "interval-1-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_2_CAPACITY = "interval-2-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_3_CAPACITY = "interval-3-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_4_CAPACITY = "interval-4-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_5_CAPACITY = "interval-5-grid-capacity";
    public static final String CHANNEL_BATTERY_INTERVAL_6_CAPACITY = "interval-6-grid-capacity";

    public static final String CHANNEL_BATTERY_INTERVAL_1_POWER_LIMIT = "interval-1-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_2_POWER_LIMIT = "interval-2-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_3_POWER_LIMIT = "interval-3-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_4_POWER_LIMIT = "interval-4-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_5_POWER_LIMIT = "interval-5-grid-power-limit";
    public static final String CHANNEL_BATTERY_INTERVAL_6_POWER_LIMIT = "interval-6-grid-power-limit";

    public static final String CHANNEL_INVERTER_GRID_POWER = "inverter-grid-power";
    public static final String CHANNEL_INVERTER_GRID_VOLTAGE = "inverter-grid-voltage";
    public static final String CHANNEL_INVERTER_GRID_CURRENT = "inverter-grid-current";

    public static final String CHANNEL_INVERTER_AC_TEMPERATURE = "inverter-ac-temperature";
    public static final String CHANNEL_INVERTER_DC_TEMPERATURE = "inverter-dc-temperature";

    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-grid-voltage";
    public static final String CHANNEL_BATTERY_CURRENT = "battery-grid-current";
    public static final String CHANNEL_BATTERY_POWER = "battery-grid-power";
    public static final String CHANNEL_BATTERY_SOC = "battery-soc";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "battery-temperature";

    public static final String CHANNEL_INVERTER_SOLAR_ENERGY_TODAY = "inverter-solar-energy-today";
    public static final String CHANNEL_INVERTER_SOLAR_ENERGY_TOTAL = "inverter-solar-energy-total";
    public static final String CHANNEL_INVERTER_SOLAR_POWER_NOW = "inverter-solar-power-now";

    public static final String CHANNEL_INVERTER_CONTROL_TIMER = "inverter-control-timer";
    public static final String CHANNEL_INVERTER_CONTROL_ENERGY_PATTERN = "inverter-control-energy-pattern";
    public static final String CHANNEL_INVERTER_CONTROL_WORK_MODE = "inverter-control-work-mode";

    // Thing Discovery
    public static final String CONFIG_GATE_SERIAL = "gsn";
    public static final String CONFIG_SERIAL = "serialnumber";
    public static final String CONFIG_NAME = "alias";
}
