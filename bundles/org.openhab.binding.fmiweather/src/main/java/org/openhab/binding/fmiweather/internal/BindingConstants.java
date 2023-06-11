/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    private static final String BINDING_ID = "fmiweather";

    public static final int RETRIES = 3;
    public static final int RETRY_DELAY_MILLIS = 1500;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OBSERVATION = new ThingTypeUID(BINDING_ID, "observation");
    public static final ThingTypeUID THING_TYPE_FORECAST = new ThingTypeUID(BINDING_ID, "forecast");
    public static final ThingUID UID_LOCAL_FORECAST = new ThingUID(BINDING_ID, "forecast", "local");

    // List of all Channel ids
    public static final String CHANNEL_TIME = "time";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_GUST = "wind-gust";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_PRECIPITATION_AMOUNT = "precipitation";
    public static final String CHANNEL_SNOW_DEPTH = "snow-depth";
    public static final String CHANNEL_VISIBILITY = "visibility";
    public static final String CHANNEL_CLOUDS = "clouds";
    public static final String CHANNEL_OBSERVATION_PRESENT_WEATHER = "present-weather";

    public static final String CHANNEL_TOTAL_CLOUD_COVER = "total-cloud-cover";
    public static final String CHANNEL_PRECIPITATION_INTENSITY = "precipitation-intensity";
    public static final String CHANNEL_FORECAST_WEATHER_ID = "weather-id";

    // Configuration properties
    public static final String FMISID = "fmisid";
    public static final String LOCATION = "location";
}
