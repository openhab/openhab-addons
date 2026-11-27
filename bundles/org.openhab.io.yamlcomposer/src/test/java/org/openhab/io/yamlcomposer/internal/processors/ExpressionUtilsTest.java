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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExpressionUtils}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ExpressionUtilsTest {

    @Nested
    @DisplayName("Top-Level Delimiter Search Tests")
    class TopLevelDelimiterTests {

        @Test
        @DisplayName("Should find top-level comment delimiter after a list with embedded hashes")
        void testFindsTopLevelHashAfterList() {
            String line = "x in ['a#b', 'c'] # This is a comment";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "#");

            assertNotNull(index);
            String before = line.substring(0, index).trim();
            String after = line.substring(index + 1).trim();

            assertThat(before, is("x in ['a#b', 'c']"));
            assertThat(after, is("This is a comment"));
        }

        @Test
        @DisplayName("Should ignore hash (#) symbols inside single quotes")
        void testIgnoresHashInsideSingleQuotes() {
            String line = "x in ['a#b', 'c']";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "#");

            assertThat(index, is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore hash (#) symbols inside double quotes")
        void testIgnoresHashInsideDoubleQuotes() {
            String line = "x in [\"a#b\", \"c\"]";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "#");

            assertThat(index, is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore hash (#) symbols inside nested brackets")
        void testIgnoresHashInsideBrackets() {
            String line = "items: [ 'val#1', [ 'nested#val' ] ]";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "#");

            assertThat(index, is(nullValue()));
        }

        @Test
        @DisplayName("Should handle escaped quotes before delimiter safely")
        void testHandlesEscapedQuotes() {
            String line = "x in ['a\\'#b', 'c'] # comment";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "#");

            assertNotNull(index);
            String after = line.substring(index + 1).trim();
            assertThat(after, is("comment"));
        }
    }

    @Nested
    @DisplayName("Word Boundary and Keyword Delimiter Tests")
    class WordDelimiterTests {

        @Test
        @DisplayName("Should respect word boundaries and ignore delimiters inside words like 'gift'")
        void testRespectsWordBoundaries() {
            String line = "gift = true if active";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "if");

            assertNotNull(index);
            String condition = line.substring(index + 2).trim();
            assertThat(condition, is("active"));
        }

        @Test
        @DisplayName("Should ignore word delimiters inside string literals")
        void testIgnoresWordDelimiterInQuotes() {
            String line = "message = 'Check if true' if condition";
            Integer index = ExpressionUtils.findTopLevelIndex(line, "if");

            assertNotNull(index);
            String after = line.substring(index + 2).trim();
            assertThat(after, is("condition"));
        }
    }
}
