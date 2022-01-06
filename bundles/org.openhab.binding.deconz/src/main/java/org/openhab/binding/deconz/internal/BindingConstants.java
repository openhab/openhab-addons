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
package org.openhab.binding.deconz.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {
    public static final String BINDING_ID = "deconz";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "deconz");

    // sensors
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "presencesensor");
    public static final ThingTypeUID THING_TYPE_POWER_SENSOR = new ThingTypeUID(BINDING_ID, "powersensor");
    public static final ThingTypeUID THING_TYPE_CONSUMPTION_SENSOR = new ThingTypeUID(BINDING_ID, "consumptionsensor");
    public static final ThingTypeUID THING_TYPE_DAYLIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "daylightsensor");
    public static final ThingTypeUID THING_TYPE_COLOR_CONTROL = new ThingTypeUID(BINDING_ID, "colorcontrol");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "lightsensor");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "temperaturesensor");
    public static final ThingTypeUID THING_TYPE_HUMIDITY_SENSOR = new ThingTypeUID(BINDING_ID, "humiditysensor");
    public static final ThingTypeUID THING_TYPE_PRESSURE_SENSOR = new ThingTypeUID(BINDING_ID, "pressuresensor");
    public static final ThingTypeUID THING_TYPE_OPENCLOSE_SENSOR = new ThingTypeUID(BINDING_ID, "openclosesensor");
    public static final ThingTypeUID THING_TYPE_WATERLEAKAGE_SENSOR = new ThingTypeUID(BINDING_ID,
            "waterleakagesensor");
    public static final ThingTypeUID THING_TYPE_FIRE_SENSOR = new ThingTypeUID(BINDING_ID, "firesensor");
    public static final ThingTypeUID THING_TYPE_ALARM_SENSOR = new ThingTypeUID(BINDING_ID, "alarmsensor");
    public static final ThingTypeUID THING_TYPE_VIBRATION_SENSOR = new ThingTypeUID(BINDING_ID, "vibrationsensor");
    public static final ThingTypeUID THING_TYPE_BATTERY_SENSOR = new ThingTypeUID(BINDING_ID, "batterysensor");
    public static final ThingTypeUID THING_TYPE_CARBONMONOXIDE_SENSOR = new ThingTypeUID(BINDING_ID,
            "carbonmonoxidesensor");
    public static final ThingTypeUID THING_TYPE_AIRQUALITY_SENSOR = new ThingTypeUID(BINDING_ID, "airqualitysensor");
    // Special sensor - Thermostat
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // lights
    public static final ThingTypeUID THING_TYPE_ONOFF_LIGHT = new ThingTypeUID(BINDING_ID, "onofflight");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmablelight");
    public static final ThingTypeUID THING_TYPE_COLOR_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID,
            "colortemperaturelight");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "colorlight");
    public static final ThingTypeUID THING_TYPE_EXTENDED_COLOR_LIGHT = new ThingTypeUID(BINDING_ID,
            "extendedcolorlight");
    public static final ThingTypeUID THING_TYPE_WINDOW_COVERING = new ThingTypeUID(BINDING_ID, "windowcovering");
    public static final ThingTypeUID THING_TYPE_WARNING_DEVICE = new ThingTypeUID(BINDING_ID, "warningdevice");
    public static final ThingTypeUID THING_TYPE_DOORLOCK = new ThingTypeUID(BINDING_ID, "doorlock");

    // groups
    public static final ThingTypeUID THING_TYPE_LIGHTGROUP = new ThingTypeUID(BINDING_ID, "lightgroup");

    // sensor channel ids
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_LAST_UPDATED = "last_updated";
    public static final String CHANNEL_LAST_SEEN = "last_seen";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_LIGHT_LUX = "lightlux";
    public static final String CHANNEL_LIGHT_LEVEL = "light_level";
    public static final String CHANNEL_DARK = "dark";
    public static final String CHANNEL_DAYLIGHT = "daylight";
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_BUTTONEVENT = "buttonevent";
    public static final String CHANNEL_GESTURE = "gesture";
    public static final String CHANNEL_GESTUREEVENT = "gestureevent";
    public static final String CHANNEL_OPENCLOSE = "open";
    public static final String CHANNEL_WATERLEAKAGE = "waterleakage";
    public static final String CHANNEL_FIRE = "fire";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_TAMPERED = "tampered";
    public static final String CHANNEL_VIBRATION = "vibration";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_CARBONMONOXIDE = "carbonmonoxide";
    public static final String CHANNEL_AIRQUALITY = "airquality";
    public static final String CHANNEL_AIRQUALITYPPB = "airqualityppb";
    public static final String CHANNEL_HEATSETPOINT = "heatsetpoint";
    public static final String CHANNEL_THERMOSTAT_MODE = "mode";
    public static final String CHANNEL_TEMPERATURE_OFFSET = "offset";
    public static final String CHANNEL_VALVE_POSITION = "valve";
    public static final String CHANNEL_WINDOWOPEN = "windowopen";

    // group + light channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_ALERT = "alert";
    public static final String CHANNEL_ALL_ON = "all_on";
    public static final String CHANNEL_ANY_ON = "any_on";
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_EFFECT_SPEED = "effectSpeed";
    public static final String CHANNEL_SCENE = "scene";
    public static final String CHANNEL_ONTIME = "ontime";

    // Thing configuration
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_HTTP_PORT = "httpPort";
    public static final String CONFIG_APIKEY = "apikey";
    public static final String PROPERTY_UDN = "UDN";
    public static final String CONFIG_ID = "id";
    public static final String UNIQUE_ID = "uid";

    public static final String PROPERTY_CT_MIN = "ctmin";
    public static final String PROPERTY_CT_MAX = "ctmax";

    // CT value range according to ZCL Spec
    public static final int ZCL_CT_UNDEFINED = 0; // 0x0000
    public static final int ZCL_CT_MIN = 1;
    public static final int ZCL_CT_MAX = 65279; // 0xFEFF
    public static final int ZCL_CT_INVALID = 65535; // 0xFFFF

    public static final double HUE_FACTOR = 65535 / 360.0;
    public static final int BRIGHTNESS_MIN = 0;
    public static final int BRIGHTNESS_MAX = 254;
    public static final double BRIGHTNESS_FACTOR = BRIGHTNESS_MAX / PercentType.HUNDRED.doubleValue();
}
