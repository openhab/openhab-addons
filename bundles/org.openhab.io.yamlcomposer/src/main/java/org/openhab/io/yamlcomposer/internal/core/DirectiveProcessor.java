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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.StringInterpolator;
import org.openhab.io.yamlcomposer.internal.directives.ControlFlowDirective;
import org.openhab.io.yamlcomposer.internal.directives.Directive;
import org.openhab.io.yamlcomposer.internal.directives.ElseDirective;
import org.openhab.io.yamlcomposer.internal.directives.ElseIfDirective;
import org.openhab.io.yamlcomposer.internal.directives.ForDirective;
import org.openhab.io.yamlcomposer.internal.directives.IfDirective;
import org.openhab.io.yamlcomposer.internal.directives.VarDirective;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;

/**
 * Handles structural execution and container unrolling for {@link Directive} instances.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class DirectiveProcessor {

    private final BufferedLogger logger;
    private final Consumer<String> envVarCallback;

    public static class IfChainState {
        boolean matched = false;
        boolean active = false;

        public void startChain() {
            active = true;
            matched = false;
        }

        public void breakChain() {
            active = false;
            matched = false;
        }
    }

    public DirectiveProcessor(BufferedLogger logger, Consumer<String> envVarCallback) {
        this.logger = logger;
        this.envVarCallback = envVarCallback;
    }

    /**
     * Executes a directive within a Map context, returning the resulting object to be merged or processed.
     */
    public @Nullable Object processMapDirective(Directive directive, @Nullable Object oldVal,
            Map<Object, @Nullable Object> targetMap, IfChainState ifChainState, RecursiveTransformer transformer,
            Set<Class<? extends Placeholder>> allowedTypes, IdentityHashMap<Object, Object> visited) {
        switch (directive) {
            case IfDirective ifDirective -> {
                ifChainState.startChain();
                if (ifDirective.truthy()) {
                    ifChainState.matched = true;
                    return transformer.transform(oldVal, allowedTypes, visited);
                }
            }
            case ElseIfDirective elseIfDirective -> {
                if (!ifChainState.active) {
                    logger.warn("{} {} without preceding !if.", elseIfDirective.sourceLocation(),
                            elseIfDirective.tag());
                    return null;
                }
                if (ifChainState.matched) {
                    return null; // A previous branch in this chain already matched
                }
                if (elseIfDirective.truthy()) {
                    ifChainState.matched = true;
                    return transformer.transform(oldVal, allowedTypes, visited);
                }
            }
            case ElseDirective elseDirective -> {
                if (!ifChainState.active) {
                    logger.warn("{} !else without preceding !if.", elseDirective.sourceLocation());
                    return null;
                }
                boolean wasMatched = ifChainState.matched;
                ifChainState.breakChain(); // Terminate chain
                if (wasMatched) {
                    return null; // A previous branch in this chain already matched
                }
                return transformer.transform(oldVal, allowedTypes, visited);
            }
            case ForDirective forDirective -> {
                ifChainState.breakChain(); // Intervening loop breaks any active if-chain
                List<@Nullable Object> loopResults = new ArrayList<>();
                processForDirective(forDirective, oldVal, transformer, (loopVars, rawBlock) -> {
                    RecursiveTransformer loopTransformer = transformer.withOverrideVariables(loopVars);
                    IdentityHashMap<Object, Object> iterationVisited = new IdentityHashMap<>(visited);

                    Object newVal = loopTransformer.transform(rawBlock, allowedTypes, iterationVisited);
                    loopResults.add(newVal);
                });
                return loopResults;
            }
            case VarDirective varDirective -> {
                ifChainState.breakChain(); // Intervening variable declaration breaks any active if-chain
                processVarDirective(varDirective, oldVal, transformer, allowedTypes, visited);
            }
        }
        return null;
    }

    /**
     * Processes a Map item inside a List context. If the Map contains control directives,
     * it is evaluated and this method returns the resulting object/list.
     * Otherwise, returns {@code null} to indicate it is a plain data map.
     */
    public @Nullable Object processListMap(Map<?, ?> map, IfChainState ifChainState, RecursiveTransformer transformer,
            Set<Class<? extends Placeholder>> allowedTypes, IdentityHashMap<Object, Object> visited) {
        boolean hasControlDirective = false;
        for (Object k : map.keySet()) {
            Object transformedKey = transformer.transform(k, allowedTypes, visited);
            if (transformedKey instanceof ControlFlowDirective) {
                hasControlDirective = true;
                break;
            }
        }

        if (!hasControlDirective) {
            return null;
        }

        List<@Nullable Object> listResults = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object newKey = transformer.transform(entry.getKey(), allowedTypes, visited);

            if (newKey instanceof Directive directive) {
                Object directiveResult = processListDirective(directive, entry.getValue(), ifChainState, transformer,
                        allowedTypes, visited);
                if (directiveResult != null) {
                    if (directiveResult instanceof List<?> listVal) {
                        listResults.addAll(listVal);
                    } else {
                        listResults.add(directiveResult);
                    }
                }
            } else {
                ifChainState.breakChain();
                Object newVal = transformer.transform(entry.getValue(), allowedTypes, visited);
                if (newVal != RemovalSignal.REMOVE && newVal != null && newKey != null) {
                    listResults.add(Map.of(Objects.requireNonNull(newKey), newVal));
                }
            }
        }
        return listResults;
    }

    /**
     * Executes a directive within a List context, returning the resulting object.
     */
    public @Nullable Object processListDirective(Directive directive, @Nullable Object oldVal,
            IfChainState ifChainState, RecursiveTransformer transformer, Set<Class<? extends Placeholder>> allowedTypes,
            IdentityHashMap<Object, Object> visited) {
        switch (directive) {
            case IfDirective ifDirective -> {
                ifChainState.startChain();
                if (ifDirective.truthy()) {
                    ifChainState.matched = true;
                    return transformer.transform(oldVal, allowedTypes, visited);
                }
            }
            case ElseIfDirective elseIfDirective -> {
                if (!ifChainState.active) {
                    logger.warn("{} {} without preceding !if.", elseIfDirective.sourceLocation(),
                            elseIfDirective.tag());
                    return null;
                }
                if (ifChainState.matched) {
                    return null;
                }
                if (elseIfDirective.truthy()) {
                    ifChainState.matched = true;
                    return transformer.transform(oldVal, allowedTypes, visited);
                }
            }
            case ElseDirective elseDirective -> {
                if (!ifChainState.active) {
                    logger.warn("{} !else without preceding !if.", elseDirective.sourceLocation());
                    return null;
                }
                boolean wasMatched = ifChainState.matched;
                ifChainState.breakChain();
                if (wasMatched) {
                    return null;
                }
                return transformer.transform(oldVal, allowedTypes, visited);
            }
            case ForDirective forDirective -> {
                ifChainState.breakChain();
                List<@Nullable Object> loopResults = new ArrayList<>();
                processForDirective(forDirective, oldVal, transformer, (loopVars, rawBlock) -> {
                    RecursiveTransformer loopTransformer = transformer.withOverrideVariables(loopVars);
                    IdentityHashMap<Object, Object> iterationVisited = new IdentityHashMap<>(visited);

                    Object newVal = loopTransformer.transform(rawBlock, allowedTypes, iterationVisited);
                    if (newVal != null && newVal != RemovalSignal.REMOVE) {
                        if (newVal instanceof List<?> listVal) {
                            loopResults.addAll(listVal);
                        } else {
                            loopResults.add(newVal);
                        }
                    }
                });
                return loopResults;
            }
            case VarDirective varDirective -> {
                ifChainState.breakChain();
                processVarDirective(varDirective, oldVal, transformer, allowedTypes, visited);
            }
        }
        return null;
    }

    public void processVarDirective(VarDirective varDirective, @Nullable Object oldVal,
            RecursiveTransformer transformer, Set<Class<? extends Placeholder>> allowedTypes,
            IdentityHashMap<Object, Object> visited) {
        String varName = varDirective.variableName();
        if (VariableLoader.isSpecialVariable(varName)) {
            logger.warn("{} Cannot redefine special variable '{}'.", varDirective.sourceLocation(), varName);
            return;
        }

        Object resolvedVal = transformer.transform(oldVal, allowedTypes, visited);
        transformer.getVariables().put(varName, resolvedVal);
    }

    private void processForDirective(ForDirective forDirective, @Nullable Object oldVal,
            RecursiveTransformer transformer,
            BiConsumer<Map<String, @Nullable Object>, @Nullable Object> iterationConsumer) {
        List<String> variables = forDirective.variables();
        @Nullable
        Object rawTarget = forDirective.target();

        if (variables.isEmpty() || variables.stream().allMatch(String::isBlank) || rawTarget == null) {
            logger.warn("{} !for directive is missing variables or target: {}.", forDirective.sourceLocation(),
                    forDirective);
            return;
        }

        @Nullable
        Object target = rawTarget;
        if (rawTarget instanceof String targetExpr) {
            String expr = targetExpr.trim();
            Map<String, @Nullable Object> vars = transformer.getVariables();

            if (vars.containsKey(expr)) {
                target = vars.get(expr);
            } else {
                target = StringInterpolator.evaluateExpression(expr, vars, envVarCallback, logger.getLogSession(),
                        forDirective.sourceLocation());
            }
        }

        if (target == null) {
            logger.warn("{} !for target is null or undefined: {}.", forDirective.sourceLocation(), rawTarget);
            return;
        }

        Iterable<?> iterableTarget = null;
        if (target instanceof Map<?, ?> map) {
            iterableTarget = map.entrySet();
        } else if (target instanceof Iterable<?> iterable) {
            iterableTarget = iterable;
        }

        if (iterableTarget == null) {
            logger.warn("{} !for target is not iterable: {}.", forDirective.sourceLocation(), target);
            return;
        }

        for (Object item : iterableTarget) {
            Map<String, @Nullable Object> loopVars = new HashMap<>();
            List<@Nullable Object> extractedValues = new ArrayList<>();

            if (item instanceof Map.Entry<?, ?> entry) {
                extractedValues.add(entry.getKey());
                extractedValues.add(entry.getValue());
            } else if (item instanceof List<?> list) {
                extractedValues.addAll(list);
            } else {
                extractedValues.add(item);
            }

            for (int i = 0; i < variables.size(); i++) {
                String varName = variables.get(i);
                if (i < extractedValues.size()) {
                    loopVars.put(varName, extractedValues.get(i));
                } else {
                    loopVars.put(varName, null);
                }
            }

            if (shouldKeepIteration(forDirective, loopVars, transformer)) {
                iterationConsumer.accept(loopVars, oldVal);
            }
        }
    }

    private boolean shouldKeepIteration(ForDirective forDirective, Map<String, @Nullable Object> loopVars,
            RecursiveTransformer parentTransformer) {
        String filterCondition = forDirective.filterCondition();
        if (filterCondition == null || filterCondition.isBlank()) {
            return true;
        }
        RecursiveTransformer filterTransformer = parentTransformer.withOverrideVariables(loopVars);

        Object evaluated = StringInterpolator.evaluateExpression(filterCondition.trim(),
                filterTransformer.getVariables(), envVarCallback, logger.getLogSession(),
                forDirective.sourceLocation());
        return ExpressionEvaluator.isTruthy(evaluated);
    }
}
