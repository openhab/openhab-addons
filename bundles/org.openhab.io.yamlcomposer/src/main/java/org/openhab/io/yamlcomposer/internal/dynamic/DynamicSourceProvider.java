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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Supplies raw entities and entity-level change notifications for a dynamic data source (e.g., "things").
 *
 * @param <T> entity type exposed by this source
 */
@NonNullByDefault
public interface DynamicSourceProvider<T> {
    /** Returns source identifier (e.g. "ITEMS", "THINGS"). */
    String getSourceName();

    /** Returns true if this provider supports the requested source identifier (e.g. "THINGS"). */
    boolean supportsSource(String source);

    /** Returns all current raw entities from the underlying system registry. */
    Collection<T> getAllEntities();

    /** Returns the unique primary key for a raw entity (e.g. Item name or Thing UID). */
    String getKey(T entity);

    /** Converts a raw entity instance into a standard property map. */
    Map<String, @Nullable Object> adaptToMap(T entity);

    /** Registers a listener to receive discrete entity change events. */
    void setOnChangeListener(Consumer<EntityChange> listener);

    /** Returns an unmodifiable point-in-time snapshot map of all entities. */
    default Map<String, Map<String, @Nullable Object>> getSourceMap() {
        Map<String, Map<String, @Nullable Object>> map = new LinkedHashMap<>();
        for (T entity : getAllEntities()) {
            map.put(getKey(entity), adaptToMap(entity));
        }
        return Collections.unmodifiableMap(map);
    }
}
