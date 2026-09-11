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

/**
 * Tests for GME price timeline handling.
 *
 * @author Andrea Riela - Initial contribution
 */
class GmePriceTimelineTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Test
    void mapsNormalDayTo24HourlyInstants() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        ZonedDateTime first = GmePriceTimeline.getStartTime(entry(date, 1), ROME);
        ZonedDateTime last = GmePriceTimeline.getStartTime(entry(date, 24), ROME);

        assertEquals(0, first.getHour());
        assertEquals(23, last.getHour());
        assertEquals(23, java.time.Duration.between(first.toInstant(), last.toInstant()).toHours());
    }

    @Test
    void mapsSpringDstDayTo23Hours() {
        LocalDate date = LocalDate.of(2026, 3, 29);

        ZonedDateTime first = GmePriceTimeline.getStartTime(entry(date, 1), ROME);
        ZonedDateTime last = GmePriceTimeline.getStartTime(entry(date, 23), ROME);

        assertEquals(0, first.getHour());
        assertEquals(23, last.getHour());
        assertEquals(22, java.time.Duration.between(first.toInstant(), last.toInstant()).toHours());
    }

    @Test
    void mapsAutumnDstDayTo25Hours() {
        LocalDate date = LocalDate.of(2026, 10, 25);

        ZonedDateTime first = GmePriceTimeline.getStartTime(entry(date, 1), ROME);
        ZonedDateTime last = GmePriceTimeline.getStartTime(entry(date, 25), ROME);

        assertEquals(0, first.getHour());
        assertEquals(23, last.getHour());
        assertEquals(24, java.time.Duration.between(first.toInstant(), last.toInstant()).toHours());
    }

    @Test
    void returnsExpectedHoursForNormalAndDstDays() {
        assertEquals(24, GmePriceTimeline.getExpectedHours(LocalDate.of(2026, 9, 10), ROME));
        assertEquals(23, GmePriceTimeline.getExpectedHours(LocalDate.of(2026, 3, 29), ROME));
        assertEquals(25, GmePriceTimeline.getExpectedHours(LocalDate.of(2026, 10, 25), ROME));
    }

    @Test
    void acceptsCompleteDailySet() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        List<GmePriceEntry> prices = new ArrayList<>();
        for (int hour = 1; hour <= 24; hour++) {
            prices.add(entry(date, hour));
        }

        assertTrue(GmePriceTimeline.isCompleteDailySet(prices, date, ROME));
    }

    @Test
    void rejectsIncompleteDailySet() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        List<GmePriceEntry> prices = new ArrayList<>();
        for (int hour = 1; hour <= 23; hour++) {
            prices.add(entry(date, hour));
        }

        assertFalse(GmePriceTimeline.isCompleteDailySet(prices, date, ROME));
    }

    @Test
    void rejectsDuplicateHour() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        List<GmePriceEntry> prices = new ArrayList<>();
        for (int hour = 1; hour <= 23; hour++) {
            prices.add(entry(date, hour));
        }
        prices.add(entry(date, 23));

        assertFalse(GmePriceTimeline.isCompleteDailySet(prices, date, ROME));
    }

    @Test
    void rejectsWrongDate() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        List<GmePriceEntry> prices = new ArrayList<>();
        for (int hour = 1; hour <= 23; hour++) {
            prices.add(entry(date, hour));
        }
        prices.add(entry(date.plusDays(1), 24));

        assertFalse(GmePriceTimeline.isCompleteDailySet(prices, date, ROME));
    }

    @Test
    void findsNextPriceAcrossMidnight() {
        LocalDate today = LocalDate.of(2026, 9, 10);
        LocalDate tomorrow = today.plusDays(1);

        List<GmePriceEntry> prices = new ArrayList<>();
        prices.add(entry(today, 24));
        prices.add(entry(tomorrow, 1));

        Instant now = ZonedDateTime.of(2026, 9, 10, 23, 30, 0, 0, ROME).toInstant();

        GmePriceEntry next = GmePriceTimeline.findNextPrice(prices, now, ROME).orElseThrow();

        assertEquals(tomorrow, next.flowDate());
        assertEquals(1, next.hour());
    }

    @Test
    void findsCurrentPrice() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        List<GmePriceEntry> prices = List.of(entry(date, 10), entry(date, 11), entry(date, 12));

        Instant now = ZonedDateTime.of(2026, 9, 10, 10, 30, 0, 0, ROME).toInstant();

        GmePriceEntry current = GmePriceTimeline.findCurrentPrice(prices, now, ROME).orElseThrow();

        assertEquals(11, current.hour());
    }

    private static GmePriceEntry entry(LocalDate date, int hour) {
        return new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), 0, null);
    }
}
