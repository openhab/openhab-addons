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
package org.openhab.binding.touchwand.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TouchWandBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandBindingConstants {

    public static final String BINDING_ID = "touchwand";
    public static final String DISCOVERY_THREAD_ID = "OH-binding-touchwand-discovery";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_SHUTTER = new ThingTypeUID(BINDING_ID, "shutter");
    public static final ThingTypeUID THING_TYPE_WALLCONTROLLER = new ThingTypeUID(BINDING_ID, "wallcontroller");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_ALARMSENSOR = new ThingTypeUID(BINDING_ID, "alarmsensor");
    public static final ThingTypeUID THING_TYPE_BSENSOR = new ThingTypeUID(BINDING_ID, "bsensor");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SHUTTER = "shutter";
    public static final String CHANNEL_DIMMER = "brightness";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_WALLCONTROLLER_ACTION = "wallaction";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_LEAK = "leak";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_ILLUMINATION = "illumination";
    public static final String CHANNEL_DOORWINDOW = "isOpen";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_THERMOSTAT_STATE = "state";
    public static final String CHANNEL_THERMOSTAT_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_THERMOSTAT_ROOM_TEMPERATURE = "roomTemperature";
    public static final String CHANNEL_THERMOSTAT_MODE = "mode";
    public static final String CHANNEL_THERMOSTAT_FAN_LEVEL = "fanLevel";
    public static final String CHANNEL_SMOKE = "smoke";

    // List of configuration parameters

    public static final String HOST = "ipAddress";
    public static final String PORT = "port";
    public static final String USER = "username";
    public static final String PASS = "password";
    public static final String STATUS_REFRESH_TIME = "statusrefresh";
    public static final String ADD_SECONDARY_UNITS = "addSecondaryUnits";

    // Unit handler properties

    public static final String HANDLER_PROPERTIES_ID = "id";
    public static final String HANDLER_PROPERTIES_NAME = "name";

    // Connectivity options

    public static final String CONNECTIVITY_KNX = "knx";
    public static final String CONNECTIVITY_ZWAVE = "zwave";
    public static final String CONNECTIVITY_RISCO = "risco";
    public static final String CONNECTIVITY_PIMA = "pima";
    public static final String CONNECTIVITY_ACWAND = "acwand";

    // commands

    public static final String SWITCH_STATUS_ON = "255";
    public static final String SWITCH_STATUS_OFF = "0";

    // thermostat commands
    public static final String THERMOSTAT_STATE_ON = "1";
    public static final String THERMOSTAT_STATE_OFF = "0";
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SWITCH);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SHUTTER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_WALLCONTROLLER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_DIMMER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ALARMSENSOR);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_BSENSOR);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_THERMOSTAT);
    }

    public static final String TYPE_WALLCONTROLLER = "WallController";
    public static final String TYPE_SWITCH = "Switch";
    public static final String TYPE_SHUTTER = "shutter";
    public static final String TYPE_DIMMER = "dimmer";
    public static final String TYPE_ALARMSENSOR = "AlarmSensor";
    public static final String TYPE_BSENSOR = "bsensor";
    public static final String TYPE_THERMOSTAT = "thermostat";
    public static final String TYPE_UNKNOWN = "unknown";

    public static final int SENSOR_TYPE_TEMPERATURE = 1;
    public static final int SENSOR_TYPE_LUMINANCE = 3;
    public static final int SENSOR_TYPE_LEAK = 6;
    public static final int SENSOR_TYPE_DOOR_WINDOW = 10;
    public static final int SENSOR_TYPE_MOTION = 12;

    // bsensor currStatus options

    public static final String BSENSOR_STATUS_OPEN = "open";
    public static final String BSENSOR_STATUS_CLOSE = "close";

    public static final String BSENSOR_SUBTYPE_DOORWINDOW = "doors &windows";
    public static final String BSENSOR_SUBTYPE_MOTION = "motion";
    public static final String BSENSOR_SUBTYPE_SMOKE = "smoke";

    public static final String[] SUPPORTED_TOUCHWAND_TYPES = { TYPE_WALLCONTROLLER, TYPE_SWITCH, TYPE_SHUTTER,
            TYPE_DIMMER, TYPE_ALARMSENSOR, TYPE_BSENSOR, TYPE_THERMOSTAT };
}
