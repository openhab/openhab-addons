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
package org.openhab.binding.tibber.internal.action;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.Utils;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.dto.PriceInfo;
import org.openhab.binding.tibber.internal.dto.ScheduleEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;
import org.openhab.binding.tibber.internal.exception.PriceCalculationException;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link TibberActions} for price calculations in rules
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "tibber")
@NonNullByDefault
public class TibberActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(TibberActions.class);

    private @Nullable TibberHandler thingHandler;

    @RuleAction(label = "@text/actionPriceInfoStartLabel", description = "@text/actionPriceInfoStartDescription")
    public @ActionOutput(name = "result", label = "Earliest Start", type = "java.time.Instant") Instant priceInfoStart() {
        TibberHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            logger.warn("No Thing attached to Actions! Maybe OFFLINE or Thing deactivated.");
            return Instant.MAX;
        }
        try {
            PriceCalculator calc = thingHandler.getPriceCalculator();
            return calc.priceInfoStart();
        } catch (PriceCalculationException e) {
            logger.warn("{}", e.getMessage());
            return Instant.MAX;
        }
    }

    @RuleAction(label = "@text/actionPriceInfoEndLabel", description = "@text/actionPriceInfoEndDescription")
    public @ActionOutput(name = "result", label = "Latest End", type = "java.time.Instant") Instant priceInfoEnd() {
        TibberHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            logger.warn("No Thing attached to Actions! Maybe OFFLINE or Thing deactivated.");
            return Instant.MIN;
        }
        try {
            PriceCalculator calc = thingHandler.getPriceCalculator();
            return calc.priceInfoEnd();
        } catch (PriceCalculationException e) {
            logger.warn("{}", e.getMessage());
            return Instant.MIN;
        }
    }

    @RuleAction(label = "@text/actionListPricesLabel", description = "@text/actionListPricesDescription")
    public @ActionOutput(name = "result", label = "@text/actionOutputResultLabel", type = "java.lang.String") String listPrices(
            @ActionInput(name = "parameters", label = "@text/actionInputParametersLabel", type = "java.lang.Object") Object parameters) {
        TibberHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            logger.warn("No Thing attached to Actions! Maybe OFFLINE or Thing deactivated.");
            return "";
        }
        try {
            Map<String, Object> parameterMap = new HashMap<>();
            Utils.convertParameters(parameters, parameterMap);
            PriceCalculator calc = thingHandler.getPriceCalculator();
            completeConfig(parameterMap);
            Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
            Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
            Boolean ascending = (Boolean) parameterMap.get(PARAM_ASCENDING);
            if (start != null && stop != null && ascending != null) {
                List<PriceInfo> priceList = calc.listPrices(start, stop, ascending);
                return "{\"size\":" + priceList.size() + ",\"priceList\":" + priceList.toString() + "}";
            } else {
                throw new CalculationParameterException("Cannot perform calculation with parameters " + parameterMap);
            }
        } catch (PriceCalculationException | CalculationParameterException e) {
            logger.warn("{}", e.getMessage());
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    @RuleAction(label = "@text/actionBestPricePeriodLabel", description = "@text/actionBestPricePeriodDescription")
    public @ActionOutput(name = "result", label = "@text/actionOutputResultLabel", type = "java.lang.String") String bestPricePeriod(
            @ActionInput(name = "parameters", label = "@text/actionInputParametersLabel", type = "java.lang.Object") Object parameters) {
        TibberHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            logger.warn("No Thing attached to Actions! Maybe OFFLINE or Thing deactivated.");
            return "";
        }
        try {
            Map<String, Object> parameterMap = new HashMap<>();
            Utils.convertParameters(parameters, parameterMap);
            PriceCalculator calc = thingHandler.getPriceCalculator();
            completeConfig(parameterMap);
            Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
            Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
            if (start != null && stop != null) {
                boolean onlyPeriod = false;
                // check if curve is present
                Object curve = parameterMap.get(PARAM_CURVE);
                List<CurveEntry> curveList = null;
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
                    Long duration = (Long) parameterMap.get(PARAM_DURATION);
                    if (duration == null) {
                        throw new CalculationParameterException(
                                "No curve and no duration given for bestPeriod calculation " + parameters);
                    }
                    CurveEntry entry = new CurveEntry(powerValue, duration.intValue());
                    curveList = List.of(entry);
                } else if (curve instanceof List list) {
                    curveList = list;
                } else {
                    throw new CalculationParameterException(
                            "No curve and no duration given for bestPeriod calculation " + parameters);
                }
                Map<String, Object> result = calc.calculateBestPrice(start, stop, curveList);
                if (onlyPeriod) {
                    result.remove("lowestPrice");
                    result.remove("averagePrice");
                    result.remove("highestPrice");
                }
                Gson gson = new Gson();
                return gson.toJson(result);
            } else {
                throw new CalculationParameterException("Cannot perform calculation with parameters " + parameterMap);
            }
        } catch (PriceCalculationException | CalculationParameterException e) {
            logger.warn("{}", e.getMessage());
            return "";
        }
    }

    @RuleAction(label = "@text/actionBestPriceScheduleLabel", description = "@text/actionBestPriceScheduleDescription")
    public @ActionOutput(name = "result", label = "@text/actionOutputResultLabel", type = "java.lang.String") String bestPriceSchedule(
            @ActionInput(name = "parameters", label = "@text/actionInputParametersLabel", type = "java.lang.Object") Object parameters) {
        TibberHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            logger.warn("No Thing attached to Actions! Maybe OFFLINE or Thing deactivated.");
            return "";
        }
        try {
            Map<String, Object> parameterMap = new HashMap<>();
            Utils.convertParameters(parameters, parameterMap);
            PriceCalculator calc = thingHandler.getPriceCalculator();
            completeConfig(parameterMap);
            Instant start = (Instant) parameterMap.get(PARAM_EARLIEST_START);
            Instant stop = (Instant) parameterMap.get(PARAM_LATEST_END);
            Long duration = (Long) parameterMap.get(PARAM_DURATION);
            Integer power = (Integer) parameterMap.get(PARAM_POWER);
            if (start != null && stop != null && duration != null && power != null) {
                List<ScheduleEntry> priceList = calc.calculateNonConsecutive(start, stop, power.intValue(),
                        duration.intValue());
                double totalCost = 0;
                for (Iterator<ScheduleEntry> iterator = priceList.iterator(); iterator.hasNext();) {
                    totalCost += iterator.next().cost;
                }
                Gson gson = new Gson();
                JsonObject result = new JsonObject();
                result.addProperty("cost", totalCost);
                result.addProperty("size", priceList.size());
                JsonArray priceListJson = (JsonArray) JsonParser.parseString(priceList.toString());
                result.add("schedule", priceListJson);
                return gson.toJson(result);
            } else {
                throw new CalculationParameterException("Cannot perform calculation with parameters " + parameterMap);
            }
        } catch (PriceCalculationException | CalculationParameterException e) {
            logger.warn("{}", e.getMessage());
            return "";
        }
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
        if (handler instanceof TibberHandler tibberHandler) {
            this.thingHandler = tibberHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    /**
     * Fill parameters with default values if they are not given
     *
     * @param parameterMap
     */
    private void completeConfig(Map<String, Object> parameterMap) throws CalculationParameterException {
        parameterMap.putIfAbsent(PARAM_EARLIEST_START, Instant.now());
        parameterMap.putIfAbsent(PARAM_LATEST_END, priceInfoEnd());
        parameterMap.putIfAbsent(PARAM_ASCENDING, true);
        Object duration = parameterMap.get(PARAM_DURATION);
        if (duration != null) {
            parameterMap.put(PARAM_DURATION, Utils.parseDuration(duration.toString()));
        }
    }
}
