/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.juicenet.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link JuiceNetBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetBindingConstants {
    private static final String BINDING_ID = "juicenet";

    // List of Bridge Type
    public static final String BRIDGE = "juicenet-account";

    // List of all Device Types
    public static final String DEVICE = "juicenet-device";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE);

    // Device properties
    public static final String DEVICE_PROP_NAME = "name";
    public static final String DEVICE_PROP_UNIT_ID = "unit_id";
    public static final String DEVICE_PROP_TOKEN = "token";

    // List of all Channel ids
    public static final String DEVICE_CHARGING_STATE = "charging_state";
    public static final String DEVICE_STATE = "state";
    public static final String DEVICE_OVERRIDE = "override";
    public static final String DEVICE_CHARGING_TIME_LEFT = "charging_time_left";
    public static final String DEVICE_PLUG_UNPLUG_TIME = "plug_unplug_time";
    public static final String DEVICE_TARGET_TIME = "target_time";
    public static final String DEVICE_UNIT_TIME = "unit_time";
    public static final String DEVICE_TEMPERATURE = "temperature";
    public static final String DEVICE_AMPS_LIMIT = "amps_limit";
    public static final String DEVICE_AMPS_CURRENT = "amps_current";
    public static final String DEVICE_VOLTAGE = "voltage";
    public static final String DEVICE_ENERGY = "wh_energy";
    public static final String DEVICE_SAVINGS = "savings";
    public static final String DEVICE_POWER = "watt_power";
    public static final String DEVICE_CHARGING_TIME = "seconds_charging";
    public static final String DEVICE_PLUGINENERGY = "wh_energy_at_plugin";
    public static final String DEVICE_ENERGYTOADD = "wh_energy_to_add";
    public static final String DEVICE_LIFETIME_ENERGY = "lifetime_wh_energy";
    public static final String DEVICE_LIFETIME_SAVINGS = "lifetime_savings";

    public static final String DEVICE_GASCOST = "gascost";
    public static final String DEVICE_MPG = "mpg";
    public static final String DEVICE_ECOST = "ecost";
    public static final String DEVICE_WHPERMILE = "whpermile";

    public static final String DEVICE_CAR_DESCRIPTION = "car_description";
    public static final String DEVICE_CAR_BATTERY_SIZE = "car_battery_size_wh";
    public static final String DEVICE_CAR_BATTERY_RANGE = "car_battery_range_m";
    public static final String DEVICE_CAR_CHARGING_RATE = "car_charging_rate_w";
}
