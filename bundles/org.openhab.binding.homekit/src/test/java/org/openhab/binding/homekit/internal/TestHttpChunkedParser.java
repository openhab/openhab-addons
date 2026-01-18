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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;

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
    private final String header = s0 + s1 + s2 + s3 + crlf;

    @Test
    void testValidChunkedPayload() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                 s0.getBytes(),
                 s1.getBytes(),
                 s2.getBytes(),
                 s3.getBytes(),
                 crlf.getBytes(),
                 s5.getBytes(),
                 s6.getBytes(),
                 s7.getBytes(),
                 s8.getBytes(),
                 s9.getBytes(),
                 crlf.getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(header, new String(payload.headers()));
            assertEquals("123456789123456789abcdef", new String(payload.content()));
        }
    }

    @Test
    void testBadChunkedSizePayload() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                 s0.getBytes(),
                 s1.getBytes(),
                 s2.getBytes(),
                 s3.getBytes(),
                 crlf.getBytes(),
                 s5err.getBytes(),
                 s6.getBytes(),
                 s7.getBytes(),
                 s8.getBytes(),
                 s9.getBytes(),
                 crlf.getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            assertThrows(ExecutionException.class, () -> future.get());
        }
    }

    @Test
    void testValidChunkedPayloadWitSplitFrames() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                s0.getBytes(),
                s1.getBytes(),
                s2.getBytes(),
                s3.getBytes(),
                "\r".getBytes(),
                "\n".getBytes(),
                s5.getBytes(),
                s6.getBytes(),
                s7.getBytes(),
                s8.getBytes(),
                "0".getBytes(),
                "\r".getBytes(),
                "\n".getBytes(),
                "\r".getBytes(),
                "\n".getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(header, new String(payload.headers()));
            assertEquals("123456789123456789abcdef", new String(payload.content()));
        }
    }
}
