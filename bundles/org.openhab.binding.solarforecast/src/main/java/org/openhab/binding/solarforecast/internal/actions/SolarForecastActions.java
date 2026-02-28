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
package org.openhab.binding.solarforecast.internal.actions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actions to query forecast objects
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "solarforecast")
@NonNullByDefault
public class SolarForecastActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(SolarForecastActions.class);

    private @Nullable ThingHandler thingHandler;

    @RuleAction(label = "@text/actionDayLabel", description = "@text/actionDayDesc")
    public @ActionOutput(label = "Energy Of Day", type = "QuantityType<Energy>") QuantityType<Energy> getEnergyOfDay(
            @ActionInput(name = "localDate", label = "@text/actionInputDayLabel", description = "@text/actionInputDayDesc") LocalDate localDate,
            @ActionInput(name = "args") String... args) {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        if (forecasts.isEmpty()) {
            logger.debug("No forecasts found for {}", localDate);
            return Utils.getEnergyState(-1);
        }
        QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        for (SolarForecast forecast : forecasts) {
            QuantityType<Energy> qt = forecast.getDay(localDate, args);
            if (qt.floatValue() >= 0) {
                measure = measure.add(qt);
            } else {
                // break in case of failure getting values to avoid ambiguous values
                logger.debug("Ambiguous measure {} found for {}", qt, localDate);
                return Utils.getEnergyState(-1);
            }
        }
        return measure;
    }

    @RuleAction(label = "@text/actionPowerLabel", description = "@text/actionPowerDesc")
    public @ActionOutput(label = "Power", type = "QuantityType<Power>") QuantityType<Power> getPower(
            @ActionInput(name = "timestamp", label = "@text/actionInputDateTimeLabel", description = "@text/actionInputDateTimeDesc") Instant timestamp,
            @ActionInput(name = "args") String... args) {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        if (forecasts.isEmpty()) {
            logger.debug("No forecasts found for {}", timestamp);
            return Utils.getPowerState(-1);
        }
        QuantityType<Power> measure = QuantityType.valueOf(0, MetricPrefix.KILO(Units.WATT));
        for (SolarForecast forecast : forecasts) {
            QuantityType<Power> qt = forecast.getPower(timestamp, args);
            if (qt.floatValue() >= 0) {
                measure = measure.add(qt);
            } else {
                // break in case of failure getting values to avoid ambiguous values
                logger.debug("Ambiguous measure {} found for {}", qt, timestamp);
                return Utils.getPowerState(-1);
            }
        }
        return measure;
    }

    @RuleAction(label = "@text/actionEnergyLabel", description = "@text/actionEnergyDesc")
    public @ActionOutput(label = "Energy", type = "QuantityType<Energy>") QuantityType<Energy> getEnergy(
            @ActionInput(name = "start", label = "@text/actionInputDateTimeBeginLabel", description = "@text/actionInputDateTimeBeginDesc") Instant start,
            @ActionInput(name = "end", label = "@text/actionInputDateTimeEndLabel", description = "@text/actionInputDateTimeEndDesc") Instant end,
            @ActionInput(name = "args") String... args) {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        if (forecasts.isEmpty()) {
            logger.debug("No forecasts found for between {} and {}", start, end);
            return Utils.getEnergyState(-1);
        }
        QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        for (SolarForecast forecast : forecasts) {
            QuantityType<Energy> qt = forecast.getEnergy(start, end, args);
            if (qt.floatValue() >= 0) {
                measure = measure.add(qt);
            } else {
                // break in case of failure getting values to avoid ambiguous values
                logger.debug("Ambiguous measure {} found between {} and {}", qt, start, end);
                return Utils.getEnergyState(-1);
            }
        }
        return measure;
    }

    @RuleAction(label = "@text/actionForecastBeginLabel", description = "@text/actionForecastBeginDesc")
    public @ActionOutput(label = "Forecast Begin", type = "java.time.Instant") Instant getForecastBegin() {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        return Utils.getCommonStartTime(forecasts);
    }

    @RuleAction(label = "@text/actionForecastEndLabel", description = "@text/actionForecastEndDesc")
    public @ActionOutput(label = "Forecast End", type = "java.time.Instant") Instant getForecastEnd() {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        return Utils.getCommonEndTime(forecasts);
    }

    @RuleAction(label = "@text/actionTriggerUpdateLabel", description = "@text/actionTriggerUpdateDesc")
    public void triggerUpdate() {
        List<SolarForecast> forecasts = getProvider().getSolarForecasts();
        forecasts.forEach(forecast -> {
            forecast.triggerUpdate();
        });
    }

    public static QuantityType<Energy> getEnergyOfDay(ThingActions actions, LocalDate ld, String... args) {
        return ((SolarForecastActions) actions).getEnergyOfDay(ld, args);
    }

    public static QuantityType<Power> getPower(ThingActions actions, Instant dateTime, String... args) {
        return ((SolarForecastActions) actions).getPower(dateTime, args);
    }

    public static QuantityType<Energy> getEnergy(ThingActions actions, Instant begin, Instant end, String... args) {
        return ((SolarForecastActions) actions).getEnergy(begin, end, args);
    }

    public static Instant getForecastBegin(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastBegin();
    }

    public static Instant getForecastEnd(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastEnd();
    }

    public static void triggerUpdate(ThingActions actions) {
        ((SolarForecastActions) actions).triggerUpdate();
    }

    SolarForecastProvider getProvider() {
        if (thingHandler instanceof SolarForecastProvider provider) {
            return provider;
        } else {
            throw new IllegalStateException("ThingHandler " + thingHandler + " is not a SolarForecastProvider");
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        thingHandler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }
}
