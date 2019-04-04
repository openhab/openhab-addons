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
package org.openhab.binding.ambientweather.internal.processor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractProcessor} is the generic/error processor
 * for info and weather updates from weather stations that are currently
 * not supported by this binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractProcessor {
    // @formatter:off
    private static final String[] UV_INDEX = {
        "LOW",
        "LOW",
        "LOW",
        "MODERATE",
        "MODERATE",
        "MODERATE",
        "HIGH",
        "HIGH",
        "VERY HIGH",
        "VERY HIGH",
        "VERY HIGH",
        "EXTREME",
        "EXTREME",
        "EXTREME",
        "EXTREME",
        "EXTREME"
    };
    // @formatter:on

    public static final String[] WIND_DIRECTIONS = new String[] { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S",
            "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * Used to extract remote sensor data from the data event Json
     */
    protected RemoteSensor remoteSensor = new RemoteSensor();

    /*
     * Updates the info channels (i.e. name and location) for a station
     */
    public abstract void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name,
            String location);

    /*
     * Updates the weather data channels for a station
     */
    public abstract void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData);

    /*
     * Helper function called by processor to convert UTC time milliseconds to local time
     */
    public DateTimeType getLocalDateTimeType(long dateTimeMillis, ZoneId zoneId) {
        Instant instant = Instant.ofEpochMilli(dateTimeMillis);
        ZonedDateTime localDateTime = instant.atZone(zoneId);
        DateTimeType dateTimeType = new DateTimeType(localDateTime);
        return dateTimeType;
    }

    /*
     * Helper function called by processor to convert UTC time string to local time
     * Input string is of form 2018-12-02T10:47:00.000Z
     */
    public DateTimeType getLocalDateTimeType(String dateTimeString, ZoneId zoneId) {
        DateTimeType dateTimeType;
        try {
            Instant instant = Instant.parse(dateTimeString);
            ZonedDateTime localDateTime = instant.atZone(zoneId);
            dateTimeType = new DateTimeType(localDateTime);
        } catch (DateTimeParseException e) {
            logger.debug("Error parsing date/time string: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        } catch (IllegalArgumentException e) {
            logger.debug("Error converting to DateTimeType: {}", e.getMessage());
            dateTimeType = new DateTimeType();
        }
        return dateTimeType;
    }

    /*
     * Convert the UV Index integer value to a string representation
     */
    public String convertUVIndexToString(int uvIndex) {
        if (uvIndex < 0 || uvIndex >= UV_INDEX.length) {
            return "UNKNOWN";
        }
        return UV_INDEX[uvIndex];
    }

    /*
     * Convert the wind directions in degrees to a string representation
     */
    protected String convertWindDirectionToString(double windDirectionDegrees) {
        double step = 360.0 / WIND_DIRECTIONS.length;
        double b = Math.floor((windDirectionDegrees + (step / 2.0)) / step);
        return WIND_DIRECTIONS[(int) (b % WIND_DIRECTIONS.length)];
    }
}
