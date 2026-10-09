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
package org.openhab.automation.optimalwindow.internal.handler;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.optimalwindow.internal.calc.TimeRange;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.automation.optimalwindow.internal.calc.WindowConfiguration;
import org.openhab.automation.optimalwindow.internal.calc.WindowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of the optimal window of a trigger or a condition.
 *
 * The window is recalculated when the forecast item receives a new time series, when the search range moves on, and
 * at least every {@link #MAX_RESULT_AGE}. Once a window has started, it is kept until the end of its search range, so
 * new forecast values cannot interrupt it and it runs only once per range. If the next search range starts before the
 * window of the previous range has ended, e.g. with ranges longer than 24 hours, the previous window is kept until it
 * has ended, also if it has not started yet.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class WindowTracker {
    static final Duration MAX_RESULT_AGE = Duration.ofMinutes(15);

    private final Logger logger = LoggerFactory.getLogger(WindowTracker.class);
    private final WindowCalculator calculator;
    private final WindowConfiguration config;

    private @Nullable WindowResult result;
    private @Nullable TimeRange calculatedRange;
    private Instant calculatedAt = Instant.EPOCH;

    public WindowTracker(WindowCalculator calculator, WindowConfiguration config) {
        this.calculator = calculator;
        this.config = config;
    }

    /**
     * @return the name of the forecast item
     */
    public String getForecastItem() {
        return config.forecastItem;
    }

    /**
     * Called when the forecast item received a new time series. The window is recalculated on the next call of
     * {@link #getResult(ZonedDateTime)}, which also gives the persistence service time to store the new values.
     */
    public synchronized void forecastUpdated() {
        calculatedAt = Instant.EPOCH;
    }

    /**
     * Returns the optimal window for the given time.
     *
     * @param now the current time
     *
     * @return the window, or {@code null} if it cannot be calculated, e.g. because the forecast does not cover the
     *         search range
     *
     * @throws RuntimeException if the forecast cannot be retrieved
     */
    public synchronized @Nullable WindowResult getResult(ZonedDateTime now) {
        TimeRange range = WindowCalculator.getRange(config.rangeStart, config.rangeDuration, now);
        Instant instant = now.toInstant();
        WindowResult previous = result;
        if (previous != null && isKept(previous, range, instant)) {
            return previous;
        }
        if (range.equals(calculatedRange) && calculatedAt.plus(MAX_RESULT_AGE).isAfter(instant)) {
            return previous;
        }

        WindowResult calculated = calculator.calculate(config, now);
        logger.trace("Optimal window for {}: {}", config, calculated);
        result = calculated;
        calculatedRange = range;
        calculatedAt = instant;
        return calculated;
    }

    private static boolean isKept(WindowResult result, TimeRange range, Instant now) {
        TimeRange searchRange = result.getSearchRange();
        if (!searchRange.contains(now)) {
            return false;
        }
        if (searchRange.equals(range)) {
            // a started window is not interrupted by new forecast values and runs only once per range
            return !result.getStart().isAfter(now);
        }
        // the next range has already started, the window of the previous range is kept until it has ended
        return result.getEnd().isAfter(now);
    }
}
