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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("ShellyCacheListThreadpool");
    private static final long EXPIRY_IN_SEC = 15 * 60; // 15min
    private long experyInSec;

    private record CacheEntry<V>(Long created, V value) {
    }

    // Non-thread-safe HashMap: all access to 'storage' is synchronized on this instance
    private final @NonNullByDefault({}) Map<K, CacheEntry<V>> storage = new HashMap<>();

    // All access must be guarded by "this"
    private @Nullable ScheduledFuture<?> cleanupJob;

    public ShellyCacheList() {
        experyInSec = EXPIRY_IN_SEC;
    }

    public ShellyCacheList(long experyInSec) {
        this.experyInSec = experyInSec;
    }

    @Nullable
    public synchronized V get(K key) {
        CacheEntry<V> entry = storage.get(key);
        return entry == null ? null : entry.value;
    }

    public @Nullable V put(K key, V value) {
        CacheEntry<V> entry = new CacheEntry<>(System.currentTimeMillis(), value);
        synchronized (this) {
            entry = storage.put(key, entry);
            startJob(); // start background cleanup
        }
        return entry == null ? null : value;
    }

    private synchronized void startJob() {
        if (cleanupJob == null) {
            cleanupJob = scheduler.scheduleWithFixedDelay(this::cleanupMap, experyInSec, experyInSec, TimeUnit.SECONDS);
        }
    }

    private void cleanupMap() {
        Entry<K, CacheEntry<V>> entry;
        synchronized (this) {
            for (Iterator<Entry<K, CacheEntry<V>>> iterator = storage.entrySet().iterator(); iterator.hasNext();) {
                entry = iterator.next();
                if (isExpired(entry)) {
                    iterator.remove();
                }
            }

            if (storage.isEmpty()) {
                cancelJob(); // stop background cleanup
            }
        }
    }

    public boolean isExpired(Entry<K, CacheEntry<V>> entry) {
        return System.currentTimeMillis() > (entry.getValue().created.longValue() + experyInSec * 1000);
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
