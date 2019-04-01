/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link DarkSkyBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyBindingConstants {

    public static final String BINDING_ID = "darksky";

    public static final String API = "api";
    public static final String LOCAL = "local";

    // Bridge
    public static final ThingTypeUID THING_TYPE_WEATHER_API = new ThingTypeUID(BINDING_ID, "weather-api");

    // Thing
    public static final ThingTypeUID THING_TYPE_WEATHER_AND_FORECAST = new ThingTypeUID(BINDING_ID,
            "weather-and-forecast");

    // List of all properties
    public static final String CONFIG_API_KEY = "apikey";
    public static final String CONFIG_LANGUAGE = "language";
    public static final String CONFIG_LOCATION = "location";

    // Channel group types
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HOURLY_FORECAST = new ChannelGroupTypeUID(BINDING_ID,
            "hourlyForecast");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DAILY_FORECAST = new ChannelGroupTypeUID(BINDING_ID,
            "dailyForecast");

    // List of all channel groups
    public static final String CHANNEL_GROUP_CURRENT_WEATHER = "current";
    public static final String CHANNEL_GROUP_FORECAST_TODAY = "forecastToday";
    public static final String CHANNEL_GROUP_FORECAST_TOMORROW = "forecastTomorrow";

    // List of all channels
    public static final String CHANNEL_TIME_STAMP = "time-stamp";
    public static final String CHANNEL_CONDITION = "condition";
    public static final String CHANNEL_CONDITION_ICON = "icon";
    public static final String CHANNEL_CONDITION_ICON_ID = "icon-id";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_MIN_TEMPERATURE = "min-temperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "max-temperature";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_GUST_SPEED = "gust-speed";
    public static final String CHANNEL_CLOUDINESS = "cloudiness";
    public static final String CHANNEL_VISIBILITY = "visibility";
    public static final String CHANNEL_RAIN = "rain";
    public static final String CHANNEL_SNOW = "snow";
    public static final String CHANNEL_PRECIPITATION_PROBABILITY = "precip-probability";
    public static final String CHANNEL_UVINDEX = "uvindex";
    public static final String CHANNEL_OZONE = "ozone";
    public static final String CHANNEL_SUNRISE = "sunrise";
    public static final String CHANNEL_SUNSET = "sunset";

    public static final String TRIGGER_SUNRISE = CHANNEL_GROUP_CURRENT_WEATHER + "#sunrise-event";
    public static final String TRIGGER_SUNSET = CHANNEL_GROUP_CURRENT_WEATHER + "#sunset-event";

    public static final String EVENT_START = "START";

    // List of all configuration
    public static final String CONFIG_FORECAST_DAYS = "forecastDays";
}
