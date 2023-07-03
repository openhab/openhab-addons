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
package org.openhab.binding.energidataservice.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;

/**
 * Tests for {@link CacheManager}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class CacheManagerTest {

    @Test
    void areSpotPricesFullyCachedToday() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areSpotPricesFullyCached(), is(true));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingAtStart() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T21:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areSpotPricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingAtEnd() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T20:00:00Z");
        Instant last = Instant.parse("2023-02-07T21:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areSpotPricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayOtherTimezoneIsIgnored() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, ZoneId.of("Asia/Tokyo"));
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areSpotPricesFullyCached(), is(true));
    }

    @Test
    void areSpotPricesFullyCachedTomorrow() {
        Instant now = Instant.parse("2023-02-07T12:00:00Z");
        Instant first = Instant.parse("2023-02-06T12:00:00Z");
        Instant last = Instant.parse("2023-02-08T22:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areSpotPricesFullyCached(), is(true));
    }

    @Test
    void areHistoricSpotPricesCached() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areHistoricSpotPricesCached(), is(true));
    }

    @Test
    void areHistoricSpotPricesCachedFirstHourMissing() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T21:00:00Z");
        Instant last = Instant.parse("2023-02-07T08:00:00Z");
        Clock clock = Clock.fixed(now, EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE);
        CacheManager cacheManager = new CacheManager(clock);
        populateWithSpotPrices(cacheManager, first, last);
        assertThat(cacheManager.areHistoricSpotPricesCached(), is(false));
    }

    private void populateWithSpotPrices(CacheManager cacheManager, Instant first, Instant last) {
        int size = (int) Duration.between(first, last).getSeconds() / 60 / 60 + 1;
        ElspotpriceRecord[] records = new ElspotpriceRecord[size];
        int i = 0;
        for (Instant hourStart = first; !hourStart.isAfter(last); hourStart = hourStart.plus(1, ChronoUnit.HOURS)) {
            records[i++] = new ElspotpriceRecord(hourStart, BigDecimal.ONE, BigDecimal.ZERO);
        }
        cacheManager.putSpotPrices(records, EnergiDataServiceBindingConstants.CURRENCY_DKK);
    }
}
