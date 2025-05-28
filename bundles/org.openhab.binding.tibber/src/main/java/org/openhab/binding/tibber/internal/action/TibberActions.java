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
package org.openhab.binding.tibber.internal.action;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.Utils;
import org.openhab.binding.tibber.internal.calculator.CurveEntry;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.calculator.PriceInfo;
import org.openhab.binding.tibber.internal.calculator.ScheduleEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link TibberActions} which can be sent to a vehicle
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "tibber")
@NonNullByDefault
public class TibberActions implements ThingActions {
    private Optional<TibberHandler> thingHandler = Optional.empty();

    @RuleAction(label = "Price Info Start", description = "Earliest Price Info Start")
    public Instant priceInfoStart() {
        if (thingHandler.isPresent()) {
            PriceCalculator calc = thingHandler.get().getPriceCalculator();
            if (calc != null) {
                return calc.priceInfoStart();
            }
        }
        return Instant.MAX;
    }

    @RuleAction(label = "Price Info End", description = "Latest Price Info End")
    public Instant priceInfoEnd() {
        if (thingHandler.isPresent()) {
            PriceCalculator calc = thingHandler.get().getPriceCalculator();
            if (calc != null) {
                return calc.priceInfoEnd();
            }
        }
        return Instant.MIN;
    }

    @RuleAction(label = "List Prices", description = "List ascending / decending Prices")
    public String listPrices(Object parameterObject) {
        Map<String, Object> parameterMap = new HashMap<>();
        boolean jsonType = Utils.convertParameters(parameterObject, parameterMap);
        if (thingHandler.isPresent()) {
            PriceCalculator calc = thingHandler.get().getPriceCalculator();
            if (calc != null) {
                completeConfig(parameterMap);
                Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
                Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
                Boolean ascending = (Boolean) parameterMap.get(PARAM_ASCENDING);
                if (start != null && stop != null && ascending != null) {
                    List<PriceInfo> priceList = calc.listPrices(start, stop, ascending);
                    // if (jsonType) {
                    return "{\"size\":" + priceList.size() + ",\"priceList\":" + priceList.toString() + "}";
                    // }
                    // else {
                    // Map<String, Object> result = new HashMap<>();
                    // result.put("size", priceList.size());
                    // result.put("priceList", priceList);
                    // return result;
                    // }
                }
            }
        }
        // if (jsonType) {
        return "{}";
        // }
        // else {
        // return Map.of();
        // }
    }

    @RuleAction(label = "Best Price Period", description = "Best Price Period")
    public String bestPricePeriod(Object parameterObject) {
        Map<String, Object> parameterMap = new HashMap<>();
        boolean jsonType = Utils.convertParameters(parameterObject, parameterMap);
        if (thingHandler.isPresent()) {
            PriceCalculator calc = thingHandler.get().getPriceCalculator();
            if (calc != null) {
                completeConfig(parameterMap);
                Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
                Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
                if (start != null && stop != null) {
                    boolean onlyPeriod = false;
                    // check if curve is present
                    Object curve = parameterMap.get(PARAM_CURVE);
                    if (curve == null) {
                        // if no curve is given check for power and duration parameters
                        Object power = parameterMap.get(PARAM_POWER);
                        int powerValue = 0;
                        if (power == null) {
                            onlyPeriod = true;
                            // simulate with 1000 W otherwise min / max handling won't work in calculation
                            // cost values will be removed afterwards
                            powerValue = 1000;
                        } else {
                            powerValue = ((Integer) power).intValue();
                        }
                        Integer duration = (Integer) parameterMap.get(PARAM_DURATION);
                        if (duration == null) {
                            throw new CalculationParameterException(
                                    "No curve and no duration given for bestPeriod calculation " + parameterObject);
                        }
                        System.out.println("Calculate with power value " + powerValue);
                        CurveEntry entry = new CurveEntry(powerValue, duration.intValue());
                        curve = List.of(entry);
                    }
                    Map<String, Object> result = calc.calculateBestPrice(start, stop, (List<CurveEntry>) curve);
                    if (onlyPeriod) {
                        result.remove("highestPrice");
                        result.remove("lowestPrice");
                    }
                    // if (jsonType) {
                    Gson gson = new Gson();
                    return gson.toJson(result);
                    // } else {
                    // return result;
                    // }
                }
            }
        }
        // if (jsonType) {
        return "{}";
        // } else {
        // return Map.of();
        // }
    }

    @RuleAction(label = "Best Price Schedule", description = "Best Price Schedule")
    public String bestPriceSchedule(Object parameterObject) {
        Map<String, Object> parameterMap = new HashMap<>();
        boolean jsonType = Utils.convertParameters(parameterObject, parameterMap);
        if (thingHandler.isPresent()) {
            PriceCalculator calc = thingHandler.get().getPriceCalculator();
            if (calc != null) {
                completeConfig(parameterMap);
                Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
                Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
                Integer duration = (Integer) parameterMap.get(PARAM_DURATION);
                Integer power = (Integer) parameterMap.get(PARAM_POWER);
                if (start != null && stop != null && duration != null && power != null) {
                    List<ScheduleEntry> priceList = calc.calculateNonConsecutive(start, stop, power.intValue(),
                            duration.intValue());
                    double totalCost = 0;
                    for (Iterator<ScheduleEntry> iterator = priceList.iterator(); iterator.hasNext();) {
                        totalCost += iterator.next().cost;
                    }
                    // if (jsonType) {
                    Gson gson = new Gson();
                    JsonObject result = new JsonObject();
                    result.addProperty("cost", totalCost);
                    result.addProperty("size", priceList.size());
                    JsonArray priceListJson = (JsonArray) JsonParser.parseString(priceList.toString());
                    result.add("schedule", priceListJson);
                    return gson.toJson(result);
                    // } else {
                    // Map<String, Object> result = new HashMap<>();
                    // result.put("cost", totalCost);
                    // result.put("size", priceList.size());
                    // result.put("schedule", priceList);
                    // return result;
                    // }
                }
            }
        }
        // if (jsonType) {
        return "{}";
        // } else {
        // return Map.of();
        // }
    }

    public static Instant priceInfoStart(ThingActions actions) {
        return ((TibberActions) actions).priceInfoStart();
    }

    public static Instant priceInfoEnd(ThingActions actions) {
        return ((TibberActions) actions).priceInfoEnd();
    }

    public static Object listPrices(ThingActions actions, Object params) {
        return ((TibberActions) actions).listPrices(params);
    }

    public static Object bestPricePeriod(ThingActions actions, Object params) {
        return ((TibberActions) actions).bestPricePeriod(params);
    }

    public static Object bestPriceSchedule(ThingActions actions, Object params) {
        return ((TibberActions) actions).bestPriceSchedule(params);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        thingHandler = Optional.of((TibberHandler) handler);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        if (thingHandler.isPresent()) {
            return thingHandler.get();
        }
        return null;
    }

    /**
     * Fill parameters with default values if they are not give
     *
     * @param parameterMap
     */
    private void completeConfig(Map<String, Object> parameterMap) {
        if (!parameterMap.containsKey(PARAM_EARLIEST_START)) {
            parameterMap.put(PARAM_EARLIEST_START, Instant.now());
        }
        if (!parameterMap.containsKey(PARAM_LATEST_END)) {
            parameterMap.put(PARAM_LATEST_END, priceInfoEnd());
        }
        if (!parameterMap.containsKey(PARAM_ASCENDING)) {
            parameterMap.put(PARAM_ASCENDING, true);
        }
        Object duration = parameterMap.get(PARAM_DURATION);
        if (duration != null) {
            parameterMap.put(PARAM_DURATION, Utils.parseDuration(duration.toString()));
        }
    }
}
