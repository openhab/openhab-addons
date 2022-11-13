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
    public static final String BRIDGE = "account";

    // List of all Device Types
    public static final String DEVICE = "device";

    // List of all Bridge Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE);

    // Device config parameter
    public static final String PARAMETER_UNIT_ID = "unitID";

    // Device properties
    public static final String PROPERTY_NAME = "name";

    // List of all Channel ids
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_CHARGING_STATE = "chargingState";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_MESSAGE = "message";
    public static final String CHANNEL_OVERRIDE = "override";
    public static final String CHANNEL_CHARGING_TIME_LEFT = "chargingTimeLeft";
    public static final String CHANNEL_PLUG_UNPLUG_TIME = "plugUnplugTime";
    public static final String CHANNEL_TARGET_TIME = "targetTime";
    public static final String CHANNEL_UNIT_TIME = "unitTime";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_CURRENT_LIMIT = "currentLimit";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_SAVINGS = "savings";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CHARGING_TIME = "chargingTime";
    public static final String CHANNEL_ENERGY_AT_PLUGIN = "energyAtPlugin";
    public static final String CHANNEL_ENERGY_TO_ADD = "energyToAdd";
    public static final String CHANNEL_LIFETIME_ENERGY = "lifetimeEnergy";
    public static final String CHANNEL_LIFETIME_SAVINGS = "lifetimeSavings";

    public static final String CHANNEL_GAS_COST = "gasCost";
    public static final String CHANNEL_FUEL_CONSUMPTION = "fuelConsumption";
    public static final String CHANNEL_ECOST = "ecost";
    public static final String CHANNEL_ENERGY_PER_MILE = "energyPerMile";

    public static final String CHANNEL_CAR_DESCRIPTION = "carDescription";
    public static final String CHANNEL_CAR_BATTERY_SIZE = "carBatterySize";
    public static final String CHANNEL_CAR_BATTERY_RANGE = "carBatteryRange";
    public static final String CHANNEL_CAR_CHARGING_RATE = "carChargingRate";
}
