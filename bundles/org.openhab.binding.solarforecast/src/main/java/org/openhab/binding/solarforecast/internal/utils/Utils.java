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
package org.openhab.binding.solarforecast.internal.utils;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.KILOWATT_UNIT;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Utils} Helpers for Solcast and ForecastSolar
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };

    private static Clock clock = Clock.systemDefaultZone();

    /**
     * Only for unit testing setting a fixed clock with desired date-time
     *
     * @param c
     */
    public static void setClock(Clock c) {
        clock = c;
    }

    public static void setTimeZoneProvider(TimeZoneProvider tzp) {
        timeZoneProvider = tzp;
    }

    public static Clock getClock() {
        return clock.withZone(timeZoneProvider.getTimeZone());
    }

    public static QuantityType<Energy> getEnergyState(double d) {
        if (d < 0) {
            return QuantityType.valueOf(-1, Units.KILOWATT_HOUR);
        }
        return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, Units.KILOWATT_HOUR);
    }

    public static QuantityType<Power> getPowerState(double d) {
        if (d < 0) {
            return QuantityType.valueOf(-1, MetricPrefix.KILO(Units.WATT));
        }
        return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, MetricPrefix.KILO(Units.WATT));
    }

    public static void addState(TreeMap<Instant, QuantityType<?>> map, Entry entry) {
        Instant timestamp = entry.timestamp();
        QuantityType<?> qt1 = map.get(timestamp);
        if (qt1 != null) {
            QuantityType<?> qt2 = (QuantityType<?>) entry.state();
            double combinedValue = qt1.doubleValue() + qt2.doubleValue();
            map.put(timestamp, QuantityType.valueOf(combinedValue, qt2.getUnit()));
        } else {
            map.put(timestamp, (QuantityType<?>) entry.state());
        }
    }

    public static boolean isBeforeOrEqual(Instant query, Instant reference) {
        return !query.isAfter(reference);
    }

    public static boolean isAfterOrEqual(Instant query, Instant reference) {
        return !query.isBefore(reference);
    }

    public static Instant getCommonStartTime(List<SolarForecast> forecastObjects) {
        if (forecastObjects.isEmpty()) {
            return Instant.MAX;
        }
        Instant start = Instant.MIN;
        for (Iterator<SolarForecast> iterator = forecastObjects.iterator(); iterator.hasNext();) {
            SolarForecast sf = iterator.next();
            // if start is maximum there's no forecast data available - return immediately
            if (sf.getForecastBegin().equals(Instant.MAX)) {
                return Instant.MAX;
            } else if (sf.getForecastBegin().isAfter(start)) {
                // take latest timestamp from all forecasts
                start = sf.getForecastBegin();
            }
        }
        return start;
    }

    public static Instant getCommonEndTime(List<SolarForecast> forecastObjects) {
        if (forecastObjects.isEmpty()) {
            return Instant.MIN;
        }
        Instant end = Instant.MAX;
        for (Iterator<SolarForecast> iterator = forecastObjects.iterator(); iterator.hasNext();) {
            SolarForecast sf = iterator.next();
            // if end is minimum there's no forecast data available - return immediately
            if (sf.getForecastEnd().equals(Instant.MIN)) {
                return Instant.MIN;
            } else if (sf.getForecastEnd().isBefore(end)) {
                // take earliest timestamp from all forecast
                end = sf.getForecastEnd();
            }
        }
        return end;
    }

    public static @Nullable ZonedDateTime getZdtFromUTC(String utc) {
        try {
            Instant timestamp = Instant.parse(utc);
            return timestamp.atZone(timeZoneProvider.getTimeZone());
        } catch (DateTimeParseException dtpe) {
            LOGGER.warn("Exception parsing time {} Reason: {}", utc, dtpe.getMessage());
        }
        return null;
    }

    public static ZonedDateTime getZdtFromUTC(Instant utc) {
        return utc.atZone(timeZoneProvider.getTimeZone());
    }

    public static Instant now() {
        return Instant.now(clock);
    }

    /**
     * Check if an item has historic data in the persistence service
     *
     * @param item the item name to check
     * @param service the persistence service to query
     * @return true if there is historic data for the item, false otherwise
     */
    public static boolean checkPersistence(String item, QueryablePersistenceService service) {
        FilterCriteria fc = new FilterCriteria();
        fc.setBeginDate(ZonedDateTime.now().minusDays(1));
        fc.setEndDate(ZonedDateTime.now());
        fc.setItemName(item);
        Iterable<HistoricItem> historicItems = service.query(fc);
        return historicItems.iterator().hasNext();
    }

    /**
     * Get the energy produced by the power item since the beginning of the current day
     *
     * @param powerItemName the name of the power item
     * @param service the persistence service to query
     * @return the total energy produced in kWh
     */
    @SuppressWarnings("unchecked")
    public static double getEnergyTillNow(String powerItemName, QueryablePersistenceService service) {
        long startCalculation = System.currentTimeMillis();
        ZonedDateTime beginPeriodDT = ZonedDateTime.now(Utils.getClock()).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endPeriodDT = ZonedDateTime.now(Utils.getClock());
        FilterCriteria fc = new FilterCriteria();
        fc.setBeginDate(beginPeriodDT);
        fc.setEndDate(endPeriodDT);
        fc.setItemName(powerItemName);
        Iterable<HistoricItem> historicItems = service.query(fc);
        double total = 0;
        double lastPowerValue = -1;
        Instant lastTimeStamp = Instant.MAX;
        for (HistoricItem historicItem : historicItems) {
            State powerState = historicItem.getState();
            if (powerState instanceof QuantityType<?> qs) {
                QuantityType<Power> powerKWState = (QuantityType<Power>) qs.toInvertibleUnit(KILOWATT_UNIT);
                if (powerKWState != null) {
                    lastPowerValue = powerKWState.doubleValue();
                } else {
                    LOGGER.trace("Cannot convert Unit {} to {}", qs.getUnit(), KILOWATT_UNIT);
                    return 0;
                }
            } else {
                LOGGER.info("Power item {} has no unit. Skip Energy calculation!", powerItemName);
                return 0;
            }
            ZonedDateTime stateTimestamp = historicItem.getTimestamp();
            if (lastTimeStamp.isBefore(stateTimestamp.toInstant()) && lastPowerValue >= 0) {
                total += calcuateKwh(lastTimeStamp, stateTimestamp.toInstant(), lastPowerValue);
            } else {
                LOGGER.info("Skip timestamp {}", stateTimestamp);
            }
            lastTimeStamp = stateTimestamp.toInstant();
        }
        if (lastTimeStamp.isBefore(endPeriodDT.toInstant()) && lastPowerValue >= 0) {
            total += calcuateKwh(lastTimeStamp, endPeriodDT.toInstant(), lastPowerValue);
        }
        long calculationDuration = System.currentTimeMillis() - startCalculation;
        LOGGER.trace("Total power till now in kWh {} took {} ms", total, calculationDuration);
        return total;
    }

    /**
     * Calculate the energy in kWh based on the power in kW and the duration between two instants.
     *
     * @param begin the start instant
     * @param end the end instant
     * @param power the power in kW
     * @return the energy in kWh
     */
    private static double calcuateKwh(Instant begin, Instant end, double power) {
        long durationSeconds = Duration.between(begin, end).getSeconds();
        return power * durationSeconds / 3600;
    }
}
