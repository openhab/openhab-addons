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
package org.openhab.binding.tibber.internal;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link Utils} provides helper calls used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static void calculateAveragePrices(TreeMap<Instant, Double> priceMap) {
        Instant calculationInstant = Instant.now().with(ChronoField.MINUTE_OF_HOUR, 0)
                .with(ChronoField.SECOND_OF_MINUTE, 0);
        Instant calculationStart = calculationInstant.minus(1, ChronoUnit.DAYS);
        // continue loop until iterator current point of calculation
        while (priceMap.ceilingEntry(calculationInstant) != null) {
            if (priceMap.floorEntry(calculationStart) != null) {
                // calculate average for 24h
                double price = 0;
                Instant iterator = calculationStart;
                while (iterator.isBefore(calculationInstant)) {
                    Entry<Instant, Double> floor = priceMap.floorEntry(calculationStart);
                    Entry<Instant, Double> ceiling = priceMap.ceilingEntry(calculationStart);
                    if (floor != null && ceiling != null) {
                        long duration = Duration.between(calculationInstant, ceiling.getKey()).toMinutes();
                        price += duration * floor.getValue();
                        iterator = iterator.plus(duration, ChronoUnit.MINUTES);
                    } else {
                        LOGGER.warn("Average price calculation out of range");
                        break;
                    }
                }
            } else {
                // skip this entry - there are not 24h prices available for calculation
            }
        }
    }

    public static JsonObject getJsonObject(JsonObject json, String[] path) {
        JsonObject iterator = json;
        for (int i = 0; i < path.length; i++) {
            if (iterator.has(path[i])) {
                iterator = iterator.getAsJsonObject(path[i]);
            } else {
                LOGGER.warn("Path {} not found in JSON {}", path[i], json.toString());
                return new JsonObject();
            }
        }
        return iterator;
    }

    public static String getJsonValue(JsonObject json, String key) {
        if (json.has(key)) {
            JsonElement elem = json.get(key);
            if (!elem.isJsonNull()) {
                return elem.getAsString();
            }
        }
        return EMPTY;
    }

    /**
     * Conversions for ThingAction calculations
     */

    // private Map<String, Object> getConfig(String config) {
    // Map<String, Object> configMap = new HashMap<>();
    // JsonObject jsonConfig = (JsonObject) JsonParser.parseString(config);
    // if (jsonConfig.has("earliestStart")) {
    // configMap.put("earliestStart", Instant.parse(jsonConfig.get("earliestStart").getAsString()));
    // } else {
    // configMap.put("earliestStart", Instant.now());
    // }
    // if (jsonConfig.has("latestEnd")) {
    // configMap.put("latestEnd", Instant.parse(jsonConfig.get("latestEnd").getAsString()));
    // } else {
    // configMap.put("latestEnd", priceInfoEnd());
    // }
    // if (jsonConfig.has("duration")) {
    // String duration = jsonConfig.get("duration").getAsString();
    // configMap.put("duration", parseDuration(duration));
    // } // no default duration
    // if (jsonConfig.has("power")) {
    // configMap.put("power", Instant.parse(jsonConfig.get("power").getAsString()));
    // } else {
    // configMap.put("power", 0);
    // }
    // if (jsonConfig.has("ascending")) {
    // configMap.put("ascending", Boolean.getBoolean(jsonConfig.get("latestEnd").getAsString()));
    // } else {
    // configMap.put("ascending", true);
    // }
    // return configMap;
    // }

    /**
     * Converts a duration String with or without unit into seconds. If no unit is attached default is seconds
     *
     * @param durationString duration with number and optional unit
     * @return duration in seconds
     */
    public static int parseDuration(String durationString) {
        String toBeParsed = durationString.strip();
        String[] split = toBeParsed.split(" ");
        try {
            int parsedDuration = Integer.parseInt(split[0]);
            if (split.length > 1) {
                switch (split[1].toLowerCase()) {
                    case "s":
                        // nothing to do
                        break;
                    case "m":
                        parsedDuration = parsedDuration * 60;
                        break;
                    case "h":
                        parsedDuration = parsedDuration * 60 * 60;
                        break;
                    default:
                        throw new CalculationParameterException("Cannot decode duration " + durationString);
                }
            }
            if (split.length > 2) {
                return parsedDuration + parseDuration(toBeParsed.substring(1 + split[0].length() + split[1].length()));
            } else {
                return parsedDuration;
            }
        } catch (NumberFormatException e) {
            throw new CalculationParameterException("Cannot decode duration " + durationString);
        }
    }

    /**
     * Converts given parameters object into Map
     *
     * @param parameters object to convert
     * @param targetMap parameter map to be filled
     * @return true if parameters were given as JSON String
     */
    public static boolean convertParameters(Object parameters, Map<String, Object> targetMap) {
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
    public static List<CurveEntry> convertCurve(JsonElement curve) {
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
                int durationValue = 0;
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

    public static State mapToState(String levelString) {
        State levelState = UnDefType.UNDEF;
        switch (levelString.toLowerCase()) {
            case PRICE_LEVEL_VERY_CHEAP:
                levelState = new DecimalType(-2);
                break;
            case PRICE_LEVEL_CHEAP:
                levelState = new DecimalType(-1);
                break;
            case PRICE_LEVEL_NORMAL:
                levelState = new DecimalType(0);
                break;
            case PRICE_LEVEL_EXPENSIVE:
                levelState = new DecimalType(1);
                break;
            case PRICE_LEVEL_VERY_EXPENSIVE:
                levelState = new DecimalType(2);
                break;
            default:
                LOGGER.warn("Level {} cannot be mapped", levelString);
                break;
        }
        return levelState;
    }
}
