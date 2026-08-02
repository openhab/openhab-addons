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
 * Small bounded in-memory cache for atomically claiming and deduplicating Rachio webhook event IDs.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class RachioWebhookDuplicateEventCache {
    private static final long DEFAULT_RETENTION_MILLIS = Duration.ofHours(24).toMillis();
    private static final int DEFAULT_MAX_ENTRIES = 2048;
    private static final long CLEANUP_INTERVAL_MILLIS = Duration.ofMinutes(5).toMillis();

    private static final EventClaim UNTRACKED_CLAIM = new EventClaim(null, 0);

    private final Map<String, EventClaim> eventIds = new ConcurrentHashMap<>();
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

    @Nullable
    EventClaim claim(@Nullable String eventId) {
        String normalizedEventId = normalizeEventId(eventId);
        if (normalizedEventId == null) {
            return UNTRACKED_CLAIM;
        }

        long now = clockMillis.getAsLong();
        cleanupIfNeeded(now);
        EventClaim claim = new EventClaim(normalizedEventId, now);

        while (true) {
            EventClaim previous = eventIds.putIfAbsent(normalizedEventId, claim);
            if (previous == null) {
                trimToMaxEntries();
                return claim;
            }
            if (!isExpired(previous.claimedAtMillis(), now)) {
                return null;
            }
            eventIds.remove(normalizedEventId, previous);
        }
    }

    void release(EventClaim claim) {
        @Nullable
        String eventId = claim.eventId();
        if (eventId != null) {
            eventIds.remove(eventId, claim);
        }
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
        eventIds.entrySet().removeIf(entry -> isExpired(entry.getValue().claimedAtMillis(), now));
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
        eventIds.entrySet().stream().sorted(Comparator.comparingLong(entry -> entry.getValue().claimedAtMillis()))
                .limit(overflow).forEach(entry -> eventIds.remove(entry.getKey(), entry.getValue()));
    }

    static final class EventClaim {
        private final @Nullable String eventId;
        private final long claimedAtMillis;

        private EventClaim(@Nullable String eventId, long claimedAtMillis) {
            this.eventId = eventId;
            this.claimedAtMillis = claimedAtMillis;
        }

        private @Nullable String eventId() {
            return eventId;
        }

        private long claimedAtMillis() {
            return claimedAtMillis;
        }
    }
}
