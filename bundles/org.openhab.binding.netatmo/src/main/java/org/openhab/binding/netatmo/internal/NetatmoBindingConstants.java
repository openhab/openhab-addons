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
package org.openhab.binding.netatmo.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NetatmoBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NetatmoBindingConstants {

    public static final String BINDING_ID = "netatmo";
    public static final String VENDOR = "Netatmo";

    // Things properties
    public static final String PROPERTY_CITY = "city";
    public static final String PROPERTY_COUNTRY = "country";
    public static final String PROPERTY_TIMEZONE = "timezone";
    public static final String PROPERTY_FEATURE = "feature";

    // Channel group ids
    public static final String GROUP_LAST_EVENT = "last-event";
    public static final String GROUP_SUB_EVENT = "sub-event";
    public static final String GROUP_TEMPERATURE = "temperature";
    public static final String GROUP_HUMIDITY = "humidity";
    public static final String GROUP_AIR_QUALITY = "airquality";
    public static final String GROUP_NOISE = "noise";
    public static final String GROUP_PRESSURE = "pressure";
    public static final String GROUP_TIMESTAMP = "timestamp";
    public static final String GROUP_RAIN = "rain";
    public static final String GROUP_WIND = "wind";
    public static final String GROUP_ENERGY = "energy";
    public static final String GROUP_SIGNAL = "signal";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_SECURITY = "security";
    public static final String GROUP_CAM_STATUS = "status";
    public static final String GROUP_CAM_LIVE = "live";
    public static final String GROUP_PRESENCE = "presence";
    public static final String GROUP_SIREN = "siren";
    public static final String GROUP_PERSON = "person";
    public static final String GROUP_PROPERTIES = "properties";
    public static final String GROUP_SETPOINT = "setpoint";
    public static final String GROUP_LOCATION = "location";

    // Alternative extended groups
    public static final String OPTION_EXTENDED = "-extended";
    public static final String OPTION_OUTSIDE = "-outside";
    public static final String OPTION_DOORBELL = "-doorbell";
    public static final String OPTION_PERSON = "-person";
    public static final String OPTION_ROOM = "-room";
    public static final String OPTION_THERMOSTAT = "-thermostat";
    public static final String OPTION_SMOKE = "-smoke";
    public static final Set<String> GROUP_VARIATIONS = Set.of(OPTION_EXTENDED, OPTION_OUTSIDE, OPTION_DOORBELL,
            OPTION_PERSON, OPTION_ROOM, OPTION_THERMOSTAT, OPTION_SMOKE);

    public static final String GROUP_TYPE_TIMESTAMP_EXTENDED = GROUP_TIMESTAMP + OPTION_EXTENDED;
    public static final String GROUP_TYPE_BATTERY_EXTENDED = GROUP_BATTERY + OPTION_EXTENDED;
    public static final String GROUP_TYPE_PRESSURE_EXTENDED = GROUP_PRESSURE + OPTION_EXTENDED;
    public static final String GROUP_TYPE_TEMPERATURE_EXTENDED = GROUP_TEMPERATURE + OPTION_EXTENDED;
    public static final String GROUP_TYPE_AIR_QUALITY_EXTENDED = GROUP_AIR_QUALITY + OPTION_EXTENDED;
    public static final String GROUP_TYPE_TEMPERATURE_OUTSIDE = GROUP_TEMPERATURE + OPTION_OUTSIDE;
    public static final String GROUP_DOORBELL_STATUS = GROUP_CAM_STATUS + OPTION_DOORBELL;
    public static final String GROUP_DOORBELL_LIVE = GROUP_CAM_LIVE + OPTION_DOORBELL;
    public static final String GROUP_DOORBELL_LAST_EVENT = GROUP_LAST_EVENT + OPTION_DOORBELL;
    public static final String GROUP_DOORBELL_SUB_EVENT = GROUP_SUB_EVENT + OPTION_DOORBELL;
    public static final String GROUP_PERSON_LAST_EVENT = GROUP_LAST_EVENT + OPTION_PERSON;
    public static final String GROUP_SMOKE_LAST_EVENT = GROUP_LAST_EVENT + OPTION_SMOKE;
    public static final String GROUP_TYPE_ROOM_TEMPERATURE = GROUP_TEMPERATURE + OPTION_ROOM;
    public static final String GROUP_TYPE_ROOM_PROPERTIES = GROUP_PROPERTIES + OPTION_ROOM;
    public static final String GROUP_TYPE_TH_PROPERTIES = GROUP_PROPERTIES + OPTION_THERMOSTAT;

    // Channel ids
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_TREND = "trend";
    public static final String CHANNEL_MAX_TIME = "max-time";
    public static final String CHANNEL_MIN_TIME = "min-time";
    public static final String CHANNEL_MAX_VALUE = "max-today";
    public static final String CHANNEL_MIN_VALUE = "min-today";
    public static final String CHANNEL_HUMIDEX = "humidex";
    public static final String CHANNEL_CO2 = "co2";
    public static final String CHANNEL_HEALTH_INDEX = "health-index";
    public static final String CHANNEL_HUMIDEX_SCALE = "humidex-scale";
    public static final String CHANNEL_DEWPOINT = "dewpoint";
    public static final String CHANNEL_DEWPOINT_DEP = "dewpoint-depression";
    public static final String CHANNEL_HEAT_INDEX = "heat-index";
    public static final String CHANNEL_ABSOLUTE_PRESSURE = "absolute";
    public static final String CHANNEL_LAST_SEEN = "last-seen";
    public static final String CHANNEL_MEASURES_TIMESTAMP = "measures";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_BATTERY_STATUS = "status";
    public static final String CHANNEL_SIGNAL_STRENGTH = "strength";
    public static final String CHANNEL_SUM_RAIN1 = "sum-1";
    public static final String CHANNEL_SUM_RAIN24 = "sum-24";
    public static final String CHANNEL_WIND_ANGLE = "angle";
    public static final String CHANNEL_STATUS = GROUP_CAM_STATUS;
    public static final String CHANNEL_WIND_STRENGTH = "strength";
    public static final String CHANNEL_MAX_WIND_STRENGTH = "max-strength";
    public static final String CHANNEL_DATE_MAX_WIND_STRENGTH = "max-strength-date";
    public static final String CHANNEL_GUST_ANGLE = "gust-angle";
    public static final String CHANNEL_GUST_STRENGTH = "gust-strength";
    public static final String CHANNEL_SETPOINT_MODE = "mode";
    public static final String CHANNEL_SETPOINT_START_TIME = "start";
    public static final String CHANNEL_SETPOINT_END_TIME = "end";
    public static final String CHANNEL_THERM_RELAY = "relay";
    public static final String CHANNEL_ANTICIPATING = "anticipating";
    public static final String CHANNEL_ROOM_WINDOW_OPEN = "window-open";
    public static final String CHANNEL_ROOM_HEATING_POWER = "heating-power-request";
    public static final String CHANNEL_PLANNING = "planning";
    public static final String CHANNEL_PERSON_COUNT = "person-count";
    public static final String CHANNEL_UNKNOWN_PERSON_COUNT = "unknown-person-count";
    public static final String CHANNEL_UNKNOWN_PERSON_PICTURE = "unknown-person-picture";
    public static final String CHANNEL_MONITORING = "monitoring";
    public static final String CHANNEL_SD_CARD = "sd-card";
    public static final String CHANNEL_ALIM_STATUS = "alim";
    public static final String CHANNEL_LIVEPICTURE = "picture";
    public static final String CHANNEL_LIVEPICTURE_VPN_URL = "vpn-picture-url";
    public static final String CHANNEL_LIVEPICTURE_LOCAL_URL = "local-picture-url";
    public static final String CHANNEL_LIVESTREAM_VPN_URL = "vpn-stream-url";
    public static final String CHANNEL_LIVESTREAM_LOCAL_URL = "local-stream-url";
    public static final String CHANNEL_EVENT_TYPE = "type";
    public static final String CHANNEL_EVENT_SUBTYPE = "subtype";
    public static final String CHANNEL_EVENT_VIDEO_STATUS = "video-status";
    public static final String CHANNEL_EVENT_MESSAGE = "message";
    public static final String CHANNEL_EVENT_TIME = "time";
    public static final String CHANNEL_EVENT_SNAPSHOT = "snapshot";
    public static final String CHANNEL_EVENT_SNAPSHOT_URL = "snapshot-url";
    public static final String CHANNEL_EVENT_VIGNETTE = "vignette";
    public static final String CHANNEL_EVENT_VIGNETTE_URL = "vignette-url";
    public static final String CHANNEL_EVENT_VIDEO_VPN_URL = "vpn-video-url";
    public static final String CHANNEL_EVENT_VIDEO_LOCAL_URL = "local-video-url";
    public static final String CHANNEL_EVENT_PERSON_ID = "person-id";
    public static final String CHANNEL_EVENT_CAMERA_ID = "camera-id";
    public static final String CHANNEL_PERSON_AT_HOME = "at-home";
    public static final String CHANNEL_PERSON_AVATAR = "avatar";
    public static final String CHANNEL_PERSON_AVATAR_URL = "avatar-url";
    public static final String CHANNEL_HOME_EVENT = "home-event";
    public static final String CHANNEL_SETPOINT_DURATION = "setpoint-duration";
    public static final String CHANNEL_FLOODLIGHT = "floodlight";
}
