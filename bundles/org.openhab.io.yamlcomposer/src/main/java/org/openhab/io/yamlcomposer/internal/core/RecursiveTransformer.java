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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.placeholders.InterpolablePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;
import org.openhab.io.yamlcomposer.internal.processors.PlaceholderProcessor;

/**
 * The {@link RecursiveTransformer} traverses a YAML data tree, applies merge keys logic,
 * and transforms placeholders into the final values by invoking registered handlers
 * for their respective types.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class RecursiveTransformer {

    private final Map<Class<? extends Placeholder>, PlaceholderProcessor<?>> handlers = new LinkedHashMap<>();
    private final DirectiveProcessor directiveProcessor;
    private final StructuralMerger structuralMerger;
    private final Path absolutePath;

    public RecursiveTransformer(Consumer<String> envVarCallback, Path absolutePath, BufferedLogger logger) {
        this.absolutePath = absolutePath;
        this.directiveProcessor = new DirectiveProcessor(logger, envVarCallback);
        this.structuralMerger = new StructuralMerger(logger);
    }

    StructuralMerger getStructuralMerger() {
        return structuralMerger;
    }

    DirectiveProcessor getDirectiveProcessor() {
        return directiveProcessor;
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Registers a handler for a specific placeholder type.
     *
     * @param handler the processor that can transform the placeholder
     */
    public void register(PlaceholderProcessor<?> handler) {
        handlers.put(handler.getPlaceholderType(), handler);
    }

    /**
     * Transform any object, including scalars, maps, and lists, using the supplied evaluation context.
     *
     * @param data the object to transform
     * @param context the evaluation context
     * @return the transformed object
     */
    public @Nullable Object transform(@Nullable Object data, EvaluationContext context) {
        return transformInternal(data, context);
    }

    /**
     * Transforms the given map using the supplied evaluation context.
     * <p>
     * Keys and values are transformed recursively. If the overall transformed result is not a
     * {@link Map} (for example, if a placeholder handler returned a non-container), an
     * {@link IllegalStateException} is thrown.
     *
     * @param data the map to transform
     * @param context the evaluation context
     * @return the transformed map
     * @throws IllegalStateException if the transformed result is not a Map
     */
    @SuppressWarnings("unchecked")
    public Map<Object, @Nullable Object> transform(Map<?, ?> data, EvaluationContext context) {
        Object transformed = transform((Object) data, context);
        if (transformed instanceof Map<?, ?> map) {
            return (Map<Object, @Nullable Object>) map;
        }
        throw new IllegalStateException("Expected transformed result to be a Map but was: "
                + (transformed == null ? "null" : transformed.getClass()));
    }

    /**
     * The actual recursive tree traversal and directive evaluation engine.
     */
    private @Nullable Object transformInternal(@Nullable Object data, EvaluationContext context) {

        if (data == null) {
            return null;
        }

        // Handle cyclic references for containers: if we've already started transforming
        // this container, return the placeholder/result to avoid infinite recursion.
        if ((data instanceof Map<?, ?> || data instanceof List<?>)) {
            if (context.visited().containsKey(data)) {
                return context.visited().get(data);
            }
        }

        // Resolve placeholder value (arguments) first before transforming the placeholder itself
        // So that e.g. !include ${filename} gets the real argument value to transform
        if (data instanceof InterpolablePlaceholder interpolable) {
            ProcessingPhase valuePhase = interpolable.eagerArgumentProcessing() ? ProcessingPhase.ALL
                    : ProcessingPhase.SUBSTITUTION;
            Object transformedValue = transformInternal(interpolable.value(), context.withProcessingPhase(valuePhase));
            data = interpolable.withValue(transformedValue);
        }

        if (data instanceof Placeholder placeholder && context.activePhase().includes(placeholder)) {
            Object result = processPlaceholder(placeholder, context);
            return transformInternal(result, context);
        }

        if (data instanceof Map<?, ?> map) {
            return resolveMap(map, context);
        }

        if (data instanceof List<?> list) {
            return resolveList(list, context);
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    private @Nullable Object processPlaceholder(Placeholder placeholder, EvaluationContext context) {
        PlaceholderProcessor<Placeholder> handler = (PlaceholderProcessor<Placeholder>) handlers
                .get(placeholder.getClass());

        if (handler == null) {
            return null;
        }
        return handler.process(placeholder, this, context);
    }

    /**
     * Resolves a map by transforming its keys and values, applying placeholder handlers as needed,
     * and handling special cases like merge keys, structural directives, and removal signals.
     *
     * @param rawMap the original map to transform
     * @param allowedTypes the set of placeholder classes to transform
     * @return the transformed map with placeholders transformed, or the original map if no changes were made
     */
    private Object resolveMap(Map<?, ?> rawMap, EvaluationContext context) {
        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) rawMap;
        Map<Object, @Nullable Object> result = new LinkedHashMap<>(map.size());
        // Register in visited before transforming entries to handle self-references
        context.visited().put(rawMap, result);
        structuralMerger.composeMap(map, result, this, context);
        return result;
    }

    private Object resolveList(List<?> list, EvaluationContext context) {
        List<@Nullable Object> result = new ArrayList<>(list.size());
        context.visited().put(list, result);

        DirectiveProcessor.IfChainState ifChainState = new DirectiveProcessor.IfChainState();

        for (Object oldItem : list) {
            Object processedItem = oldItem;
            boolean isDirectiveMap = false;
            boolean handled = false;

            if (oldItem instanceof Map<?, ?> map) {
                Object directiveResult = directiveProcessor.processListMap(map, ifChainState, this, context);
                if (directiveResult != null) {
                    processedItem = directiveResult;
                    isDirectiveMap = true;
                    handled = true;
                }
            }

            // Standard scalars, plain maps, or maps without control directives
            if (!handled) {
                ifChainState.breakChain();
                processedItem = transformInternal(oldItem, context);
            }

            if (processedItem == null) {
                continue;
            }

            if (processedItem instanceof List<?> unrolledList) {
                // If it's a directive map (like key-level !if returning a list), flatten it inline.
                // Otherwise, preserve it as a nested list node.
                if (isDirectiveMap) {
                    for (Object item : unrolledList) {
                        if (item != null) {
                            result.add(item);
                        }
                    }
                } else {
                    result.add(unrolledList);
                }
            } else {
                result.add(processedItem);
            }
        }

        return result;
    }
}
