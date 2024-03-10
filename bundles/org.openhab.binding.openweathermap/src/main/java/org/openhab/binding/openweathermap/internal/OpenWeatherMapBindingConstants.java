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
package org.openhab.binding.openweathermap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link OpenWeatherMapBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapBindingConstants {

    public static final String BINDING_ID = "openweathermap";

    public static final String API = "api";
    public static final String LOCAL = "local";

    // Bridge
    public static final ThingTypeUID THING_TYPE_WEATHER_API = new ThingTypeUID(BINDING_ID, "weather-api");

    // Thing
    public static final ThingTypeUID THING_TYPE_WEATHER_AND_FORECAST = new ThingTypeUID(BINDING_ID,
            "weather-and-forecast");
    public static final ThingTypeUID THING_TYPE_AIR_POLLUTION = new ThingTypeUID(BINDING_ID, "air-pollution");
    // One Call API forecast
    public static final ThingTypeUID THING_TYPE_ONECALL_WEATHER_AND_FORECAST = new ThingTypeUID(BINDING_ID, "onecall");
    // One Call API historical data
    public static final ThingTypeUID THING_TYPE_ONECALL_HISTORY = new ThingTypeUID(BINDING_ID, "onecall-history");

    // List of all properties
    public static final String CONFIG_API_KEY = "apikey";
    public static final String CONFIG_LANGUAGE = "language";
    public static final String CONFIG_LOCATION = "location";

    // Channel group types
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_STATION = new ChannelGroupTypeUID(BINDING_ID, "station");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HOURLY_FORECAST = new ChannelGroupTypeUID(BINDING_ID,
            "hourlyForecast");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DAILY_FORECAST = new ChannelGroupTypeUID(BINDING_ID,
            "dailyForecast");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_AIR_POLLUTION_FORECAST = new ChannelGroupTypeUID(
            BINDING_ID, "airPollutionForecast");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_ONECALL_MINUTELY_FORECAST = new ChannelGroupTypeUID(
            BINDING_ID, "oneCallMinutely");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_ONECALL_HOURLY_FORECAST = new ChannelGroupTypeUID(
            BINDING_ID, "oneCallHourly");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_ONECALL_DAILY_FORECAST = new ChannelGroupTypeUID(
            BINDING_ID, "oneCallDaily");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_ONECALL_CURRENT = new ChannelGroupTypeUID(BINDING_ID,
            "oneCallCurrent");
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_ONECALL_ALERTS = new ChannelGroupTypeUID(BINDING_ID,
            "oneCallAlerts");

    // List of all channel groups
    public static final String CHANNEL_GROUP_STATION = "station";
    public static final String CHANNEL_GROUP_CURRENT_WEATHER = "current";
    public static final String CHANNEL_GROUP_FORECAST_TODAY = "forecastToday";
    public static final String CHANNEL_GROUP_FORECAST_TOMORROW = "forecastTomorrow";
    public static final String CHANNEL_GROUP_CURRENT_AIR_POLLUTION = "current";
    public static final String CHANNEL_GROUP_ONECALL_CURRENT = "current";
    public static final String CHANNEL_GROUP_ONECALL_HISTORY = "history";
    public static final String CHANNEL_GROUP_ONECALL_TODAY = "forecastToday";
    public static final String CHANNEL_GROUP_ONECALL_TOMORROW = "forecastTomorrow";

    // List of all channels
    public static final String CHANNEL_STATION_ID = "id";
    public static final String CHANNEL_STATION_NAME = "name";
    public static final String CHANNEL_STATION_LOCATION = "location";
    public static final String CHANNEL_TIME_STAMP = "time-stamp";
    public static final String CHANNEL_SUNRISE = "sunrise";
    public static final String CHANNEL_SUNSET = "sunset";
    public static final String CHANNEL_MOONRISE = "moonrise";
    public static final String CHANNEL_MOONSET = "moonset";
    public static final String CHANNEL_MOON_PHASE = "moon-phase";
    public static final String CHANNEL_CONDITION = "condition";
    public static final String CHANNEL_CONDITION_ID = "condition-id";
    public static final String CHANNEL_CONDITION_ICON = "icon";
    public static final String CHANNEL_CONDITION_ICON_ID = "icon-id";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_APPARENT_TEMPERATURE = "apparent-temperature";
    public static final String CHANNEL_APPARENT_MORNING = "apparent-morning";
    public static final String CHANNEL_APPARENT_DAY = "apparent-day";
    public static final String CHANNEL_APPARENT_EVENING = "apparent-evening";
    public static final String CHANNEL_APPARENT_NIGHT = "apparent-night";
    public static final String CHANNEL_MIN_TEMPERATURE = "min-temperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "max-temperature";
    public static final String CHANNEL_MORNING_TEMPERATURE = "morning-temperature";
    public static final String CHANNEL_DAY_TEMPERATURE = "day-temperature";
    public static final String CHANNEL_EVENING_TEMPERATURE = "evening-temperature";
    public static final String CHANNEL_NIGHT_TEMPERATURE = "night-temperature";
    public static final String CHANNEL_DEW_POINT = "dew-point";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_GUST_SPEED = "gust-speed";
    public static final String CHANNEL_CLOUDINESS = "cloudiness";
    public static final String CHANNEL_PRECIP_PROBABILITY = "precip-probability";
    public static final String CHANNEL_RAIN = "rain";
    public static final String CHANNEL_SNOW = "snow";
    public static final String CHANNEL_VISIBILITY = "visibility";
    public static final String CHANNEL_UVINDEX = "uvindex";
    public static final String CHANNEL_AIR_QUALITY_INDEX = "airQualityIndex";
    public static final String CHANNEL_PARTICULATE_MATTER_2_5 = "particulateMatter2dot5";
    public static final String CHANNEL_PARTICULATE_MATTER_10 = "particulateMatter10";
    public static final String CHANNEL_CARBON_MONOXIDE = "carbonMonoxide";
    public static final String CHANNEL_NITROGEN_MONOXIDE = "nitrogenMonoxide";
    public static final String CHANNEL_NITROGEN_DIOXIDE = "nitrogenDioxide";
    public static final String CHANNEL_OZONE = "ozone";
    public static final String CHANNEL_SULPHUR_DIOXIDE = "sulphurDioxide";
    public static final String CHANNEL_AMMONIA = "ammonia";
    public static final String CHANNEL_PRECIPITATION = "precipitation";
    public static final String CHANNEL_ALERT_EVENT = "event";
    public static final String CHANNEL_ALERT_DESCRIPTION = "description";
    public static final String CHANNEL_ALERT_ONSET = "onset";
    public static final String CHANNEL_ALERT_EXPIRES = "expires";
    public static final String CHANNEL_ALERT_SOURCE = "source";

    // List of all configuration
    public static final String CONFIG_FORECAST_DAYS = "forecastDays";
}
