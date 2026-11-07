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
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.MONTHLY;
import static org.openhab.binding.tibber.internal.history.TibberHistory.TimeWindow.WEEKLY;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_CONSUMPTION;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_COST;
import static org.openhab.binding.tibber.internal.history.TibberHistorySeries.PURPOSE_PRODUCTION;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link TibberHistoryCurrentPeriods}: the completed days of the running week, month and year are
 * summed from the DAILY series, today and future entries are excluded and live values are added to the base.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHistoryCurrentPeriodsTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    @Test
    void whenDailyEntriesBeforeTodayThenCurrentPeriodSumsAreComputed() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        Instant startOfToday = startOfToday(nowZdt);
        TibberHistorySeries daily = new TibberHistorySeries(null);
        // The three fixtures use different purposes so that each window assertion is independent
        daily.put(startOfToday.minus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), data("1.5 kWh", null, null));
        daily.put(startOfCurrentMonth(nowZdt).plus(1, ChronoUnit.HOURS).toInstant(), data(null, "0.25", null));
        daily.put(startOfCurrentYear(nowZdt).toInstant(), data(null, null, "3.0 kWh"));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        boolean weekHasCompletedDay = nowZdt.getDayOfWeek() != DayOfWeek.MONDAY;
        assertState(weekHasCompletedDay ? QuantityType.valueOf("1.5 kWh") : null,
                values.get(WEEKLY, PURPOSE_CONSUMPTION));
        boolean monthHasCompletedDay = nowZdt.getDayOfMonth() > 1;
        assertState(monthHasCompletedDay ? DecimalType.valueOf("0.25") : null, values.get(MONTHLY, PURPOSE_COST));
        boolean yearHasCompletedDay = nowZdt.getDayOfYear() > 1;
        assertState(yearHasCompletedDay ? QuantityType.valueOf("3.0 kWh") : null,
                values.get(ANNUAL, PURPOSE_PRODUCTION));
    }

    @Test
    void whenEntryIsTodayOrLaterThenItIsNotIncluded() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        Instant startOfToday = startOfToday(nowZdt);
        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(startOfToday, data("1.5 kWh", "0.15", "0.5 kWh"));
        daily.put(startOfToday.plus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), data("2.5 kWh", "0.25", "0.7 kWh"));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        assertNull(values.get(WEEKLY, PURPOSE_CONSUMPTION));
        assertNull(values.get(MONTHLY, PURPOSE_COST));
        assertNull(values.get(ANNUAL, PURPOSE_PRODUCTION));
    }

    @Test
    void whenEntryIsBeforeCurrentPeriodThenItIsIgnored() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(startOfCurrentMonth(nowZdt).minusDays(40).toInstant(), data("1.5 kWh", "0.15", null));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        assertNull(values.get(WEEKLY, PURPOSE_CONSUMPTION));
        assertNull(values.get(WEEKLY, PURPOSE_COST));
        assertNull(values.get(MONTHLY, PURPOSE_CONSUMPTION));
        assertNull(values.get(MONTHLY, PURPOSE_COST));
    }

    @Test
    void whenDailySeriesIsEmptyThenValuesAreNotFreshAndGetReturnsNull() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(new TibberHistorySeries(null),
                ZONE, now);

        // Assert
        assertNull(values.newestDailyEntry());
        assertFalse(values.isFresh(ZONE, now));
        assertNull(values.get(WEEKLY, PURPOSE_CONSUMPTION));
        assertNull(values.get(MONTHLY, PURPOSE_COST));
        assertNull(values.get(ANNUAL, PURPOSE_PRODUCTION));
    }

    @Test
    void whenNewestEntryIsYesterdayThenValuesAreFresh() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        Instant newestEntry = startOfToday(nowZdt).minus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS);
        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(newestEntry, data("1.5 kWh", "0.15", null));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        assertEquals(newestEntry, values.newestDailyEntry());
        assertTrue(values.isFresh(ZONE, now));
    }

    @Test
    void whenNewestEntryIsOlderThanYesterdayThenValuesAreNotFresh() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        Instant newestEntry = startOfToday(nowZdt).minus(2, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS);
        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(newestEntry, data("1.5 kWh", "0.15", null));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        assertEquals(newestEntry, values.newestDailyEntry());
        assertFalse(values.isFresh(ZONE, now));
    }

    @Test
    void whenQuantityStatesAddedThenValuesAreSummed() {
        // Arrange
        State completed = QuantityType.valueOf("1.5 kWh");
        State today = QuantityType.valueOf("0.5 kWh");

        // Act
        State result = TibberHistoryCurrentPeriods.add(completed, today);

        // Assert
        assertEquals(QuantityType.valueOf("2.0 kWh"), result);
    }

    @Test
    void whenDecimalStatesAddedThenValuesAreSummed() {
        // Arrange
        State completed = DecimalType.valueOf("0.15");
        State today = DecimalType.valueOf("0.20");

        // Act
        State result = TibberHistoryCurrentPeriods.add(completed, today);

        // Assert
        assertEquals(DecimalType.valueOf("0.35"), result);
    }

    @Test
    void whenStatesHaveIncompatibleTypesOrUnitsThenAddReturnsNull() {
        // Arrange
        State quantity = QuantityType.valueOf("1.5 kWh");
        State decimal = DecimalType.valueOf("0.15");

        // Assert
        assertNull(TibberHistoryCurrentPeriods.add(quantity, decimal));
        assertNull(TibberHistoryCurrentPeriods.add(decimal, quantity));
        assertNull(TibberHistoryCurrentPeriods.add(null, decimal));
        assertNull(TibberHistoryCurrentPeriods.add(quantity, null));
    }

    @Test
    void whenAnnualSumsThenOnlyCurrentYearEntriesAreIncluded() {
        // Arrange
        ZonedDateTime nowZdt = ZonedDateTime.now(ZONE);
        Instant now = nowZdt.toInstant();
        assumeTrue(nowZdt.getDayOfYear() >= 3, "needs two completed days in the current year");
        ZonedDateTime yearStart = startOfCurrentYear(nowZdt);
        Instant startOfToday = startOfToday(nowZdt);
        TibberHistorySeries daily = new TibberHistorySeries(null);
        daily.put(yearStart.minusDays(40).toInstant(), data("99.0 kWh", "9.9", null));
        daily.put(yearStart.toInstant(), data("4.0 kWh", "0.4", null));
        daily.put(startOfToday.minus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS), data("1.5 kWh", "0.15", null));

        // Act
        TibberHistoryCurrentPeriods.Values values = TibberHistoryCurrentPeriods.compute(daily, ZONE, now);

        // Assert
        assertEquals(QuantityType.valueOf("5.5 kWh"), values.get(ANNUAL, PURPOSE_CONSUMPTION));
        assertEquals(DecimalType.valueOf("0.55"), values.get(ANNUAL, PURPOSE_COST));
        assertNull(values.get(ANNUAL, PURPOSE_PRODUCTION));
    }

    private static void assertState(@Nullable State expected, @Nullable State actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Instant startOfToday(ZonedDateTime now) {
        return now.truncatedTo(ChronoUnit.DAYS).toInstant();
    }

    private static ZonedDateTime startOfCurrentMonth(ZonedDateTime now) {
        return now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
    }

    private static ZonedDateTime startOfCurrentYear(ZonedDateTime now) {
        return now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
    }

    private static JsonObject data(@Nullable String consumption, @Nullable String cost, @Nullable String production) {
        JsonObject data = new JsonObject();
        if (consumption != null) {
            data.addProperty(PURPOSE_CONSUMPTION, consumption);
        }
        if (cost != null) {
            data.addProperty(PURPOSE_COST, cost);
        }
        if (production != null) {
            data.addProperty(PURPOSE_PRODUCTION, production);
        }
        return data;
    }
}
