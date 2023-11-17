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
package org.openhab.binding.solarforecast.internal.actions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
 * Actions to query forecast objects
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
            if (!l.isEmpty()) {
                QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getDay(localDate, args);
                    if (s instanceof QuantityType<?> quantityState) {
                        measure = measure.add((QuantityType<Energy>) quantityState);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.trace("Ambiguous measure {} found for {} - return UNDEF", s, localDate);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            } else {
                logger.trace("No forecasts found for {} - return UNDEF", localDate);
                return UnDefType.UNDEF;
            }
        } else {
            logger.trace("Handler missing - return UNDEF");
            return UnDefType.UNDEF;
        }
    }

    @RuleAction(label = "@text/actionPowerLabel", description = "@text/actionPowerDesc")
    public State getPower(
            @ActionInput(name = "timestamp", label = "@text/actionInputDateTimeLabel", description = "@text/actionInputDateTimeDesc") ZonedDateTime timestamp,
            String... args) {
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            if (!l.isEmpty()) {
                QuantityType<Power> measure = QuantityType.valueOf(0, MetricPrefix.KILO(Units.WATT));
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getPower(timestamp, args);
                    if (s instanceof QuantityType<?> quantityState) {
                        measure = measure.add((QuantityType<Power>) quantityState);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.trace("Ambiguous measure {} found for {} - return UNDEF", s, timestamp);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            } else {
                logger.trace("No forecasts found for {} - return UNDEF", timestamp);
                return UnDefType.UNDEF;
            }
        } else {
            logger.trace("Handler missing - return UNDEF");
            return UnDefType.UNDEF;
        }
    }

    @RuleAction(label = "@text/actionEnergyLabel", description = "@text/actionEnergyDesc")
    public State getEnergy(
            @ActionInput(name = "start", label = "@text/actionInputDateTimeBeginLabel", description = "@text/actionInputDateTimeBeginDesc") ZonedDateTime start,
            @ActionInput(name = "end", label = "@text/actionInputDateTimeEndLabel", description = "@text/actionInputDateTimeEndDesc") ZonedDateTime end,
            String... args) {
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            if (!l.isEmpty()) {
                QuantityType<Energy> measure = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    State s = solarForecast.getEnergy(start, end, args);
                    if (s instanceof QuantityType<?> quantityState) {
                        measure = measure.add((QuantityType<Energy>) quantityState);
                    } else {
                        // break in case of failure getting values to avoid ambiguous values
                        logger.trace("Ambiguous measure {} found between {} and {} - return UNDEF", s, start, end);
                        return UnDefType.UNDEF;
                    }
                }
                return measure;
            } else {
                logger.trace("No forecasts found for between {} and {} - return UNDEF", start, end);
                return UnDefType.UNDEF;
            }
        } else {
            logger.trace("Handler missing - return UNDEF");
            return UnDefType.UNDEF;
        }
    }

    @RuleAction(label = "@text/actionForecastBeginLabel", description = "@text/actionForecastBeginDesc")
    public ZonedDateTime getForecastBegin() {
        ZonedDateTime returnZdt = LocalDateTime.MAX.atZone(ZoneId.systemDefault());
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            if (!l.isEmpty()) {
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    ZonedDateTime begin = solarForecast.getForecastBegin();
                    // break in case of failure getting values to avoid ambiguous values
                    if (begin.toLocalDateTime().equals(LocalDateTime.MAX)) {
                        return LocalDateTime.MAX.atZone(ZoneId.systemDefault());
                    }
                    // take latest possible timestamp to avoid ambiguous values
                    if (begin.isBefore(returnZdt)) {
                        returnZdt = begin;
                    }
                }
                return returnZdt;
            } else {
                logger.trace("No forecasts found - return invalid date MAX");
                return LocalDateTime.MAX.atZone(ZoneId.systemDefault());
            }
        } else {
            logger.trace("Handler missing - return invalid date MAX");
            return LocalDateTime.MAX.atZone(ZoneId.systemDefault());
        }
    }

    @RuleAction(label = "@text/actionForecastEndLabel", description = "@text/actionForecastEndDesc")
    public ZonedDateTime getForecastEnd() {
        ZonedDateTime returnZdt = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        if (thingHandler.isPresent()) {
            List<SolarForecast> l = ((SolarForecastProvider) thingHandler.get()).getSolarForecasts();
            if (!l.isEmpty()) {
                for (Iterator<SolarForecast> iterator = l.iterator(); iterator.hasNext();) {
                    SolarForecast solarForecast = iterator.next();
                    ZonedDateTime forecastLdt = solarForecast.getForecastEnd();
                    // break in case of failure getting values to avoid ambiguous values
                    if (forecastLdt.equals(LocalDateTime.MIN)) {
                        return LocalDateTime.MIN.atZone(ZoneId.systemDefault());
                    }
                    // take earliest possible timestamp to avoid ambiguous values
                    if (forecastLdt.isAfter(returnZdt)) {
                        returnZdt = forecastLdt;
                    }
                }
                return returnZdt;
            } else {
                logger.trace("No forecasts found - return invalid date MIN");
                return LocalDateTime.MIN.atZone(ZoneId.systemDefault());
            }
        } else {
            logger.trace("Handler missing - return invalid date MIN");
            return LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        }
    }

    public static State getDay(ThingActions actions, LocalDate ld, String... args) {
        return ((SolarForecastActions) actions).getDay(ld, args);
    }

    public static State getPower(ThingActions actions, ZonedDateTime dateTime, String... args) {
        return ((SolarForecastActions) actions).getPower(dateTime, args);
    }

    public static State getEnergy(ThingActions actions, ZonedDateTime begin, ZonedDateTime end, String... args) {
        return ((SolarForecastActions) actions).getEnergy(begin, end, args);
    }

    public static ZonedDateTime getForecastBegin(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastBegin();
    }

    public static ZonedDateTime getForecastEnd(ThingActions actions) {
        return ((SolarForecastActions) actions).getForecastEnd();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
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
