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
package org.openhab.io.yamlcomposer.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.core.EvaluationContext;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;

/**
 * The {@link StringInterpolator} provides utility methods for performing
 * variable substitution and expression evaluation in YAML processing.
 *
 * @author Jimmy Tanagra - Initial contribution
 * @author Leo Siepel - Bound substitution pattern backtracking
 */
@NonNullByDefault
public class StringInterpolator {
    static final String DEFAULT_BEGIN = "${";
    static final String DEFAULT_END = "}";

    /**
     * Matches ${...} interpolation patterns in YAML strings.
     *
     * Allows nested quotes like default('{foo}') without prematurely stopping at
     * the inner '}'
     *
     * Examples:
     * - ${name} : Simple variable substitution
     * - ${ name|capitalize } : Apply capitalize filter
     * - ${ name|default('value') } : Use default if variable is not set
     * - ${ name|upper } : Convert to uppercase
     */
    public static final SubstitutionPattern DEFAULT_SUBSTITUTION_PATTERN = new SubstitutionPattern(DEFAULT_BEGIN,
            DEFAULT_END);

    /** Literal delimiters used to locate substitution expressions. */
    public record SubstitutionPattern(String begin, String end) {
        public SubstitutionPattern {
            if (begin.isEmpty() || end.isEmpty()) {
                throw new IllegalArgumentException("Substitution delimiters must not be empty");
            }
        }
    }

    private StringInterpolator() {
        // Utility class, no instances
    }

    /**
     * Creates a substitution pattern from literal begin and end delimiters.
     *
     * @param begin the opening delimiter (e.g., "${")
     * @param end the closing delimiter (e.g., "}")
     * @return the substitution pattern
     */
    public static SubstitutionPattern compileSubstitutionPattern(String begin, String end) {
        if (DEFAULT_BEGIN.equals(begin) && DEFAULT_END.equals(end)) {
            return DEFAULT_SUBSTITUTION_PATTERN;
        }
        return new SubstitutionPattern(begin, end);
    }

    /**
     * Parses a substitution pattern from a pattern specification string with format
     * {@code <begin>..<end>}.
     *
     * @param patternSpec the pattern specification, e.g. {@code $[[..]]}
     * @return the substitution pattern, or {@code null} when the specification is invalid
     */
    public static @Nullable SubstitutionPattern compilePatternSpec(String patternSpec) {
        int separator = patternSpec.indexOf("..");
        if (separator <= 0 || separator >= patternSpec.length() - 2) {
            return null;
        }

        String begin = patternSpec.substring(0, separator);
        String end = patternSpec.substring(separator + 2);
        if (begin.isEmpty() || end.isEmpty()) {
            return null;
        }

        return compileSubstitutionPattern(begin, end);
    }

    /**
     * Evaluates a variable value, performing substitution if needed.
     * Returns the native object type if the entire value is a single placeholder,
     * otherwise returns a string with interpolated values.
     *
     * @param value the raw string value that may contain placeholder patterns to substitute
     * @param pattern the substitution pattern to use
     * @param context the evaluation context containing variables and callbacks
     * @param logSession the logging session
     * @param sourceLocation description of the source location for logging
     * @return the evaluated value
     */
    public static @Nullable Object interpolate(String value, SubstitutionPattern pattern, EvaluationContext context,
            LogSession logSession, String sourceLocation) {
        int start = value.indexOf(pattern.begin());
        if (start < 0 || value.indexOf(pattern.end(), start + pattern.begin().length()) < 0) {
            return value;
        }

        int[] closingPositions = closingPositions(value, pattern.end(), start + pattern.begin().length());
        StringBuilder interpolated = new StringBuilder(value.length());
        int previousEnd = 0;
        while (start >= 0) {
            int contentStart = start + pattern.begin().length();
            int closing = closingPositions[contentStart];
            if (closing < 0) {
                // Preserve diagnostics for malformed expressions, including unclosed quotes or braces.
                closing = value.indexOf(pattern.end(), contentStart);
            }
            if (closing < 0) {
                break;
            }

            String content = value.substring(contentStart, closing);
            Object result = evaluateExpression(content, context, logSession, sourceLocation);
            int end = closing + pattern.end().length();
            // A single placeholder retains its native type (including null, maps and lists).
            if (start == 0 && end == value.length()) {
                return result;
            }
            interpolated.append(value, previousEnd, start);
            if (result != null) {
                interpolated.append(result);
            }
            previousEnd = end;
            start = value.indexOf(pattern.begin(), end);
        }
        return interpolated.append(value, previousEnd, value.length()).toString();
    }

    private static int[] closingPositions(String value, String end, int from) {
        int[] positions = new int[value.length() + 1];
        positions[value.length()] = -1;
        int singleQuote = -1;
        int doubleQuote = -1;
        int brace = -1;
        // Resolve each suffix once, so failed quoted/braced segments and later opening delimiters
        // cannot repeatedly rescan the remaining input. Complete segments skip their contents.
        for (int i = value.length() - 1; i >= from; i--) {
            char ch = value.charAt(i);
            int segmentEnd = switch (ch) {
                case '\'' -> singleQuote;
                case '"' -> doubleQuote;
                case '{' -> brace;
                default -> -1;
            };
            positions[i] = value.startsWith(end, i) ? i : positions[segmentEnd < 0 ? i + 1 : segmentEnd + 1];
            if (ch == '}') {
                brace = i;
            } else if ((ch == '\'' || ch == '"') && !isEscaped(value, i)) {
                if (ch == '\'') {
                    singleQuote = i;
                } else {
                    doubleQuote = i;
                }
            }
        }
        return positions;
    }

    private static boolean isEscaped(String value, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && value.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return backslashes % 2 != 0;
    }

    /**
     * Evaluates a Jinjava expression.
     *
     * When an error occurs during evaluation, and null is returned.
     * A warning is logged and consolidated through logSession
     *
     * @param expression the expression to evaluate without the ${} delimiters
     * @param context the evaluation context containing variables and callbacks
     * @param logSession the logging session for warnings and errors during evaluation
     * @param sourceLocation description of the source location for logging
     * @return the evaluated result
     */
    public static @Nullable Object evaluateExpression(String expression, EvaluationContext context,
            LogSession logSession, String sourceLocation) {
        Object rendered = ExpressionEvaluator.renderObject(expression, context, logSession, sourceLocation);
        return rendered;
    }
}
