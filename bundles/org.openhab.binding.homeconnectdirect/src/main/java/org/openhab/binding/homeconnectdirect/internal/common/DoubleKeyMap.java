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
package org.openhab.binding.homeconnectdirect.internal.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A memory-optimized, thread-safe map that allows looking up values by two different unique keys.
 * It stores values only once (referenced by K1) and maintains key mappings to allow lookup by K2.
 *
 * @param <K1> The first key type (Primary Key)
 * @param <K2> The second key type (Secondary Key)
 * @param <V> The value type
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class DoubleKeyMap<K1, K2, V> {

    // Primary storage: K1 -> Value
    private final Map<K1, V> k1ToVal = new HashMap<>();

    // Key mappings to maintain 1:1 relationship and allow bi-directional lookups/removals
    private final Map<K1, K2> k1ToK2 = new HashMap<>();
    private final Map<K2, K1> k2ToK1 = new HashMap<>();

    public synchronized void put(K1 k1, K2 k2, V value) {
        removeByKey1(k1);
        removeByKey2(k2);

        k1ToVal.put(k1, value);
        k1ToK2.put(k1, k2);
        k2ToK1.put(k2, k1);
    }

    public synchronized @Nullable V getByKey1(@Nullable K1 k1) {
        if (k1 == null) {
            return null;
        }
        return k1ToVal.get(k1);
    }

    public synchronized @Nullable V getByKey2(@Nullable K2 k2) {
        @Nullable
        K1 k1 = k2ToK1.get(k2);
        if (k1 == null) {
            return null;
        }
        return k1ToVal.get(k1);
    }

    public synchronized @Nullable V removeByKey1(@Nullable K1 k1) {
        if (k1 == null) {
            return null;
        }

        @Nullable
        V value = k1ToVal.remove(k1);
        @Nullable
        K2 k2 = k1ToK2.remove(k1);

        if (k2 != null) {
            k2ToK1.remove(k2);
        }
        return value;
    }

    public synchronized @Nullable V removeByKey2(@Nullable K2 k2) {
        @Nullable
        K1 k1 = k2ToK1.remove(k2);
        if (k1 == null) {
            return null;
        }

        @Nullable
        V value = k1ToVal.remove(k1);
        k1ToK2.remove(k1);

        return value;
    }

    public synchronized void clear() {
        k1ToVal.clear();
        k1ToK2.clear();
        k2ToK1.clear();
    }

    public synchronized int size() {
        return k1ToVal.size();
    }

    public synchronized Set<Map.Entry<K1, V>> entrySet() {
        return k1ToVal.entrySet();
    }

    public synchronized Collection<V> values() {
        return k1ToVal.values();
    }

    @Override
    public synchronized String toString() {
        return "DoubleKeyMap{size=" + k1ToVal.size() + ", data=" + k1ToVal + "}";
    }
}
