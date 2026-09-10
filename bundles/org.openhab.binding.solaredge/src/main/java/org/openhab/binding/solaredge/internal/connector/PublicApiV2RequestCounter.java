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
package org.openhab.binding.solaredge.internal.connector;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * Persists hourly Monitoring API V2 request counts for a rolling 30-day total.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PublicApiV2RequestCounter {
    private static final String STORAGE_KEY = "publicApiV2RequestCounts";
    private static final long WINDOW_HOURS = Duration.ofDays(30).toHours();

    private final Storage<String> storage;
    private final Clock clock;
    private final TreeMap<Long, Integer> hourlyCounts = new TreeMap<>();

    public PublicApiV2RequestCounter(Storage<String> storage) {
        this(storage, Clock.systemUTC());
    }

    PublicApiV2RequestCounter(Storage<String> storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
        restore();
    }

    public synchronized int recordRequest() {
        long currentHour = currentHour();
        prune(currentHour);
        hourlyCounts.merge(currentHour, 1, Integer::sum);
        persist();
        return total();
    }

    public synchronized int getRequestCount() {
        long currentHour = currentHour();
        if (prune(currentHour)) {
            persist();
        }
        return total();
    }

    private void restore() {
        @Nullable
        String stored = storage.get(STORAGE_KEY);
        if (stored == null || stored.isBlank()) {
            return;
        }
        for (String bucket : stored.split(",")) {
            String[] parts = bucket.split("=", 2);
            if (parts.length == 2) {
                try {
                    hourlyCounts.put(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    // Ignore damaged buckets while retaining any valid request history.
                }
            }
        }
        prune(currentHour());
    }

    private boolean prune(long currentHour) {
        Map<Long, Integer> expired = hourlyCounts.headMap(currentHour - WINDOW_HOURS + 1);
        boolean changed = !expired.isEmpty();
        expired.clear();
        return changed;
    }

    private int total() {
        return hourlyCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private long currentHour() {
        return clock.instant().getEpochSecond() / 3600;
    }

    private void persist() {
        StringBuilder value = new StringBuilder();
        for (Map.Entry<Long, Integer> entry : hourlyCounts.entrySet()) {
            if (!value.isEmpty()) {
                value.append(',');
            }
            value.append(entry.getKey()).append('=').append(entry.getValue());
        }
        storage.put(STORAGE_KEY, value.toString());
    }
}
