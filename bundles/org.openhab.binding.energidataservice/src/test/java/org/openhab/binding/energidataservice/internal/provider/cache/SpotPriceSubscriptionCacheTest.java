/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.energidataservice.internal.api.dto.DayAheadPriceRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.provider.subscription.SpotPriceSubscription;

/**
 * Tests for {@link SpotPriceSubscriptionCache}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class SpotPriceSubscriptionCacheTest {

    @Test
    void putDoesNotThrowOnNullSpotPrice() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = { new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), null, null) };
        assertThat(cache.put(records), is(false));
    }

    @Test
    void getMatchingStart() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T09:00:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:00:00Z")), is(equalTo(BigDecimal.ONE)));
    }

    @Test
    void getBetween() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T09:00:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:30:00Z")), is(equalTo(BigDecimal.ONE)));
    }

    @Test
    void getAfterLast() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T09:00:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T10:00:01Z")), is(nullValue()));
    }

    @Test
    void getQuarterOfAnHourMatchingStart() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:15:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:00:00Z")), is(equalTo(BigDecimal.ONE)));
    }

    @Test
    void getQuarterOfAnHourBetween() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:15:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:07:30Z")), is(equalTo(BigDecimal.ONE)));
    }

    @Test
    void getQuarterOfAnHourLast() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:15:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:29:59Z")), is(equalTo(BigDecimal.valueOf(2))));
    }

    @Test
    void getQuarterOfAnHourAfterLast() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T08:38:47Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2023-02-06T08:15:00Z"), BigDecimal.valueOf(2000),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2023-02-06T08:30:00Z")), is(nullValue()));
    }

    @Test
    void getMixedDurations() {
        Clock clock = Clock.fixed(Instant.parse("2025-10-30T12:00:00Z"), NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        ElspotpriceRecord[] records = {
                new ElspotpriceRecord(Instant.parse("2025-10-30T21:00:00Z"), BigDecimal.valueOf(800), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2025-10-30T22:00:00Z"), BigDecimal.valueOf(900), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2025-10-30T23:00:00Z"), BigDecimal.valueOf(1000), BigDecimal.ZERO),
                new ElspotpriceRecord(Instant.parse("2025-10-30T23:15:00Z"), BigDecimal.valueOf(1100),
                        BigDecimal.ZERO) };
        cache.put(records);
        assertThat(cache.get(Instant.parse("2025-10-30T22:00:00Z")), is(equalTo(new BigDecimal("0.9"))));
        assertThat(cache.get(Instant.parse("2025-10-30T22:59:59Z")), is(equalTo(new BigDecimal("0.9"))));
        assertThat(cache.get(Instant.parse("2025-10-30T23:14:59Z")), is(equalTo(new BigDecimal("1"))));
        assertThat(cache.get(Instant.parse("2025-10-30T23:15:00Z")), is(equalTo(new BigDecimal("1.1"))));
        assertThat(cache.get(Instant.parse("2025-10-30T23:29:59Z")), is(equalTo(new BigDecimal("1.1"))));
        assertThat(cache.get(Instant.parse("2025-10-30T23:30:00Z")), is(nullValue()));
    }

    @Test
    void areSpotPricesFullyCachedToday() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(true));
    }

    @Test
    void areSpotPricesFullyCachedTodayForQuarterOfAnHour() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:45:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithDayAheadPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(true));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingAtStart() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T21:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingInMiddle() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, Instant.parse("2023-02-06T08:00:00Z"), Instant.parse("2023-02-07T11:00:00Z"));
        populateWithSpotPrices(cache, Instant.parse("2023-02-07T13:00:00Z"), Instant.parse("2023-02-07T22:00:00Z"));
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingInMiddleForQuarterOfAnHour() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithDayAheadPrices(cache, Instant.parse("2023-02-06T08:00:00Z"), Instant.parse("2023-02-07T12:30:00Z"));
        populateWithDayAheadPrices(cache, Instant.parse("2023-02-07T13:00:00Z"), Instant.parse("2023-02-07T22:00:00Z"));
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingAtEnd() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T21:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayMissingAtEndForQuarterOfAnHour() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:30:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithDayAheadPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTodayOtherTimezoneIsIgnored() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T22:00:00Z");
        Clock clock = Clock.fixed(now, ZoneId.of("Asia/Tokyo"));
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(true));
    }

    @Test
    void areSpotPricesFullyCachedTodaySingle() {
        Instant now = Instant.parse("2025-02-07T12:00:00Z");
        Instant first = Instant.parse("2025-02-08T22:00:00Z");
        Instant last = Instant.parse("2025-02-08T22:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(false));
    }

    @Test
    void areSpotPricesFullyCachedTomorrow() {
        Instant now = Instant.parse("2023-02-07T12:00:00Z");
        Instant first = Instant.parse("2023-02-06T12:00:00Z");
        Instant last = Instant.parse("2023-02-08T22:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.arePricesFullyCached(), is(true));
    }

    @Test
    void areHistoricSpotPricesCached() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.areHistoricPricesCached(), is(true));
    }

    @Test
    void areHistoricSpotPricesCachedFirstHourMissing() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T21:00:00Z");
        Instant last = Instant.parse("2023-02-07T08:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(cache.areHistoricPricesCached(), is(false));
    }

    @Test
    void updateCacheIsNotChanged() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(populateWithSpotPrices(cache, first, last), is(false));
    }

    @Test
    void updateCacheIsNotChangedSameValue() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        ElspotpriceRecord[] changedRecords = new ElspotpriceRecord[1];
        changedRecords[0] = new ElspotpriceRecord(last, BigDecimal.ONE, BigDecimal.ZERO);
        assertThat(cache.put(changedRecords), is(false));
    }

    @Test
    void updateCacheIsChangedByOneValue() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        ElspotpriceRecord[] changedRecords = new ElspotpriceRecord[1];
        changedRecords[0] = new ElspotpriceRecord(last, BigDecimal.TEN, BigDecimal.ZERO);
        assertThat(cache.put(changedRecords), is(true));
    }

    @Test
    void updateCacheIsChangedByAdditionalKey() {
        Instant now = Instant.parse("2023-02-07T08:38:47Z");
        Instant first = Instant.parse("2023-02-06T08:00:00Z");
        Instant last = Instant.parse("2023-02-07T07:00:00Z");
        Clock clock = Clock.fixed(now, NORD_POOL_TIMEZONE);
        SpotPriceSubscriptionCache cache = new SpotPriceSubscriptionCache(SpotPriceSubscription.of("DK1", CURRENCY_DKK),
                clock);
        populateWithSpotPrices(cache, first, last);
        assertThat(populateWithSpotPrices(cache, first, last.plus(1, ChronoUnit.HOURS)), is(true));
    }

    private boolean populateWithSpotPrices(SpotPriceSubscriptionCache cache, Instant first, Instant last) {
        Duration duration = Duration.ofHours(1);
        int size = (int) Duration.between(first, last).getSeconds() / (int) duration.getSeconds() + 1;
        ElspotpriceRecord[] records = new ElspotpriceRecord[size];
        int i = 0;
        for (Instant start = first; !start.isAfter(last); start = start.plus(duration)) {
            records[i++] = new ElspotpriceRecord(start, BigDecimal.ONE, BigDecimal.ZERO);
        }
        return cache.put(records);
    }

    private boolean populateWithDayAheadPrices(SpotPriceSubscriptionCache cache, Instant first, Instant last) {
        Duration duration = Duration.ofMinutes(15);
        int size = (int) Duration.between(first, last).getSeconds() / (int) duration.getSeconds() + 1;
        DayAheadPriceRecord[] records = new DayAheadPriceRecord[size];
        int i = 0;
        for (Instant start = first; !start.isAfter(last); start = start.plus(duration)) {
            records[i++] = new DayAheadPriceRecord(start, BigDecimal.ONE, BigDecimal.ZERO);
        }
        return cache.put(records);
    }
}
