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
package org.openhab.binding.miele.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link TimeStabilizer} keeps a cache of historic timestamp values in order to
 * provide moving average calculations to smooth out short-term fluctuations.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class TimeStabilizer {

    private static final int SLIDING_SECONDS = 300;
    private static final int MAX_FLUCTUATION_SECONDS = 180;

    private final Deque<Item> cache = new ConcurrentLinkedDeque<Item>();

    private class Item {
        public Instant start;
        public Instant end;
        public Instant instant;

        public Item(Instant instant, Instant start, Instant end) {
            this.start = start;
            this.end = end;
            this.instant = instant;
        }
    }

    public TimeStabilizer() {
    }

    public void clear() {
        cache.clear();
    }

    public Instant apply(Instant value) {
        return this.apply(value, Instant.now());
    }

    public Instant apply(Instant value, Instant now) {
        if (cache.isEmpty()) {
            cache.add(new Item(value, now, now));
            return value;
        }

        @Nullable
        Item first = cache.getFirst();
        @Nullable
        Item last = cache.getLast();
        last.end = now;

        Instant windowStart = now.minusSeconds(SLIDING_SECONDS);
        Instant start = first.start;
        Instant base = first.instant;
        long weightedDiffFromBase = 0;
        final Iterator<Item> it = cache.iterator();
        while (it.hasNext()) {
            Item current = it.next();

            if (current.end.isBefore(windowStart)) {
                it.remove();
                continue;
            }
            if (current.start.isBefore(windowStart) && current.end.isAfter(windowStart)) {
                // Truncate last item before sliding window.
                start = current.start = windowStart;
            }
            long secs = current.start.until(current.end, ChronoUnit.SECONDS);
            weightedDiffFromBase += base.until(current.instant, ChronoUnit.SECONDS) * secs;
        }

        Instant average = base.plusSeconds(weightedDiffFromBase / start.until(now, ChronoUnit.SECONDS));
        if (value.isBefore(average.minusSeconds(MAX_FLUCTUATION_SECONDS))
                || value.isAfter(average.plusSeconds(MAX_FLUCTUATION_SECONDS))) {
            cache.clear();
            average = value;
        }

        cache.add(new Item(value, now, now));

        return average;
    }
}
