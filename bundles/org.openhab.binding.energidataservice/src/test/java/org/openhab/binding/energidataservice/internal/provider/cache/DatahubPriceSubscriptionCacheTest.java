/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.provider.cache;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;

/**
 * Tests for {@link DatahubPriceSubscriptionCache}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class DatahubPriceSubscriptionCacheTest {

    @Test
    void areTariffsValidTomorrowTwoDaysBeforeEnding() {
        Instant now = Instant.parse("2024-09-29T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);
        assertThat(cache.areTariffsValidTomorrow(), is(true));
    }

    @Test
    void areTariffsValidTomorrowOneDayBeforeEnding() {
        Instant now = Instant.parse("2024-09-30T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);
        assertThat(cache.areTariffsValidTomorrow(), is(false));
    }

    @Test
    void updateCacheIsNotChanged() {
        Instant now = Instant.parse("2024-09-30T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);
        assertThat(populateWithDatahubPrices(cache, from, to), is(false));
    }

    @Test
    void updateCacheIsNotChangedSameValue() {
        Instant now = Instant.parse("2024-09-30T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);

        from = LocalDateTime.parse("2024-10-01T00:00:00");
        to = LocalDateTime.parse("2024-11-01T00:00:00");
        populateWithDatahubPrices(cache, from, to);

        var changedRecords = new ArrayList<DatahubPricelistRecord>();
        changedRecords.add(new DatahubPricelistRecord(from, to, "CD", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(cache.put(changedRecords), is(false));
    }

    @Test
    void updateCacheIsChangedByOneValue() {
        Instant now = Instant.parse("2024-09-30T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);

        from = LocalDateTime.parse("2024-10-01T00:00:00");
        to = LocalDateTime.parse("2024-11-01T00:00:00");
        populateWithDatahubPrices(cache, from, to);

        var changedRecords = new ArrayList<DatahubPricelistRecord>();
        changedRecords.add(new DatahubPricelistRecord(from, to, "CD", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(cache.put(changedRecords), is(true));
    }

    @Test
    void updateCacheIsChangedByAdditionalKey() {
        Instant now = Instant.parse("2024-09-30T09:22:00Z");
        LocalDateTime from = LocalDateTime.parse("2024-08-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2024-10-01T00:00:00");
        Clock clock = Clock.fixed(now, DATAHUB_TIMEZONE);
        DatahubPriceSubscriptionCache cache = new DatahubPriceSubscriptionCache(clock);
        populateWithDatahubPrices(cache, from, to);
        assertThat(populateWithDatahubPrices(cache, to, to.plusMonths(1)), is(true));
    }

    private boolean populateWithDatahubPrices(DatahubPriceSubscriptionCache cache, LocalDateTime validFrom,
            LocalDateTime validTo) {
        var records = new ArrayList<DatahubPricelistRecord>();
        records.add(new DatahubPricelistRecord(validFrom, validTo, "CD", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        return cache.put(records);
    }
}
