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

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.NORD_POOL_TIMEZONE;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Electricity price specific {@link SubscriptionDataCache} implementation.
 * All price durations must be the same.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class ElectricityPriceSubscriptionCache<R> implements SubscriptionDataCache<R> {

    public static final int NUMBER_OF_HISTORIC_HOURS = 24;

    protected final NavigableMap<Instant, BigDecimal> priceMap;

    protected final Clock clock;

    protected final Duration priceDuration;

    protected ElectricityPriceSubscriptionCache(Clock clock, Duration priceDuration) {
        this.clock = clock.withZone(NORD_POOL_TIMEZONE);
        this.priceDuration = priceDuration;
        this.priceMap = new ConcurrentSkipListMap<>();
    }

    @Override
    public void flush() {
        Instant firstHourStart = getFirstHourStart();
        priceMap.entrySet().removeIf(entry -> entry.getKey().isBefore(firstHourStart));
    }

    /**
     * Get map of all cached prices.
     *
     * @return prices currently available, {@link #NUMBER_OF_HISTORIC_HOURS} back
     */
    @Override
    public Map<Instant, BigDecimal> get() {
        return new HashMap<>(priceMap);
    }

    /**
     * Get price valid at provided instant.
     *
     * @param time {@link Instant} for which to get the price
     * @return price at given time or null if not available
     */
    @Override
    public @Nullable BigDecimal get(Instant time) {
        Map.Entry<Instant, BigDecimal> entry = priceMap.floorEntry(time);
        if (entry != null && !time.isAfter(entry.getKey().plus(priceDuration))) {
            return entry.getValue();
        }
        return null;
    }

    /**
     * Check if historic prices ({@link #NUMBER_OF_HISTORIC_HOURS}) are cached.
     * 
     * @return true if historic prices are cached
     */
    @Override
    public boolean areHistoricPricesCached() {
        return arePricesCached(getCurrentHourStart().minus(1, ChronoUnit.HOURS));
    }

    protected boolean arePricesCached(Instant end) {
        for (Instant start = getFirstHourStart(); start.compareTo(end) <= 0; start = start.plus(priceDuration)) {
            if (priceMap.get(start) == null) {
                return false;
            }
        }

        return true;
    }

    protected Instant getCurrentHourStart() {
        return getHourStart(Instant.now(clock));
    }

    protected Instant getFirstHourStart() {
        return getHourStart(Instant.now(clock).minus(NUMBER_OF_HISTORIC_HOURS, ChronoUnit.HOURS));
    }

    protected Instant getHourStart(Instant instant) {
        return instant.truncatedTo(ChronoUnit.HOURS);
    }
}
