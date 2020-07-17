/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link E3DCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBindingConstants {

    private static final String BINDING_ID = "e3dc";

    // Suppoerted Thing Types
    public static final ThingTypeUID THING_TYPE_E3DC_DEVICE = new ThingTypeUID(BINDING_ID, "e3dc-device");
    public static final ThingTypeUID THING_TYPE_E3DC_INFO = new ThingTypeUID(BINDING_ID, "e3dc-info");
    public static final ThingTypeUID THING_TYPE_E3DC_POWER = new ThingTypeUID(BINDING_ID, "e3dc-power");
    public static final ThingTypeUID THING_TYPE_E3DC_WALLBOX = new ThingTypeUID(BINDING_ID, "e3dc-wallbox");
    public static final ThingTypeUID THING_TYPE_E3DC_STRING_DETAILS = new ThingTypeUID(BINDING_ID, "e3dc-strings");
    public static final ThingTypeUID THING_TYPE_E3DC_EMERGENCY_POWER = new ThingTypeUID(BINDING_ID, "e3dc-emergency");

    // Channels for Info Block
    public static final String MODBUS_ID_CHANNEL = "modbus-id";
    public static final String MODBUS_FIRMWARE_CHANNEL = "modbus-firmware";
    public static final String SUPPORTED_REGSITERS_CHANNEL = "supported-registers";
    public static final String MANUFACTURER_NAME_CHANNEL = "manufacturer-name";
    public static final String MODEL_NAME_CHANNEL = "model-name";
    public static final String SERIAL_NUMBER_CHANNEL = "serial-number";
    public static final String FIRMWARE_RELEASE_CHANNEL = "firmware-relase";
    public static final String INFO_CHANNEL = "info";

    // Channels for Power Block
    public static final String PV_POWER_SUPPLY_CHANNEL = "pv-power-supply";
    public static final String BATTERY_POWER_SUPPLY_CHANNEL = "battery-power-supply";
    public static final String BATTERY_POWER_CONSUMPTION = "battery-power-consumption";
    public static final String HOUSEHOLD_POWER_CONSUMPTION_CHANNEL = "household-power-consumption";
    public static final String GRID_POWER_CONSUMPTION_CHANNEL = "grid-power-consumption";
    public static final String GRID_POWER_SUPPLY_CHANNEL = "grid-power-supply";
    public static final String EXTERNAL_POWER_SUPPLY_CHANNEL = "external-power-supply";
    public static final String WALLBOX_POWER_CONSUMPTION_CHANNEL = "wallbox-power-consumption";
    public static final String WALLBOX_PV_POWER_CONSUMPTION_CHANNEL = "wallbox-pv-power-consumption";
    public static final String AUTARKY = "autarky";
    public static final String SELF_CONSUMPTION = "self-consumption";
    public static final String BATTERY_STATE_OF_CHARGE_CHANNEL = "battery-soc";

    // Channels for Wallbox Block
    public static final String WB_AVAILABLE_CHANNEL = "wb-available";
    public static final String WB_SUNMODE_CHANNEL = "wb-sunmode";
    public static final String WB_CHARGING_CHANNEL = "wb-cahrging";
    public static final String WB_JACK_LOCKED_CHANNEL = "wb-jack-locked";
    public static final String WB_JACK_PLUGGED_CHANNEL = "wb-jack-locked";
    public static final String WB_SCHUKO_ON_CHANNEL = "wb-schuko-on";
    public static final String WB_SCHUKO_PLUGGED_CHANNEL = "wb-schuko-plugged";
    public static final String WB_SCHUKO_LOCKED_CHANNEL = "wb-schuko-locked";
    public static final String WB_REALY_16A_CHANNEL = "wb-relay-16a";
    public static final String WB_RELAY_32A_CHANNEL = "wb-relay-32a";
    public static final String WB_3PHASE_CHANNEL = "wb-3phase";

    // Channels for String details
    public static final String STRING1_DC_VOLTAGE_CHANNEL = "string1-dc-voltage";
    public static final String STRING1_DC_CURRENT_CHANNEL = "string1-dc-current";
    public static final String STRING1_DC_OUTPUT_CHANNEL = "string1-dc-output";
    public static final String STRING2_DC_VOLTAGE_CHANNEL = "string2-dc-voltage";
    public static final String STRING2_DC_CURRENT_CHANNEL = "string2-dc-current";
    public static final String STRING2_DC_OUTPUT_CHANNEL = "string2-dc-output";
    public static final String STRING3_DC_VOLTAGE_CHANNEL = "string3-dc-voltage";
    public static final String STRING3_DC_CURRENT_CHANNEL = "string3-dc-current";
    public static final String STRING3_DC_OUTPUT_CHANNEL = "string3-dc-output";

    // Channels for Emergency Status
    public static final String EMERGENCY_POWER_STATUS = "emergency-power-status";
    public static final String BATTERY_LOADING_LOCKED = "battery-loading-lock";
    public static final String BATTERY_UNLOADING_LOCKED = "battery-unloading-lock";
    public static final String EMERGENCY_POWER_POSSIBLE = "emergency-power-possible";
    public static final String WEATHER_PREDICTION_LOADING = "weather-predicted-loading";
    public static final String REGULATION_STATUS = "regulation-status";
    public static final String LOADING_LOCK_TIME = "loading-lock-timey";
    public static final String UNLOADING_LOCKTIME = "unloading-lock-time";
}
