/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WeatherBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class WeatherBindingConstants {

    public static final String BINDING_ID = "weather";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_FORECAST = new ThingTypeUID(BINDING_ID, "forecast");

    // List of channel groups.
    public final static String CHANNEL_GROUP_ATMOSPHERE = "atmosphere";
    public final static String CHANNEL_GROUP_CLOUD = "cloud";
    public final static String CHANNEL_GROUP_CONDITION = "condition";
    public final static String CHANNEL_GROUP_PRECIPITATION = "precipitation";
    public final static String CHANNEL_GROUP_STATION = "station";
    public final static String CHANNEL_GROUP_WIND = "wind";
    public final static String CHANNEL_GROUP_TEMPERATURE = "temperature";

    // List of all Channel ids
    // Atmosphere channels
    public final static String CHANNEL_HUMIDITY = CHANNEL_GROUP_ATMOSPHERE + "#humidity";
    public final static String CHANNEL_VISIBILITY = CHANNEL_GROUP_ATMOSPHERE + "#visibility";
    public final static String CHANNEL_PRESSURE = CHANNEL_GROUP_ATMOSPHERE + "#pressure";
    public final static String CHANNEL_PRESSURE_TREND = CHANNEL_GROUP_ATMOSPHERE + "#pressureTrend";
    public final static String CHANNEL_OZONE = CHANNEL_GROUP_ATMOSPHERE + "#ozone";
    public final static String CHANNEL_UVINDEX = CHANNEL_GROUP_ATMOSPHERE + "#uvIndex";
    // Cloud channels
    public final static String CHANNEL_CLOUD_COVER_PERCENT = CHANNEL_GROUP_CLOUD + "#cloudCoverPercent";
    // Condition channels
    public final static String CHANNEL_CONDITION = CHANNEL_GROUP_CONDITION + "#condition";
    public final static String CHANNEL_OBSERVATION_TIME = CHANNEL_GROUP_CONDITION + "#observationTime";
    public final static String CHANNEL_CONDITION_ID = CHANNEL_GROUP_CONDITION + "#conditionId";
    public final static String CHANNEL_ICON = CHANNEL_GROUP_CONDITION + "#icon";
    // Precipitation channels
    public final static String CHANNEL_RAIN = CHANNEL_GROUP_PRECIPITATION + "#rain";
    public final static String CHANNEL_SNOW = CHANNEL_GROUP_PRECIPITATION + "#snow";
    public final static String CHANNEL_TYPE = CHANNEL_GROUP_PRECIPITATION + "#type";
    public final static String CHANNEL_TOTAL = CHANNEL_GROUP_PRECIPITATION + "#total";
    public final static String CHANNEL_PROBABILILITY = CHANNEL_GROUP_PRECIPITATION + "#probability";
    // Station channels
    public final static String CHANNEL_STATION_NAME = CHANNEL_GROUP_STATION + "#stationName";
    public final static String CHANNEL_STATION_ID = CHANNEL_GROUP_STATION + "#stationId";
    public final static String CHANNEL_LATITUDE = CHANNEL_GROUP_STATION + "#latitude";
    public final static String CHANNEL_LONGITUDE = CHANNEL_GROUP_STATION + "#longitude";
    public final static String CHANNEL_ALTITUDE = CHANNEL_GROUP_STATION + "#altitude";
    // Temperature channels.
    public final static String CHANNEL_CURRENT = CHANNEL_GROUP_TEMPERATURE + "#current";
    public final static String CHANNEL_MIN = CHANNEL_GROUP_TEMPERATURE + "#min";
    public final static String CHANNEL_MAX = CHANNEL_GROUP_TEMPERATURE + "#max";
    public final static String CHANNEL_FEEL = CHANNEL_GROUP_TEMPERATURE + "#feel";
    public final static String CHANNEL_DEWPOINT = CHANNEL_GROUP_TEMPERATURE + "#dewpoint";
    // Wind channels.
    public final static String CHANNEL_SPEED = CHANNEL_GROUP_WIND + "#speed";
    public final static String CHANNEL_DIRECTION = CHANNEL_GROUP_WIND + "#direction";
    public final static String CHANNEL_DEGREE = CHANNEL_GROUP_WIND + "#degree";
    public final static String CHANNEL_GUST = CHANNEL_GROUP_WIND + "#gust";
    public final static String CHANNEL_CHILL = CHANNEL_GROUP_WIND + "#chill";

    // Properties to track things.
    public static final String PROPERTY_DAY = "day";

}
