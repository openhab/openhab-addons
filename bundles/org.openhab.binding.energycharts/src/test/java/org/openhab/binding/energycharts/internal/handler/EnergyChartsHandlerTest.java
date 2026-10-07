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
package org.openhab.binding.energycharts.internal.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energycharts.internal.MarketPrice;
import org.openhab.binding.energycharts.internal.TimeRange;

/**
 * Tests the refresh decision of the {@link EnergyChartsHandler}.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
class EnergyChartsHandlerTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);

    private static SortedSet<MarketPrice> pricesFor(int days) {
        SortedSet<MarketPrice> prices = new TreeSet<>();
        ZonedDateTime start = TODAY.atStartOfDay(ZONE);
        ZonedDateTime end = start.plusDays(days);
        for (ZonedDateTime t = start; t.isBefore(end); t = t.plusMinutes(15)) {
            long s = t.toInstant().toEpochMilli();
            prices.add(new MarketPrice(1, 1, 1, 1, new TimeRange(s, s + Duration.ofMinutes(15).toMillis())));
        }
        return prices;
    }

    private static ZonedDateTime at(int hour) {
        return TODAY.atTime(hour, 5).atZone(ZONE);
    }

    @Test
    void refreshWithoutPrices() {
        assertTrue(EnergyChartsHandler.needsRefresh(null, at(10), Instant.EPOCH));
        assertTrue(EnergyChartsHandler.needsRefresh(new TreeSet<>(), at(10), Instant.EPOCH));
    }

    @Test
    void noRefreshBeforeNextDayIsPublished() {
        assertFalse(EnergyChartsHandler.needsRefresh(pricesFor(1), at(10), Instant.EPOCH));
    }

    @Test
    void refreshAfterNextDayIsPublishedAtMostHourly() {
        assertTrue(EnergyChartsHandler.needsRefresh(pricesFor(1), at(13), Instant.EPOCH));
        assertFalse(EnergyChartsHandler.needsRefresh(pricesFor(1), at(14), at(14).minusMinutes(30).toInstant()));
        assertTrue(EnergyChartsHandler.needsRefresh(pricesFor(1), at(14), at(14).minusHours(1).toInstant()));
    }

    @Test
    void noRefreshWhenNextDayIsAvailable() {
        assertFalse(EnergyChartsHandler.needsRefresh(pricesFor(2), at(15), Instant.EPOCH));
    }

    @Test
    void refreshWhenCurrentIntervalIsMissing() {
        assertTrue(EnergyChartsHandler.needsRefresh(pricesFor(1), at(10).plusDays(1), Instant.EPOCH));
    }
}
