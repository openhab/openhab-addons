/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TeslaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
public class TeslaBindingConstants {

    // REST URI constants
    public static final String API_NAME = "Tesla Client API";
    public static final String API_VERSION = "api/1/";
    public static final String TESLA_OWNERS_URI = "https://owner-api.teslamotors.com/";
    public static final String TESLA_ACCESS_TOKEN_URI = "oauth/token";
    public static final String TESLA_EVENT_URI = "https://streaming.vn.teslamotors.com/stream/";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String VIN = "vin";
    public static final String VEHICLES = "vehicles";
    public static final String COMMAND_PATH = "command/{cmd}";
    public static final String DATA_REQUEST_PATH = "data_request/{cmd}";
    public static final String VEHICLE_ID_PATH = "/{vid}/";

    // Tesla REST API commands
    public static final String TESLA_DATA_THROTTLE = "datathrottle";
    public static final String TESLA_COMMAND_THROTTLE = "commandthrottle";
    public static final String TESLA_COMMAND_SET_CHARGE_LIMIT = "set_charge_limit";
    public static final String TESLA_COMMAND_WAKE_UP = "wake_up";
    public static final String TESLA_COMMAND_OPEN_CHARGE_PORT = "charge_port_door_open";
    public static final String TESLA_COMMAND_CHARGE_START = "charge_start";
    public static final String TESLA_COMMAND_CHARGE_STOP = "charge_stop";
    public static final String TESLA_COMMAND_CHARGE_STD = "charge_standard";
    public static final String TESLA_COMMAND_CHARGE_MAX = "charge_max_range";
    public static final String TESLA_COMMAND_FLASH_LIGHTS = "flash_lights";
    public static final String TESLA_COMMAND_HONK_HORN = "honk_horn";
    public static final String TESLA_COMMAND_DOOR_LOCK = "door_lock";
    public static final String TESLA_COMMAND_DOOR_UNLOCK = "door_unlock";
    public static final String TESLA_COMMAND_SET_TEMP = "set_temps";
    public static final String TESLA_COMMAND_AUTO_COND_START = "auto_conditioning_start";
    public static final String TESLA_COMMAND_AUTO_COND_STOP = "auto_conditioning_stop";
    public static final String TESLA_COMMAND_SUN_ROOF = "sun_roof_control";

    // Tesla REST API vehicle states
    public static final String TESLA_DRIVE_STATE = "drive_state";
    public static final String TESLA_CHARGE_STATE = "charge_state";
    public static final String TESLA_CLIMATE_STATE = "climate_state";
    public static final String TESLA_VEHICLE_STATE = "vehicle_state";
    public static final String TESLA_GUI_STATE = "gui_settings";
    public static final String TESLA_MOBILE_ENABLED_STATE = "mobile_enabled";

    public static final String BINDING_ID = "tesla";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MODELS = new ThingTypeUID(BINDING_ID, "models");

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

    // List of all Channels
    public static final String CHANNEL_CHARGE = "charge";

}