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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests substitution boundaries and resistance to repeated ambiguous segments.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class StringInterpolatorTest {

    @ParameterizedTest
    @MethodSource("substitutions")
    void preservesSubstitutionContent(String begin, String end, String content) {
        Pattern pattern = StringInterpolator.compileSubstitutionPattern(begin, end);
        Matcher matcher = pattern.matcher(begin + content + end);

        assertTrue(matcher.matches());
        assertEquals(content, matcher.group("content"));
    }

    private static Stream<Arguments> substitutions() {
        return Stream.of(Arguments.of("${", "}", ""), Arguments.of("${", "}", "name"),
                Arguments.of("${", "}", "name|default('{foo}')"), Arguments.of("${", "}", "\"${}\""),
                Arguments.of("${", "}", "{'key': 42}"), Arguments.of("${", "}", "'"), Arguments.of("${", "}", "\""),
                Arguments.of("${", "}", "'\""), Arguments.of("${", "}", "\"'"), Arguments.of("${", "}", "${}"),
                Arguments.of("${", "}", "${"), Arguments.of("$[[", "]]", "name|default(']]')"),
                Arguments.of("$((", "))", "\"))\""), Arguments.of("@[", "]", "{'key': ']'}"));
    }

    @Test
    void findsMultiplePlaceholdersWithQuotedClosingDelimiters() {
        Matcher matcher = StringInterpolator.compileSubstitutionPattern("$[[", "]]")
                .matcher("before $[[name|default(']]')]] between $[[other]] after");

        assertTrue(matcher.find());
        assertEquals("name|default(']]')", matcher.group("content"));
        assertTrue(matcher.find());
        assertEquals("other", matcher.group("content"));
        assertFalse(matcher.find());
    }

    @Test
    void rejectsRepeatedBracedSegmentsWithoutBacktrackingThroughTheirPartitions() throws Exception {
        String content = "{\n\n    " + "\n        {{\n    \n        {{}} ".repeat(2000) + "!";

        assertFalse(bounded(() -> StringInterpolator.DEFAULT_SUBSTITUTION_PATTERN.matcher("${" + content).matches()));
        assertFalse(bounded(
                () -> StringInterpolator.compileSubstitutionPattern("$[[", "]]").matcher("$[[" + content).find()));
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
