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
package org.openhab.binding.weatherunderground.internal.json;

/**
 * The {@link WeatherUndergroundJsonForecast} is the Java class used
 * to map the entry "forecast" from the JSON response to a Weather Underground
 * request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonForecast {

    // Commented members indicate properties returned by the API not used by the binding

    // private Object txt_forecast;
    private WeatherUndergroundJsonSimpleForecast simpleforecast;

    public WeatherUndergroundJsonForecast() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for a given day
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the day
     */
    public WeatherUndergroundJsonForecastDay getSimpleForecast(int day) {
        return (simpleforecast == null) ? null : simpleforecast.getForecastDay(day);
    }
}
