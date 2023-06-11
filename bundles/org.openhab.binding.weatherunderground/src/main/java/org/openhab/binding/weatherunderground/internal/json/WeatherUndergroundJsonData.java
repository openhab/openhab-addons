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
 * The {@link WeatherUndergroundJsonData} is the Java class used to map the JSON
 * response to a Weather Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonData {

    private WeatherUndergroundJsonResponse response;
    private WeatherUndergroundJsonCurrent current_observation;
    private WeatherUndergroundJsonForecast forecast;
    private WeatherUndergroundJsonLocation location;

    public WeatherUndergroundJsonData() {
    }

    /**
     * Get the {@link WeatherUndergroundJsonResponse} object
     *
     * @return the {@link WeatherUndergroundJsonResponse} object
     */
    public WeatherUndergroundJsonResponse getResponse() {
        return response;
    }

    /**
     * Get the {@link WeatherUndergroundJsonLocation} object
     *
     * @return the {@link WeatherUndergroundJsonLocation} object
     */
    public WeatherUndergroundJsonLocation getLocation() {
        return location;
    }

    /**
     * Get the {@link WeatherUndergroundJsonForecast} object
     *
     * @return the {@link WeatherUndergroundJsonForecast} object
     */
    public WeatherUndergroundJsonForecast getForecast() {
        return forecast;
    }

    /**
     * Get the {@link WeatherUndergroundJsonCurrent} object
     *
     * Used to update the channels current#xxx
     *
     * @return the {@link WeatherUndergroundJsonCurrent} object
     */
    public WeatherUndergroundJsonCurrent getCurrent() {
        return current_observation;
    }
}
