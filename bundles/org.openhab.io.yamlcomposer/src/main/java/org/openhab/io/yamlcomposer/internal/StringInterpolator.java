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

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;

/**
 * The {@link StringInterpolator} provides utility methods for performing
 * variable substitution and expression evaluation in YAML processing.
 *
 * @author Jimmy Tanagra - Initial contribution
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
    public static final Pattern DEFAULT_SUBSTITUTION_PATTERN = Pattern.compile("""
            \\$\\{
            (?<content>
                (?:
                    "[^"]*" |
                    '[^']*' |
                    \\{[^}]*?\\} |
                    [^}]
                )*?
            )
            \\}
            """, Pattern.COMMENTS | Pattern.DOTALL);

    private StringInterpolator() {
        // Utility class, no instances
    }

    /**
     * Compiles a substitution pattern from begin and end delimiters.
     *
     * @param begin the opening delimiter (e.g., "${")
     * @param end the closing delimiter (e.g., "}")
     * @return the compiled pattern
     */
    public static Pattern compileSubstitutionPattern(String begin, String end) {
        if (DEFAULT_BEGIN.equals(begin) && DEFAULT_END.equals(end)) {
            return DEFAULT_SUBSTITUTION_PATTERN;
        }
        String quotedBegin = Pattern.quote(begin);
        String quotedEnd = Pattern.quote(end);
        // Allow quoted segments to contain the end delimiter; otherwise stop at the
        // first closing delimiter.
        String content = """
                "[^"]*"|'[^']*'|\\{[^}]*?\\}|(?:(?!%s).)
                """.formatted(quotedEnd).strip();
        String regex = quotedBegin + "(?<content>(" + content + ")*?)" + quotedEnd;
        return Pattern.compile(regex, Pattern.DOTALL);
    }

    /**
     * Compiles a substitution pattern from a pattern specification string with format
     * {@code <begin>..<end>}.
     *
     * @param patternSpec the pattern specification, e.g. {@code $[[..]]}
     * @return the compiled pattern, or {@code null} when the specification is invalid
     */
    public static @Nullable Pattern compilePatternSpec(String patternSpec) {
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
     * @param variables the variable map
     * @param logSession the logging session
     * @param sourceLocation description of the source location for logging
     * @return the evaluated value
     */
    public static @Nullable Object interpolate(String value, Pattern pattern, Map<String, @Nullable Object> variables,
            LogSession logSession, String sourceLocation) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return value;
        }

        // If the whole value is exactly one ${...}, evaluate it and return the native object.
        // This preserves non-string types (maps, lists, numbers, booleans)
        // when the value is a plain placeholder.
        if (matcher.matches()) {
            String content = Objects.requireNonNull(matcher.group("content"));
            return evaluateExpression(content, variables, logSession, sourceLocation);
        }

        // Evaluate the expressions inside the ${...} patterns and replace them in the string.
        String interpolated = matcher.replaceAll(match -> {
            String content = Objects.requireNonNull(match.group("content"));
            Object result = evaluateExpression(content, variables, logSession, sourceLocation);
            String rendered = result != null ? result.toString() : "";
            return Matcher.quoteReplacement(rendered);
        });

        return interpolated;
    }

    /**
     * Evaluates a Jinjava expression.
     *
     * When an error occurs during evaluation, and null is returned.
     * A warning is logged and consolidated through logSession
     *
     * @param expression the expression to evaluate without the ${} delimiters
     * @param variables the variable context for evaluation
     * @param logSession the logging session for warnings and errors during evaluation
     * @param sourceLocation description of the source location for logging
     * @return the evaluated result
     */
    public static @Nullable Object evaluateExpression(String expression, Map<String, @Nullable Object> variables,
            LogSession logSession, String sourceLocation) {
        Object rendered = ExpressionEvaluator.renderObject(expression, variables, logSession, sourceLocation);
        return rendered;
    }
}
