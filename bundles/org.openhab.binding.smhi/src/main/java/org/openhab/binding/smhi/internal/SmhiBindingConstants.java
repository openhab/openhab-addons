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
package org.openhab.binding.smhi.internal;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SmhiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiBindingConstants {

    public static final String BINDING_ID = "smhi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FORECAST = new ThingTypeUID(BINDING_ID, "forecast");

    // Smhi's ids for parameters, also used as channel ids
    public static final String PRESSURE = "msl";
    public static final String TEMPERATURE = "t";
    public static final String VISIBILITY = "vis";
    public static final String WIND_DIRECTION = "wd";
    public static final String WIND_SPEED = "ws";
    public static final String RELATIVE_HUMIDITY = "r";
    public static final String THUNDER_PROBABILITY = "tstm";
    public static final String TOTAL_CLOUD_COVER = "tcc_mean";
    public static final String LOW_CLOUD_COVER = "lcc_mean";
    public static final String MEDIUM_CLOUD_COVER = "mcc_mean";
    public static final String HIGH_CLOUD_COVER = "hcc_mean";
    public static final String GUST = "gust";
    public static final String PRECIPITATION_MIN = "pmin";
    public static final String PRECIPITATION_MAX = "pmax";
    public static final String PRECIPITATION_MEAN = "pmean";
    public static final String PRECIPITATION_MEDIAN = "pmedian";
    public static final String PERCENT_FROZEN = "spp";
    public static final String PRECIPITATION_CATEGORY = "pcat";
    public static final String WEATHER_SYMBOL = "wsymb2";

    public static final String TEMPERATURE_MAX = "tmax";
    public static final String TEMPERATURE_MIN = "tmin";
    public static final String WIND_MAX = "wsmax";
    public static final String WIND_MIN = "wsmin";
    public static final String PRECIPITATION_TOTAL = "ptotal";

    public static final List<String> HOURLY_CHANNELS = List.of(PRESSURE, TEMPERATURE, VISIBILITY, WIND_DIRECTION,
            WIND_SPEED, RELATIVE_HUMIDITY, THUNDER_PROBABILITY, TOTAL_CLOUD_COVER, LOW_CLOUD_COVER, MEDIUM_CLOUD_COVER,
            HIGH_CLOUD_COVER, GUST, PRECIPITATION_MIN, PRECIPITATION_MAX, PRECIPITATION_MEAN, PRECIPITATION_MEDIAN,
            PERCENT_FROZEN, PRECIPITATION_CATEGORY, WEATHER_SYMBOL);

    public static final List<String> DAILY_CHANNELS = List.of(PRESSURE, TEMPERATURE, TEMPERATURE_MAX, TEMPERATURE_MIN,
            VISIBILITY, WIND_DIRECTION, WIND_SPEED, WIND_MAX, WIND_MIN, RELATIVE_HUMIDITY, THUNDER_PROBABILITY,
            TOTAL_CLOUD_COVER, LOW_CLOUD_COVER, MEDIUM_CLOUD_COVER, HIGH_CLOUD_COVER, GUST, PRECIPITATION_MIN,
            PRECIPITATION_MAX, PRECIPITATION_TOTAL, PRECIPITATION_MEAN, PRECIPITATION_MEDIAN, PERCENT_FROZEN,
            PRECIPITATION_CATEGORY, WEATHER_SYMBOL);

    public static final String BASE_URL = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/";
    public static final String APPROVED_TIME_URL = BASE_URL + "approvedtime.json";
    public static final String POINT_FORECAST_URL = BASE_URL + "geotype/point/lon/%.6f/lat/%.6f/data.json";

    public static final BigDecimal OCTAS_TO_PERCENT = BigDecimal.valueOf(12.5);
}
