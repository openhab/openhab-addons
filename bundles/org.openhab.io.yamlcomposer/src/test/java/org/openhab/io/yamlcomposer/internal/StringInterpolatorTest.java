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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.openhab.io.yamlcomposer.internal.StringInterpolator.SubstitutionPattern;
import org.openhab.io.yamlcomposer.internal.core.EvaluationContext;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;

/**
 * Tests substitution boundaries and resistance to repeated ambiguous segments.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class StringInterpolatorTest {

    private final EvaluationContext context = new EvaluationContext(name -> {
    }, name -> null);
    private final LogSession logSession = new LogSession();

    @ParameterizedTest
    @MethodSource("substitutions")
    void preservesSubstitutionContent(String begin, String end, String content) {
        SubstitutionPattern pattern = StringInterpolator.compileSubstitutionPattern(begin, end);
        try (MockedStatic<ExpressionEvaluator> evaluator = mockStatic(ExpressionEvaluator.class)) {
            evaluator.when(() -> ExpressionEvaluator.renderObject(content, context, logSession, "test"))
                    .thenReturn(content);

            assertEquals(content, interpolate(begin + content + end, pattern));
            evaluator.verify(() -> ExpressionEvaluator.renderObject(content, context, logSession, "test"));
        }
    }

    private static Stream<Arguments> substitutions() {
        return Stream.of(Arguments.of("${", "}", ""), Arguments.of("${", "}", "name"),
                Arguments.of("${", "}", "name|default('{foo}')"), Arguments.of("${", "}", "\"${}\""),
                Arguments.of("${", "}", "{'key': 42}"), Arguments.of("${", "}", "'"), Arguments.of("${", "}", "\""),
                Arguments.of("${", "}", "'\""), Arguments.of("${", "}", "\"'"), Arguments.of("${", "}", "${}"),
                Arguments.of("${", "}", "${"), Arguments.of("$[[", "]]", "name|default(']]')"),
                Arguments.of("$((", "))", "\"))\""), Arguments.of("@[", "]", "{'key': ']'}"),
                Arguments.of("${", "}", "\"a\\\"}b\""), Arguments.of("${", "}", "'a\\'}b'"),
                Arguments.of("$[[", "]]", "\"a\\\"]]b\""), Arguments.of("@[", "]", "'a\\']b'"),
                Arguments.of("${", "}", "\"a\\\\\""), Arguments.of("${", "}", "\"a\\\\\\\"}b\""));
    }

    @Test
    void findsMultiplePlaceholdersWithQuotedClosingDelimiters() {
        context.scope().put("other", "$\\other");

        assertEquals("before ]] between $\\other after",
                interpolate("before $[[name|default(']]')]] between $[[other]] after",
                        StringInterpolator.compileSubstitutionPattern("$[[", "]]")));
    }

    @ParameterizedTest
    @MethodSource("escapedQuotes")
    void interpolatesEscapedQuotes(String begin, String end, String expression, String expected) {
        SubstitutionPattern pattern = StringInterpolator.compileSubstitutionPattern(begin, end);

        assertEquals(expected, interpolate(begin + expression + end, pattern));
        assertEquals("before " + expected + " after",
                interpolate("before " + begin + expression + end + " after", pattern));
        assertTrue(logSession.getTrackedWarnings().isEmpty());
    }

    private static Stream<Arguments> escapedQuotes() {
        return Stream.of(Arguments.of("${", "}", "\"a\\\"}b\"", "a\"}b"), Arguments.of("${", "}", "'a\\'}b'", "a'}b"),
                Arguments.of("$[[", "]]", "\"a\\\"]]b\"", "a\"]]b"), Arguments.of("@[", "]", "'a\\']b'", "a']b"),
                Arguments.of("${", "}", "\"a\\\\\"", "a\\"));
    }

    @Test
    void retainsNativeTypesAndRendersEmbeddedNulls() {
        List<Integer> list = List.of(1, 2);
        Map<String, Integer> map = Map.of("key", 42);
        context.scope().put("list", list);
        context.scope().put("map", map);
        context.scope().put("nothing", null);

        assertEquals(list, interpolate("${list}", StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN));
        assertEquals(map, interpolate("${map}", StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN));
        assertNull(interpolate("${nothing}", StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN));
        assertEquals("before  after",
                interpolate("before ${nothing} after", StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN));
    }

    @ParameterizedTest
    @ValueSource(strings = { "${", "$[[" })
    void leavesUnterminatedExpressionsUnchangedWithinBoundedTime(String begin) throws Exception {
        SubstitutionPattern pattern = StringInterpolator.compileSubstitutionPattern(begin,
                "${".equals(begin) ? "}" : "]]");
        String braces = begin + "{".repeat(100000) + "!";
        String openings = begin.repeat(100000) + "!";
        String segments = begin + "{\n\n    " + "\n        {{\n    \n        {{}} ".repeat(2000) + "!";

        assertEquals(braces, bounded(() -> interpolate(braces, pattern)));
        assertEquals(openings, bounded(() -> interpolate(openings, pattern)));
        if (!"${".equals(begin)) {
            assertEquals(segments, bounded(() -> interpolate(segments, pattern)));
        }
        assertTrue(logSession.getTrackedWarnings().isEmpty());
    }

    @Test
    void doesNotRescanSuffixesWhenQuotedDelimitersRequireFallback() throws Exception {
        String value = "$[[ ']]' ".repeat(20000);
        SubstitutionPattern pattern = StringInterpolator.compileSubstitutionPattern("$[[", "]]");
        // Resolve deliberately malformed expressions without involving the expression parser's own limits.
        assertEquals("' ".repeat(20000), bounded(() -> {
            try (MockedStatic<ExpressionEvaluator> evaluator = mockStatic(ExpressionEvaluator.class)) {
                Object interpolated = interpolate(value, pattern);
                evaluator.verify(() -> ExpressionEvaluator.renderObject(" '", context, logSession, "test"),
                        times(20000));
                return interpolated;
            }
        }));
    }

    private @Nullable Object interpolate(String value, SubstitutionPattern pattern) {
        return StringInterpolator.interpolate(value, pattern, context, logSession, "test");
    }

    private static <T> T bounded(Callable<T> operation) throws Exception {
        FutureTask<T> task = new FutureTask<>(operation);
        Thread.ofVirtual().start(task);
        try {
            return task.get(10, TimeUnit.SECONDS);
        } finally {
            task.cancel(true);
        }
    }
}
