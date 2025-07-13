/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.DurationUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link Utils} provides helper calls used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * JSONObjects form Tibber API are deeply nested. Helper function to get a JSONObject from a defined path.
     *
     * @param json JsonObject to be queried
     * @param path array of JSON keys defining the path to a JsonObject
     * @return JsonObject from after iterating though the path
     */
    public static JsonObject getJsonObject(JsonObject json, String[] path) {
        JsonObject iterator = json;
        for (int i = 0; i < path.length; i++) {
            if (iterator.has(path[i])) {
                iterator = iterator.getAsJsonObject(path[i]);
            } else {
                LOGGER.error("Unable to resolve path {} from {}", Utils.path(path), json);
                return new JsonObject();
                // all paths are tested according to Tibber API. If this happens binding needs to be adapted
            }
        }
        return iterator;
    }

    /**
     * Get the value from the key of a JsonObject
     *
     * @param json JsonObject to be queried
     * @param key as String
     * @return value as String, "null" if JSON value is null, EMPTY if key isn't present
     */
    public static String getJsonValue(JsonObject json, String key) {
        if (json.has(key)) {
            JsonElement elem = json.get(key);
            if (!elem.isJsonNull()) {
                return elem.getAsString();
            } else {
                return NULL_VALUE;
            }
        }
        return EMPTY_VALUE;
    }

    /**
     * Return String path separated with /
     *
     * @param path array of Strings
     * @return String representation with / separator for each path entry
     */
    public static String path(String[] path) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            strBuffer.append("/" + path[i]);
        }
        return strBuffer.toString();
    }

    /**
     * Converts a duration String with or without unit into seconds. If no unit is attached default is seconds
     *
     * @param durationString duration with number and optional unit
     * @return duration in seconds
     */
    public static long parseDuration(String durationString) throws CalculationParameterException {
        try {
            return Long.parseLong(durationString); // check if String is a number
        } catch (NumberFormatException e) {
            try {
                return DurationUtils.parse(durationString).toSeconds(); // check if String is a duration
            } catch (IllegalArgumentException e2) {
                throw new CalculationParameterException("Cannot decode duration " + durationString);
            }
        }
    }

    /**
     * Converts given parameters object into Map
     *
     * @param parameters object to convert
     * @param targetMap parameter map to be filled
     * @return true if parameters were given as JSON String
     */
    @SuppressWarnings("unchecked")
    public static boolean convertParameters(Object parameters, Map<String, Object> targetMap)
            throws CalculationParameterException {
        if (parameters instanceof String json) {
            JsonObject parametersJson = (JsonObject) JsonParser.parseString(json);
            Map<String, JsonElement> parameterMap = parametersJson.asMap();
            JsonElement earliestStart = parameterMap.get(PARAM_EARLIEST_START);
            if (earliestStart != null) {
                targetMap.put(PARAM_EARLIEST_START, Instant.parse(earliestStart.getAsString()));
            }
            JsonElement latestEnd = parameterMap.get(PARAM_LATEST_END);
            if (latestEnd != null) {
                targetMap.put(PARAM_LATEST_END, Instant.parse(latestEnd.getAsString()));
            }
            JsonElement ascending = parameterMap.get(PARAM_ASCENDING);
            if (ascending != null) {
                targetMap.put(PARAM_ASCENDING, ascending.getAsBoolean());
            }
            JsonElement power = parameterMap.get(PARAM_POWER);
            if (power != null) {
                targetMap.put(PARAM_POWER, power.getAsInt());
            }
            JsonElement duration = parameterMap.get(PARAM_DURATION);
            if (duration != null) {
                targetMap.put(PARAM_DURATION, Utils.parseDuration(duration.getAsString()));
            }
            JsonElement curve = parameterMap.get(PARAM_CURVE);
            if (curve != null) {
                List<CurveEntry> curveList = Utils.convertCurve(curve);
                targetMap.put(PARAM_DURATION, curveList);
            }
            return true;
        } else if (parameters instanceof Map map) {
            targetMap.putAll(map);
            return false;
        } else {
            throw new CalculationParameterException(
                    "Calculation parameters are neiter Map nor JSON String " + parameters.getClass().toString());
        }
    }

    /**
     * Converts a JSON array with timestamp OR duration values into required CurveEntry list
     *
     * @param curve JSON array
     * @return List of CurveEntry
     */
    public static List<CurveEntry> convertCurve(JsonElement curve) throws CalculationParameterException {
        List<CurveEntry> curveList = new ArrayList<>();
        JsonArray curveArray = ((JsonArray) curve);
        int previousPower = Integer.MAX_VALUE;
        Instant previousTimestamp = Instant.MAX;

        for (Iterator<JsonElement> iterator = curveArray.iterator(); iterator.hasNext();) {
            JsonObject entry = iterator.next().getAsJsonObject();
            JsonElement timestamp = entry.get(PARAM_TIMESTAMP);
            if (timestamp != null) {
                // durations needs to be calculated
                Instant currentTimestamp = Instant.parse(timestamp.getAsString());
                if (!Instant.MAX.equals(previousTimestamp)) {
                    curveList.add(new CurveEntry(previousPower,
                            (int) Duration.between(previousTimestamp, currentTimestamp).getSeconds()));
                }
                previousTimestamp = currentTimestamp;
                JsonElement power = entry.get(PARAM_POWER);
                if (power != null) {
                    previousPower = power.getAsInt();
                } else {
                    throw new CalculationParameterException("All curve elements needs power value " + curve);
                }
            } else {
                int powerValue = 0;
                long durationValue = 0;
                JsonElement power = entry.get(PARAM_POWER);
                if (power != null) {
                    powerValue = power.getAsInt();
                } else {
                    throw new CalculationParameterException("All curve elements needs power value " + curve);
                }
                JsonElement duration = entry.get(PARAM_DURATION);
                if (duration != null) {
                    durationValue = Utils.parseDuration(duration.getAsString());
                } else {
                    throw new CalculationParameterException("All curve elements needs duration value " + curve);
                }
                curveList.add(new CurveEntry(powerValue, durationValue));
            }
        }
        return curveList;
    }

    /**
     * Map level String from Tibber response to State.
     * See https://developer.tibber.com/docs/reference#pricelevel
     *
     * @param levelString levelString String according to Tibber API
     * @return DecimalType if price decoding is successful, UNDEF otherwise
     */
    public static State mapToState(String levelString) {
        int level = mapLevelToInt(levelString);
        if (level == Integer.MAX_VALUE) {
            return UnDefType.UNDEF;
        } else {
            return new DecimalType(level);
        }
    }

    /**
     * Map level String from Tibber response to integer.
     * See https://developer.tibber.com/docs/reference#pricelevel
     *
     * @param levelString String according to Tibber API
     * @return 0 is normal level, negative values cheaper and positive values expensive values.
     */
    public static int mapLevelToInt(String levelString) {
        switch (levelString.toLowerCase()) {
            case PRICE_LEVEL_VERY_CHEAP:
                return -2;
            case PRICE_LEVEL_CHEAP:
                return -1;
            case PRICE_LEVEL_NORMAL:
                return 0;
            case PRICE_LEVEL_EXPENSIVE:
                return 1;
            case PRICE_LEVEL_VERY_EXPENSIVE:
                return 2;
            default:
                LOGGER.warn("Level {} cannot be mapped. Please create GitHub issue", levelString);
                return Integer.MAX_VALUE;
        }
    }

    // fulfill https://developer.tibber.com/docs/guides/calling-api
    // Clients must set the User-Agent HTTP header when calling the GraphQL API. Both platform and driver version
    // must be indicated. E.g. Homey/10.0.0 com.tibber/1.8.3.
    public static String getUserAgent(Object reference) {
        Bundle b = FrameworkUtil.getBundle(reference.getClass());
        if (b == null) {
            return "openHAB5/com.tibber.test";
        } else {
            return "openHAB/" + b.getVersion().toString();
        }
    }
}
