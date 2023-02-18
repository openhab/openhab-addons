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
package org.openhab.binding.metofficedatahub.internal;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link MetOfficeDataHubBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubBindingConstants {

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting()
            .disableHtmlEscaping().serializeNulls().create();

    private static final String BINDING_ID = "metofficedatahub";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SITE_SPEC_API = new ThingTypeUID(BINDING_ID, "siteSpecificApi");

    /**
     * Site Specific API - Shared
     */

    public static final String SITE_TIMESTAMP = "forecastTimestamp";

    /**
     * Site Specific API - Hourly Forecast Channel Names
     */
    public static final String SITE_HOURLY_FORECAST_SCREEN_TEMPERATURE = "siteScreenTemperature";

    public static final String SITE_HOURLY_FORECAST_MIN_SCREEN_TEMPERATURE = "siteMinScreenTemperature";
    public static final String SITE_HOURLY_FORECAST_MAX_SCREEN_TEMPERATURE = "siteMaxScreenTemperature";

    public static final String SITE_HOURLY_FEELS_LIKE_TEMPERATURE = "feelsLikeTemperature";

    public static final String SITE_HOURLY_SCREEN_RELATIVE_HUMIDITY = "screenRelativeHumidity";

    public static final String SITE_HOURLY_VISIBILITY = "visibility";

    public static final String SITE_HOURLY_PROBABILITY_OF_PRECIPITATION = "probOfPrecipitation";

    public static final String SITE_HOURLY_TOTAL_PRECIPITATION_AMOUNT = "totalPrecipAmount";

    public static final String SITE_HOURLY_TOTAL_SNOW_AMOUNT = "totalSnowAmount";

    public static final String SITE_HOURLY_UV_INDEX = "uvIndex";

    public static final String SITE_HOURLY_MSLP = "mslp";

    public static final String SITE_HOURLY_WIND_SPEED_10M = "windSpeed10m";

    public static final String SITE_HOURLY_WIND_GUST_SPEED_10M = "windGustSpeed10m";

    public static final String SITE_HOURLY_MAX_10M_WIND_GUST = "max10mWindGust";

    public static final String SITE_HOURLY_WIND_DIRECTION_FROM_10M = "windDirectionFrom10m";

    public static final String SITE_HOURLY_SCREEN_DEW_POINT_TEMPERATURE = "screenDewPointTemperature";

    public static final String SITE_HOURLY_LOCATION_NAME = "locationName";

    public static final String SITE_DAILY_MIDDAY_WIND_SPEED_10M = "middayWindSpeed10m";

    public static final String SITE_DAILY_MIDNIGHT_WIND_SPEED_10M = "midnightWindSpeed10m";

    public static final String SITE_DAILY_MIDDAY_WIND_DIRECTION_10M = "midday10MWindDirection";
    public static final String SITE_DAILY_MIDNIGHT_WIND_DIRECTION_10M = "midnight10MWindDirection";

    public static final String SITE_DAILY_MIDDAY_WIND_GUST_10M = "midday10mWindGust";

    public static final String SITE_DAILY_MIDNIGHT_WIND_GUST_10M = "midnight10mWindGust";

    public static final String SITE_DAILY_MIDDAY_VISIBILITY = "middayVisibility";

    public static final String SITE_DAILY_MIDNIGHT_VISIBILITY = "midnightVisibility";

    public static final String SITE_DAILY_MIDDAY_REL_HUMIDITY = "middayRelativeHumidity";

    public static final String SITE_DAILY_MIDNIGHT_REL_HUMIDITY = "midnightRelativeHumidity";

    public static final String SITE_DAILY_MIDDAY_MSLP = "middayMslp";
    public static final String SITE_DAILY_MIDNIGHT_MSLP = "midnightMslp";

    public static final String SITE_DAILY_DAY_MAX_UV_INDEX = "maxUvIndex";

    public static final String SITE_DAILY_DAY_UPPER_BOUND_MAX_TEMP = "dayUpperBoundMaxTemp";
    public static final String SITE_DAILY_DAY_LOWER_BOUND_MAX_TEMP = "dayLowerBoundMaxTemp";

    public static final String SITE_DAILY_NIGHT_UPPER_BOUND_MAX_TEMP = "nightUpperBoundMinTemp";
    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MAX_TEMP = "nightLowerBoundMinTemp";

    public static final String SITE_DAILY_NIGHT_FEELS_LIKE_MIN_TEMP = "nightMinFeelsLikeTemp";

    public static final String SITE_DAILY_DAY_FEELS_LIKE_MAX_TEMP = "dayMaxFeelsLikeTemp";

    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MIN_TEMP = "nightLowerBoundMinTemp";

    public static final String SITE_DAILY_DAY_MAX_FEELS_LIKE_TEMP = "dayMaxFeelsLikeTemp";

    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MIN_FEELS_LIKE_TEMP = "nightLowerBoundMinFeelsLikeTemp";

    public static final String SITE_DAILY_DAY_LOWER_BOUND_MAX_FEELS_LIKE_TEMP = "dayLowerBoundMaxFeelsLikeTemp";

    public static final String SITE_DAILY_DAY_UPPER_BOUND_MAX_FEELS_LIKE_TEMP = "dayUpperBoundMaxFeelsLikeTemp";

    public static final String SITE_DAILY_UPPER_BOUND_MIN_FEELS_LIKE_TEMP = "nightUpperBoundMinFeelsLikeTemp";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_PRECIPITATION = "dayProbabilityOfPrecipitation";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_PRECIPITATION = "nightProbabilityOfPrecipitation";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_SNOW = "dayProbabilityOfSnow";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_SNOW = "nightProbabilityOfSnow";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_SNOW = "dayProbabilityOfHeavySnow";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_SNOW = "nightProbabilityOfHeavySnow";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_RAIN = "dayProbabilityOfRain";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_RAIN = "nightProbabilityOfRain";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_RAIN = "dayProbabilityOfHeavyRain";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_RAIN = "nightProbabilityOfHeavyRain";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HAIL = "dayProbabilityOfHail";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HAIL = "nightProbabilityOfHail";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_SFERICS = "dayProbabilityOfSferics";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_SFERICS = "nightProbabilityOfSferics";

    public static final String SITE_DAILY_DAY_MAX_SCREEN_TEMPERATURE = "dayMaxScreenTemperature";
    public static final String SITE_DAILY_NIGHT_MIN_SCREEN_TEMPERATURE = "nightMinScreenTemperature";

    public static final String GROUP_PREFIX_HOURS_FORECAST = "currentHoursForecast";
    public static final String GROUP_PREFIX_DAILY_FORECAST = "currentDailyForecast";
    public static final String GROUP_POSTFIX_BOTH_FORECASTS = "Plus";
    public static final char GROUP_PREFIX_TO_ITEM = '#';

    public static final String GET_FORECAST_URL_DAILY = "https://api-metoffice.apiconnect.ibmcloud.com/v0/forecasts/point/daily?includeLocationName=true&latitude=<LATITUDE>&longitude=<LONGITUDE>";
    public static final String GET_FORECAST_URL_HOURLY = "https://api-metoffice.apiconnect.ibmcloud.com/v0/forecasts/point/hourly?includeLocationName=true&latitude=<LATITUDE>&longitude=<LONGITUDE>";

    public static final long DAY_IN_MILLIS = 86400000;

    public static final Random RANDOM_GENERATOR = new Random();

    public static final String BRIDGE_PROP_FORECAST_REQUEST_COUNT = "Site Specific API Call Count";
}
