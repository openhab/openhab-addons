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
package org.openhab.binding.gme.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GmePriceTimelineTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Test
    void returnsExpectedPeriodsForSupportedGranularitiesAndDstDays() {
        LocalDate normal = LocalDate.of(2026, 9, 10);
        LocalDate spring = LocalDate.of(2026, 3, 29);
        LocalDate autumn = LocalDate.of(2026, 10, 25);

        assertEquals(24, GmePriceTimeline.getExpectedPeriods(normal, ROME, GmeGranularity.PT60));
        assertEquals(48, GmePriceTimeline.getExpectedPeriods(normal, ROME, GmeGranularity.PT30));
        assertEquals(96, GmePriceTimeline.getExpectedPeriods(normal, ROME, GmeGranularity.PT15));
        assertEquals(23, GmePriceTimeline.getExpectedPeriods(spring, ROME, GmeGranularity.PT60));
        assertEquals(92, GmePriceTimeline.getExpectedPeriods(spring, ROME, GmeGranularity.PT15));
        assertEquals(25, GmePriceTimeline.getExpectedPeriods(autumn, ROME, GmeGranularity.PT60));
        assertEquals(100, GmePriceTimeline.getExpectedPeriods(autumn, ROME, GmeGranularity.PT15));
    }

    @Test
    void mapsQuarterHourPeriodsToTimeline() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        assertEquals(ZonedDateTime.of(2026, 9, 10, 0, 0, 0, 0, ROME),
                GmePriceTimeline.getStartTime(entry(date, 1, GmeGranularity.PT15), ROME));
        assertEquals(ZonedDateTime.of(2026, 9, 10, 10, 30, 0, 0, ROME),
                GmePriceTimeline.getStartTime(entry(date, 43, GmeGranularity.PT15), ROME));
        assertEquals(ZonedDateTime.of(2026, 9, 10, 23, 45, 0, 0, ROME),
                GmePriceTimeline.getStartTime(entry(date, 96, GmeGranularity.PT15), ROME));
    }

    @Test
    void mapsDstQuarterHourPeriodsOnAbsoluteTimeline() {
        LocalDate spring = LocalDate.of(2026, 3, 29);
        LocalDate autumn = LocalDate.of(2026, 10, 25);

        ZonedDateTime springLast = GmePriceTimeline.getStartTime(entry(spring, 92, GmeGranularity.PT15), ROME);
        ZonedDateTime autumnLast = GmePriceTimeline.getStartTime(entry(autumn, 100, GmeGranularity.PT15), ROME);

        assertEquals(23, springLast.getHour());
        assertEquals(45, springLast.getMinute());
        assertEquals(23, autumnLast.getHour());
        assertEquals(45, autumnLast.getMinute());
    }

    @Test
    void acceptsCompleteQuarterHourDailySet() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        assertTrue(GmePriceTimeline.isCompleteDailySet(completeDay(date, GmeGranularity.PT15), date, ROME,
                GmeGranularity.PT15));
    }

    @Test
    void rejectsIncompleteOrDuplicateQuarterHourSet() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        List<GmePriceEntry> incomplete = new ArrayList<>(completeDay(date, GmeGranularity.PT15));
        incomplete.removeLast();
        assertFalse(GmePriceTimeline.isCompleteDailySet(incomplete, date, ROME, GmeGranularity.PT15));

        List<GmePriceEntry> duplicate = new ArrayList<>(incomplete);
        duplicate.add(entry(date, 95, GmeGranularity.PT15));
        assertFalse(GmePriceTimeline.isCompleteDailySet(duplicate, date, ROME, GmeGranularity.PT15));
    }

    @Test
    void supportsLegacyHourlyPeriodZero() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        GmePriceEntry legacy = new GmePriceEntry(date, 11, "MGP", "PUN", new BigDecimal("100.000000"), 0,
                GmeGranularity.PT60, null);

        assertEquals(ZonedDateTime.of(2026, 9, 10, 10, 0, 0, 0, ROME), GmePriceTimeline.getStartTime(legacy, ROME));
    }

    @Test
    void findsCurrentAndNextQuarterHourPrice() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        List<GmePriceEntry> prices = List.of(entry(date, 42, GmeGranularity.PT15), entry(date, 43, GmeGranularity.PT15),
                entry(date, 44, GmeGranularity.PT15));

        Instant now = ZonedDateTime.of(2026, 9, 10, 10, 37, 0, 0, ROME).toInstant();

        assertEquals(43, GmePriceTimeline.findCurrentPrice(prices, now, ROME).orElseThrow().period());
        assertEquals(44, GmePriceTimeline.findNextPrice(prices, now, ROME).orElseThrow().period());
    }

    private static List<GmePriceEntry> completeDay(LocalDate date, GmeGranularity granularity) {
        int periods = GmePriceTimeline.getExpectedPeriods(date, ROME, granularity);
        List<GmePriceEntry> prices = new ArrayList<>();
        for (int period = 1; period <= periods; period++) {
            prices.add(entry(date, period, granularity));
        }
        return prices;
    }

    private static GmePriceEntry entry(LocalDate date, int period, GmeGranularity granularity) {
        int hour = ((period - 1) * granularity.minutes()) / 60 + 1;
        return new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), period, granularity, null);
    }
}
