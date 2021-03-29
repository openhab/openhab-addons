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
    public static final String STATION_SETTINGS_URL = "/station.htm";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_WEATHER_STATION = new ThingTypeUID(BINDING_ID, "weatherstation");

    // List of all Channel ids
    public static final String INDOOR_TEMP = "indoor_temp";
    public static final String OUTDOOR_TEMP = "outdoorTemperature";
    public static final String INDOOR_HUMIDITY = "indoorHumidity";
    public static final String OUTDOOR_HUMIDITY = "outdoorHumidity";
    public static final String ABS_PRESSURE = "abs_pressure";
    public static final String REL_PRESSURE = "rel_pressure";
    public static final String WIND_DIRECTION = "wind_direction";
    public static final String WIND_SPEED = "wind_speed";
    public static final String WIND_SPEED2 = "wind_speed2";
    public static final String WIND_GUST = "wind_gust";
    public static final String DAILY_GUST = "daily_gust";
    public static final String SOLAR_RADIATION = "solar_radiation";
    public static final String UV = "uv";
    public static final String UVI = "uvi";
    public static final String HOURLY_RAIN = "hourly_rain";
    public static final String DAILY_RAIN = "daily_rain";
    public static final String WEEKLY_RAIN = "weekly_rain";
    public static final String MONTHLY_RAIN = "monthly_rain";
    public static final String YEARLY_RAIN = "yearly_rain";
    public static final String INDOOR_BATTERY = "indoorBattery";
    public static final String BATTERY_OUT = "battery_out";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String REBOOT = "reboot";
    public static final String LAST_UPDATED_TIME = "lastUpdatedTime";
}
