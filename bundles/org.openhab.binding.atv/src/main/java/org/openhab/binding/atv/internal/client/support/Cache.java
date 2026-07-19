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
package org.openhab.binding.atv.internal.client.support;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple LRU cache for data based on an identifier.
 *
 * <p>
 * {@link #get(Object)} refreshes an entry's recency and {@link #put(Object, Object)} evicts
 * the least recently used entry beyond the limit. All methods are synchronized, since callers
 * may use the cache from several threads.
 *
 * @param <K> identifier type
 * @param <V> data type
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Cache<K, V> {

    private final int limit;
    private final LinkedHashMap<K, V> data;

    /**
     * Creates a cache.
     *
     * @param limit maximum number of entries kept
     */
    public Cache(int limit) {
        this.limit = limit;
        this.data = new LinkedHashMap<>(16, 0.75f, true);
    }

    /**
     * Returns whether the cache is empty.
     */
    public synchronized boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the number of cached entries.
     */
    public synchronized int size() {
        return data.size();
    }

    /**
     * Returns whether an identifier is cached.
     */
    public synchronized boolean contains(K identifier) {
        return data.containsKey(identifier);
    }

    /**
     * Puts something in the cache, evicting the least recently used entry when full.
     *
     * @param identifier entry identifier
     * @param value data to cache
     */
    public synchronized void put(K identifier, V value) {
        data.put(identifier, value);
        if (data.size() > limit) {
            Iterator<K> iterator = data.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    /**
     * Gets something from the cache, making the entry the most recently used.
     *
     * @param identifier entry identifier
     * @return the cached data, or {@code null} when absent
     */
    public synchronized @Nullable V get(K identifier) {
        return data.get(identifier);
    }

    /**
     * Returns the identifier of the most recently used entry.
     *
     * @return most recent identifier, or {@code null} when empty
     */
    public synchronized @Nullable K latest() {
        K result = null;
        for (K key : data.keySet()) {
            result = key;
        }
        return result;
    }
}
