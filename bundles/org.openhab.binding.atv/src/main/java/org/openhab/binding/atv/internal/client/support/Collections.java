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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Map merging utilities. {@link CaseInsensitiveMap} covers case-insensitive lookups.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Collections {

    private Collections() {
        // static utility class
    }

    /**
     * Merges entries from {@code from} into {@code into}, not overriding existing keys.
     *
     * @param <K> key type
     * @param <V> value type
     * @param into target map, modified in place
     * @param from source map, not modified
     * @return {@code into} (the same instance, for chaining)
     */
    public static <K, V> Map<K, V> dictMerge(Map<K, V> into, Map<? extends K, ? extends V> from) {
        return dictMerge(into, from, false);
    }

    /**
     * Merges entries from {@code from} into {@code into}. When {@code allowOverwrite} is
     * {@code false}, only keys absent in {@code into} are copied; otherwise all entries from
     * {@code from} are copied.
     *
     * @param <K> key type
     * @param <V> value type
     * @param into target map, modified in place
     * @param from source map, not modified
     * @param allowOverwrite whether existing keys in {@code into} may be overwritten
     * @return {@code into} (the same instance, for chaining)
     */
    public static <K, V> Map<K, V> dictMerge(Map<K, V> into, Map<? extends K, ? extends V> from,
            boolean allowOverwrite) {
        for (Map.Entry<? extends K, ? extends V> entry : from.entrySet()) {
            if (allowOverwrite || !into.containsKey(entry.getKey())) {
                into.put(entry.getKey(), entry.getValue());
            }
        }
        return into;
    }
}
