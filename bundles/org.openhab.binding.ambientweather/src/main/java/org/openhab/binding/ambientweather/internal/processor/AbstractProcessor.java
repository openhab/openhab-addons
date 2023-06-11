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
package org.openhab.binding.ambientweather.internal.processor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ambientweather.internal.model.EventDataJson;
import org.openhab.binding.ambientweather.internal.util.PressureTrend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link AbstractProcessor} is the generic/error processor
 * for info and weather updates from weather stations that are currently
 * not supported by this binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractProcessor implements ProcessorInterface {
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

    private static final String[] WIND_DIRECTIONS = new String[] { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

    protected static final String NOT_APPLICABLE = "N/A";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Used to calculate barometric pressure trend
    protected PressureTrend pressureTrend = new PressureTrend();

    /*
     * The channel group Id for this processor
     */
    protected String channelGroupId = "";

    /*
     * Used to extract remote sensor data from the data event Json
     */
    protected RemoteSensor remoteSensor = new RemoteSensor();

    /*
     * Parse the event data json string
     */
    protected @Nullable EventDataJson parseEventData(String station, String jsonData) {
        EventDataJson data = null;
        try {
            logger.debug("Station {}: Parsing weather data event json", station);
            data = ProcessorFactory.getGson().fromJson(jsonData, EventDataJson.class);
        } catch (JsonSyntaxException e) {
            logger.info("Station {}: Data event cannot be parsed: {}", station, e.getMessage());
        }
        return data;
    }

    /*
     * Convert the UV Index integer value to a string representation
     */
    protected String convertUVIndexToString(int uvIndex) {
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
