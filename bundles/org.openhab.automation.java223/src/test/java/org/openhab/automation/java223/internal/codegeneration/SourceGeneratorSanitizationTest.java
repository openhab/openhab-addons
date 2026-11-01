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
package org.openhab.automation.java223.internal.codegeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the name sanitization performed by the {@link SourceGenerator}, so that the generated helper classes
 * always contain valid Java identifiers.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class SourceGeneratorSanitizationTest {

    /**
     * The complete list of the Java reserved keywords (JLS 3.9) and literals (JLS 3.10). None of them can be used as
     * a member name.
     */
    private static final List<String> JAVA_RESERVED_KEYWORDS = List.of("abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
            "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
            "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null");

    @Test
    public void validNameIsNotModified() {
        assertEquals("my_item", SourceGenerator.sanitizeNameToJava("my_item"));
        assertEquals("MyItem", SourceGenerator.sanitizeNameToJava("MyItem"));
        assertEquals("item1", SourceGenerator.sanitizeNameToJava("item1"));
        assertEquals("_item", SourceGenerator.sanitizeNameToJava("_item"));
    }

    @Test
    public void specialCharactersAreReplacedByUnderscores() {
        assertEquals("my_item", SourceGenerator.sanitizeNameToJava("my-item"));
        assertEquals("a_b_c", SourceGenerator.sanitizeNameToJava("a:b:c"));
        assertEquals("my_item_", SourceGenerator.sanitizeNameToJava("my item!"));
        assertEquals("my_item", SourceGenerator.sanitizeNameToJava("my.item"));
    }

    @Test
    public void nameStartingWithADigitIsPrefixedByUnderscore() {
        assertEquals("_1st_item", SourceGenerator.sanitizeNameToJava("1st_item"));
        assertEquals("_42", SourceGenerator.sanitizeNameToJava("42"));
    }

    @Test
    public void emptyNameIsSanitizedToUnderscore() {
        assertEquals("_", SourceGenerator.sanitizeNameToJava(""));
    }

    @ParameterizedTest
    @ValueSource(strings = { "class", "int", "new", "static", "public", "true", "false", "null", "while", "package",
            "this", "void", "enum", "instanceof" })
    public void reservedKeywordsAreSuffixedByUnderscore(String keyword) {
        assertEquals(keyword + "_", SourceGenerator.sanitizeNameToJava(keyword));
    }

    /**
     * The contextual keywords (JLS 3.9) are not reserved: they are legal member names, so they must not be
     * sanitized.
     */
    @ParameterizedTest
    @ValueSource(strings = { "record", "var", "yield", "sealed", "permits" })
    public void contextualKeywordsAreNotModified(String keyword) {
        assertEquals(keyword, SourceGenerator.sanitizeNameToJava(keyword));
    }

    @Test
    public void allJavaReservedKeywordsAreHandled() {
        for (String keyword : JAVA_RESERVED_KEYWORDS) {
            String sanitized = SourceGenerator.sanitizeNameToJava(keyword);
            assertNotEquals(keyword, sanitized, "The reserved keyword '" + keyword + "' must be sanitized");
            assertTrue(sanitized.startsWith(keyword), "The sanitized name must stay close to the original one");
        }
    }

    @Test
    public void sanitizedNameIsAValidJavaIdentifier() {
        List<String> names = List.of("my-item", "class", "1st", "", "a:b:c", "my item", "new", "int");
        for (String name : names) {
            String sanitized = SourceGenerator.sanitizeNameToJava(name);
            assertTrue(sanitized.matches("[a-zA-Z_$][a-zA-Z0-9_$]*"),
                    "The sanitized name '" + sanitized + "' is not a valid Java identifier");
        }
    }

    @Test
    public void commentWithoutSpecialSequenceIsNotModified() {
        assertEquals("A simple label", SourceGenerator.sanitizeComment("A simple label"));
        assertEquals("", SourceGenerator.sanitizeComment(""));
    }

    @Test
    public void commentTerminatingSequenceIsNeutralized() {
        assertEquals("a *\\/ b", SourceGenerator.sanitizeComment("a */ b"));
        assertEquals("*\\/", SourceGenerator.sanitizeComment("*/"));
        assertEquals("end of comment *\\/ then code", SourceGenerator.sanitizeComment("end of comment */ then code"));
    }

    @Test
    public void commentOpeningSequenceIsNeutralized() {
        assertEquals("a /\\* b", SourceGenerator.sanitizeComment("a /* b"));
        assertEquals("/\\*", SourceGenerator.sanitizeComment("/*"));
    }

    /**
     * The sanitized text must not be able to close the comment it is inserted in.
     */
    @Test
    public void sanitizedCommentCannotCloseTheComment() {
        List<String> labels = List.of("*/", "a */ b", "*/ public static String evil = \"x\";", "/*", "a /* b", "**/",
                "*/ */", "/* */");
        for (String label : labels) {
            String sanitized = SourceGenerator.sanitizeComment(label);
            assertFalse(sanitized.contains("*/"), "The sanitized comment '" + sanitized + "' must not contain '*/'");
            assertFalse(sanitized.contains("/*"), "The sanitized comment '" + sanitized + "' must not contain '/*'");
        }
    }

    @Test
    public void sanitizedCommentKeepsTheOriginalTextReadable() {
        // only the comment delimiters are altered, the rest of the label is left untouched
        assertEquals("Kitchen light", SourceGenerator.sanitizeComment("Kitchen light"));
        assertEquals("50% (on/off)", SourceGenerator.sanitizeComment("50% (on/off)"));
        assertEquals("a *\\/ b", SourceGenerator.sanitizeComment("a */ b"));
    }

    @Test
    public void collidingNamesGetAUniqueSuffix() {
        Map<String, String> sanitizedNames = SourceGenerator
                .buildSanitizedNames(List.of("my-item", "my_item", "my.item", "other"));

        assertEquals("my_item", sanitizedNames.get("my-item"));
        assertEquals("my_item_2", sanitizedNames.get("my_item"));
        assertEquals("my_item_3", sanitizedNames.get("my.item"));
        assertEquals("other", sanitizedNames.get("other"));
    }

    @Test
    public void collidingNamesDoNotProduceDuplicates() {
        Map<String, String> sanitizedNames = SourceGenerator
                .buildSanitizedNames(List.of("a-b", "a_b", "a b", "a.b", "a:b"));

        Set<String> values = Set.copyOf(sanitizedNames.values());
        assertEquals(sanitizedNames.size(), values.size(), "All the sanitized names must be unique");
        assertTrue(values.contains("a_b"));
        assertTrue(values.contains("a_b_2"));
        assertTrue(values.contains("a_b_3"));
        assertTrue(values.contains("a_b_4"));
        assertTrue(values.contains("a_b_5"));
    }

    @Test
    public void reservedKeywordCollisionIsAlsoResolved() {
        Map<String, String> sanitizedNames = SourceGenerator.buildSanitizedNames(List.of("class", "class_", "class"));

        assertEquals("class_", sanitizedNames.get("class"));
        assertEquals("class__2", sanitizedNames.get("class_"));
        assertEquals(2, sanitizedNames.size());
    }

    /**
     * The same name may be provided several times (for example when two registries hold the same object). The name
     * computed during the first occurrence must be kept.
     */
    @Test
    public void duplicatedNamesKeepTheFirstComputedName() {
        Map<String, String> sanitizedNames = SourceGenerator
                .buildSanitizedNames(List.of("my_item", "other_item", "my_item", "my_item"));

        assertEquals(2, sanitizedNames.size());
        assertEquals("my_item", sanitizedNames.get("my_item"));
        assertEquals("other_item", sanitizedNames.get("other_item"));
    }
}
