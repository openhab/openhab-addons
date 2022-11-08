/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class Pair<K, V> {

    public K key;

    public V value;

    public static Pair of(String key, String value) {

        Pair p = new Pair();
        p.key = key;
        p.value = value;
        return p;
    }

    @Override
    public String toString() {

        return "Pair [key=" + this.key + ", value=" + this.value + "]";
    }
}
