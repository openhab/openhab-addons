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
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_SITE_SPEC_API = new ThingTypeUID(BINDING_ID, "site");

    /**
     * Site Specific API - Shared
     */

    public static final String SITE_TIMESTAMP = "forecast-ts";

    /**
     * Site Specific API - Hourly Forecast Channel Names
     */
    public static final String SITE_HOURLY_FORECAST_SCREEN_TEMPERATURE = "air-temp-current";

    public static final String SITE_HOURLY_FORECAST_MIN_SCREEN_TEMPERATURE = "air-temp-min";
    public static final String SITE_HOURLY_FORECAST_MAX_SCREEN_TEMPERATURE = "air-temp-max";

    public static final String SITE_HOURLY_FEELS_LIKE_TEMPERATURE = "feels-like";

    public static final String SITE_HOURLY_SCREEN_RELATIVE_HUMIDITY = "humidity";

    public static final String SITE_HOURLY_VISIBILITY = "visibility";

    public static final String SITE_HOURLY_PROBABILITY_OF_PRECIPITATION = "precip-prob";

    public static final String SITE_HOURLY_PRECIPITATION_RATE = "precip-rate";

    public static final String SITE_HOURLY_TOTAL_PRECIPITATION_AMOUNT = "precip-total";

    public static final String SITE_HOURLY_TOTAL_SNOW_AMOUNT = "snow-total";

    public static final String SITE_HOURLY_UV_INDEX = "uv-index";

    public static final String SITE_HOURLY_PRESSURE = "pressure";

    public static final String SITE_HOURLY_WIND_SPEED_10M = "wind-speed";

    public static final String SITE_HOURLY_WIND_GUST_SPEED_10M = "wind-speed-gust";

    public static final String SITE_HOURLY_MAX_10M_WIND_GUST = "wind-gust-max";

    public static final String SITE_HOURLY_WIND_DIRECTION_FROM_10M = "wind-direction";

    public static final String SITE_HOURLY_SCREEN_DEW_POINT_TEMPERATURE = "dewpoint";

    public static final String SITE_DAILY_MIDDAY_WIND_SPEED_10M = "wind-speed-day";

    public static final String SITE_DAILY_MIDNIGHT_WIND_SPEED_10M = "wind-speed-night";

    public static final String SITE_DAILY_MIDDAY_WIND_DIRECTION_10M = "wind-direction-day";
    public static final String SITE_DAILY_MIDNIGHT_WIND_DIRECTION_10M = "wind-direction-night";

    public static final String SITE_DAILY_MIDDAY_WIND_GUST_10M = "wind-gust-day";

    public static final String SITE_DAILY_MIDNIGHT_WIND_GUST_10M = "wind-gust-night";

    public static final String SITE_DAILY_MIDDAY_VISIBILITY = "visibility-day";

    public static final String SITE_DAILY_MIDNIGHT_VISIBILITY = "visibility-night";

    public static final String SITE_DAILY_MIDDAY_REL_HUMIDITY = "humidity-day";

    public static final String SITE_DAILY_MIDNIGHT_REL_HUMIDITY = "humidity-night";

    public static final String SITE_DAILY_MIDDAY_PRESSURE = "pressure-day";
    public static final String SITE_DAILY_MIDNIGHT_PRESSURE = "pressure-night";

    public static final String SITE_DAILY_DAY_MAX_UV_INDEX = "uv-max";

    public static final String SITE_DAILY_DAY_UPPER_BOUND_MAX_TEMP = "temp-max-ub-day";
    public static final String SITE_DAILY_DAY_LOWER_BOUND_MAX_TEMP = "temp-max-lb-day";

    public static final String SITE_DAILY_NIGHT_UPPER_BOUND_MAX_TEMP = "temp-min-ub-night";
    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MAX_TEMP = "temp-min-lb-night";

    public static final String SITE_DAILY_NIGHT_FEELS_LIKE_MIN_TEMP = "feels-like-min-night";

    public static final String SITE_DAILY_DAY_FEELS_LIKE_MAX_TEMP = "feels-like-max-day";

    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MIN_TEMP = "temp-min-lb-night";

    public static final String SITE_DAILY_DAY_MAX_FEELS_LIKE_TEMP = "feels-like-max-day";

    public static final String SITE_DAILY_NIGHT_LOWER_BOUND_MIN_FEELS_LIKE_TEMP = "feels-like-min-lb-night";

    public static final String SITE_DAILY_DAY_LOWER_BOUND_MAX_FEELS_LIKE_TEMP = "feels-like-max-lb-day";

    public static final String SITE_DAILY_DAY_UPPER_BOUND_MAX_FEELS_LIKE_TEMP = "feels-like-max-ub-day";

    public static final String SITE_DAILY_UPPER_BOUND_MIN_FEELS_LIKE_TEMP = "feels-like-min-ub-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_PRECIPITATION = "precip-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_PRECIPITATION = "precip-prob-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_SNOW = "snow-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_SNOW = "snow-prob-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_SNOW = "heavy-snow-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_SNOW = "heavy-snow-prob-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_RAIN = "rain-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_RAIN = "rain-prob-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HEAVY_RAIN = "day-prob-heavy-rain";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HEAVY_RAIN = "night-prob-heavy-rain";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_HAIL = "hail-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_HAIL = "hail-prob-night";

    public static final String SITE_DAILY_DAY_PROBABILITY_OF_SFERICS = "sferics-prob-day";

    public static final String SITE_DAILY_NIGHT_PROBABILITY_OF_SFERICS = "sferics-prob-night";

    public static final String SITE_DAILY_DAY_MAX_SCREEN_TEMPERATURE = "temp-max-day";
    public static final String SITE_DAILY_NIGHT_MIN_SCREEN_TEMPERATURE = "temp-min-night";

    public static final String GROUP_PREFIX_HOURS_FORECAST = "current-forecast";
    public static final String GROUP_PREFIX_DAILY_FORECAST = "daily-forecast";
    public static final String GROUP_POSTFIX_BOTH_FORECASTS = "-plus";
    public static final char GROUP_PREFIX_TO_ITEM = '#';

    public static final String GET_FORECAST_URL_DAILY = "https://data.hub.api.metoffice.gov.uk/sitespecific/v0/point/daily?latitude=<LATITUDE>&longitude=<LONGITUDE>";
    public static final String GET_FORECAST_URL_HOURLY = "https://data.hub.api.metoffice.gov.uk/sitespecific/v0/point/hourly?latitude=<LATITUDE>&longitude=<LONGITUDE>";
    public static final String GET_FORECAST_KEY_LATITUDE = "<LATITUDE>";
    public static final String GET_FORECAST_KEY_LONGITUDE = "<LONGITUDE>";
    public static final String GET_FORECAST_API_KEY_HEADER = "apikey";
    public static final int GET_FORECAST_REQUEST_TIMEOUT_SECONDS = 3;
    public static final String EXPECTED_TS_FORMAT = "YYYY-MM-dd HH:mm:ss.SSS";

    public static final long DAY_IN_MILLIS = 86400000;

    public static final Random RANDOM_GENERATOR = new Random();

    public static final String BRIDGE_PROP_FORECAST_REQUEST_COUNT = "Site Specific API Call Count";

    public static final Runnable NO_OP = () -> {
    };
}
