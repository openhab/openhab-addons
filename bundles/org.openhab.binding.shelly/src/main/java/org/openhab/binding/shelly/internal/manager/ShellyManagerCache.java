/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ShellyManagerCache} implements a cache with expiring times of the entries
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyManagerCache<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private Map<K, Long> timeMap = new ConcurrentHashMap<>();
    private long expiryInMillis = ShellyManagerConstants.CACHE_TIMEOUT_DEF_MIN * 60 * 1000; // Default 1h

    public ShellyManagerCache() {
        initialize();
    }

    public ShellyManagerCache(long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
        initialize();
    }

    void initialize() {
        new CleanerThread().start();
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
            V value = m.get(key);
            if (value != null) { // don't allow null values
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

    class CleanerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                cleanMap();
                try {
                    Thread.sleep(expiryInMillis / 2);
                } catch (InterruptedException e) {
                }
            }
        }

        private void cleanMap() {
            long currentTime = new Date().getTime();
            for (K key : timeMap.keySet()) {
                if (currentTime > (timeMap.get(key) + expiryInMillis)) {
                    remove(key);
                    timeMap.remove(key);
                }
            }
        }
    }
}
