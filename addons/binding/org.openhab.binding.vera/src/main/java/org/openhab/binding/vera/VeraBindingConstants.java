/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link VeraBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraBindingConstants {

    public static final String BINDING_ID = "vera";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "veraController");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "veraDevice");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "veraScene");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE,
            THING_TYPE_DEVICE, THING_TYPE_SCENE);

    // List of all Channel IDs
    public static final String BATTERY_CHANNEL = "battery";
    public static final String DOORLOCK_CHANNEL = "doorlock";
    public static final String SENSOR_BINARY_CHANNEL = "sensorBinary";
    public static final String SWITCH_BINARY_CHANNEL = "switchBinary";
    public static final String SWITCH_MULTILEVEL_CHANNEL = "switchMultilevel";
    public static final String SWITCH_COLOR_CHANNEL = "switchColor";

    // thermostat
    public static final String THERMOSTAT_MODE_CHANNEL = "thermostatMode";
    public static final String THERMOSTAT_SET_POINT_CHANNEL = "thermostatSetPoint";
    public static final String THERMOSTAT_MODE_CC_CHANNEL = "thermostatModeCC";

    // sensor multilevel
    public static final String SENSOR_TEMPERATURE_CHANNEL = "sensorTemperature";
    public static final String SENSOR_LUMINOSITY_CHANNEL = "sensorLuminosity";
    public static final String SENSOR_HUMIDITY_CHANNEL = "sensorHumidity";
    public static final String SENSOR_ULTRAVIOLET_CHANNEL = "sensorUltraviolet";
    public static final String SENSOR_ENERGY_CHANNEL = "sensorEnergy";

    // sensor multilevel (meter)
    public static final String SENSOR_METER_KWH_CHANNEL = "sensorMeterKWh";
    public static final String SENSOR_METER_W_CHANNEL = "sensorMeterW";

    // sensor binary
    public static final String SENSOR_SMOKE_CHANNEL = "sensorSmoke";
    public static final String SENSOR_CO_CHANNEL = "sensorCo";
    public static final String SENSOR_FLOOD_CHANNEL = "sensorFlood";
    public static final String SENSOR_DOOR_WINDOW_CHANNEL = "sensorDoorWindow";
    public static final String SENSOR_MOTION_CHANNEL = "sensorMotion";

    // switch multilevel
    public static final String SWITCH_ROLLERSHUTTER_CHANNEL = "switchBlinds";

    // special channels
    public static final String ACTIONS_CHANNEL = "actions";
    public static final String ACTIONS_CHANNEL_OPTION_REFRESH = "REFRESH";

    /* Bridge config properties */
    public static final String BRIDGE_CONFIG_VERA_SERVER_IP_ADDRESS = "veraIpAddress";
    public static final String BRIDGE_CONFIG_VERA_SERVER_PORT = "veraControllerPort";
    public static final String BRIDGE_CONFIG_POLLING_INTERVAL = "pollingInterval";
    public static final String BRIDGE_CONFIG_CLEAR_NAMES = "clearNames";
    public static final String BRIDGE_CONFIG_DEFAULT_ROOM_NAME = "defaultRoomName";

    public static final String DEVICE_CONFIG_ID = "deviceId";
    public static final String DEVICE_PROP_CATEGORY = "category";
    public static final String DEVICE_PROP_SUBCATEGORY = "subcategory";

    public static final String SCENE_CONFIG_ID = "sceneId";
}
