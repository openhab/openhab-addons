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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.directives.Directive;
import org.openhab.io.yamlcomposer.internal.placeholders.DefaultPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.FreezePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.MergeKeyPlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.RemovePlaceholder;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;

/**
 * Performs structural composition while keeping target values authoritative by default.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class StructuralMerger {
    private final BufferedLogger logger;

    private record MergeEntry(MergeKeyPlaceholder key, @Nullable Object value) {
    }

    private record DeclarationEntry(Object key, @Nullable Object mergeSource) {
        public boolean isMergeKey() {
            return key instanceof MergeKeyPlaceholder;
        }
    }

    private enum ValueMode {
        TRANSFORM,
        PRESERVE
    }

    public StructuralMerger(BufferedLogger logger) {
        this.logger = logger;
    }

    /**
     * Composes a mapping by evaluating structural directives and merge keys while preserving entry values
     * untransformed.
     * <p>
     * Performs composition in the following steps and projects entries back in declaration order:
     * <ul>
     * <li>Evaluates map keys and structural directives (such as {@code !if} and {@code !for}).</li>
     * <li>Preserves entry values untransformed so value evaluation can be handled in a later phase.</li>
     * <li>Resolves shallow and deep merge directives with target-priority semantics.</li>
     * </ul>
     *
     * @param rawMap the input map to compose
     * @param result the map to populate with composed entries
     * @param transformer the recursive transformer to use for evaluating keys and directives
     * @param context the current evaluation context
     */
    public void composeMapPreserveValues(Map<?, ?> rawMap, Map<Object, @Nullable Object> result,
            RecursiveTransformer transformer, EvaluationContext context) {
        composeMap(rawMap, result, ValueMode.PRESERVE, transformer, context);
    }

    /**
     * Composes a mapping by evaluating structural directives, merge keys, and transforming entry values eagerly.
     * <p>
     * Performs composition in the following steps and projects entries back in declaration order:
     * <ul>
     * <li>Evaluates map keys and structural directives (such as {@code !if} and {@code !for}).</li>
     * <li>Transforms entry values eagerly during composition.</li>
     * <li>Resolves shallow and deep merge directives with target-priority semantics.</li>
     * </ul>
     *
     * @param rawMap the input map to compose
     * @param result the map to populate with composed entries
     * @param transformer the recursive transformer to use for evaluating entries
     * @param context the current evaluation context
     */
    public void composeMap(Map<?, ?> rawMap, Map<Object, @Nullable Object> result, RecursiveTransformer transformer,
            EvaluationContext context) {
        composeMap(rawMap, result, ValueMode.TRANSFORM, transformer, context);
    }

    private void composeMap(Map<?, ?> rawMap, Map<Object, @Nullable Object> result, ValueMode valueMode,
            RecursiveTransformer transformer, EvaluationContext context) {

        DirectiveProcessor directiveProcessor = transformer.getDirectiveProcessor();
        EvaluationContext scopeContext = context.withScope(context.scope().createChild());
        EvaluationContext mergeContext = scopeContext.withProcessingPhase(ProcessingPhase.MERGE);
        DirectiveProcessor.IfChainState ifChainState = new DirectiveProcessor.IfChainState();

        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) rawMap;
        Map<Object, @Nullable Object> resolved = new LinkedHashMap<>(map.size());
        List<MergeEntry> mergeEntries = new ArrayList<>();
        List<DeclarationEntry> declarationEntries = new ArrayList<>();

        EvaluationContext directiveContext = valueMode == ValueMode.PRESERVE
                ? scopeContext.withProcessingPhase(ProcessingPhase.DIRECTIVES)
                : scopeContext;

        // Phase 1: Evaluate keys, directives, and entry values into local scope
        for (Map.Entry<Object, @Nullable Object> rawEntry : map.entrySet()) {
            Object transformedKey = transformer.transform(rawEntry.getKey(), scopeContext);
            if (transformedKey == null) {
                continue;
            }

            switch (transformedKey) {
                case MergeKeyPlaceholder mergeKey -> {
                    ifChainState.breakChain();
                    if (mergeKey.isDeep() && !isValidDeepMergeKey(mergeKey)) {
                        warnInvalidDeepMergeKey(mergeKey);
                    } else {
                        Object value = transformer.transform(rawEntry.getValue(), mergeContext);
                        mergeEntries.add(new MergeEntry(mergeKey, value));
                        declarationEntries.add(new DeclarationEntry(mergeKey, value));
                    }
                }
                case Directive directive -> {
                    Object value = directiveProcessor.processMapDirective(directive, rawEntry.getValue(), ifChainState,
                            transformer, directiveContext);
                    addMapEntries(value, resolved, declarationEntries);
                }
                default -> {
                    ifChainState.breakChain();

                    Object entryValue = rawEntry.getValue();
                    if (valueMode == ValueMode.TRANSFORM) {
                        Object transformedValue = transformer.transform(entryValue, scopeContext);
                        if (transformedValue == null && entryValue != null) {
                            continue;
                        }
                        entryValue = transformedValue;
                    }

                    resolved.put(transformedKey, entryValue);
                    declarationEntries.add(new DeclarationEntry(transformedKey, null));
                }
            }
        }

        // Phase 2: Apply shallow and deep merge entries onto resolved map, so that
        // the entire target values, including the result of all directives,
        // even if they appear after the merge keys, take precedence over source values.
        for (MergeEntry mergeEntry : mergeEntries) {
            if (mergeEntry.key().isDeep()) {
                resolveDeepMerge(mergeEntry, resolved);
            } else {
                resolveShallowMerge(mergeEntry, resolved);
            }
        }

        // Phase 3: Project resolved entries back in original declaration order.
        projectInDeclarationOrder(declarationEntries, resolved, result);
    }

    /**
     * Projects resolved entries into declaration order matching original merge key and directive locations.
     * <p>
     * Note: {@code sourceKeys(declarationEntry.mergeSource())} is inspected solely as an ordering
     * template to determine where merged keys should sit in the sequence. Values placed into
     * {@code result} are retrieved directly from {@code resolved}, which were already merged and
     * authoritative during Phase 2.
     */
    private void projectInDeclarationOrder(List<DeclarationEntry> declarationEntries,
            Map<Object, @Nullable Object> resolved, Map<Object, @Nullable Object> result) {
        result.clear();
        Set<Object> processedKeys = new LinkedHashSet<>();

        for (DeclarationEntry declarationEntry : declarationEntries) {
            if (declarationEntry.isMergeKey()) {
                // Inspect key names from the merge source to insert them at the exact << location.
                // Values are pulled from 'resolved' without re-merging or re-evaluating.
                for (Object sourceKey : sourceKeys(declarationEntry.mergeSource())) {
                    if (resolved.containsKey(sourceKey) && processedKeys.add(sourceKey)) {
                        result.put(sourceKey, resolved.get(sourceKey));
                    }
                }
            } else {
                Object key = declarationEntry.key();
                if (resolved.containsKey(key) && processedKeys.add(key)) {
                    result.put(key, resolved.get(key));
                }
            }
        }

        // Safety fallback: append any residual entries if an external transformation bypassed declaration entries.
        for (Map.Entry<Object, @Nullable Object> resolvedEntry : resolved.entrySet()) {
            Object resolvedKey = resolvedEntry.getKey();
            if (!processedKeys.contains(resolvedKey)) {
                result.put(resolvedKey, resolvedEntry.getValue());
            }
        }
    }

    private void addMapEntries(@Nullable Object source, Map<Object, @Nullable Object> target,
            List<DeclarationEntry> declarationEntries) {
        if (source instanceof Map<?, ?> sourceMap) {
            sourceMap.forEach((sourceKey, sourceValue) -> {
                if (sourceKey != null) {
                    target.putIfAbsent(sourceKey, sourceValue);
                    declarationEntries.add(new DeclarationEntry(sourceKey, null));
                }
            });
        } else if (source instanceof List<?> sourceList) {
            sourceList.forEach(item -> addMapEntries(item, target, declarationEntries));
        }
    }

    private Set<Object> sourceKeys(@Nullable Object source) {
        Set<Object> keys = new LinkedHashSet<>();
        if (source instanceof Map<?, ?> map) {
            map.keySet().forEach(rawKey -> {
                Object normalizedKey = unwrapKey(rawKey);
                if (normalizedKey != null) {
                    keys.add(normalizedKey);
                }
            });
        } else if (source instanceof List<?> list) {
            list.forEach(item -> keys.addAll(sourceKeys(item)));
        }
        return keys;
    }

    /** Applies standard YAML's shallow, first-wins merge-key operation. */
    private void resolveShallowMerge(MergeEntry mergeEntry, Map<?, ?> target) {
        Object source = mergeEntry.value();
        if (source == null) {
            return;
        }
        String location = mergeEntry.key().sourceLocation();
        if (source instanceof Map<?, ?> sourceMap) {
            shallowMerge(sourceMap, target);
        } else if (source instanceof List<?> sourceList) {
            for (Object sourceElement : sourceList) {
                if (sourceElement instanceof Map<?, ?> sourceMap) {
                    shallowMerge(sourceMap, target);
                } else {
                    logger.warn("{} Invalid merge key sequence element; expected a mapping.", location);
                }
            }
        } else {
            logger.warn("{} Invalid merge key value; expected a mapping or sequence of mappings.", location);
        }
    }

    private void shallowMerge(Map<?, ?> source, Map<?, ?> target) {
        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> mutableTarget = (Map<Object, @Nullable Object>) target;
        for (Map.Entry<?, ?> sourceEntry : source.entrySet()) {
            Object sourceKey = unwrapKey(sourceEntry.getKey());
            if (sourceKey != null && !mutableTarget.containsKey(sourceKey)) {
                mutableTarget.put(sourceKey, copySource(sourceEntry.getValue()));
            }
        }
    }

    /** Resolves and applies merge entry placeholders in a mapping context. */
    private void resolveDeepMerge(MergeEntry mergeEntry, Map<Object, @Nullable Object> target) {
        MergeKeyPlaceholder placeholder = mergeEntry.key();
        Object source = mergeEntry.value();
        if (source instanceof Map<?, ?> sourceMap) {
            deepMerge(sourceMap, target);
        } else if (source instanceof List<?> sourceList) {
            for (Object sourceElement : sourceList) {
                if (sourceElement instanceof Map<?, ?> sourceMap) {
                    deepMerge(sourceMap, target);
                } else {
                    logger.warn("{} Invalid !deep sequence element; expected a mapping but got {}.",
                            placeholder.sourceLocation(),
                            sourceElement == null ? "null" : sourceElement.getClass().getSimpleName());
                }
            }
        } else if (source != null) {
            logger.warn("{} Invalid !deep value; expected a mapping or sequence of mappings, got {}.",
                    placeholder.sourceLocation(), source.getClass().getSimpleName());
        }
    }

    /** Deep-merges a source map into a target map. */
    void deepMerge(Map<?, ?> source, Map<?, ?> target) {
        @SuppressWarnings("unchecked")
        Map<Object, @Nullable Object> mutableTarget = (Map<Object, @Nullable Object>) target;
        normalizeKeys(mutableTarget);

        for (Map.Entry<?, ?> sourceEntry : source.entrySet()) {
            Object rawSourceKey = sourceEntry.getKey();
            if (rawSourceKey == null) {
                continue;
            }
            Object sourceKey = unwrapKey(rawSourceKey);
            if (sourceKey == null) {
                continue;
            }

            Object sourceValue = sourceEntry.getValue();
            if (!mutableTarget.containsKey(sourceKey)) {
                mutableTarget.put(sourceKey, copySource(sourceValue));
                continue;
            }

            Object targetValue = mutableTarget.get(sourceKey);
            if (targetValue instanceof RemovePlaceholder || targetValue instanceof FreezePlaceholder) {
                // Keep RemovePlaceholder in mutableTarget so subsequent !deep passes skip sourceKey
                continue;
            }

            Object incoming = copySource(sourceValue);
            if (targetValue instanceof DefaultPlaceholder defaultPlaceholder) {
                Object defaultValue = defaultPlaceholder.value();
                if (defaultValue instanceof Map<?, ?> defaultMap && incoming instanceof Map<?, ?> incomingMap) {
                    Map<Object, @Nullable Object> merged = copyMap(defaultMap);
                    mergeWithSourcePriority(incomingMap, merged);
                    mutableTarget.put(sourceKey, merged);
                } else {
                    mutableTarget.put(sourceKey, deduplicateList(incoming));
                }
                continue;
            }

            if (targetValue == null) {
                mutableTarget.put(sourceKey, incoming);
            } else if (targetValue instanceof Map<?, ?> existingMap && incoming instanceof Map<?, ?> incomingMap) {
                Map<Object, @Nullable Object> merged = new LinkedHashMap<>();
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> typedExisting = (Map<Object, @Nullable Object>) existingMap;
                merged.putAll(typedExisting);
                deepMerge(incomingMap, merged);
                mutableTarget.put(sourceKey, merged);
            } else if (targetValue instanceof List<?> existingList && incoming instanceof List<?> incomingList) {
                mutableTarget.put(sourceKey, mergeLists(existingList, incomingList));
            } else if (!sameType(targetValue, incoming)) {
                logger.warn("Type mismatch while structurally merging key '{}'; keeping target value", sourceKey);
            }
        }
    }

    private void normalizeKeys(Map<Object, @Nullable Object> map) {
        Map<Object, @Nullable Object> normalized = new LinkedHashMap<>(map.size());
        map.forEach((rawKey, value) -> {
            Object normalizedKey = rawKey instanceof Directive ? rawKey : unwrapKey(rawKey);
            if (normalizedKey != null) {
                normalized.put(normalizedKey, value);
            }
        });
        map.clear();
        map.putAll(normalized);
    }

    private boolean isValidDeepMergeKey(MergeKeyPlaceholder key) {
        return "<<".equals(key.value());
    }

    private void warnInvalidDeepMergeKey(MergeKeyPlaceholder key) {
        logger.warn("{} !deep: Invalid key '{}'. Use '!deep <<:' instead. Deep merging not performed.",
                key.sourceLocation(), key.value());
    }

    /**
     * Unwraps deferred placeholder wrappers (e.g., {@link SubstitutionPlaceholder}) on map keys
     * to extract the raw underlying key object for equality matching and map storage.
     *
     * @param key the potentially wrapped map key
     * @return the unwrapped raw key object, or the original key if no unwrapping is required
     */
    private @Nullable Object unwrapKey(@Nullable Object key) {
        if (key instanceof SubstitutionPlaceholder substitution) {
            return substitution.value();
        }
        return key;
    }

    private Map<Object, @Nullable Object> copyMap(Map<?, ?> map) {
        Map<Object, @Nullable Object> copy = new LinkedHashMap<>(map.size());
        for (Map.Entry<?, ?> sourceEntry : map.entrySet()) {
            Object sourceKey = unwrapKey(sourceEntry.getKey());
            if (sourceKey != null) {
                copy.put(sourceKey, copySource(sourceEntry.getValue()));
            }
        }
        return copy;
    }

    public @Nullable Object copySource(@Nullable Object source) {
        return copySource(source, new IdentityHashMap<>());
    }

    private @Nullable Object copySource(@Nullable Object source, Map<Object, Object> visited) {
        if (source == null) {
            return null;
        }

        Object cachedCopy = visited.get(source);
        if (cachedCopy != null) {
            return cachedCopy;
        }

        if (source instanceof Map<?, ?> map) {
            Map<Object, @Nullable Object> copy = new LinkedHashMap<>(map.size());
            visited.put(source, copy);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (key != null) {
                    Object keyCopy = copySource(key, visited);
                    if (keyCopy != null) {
                        copy.put(keyCopy, copySource(entry.getValue(), visited));
                    }
                }
            }
            return copy;
        }

        if (source instanceof List<?> list) {
            List<@Nullable Object> copy = new ArrayList<>(list.size());
            visited.put(source, copy);
            for (Object item : list) {
                copy.add(copySource(item, visited));
            }
            return copy;
        }

        return source;
    }

    private List<@Nullable Object> mergeLists(List<?> targetList, List<?> sourceList) {
        LinkedHashSet<@Nullable Object> merged = new LinkedHashSet<>();
        boolean sourceHasValues = !sourceList.isEmpty();
        Set<Object> toRemove = new HashSet<>();

        for (Object item : targetList) {
            if (item instanceof DefaultPlaceholder defaultPlaceholder) {
                if (!sourceHasValues) {
                    // add back the defaultPlaceholder so subsequent merges can still see it if the source list is empty
                    merged.add(defaultPlaceholder);
                }
            } else if (item instanceof RemovePlaceholder removePlaceholder) {
                Object toRemoveItem = removePlaceholder.value();
                if (toRemoveItem != null) {
                    toRemove.add(toRemoveItem);
                    // add back the removePlaceholder so to prevent subsequent merges from re-adding the removed item
                    merged.add(removePlaceholder);
                }
            } else {
                merged.add(item);
            }
        }

        for (Object item : sourceList) {
            merged.add(item);
        }

        for (Object item : toRemove) {
            merged.remove(item);
        }

        return new ArrayList<>(merged);
    }

    private void mergeWithSourcePriority(Map<?, ?> source, Map<Object, @Nullable Object> target) {
        for (Map.Entry<?, ?> sourceEntry : source.entrySet()) {
            Object rawSourceKey = sourceEntry.getKey();
            Object sourceKey = unwrapKey(rawSourceKey);
            if (sourceKey == null) {
                continue;
            }

            Object sourceValue = copySource(sourceEntry.getValue());
            Object targetValue = target.get(sourceKey);

            if (targetValue instanceof Map<?, ?> targetMap && sourceValue instanceof Map<?, ?> sourceMap) {
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> nestedTarget = (Map<Object, @Nullable Object>) targetMap;
                mergeWithSourcePriority(sourceMap, nestedTarget);
            } else {
                target.put(sourceKey, sourceValue);
            }
        }
    }

    private @Nullable Object deduplicateList(@Nullable Object value) {
        return value instanceof List<?> list ? mergeLists(List.of(), list) : value;
    }

    private boolean sameType(@Nullable Object first, @Nullable Object second) {
        return first == null || second == null || first.getClass().equals(second.getClass());
    }
}
