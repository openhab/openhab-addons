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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;

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
    private static final String ERROR_403 = "HTTP/1.1 403 Forbidden\r\nTransfer-Encoding: chunked\r\n\r\n";
    private static final String ERROR_404 = "HTTP/1.1 404 Not Found\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\r\nConnection: close\r\n\r\n";
    private static final String ERROR_500 = "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";

    private static final String CONTENT = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    private static final String CHUNK_1 = "%x\r";
    private static final String CHUNK_2 = "\n";
    private static final String CHUNK = CHUNK_1 + CHUNK_2;
    private static final String CRLF = "\r\n";

    @Test
    void testHttpWithChunkedContentOk() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_C + HEADERS_Z;
            String hc = h + CHUNK.formatted(100) + CONTENT + CRLF + CHUNK.formatted(0) + CRLF;
            parser.accept(hc.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithChunkedContentOkManyPartial() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(HEADERS_A.substring(0, 8).getBytes());
            parser.accept(HEADERS_A.substring(8).getBytes());
            parser.accept(HEADERS_C.substring(0, 14).getBytes());
            parser.accept(HEADERS_C.substring(14).getBytes());
            parser.accept(HEADERS_Z.substring(0, 19).getBytes());
            parser.accept(HEADERS_Z.substring(19).getBytes());
            parser.accept(CHUNK.formatted(100).getBytes());
            parser.accept(CONTENT.substring(0, 51).getBytes());
            parser.accept(CONTENT.substring(51).getBytes());
            parser.accept(CRLF.getBytes());
            parser.accept(CHUNK.formatted(0).getBytes());
            parser.accept(CRLF.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            String h = HEADERS_A + HEADERS_C + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithChunkedContentOkManyPartialAndSplitChunkHeader() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(HEADERS_A.substring(0, 8).getBytes());
            parser.accept(HEADERS_A.substring(8).getBytes());
            parser.accept(HEADERS_C.substring(0, 14).getBytes());
            parser.accept(HEADERS_C.substring(14).getBytes());
            parser.accept(HEADERS_Z.substring(0, 19).getBytes());
            parser.accept(HEADERS_Z.substring(19).getBytes());
            parser.accept(CHUNK_1.formatted(100).getBytes());
            parser.accept(CHUNK_2.getBytes());
            parser.accept(CONTENT.substring(0, 51).getBytes());
            parser.accept(CONTENT.substring(51).getBytes());
            parser.accept(CRLF.getBytes());
            parser.accept(CHUNK.formatted(0).getBytes());
            parser.accept(CRLF.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            String h = HEADERS_A + HEADERS_C + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithContentDiscardExtra() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            String hc = h + CONTENT + "EXTRA";
            parser.accept(hc.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithContentManyPartialOk() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(HEADERS_A.substring(0, 11).getBytes());
            parser.accept(HEADERS_A.substring(11).getBytes());
            parser.accept(HEADERS_B.substring(0, 11).getBytes());
            parser.accept(HEADERS_B.substring(11).formatted(100).getBytes());
            parser.accept(HEADERS_Z.substring(0, 12).getBytes());
            parser.accept(HEADERS_Z.substring(12).getBytes());
            parser.accept(CONTENT.substring(0, 42).getBytes());
            parser.accept(CONTENT.substring(42).getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithContentManyPartialOkAndSplitCRLF() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(HEADERS_A.substring(0, 11).getBytes());
            parser.accept(HEADERS_A.substring(11).getBytes());
            parser.accept(HEADERS_B.substring(0, 11).getBytes());
            parser.accept(HEADERS_B.substring(11).formatted(100).getBytes());
            parser.accept(HEADERS_Z1.getBytes());
            parser.accept(HEADERS_Z2.getBytes());
            parser.accept(CONTENT.substring(0, 42).getBytes());
            parser.accept(CONTENT.substring(42).getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithContentOk() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
            String hc = h + CONTENT;
            parser.accept(hc.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(100, content.length);
            assertEquals(CONTENT, new String(content));
            byte[] headers = parser.getHeaders();
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testHttpWithMultipleFrames() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B.formatted(300) + HEADERS_Z;
            String hc = h + CONTENT;
            parser.accept(hc.getBytes());
            assertFalse(parser.isComplete());
            parser.accept(CONTENT.getBytes());
            parser.accept(CONTENT.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(300, content.length);
        }
    }

    @Test
    void testHttpWithNoContentLength() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B;
            String hc = h + CONTENT;
            parser.accept(hc.getBytes());
            assertFalse(parser.isComplete());
        }
    }

    @Test
    void testHttpWithWrongContentLength() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B.formatted(200) + HEADERS_Z;
            String hc = h + CONTENT;
            parser.accept(hc.getBytes());
            assertFalse(parser.isComplete());
        }
    }

    @Test
    void testHttpWithZeroContentLength() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            String h = HEADERS_A + HEADERS_B.formatted(0) + HEADERS_Z;
            String hc = h + CONTENT;
            parser.accept(hc.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(0, content.length);
            byte[] headers = parser.getHeaders();
            assertEquals(h, new String(headers));
        }
    }

    @Test
    void testOk204() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(OK_204.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(0, content.length);
            byte[] headers = parser.getHeaders();
            assertEquals(OK_204, new String(headers));
        }
    }

    @Test
    void testError403() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(ERROR_403.getBytes());
            parser.accept(CHUNK.formatted(0).getBytes());
            parser.accept(CRLF.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(0, content.length);
            byte[] headers = parser.getHeaders();
            assertEquals(ERROR_403, new String(headers));
        }
    }

    @Test
    void testError404() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(ERROR_404.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(0, content.length);
            byte[] headers = parser.getHeaders();
            assertEquals(ERROR_404, new String(headers));
        }
    }

    @Test
    void testError500() throws IOException {
        try (HttpPayloadParser parser = new HttpPayloadParser()) {
            parser.accept(ERROR_500.getBytes());
            assertTrue(parser.isComplete());
            byte[] content = parser.getContent();
            assertEquals(0, content.length);
            byte[] headers = parser.getHeaders();
            assertEquals(ERROR_500, new String(headers));
        }
    }
}
