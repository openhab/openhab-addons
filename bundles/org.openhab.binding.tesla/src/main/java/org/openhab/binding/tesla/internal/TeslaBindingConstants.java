/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TeslaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class TeslaBindingConstants {

    // REST URI constants
    public static final String API_NAME = "Tesla Client API";
    public static final String API_VERSION = "api/1/";
    public static final String PATH_COMMAND = "command/{cmd}";
    public static final String PATH_DATA_REQUEST = "data_request/{cmd}";
    public static final String PATH_VEHICLE_ID = "/{vid}/";
    public static final String PATH_WAKE_UP = "wake_up";
    public static final String URI_ACCESS_TOKEN = "oauth/token";
    public static final String URI_EVENT = "https://streaming.vn.teslamotors.com/stream/";
    public static final String URI_OWNERS = "https://owner-api.teslamotors.com/";
    public static final String VALETPIN = "valetpin";
    public static final String VEHICLES = "vehicles";
    public static final String VIN = "vin";

    // Tesla REST API commands
    public static final String COMMAND_AUTO_COND_START = "auto_conditioning_start";
    public static final String COMMAND_AUTO_COND_STOP = "auto_conditioning_stop";
    public static final String COMMAND_CHARGE_MAX = "charge_max_range";
    public static final String COMMAND_CHARGE_START = "charge_start";
    public static final String COMMAND_CHARGE_STD = "charge_standard";
    public static final String COMMAND_CHARGE_STOP = "charge_stop";
    public static final String COMMAND_DOOR_LOCK = "door_lock";
    public static final String COMMAND_DOOR_UNLOCK = "door_unlock";
    public static final String COMMAND_FLASH_LIGHTS = "flash_lights";
    public static final String COMMAND_HONK_HORN = "honk_horn";
    public static final String COMMAND_OPEN_CHARGE_PORT = "charge_port_door_open";
    public static final String COMMAND_RESET_VALET_PIN = "reset_valet_pin";
    public static final String COMMAND_SET_CHARGE_LIMIT = "set_charge_limit";
    public static final String COMMAND_SET_TEMP = "set_temps";
    public static final String COMMAND_SET_VALET_MODE = "set_valet_mode";
    public static final String COMMAND_SUN_ROOF = "sun_roof_control";
    public static final String COMMAND_THROTTLE = "commandthrottle";
    public static final String COMMAND_TRUNK_OPEN = "trunk_open";
    public static final String COMMAND_WAKE_UP = "wake_up";
    public static final String DATA_THROTTLE = "datathrottle";

    // Tesla REST API vehicle states
    public static final String CHARGE_STATE = "charge_state";
    public static final String CLIMATE_STATE = "climate_state";
    public static final String DRIVE_STATE = "drive_state";
    public static final String GUI_STATE = "gui_settings";
    public static final String MOBILE_ENABLED_STATE = "mobile_enabled";
    public static final String VEHICLE_STATE = "vehicle_state";
    public static final String VEHICLE_CONFIG = "vehicle_config";

    public static final String BINDING_ID = "tesla";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_MODELS = new ThingTypeUID(BINDING_ID, "models");
    public static final ThingTypeUID THING_TYPE_MODEL3 = new ThingTypeUID(BINDING_ID, "model3");
    public static final ThingTypeUID THING_TYPE_MODELX = new ThingTypeUID(BINDING_ID, "modelx");
    public static final ThingTypeUID THING_TYPE_MODELY = new ThingTypeUID(BINDING_ID, "modely");

    public enum EventKeys {
        timestamp,
        odometer,
        speed,
        soc,
        elevation,
        est_heading,
        est_lat,
        est_lng,
        power,
        shift_state,
        range,
        est_range,
        heading
    }

    public static final String CHANNEL_CHARGE = "charge";
    public static final String CHANNEL_COMBINED_TEMP = "combinedtemp";

    // thing configurations
    public static final String CONFIG_ALLOWWAKEUP = "allowWakeup";
    public static final String CONFIG_ENABLEEVENTS = "enableEvents";
    public static final String CONFIG_REFRESHTOKEN = "refreshToken";
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";

}
