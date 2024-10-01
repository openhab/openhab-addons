/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * UT for {@link org.openhab.binding.argoclima.internal.utils.StringUtils
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
class StringUtilsTest {

    /**
     * Test method for
     * {@link org.openhab.binding.argoclima.internal.utils.StringUtils#strip(java.lang.String, java.lang.String)}.
     */
    @Test
    void testStrip() {
        assertEquals("", StringUtils.strip("", "a n y t h \t\n g"));
        assertEquals("  abc", StringUtils.strip("  abcyx", "xyz"));
        assertEquals("", StringUtils.strip(" ", " "));
        assertEquals("", StringUtils.strip(" ", "\s"), "Whitespace as Java-escaped char");
        assertEquals("\t", StringUtils.strip("  \t", " "), "Match only exact whitespace type");
        assertEquals("", StringUtils.strip("  \t", " \t"), "Match only exact whitespace type");
        assertEquals("test test", StringUtils.strip(" test test ", " "), "No middle removal");
        assertEquals("in", StringUtils.strip("begin", "geb"));
        assertEquals("e", StringUtils.strip("end\n", "dn\n"));
        assertEquals("TEST", StringUtils.strip("TEST", "test"), "Case-sensitive");
        assertEquals("", StringUtils.strip("abcxyz", "aabbcczzyyxx"), "Repteat pattern characters");
        assertEquals("text ", StringUtils.strip(".\\(){}text -[]", ".\\()[]{}-"), "Regex special characters");
        assertEquals("quoted", StringUtils.strip("\"quoted\"", "\""));
        assertEquals("quoted", StringUtils.strip("'quoted'", "'"));
        assertEquals("bracketed", StringUtils.strip("[ -\t'bracketed' ]", "[]- \t\"'"));
        assertEquals("SUN, MON", StringUtils.strip("[SUN, MON]", "[]{}()"));
    }

    /**
     * Test method for
     * {@link org.openhab.binding.argoclima.internal.utils.StringUtils#splitByWholeSeparator(String, String)}.
     */
    @Test
    void testSplitByWholeSeparator() {
        assertIterableEquals(List.<String> of(), StringUtils.splitByWholeSeparator("", "anything"));
        assertIterableEquals(List.<String> of(), StringUtils.splitByWholeSeparator("###", "#"));
        assertIterableEquals(List.of("one", "two=-three"), StringUtils.splitByWholeSeparator("one-=two=-three", "-="));
        assertIterableEquals(List.of("ab", "de", "fg"), StringUtils.splitByWholeSeparator("ab de fg", " "));
        assertIterableEquals(List.of("ab", "de", "fg"), StringUtils.splitByWholeSeparator("ab   de   fg", " "));
        assertIterableEquals(List.of("ab", "de", "fg"), StringUtils.splitByWholeSeparator("  ab   de   fg  ", " "));
        assertIterableEquals(List.of("ab", "cd", "ef"), StringUtils.splitByWholeSeparator("ab:cd:ef", ":"));
        assertIterableEquals(List.of("ab", "cd", "ef"), StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertIterableEquals(List.of("ab", "cd", "ef"), StringUtils.splitByWholeSeparator("ab#cd#ef", "#"));
        assertIterableEquals(List.of("ab", "cd", "ef"), StringUtils.splitByWholeSeparator("ab###cd##ef", "#"));
        assertIterableEquals(List.of("ab", "cd", "ef"), StringUtils.splitByWholeSeparator("####ab#cd#ef#", "#"));
    }
}
