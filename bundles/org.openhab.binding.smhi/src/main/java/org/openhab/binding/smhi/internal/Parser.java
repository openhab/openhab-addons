/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smhi.internal;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class with static methods for parsing json strings returned from Smhi
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class Parser {

    /**
     * Parse a json string received from Smhi containing forecasts.
     *
     * @param json A json string
     * @return A {@link SmhiTimeSeries} object
     */
    public static SmhiTimeSeries parseTimeSeries(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();

        ZonedDateTime createdTime = parseTime(object, "createdTime");
        ZonedDateTime referenceTime = parseTime(object, "referenceTime");
        JsonArray timeSeries = object.get("timeSeries").getAsJsonArray();

        List<Forecast> forecasts = StreamSupport.stream(timeSeries.spliterator(), false)
                .map(element -> parseForecast(element.getAsJsonObject())).sorted(Comparator.naturalOrder()).toList();

        return new SmhiTimeSeries(createdTime, referenceTime, forecasts);
    }

    /**
     * Parse a json string containing a timestamp
     *
     * @param obj A {@link JsonObject} containing a timestamp for one of its keys
     * @param key The json key to get the time from
     * @return {@link ZonedDateTime} representation of the requested value
     */
    private static ZonedDateTime parseTime(JsonObject obj, String key) {
        return ZonedDateTime.parse(obj.get(key).getAsString());
    }

    /**
     * Parse json object from the createdtime endpoint
     *
     * @param json A {@link String} repreenting the json
     * @return {@link ZonedDateTime} of the created time
     */
    public static ZonedDateTime parseCreatedTime(String json) {
        JsonObject timeObj = JsonParser.parseString(json).getAsJsonObject();

        return parseTime(timeObj, "createdTime");
    }

    /**
     * Parse a single forecast, i.e. a forecast for a specific time.
     *
     * @param object A {@link JsonObject} for a single forecast entry
     * @return A {@link Forecast} object
     */
    private static Forecast parseForecast(JsonObject object) {
        ZonedDateTime time = parseTime(object, "time");
        ZonedDateTime intervalStartTime = parseTime(object, "intervalParametersStartTime");
        Map<String, BigDecimal> parameters = new HashMap<>();

        JsonObject data = object.get("data").getAsJsonObject();

        data.asMap().forEach((name, value) -> {
            BigDecimal v = value.getAsBigDecimal();
            parameters.put(name, v);
        });

        return new Forecast(time, intervalStartTime, parameters);
    }
}
