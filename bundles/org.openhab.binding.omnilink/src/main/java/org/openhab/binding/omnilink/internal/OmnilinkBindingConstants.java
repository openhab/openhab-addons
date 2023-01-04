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
package org.openhab.binding.omnilink.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OmnilinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class OmnilinkBindingConstants {

    public static final String BINDING_ID = "omnilink";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_OMNI_AREA = new ThingTypeUID(BINDING_ID, "area");
    public static final ThingTypeUID THING_TYPE_LUMINA_AREA = new ThingTypeUID(BINDING_ID, "lumina_area");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");
    public static final ThingTypeUID THING_TYPE_UNIT_UPB = new ThingTypeUID(BINDING_ID, "upb");
    public static final ThingTypeUID THING_TYPE_UNIT = new ThingTypeUID(BINDING_ID, "unit");
    public static final ThingTypeUID THING_TYPE_DIMMABLE = new ThingTypeUID(BINDING_ID, "dimmable");
    public static final ThingTypeUID THING_TYPE_FLAG = new ThingTypeUID(BINDING_ID, "flag");
    public static final ThingTypeUID THING_TYPE_OUTPUT = new ThingTypeUID(BINDING_ID, "output");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_BUTTON = new ThingTypeUID(BINDING_ID, "button");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_AUDIO_ZONE = new ThingTypeUID(BINDING_ID, "audio_zone");
    public static final ThingTypeUID THING_TYPE_AUDIO_SOURCE = new ThingTypeUID(BINDING_ID, "audio_source");
    public static final ThingTypeUID THING_TYPE_CONSOLE = new ThingTypeUID(BINDING_ID, "console");
    public static final ThingTypeUID THING_TYPE_TEMP_SENSOR = new ThingTypeUID(BINDING_ID, "temp_sensor");
    public static final ThingTypeUID THING_TYPE_HUMIDITY_SENSOR = new ThingTypeUID(BINDING_ID, "humidity_sensor");

    // List of all Channel ids

    // zones
    public static final String CHANNEL_ZONE_CONTACT = "contact";
    public static final String CHANNEL_ZONE_CURRENT_CONDITION = "current_condition";
    public static final String CHANNEL_ZONE_LATCHED_ALARM_STATUS = "latched_alarm_status";
    public static final String CHANNEL_ZONE_ARMING_STATUS = "arming_status";
    public static final String CHANNEL_ZONE_BYPASS = "bypass";
    public static final String CHANNEL_ZONE_RESTORE = "restore";

    // areas
    public static final String CHANNEL_AREA_MODE = "mode";
    public static final String CHANNEL_AREA_ACTIVATE_KEYPAD_EMERGENCY = "activate_keypad_emergency";
    public static final String CHANNEL_AREA_ALARM_BURGLARY = "alarm_burglary";
    public static final String CHANNEL_AREA_ALARM_FIRE = "alarm_fire";
    public static final String CHANNEL_AREA_ALARM_GAS = "alarm_gas";
    public static final String CHANNEL_AREA_ALARM_AUXILIARY = "alarm_auxiliary";
    public static final String CHANNEL_AREA_ALARM_FREEZE = "alarm_freeze";
    public static final String CHANNEL_AREA_ALARM_WATER = "alarm_water";
    public static final String CHANNEL_AREA_ALARM_DURESS = "alarm_duress";
    public static final String CHANNEL_AREA_ALARM_TEMPERATURE = "alarm_temperature";

    public static final String CHANNEL_AREA_SECURITY_MODE_DISARM = "disarm";
    public static final String CHANNEL_AREA_SECURITY_MODE_DAY = "day";
    public static final String CHANNEL_AREA_SECURITY_MODE_NIGHT = "night";
    public static final String CHANNEL_AREA_SECURITY_MODE_AWAY = "away";
    public static final String CHANNEL_AREA_SECURITY_MODE_VACATION = "vacation";
    public static final String CHANNEL_AREA_SECURITY_MODE_DAY_INSTANT = "day_instant";
    public static final String CHANNEL_AREA_SECURITY_MODE_NIGHT_DELAYED = "night_delayed";

    public static final String CHANNEL_AREA_SECURITY_MODE_HOME = "home";
    public static final String CHANNEL_AREA_SECURITY_MODE_SLEEP = "sleep";
    public static final String CHANNEL_AREA_SECURITY_MODE_PARTY = "party";
    public static final String CHANNEL_AREA_SECURITY_MODE_SPECIAL = "special";

    // units
    public static final String CHANNEL_UNIT_LEVEL = "level";
    public static final String CHANNEL_UNIT_SWITCH = "switch";
    public static final String CHANNEL_UNIT_ON_FOR_SECONDS = "on_for_seconds";
    public static final String CHANNEL_UNIT_ON_FOR_MINUTES = "on_for_minutes";
    public static final String CHANNEL_UNIT_ON_FOR_HOURS = "on_for_hours";
    public static final String CHANNEL_UNIT_OFF_FOR_SECONDS = "off_for_seconds";
    public static final String CHANNEL_UNIT_OFF_FOR_MINUTES = "off_for_minutes";
    public static final String CHANNEL_UNIT_OFF_FOR_HOURS = "off_for_hours";
    public static final String CHANNEL_FLAG_VALUE = "value";
    public static final String CHANNEL_FLAG_SWITCH = "switch";
    public static final String CHANNEL_UPB_STATUS = "upb_status";

    public static final String CHANNEL_ROOM_SWITCH = "switch";
    public static final String CHANNEL_ROOM_SCENE_A = "scene_a";
    public static final String CHANNEL_ROOM_SCENE_B = "scene_b";
    public static final String CHANNEL_ROOM_SCENE_C = "scene_c";
    public static final String CHANNEL_ROOM_SCENE_D = "scene_d";
    public static final String CHANNEL_ROOM_STATE = "state";

    public static final String CHANNEL_SYSTEM_DATE = "system_date";
    public static final String CHANNEL_EVENT_LOG = "last_log";

    // buttons
    public static final String CHANNEL_BUTTON_PRESS = "press";

    // locks
    public static final String CHANNEL_LOCK_SWITCH = "switch";

    // thermostats
    public static final String CHANNEL_THERMO_FREEZE_ALARM = "freeze_alarm";
    public static final String CHANNEL_THERMO_COMM_FAILURE = "comm_failure";
    public static final String CHANNEL_THERMO_STATUS = "status";
    public static final String CHANNEL_THERMO_CURRENT_TEMP = "temperature";
    public static final String CHANNEL_THERMO_OUTDOOR_TEMP = "outdoor_temperature";
    public static final String CHANNEL_THERMO_HUMIDITY = "humidity";
    public static final String CHANNEL_THERMO_HUMIDIFY_SETPOINT = "humidify_setpoint";
    public static final String CHANNEL_THERMO_DEHUMIDIFY_SETPOINT = "dehumidify_setpoint";
    public static final String CHANNEL_THERMO_SYSTEM_MODE = "system_mode";
    public static final String CHANNEL_THERMO_FAN_MODE = "fan_mode";
    public static final String CHANNEL_THERMO_HOLD_STATUS = "hold_status";
    public static final String CHANNEL_THERMO_COOL_SETPOINT = "cool_setpoint";
    public static final String CHANNEL_THERMO_HEAT_SETPOINT = "heat_setpoint";

    // temp / humidity sensors
    public static final String CHANNEL_AUX_TEMP = "temperature";
    public static final String CHANNEL_AUX_HUMIDITY = "humidity";
    public static final String CHANNEL_AUX_LOW_SETPOINT = "low_setpoint";
    public static final String CHANNEL_AUX_HIGH_SETPOINT = "high_setpoint";

    // consoles
    public static final String CHANNEL_CONSOLE_BEEP = "beep";
    public static final String CHANNEL_CONSOLE_ENABLE_DISABLE_BEEPER = "enable_disable_beeper";

    // audio zones
    public static final String CHANNEL_AUDIO_ZONE_POWER = "zone_power";
    public static final String CHANNEL_AUDIO_ZONE_MUTE = "zone_mute";
    public static final String CHANNEL_AUDIO_ZONE_VOLUME = "zone_volume";
    public static final String CHANNEL_AUDIO_ZONE_SOURCE = "zone_source";
    public static final String CHANNEL_AUDIO_ZONE_CONTROL = "zone_control";

    // audio sources
    public static final String CHANNEL_AUDIO_SOURCE_TEXT1 = "source_text_1";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT2 = "source_text_2";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT3 = "source_text_3";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT4 = "source_text_4";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT5 = "source_text_5";
    public static final String CHANNEL_AUDIO_SOURCE_TEXT6 = "source_text_6";
    public static final String CHANNEL_AUDIO_SOURCE_POLLING = "polling";

    // trigger channels
    public static final String TRIGGER_CHANNEL_BUTTON_ACTIVATED_EVENT = "activated_event";
    public static final String TRIGGER_CHANNEL_PHONE_LINE_EVENT = "phone_line_event";
    public static final String TRIGGER_CHANNEL_AC_POWER_EVENT = "ac_power_event";
    public static final String TRIGGER_CHANNEL_BATTERY_EVENT = "battery_event";
    public static final String TRIGGER_CHANNEL_DCM_EVENT = "dcm_event";
    public static final String TRIGGER_CHANNEL_ENERGY_COST_EVENT = "energy_cost_event";
    public static final String TRIGGER_CHANNEL_CAMERA_TRIGGER_EVENT = "camera_trigger_event";
    public static final String TRIGGER_CHANNEL_ACCESS_CONTROL_READER_EVENT = "access_control_reader_event";
    public static final String TRIGGER_CHANNEL_AREA_ALL_ON_OFF_EVENT = "all_on_off_event";
    public static final String TRIGGER_CHANNEL_SWITCH_PRESS_EVENT = "switch_press_event";
    public static final String TRIGGER_CHANNEL_UPB_LINK_ACTIVATED_EVENT = "upb_link_activated_event";
    public static final String TRIGGER_CHANNEL_UPB_LINK_DEACTIVATED_EVENT = "upb_link_deactivated_event";

    // thing configuration and properties keys
    public static final String THING_PROPERTIES_NAME = "name";
    public static final String THING_PROPERTIES_NUMBER = "number";
    public static final String THING_PROPERTIES_AREA = "area";
    public static final String THING_PROPERTIES_AUTOSTART = "autostart";
    public static final String THING_PROPERTIES_PHONE_NUMBER = "phone_number";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OMNI_AREA,
            THING_TYPE_LUMINA_AREA, THING_TYPE_ZONE, THING_TYPE_BRIDGE, THING_TYPE_FLAG, THING_TYPE_ROOM,
            THING_TYPE_BUTTON, THING_TYPE_UNIT_UPB, THING_TYPE_THERMOSTAT, THING_TYPE_CONSOLE, THING_TYPE_AUDIO_ZONE,
            THING_TYPE_AUDIO_SOURCE, THING_TYPE_TEMP_SENSOR, THING_TYPE_HUMIDITY_SENSOR, THING_TYPE_LOCK,
            THING_TYPE_OUTPUT, THING_TYPE_UNIT, THING_TYPE_DIMMABLE);
    public static final Set<ThingTypeUID> SUPPORTED_UNIT_TYPES_UIDS = Set.of(THING_TYPE_UNIT_UPB, THING_TYPE_ROOM,
            THING_TYPE_FLAG, THING_TYPE_OUTPUT, THING_TYPE_DIMMABLE, THING_TYPE_UNIT);
}
