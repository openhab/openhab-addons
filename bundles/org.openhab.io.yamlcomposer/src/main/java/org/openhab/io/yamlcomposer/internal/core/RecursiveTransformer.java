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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.InterpolablePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;
import org.openhab.io.yamlcomposer.internal.processors.PlaceholderProcessor;

/**
 * The {@link RecursiveTransformer} traverses a YAML data tree, applies merge keys logic,
 * and transforms placeholders into the final values by invoking registered handlers
 * for their respective types.
 *
 * It holds a variables map that can be overridden for nested transformations,
 * allowing for context-specific variable values during processing.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class RecursiveTransformer {

    private final Map<Class<? extends Placeholder>, PlaceholderProcessor<?>> handlers = new LinkedHashMap<>();
    private final Map<String, @Nullable Object> variables;

    public RecursiveTransformer(Map<String, @Nullable Object> variables) {
        this.variables = variables;
    }

    public Map<String, @Nullable Object> getVariables() {
        return variables;
    }

    /**
     * Creates a new RecursiveTransformer with the same handlers but a new variables map that
     * includes the given overrides.
     *
     * The new combined variables map will be used for all placeholder processing within
     * the nested structure, allowing for isolated modifications without impacting the original context.
     *
     * @param overrideVariables additional variables to include in the new transformer's context,
     *            which will override any existing variables with the same keys
     * @return a new RecursiveTransformer instance with the combined variables and the same handlers
     */
    public RecursiveTransformer withOverrideVariables(Map<String, @Nullable Object> overrideVariables) {
        Map<String, @Nullable Object> combinedVariables = new HashMap<>(variables);
        combinedVariables.putAll(overrideVariables);
        RecursiveTransformer copy = new RecursiveTransformer(combinedVariables);
        copy.handlers.putAll(this.handlers);
        return copy;
    }

    /**
     * Registers a handler for a specific placeholder type.
     *
     * @param clazz the placeholder class to handle
     * @param handler the processor that can transform the placeholder
     */
    public void register(PlaceholderProcessor<?> handler) {
        handlers.put(handler.getPlaceholderType(), handler);
    }

    /**
     * Default: transforms the entire tree using all registered handlers.
     * Placeholders are transformed if their class matches any registered handler.
     *
     * This is the most common usage, but the other overloads allow for more control
     * and optimization by restricting which handlers are applied.
     *
     * @param data the YAML data tree to transform
     * @return the transformed data tree with placeholders transformed
     */
    public @Nullable Object transform(@Nullable Object data) {
        return transformWithVisited(data, handlers.keySet());
    }

    /**
     * Transforms the data tree but only applies handlers for the specified placeholder classes.
     *
     * <p>
     * Use this when you want to restrict transformation to a subset of placeholder
     * classes.
     *
     * @param data the YAML data tree to transform
     * @param allowedTypes the set of placeholder classes to transform
     * @return the transformed data tree with the given placeholders transformed
     */
    public @Nullable Object transform(@Nullable Object data, Set<Class<? extends Placeholder>> allowedTypes) {
        return transformWithVisited(data, allowedTypes);
    }

    /**
     * Convenience overload for transforming a Map container.
     *
     * <p>
     * Transforms the given map and transforms any placeholders found within keys and values.
     * This is equivalent to calling {@link #transform(Object, Set)} with the full set of
     * registered placeholder handler types.
     *
     * @param data the map to transform
     * @return the transformed map
     */
    public Map<Object, @Nullable Object> transform(Map<?, ?> data) {
        return transform(data, handlers.keySet());
    }

    /**
     * Transforms the given map but only applies handlers for the specified placeholder classes.
     *
     * <p>
     * Keys and values are transformed recursively. If the overall transformed result is not a
     * {@link Map} (for example, if a placeholder handler returned a non-container), an
     * {@link IllegalStateException} is thrown.
     *
     * @param data the map to transform
     * @param allowedTypes the set of placeholder classes to transform
     * @return the transformed map
     * @throws IllegalStateException if the transformed result is not a Map
     */
    @SuppressWarnings("unchecked")
    public Map<Object, @Nullable Object> transform(Map<?, ?> data, Set<Class<? extends Placeholder>> allowedTypes) {
        Object transformed = transformWithVisited(data, allowedTypes);
        if (transformed instanceof Map<?, ?> map) {
            return (Map<Object, @Nullable Object>) map;
        }
        throw new IllegalStateException("Expected transformed result to be a Map but was: "
                + (transformed == null ? "null" : transformed.getClass()));
    }

    /**
     * Central helper to start transformation with a fresh visited map.
     */
    private @Nullable Object transformWithVisited(@Nullable Object data,
            Set<Class<? extends Placeholder>> allowedTypes) {
        return transformInternal(data, allowedTypes, new IdentityHashMap<>());
    }

    /**
     * The actual tree traversal.
     */
    private @Nullable Object transformInternal(@Nullable Object data, Set<Class<? extends Placeholder>> allowedTypes,
            IdentityHashMap<Object, Object> visited) {

        if (data == null) {
            return null;
        }

        Class<?> clazz = data.getClass();

        // Handle cyclic references for containers: if we've already started transforming
        // this container, return the placeholder/result to avoid infinite recursion.
        if ((data instanceof Map<?, ?> || data instanceof List<?>)) {
            if (visited.containsKey(data)) {
                return visited.get(data);
            }
        }

        // Resolve placeholder value (arguments) first before transforming the placeholder itself
        // So that e.g. !include ${filename} gets the real argument value to transform
        if (data instanceof InterpolablePlaceholder interpolable) {
            Object transformedValue;

            if (interpolable.eagerArgumentProcessing()) {
                // Eagerly transform arguments using all registered handlers
                transformedValue = transform(interpolable.value());
            } else {
                // Only perform substitutions in arguments
                // for !if conditions, don't transform placeholders (e.g. !include) in the unselected branch
                transformedValue = transform(interpolable.value(), ProcessingPhase.SUBSTITUTION);
            }
            data = interpolable.withValue(transformedValue);
        }

        if (data instanceof Placeholder placeholder && allowedTypes.contains(clazz)) {
            // Use the override callback if provided, otherwise look up in registry
            PlaceholderProcessor<?> handler = handlers.get(clazz);

            if (handler != null) {
                // Execute and recurse
                Object result = invokeHandler(handler, placeholder);
                return transformInternal(result, allowedTypes, visited);
            }
        }

        if (data instanceof Map<?, ?> map) {
            return resolveMap(map, allowedTypes, visited);
        }

        if (data instanceof List<?> list) {
            return resolveList(list, allowedTypes, visited);
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    private @Nullable <T extends Placeholder> Object invokeHandler(PlaceholderProcessor<?> handler,
            Placeholder placeholder) {
        return ((PlaceholderProcessor<T>) handler).process((T) placeholder, this);
    }

    /**
     * Resolves a map by transforming its keys and values, applying placeholder handlers as needed,
     * and handling special cases like merge keys and removal signals.
     *
     * @param rawMap the original map to transform
     * @param allowedTypes the set of placeholder classes to transform
     * @return the transformed map with placeholders transformed, or the original map if no changes were made
     */
    private Object resolveMap(Map<?, ?> rawMap, Set<Class<? extends Placeholder>> allowedTypes,
            IdentityHashMap<Object, Object> visited) {
        // Always create a new map for transformed results to simplify cycle handling
        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) rawMap;

        Map<Object, @Nullable Object> result = new LinkedHashMap<>(map.size());
        // Register in visited before transforming entries to handle self-references
        visited.put(rawMap, result);

        List<Map.Entry<Object, @Nullable Object>> mergeEntries = new ArrayList<>();

        for (Map.Entry<Object, @Nullable Object> entry : map.entrySet()) {
            Object oldKey = entry.getKey();
            Object oldVal = entry.getValue();

            Object newKey = transformInternal(oldKey, allowedTypes, visited);
            Object newVal = transformInternal(oldVal, allowedTypes, visited);

            // Dropping null keys or removal signals
            if (shouldRemoveEntry(newKey, newVal, oldVal)) {
                continue;
            }

            newKey = Objects.requireNonNull(newKey); // null keys should have been filtered out in shouldRemoveEntry

            if (newKey instanceof MergeKeyPlaceholder mkp) {
                mergeEntries.add(new AbstractMap.SimpleEntry<>(mkp, newVal));
                continue;
            }

            if ("<<".equals(newKey)) {
                mergeEntries.add(new AbstractMap.SimpleEntry<>(newKey, newVal));
                continue;
            }

            result.put(newKey, newVal);
        }

        resolveMergeKeys(result, allowedTypes, mergeEntries, visited);

        return result;
    }

    private boolean shouldRemoveEntry(@Nullable Object newKey, @Nullable Object newVal, @Nullable Object oldVal) {
        return newKey == null //
                || newKey == RemovalSignal.REMOVE //
                || newVal == RemovalSignal.REMOVE //
                || (newVal == null && oldVal != null);
    }

    public void resolveMergeKeys(Map<?, ?> rawMap) {
        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) rawMap;

        // First: handle YAML merge keys (<<: ...). We collect merge-key entries, transform their
        // values fully, merge into this container, and remove the merge-key entries before
        // performing the normal per-entry transformation below.
        List<Map.Entry<Object, @Nullable Object>> mergeEntries = new ArrayList<>();
        for (Map.Entry<Object, @Nullable Object> entry : map.entrySet()) {
            if (entry.getKey() instanceof MergeKeyPlaceholder || "<<".equals(entry.getKey())) {
                mergeEntries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        resolveMergeKeys(map, Set.of(), mergeEntries, new IdentityHashMap<>());
    }

    @SuppressWarnings({ "unchecked" })
    private void resolveMergeKeys(Map<?, ?> rawMap, Set<Class<? extends Placeholder>> allowedTypes,
            List<Map.Entry<Object, @Nullable Object>> mergeEntries, IdentityHashMap<Object, Object> visited) {

        Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) rawMap;

        if (!mergeEntries.isEmpty()) {
            for (var mergeEntry : mergeEntries) {
                @Nullable
                Object rawVal = mergeEntry.getValue();
                @Nullable
                Object transformedVal = transformInternal(rawVal, allowedTypes, visited);

                if (transformedVal instanceof Map<?, ?> fromMap) {
                    mergeMap((Map<Object, @Nullable Object>) fromMap, map);
                } else if (transformedVal instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> m) {
                            mergeMap((Map<Object, @Nullable Object>) m, map);
                        }
                    }
                } else if (transformedVal == null) {
                    if (rawVal instanceof SubstitutionPlaceholder || rawVal instanceof IfPlaceholder) {
                        // nothing to merge
                    }
                }

                map.remove(mergeEntry.getKey());
            }
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

    private Object resolveList(List<?> list, Set<Class<? extends Placeholder>> allowedTypes,
            IdentityHashMap<Object, Object> visited) {
        // Always produce a new list and register it to handle cycles
        List<@Nullable Object> result = new ArrayList<>(list.size());
        visited.put(list, result);

        for (Object oldItem : list) {
            Object newItem = transformInternal(oldItem, allowedTypes, visited);
            if (newItem != RemovalSignal.REMOVE && newItem != null) {
                result.add(newItem);
            }
        }

        return result;
    }
}
