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
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;

/**
 * Test cases for the {@link HttpPayloadParser} HTTP parsing.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestHttpPayloadParser {

    private static final String HEADERS_A = "HTTP/1.1 200 OK\r\nContent-type: application/hap+json\r\n";
    private static final String HEADERS_B = "content-length: %d\r\n";
    private static final String HEADERS_C = "transfer-encoding: chunked\r\n";
    private static final String HEADERS_Z1 = "connection: keep-alive\r\n\r";
    private static final String HEADERS_Z2 = "\n";
    private static final String HEADERS_Z = HEADERS_Z1 + HEADERS_Z2;

    private static final String OK_204 = "HTTP/1.1 204 No Content\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\r\nConnection: close\r\n\r\n";
    private static final String ERROR_403 = "HTTP/1.1 403 Forbidden\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\\r\\nConnection: close\r\n\r\n";
    private static final String ERROR_404 = "HTTP/1.1 404 Not Found\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\r\nConnection: close\r\n\r\n";
    private static final String ERROR_500 = "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";

    private static final String CONTENT = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    private static final String CHUNK_1 = "%x\r";
    private static final String CHUNK_2 = "\n";
    private static final String CHUNK = CHUNK_1 + CHUNK_2;
    private static final String CRLF = "\r\n";

    @Test
    void testHttpWithChunkedContentOk() throws IOException {
        String h = HEADERS_A + HEADERS_C + HEADERS_Z;
        String hc = h + CHUNK.formatted(100) + CONTENT + CRLF + CHUNK.formatted(0) + CRLF;
        InputStream inputStream = new JUnitTestInputStream(List.of(hc.getBytes()));
        try (HttpPayloadParser parser = new HttpPayloadParser(inputStream)) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            assertEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithChunkedContentOkManyPartial() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.substring(0, 8).getBytes(), 
                HEADERS_A.substring(8).getBytes(),
                HEADERS_C.substring(0, 14).getBytes(), 
                HEADERS_C.substring(14).getBytes(),
                HEADERS_Z.substring(0, 19).getBytes(), 
                HEADERS_Z.substring(19).getBytes(),
                CHUNK.formatted(100).getBytes(), 
                CONTENT.substring(0, 51).getBytes(), 
                CONTENT.substring(51).getBytes(),
                CRLF.getBytes(), 
                CHUNK.formatted(0).getBytes(), 
                CRLF.getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            byte[] content = payload.content();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = payload.headers();
            String h = HEADERS_A + HEADERS_C + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithChunkedContentOkManyPartialAndSplitChunkHeader() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.substring(0, 8).getBytes(), 
                HEADERS_A.substring(8).getBytes(),
                HEADERS_C.substring(0, 14).getBytes(), 
                HEADERS_C.substring(14).getBytes(),
                HEADERS_Z.substring(0, 19).getBytes(), 
                HEADERS_Z.substring(19).getBytes(),
                CHUNK_1.formatted(100).getBytes(), 
                CHUNK_2.getBytes(),
                CONTENT.substring(0, 51).getBytes(), 
                CONTENT.substring(51).getBytes(),
                CRLF.getBytes(), 
                CHUNK.formatted(0).getBytes(), 
                CRLF.getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            byte[] content = payload.content();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = payload.headers();
            String h = HEADERS_A + HEADERS_C + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithContentDiscardExtra() throws IOException {
        String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
        String hc = h + CONTENT + "EXTRA";
        InputStream inputStream = new JUnitTestInputStream(List.of(hc.getBytes()));
        try (HttpPayloadParser parser = new HttpPayloadParser(inputStream)) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            assertEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithContentManyPartialOk() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.substring(0, 11).getBytes(),
                HEADERS_A.substring(11).getBytes(),
                HEADERS_B.substring(0, 11).getBytes(),
                HEADERS_B.substring(11).formatted(100).getBytes(),
                HEADERS_Z.substring(0, 12).getBytes(),
                HEADERS_Z.substring(12).getBytes(),
                CONTENT.substring(0, 42).getBytes(),
                CONTENT.substring(42).getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            assertEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithContentManyPartialOkAndSplitCRLF() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.substring(0, 11).getBytes(),
                HEADERS_A.substring(11).getBytes(),
                HEADERS_B.substring(0, 11).getBytes(),
                HEADERS_B.substring(11).formatted(100).getBytes(),
                HEADERS_Z1.getBytes(),
                HEADERS_Z2.getBytes(),
                CONTENT.substring(0, 42).getBytes(),
                CONTENT.substring(42).getBytes()
                // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            assertEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithContentOk() throws IOException {
        String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
        String hc = h + CONTENT;
        List<byte[]> parts = List.of(hc.getBytes());
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            assertEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithMultipleFrames() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                 HEADERS_A.getBytes(), 
                 HEADERS_B.formatted(100) .getBytes(),
                 HEADERS_Z.getBytes(),
                 CONTENT.getBytes(),
                 (HEADERS_A + HEADERS_B.formatted(50) + HEADERS_Z).getBytes(),
                 CONTENT.substring(0, 50).getBytes() 
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            // first payload
            CompletableFuture<HttpPayload> future1 = parser.awaitHttpPayload();
            assertNotNull(future1);
            HttpPayload payload = assertDoesNotThrow(() -> future1.get());
            assertNotNull(payload);
            assertEquals(100, payload.content().length);
            assertEquals(CONTENT, new String(payload.content()));
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            assertEquals(h, new String(payload.headers()));
            // second payload
            CompletableFuture<HttpPayload> future2 = parser.awaitHttpPayload();
            assertNotNull(future2);
            payload = assertDoesNotThrow(() -> future2.get());
            assertNotNull(payload);
            assertEquals(50, payload.content().length);
            assertNotEquals(CONTENT, new String(payload.content()));
            assertNotEquals(h, new String(payload.headers()));
        }
    }

    @Test
    void testHttpWithZeroContentLength() throws IOException {
        String h = HEADERS_A + HEADERS_B.formatted(0) + HEADERS_Z;
        String hc = h + CONTENT;
        List<byte[]> parts = List.of(hc.getBytes());
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
        }
    }

    @Test
    void testOk204() throws IOException {
        List<byte[]> parts = List.of(OK_204.getBytes());
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(0, payload.content().length);
            assertEquals(OK_204, new String(payload.headers()));
        }
    }

    @Test
    void testError403() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_403.substring(0, 15).getBytes(),
                ERROR_403.substring(15).getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(0, payload.content().length);
            assertEquals(ERROR_403, new String(payload.headers()));
        }
    }

    @Test
    void testError404() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_404.substring(0, 20).getBytes(),
                ERROR_404.substring(20).getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(0, payload.content().length);
            assertEquals(ERROR_404, new String(payload.headers()));
        }
    }

    @Test
    void testError500() throws IOException {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_500.substring(0, 22).getBytes(),
                ERROR_500.substring(22).getBytes()
        // @formatter:on
        );
        try (HttpPayloadParser parser = new HttpPayloadParser(new JUnitTestInputStream(parts))) {
            CompletableFuture<HttpPayload> future = parser.awaitHttpPayload();
            assertNotNull(future);
            HttpPayload payload = assertDoesNotThrow(() -> future.get());
            assertNotNull(payload);
            assertEquals(0, payload.content().length);
            assertEquals(ERROR_500, new String(payload.headers()));
        }
    }
}
