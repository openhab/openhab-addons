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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
     * @param metadata
     * @return
     */
    public static State max(SmhiTimeSeries timeSeries, int dayOffset, @Nullable ParameterMetadata metadata) {
        if (metadata == null) {
            return UnDefType.UNDEF;
        }

        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset, false);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(metadata.name()))
                .filter(p -> !metadata.missingValue().equals(p)).max(BigDecimal::compareTo)
                .map(value -> Util.getParameterAsState(metadata, value))
                .orElseGet(() -> Util.getParameterAsState(metadata, metadata.missingValue()));
    }

    /**
     * Get the minimum value for the specified parameter for the n:th day after the forecast's reference time
     *
     * @param timeSeries
     * @param dayOffset
     * @param metadata
     * @return
     */
    public static State min(SmhiTimeSeries timeSeries, int dayOffset, @Nullable ParameterMetadata metadata) {
        if (metadata == null) {
            return UnDefType.UNDEF;
        }

        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset, false);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(metadata.name()))
                .filter(p -> !metadata.missingValue().equals(p)).min(BigDecimal::compareTo)
                .map(value -> Util.getParameterAsState(metadata, value))
                .orElseGet(() -> Util.getParameterAsState(metadata, metadata.missingValue()));
    }

    /**
     * Get the total value for the specified parameter for the n:th day after the forecast's reference time.
     * If there aren't any values for every hour, the previous value is used for each empty slot.
     *
     * @param timeSeries
     * @param dayOffset
     * @param baseMetadata
     * @param totalMetadata
     * @return
     */
    public static State total(SmhiTimeSeries timeSeries, int dayOffset, @Nullable ParameterMetadata baseMetadata,
            @Nullable ParameterMetadata totalMetadata) {
        if (baseMetadata == null || totalMetadata == null) {
            return UnDefType.UNDEF;
        }

        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset, true);
        if (dayForecasts.size() == 1) {
            return dayForecasts.getFirst().getParameterAsState(baseMetadata);
        }
        return dayForecasts.stream().map(forecast -> {
            BigDecimal hours = BigDecimal
                    .valueOf(forecast.getIntervalStartTime().until(forecast.getTime(), ChronoUnit.HOURS));
            return forecast.getParameter(baseMetadata.name()).multiply(hours);
        }).reduce(BigDecimal::add).map(value -> Util.getParameterAsState(totalMetadata, value))
                .orElseGet(() -> Util.getParameterAsState(totalMetadata, DEFAULT_MISSING_VALUE));
    }

    /**
     * Get the value at 12:00 UTC for the specified parameter for the n:th day after the forecast's reference time.
     * If that time is not included (should only happen for day 0 if after 12:00), get the first value for the day
     * instead.
     *
     * @param timeSeries
     * @param dayOffset
     * @param metadata
     * @return
     */
    public static State noonOrFirst(SmhiTimeSeries timeSeries, int dayOffset, @Nullable ParameterMetadata metadata) {
        if (metadata == null) {
            return UnDefType.UNDEF;
        }

        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset, false);
        return dayForecasts.stream().filter(forecast -> forecast.getTime().getHour() >= 12).findFirst()
                .map(f -> f.getParameterAsState(metadata))
                .orElseGet(() -> Util.getParameterAsState(metadata, metadata.missingValue()));
    }
}
