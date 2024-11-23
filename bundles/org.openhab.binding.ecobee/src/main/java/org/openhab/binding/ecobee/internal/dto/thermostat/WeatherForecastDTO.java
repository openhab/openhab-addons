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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.time.LocalDateTime;

/**
 * The {@link WeatherForecastDTO} contains the weather forecast information for
 * the thermostat. The first forecast is the most accurate, later forecasts
 * become less accurate in distance and time.
 *
 * Weather Symbol Mappings
 *
 * @author Mark Hilbush - Initial contribution
 */
public class WeatherForecastDTO {

    /*
     * The Integer value used to map to a weatherSymbol.
     */
    public Integer weatherSymbol;

    /*
     * The time stamp of the weather forecast in the thermostat's time zone.
     */
    public LocalDateTime dateTime;

    /*
     * A text value representing the current weather condition.
     */
    public String condition;

    /*
     * The current temperature.
     */
    public Integer temperature;

    /*
     * The current barometric pressure.
     */
    public Integer pressure;

    /*
     * The current humidity.
     */
    public Integer relativeHumidity;

    /*
     * he dewpoint.
     */
    public Integer dewpoint;

    /*
     * The visibility in meters; 0 - 70,000.
     */
    public Integer visibility;

    /*
     * The wind speed as an integer in mph * 1000.
     */
    public Integer windSpeed;

    /*
     * The wind gust speed.
     */
    public Integer windGust;

    /*
     * The wind direction.
     */
    public String windDirection;

    /*
     * The wind bearing.
     */
    public Integer windBearing;

    /*
     * The probability of precipitation.
     */
    public Integer pop;

    /*
     * The predicted high temperature for the day.
     */
    public Integer tempHigh;

    /*
     * The predicted low temperature for the day.
     */
    public Integer tempLow;

    /*
     * The cloud cover condition.
     */
    public Integer sky;
}
