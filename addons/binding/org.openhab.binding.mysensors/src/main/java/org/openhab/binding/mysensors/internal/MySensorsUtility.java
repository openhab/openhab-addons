/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.openhab.binding.mysensors.internal.exception.MergeException;

/**
 * Class gives some utility methods that not belong to a specific class
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsUtility {
    /**
     * Invert a generics map swapping key with value
     *
     * @param map the map to be inverted
     * @param hasDuplicate if true only one value (randomly) will be used as key in map that contains same value for
     *            different keys.
     * @return the new inverted map
     */
    public static <V, K> Map<V, K> invertMap(Map<K, V> map, boolean hasDuplicate) {
        if (!hasDuplicate) {
            return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey()));
        } else {
            return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey(), (a, b) -> a));
        }
    }

    /**
     * Join two map
     *
     * @param map1
     * @param map2
     * @return the new map that contains the entry of map1 and map2
     */
    public static <K, V> Map<K, V> joinMap(Map<K, V> map1, Map<K, V> map2) {
        HashMap<K, V> joinMap = new HashMap<K, V>();
        joinMap.putAll(map1);
        joinMap.putAll(map2);
        return joinMap;
    }

    /**
     * Merge one map in another one
     *
     * @param map1 the destination map, will be the merged map.
     * @param map2 the map that will be merged into map1
     *
     * @throws NullPointerException
     */
    public static <K, V> void mergeMap(Map<K, V> map1, Map<K, V> map2, boolean allowOverwrite)
            throws MergeException {
        if (!allowOverwrite) {
            map1.keySet().forEach((a) -> {
                if (map2.containsKey(a)) {
                    throw new IllegalArgumentException("Same key found in map: " + a);
                }
            });
        }

        map1.putAll(map2);
    }

    public static <K, V> boolean containsSameKey(Map<K, V> map1, Map<K, V> map2) {
        return map1.keySet().equals(map2.keySet());
    }
}
