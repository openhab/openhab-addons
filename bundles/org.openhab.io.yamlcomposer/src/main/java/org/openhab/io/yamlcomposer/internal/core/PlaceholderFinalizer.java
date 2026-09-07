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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;

/**
 * Executes the finalization sequence across a composed data tree by first purging
 * internal {@link RemovePlaceholder} sentinels and then delegating remaining placeholder
 * unwrapping (such as {@link FreezePlaceholder} and {@link DefaultPlaceholder}) to
 * {@link RecursiveTransformer} under {@link ProcessingPhase#FINALIZATION}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class PlaceholderFinalizer {

    private PlaceholderFinalizer() {
        // Utility class
    }

    /**
     * Purges {@link RemovePlaceholder} sentinels and runs the {@link ProcessingPhase#FINALIZATION}
     * transformation phase on the cleaned tree.
     *
     * @param node the root data structure to finalize
     * @param transformer the recursive transformer to perform the finalization pass
     * @param context the base evaluation context
     * @return the fully finalized data structure
     */
    public static @Nullable Object finalize(@Nullable Object node, RecursiveTransformer transformer,
            EvaluationContext context) {

        Object cleanedNode = purgeRemovals(node, Collections.newSetFromMap(new IdentityHashMap<>()));

        EvaluationContext finalizationContext = context.withProcessingPhase(ProcessingPhase.FINALIZATION);
        return transformer.transform(cleanedNode, finalizationContext);
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Object purgeRemovals(@Nullable Object node, Set<Object> visited) {
        if (node == null || !visited.add(node)) {
            return node;
        }

        if (node instanceof Map<?, ?> map) {
            return purgeMap((Map<Object, @Nullable Object>) map, visited);
        } else if (node instanceof List<?> list) {
            return purgeList((List<@Nullable Object>) list, visited);
        }

        return node;
    }

    private static Map<Object, @Nullable Object> purgeMap(Map<Object, @Nullable Object> map, Set<Object> visited) {
        boolean needsModification = false;
        List<Object> keysToRemove = new ArrayList<>();
        Map<Object, @Nullable Object> updatedEntries = new LinkedHashMap<>();

        for (Map.Entry<Object, @Nullable Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof RemovePlaceholder) {
                keysToRemove.add(key);
                needsModification = true;
            } else if (value instanceof Map<?, ?> || value instanceof List<?>) {
                Object cleanedValue = purgeRemovals(value, visited);
                if (!Objects.equals(cleanedValue, value)) {
                    updatedEntries.put(key, cleanedValue);
                    needsModification = true;
                }
            }
        }

        if (!needsModification) {
            return map;
        }

        try {
            for (Object key : keysToRemove) {
                map.remove(key);
            }
            map.putAll(updatedEntries);
            return map;
        } catch (UnsupportedOperationException e) {
            Map<Object, @Nullable Object> mutableCopy = new LinkedHashMap<>(map);
            for (Object key : keysToRemove) {
                mutableCopy.remove(key);
            }
            mutableCopy.putAll(updatedEntries);
            return mutableCopy;
        }
    }

    private static List<@Nullable Object> purgeList(List<@Nullable Object> list, Set<Object> visited) {
        boolean needsModification = false;
        List<@Nullable Object> cleanedList = new ArrayList<>(list.size());

        for (Object item : list) {
            if (item instanceof RemovePlaceholder) {
                needsModification = true;
            } else if (item instanceof Map<?, ?> || item instanceof List<?>) {
                Object cleanedItem = purgeRemovals(item, visited);
                if (!Objects.equals(cleanedItem, item)) {
                    needsModification = true;
                }
                cleanedList.add(cleanedItem);
            } else {
                cleanedList.add(item);
            }
        }

        if (!needsModification) {
            return list;
        }

        try {
            list.clear();
            list.addAll(cleanedList);
            return list;
        } catch (UnsupportedOperationException e) {
            return cleanedList;
        }
    }
}
