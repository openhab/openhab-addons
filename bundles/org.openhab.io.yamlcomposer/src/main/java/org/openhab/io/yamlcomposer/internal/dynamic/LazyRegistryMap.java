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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A lazy-loading map that retrieves entities from a registry on demand.
 * Cached entries are stored in memory for subsequent access, and the entire registry can be loaded when needed.
 *
 * @param <T> The type of the entity in the registry.
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault({})
public class LazyRegistryMap<T> extends AbstractMap<String, Map<String, @Nullable Object>> {
    private final Function<String, @Nullable T> lookupFunction;
    private final Supplier<Collection<T>> allEntitiesSupplier;
    private final Function<T, String> keyExtractor;
    private final Function<T, Map<String, @Nullable Object>> adapter;
    private final Map<String, Map<String, @Nullable Object>> cache = new LinkedHashMap<>();
    private boolean fullyLoaded = false;

    public LazyRegistryMap(Function<String, @Nullable T> lookupFunction, Supplier<Collection<T>> allEntitiesSupplier,
            Function<T, String> keyExtractor, Function<T, Map<String, @Nullable Object>> adapter) {
        this.lookupFunction = lookupFunction;
        this.allEntitiesSupplier = allEntitiesSupplier;
        this.keyExtractor = keyExtractor;
        this.adapter = adapter;
    }

    @Override
    public @Nullable Map<String, @Nullable Object> get(@Nullable Object key) {
        if (!(key instanceof String keyStr)) {
            return null;
        }
        if (cache.containsKey(keyStr)) {
            return cache.get(keyStr);
        }
        T entity = lookupFunction.apply(keyStr);
        if (entity == null) {
            return null;
        }
        Map<String, @Nullable Object> adapted = adapter.apply(entity);
        cache.put(keyStr, adapted);
        return adapted;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        if (!(key instanceof String keyStr)) {
            return false;
        }
        if (cache.containsKey(keyStr)) {
            return true;
        }
        return lookupFunction.apply(keyStr) != null;
    }

    @Override
    public Set<Entry<String, Map<String, @Nullable Object>>> entrySet() {
        if (!fullyLoaded) {
            for (T entity : allEntitiesSupplier.get()) {
                String key = keyExtractor.apply(entity);
                cache.computeIfAbsent(key, k -> adapter.apply(entity));
            }
            fullyLoaded = true;
        }
        return cache.entrySet();
    }
}
