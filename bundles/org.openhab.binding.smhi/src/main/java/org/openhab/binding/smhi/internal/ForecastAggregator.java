/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class ForecastAggregator {
    public static Optional<BigDecimal> max(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter)).filter(Optional::isPresent)
                .map(Optional::get).max(BigDecimal::compareTo);
    }

    public static Optional<BigDecimal> min(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        return dayForecasts.stream().map(forecast -> forecast.getParameter(parameter)).filter(Optional::isPresent)
                .map(Optional::get).min(BigDecimal::compareTo);
    }

    public static Optional<BigDecimal> total(TimeSeries timeSeries, int dayOffset, String parameter) {
        List<Forecast> dayForecasts = timeSeries.getDay(dayOffset);
        BigDecimal sum = dayForecasts.stream().map(forecast -> forecast.getParameter(parameter))
                .filter(Optional::isPresent).map(Optional::get).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(dayForecasts.size()), RoundingMode.HALF_UP);
        return Optional.of(mean.multiply(BigDecimal.valueOf(24)));
    }
}
