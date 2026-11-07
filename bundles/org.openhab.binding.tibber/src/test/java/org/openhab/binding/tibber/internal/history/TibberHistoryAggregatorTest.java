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
package org.openhab.binding.tibber.internal.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.ANNUAL;
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.DAILY;
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.MONTHLY;
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.WEEKLY;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_CONSUMPTION;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_COST;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_PRODUCTION;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link TibberHistoryAggregator}: the running (incomplete) period of a coarser resolution is
 * synthesized from the next finer resolution. The running month in particular is aggregated from DAILY data,
 * because WEEKLY entries are dated at the start of a completed ISO week and only rarely fall into the current
 * month.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHistoryAggregatorTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final TibberHistoryAggregator AGGREGATOR = new TibberHistoryAggregator(ZONE);

    private static final String CONSUMPTION_1 = "1.500 kWh";
    private static final String CONSUMPTION_2 = "0.533 kWh";
    private static final String COST_1 = "0.300 EUR";
    private static final String COST_2 = "0.150 EUR";
    private static final String PRODUCTION_1 = "0.400 kWh";
    private static final String PRODUCTION_2 = "0.600 kWh";

    @Test
    void whenFinerSeriesEmptyThenCoarserSeriesReturnedUnchanged() {
        // Arrange
        TibberHistorySeries finerSeries = new TibberHistorySeries(null);
        TibberHistorySeries coarserSeries = new TibberHistorySeries(null);
        coarserSeries.put(startOfPreviousMonth().toInstant(), data("30.0 kWh", "15.0 EUR", "2.0 kWh"));

        // Act
        TibberHistorySeries result = AGGREGATOR.fillCurrentPeriod(DAILY, MONTHLY, finerSeries, coarserSeries);

        // Assert
        assertSame(coarserSeries, result);
    }

    @Test
    void whenWeeklyHasNoEntriesInCurrentMonthThenSeriesReturnedUnchanged() {
        // Arrange
        ZonedDateTime monthStart = startOfCurrentMonth();
        ZonedDateTime mondayBeforeMonth = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (!mondayBeforeMonth.isBefore(monthStart)) {
            mondayBeforeMonth = mondayBeforeMonth.minusWeeks(1);
        }
        TibberHistorySeries weeklySeries = new TibberHistorySeries(null);
        weeklySeries.put(mondayBeforeMonth.toInstant(), data("9.5 kWh", "4.2 EUR", "0.0 kWh"));
        TibberHistorySeries monthlySeries = new TibberHistorySeries(null);
        monthlySeries.put(startOfPreviousMonth().toInstant(), data("30.0 kWh", "15.0 EUR", "2.0 kWh"));

        // Act
        TibberHistorySeries result = AGGREGATOR.fillCurrentPeriod(WEEKLY, MONTHLY, weeklySeries, monthlySeries);

        // Assert
        assertSame(monthlySeries, result);
        assertFalse(result.containsKey(monthStart.toInstant()));
    }

    @Test
    void whenDailyCoversCurrentMonthThenSyntheticMonthlyEntryIsInjected() {
        // Arrange
        assumeTrue(LocalDate.now(ZONE).getDayOfMonth() >= 3, "needs two completed days in the current month");
        ZonedDateTime monthStart = startOfCurrentMonth();
        ZonedDateTime todayStart = LocalDate.now(ZONE).atStartOfDay(ZONE);
        TibberHistorySeries dailySeries = new TibberHistorySeries(null);
        dailySeries.put(todayStart.minusDays(1).toInstant(), data(CONSUMPTION_1, COST_1, PRODUCTION_1));
        dailySeries.put(todayStart.minusDays(2).toInstant(), data(CONSUMPTION_2, COST_2, PRODUCTION_2));
        TibberHistorySeries monthlySeries = new TibberHistorySeries(null);
        monthlySeries.put(startOfPreviousMonth().toInstant(), data("30.0 kWh", "15.0 EUR", "2.0 kWh"));

        // Act
        TibberHistorySeries result = AGGREGATOR.fillCurrentPeriod(DAILY, MONTHLY, dailySeries, monthlySeries);

        // Assert
        assertEquals(monthlySeries.size() + 1, result.size());
        JsonObject synthetic = Objects.requireNonNull(result.get(monthStart.toInstant()));
        assertEquals("2.0330 kWh", synthetic.get(PURPOSE_CONSUMPTION).getAsString());
        assertEquals("0.4500 EUR", synthetic.get(PURPOSE_COST).getAsString());
        assertEquals("1.0000 kWh", synthetic.get(PURPOSE_PRODUCTION).getAsString());
    }

    @Test
    void whenAnnualAggregatedFromEnrichedMonthlyThenCurrentYearIncludesSyntheticMonth() {
        // Arrange
        assumeTrue(LocalDate.now(ZONE).getDayOfMonth() >= 3, "needs two completed days in the current month");
        ZonedDateTime monthStart = startOfCurrentMonth();
        ZonedDateTime yearStart = startOfCurrentYear();
        ZonedDateTime todayStart = LocalDate.now(ZONE).atStartOfDay(ZONE);
        TibberHistorySeries dailySeries = new TibberHistorySeries(null);
        dailySeries.put(todayStart.minusDays(1).toInstant(), data(CONSUMPTION_1, COST_1, PRODUCTION_1));
        dailySeries.put(todayStart.minusDays(2).toInstant(), data(CONSUMPTION_2, COST_2, PRODUCTION_2));
        boolean previousMonthInCurrentYear = monthStart.getMonth() != Month.JANUARY;
        TibberHistorySeries monthlySeries = new TibberHistorySeries(null);
        if (previousMonthInCurrentYear) {
            monthlySeries.put(startOfPreviousMonth().toInstant(), data("5.000 kWh", "2.500 EUR", "1.000 kWh"));
        }
        TibberHistorySeries enrichedMonthly = AGGREGATOR.fillCurrentPeriod(DAILY, MONTHLY, dailySeries, monthlySeries);
        TibberHistorySeries annualSeries = new TibberHistorySeries(null);

        // Act
        TibberHistorySeries enrichedAnnual = AGGREGATOR.fillCurrentPeriod(MONTHLY, ANNUAL, enrichedMonthly,
                annualSeries);

        // Assert
        assertEquals(1, enrichedAnnual.size());
        JsonObject synthetic = Objects.requireNonNull(enrichedAnnual.get(yearStart.toInstant()));
        String expectedConsumption = previousMonthInCurrentYear ? "7.0330 kWh" : "2.0330 kWh";
        assertEquals(expectedConsumption, synthetic.get(PURPOSE_CONSUMPTION).getAsString());
    }

    @Test
    void whenEntryAtOrAfterCurrentHourThenItIsNotSummed() {
        // Arrange
        TibberHistorySeries dailySeries = new TibberHistorySeries(null);
        dailySeries.put(Instant.now().truncatedTo(ChronoUnit.HOURS), data(CONSUMPTION_1, COST_1, PRODUCTION_1));
        TibberHistorySeries monthlySeries = new TibberHistorySeries(null);

        // Act
        TibberHistorySeries result = AGGREGATOR.fillCurrentPeriod(DAILY, MONTHLY, dailySeries, monthlySeries);

        // Assert
        assertSame(monthlySeries, result);
    }

    @Test
    void whenNoFinerDataForCurrentPeriodThenCoarserSeriesUnchanged() {
        // Arrange
        ZonedDateTime monthStart = startOfCurrentMonth();
        TibberHistorySeries dailySeries = new TibberHistorySeries(null);
        dailySeries.put(monthStart.minusDays(10).toInstant(), data(CONSUMPTION_1, COST_1, PRODUCTION_1));
        TibberHistorySeries monthlySeries = new TibberHistorySeries(null);

        // Act
        TibberHistorySeries result = AGGREGATOR.fillCurrentPeriod(DAILY, MONTHLY, dailySeries, monthlySeries);

        // Assert
        assertSame(monthlySeries, result);
        assertFalse(result.containsKey(monthStart.toInstant()));
    }

    private static ZonedDateTime startOfCurrentMonth() {
        return LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE);
    }

    private static ZonedDateTime startOfPreviousMonth() {
        return startOfCurrentMonth().minusMonths(1);
    }

    private static ZonedDateTime startOfCurrentYear() {
        return LocalDate.now(ZONE).withDayOfYear(1).atStartOfDay(ZONE);
    }

    private static JsonObject data(String consumption, String cost, String production) {
        JsonObject data = new JsonObject();
        data.addProperty(PURPOSE_CONSUMPTION, consumption);
        data.addProperty(PURPOSE_COST, cost);
        data.addProperty(PURPOSE_PRODUCTION, production);
        return data;
    }
}
