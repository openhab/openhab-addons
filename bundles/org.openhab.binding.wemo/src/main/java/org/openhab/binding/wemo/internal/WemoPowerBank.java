/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * Class for caching and processing historic values for current power.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoPowerBank {

    private final Deque<CacheItem> slidingCache = new ConcurrentLinkedDeque<CacheItem>();

    @Nullable
    private QuantityType<?> previousCurrentPower = null;
    private int slidingSeconds;

    private class CacheItem {
        public Instant start;
        public @Nullable Instant end;
        public double power;

        public CacheItem(double power, Instant start) {
            this.start = start;
            this.power = power;
        }
    }

    public WemoPowerBank() {
        this.slidingSeconds = 60;
    }

    public WemoPowerBank(int slidingSeconds) {
        this.slidingSeconds = slidingSeconds;
    }

    public void clear() {
        slidingCache.clear();
        previousCurrentPower = null;
    }

    public void apply(double value) {
        this.apply(value, Instant.now());
    }

    public void apply(double value, Instant now) {
        if (slidingCache.isEmpty()) {
            slidingCache.add(new CacheItem(value, now));
            return;
        }
        @Nullable
        CacheItem last = slidingCache.getLast();
        last.end = now;
        Instant windowStart = now.minusSeconds(slidingSeconds);
        final Iterator<CacheItem> it = slidingCache.iterator();
        while (it.hasNext()) {
            CacheItem current = it.next();
            Instant end = current.end;
            end = end != null ? end.minusNanos(1) : now;
            if (end.isBefore(windowStart)) {
                it.remove();
                continue;
            }
            if (current.start.isBefore(windowStart) && end.isAfter(windowStart)) {
                // Truncate last item before sliding window.
                current.start = windowStart;
                break;
            }
        }
        slidingCache.add(new CacheItem(value, now));
    }

    public void setPreviousCurrentPower(QuantityType<?> previousCurrentPower) {
        this.previousCurrentPower = previousCurrentPower;
    }

    public @Nullable QuantityType<?> getPreviousCurrentPower() {
        return previousCurrentPower;
    }

    public double getCalculatedAverage(double currentValue) {
        double historyWattMillis = 0;
        long historyMillis = 0;
        for (CacheItem item : slidingCache) {
            Instant end = item.end;
            if (end != null) {
                long millis = item.start.until(end, ChronoUnit.MILLIS);
                historyWattMillis += item.power * millis;
                historyMillis += millis;
            }
        }
        double average;
        if (historyMillis > 0) {
            average = historyWattMillis / historyMillis;
        } else {
            average = currentValue;
        }

        return average;
    }
}
