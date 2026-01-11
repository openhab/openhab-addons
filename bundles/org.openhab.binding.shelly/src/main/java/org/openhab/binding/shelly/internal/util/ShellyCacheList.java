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
package org.openhab.binding.shelly.internal.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;

/**
 * {@link ShellyCacheList} implements a cache with expiring times of the entries
 *
 * @author Markus Michels - Initial contribution
 * @author Jacob Laursen - Refacoring to make it error prune and thread safe
 */
@NonNullByDefault
public class ShellyCacheList<K, V> {
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("ShellyCacheListThreadpool");
    private static final long EXPIRY_IN_SEC = 15 * 60; // 15min
    private final long expiryInSec;

    private record CacheEntry<V>(Long created, V value) {
    }

    // Non-thread-safe HashMap: all access to 'storage' is synchronized on this instance
    private final @NonNullByDefault({}) Map<K, CacheEntry<V>> storage = new HashMap<>();

    // All access must be guarded by "this"
    private volatile @Nullable ScheduledFuture<?> cleanupJob;

    public ShellyCacheList() {
        expiryInSec = EXPIRY_IN_SEC;
    }

    public ShellyCacheList(long expiryInSec) {
        this.expiryInSec = expiryInSec;
    }

    @Nullable
    public synchronized V get(K key) {
        Objects.requireNonNull(key, "key must not be null");
        CacheEntry<V> entry = storage.get(key);
        return entry == null ? null : entry.value;
    }

    public @Nullable V put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        CacheEntry<V> previous;
        synchronized (this) {
            previous = storage.put(key, new CacheEntry<>(System.currentTimeMillis(), value));
            startJob(); // start background cleanup
        }
        return previous == null ? null : previous.value();
    }

    public synchronized boolean putIfAbsent(K key, V value, BiPredicate<V, V> isDuplicate) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        CacheEntry<V> existing = storage.get(key);
        if (existing != null) {
            if (!isExpired(existing) && isDuplicate.test(existing.value(), value)) {
                return false;
            }
        }
        storage.put(key, new CacheEntry<>(System.currentTimeMillis(), value));
        startJob();
        return true;
    }

    private synchronized void startJob() {
        if (cleanupJob == null) {
            cleanupJob = scheduler.scheduleWithFixedDelay(this::cleanupMap, expiryInSec, expiryInSec, TimeUnit.SECONDS);
        }
    }

    private void cleanupMap() {
        Entry<K, CacheEntry<V>> entry;
        synchronized (this) {
            for (Iterator<Entry<K, CacheEntry<V>>> iterator = storage.entrySet().iterator(); iterator.hasNext();) {
                entry = iterator.next();
                if (isExpired(entry.getValue())) {
                    iterator.remove();
                }
            }

            if (storage.isEmpty()) {
                cancelJob(); // stop background cleanup
            }
        }
    }

    public boolean isExpired(CacheEntry<V> ce) {
        return System.currentTimeMillis() > (ce.created() + expiryInSec * 1000);
    }

    private synchronized void cancelJob() {
        ScheduledFuture<?> cleanupJob = this.cleanupJob;
        if (cleanupJob != null) {
            cleanupJob.cancel(true);
            this.cleanupJob = null;
        }
    }

    public synchronized void dispose() {
        cancelJob();
        storage.clear();
    }
}
