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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link Map} with String keys compared case-insensitively.
 *
 * <p>
 * Keys are lowercased on insertion, so iteration yields lowercase keys in insertion order
 * (backed by a {@link LinkedHashMap}). Re-inserting an existing key (in any case) keeps its
 * original position.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CaseInsensitiveMap<V> implements Map<String, V> {

    private final LinkedHashMap<String, V> data = new LinkedHashMap<>();

    /**
     * Creates an empty map.
     */
    public CaseInsensitiveMap() {
    }

    /**
     * Creates a map with all entries of {@code initial}, keys compared case-insensitively. If
     * {@code initial} contains keys that only differ in case, the value of the last one iterated
     * wins.
     *
     * @param initial entries to copy
     */
    public CaseInsensitiveMap(Map<String, ? extends V> initial) {
        putAll(initial);
    }

    private static String lower(String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    private static @Nullable String lowerIfString(@Nullable Object key) {
        return key instanceof String s ? lower(s) : null;
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        String lowered = lowerIfString(key);
        return lowered == null ? null : data.get(lowered);
    }

    @Override
    public @Nullable V put(String key, V value) {
        return data.put(lower(key), value);
    }

    @Override
    public @Nullable V remove(@Nullable Object key) {
        String lowered = lowerIfString(key);
        return lowered == null ? null : data.remove(lowered);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        String lowered = lowerIfString(key);
        return lowered != null && data.containsKey(lowered);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return data.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<V> values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return data.entrySet();
    }

    /**
     * Compares two maps with keys compared case-insensitively. A plain {@link Map} with String
     * keys is equal to this map if it has the same entries after lowercasing its keys.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof CaseInsensitiveMap<?> cim) {
            return data.equals(cim.data);
        }
        if (other instanceof Map<?, ?> map) {
            if (map.size() != data.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String lowered = lowerIfString(entry.getKey());
                if (lowered == null) {
                    // Non-string keys: the maps cannot be equal.
                    return false;
                }
                V value = data.get(lowered);
                if (value == null ? !(data.containsKey(lowered) && entry.getValue() == null)
                        : !value.equals(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
