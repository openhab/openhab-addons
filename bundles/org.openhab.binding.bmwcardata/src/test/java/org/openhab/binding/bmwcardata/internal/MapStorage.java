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
package org.openhab.binding.bmwcardata.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * In-memory {@link Storage} for tests.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class MapStorage implements Storage<String> {

    private final Map<String, String> map = new HashMap<>();

    @Override
    public @Nullable String put(String key, @Nullable String value) {
        return value == null ? map.remove(key) : map.put(key, value);
    }

    @Override
    public @Nullable String remove(String key) {
        return map.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public @Nullable String get(String key) {
        return map.get(key);
    }

    @Override
    public Collection<String> getKeys() {
        return map.keySet();
    }

    @Override
    public Collection<@Nullable String> getValues() {
        return Map.<String, @Nullable String> copyOf(map).values();
    }
}
