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
package org.openhab.binding.icloud.internal.utilities;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of simple pair. Used mainly for HTTP header handling.
 *
 * @author Simon Spielmann - Initial contribution.
 * @param <K> Type of first element
 * @param <V> Type of second element
 */
@NonNullByDefault
public class Pair<@NonNull K, @NonNull V> {

    private K key;

    private V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Create pair with key and value. Both of type {@link String}.
     *
     * @param key Key
     * @param value Value
     * @return Pair with given key and value
     */
    public static Pair<String, String> of(String key, String value) {
        return new Pair<>(key, value);
    }

    @Override
    public String toString() {
        return "Pair [key=" + this.key + ", value=" + this.value + "]";
    }

    /**
     * @return key
     */
    public K getKey() {
        return this.key;
    }

    /**
     * @return value
     */
    public V getValue() {
        return this.value;
    }
}
