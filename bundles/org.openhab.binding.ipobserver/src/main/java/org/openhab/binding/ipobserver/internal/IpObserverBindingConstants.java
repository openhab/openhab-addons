/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ipobserver.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IpObserverBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpObserverBindingConstants {
    public static final String BINDING_ID = "ipobserver";
    public static final String REBOOT_URL = "/msgreboot.htm";
    public static final String LIVE_DATA_URL = "/livedata.htm";
    public static final String SERVER_UPDATE_URL = "/weatherstation/updateweatherstation.php";
    public static final String STATION_SETTINGS_URL = "/station.htm";
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_WEATHER_STATION = new ThingTypeUID(BINDING_ID, "weatherstation");

    // List of all Channel ids
    public static final String TEMP_INDOOR = "temperatureIndoor";
    public static final String TEMP_OUTDOOR = "temperatureOutdoor";
    public static final String TEMP_WIND_CHILL = "temperatureWindChill";
    public static final String TEMP_DEW_POINT = "temperatureDewPoint";
    public static final String INDOOR_HUMIDITY = "humidityIndoor";
    public static final String OUTDOOR_HUMIDITY = "humidityOutdoor";
    public static final String ABS_PRESSURE = "pressureAbsolute";
    public static final String REL_PRESSURE = "pressureRelative";
    public static final String WIND_DIRECTION = "windDirection";
    public static final String WIND_AVERAGE_SPEED = "windAverageSpeed";
    public static final String WIND_SPEED = "windSpeed";
    public static final String WIND_GUST = "windGust";
    public static final String WIND_MAX_GUST = "windMaxGust";
    public static final String SOLAR_RADIATION = "solarRadiation";
    public static final String UV = "uv";
    public static final String UV_INDEX = "uvIndex";
    public static final String HOURLY_RAIN_RATE = "rainHourlyRate";
    public static final String DAILY_RAIN = "rainToday";
    public static final String WEEKLY_RAIN = "rainForWeek";
    public static final String MONTHLY_RAIN = "rainForMonth";
    public static final String YEARLY_RAIN = "rainForYear";
    public static final String TOTAL_RAIN = "rainTotal";
    public static final String INDOOR_BATTERY = "batteryIndoor";
    public static final String OUTDOOR_BATTERY = "batteryOutdoor";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String LAST_UPDATED_TIME = "lastUpdatedTime";
}
