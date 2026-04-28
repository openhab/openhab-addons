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
package org.openhab.io.yamlcomposer.internal.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.StringInterpolator;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;
import org.openhab.io.yamlcomposer.internal.placeholders.IfPlaceholder;

/**
 * Processor for resolving {@link IfPlaceholder} in YAML models.
 * Evaluates conditions and returns the appropriate branch value.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class IfProcessor implements PlaceholderProcessor<IfPlaceholder> {
    private final BufferedLogger logger;

    public IfProcessor(BufferedLogger logger) {
        this.logger = logger;
    }

    @Override
    public Class<IfPlaceholder> getPlaceholderType() {
        return IfPlaceholder.class;
    }

    private record Branch(@Nullable Object condition, @Nullable Object thenValue) {
    }

    private record Logic(List<Branch> branches, @Nullable Object elseValue) {
    }

    @Override
    public @Nullable Object process(IfPlaceholder ifPlaceholder, RecursiveTransformer recursiveTransformer) {
        Logic logic = switch (ifPlaceholder.value()) {
            case null -> null;
            case Map<?, ?> map -> parseFromMapDefinition(map, ifPlaceholder.sourceLocation());
            case List<?> list -> parseFromListDefinition(list, ifPlaceholder.sourceLocation());
            default -> null;
        };

        if (logic == null) {
            return null;
        }

        for (Branch branch : logic.branches()) {
            Object evaluated = switch (branch.condition()) {
                case null -> "false"; // Treat null condition as false
                case String s -> StringInterpolator.evaluateExpression(s, recursiveTransformer.getVariables(),
                        logger.getLogSession(), ifPlaceholder.sourceLocation());
                default -> branch.condition();
            };

            if (ExpressionEvaluator.isTruthy(evaluated)) {
                return branch.thenValue();
            }
        }
        return logic.elseValue();
    }

    private @Nullable Logic parseFromMapDefinition(Map<?, ?> map, String sourceLocation) {
        Branch mainBranch = createBranch(map, false, false, sourceLocation);
        if (mainBranch == null) {
            return null;
        }

        Object elseValue = map.get("else");

        return new Logic(List.of(mainBranch), elseValue);
    }

    private @Nullable Logic parseFromListDefinition(List<?> list, String sourceLocation) {
        List<Branch> branches = new ArrayList<>();
        @Nullable
        Object elseValue = null;

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof Map<?, ?> map)) {
                logger.warn("{} !if list item is not a mapping and will be ignored: {}.", sourceLocation, item);
                continue;
            }

            if (map.containsKey("else")) {
                elseValue = map.get("else");
                if (map.containsKey("if") || map.containsKey("elseif") || map.containsKey("then")) {
                    logger.warn("{} !if sequence item at index {} should only contain 'else'. Other keys ignored.",
                            sourceLocation, i);
                }
                if (i < list.size() - 1) {
                    logger.warn("{} !if list has unreachable branches after the 'else' block.", sourceLocation);
                }
                break; // Else stops evaluation of further branches
            }

            Branch branch = createBranch(map, i > 0, true, sourceLocation);
            if (branch != null) {
                branches.add(branch);
            }
        }

        if (branches.isEmpty()) {
            logger.warn("{} !if sequence has no valid branches.", sourceLocation);
        }

        return new Logic(branches, elseValue);
    }

    /**
     * Creates a Branch from the given map.
     *
     * @param map the map representing the branch
     * @param isElseIf whether this branch is an "elseif" branch
     * @param isList whether this is for a list (non-root) branch
     * @return the created Branch, or null if creation failed
     */
    private @Nullable Branch createBranch(Map<?, ?> map, boolean isElseIf, boolean isList, String sourceLocation) {
        String key = isElseIf ? "elseif" : "if";
        boolean hasConditionKey = map.containsKey(key);
        boolean hasValueKey = map.containsKey("then");
        Object rawCondition = map.get(key); // Will be null if missing OR explicitly null
        Object rawValue = map.get("then");

        if (rawCondition == null) {
            rawCondition = "false"; // Treat explicit null as false
        }

        // 1. Guard: Check existence and nullability
        // Condition: must exist
        // Value: must exist (can be null)
        if (!hasValueKey || !hasConditionKey) {
            String prefix = isList ? "branch " : "";
            String suffix = isList ? "Ignoring branch." : "Returning null.";

            String reason;
            if (!hasConditionKey && !hasValueKey) {
                reason = "is empty (missing both '" + key + "' and 'then' fields)";
            } else if (!hasConditionKey) {
                reason = "is missing '" + key + "' field";
            } else {
                reason = "is missing 'then' field";
            }

            logger.warn("{} !if {}{}. {}", sourceLocation, prefix, reason, suffix);
            return null;
        }

        // 2. Success: Value exists (even if null), Condition exists and is non-null
        return new Branch(rawCondition, rawValue);
    }
}
