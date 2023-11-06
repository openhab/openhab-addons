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
package org.openhab.binding.smhi.internal;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A collection class with utility methods to retrieve forecasts pertaining to a specified time.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class TimeSeries implements Iterable<Forecast> {

    private final ZonedDateTime referenceTime;
    private final List<Forecast> forecasts;

    public TimeSeries(ZonedDateTime referenceTime, List<Forecast> forecasts) {
        this.referenceTime = referenceTime;
        this.forecasts = forecasts;
    }

    public ZonedDateTime getReferenceTime() {
        return referenceTime;
    }

    /**
     * Retrieves the first {@link Forecast} that is equal to or after offset time (from now).
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
            if (forecast.getValidTime().compareTo(startTime.plusHours(hourOffset)) > 0) {
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
        ZonedDateTime day = referenceTime.plusDays(dayOffset).plusHours(1);
        return forecasts.stream().filter(forecast -> forecast.getValidTime().getDayOfMonth() == day.getDayOfMonth())
                .collect(Collectors.toList());
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
