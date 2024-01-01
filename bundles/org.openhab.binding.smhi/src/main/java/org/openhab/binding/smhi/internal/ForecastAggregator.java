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
package org.openhab.binding.smhi.internal;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class ForecastAggregator {
    /**
     * Get the maximum value for the specified parameter for the n:th day after the forecast's reference time
     *
     * @param timeSeries
     * @param dayOffset
     * @param parameter
     * @return
     */
    public static Optional<BigDecimal> max(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter)).filter(Optional::isPresent)
                .map(Optional::get).max(BigDecimal::compareTo);
    }

    /**
     * Get the minimum value for the specified parameter for the n:th day after the forecast's reference time
     *
     * @param timeSeries
     * @param dayOffset
     * @param parameter
     * @return
     */
    public static Optional<BigDecimal> min(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter)).filter(Optional::isPresent)
                .map(Optional::get).min(BigDecimal::compareTo);
    }

    /**
     * Get the total value for the specified parameter for the n:th day after the forecast's reference time.
     * If there aren't any values for every hour, the previous value is used for each empty slot.
     *
     * @param timeSeries
     * @param dayOffset
     * @param parameter
     * @return
     */
    public static Optional<BigDecimal> total(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        if (dayForecasts.size() == 1) {
            return dayForecasts.get(0).getParameter(parameter);
        }
        List<BigDecimal> values = new ArrayList<>();
        for (int i = 0; i < dayForecasts.size(); i++) {
            Forecast current = dayForecasts.get(i);
            long hours;
            if (i == 0) {
                hours = current.getValidTime().until(dayForecasts.get(i + 1).getValidTime(), ChronoUnit.HOURS);
            } else {
                hours = dayForecasts.get(i - 1).getValidTime().until(current.getValidTime(), ChronoUnit.HOURS);
            }
            values.add(current.getParameter(parameter).map(value -> value.multiply(BigDecimal.valueOf(hours)))
                    .orElse(BigDecimal.ZERO));
        }
        return values.stream().reduce(BigDecimal::add);
    }

    /**
     * Get the value at 12:00 UTC for the specified parameter for the n:th day after the forecast's reference time.
     * If that time is not included (should only happen for day 0 if after 12:00), get the first value for the day
     * instead.
     *
     * @param timeSeries
     * @param dayOffset
     * @param parameter
     * @return
     */
    public static Optional<BigDecimal> noonOrFirst(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().filter(forecast -> forecast.getValidTime().getHour() >= 12).findFirst()
                .flatMap(forecast -> forecast.getParameter(parameter));
    }
}
