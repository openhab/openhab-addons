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
package org.openhab.binding.weatherunderground.internal.json;

import java.util.List;

/**
 * The {@link WeatherUndergroundJsonSimpleForecast} is the Java class used
 * to map the entry "forecast.simpleforecast" from the JSON response
 * to a Weather Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonSimpleForecast {

    private List<WeatherUndergroundJsonForecastDay> forecastday;

    public WeatherUndergroundJsonSimpleForecast() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecastDay} object for a given day
     *
     * @return the {@link WeatherUndergroundJsonForecastDay} object for the day
     */
    public WeatherUndergroundJsonForecastDay getForecastDay(int day) {
        for (WeatherUndergroundJsonForecastDay forecast : forecastday) {
            if (forecast.getPeriod().intValue() == day) {
                return forecast;
            }
        }
        return null;
    }
}
