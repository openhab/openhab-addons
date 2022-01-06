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
package org.openhab.binding.unifi.internal.api.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiCache} is a specialised lookup table that stores objects using multiple keys in the form
 * <code>prefix:suffix</code>. Each implementation is responsible for providing a list of supported prefixes and must
 * implement {@link #getSuffix(Object, String)} to provide a value specific suffix derived from the prefix.
 *
 * Objects are then retrieved simply by using the <code>suffix</code> key component and all combinations of
 * <code>prefix:suffix</code> are searched in the order of their priority.
 *
 * @author Matthew Bowman - Initial contribution
 */
public abstract class UniFiCache<T> {

    private static final String SEPARATOR = ":";

    public static final String PREFIX_ALIAS = "alias";

    public static final String PREFIX_DESC = "desc";

    public static final String PREFIX_HOSTNAME = "hostname";

    public static final String PREFIX_ID = "id";

    public static final String PREFIX_IP = "ip";

    public static final String PREFIX_MAC = "mac";

    public static final String PREFIX_NAME = "name";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, T> map = new HashMap<>();

    private String[] prefixes;

    protected UniFiCache(String... prefixes) {
        this.prefixes = prefixes;
    }

    public final T get(Object id) {
        T value = null;
        for (String prefix : prefixes) {
            String key = prefix + SEPARATOR + id;
            if (map.containsKey(key)) {
                value = map.get(key);
                logger.trace("Cache HIT : '{}' -> {}", key, value);
                break;
            } else {
                logger.trace("Cache MISS : '{}'", key);
            }
        }
        return value;
    }

    public final void put(T value) {
        for (String prefix : prefixes) {
            String suffix = getSuffix(value, prefix);
            if (suffix != null && !suffix.isBlank()) {
                String key = prefix + SEPARATOR + suffix;
                map.put(key, value);
            }
        }
    }

    public final void putAll(UniFiCache<T> cache) {
        map.putAll(cache.map);
    }

    public final Collection<T> values() {
        return map.values().stream().distinct().collect(Collectors.toList());
    }

    protected abstract String getSuffix(T value, String prefix);
}
