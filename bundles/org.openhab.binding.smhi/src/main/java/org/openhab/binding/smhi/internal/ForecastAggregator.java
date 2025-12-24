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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.DEFAULT_MISSING_VALUE;
import static org.openhab.binding.smhi.internal.SmhiBindingConstants.PRECIPITATION_TOTAL;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

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
    public static State max(SmhiTimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter))
                .filter(p -> !DEFAULT_MISSING_VALUE.equals(p)).max(BigDecimal::compareTo)
                .map(value -> Util.getParameterAsState(parameter, value))
                .orElseGet(() -> Util.getParameterAsState(parameter, DEFAULT_MISSING_VALUE));
    }

    /**
     * Get the minimum value for the specified parameter for the n:th day after the forecast's reference time
     *
     * @param timeSeries
     * @param dayOffset
     * @param parameter
     * @return
     */
    public static State min(SmhiTimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter))
                .filter(p -> !DEFAULT_MISSING_VALUE.equals(p)).min(BigDecimal::compareTo)
                .map(value -> Util.getParameterAsState(parameter, value))
                .orElseGet(() -> Util.getParameterAsState(parameter, DEFAULT_MISSING_VALUE));
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
    public static State total(SmhiTimeSeries timeSeries, int dayOffset, String parameter) {
        ZonedDateTime start = timeSeries.getReferenceTime().plusDays(dayOffset).withHour(0);
        ZonedDateTime end = start.plusDays(1);
        List<Forecast> dayForecasts = timeSeries
                .filter(forecast -> forecast.getTime().isAfter(start) && !forecast.getTime().isAfter(end));
        if (dayForecasts.size() == 1) {
            return dayForecasts.getFirst().getParameterAsState(parameter);
        }
        return dayForecasts.stream().map(forecast -> {
            BigDecimal hours = BigDecimal
                    .valueOf(forecast.getIntervalStartTime().until(forecast.getTime(), ChronoUnit.HOURS));
            return forecast.getParameter(parameter).multiply(hours);
        }).reduce(BigDecimal::add).map(value -> Util.getParameterAsState(PRECIPITATION_TOTAL, value))
                .orElseGet(() -> Util.getParameterAsState(PRECIPITATION_TOTAL, DEFAULT_MISSING_VALUE));
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
    public static State noonOrFirst(SmhiTimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().filter(forecast -> forecast.getTime().getHour() >= 12).findFirst()
                .map(f -> f.getParameterAsState(parameter))
                .orElseGet(() -> Util.getParameterAsState(parameter, DEFAULT_MISSING_VALUE));
    }
}
