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
package org.openhab.automation.jrubyscripting.internal.watch;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;

// Copy of org.openhab.core.automation.module.script.rulesupport.internal.loader.collection.BidiSetBag

/**
 * Bidirectional bag of unique elements. A map allowing multiple, unique values to be stored against a single key.
 * Provides optimized lookup of values for a key, as well as keys referencing a value.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Jan N. Klug - Make implementation thread-safe
 * @param <K> Type of Key
 * @param <V> Type of Value
 */
@NonNullByDefault
public class BidiSetBag<K, V> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<K, Set<V>> keyToValues = new HashMap<>();
    private final Map<V, Set<K>> valueToKeys = new HashMap<>();

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            keyToValues.computeIfAbsent(key, k -> new HashSet<>()).add(value);
            valueToKeys.computeIfAbsent(value, v -> new HashSet<>()).add(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<V> getValues(K key) {
        lock.readLock().lock();
        try {
            Set<V> values = keyToValues.getOrDefault(key, Set.of());
            return Collections.unmodifiableSet(values);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<K> getKeys(V value) {
        lock.readLock().lock();
        try {
            Set<K> keys = valueToKeys.getOrDefault(value, Set.of());
            return Collections.unmodifiableSet(keys);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<V> removeKey(K key) {
        lock.writeLock().lock();
        try {
            Set<V> values = keyToValues.remove(key);
            if (values != null) {
                for (V value : values) {
                    valueToKeys.computeIfPresent(value, (k, v) -> {
                        v.remove(key);
                        return v;
                    });
                }
                return values;
            } else {
                return Set.of();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<K> removeValue(V value) {
        lock.writeLock().lock();
        try {
            Set<K> keys = valueToKeys.remove(value);
            if (keys != null) {
                for (K key : keys) {
                    keyToValues.computeIfPresent(key, (k, v) -> {
                        v.remove(value);
                        return v;
                    });
                }
                return keys;
            } else {
                return Set.of();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
