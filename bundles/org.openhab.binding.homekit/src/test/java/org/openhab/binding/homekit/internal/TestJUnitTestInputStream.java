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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Self referential unit tests for the {@link JUnitTestInputStream} class that we use for other tests.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestJUnitTestInputStream {

    private static final String TEST_STRING = "The quick brown fox jumps over the lazy dog";
    private static final int TEST_STRING_LENGTH = TEST_STRING.length();

    @Test
    void testPlainRead() throws IOException {
        try (InputStream stream = new JUnitTestInputStream(List.of(TEST_STRING.getBytes()))) {
            int c = assertDoesNotThrow(() -> stream.read());
            assertEquals(TEST_STRING.charAt(0), (char) c);
            c = assertDoesNotThrow(() -> stream.read());
            assertEquals(TEST_STRING.charAt(1), (char) c);
        }
    }

    @Test
    void testPlainReadBuffer() throws IOException {
        try (InputStream stream = new JUnitTestInputStream(List.of(TEST_STRING.getBytes()))) {
            byte[] buffer = new byte[1024];
            int c = assertDoesNotThrow(() -> stream.read(buffer));
            assertEquals(TEST_STRING_LENGTH, c);
            c = assertDoesNotThrow(() -> stream.read());
            assertEquals(-1, c); // EOF
        }
    }

    @Test
    void testPlainReadBufferWithParams() throws IOException {
        try (InputStream stream = new JUnitTestInputStream(List.of(TEST_STRING.getBytes()))) {
            byte[] buffer = new byte[1024];
            int c = assertDoesNotThrow(() -> stream.read(buffer, 0, 10));
            assertEquals(10, c);
            assertEquals(TEST_STRING.substring(0, c), new String(buffer, 0, c));
        }
    }

    @Test
    void testPlainReadAllBytes() throws IOException {
        try (InputStream stream = new JUnitTestInputStream(List.of(TEST_STRING.getBytes()))) {
            byte[] bytes = assertDoesNotThrow(() -> stream.readAllBytes());
            assertEquals(TEST_STRING_LENGTH, bytes.length);
            assertEquals(TEST_STRING, new String(bytes));
        }
    }
}
