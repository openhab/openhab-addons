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
package org.openhab.io.yamlcomposer.internal.expression;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.LogSession;
import org.openhab.io.yamlcomposer.internal.expression.filters.DigFilter;
import org.openhab.io.yamlcomposer.internal.expression.filters.LabelFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.util.ObjectTruthValue;

/**
 * Wrapper around Jinjava template engine for rendering ${...} expressions.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ExpressionEvaluator {

    private static final JinjavaConfig CONFIG;
    private static final Jinjava JINJAVA;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);

    static {
        @SuppressWarnings("null")
        JinjavaConfig config = JinjavaConfig.newBuilder() //
                .withFeatureConfig(FeatureConfig.newBuilder()
                        .add(JinjavaInterpreter.OUTPUT_UNDEFINED_VARIABLES_ERROR, FeatureStrategies.ACTIVE).build())
                .withFailOnUnknownTokens(false) //
                .withMaxRenderDepth(1) //
                .withMaxMacroRecursionDepth(0) // We don't use macros, disable recursion limit
                .withEnableRecursiveMacroCalls(false) //
                .build();
        CONFIG = config;
        JINJAVA = new Jinjava(CONFIG);
        Context context = JINJAVA.getGlobalContext();
        context.registerFilter(new LabelFilter());
        context.registerFilter(new DigFilter());
    }

    /**
     * Evaluate a Jinjava expression and return the raw object result (no string coercion).
     *
     * @param expression the expression content without delimiters (e.g., "user.profile")
     * @param variables the variable context
     * @return the evaluated object in its native type
     */
    public static @Nullable Object renderObject(String expression, Map<String, @Nullable Object> variables,
            LogSession logSession, String sourceLocation) {
        @SuppressWarnings("null")
        Context context = new Context(JINJAVA.getGlobalContext(), variables);
        context.setDynamicVariableResolver(varName -> dynamicVariableResolver(varName, variables));
        JinjavaInterpreter interpreter = new JinjavaInterpreter(JINJAVA, context, CONFIG);

        Object result = interpreter.resolveELExpression(expression, 0);

        @SuppressWarnings("null")
        List<TemplateError> errors = interpreter.getErrorsCopy();
        if (!errors.isEmpty()) {
            List<String> messages = errors.stream().map((e) -> {
                String msg = Objects.requireNonNullElse(e.getMessage(), "");
                if (e.getItem() == TemplateError.ErrorItem.TOKEN) {
                    String missingVarName = extractVariableName(e);
                    if (missingVarName != null && variables.containsKey(missingVarName)) {
                        // Jinjava emits a TOKEN error when a referenced variable resolves to null.
                        // If the variable key exists in our variables map (even if its value is null),
                        // treat it as intentionally defined and suppress an "undefined variable" suggestion.
                        return null;
                    }
                    String suggestion = findClosestVariableName(missingVarName, variables);
                    if (suggestion != null) {
                        msg += " (Did you mean '" + suggestion + "'?)";
                    }
                }
                return msg;
            }).filter(Objects::nonNull).toList();

            if (!messages.isEmpty()) {
                String combinedMessage = (messages.size() == 1) //
                        ? " " + messages.getFirst() //
                        : "\n- " + String.join("\n- ", messages);

                String logMsg = String.format("%s Error evaluating expression '%s':%s", sourceLocation, expression,
                        combinedMessage);

                logSession.trackWarning(LOGGER, logMsg);
            }
        }
        return normalizeType(result);
    }

    /**
     * Determines the truthiness of a value according to Jinjava rules.
     *
     * @param value the value to evaluate
     * @return true if the value is considered truthy, false otherwise
     */
    public static boolean isTruthy(@Nullable Object value) {
        return ObjectTruthValue.evaluate(value);
    }

    /**
     * Dynamic variable resolver for Jinjava to provide special variables like "VARS" and "ENV".
     *
     * @param varName the name of the variable being resolved
     * @param context the current variable context
     * @return the value of the special variable, or null if it's not a special variable
     */
    private static @Nullable Object dynamicVariableResolver(@Nullable String varName,
            Map<String, @Nullable Object> context) {
        if ("VARS".equals(varName)) {
            return context;
        }

        if ("ENV".equals(varName)) {
            return System.getenv();
        }
        return null;
    }

    /**
     * Normalize types returned by Jinjava to match SnakeYAML's
     *
     * Jinjava tends to return Long for all integer numbers.
     * SnakeYAML, however, uses Integer for small integers.
     * Normalize accordingly to avoid type mismatches.
     *
     * Example:
     *
     * ```yaml
     * native: 1 # returns a Java Integer
     * jinja: ${ 1 } # before normalization returns a Java Long
     * ```
     *
     * @param obj the object to normalize
     * @return the normalized object
     */
    private static @Nullable Object normalizeType(@Nullable Object obj) {
        if (obj instanceof Long longValue) {
            // Check if the value fits in an Integer
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                return Math.toIntExact(longValue);
            }
        }
        return obj;
    }

    private static @Nullable String extractVariableName(TemplateError error) {
        @SuppressWarnings("null")
        Map<String, String> categoryErrors = error.getCategoryErrors();
        String variableName = null;
        if (categoryErrors != null && categoryErrors.containsKey("variable")) {
            variableName = categoryErrors.get("variable");
        }

        if (variableName == null) {
            variableName = error.getMessage().replaceAll(".*'([^']+)'.*", "$1");
        }
        return variableName;
    }

    /**
     * Finds the most similar variable name using a standalone Levenshtein implementation.
     */
    private static @Nullable String findClosestVariableName(@Nullable String missingVar,
            Map<String, @Nullable Object> variables) {
        if (missingVar == null) {
            return null;
        }
        String bestMatch = null;
        int maxDistance = 3; // Threshold: only suggest if 1 or 2 edits away

        for (String existingVar : variables.keySet()) {
            int distance = levenshteinDistance(missingVar, existingVar, maxDistance);
            if (distance < maxDistance) {
                maxDistance = distance;
                bestMatch = existingVar;
            }
        }
        return bestMatch;
    }

    /**
     * Calculate the Levenshtein distance between two strings with an early exit if the distance exceeds the threshold.
     *
     * @param s1 The first string
     * @param s2 The second string
     * @param threshold The maximum distance to calculate before exiting early
     * @return the Levenshtein distance, or Integer.MAX_VALUE if the distance exceeds the threshold
     */
    private static int levenshteinDistance(@Nullable String s1, @Nullable String s2, int threshold) {
        if (s1 == null || s2 == null) {
            return Integer.MAX_VALUE;
        }

        int len1 = s1.length();
        int len2 = s2.length();

        // If length difference is greater than our threshold, skip expensive math
        if (Math.abs(len1 - len2) >= threshold)
            return Integer.MAX_VALUE;

        int[] costs = new int[len2 + 1];
        for (int i = 0; i <= len1; i++) {
            int lastValue = i;
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0)
                costs[len2] = lastValue;
        }
        return costs[len2];
    }
}
