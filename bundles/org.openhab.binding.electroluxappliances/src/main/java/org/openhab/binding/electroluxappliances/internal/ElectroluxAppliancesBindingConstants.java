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
package org.openhab.binding.electroluxappliances.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ElectroluxAppliancesBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAppliancesBindingConstants {

    public static final String BINDING_ID = "electroluxappliances";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ELECTROLUX_AIR_PURIFIER = new ThingTypeUID(BINDING_ID,
            "electroluxairpurifier");
    public static final ThingTypeUID THING_TYPE_ELECTROLUX_WASHING_MACHINE = new ThingTypeUID(BINDING_ID,
            "electroluxwashingmachine");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "api");

    // List of all common Channel ids
    public static final String CONNECTION_STATE = "connectionState";

    // List of all Channel ids for Air Purifers
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TVOC = "tvoc";
    public static final String CHANNEL_PM1 = "pm1";
    public static final String CHANNEL_PM25 = "pm2_5";
    public static final String CHANNEL_PM10 = "pm10";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_FILTER_LIFE = "filterLife";
    public static final String CHANNEL_DOOR_OPEN = "doorOpen";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_WORK_MODE = "workMode";
    public static final String CHANNEL_IONIZER = "ionizer";
    public static final String CHANNEL_UI_LIGHT = "uiLight";
    public static final String CHANNEL_SAFETY_LOCK = "safetyLock";

    // List of all Channel ids for Washing Machines
    public static final String DOOR_STATE = "doorState";
    public static final String DOOR_LOCK = "doorLock";
    public static final String START_TIME = "startTime";
    public static final String TIME_TO_END = "timeToEnd";
    public static final String APPLIANCE_UI_SW_VERSION = "applianceUiSwVersion";
    public static final String APPLIANCE_TOTAL_WORKING_TIME = "applianceTotalWorkingTime";
    public static final String APPLIANCE_STATE = "applianceState";
    public static final String APPLIANCE_MODE = "applianceMode";
    public static final String OPTISENSE_RESULT = "optisenseResult";
    public static final String DETERGENT_EXTRA_DOSAGE = "detergentExtradosage";
    public static final String SOFTENER_EXTRA_DOSAGE = "softenerExtradosage";
    public static final String WATER_USAGE = "waterUsage";
    public static final String CYCLE_PHASE = "cyclePhase";
    public static final String TOTAL_WASH_CYCLES_COUNT = "totalWashCyclesCount";
    public static final String ANALOG_TEMPERATURE = "analogTemperature";
    public static final String ANALOG_SPIN_SPEED = "analogSpinSpeed";
    public static final String STEAM_VALUE = "steamValue";
    public static final String PROGRAMS_ORDER = "programsOrder";

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
