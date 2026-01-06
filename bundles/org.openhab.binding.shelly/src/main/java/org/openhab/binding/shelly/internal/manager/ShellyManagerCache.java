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
package org.openhab.binding.shelly.internal.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;

/**
 * {@link ShellyManagerCache} implements a cache with expiring times of the entries
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerCache<K, V> {
    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("ShellyManagerThreadpool");
    private static final long EXPIRY_IN_MILLIS = 15 * 60 * 1000; // 15min

    private record CacheEntry<V> (Long created, V value) {
    }

    // Non-thread-safe HashMap: all access to 'storage' is synchronized on this instance
    private final @NonNullByDefault({}) Map<K, CacheEntry<V>> storage = new HashMap<>();

    // All access must be guarded by "this"
    private @Nullable ScheduledFuture<?> cleanupJob;

    public ShellyManagerCache() {
    }

    @Nullable
    public synchronized V get(K key) {
        CacheEntry<V> entry = storage.get(key);
        return entry == null ? null : entry.value;
    }

    public V put(K key, V value) {
        CacheEntry<V> entry = new CacheEntry<>(System.currentTimeMillis(), value);
        synchronized (this) {
            entry = storage.put(key, entry);
            startJob(); // start background cleanup
        }
        return entry == null ? null : value;
    }

    private synchronized void startJob() {
        if (cleanupJob == null) {
            cleanupJob = scheduler.scheduleWithFixedDelay(this::cleanupMap, EXPIRY_IN_MILLIS, EXPIRY_IN_MILLIS,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void cleanupMap() {
        long currentTime = System.currentTimeMillis();
        Entry<K, CacheEntry<V>> entry;
        synchronized (this) {
            for (Iterator<Entry<K, CacheEntry<V>>> iterator = storage.entrySet().iterator(); iterator.hasNext();) {
                entry = iterator.next();
                if (currentTime > (entry.getValue().created.longValue() + EXPIRY_IN_MILLIS)) {
                    iterator.remove();
                }
            }

            if (storage.isEmpty()) {
                cancelJob(); // stop background cleanup
            }
        }
    }

    private synchronized void cancelJob() {
        if (cleanupJob != null) {
            cleanupJob.cancel(true);
            cleanupJob = null;
        }
    }

    public synchronized void dispose() {
        cancelJob();
        storage.clear();
    }
}
