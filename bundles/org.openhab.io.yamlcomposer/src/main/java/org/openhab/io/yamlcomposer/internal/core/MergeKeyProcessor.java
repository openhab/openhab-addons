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
package org.openhab.io.yamlcomposer.internal.core;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;

/**
 * The {@code MergeKeyProcessor} processes {@link MergeKeyPlaceholder} instances in YAML models.
 * It merges the associated values into the surrounding map according to YAML 1.1 merge key semantics.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class MergeKeyProcessor {
    BufferedLogger logger;

    public MergeKeyProcessor(BufferedLogger logger) {
        this.logger = logger;
    }

    /**
     * Processes all {@link MergeKeyPlaceholder} keys in the given YAML node, merging their values into the surrounding
     * map according to YAML 1.1 merge key semantics. Placeholders are removed after merging. The node may be a map or a
     * list containing maps.
     *
     * @param node the YAML structure to process
     */
    public void resolveMergeKeys(@Nullable Object node) {
        if (node instanceof Map<?, ?> map) {
            resolveMap(map);
        } else if (node instanceof List<?> list) {
            for (Object item : list) {
                resolveMergeKeys(item);
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void resolveMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return;
        }
        Map<Object, @Nullable Object> objMap = (Map<Object, @Nullable Object>) map;
        objMap.keySet().removeIf(Objects::isNull);

        for (Object value : objMap.values()) {
            resolveMergeKeys(value);
        }

        List<Map.Entry<MergeKeyPlaceholder, @Nullable Object>> mergeEntries = new ArrayList<>();
        for (Map.Entry<Object, @Nullable Object> entry : objMap.entrySet()) {
            if (entry.getKey() instanceof MergeKeyPlaceholder mkp) {
                mergeEntries.add(new AbstractMap.SimpleEntry<>(mkp, entry.getValue()));
            }
        }
        for (var mergeEntry : mergeEntries) {
            MergeKeyPlaceholder mkp = mergeEntry.getKey();
            @Nullable
            Object value = mergeEntry.getValue();
            if (value instanceof Map<?, ?> m) {
                mergeMap((Map<Object, @Nullable Object>) m, objMap);
            } else if (value instanceof List<?> l) {
                for (Object item : l) {
                    if (item instanceof Map<?, ?> m2) {
                        mergeMap((Map<Object, @Nullable Object>) m2, objMap);
                    }
                }
            } else {
                logger.warn("{} Expected a map or list of maps as value for merging, but found: {}",
                        mkp.sourceLocation(), value == null ? "null" : value.getClass().getSimpleName());
            }
            objMap.remove(mergeEntry.getKey());
        }
    }

    private void mergeMap(Map<Object, @Nullable Object> from, Map<Object, @Nullable Object> to) {
        for (Map.Entry<Object, @Nullable Object> entry : from.entrySet()) {
            Object key = entry.getKey();
            if (!to.containsKey(key)) {
                to.put(key, entry.getValue());
            }
        }
    }
}
