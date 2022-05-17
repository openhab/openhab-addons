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
package org.openhab.binding.ws980wifi.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WS980WiFiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Joerg Dokupil - Initial contribution
 */
@NonNullByDefault
public class WS980WiFiBindingConstants {

    private static final String BINDING_ID = "ws980wifi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WS980WIFI = new ThingTypeUID(BINDING_ID, "ws980wifi");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WS980WIFI);

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE_INSIDE = "tempInside";
    public static final String CHANNEL_TEMPERATURE_OUTSIDE = "tempOutside";
    public static final String CHANNEL_TEMPERATURE_DEWPOINT = "tempDewPoint";
    public static final String CHANNEL_TEMPERATURE_WINDCHILL = "tempWindChill";
    public static final String CHANNEL_TEMPERATURE_HEATINDEX = "heatIndex";
    public static final String CHANNEL_HUMIDITY_INSIDE = "humidityInside";
    public static final String CHANNEL_HUMIDITY_OUTSIDE = "humidityOutside";
    public static final String CHANNEL_PRESSURE_ABSOLUT = "pressureAbsolut";
    public static final String CHANNEL_PRESSURE_RELATIVE = "pressureRelative";
    public static final String CHANNEL_WIND_DIRECTION = "windDirection";
    public static final String CHANNEL_WINDSPEED = "windSpeed";
    public static final String CHANNEL_WINDSPEED_GUST = "windSpeedGust";
    public static final String CHANNEL_RAIN_LAST_HOUR = "rainLastHour";
    public static final String CHANNEL_RAIN_LAST_DAY = "rainLastDay";
    public static final String CHANNEL_RAIN_LAST_WEEK = "rainLastWeek";
    public static final String CHANNEL_RAIN_LAST_MONTH = "rainLastMonth";
    public static final String CHANNEL_RAIN_LAST_YEAR = "rainLastYear";
    public static final String CHANNEL_RAIN_TOTAL = "rainTotal";
    public static final String CHANNEL_LIGTH_LEVEL = "LightLevel";
    public static final String CHANNEL_UV_RAW = "uvRaw";
    public static final String CHANNEL_UV_INDEX = "uvIndex";

    // Config properties
    public static final String HOST = "host";
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String DESCRIPTION = "description";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String MAC_ADDRESS = "macAddress";
}
