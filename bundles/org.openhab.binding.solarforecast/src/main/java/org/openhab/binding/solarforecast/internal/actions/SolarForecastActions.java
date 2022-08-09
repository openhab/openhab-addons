/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionsS to query forecast objects
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "solarforecast")
@NonNullByDefault
public class SolarForecastActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(SolarForecastActions.class);
    private Optional<ThingHandler> thingHandler = Optional.empty();

    @RuleAction(label = "@text/actionDayLabel", description = "@text/actionDayDesc")
    public State getDay(
            @ActionInput(name = "localDate", label = "@text/actionInputDayLabel", description = "@text/actionInputDayDesc") LocalDate localDate,
            String... args) {
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            logger.trace("Found {} SolarForecast entries", l.size());
            if (!l.isEmpty()) {
                QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getDay(localDate, args);
                    logger.trace("Found measure {}", s);
                    if (s instanceof QuantityType<?>) {
                        measure = measure.add((QuantityType<Energy>) s);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.debug("Found measure {} - break", s);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            }
        }
        return UnDefType.UNDEF;
    }

    @RuleAction(label = "@text/actionPowerLabel", description = "@text/actionPowerDesc")
    public State getPower(
            @ActionInput(name = "localDateTime", label = "@text/actionInputDateTimeLabel", description = "@text/actionInputDateTimeDesc") LocalDateTime localDateTime,
            String... args) {
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            logger.trace("Found {} SolarForecast entries", l.size());
            if (!l.isEmpty()) {
                QuantityType<Power> measure = QuantityType.valueOf(0, MetricPrefix.KILO(Units.WATT));
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getPower(localDateTime, args);
                    logger.trace("Found measure {}", s);
                    if (s instanceof QuantityType<?>) {
                        measure = measure.add((QuantityType<Power>) s);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.debug("Found measure {} - break", s);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            }
        }
        return UnDefType.UNDEF;
    }

    @RuleAction(label = "@text/actionEnergyLabel", description = "@text/actionEnergyDesc")
    public State getEnergy(
            @ActionInput(name = "localDateTimeBegin", label = "@text/actionInputDateTimeBeginLabel", description = "@text/actionInputDateTimeBeginDesc") LocalDateTime localDateTimeBegin,
            @ActionInput(name = "localDateTimeEnd", label = "@text/actionInputDateTimeEndLabel", description = "@text/actionInputDateTimeEndDesc") LocalDateTime localDateTimeEnd,
            String... args) {
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            logger.trace("Found {} SolarForecast entries", l.size());
            if (!l.isEmpty()) {
                QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getEnergy(localDateTimeBegin, localDateTimeEnd, args);
                    logger.trace("Found measure {}", s);
                    if (s instanceof QuantityType<?>) {
                        measure = measure.add((QuantityType<Energy>) s);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.debug("Found measure {} - break", s);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            }
        }
        return UnDefType.UNDEF;
    }

    @RuleAction(label = "@text/actionForecastBeginLabel", description = "@text/actionForecastBeginDesc")
    public LocalDateTime getForecastBegin() {
        LocalDateTime returnLdt = LocalDateTime.MIN;
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            logger.trace("Found {} SolarForecast entries", l.size());
            if (!l.isEmpty()) {
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    LocalDateTime forecastLdt = solarForecast.getForecastBegin();
                    logger.trace("Found {}", forecastLdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    // break in case of failure getting values to avoid ambiguous values
                    if (forecastLdt.equals(LocalDateTime.MIN)) {
                        return LocalDateTime.MIN;
                    }
                    // take latest possible timestamp to avoid ambiguous values
                    if (forecastLdt.isAfter(returnLdt)) {
                        returnLdt = forecastLdt;
                        logger.trace("Set {}", forecastLdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                }
                return returnLdt;
            }
        }
        return LocalDateTime.MIN;
    }

    @RuleAction(label = "@text/actionForecastEndLabel", description = "@text/actionForecastEndDesc")
    public LocalDateTime getForecastEnd() {
        LocalDateTime returnLdt = LocalDateTime.MAX;
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            if (!l.isEmpty()) {
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    LocalDateTime forecastLdt = solarForecast.getForecastEnd();
                    // break in case of failure getting values to avoid ambiguous values
                    if (forecastLdt.equals(LocalDateTime.MIN)) {
                        return LocalDateTime.MAX;
                    }
                    // take earliest possible timestamp to avoid ambiguous values
                    if (forecastLdt.isBefore(returnLdt)) {
                        returnLdt = forecastLdt;
                    }
                }
                return returnLdt;
            }
        }
        return LocalDateTime.MAX;
    }

    public static State getDay(ThingActions actions, LocalDate ld, String... args) {
        return ((SolarForecastActions) actions).getDay(ld, args);
    }

    public static State getPower(ThingActions actions, LocalDateTime dateTime, String... args) {
        return ((SolarForecastActions) actions).getPower(dateTime, args);
    }

    public static State getEnergy(ThingActions actions, LocalDateTime begin, LocalDateTime end, String... args) {
        return ((SolarForecastActions) actions).getEnergy(begin, end, args);
    }

    public static LocalDateTime getForecastBegin(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastBegin();
    }

    public static LocalDateTime getForecastEnd(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastEnd();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        logger.trace("ThingHandler {} set", handler.toString());
        thingHandler = Optional.of(handler);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        if (thingHandler.isPresent()) {
            return thingHandler.get();
        }
        return null;
    }
}
