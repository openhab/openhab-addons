/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;

/**
 * Test cases for HTTP parser; in particular for chunked payloads.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestHttpChunkedParser {

    private final String s0 = "HTTP/1.1 200 OK\r\n";
    private final String s1 = "Content-Type: application/hap+json\r\n";
    private final String s2 = "Content-Length: 0\r\n";
    private final String s3 = "Transfer-Encoding: chunked\r\n";
    private final String crlf = "\r\n";
    private final String s5 = "09\r\n";
    private final String s5err = "err\r\n";
    private final String s6 = "123456789\r\n";
    private final String s7 = "0f\r\n";
    private final String s8 = "123456789abcdef\r\n";
    private final String s9 = "0\r\n";

    @Test
    void testValidChunkedPayload() {
        HttpPayloadParser parser = new HttpPayloadParser();
        parser.accept(s0.getBytes(StandardCharsets.UTF_8));
        parser.accept(s1.getBytes(StandardCharsets.UTF_8));
        parser.accept(s2.getBytes(StandardCharsets.UTF_8));
        parser.accept(s3.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s5.getBytes(StandardCharsets.UTF_8));
        parser.accept(s6.getBytes(StandardCharsets.UTF_8));
        parser.accept(s7.getBytes(StandardCharsets.UTF_8));
        parser.accept(s8.getBytes(StandardCharsets.UTF_8));
        parser.accept(s9.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        assertTrue(parser.isComplete());
        assertEquals("123456789123456789abcdef", new String(parser.getContent(), StandardCharsets.UTF_8));
    }

    @Test
    void testBadChunkedSizePayload() {
        HttpPayloadParser parser = new HttpPayloadParser();
        parser.accept(s0.getBytes(StandardCharsets.UTF_8));
        parser.accept(s1.getBytes(StandardCharsets.UTF_8));
        parser.accept(s2.getBytes(StandardCharsets.UTF_8));
        parser.accept(s3.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s5err.getBytes(StandardCharsets.UTF_8));
        parser.accept(s6.getBytes(StandardCharsets.UTF_8));
        parser.accept(s7.getBytes(StandardCharsets.UTF_8));
        parser.accept(s8.getBytes(StandardCharsets.UTF_8));
        parser.accept(s9.getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalStateException.class, () -> parser.accept(crlf.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testChunkedPayloadWithEmptyLines() {
        HttpPayloadParser parser = new HttpPayloadParser();
        parser.accept(s0.getBytes(StandardCharsets.UTF_8));
        parser.accept(s1.getBytes(StandardCharsets.UTF_8));
        parser.accept(s2.getBytes(StandardCharsets.UTF_8));
        parser.accept(s3.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s5.getBytes(StandardCharsets.UTF_8));
        parser.accept(s6.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s7.getBytes(StandardCharsets.UTF_8));
        parser.accept(s8.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s9.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        assertTrue(parser.isComplete());
        assertEquals("123456789123456789abcdef", new String(parser.getContent(), StandardCharsets.UTF_8));
    }

    @Test
    void testIncompleteChunkedPayload() {
        HttpPayloadParser parser = new HttpPayloadParser();
        parser.accept(s0.getBytes(StandardCharsets.UTF_8));
        parser.accept(s1.getBytes(StandardCharsets.UTF_8));
        parser.accept(s2.getBytes(StandardCharsets.UTF_8));
        parser.accept(s3.getBytes(StandardCharsets.UTF_8));
        parser.accept(crlf.getBytes(StandardCharsets.UTF_8));
        parser.accept(s5.getBytes(StandardCharsets.UTF_8));
        parser.accept(s6.getBytes(StandardCharsets.UTF_8));
        parser.accept(s7.getBytes(StandardCharsets.UTF_8));
        parser.accept(s8.getBytes(StandardCharsets.UTF_8));
        parser.accept(s9.getBytes(StandardCharsets.UTF_8));
        assertFalse(parser.isComplete());
        assertEquals("", new String(parser.getContent(), StandardCharsets.UTF_8));
    }

    @Test
    void testValidChunkedPayloadWitSplitFrames() {
        HttpPayloadParser parser = new HttpPayloadParser();
        parser.accept(s0.getBytes(StandardCharsets.UTF_8));
        parser.accept(s1.getBytes(StandardCharsets.UTF_8));
        parser.accept(s2.getBytes(StandardCharsets.UTF_8));
        parser.accept(s3.getBytes(StandardCharsets.UTF_8));
        parser.accept("\r".getBytes(StandardCharsets.UTF_8));
        parser.accept("\n".getBytes(StandardCharsets.UTF_8));
        parser.accept(s5.getBytes(StandardCharsets.UTF_8));
        parser.accept(s6.getBytes(StandardCharsets.UTF_8));
        parser.accept(s7.getBytes(StandardCharsets.UTF_8));
        parser.accept(s8.getBytes(StandardCharsets.UTF_8));
        parser.accept("0".getBytes(StandardCharsets.UTF_8));
        parser.accept("\r".getBytes(StandardCharsets.UTF_8));
        parser.accept("\n".getBytes(StandardCharsets.UTF_8));
        parser.accept("\r".getBytes(StandardCharsets.UTF_8));
        parser.accept("\n".getBytes(StandardCharsets.UTF_8));
        assertTrue(parser.isComplete());
        assertEquals("123456789123456789abcdef", new String(parser.getContent(), StandardCharsets.UTF_8));
    }
}
