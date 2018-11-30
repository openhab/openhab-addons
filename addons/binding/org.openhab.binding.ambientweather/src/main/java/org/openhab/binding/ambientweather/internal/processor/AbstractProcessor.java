/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public String convertWindDirectionToString(double windDirectionDegrees) {
        String result = "UNKNOWN";
        if (windDirectionDegrees >= 0 && windDirectionDegrees <= 11.25) {
            result = "N";
        } else if (windDirectionDegrees > 11.25 && windDirectionDegrees <= 33.75) {
            result = "NNE";
        } else if (windDirectionDegrees > 33.75 && windDirectionDegrees <= 56.25) {
            result = "NE";
        } else if (windDirectionDegrees > 56.25 && windDirectionDegrees <= 78.75) {
            result = "ENE";
        } else if (windDirectionDegrees > 78.75 && windDirectionDegrees <= 101.25) {
            result = "E";
        } else if (windDirectionDegrees > 101.25 && windDirectionDegrees <= 123.75) {
            result = "ESE";
        } else if (windDirectionDegrees > 123.75 && windDirectionDegrees <= 146.25) {
            result = "SE";
        } else if (windDirectionDegrees > 146.25 && windDirectionDegrees <= 168.75) {
            result = "SSE";
        } else if (windDirectionDegrees > 168.75 && windDirectionDegrees <= 191.25) {
            result = "S";
        } else if (windDirectionDegrees > 191.25 && windDirectionDegrees <= 213.75) {
            result = "SSW";
        } else if (windDirectionDegrees > 213.75 && windDirectionDegrees <= 236.25) {
            result = "SW";
        } else if (windDirectionDegrees > 236.25 && windDirectionDegrees <= 258.75) {
            result = "WSW";
        } else if (windDirectionDegrees > 258.75 && windDirectionDegrees <= 281.25) {
            result = "W";
        } else if (windDirectionDegrees > 281.25 && windDirectionDegrees <= 303.75) {
            result = "WNW";
        } else if (windDirectionDegrees > 303.75 && windDirectionDegrees <= 326.25) {
            result = "NW";
        } else if (windDirectionDegrees > 326.25 && windDirectionDegrees <= 348.75) {
            result = "NNW";
        } else if (windDirectionDegrees > 348.75 && windDirectionDegrees <= 360) {
            result = "N";
        }

        return result;
    }
}
