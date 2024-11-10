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
package org.openhab.binding.electroluxappliance.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ElectroluxApplianceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxApplianceBindingConstants {

    public static final String BINDING_ID = "electroluxappliance";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ELECTROLUX_AIR_PURIFIER = new ThingTypeUID(BINDING_ID, "air-purifier");
    public static final ThingTypeUID THING_TYPE_ELECTROLUX_WASHING_MACHINE = new ThingTypeUID(BINDING_ID,
            "washing-machine");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "api");

    // List of all common Channel ids
    public static final String CHANNEL_DOOR_STATE = "door-state";

    // List of all Channel ids for Air Purifers
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TVOC = "tvoc";
    public static final String CHANNEL_PM1 = "pm1";
    public static final String CHANNEL_PM25 = "pm2_5";
    public static final String CHANNEL_PM10 = "pm10";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_FILTER_LIFE = "filter-life";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_WORK_MODE = "work-mode";
    public static final String CHANNEL_IONIZER = "ionizer";
    public static final String CHANNEL_UI_LIGHT = "ui-light";
    public static final String CHANNEL_SAFETY_LOCK = "safety-lock";

    // List of all Channel ids for Washing Machines
    public static final String CHANNEL_DOOR_LOCK = "door-lock";
    public static final String CHANNEL_TIME_TO_START = "time-to-start";
    public static final String CHANNEL_TIME_TO_END = "time-to-end";
    public static final String CHANNEL_APPLIANCE_UI_SW_VERSION = "appliance-ui-sw-version";
    public static final String CHANNEL_APPLIANCE_TOTAL_WORKING_TIME = "appliance-total-working-time";
    public static final String CHANNEL_APPLIANCE_STATE = "appliance-state";
    public static final String CHANNEL_APPLIANCE_MODE = "appliance-mode";
    public static final String CHANNEL_OPTISENSE_RESULT = "optisense-result";
    public static final String CHANNEL_DETERGENT_EXTRA_DOSAGE = "detergent-extradosage";
    public static final String CHANNEL_SOFTENER_EXTRA_DOSAGE = "softener-extradosage";
    public static final String CHANNEL_WATER_USAGE = "water-usage";
    public static final String CHANNEL_CYCLE_PHASE = "cycle-phase";
    public static final String CHANNEL_TOTAL_WASH_CYCLES_COUNT = "total-wash-cycles-count";
    public static final String CHANNEL_ANALOG_TEMPERATURE = "analog-temperature";
    public static final String CHANNEL_ANALOG_SPIN_SPEED = "analog-spin-speed";
    public static final String CHANNEL_STEAM_VALUE = "steam-value";
    public static final String CHANNEL_PROGRAMS_ORDER = "programs-order";

    // List of all Properties ids
    public static final String PROPERTY_BRAND = "brand";
    public static final String PROPERTY_COLOUR = "colour";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_DEVICE = "device";
    public static final String PROPERTY_FW_VERSION = "fwVersion";
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_WORKMODE = "workmode";

    // List of all Commands for Air Purifiers
    public static final String COMMAND_WORKMODE_POWEROFF = "PowerOff";
    public static final String COMMAND_WORKMODE_AUTO = "Auto";
    public static final String COMMAND_WORKMODE_MANUAL = "Manual";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_ELECTROLUX_AIR_PURIFIER, THING_TYPE_ELECTROLUX_WASHING_MACHINE);
}
