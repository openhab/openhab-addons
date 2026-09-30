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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GmePriceCacheTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Test
    void promotesCompleteTomorrowDataset() {
        LocalDate date = LocalDate.of(2026, 9, 11);

        GmePriceCache cache = new GmePriceCache();
        cache.setTomorrow(date, completeDay(date, 24));

        assertTrue(cache.promoteTomorrowToToday(date, ROME));
        assertEquals(date, cache.getTodayDate());
        assertEquals(24, cache.getTodayPrices().size());
        assertNull(cache.getTomorrowDate());
        assertTrue(cache.getTomorrowPrices().isEmpty());
    }

    @Test
    void doesNotPromoteIncompleteTomorrowDataset() {
        LocalDate date = LocalDate.of(2026, 9, 11);

        GmePriceCache cache = new GmePriceCache();
        cache.setTomorrow(date, completeDay(date, 23));

        assertFalse(cache.promoteTomorrowToToday(date, ROME));
        assertNull(cache.getTodayDate());
        assertTrue(cache.getTodayPrices().isEmpty());
        assertEquals(date, cache.getTomorrowDate());
        assertEquals(23, cache.getTomorrowPrices().size());
    }

    @Test
    void doesNotPromoteDatasetForDifferentDate() {
        LocalDate cachedDate = LocalDate.of(2026, 9, 11);
        LocalDate currentDate = cachedDate.plusDays(1);

        GmePriceCache cache = new GmePriceCache();
        cache.setTomorrow(cachedDate, completeDay(cachedDate, 24));

        assertFalse(cache.promoteTomorrowToToday(currentDate, ROME));
        assertNull(cache.getTodayDate());
        assertEquals(cachedDate, cache.getTomorrowDate());
    }

    @Test
    void promotesCompleteSpringDstDataset() {
        LocalDate date = LocalDate.of(2026, 3, 29);

        GmePriceCache cache = new GmePriceCache();
        cache.setTomorrow(date, completeDay(date, 23));

        assertTrue(cache.promoteTomorrowToToday(date, ROME));
        assertEquals(23, cache.getTodayPrices().size());
    }

    @Test
    void promotesCompleteQuarterHourDataset() {
        LocalDate date = LocalDate.of(2026, 9, 11);
        GmePriceCache cache = new GmePriceCache();
        List<GmePriceEntry> prices = new ArrayList<>();

        for (int period = 1; period <= 96; period++) {
            int hour = ((period - 1) * GmeGranularity.PT15.minutes()) / 60 + 1;
            prices.add(new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), period,
                    GmeGranularity.PT15, null));
        }

        cache.setTomorrow(date, prices);

        assertTrue(cache.promoteTomorrowToToday(date, ROME, GmeGranularity.PT15));
        assertEquals(96, cache.getTodayPrices().size());
    }

    @Test
    void promotesCompleteAutumnDstDataset() {
        LocalDate date = LocalDate.of(2026, 10, 25);

        GmePriceCache cache = new GmePriceCache();
        cache.setTomorrow(date, completeDay(date, 25));

        assertTrue(cache.promoteTomorrowToToday(date, ROME));
        assertEquals(25, cache.getTodayPrices().size());
    }

    private static List<GmePriceEntry> completeDay(LocalDate date, int hours) {
        List<GmePriceEntry> prices = new ArrayList<>();

        for (int hour = 1; hour <= hours; hour++) {
            prices.add(new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), 0, GmeGranularity.PT60,
                    null));
        }

        return prices;
    }
}
