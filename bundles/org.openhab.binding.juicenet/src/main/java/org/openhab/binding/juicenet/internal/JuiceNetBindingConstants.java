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

    // Device properties
    public static final String DEVICE_PROP_NAME = "name";
    public static final String DEVICE_PROP_UNIT_ID = "unitID";
    public static final String DEVICE_PROP_TOKEN = "token";

    // List of all Channel ids
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_CHARGING_STATE = "chargingState";
    public static final String DEVICE_STATE = "state";
    public static final String DEVICE_OVERRIDE = "override";
    public static final String DEVICE_CHARGING_TIME_LEFT = "chargingTimeLeft";
    public static final String DEVICE_PLUG_UNPLUG_TIME = "plugUnplugTime";
    public static final String DEVICE_TARGET_TIME = "targetTime";
    public static final String DEVICE_UNIT_TIME = "unitTime";
    public static final String DEVICE_TEMPERATURE = "temperature";
    public static final String DEVICE_AMPS_LIMIT = "currentLimit";
    public static final String DEVICE_AMPS_CURRENT = "current";
    public static final String DEVICE_VOLTAGE = "voltage";
    public static final String DEVICE_ENERGY = "energy";
    public static final String DEVICE_SAVINGS = "savings";
    public static final String DEVICE_POWER = "power";
    public static final String DEVICE_CHARGING_TIME = "secondsCharging";
    public static final String DEVICE_PLUGINENERGY = "energyAtPlugin";
    public static final String DEVICE_ENERGYTOADD = "energyToAdd";
    public static final String DEVICE_LIFETIME_ENERGY = "lifetimeEnergy";
    public static final String DEVICE_LIFETIME_SAVINGS = "lifetimeSavings";

    public static final String DEVICE_GASCOST = "gasCost";
    public static final String DEVICE_FUELCONSUMPTION = "fuelConsumption";
    public static final String DEVICE_ECOST = "ecost";
    public static final String DEVICE_WHPERMILE = "energyPerMile";

    public static final String DEVICE_CAR_DESCRIPTION = "carDescription";
    public static final String DEVICE_CAR_BATTERY_SIZE = "carBatterySize";
    public static final String DEVICE_CAR_BATTERY_RANGE = "carBatteryRange";
    public static final String DEVICE_CAR_CHARGING_RATE = "carChargingRate";
}
