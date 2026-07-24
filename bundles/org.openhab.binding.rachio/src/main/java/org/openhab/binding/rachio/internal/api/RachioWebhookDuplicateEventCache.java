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
package org.openhab.binding.rachio.internal.api;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Small bounded in-memory cache for deduplicating successfully processed Rachio webhook event IDs.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioWebhookDuplicateEventCache {
    private static final long DEFAULT_RETENTION_MILLIS = Duration.ofHours(24).toMillis();
    private static final int DEFAULT_MAX_ENTRIES = 2048;
    private static final long CLEANUP_INTERVAL_MILLIS = Duration.ofMinutes(5).toMillis();

    private final ConcurrentHashMap<String, Long> eventIds = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupMillis = new AtomicLong();
    private final long retentionMillis;
    private final int maxEntries;
    private final LongSupplier clockMillis;

    RachioWebhookDuplicateEventCache() {
        this(DEFAULT_RETENTION_MILLIS, DEFAULT_MAX_ENTRIES, System::currentTimeMillis);
    }

    RachioWebhookDuplicateEventCache(long retentionMillis, int maxEntries, LongSupplier clockMillis) {
        this.retentionMillis = retentionMillis;
        this.maxEntries = maxEntries;
        this.clockMillis = clockMillis;
    }

    boolean isProcessed(@Nullable String eventId) {
        String normalizedEventId = normalizeEventId(eventId);
        if (normalizedEventId == null) {
            return false;
        }

        long now = clockMillis.getAsLong();
        cleanupIfNeeded(now);

        Long previous = eventIds.get(normalizedEventId);
        if (previous == null) {
            return false;
        }

        if (isExpired(previous.longValue(), now)) {
            eventIds.remove(normalizedEventId, previous);
            return false;
        }
        return true;
    }

    void markProcessed(@Nullable String eventId) {
        String normalizedEventId = normalizeEventId(eventId);
        if (normalizedEventId == null) {
            return;
        }

        long now = clockMillis.getAsLong();
        cleanupIfNeeded(now);
        eventIds.put(normalizedEventId, now);
        trimToMaxEntries();
    }

    int size() {
        return eventIds.size();
    }

    private @Nullable String normalizeEventId(@Nullable String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return null;
        }
        return eventId.trim();
    }

    private void cleanupIfNeeded(long now) {
        long previousCleanup = lastCleanupMillis.get();
        if (eventIds.size() <= maxEntries && (now - previousCleanup) < CLEANUP_INTERVAL_MILLIS) {
            return;
        }
        if (!lastCleanupMillis.compareAndSet(previousCleanup, now)) {
            return;
        }
        eventIds.entrySet().removeIf(entry -> isExpired(entry.getValue().longValue(), now));
        trimToMaxEntries();
    }

    private boolean isExpired(long eventMillis, long now) {
        return retentionMillis >= 0 && (now - eventMillis) > retentionMillis;
    }

    private void trimToMaxEntries() {
        int overflow = eventIds.size() - maxEntries;
        if (overflow <= 0) {
            return;
        }
        eventIds.entrySet().stream().sorted(Comparator.comparingLong(Map.Entry::getValue)).limit(overflow)
                .forEach(entry -> eventIds.remove(entry.getKey(), entry.getValue()));
    }
}
