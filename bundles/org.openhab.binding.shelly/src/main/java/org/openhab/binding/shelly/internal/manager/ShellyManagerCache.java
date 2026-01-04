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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class ShellyManagerCache<K, V> extends ConcurrentHashMap<K, V> {
    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("ShellyManagerThreadpool");
    private static final long serialVersionUID = 1L;

    private Map<K, Long> timeMap = new ConcurrentHashMap<>();

    private @Nullable ScheduledFuture<?> cleanupJob;
    private static long expiryInMillis = 15 * 60 * 1000; // 15min

    public ShellyManagerCache() {
        initialize();
    }

    public void initialize() {
        if (cleanupJob == null) {
            // start background cleanup
            cleanupJob = scheduler.scheduleWithFixedDelay(this::cleanupMap, 2, 60, TimeUnit.SECONDS);
        }
    }

    private void cleanupMap() {
        long currentTime = new Date().getTime();
        for (K key : timeMap.keySet()) {
            Long timeValue = timeMap.get(key);
            if (key != null && (timeValue == null || currentTime > (timeValue + expiryInMillis))) {
                remove(key);
                timeMap.remove(key);
            }
        }
    }

    @Override
    public V put(K key, V value) {
        Date date = new Date();
        timeMap.put(key, date.getTime());
        return super.put(key, value);
    }

    @Override
    public void putAll(@Nullable Map<? extends K, ? extends V> m) {
        if (m == null) {
            throw new IllegalArgumentException();
        }
        for (K key : m.keySet()) {
            @Nullable
            V value = m.get(key);
            if (key != null && value != null) { // don't allow null values
                put(key, value);
            }
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (!containsKey(key)) {
            return put(key, value);
        } else {
            return get(key);
        }
    }
}
