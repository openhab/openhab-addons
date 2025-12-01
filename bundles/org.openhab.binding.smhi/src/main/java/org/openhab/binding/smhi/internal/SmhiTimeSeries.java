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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * A collection class with utility methods to retrieve forecasts pertaining to a specified time.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiTimeSeries implements Iterable<Forecast> {

    private final ZonedDateTime createdTime;
    private final ZonedDateTime referenceTime;
    private final List<Forecast> forecasts;

    public SmhiTimeSeries(ZonedDateTime createdTime, ZonedDateTime referenceTime, List<Forecast> forecasts) {
        this.createdTime = createdTime;
        this.referenceTime = referenceTime;
        this.forecasts = forecasts;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public ZonedDateTime getReferenceTime() {
        return referenceTime;
    }

    /**
     * Retrieves the first {@link Forecast} that is equal to or after offset time (from the reference time).
     *
     * @param hourOffset number of hours after now.
     * @return
     */
    public Optional<Forecast> getForecast(int hourOffset) {
        return getForecast(referenceTime, hourOffset);
    }

    /**
     * Retrieves the first {@link Forecast} that is equal to or after the offset time (from startTime).
     *
     * @param hourOffset number of hours after now.
     * @return
     */
    public Optional<Forecast> getForecast(ZonedDateTime startTime, int hourOffset) {
        if (hourOffset < 0) {
            throw new IllegalArgumentException("Offset must be at least 0");
        }

        for (Forecast forecast : forecasts) {
            if (forecast.getTime().compareTo(startTime.plusHours(hourOffset)) > 0) {
                return Optional.of(forecast);
            }
        }
        return Optional.empty();
    }

    /**
     * Get all Forecasts for the n:th day after the start of the TimeSeries
     *
     * @param dayOffset
     * @return
     */
    public List<Forecast> getDay(int dayOffset) {
        ZonedDateTime day = referenceTime.plusDays(dayOffset).truncatedTo(ChronoUnit.DAYS);
        return filter(forecast -> !forecast.getTime().isBefore(day) && forecast.getTime().isBefore(day.plusDays(1)));
    }

    public List<Forecast> filter(Predicate<Forecast> predicate) {
        return forecasts.stream().filter(predicate).toList();
    }

    public TimeSeries getTimeSeries(String parameter) {
        TimeSeries ts = new TimeSeries(TimeSeries.Policy.REPLACE);

        forecasts.forEach(f -> {
            State state = f.getParameterAsState(parameter);
            if (!(state instanceof UnDefType)) {
                ts.add(f.getTime().toInstant(), state);
            }
        });
        return ts;
    }

    @Override
    public Iterator<Forecast> iterator() {
        return forecasts.iterator();
    }

    @Override
    public void forEach(@Nullable Consumer<? super Forecast> action) {
        if (action == null) {
            throw new IllegalArgumentException();
        }
        for (Forecast f : forecasts) {
            action.accept(f);
        }
    }

    @Override
    public Spliterator<Forecast> spliterator() {
        return forecasts.spliterator();
    }
}
