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
package org.openhab.binding.solarforecast.internal.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.TimeSeries.Entry;

/**
 * The {@link Utils} Helpers for Solcast and ForecastSolar
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
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
}
