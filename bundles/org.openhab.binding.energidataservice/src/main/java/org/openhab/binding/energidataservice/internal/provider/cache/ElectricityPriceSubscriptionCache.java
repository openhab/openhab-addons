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

    protected ElectricityPriceSubscriptionCache(Clock clock) {
        this.clock = clock.withZone(NORD_POOL_TIMEZONE);
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
        if (entry == null) {
            return null;
        }

        Instant validUntil = getValidUntil(entry.getKey());
        if (validUntil != null && time.isBefore(validUntil)) {
            return entry.getValue();
        }

        return null;
    }

    private @Nullable Instant getValidUntil(Instant current) {
        Instant next = getPriceMapHigherKey(current);
        if (next != null) {
            return next;
        }

        Instant previous = getPriceMapLowerKey(current);
        if (previous != null) {
            return current.plus(Duration.between(previous, current));
        }

        return null;
    }

    private @Nullable Instant getPriceMapHigherKey(Instant current) {
        return priceMap.higherKey(current);
    }

    private @Nullable Instant getPriceMapLowerKey(Instant current) {
        return priceMap.lowerKey(current);
    }

    /**
     * Check if historic prices ({@link #NUMBER_OF_HISTORIC_HOURS}) are cached.
     * 
     * @return true if historic prices are cached
     */
    @Override
    public boolean areHistoricPricesCached() {
        return arePricesCached(getCurrentHourStart());
    }

    /**
     * Check if prices are cached until provided {@link Instant}.
     * The default gap expected between prices is one hour.
     * It is assumed that gaps can only become shorter, not longer.
     * For example, after day-ahead price transition to 15-minutes
     * resolution, we no longer expect one hour between prices.
     *
     * @param end Check until this time (exclusive)
     * @return true if prices are fully cached
     */
    protected boolean arePricesCached(Instant end) {
        Instant current = getFirstHourStart();
        Instant previous = null;
        Duration maxGap = Duration.ofHours(1);

        while (current.isBefore(end)) {
            if (!priceMap.containsKey(current)) {
                return false;
            }

            Instant next = getPriceMapHigherKey(current);
            if (next != null) {
                Duration gap = Duration.between(current, next);
                if (gap.compareTo(maxGap) > 0) {
                    return false;
                }
                if (gap.compareTo(maxGap) < 0) {
                    maxGap = gap;
                }
            } else {
                // No next entry — infer end time of final segment
                if (previous != null) {
                    Instant prev = previous;
                    Duration gap = Duration.between(prev, current);
                    if (current.plus(gap).isBefore(end)) {
                        return false;
                    }
                } else {
                    // Only one entry, can't infer duration
                    return false;
                }

                break;
            }

            previous = current;
            current = next;
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
