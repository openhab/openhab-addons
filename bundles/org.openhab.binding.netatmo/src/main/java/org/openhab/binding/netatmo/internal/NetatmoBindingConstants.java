/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NetatmoBindingConstants {

    public static final String BINDING_ID = "netatmo";
    public static final String SERVICE_PID = "org.openhab.binding." + BINDING_ID;
    public static final String VENDOR = "Netatmo";

    // Configuration keys
    public static final String EQUIPMENT_ID = "id";

    // Things properties
    public static final String PROPERTY_MAX_EVENT_TIME = "last-event";

    // Channel group ids
    public static final String GROUP_TEMPERATURE = "temperature";
    public static final String GROUP_HUMIDITY = "humidity";
    public static final String GROUP_CO2 = "co2";
    public static final String GROUP_NOISE = "noise";
    public static final String GROUP_PRESSURE = "pressure";
    public static final String GROUP_DEVICE = "device";
    public static final String GROUP_LOCATION = "location";
    public static final String GROUP_RAIN = "rain";
    public static final String GROUP_WIND = "wind";
    public static final String GROUP_HEALTH = "health";
    public static final String GROUP_HOME_ENERGY = "energy";
    public static final String GROUP_SIGNAL = "signal";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_ENERGY_BATTERY = "energy-battery";
    public static final String GROUP_HOME_SECURITY = "home-security";
    public static final String GROUP_WELCOME = "welcome";
    public static final String GROUP_PRESENCE = "presence";
    public static final String GROUP_WELCOME_EVENT = "welcome-event";
    public static final String GROUP_PERSON = "person";
    public static final String GROUP_PERSON_EVENT = "person-event";
    public static final String GROUP_ROOM_TEMPERATURE = "room-temperature";
    public static final String GROUP_ROOM_PROPERTIES = "room-properties";
    public static final String GROUP_TH_PROPERTIES = "th-properties";
    public static final String GROUP_TH_SETPOINT = "setpoint";

    // Channel ids
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_TREND = "trend";
    public static final String CHANNEL_MAX_TIME = "max-time";
    public static final String CHANNEL_MIN_TIME = "min-time";
    public static final String CHANNEL_MAX_VALUE = "max-today";
    public static final String CHANNEL_MIN_VALUE = "min-today";
    public static final String CHANNEL_HUMIDEX = "humidex";
    public static final String CHANNEL_HUMIDEX_SCALE = "humidex-scale";
    public static final String CHANNEL_DEWPOINT = "dewpoint";
    public static final String CHANNEL_DEWPOINT_DEP = "dewpoint-depression";
    public static final String CHANNEL_HEAT_INDEX = "heat-index";
    public static final String CHANNEL_ABSOLUTE_PRESSURE = "absolute";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_LAST_SEEN = "last-seen";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_BATTERY_STATUS = "status";
    public static final String CHANNEL_SIGNAL_STRENGTH = "strength";
    public static final String CHANNEL_SUM_RAIN1 = "sum-1";
    public static final String CHANNEL_SUM_RAIN24 = "sum-24";

    public static final String CHANNEL_WIND_ANGLE = "angle";
    public static final String CHANNEL_WIND_STRENGTH = "strength";
    public static final String CHANNEL_MAX_WIND_STRENGTH = "max-strength";
    public static final String CHANNEL_DATE_MAX_WIND_STRENGTH = "max-strength-date";
    public static final String CHANNEL_GUST_ANGLE = "gust-angle";
    public static final String CHANNEL_GUST_STRENGTH = "gust-strength";
    public static final String CHANNEL_SETPOINT_MODE = "mode";
    public static final String CHANNEL_SETPOINT_START_TIME = "start";
    public static final String CHANNEL_SETPOINT_END_TIME = "end";
    public static final String CHANNEL_THERM_RELAY = "relay-status";

    public static final String CHANNEL_ANTICIPATING = "anticipating";
    public static final String CHANNEL_ROOM_WINDOW_OPEN = "window-open";
    public static final String CHANNEL_ROOM_HEATING_POWER = "heating-power-request";
    public static final String CHANNEL_PLANNING = "planning";
    public static final String CHANNEL_HOME_CITY = "city";
    public static final String CHANNEL_HOME_COUNTRY = "country";
    public static final String CHANNEL_HOME_TIMEZONE = "timezone";
    public static final String CHANNEL_HOME_PERSONCOUNT = "person-count";
    public static final String CHANNEL_HOME_UNKNOWNCOUNT = "unknown-count";
    public static final String CHANNEL_CAMERA_IS_MONITORING = "is-monitoring";
    public static final String CHANNEL_CAMERA_SDSTATUS = "sd-status";
    public static final String CHANNEL_CAMERA_ALIMSTATUS = "alim-status";
    public static final String CHANNEL_CAMERA_LIVEPICTURE = "live-picture";
    public static final String CHANNEL_CAMERA_LIVEPICTURE_URL = "live-picture-url";
    public static final String CHANNEL_CAMERA_LIVESTREAM_URL = "live-stream-url";
    public static final String CHANNEL_EVENT_TYPE = "type";
    public static final String CHANNEL_EVENT_SUBTYPE = "subtype";
    public static final String CHANNEL_EVENT_VIDEO_STATUS = "video-status";
    public static final String CHANNEL_EVENT_MESSAGE = "message";
    public static final String CHANNEL_EVENT_TIME = "time";
    public static final String CHANNEL_EVENT_SNAPSHOT = "snapshot";
    public static final String CHANNEL_EVENT_SNAPSHOT_URL = "snapshot-url";
    public static final String CHANNEL_EVENT_VIDEO_URL = "video-url";
    public static final String CHANNEL_EVENT_PERSON_ID = "person-id";
    public static final String CHANNEL_EVENT_CAMERA_ID = "camera-id";
    public static final String CHANNEL_PERSON_AT_HOME = "at-home";
    public static final String CHANNEL_PERSON_AVATAR = "avatar";
    public static final String CHANNEL_PERSON_AVATAR_URL = "avatar-url";

    public static final String CHANNEL_HOME_EVENT = "home-event";
    public static final String CHANNEL_SETPOINT_DURATION = "setpoint-duration";

    // Presence outdoor camera specific channels
    public static final String CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE = "auto-mode";
    public static final String CHANNEL_CAMERA_FLOODLIGHT = "floodlight";

    // URI for the EventServlet
    public static final String NETATMO_CALLBACK_URI = "/netatmo";
}
