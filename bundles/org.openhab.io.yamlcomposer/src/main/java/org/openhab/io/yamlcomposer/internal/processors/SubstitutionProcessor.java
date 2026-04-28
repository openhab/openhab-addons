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

import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.StringInterpolator;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.placeholders.SubstitutionPlaceholder;

/**
 * Processor for handling variable substitutions by resolving {@code SubstitutionPlaceholder} in YAML models
 * into interpolated strings.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class SubstitutionProcessor implements PlaceholderProcessor<SubstitutionPlaceholder> {

    private final BufferedLogger logger;

    public SubstitutionProcessor(BufferedLogger logger) {
        this.logger = logger;
    }

    public Class<SubstitutionPlaceholder> getPlaceholderType() {
        return SubstitutionPlaceholder.class;
    }

    /**
     * Recursively resolve all SubstitutionPlaceholders in a value (map, list, or scalar).
     * using the RecursiveTransformer's variables as the substitution context.
     *
     * @param value The value to process
     * @return The processed value with substitutions applied
     */
    @Override
    public @Nullable Object process(SubstitutionPlaceholder placeholder, RecursiveTransformer recursiveTransformer) {
        return process(placeholder, recursiveTransformer.getVariables());
    }

    /**
     * Recursively resolve all SubstitutionPlaceholders in a value (map, list, or scalar)
     * using the provided context for substitutions.
     *
     * This overload is needed by {@link InsertProcessor} to apply substitutions
     * with a custom variable context when processing templates.
     *
     * @param value The value to process
     * @param context The variable context for substitutions
     * @return The processed value with substitutions applied
     */
    public @Nullable Object process(SubstitutionPlaceholder placeholder, Map<String, @Nullable Object> context) {
        Pattern pattern = resolvePattern(placeholder, context);
        return StringInterpolator.interpolate(placeholder.value(), pattern, context, logger.getLogSession(),
                placeholder.sourceLocation());
    }

    private Pattern resolvePattern(SubstitutionPlaceholder placeholder, Map<String, @Nullable Object> context) {
        String patternName = placeholder.patternName();
        if (patternName == null || patternName.isBlank()) {
            return StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN;
        }

        Object rawPatternSpec = context.get(patternName);
        if (!(rawPatternSpec instanceof String patternSpec) || patternSpec.isBlank()) {
            logger.warn("{} Undefined or invalid pattern variable '{}' for !sub tag; using default pattern.",
                    placeholder.sourceLocation(), patternName);
            return StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN;
        }

        Pattern compiled = StringInterpolator.compilePatternSpec(patternSpec);
        if (compiled == null) {
            logger.warn("{} Invalid pattern specification '{}' in variable '{}' for !sub tag; using default pattern.",
                    placeholder.sourceLocation(), patternSpec, patternName);
            return StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN;
        }

        return compiled;
    }
}
