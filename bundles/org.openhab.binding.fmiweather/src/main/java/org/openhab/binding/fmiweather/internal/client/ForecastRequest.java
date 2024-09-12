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
package org.openhab.binding.fmiweather.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Request for weather forecasts
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ForecastRequest extends Request {

    public static final String STORED_QUERY_ID = "fmi::forecast::harmonie::surface::point::multipointcoverage";

    // For description of variables: http://opendata.fmi.fi/meta?observableProperty=forecast
    public static final String PARAM_TEMPERATURE = "Temperature";
    public static final String PARAM_HUMIDITY = "Humidity";
    public static final String PARAM_WIND_DIRECTION = "WindDirection";
    public static final String PARAM_WIND_SPEED = "WindSpeedMS";
    public static final String PARAM_WIND_GUST = "WindGust";
    public static final String PARAM_PRESSURE = "Pressure";
    public static final String PARAM_PRECIPITATION_1H = "Precipitation1h";
    public static final String PARAM_TOTAL_CLOUD_COVER = "TotalCloudCover";
    public static final String PARAM_WEATHER_SYMBOL = "WeatherSymbol3";
    public static final String[] PARAMETERS = new String[] { PARAM_TEMPERATURE, PARAM_HUMIDITY, PARAM_WIND_DIRECTION,
            PARAM_WIND_SPEED, PARAM_WIND_GUST, PARAM_PRESSURE, PARAM_PRECIPITATION_1H, PARAM_TOTAL_CLOUD_COVER,
            PARAM_WEATHER_SYMBOL };

    public ForecastRequest(QueryParameter location, long startEpoch, long endEpoch, long timestepMinutes) {
        super(STORED_QUERY_ID, location, startEpoch, endEpoch, timestepMinutes, PARAMETERS);
    }
}
