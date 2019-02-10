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
package org.openhab.binding.smhi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link smhiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Parment - Initial contribution
 */
@NonNullByDefault
public class SmhiBindingConstants {

    private static final String BINDING_ID = "smhi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WEATHER_AND_FORECAST = new ThingTypeUID(BINDING_ID,
            "weather-and-forecast");

    public static final String CHANNEL_GROUP_CURRENT_WEATHER = "current";
    public static final String CHANNEL_GROUP_FORECAST_TOMORROW = "forecastTomorrow";
    public static final String CHANNEL_GROUP_DAILY_FORECAST_DAY2 = "forecastDay2";
    public static final String CHANNEL_GROUP_DAILY_FORECAST_DAY3 = "forecastDay3";
    // List of all Channel ids
    public static final String CHANNEL_TIME_STAMP = "time-stamp";
    public static final String CHANNEL_CONDITION = "condition";
    public static final String CHANNEL_CONDITION_ID = "condition-id";
    public static final String CHANNEL_CONDITION_ICON = "icon";
    public static final String CHANNEL_CONDITION_ICON_ID = "icon-id";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_MIN_TEMPERATURE = "temperature-min";
    public static final String CHANNEL_MAX_TEMPERATURE = "temperature-max";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_GUST_SPEED = "wind-gust";
    public static final String CHANNEL_CLOUD_COVER = "cloud-cover";
    public static final String CHANNEL_HIGH_CLOUD_COVER = "cloud-cover-high";
    public static final String CHANNEL_MEDIUM_CLOUD_COVER = "cloud-cover-medium";
    public static final String CHANNEL_LOW_CLOUD_COVER = "cloud-cover-low";
    public static final String CHANNEL_THUNDER_PROBABILITY = "thunderstorm";
    public static final String CHANNEL_VISIBILITY = "visibility";
    public static final String CHANNEL_PRECIPITATION_CATEGORY = "precipitation-cat";
    public static final String CHANNEL_PRECIPITATION_CATEGORY_ID = "precipitation-cat-id";
    public static final String CHANNEL_MEAN_PRECIPITATION = "precipitation-mean";
    public static final String CHANNEL_MAX_PRECIPITATION = "precipitation-max";
    public static final String CHANNEL_MIN_PRECIPITATION = "precipitation-min";
    public static final String CHANNEL_MEDIAN_PRECIPITATION = "precipitation-median";
    public static final String CHANNEL_FROZEN_PRECIPITATION = "precipitation-frozen";

    public static final String CHANNEL_CONDITION_ID_JSON = "Wsymb2";
    public static final String CHANNEL_TEMPERATURE_JSON = "t";
    public static final String CHANNEL_PRESSURE_JSON = "msl";
    public static final String CHANNEL_HUMIDITY_JSON = "r";
    public static final String CHANNEL_WIND_SPEED_JSON = "ws";
    public static final String CHANNEL_WIND_DIRECTION_JSON = "wd";
    public static final String CHANNEL_GUST_SPEED_JSON = "gust";
    public static final String CHANNEL_CLOUD_COVER_JSON = "tcc_mean";
    public static final String CHANNEL_HIGH_CLOUD_COVER_JSON = "hcc_mean";
    public static final String CHANNEL_MEDIUM_CLOUD_COVER_JSON = "mcc_mean";
    public static final String CHANNEL_LOW_CLOUD_COVER_JSON = "lcc_mean";
    public static final String CHANNEL_THUNDER_PROBABILITY_JSON = "tstm";
    public static final String CHANNEL_VISIBILITY_JSON = "vis";
    public static final String CHANNEL_PRECIPITATION_CATEGORY_ID_JSON = "pcat";
    public static final String CHANNEL_MEAN_PRECIPITATION_JSON = "pmean";
    public static final String CHANNEL_MAX_PRECIPITATION_JSON = "pmax";
    public static final String CHANNEL_MIN_PRECIPITATION_JSON = "pmin";
    public static final String CHANNEL_MEDIAN_PRECIPITATION_JSON = "pmedian";
    public static final String CHANNEL_FROZEN_PRECIPITATION_JSON = "spp";

}
