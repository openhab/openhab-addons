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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private static final String ERROR_403 = "HTTP/1.1 403 Forbidden\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\r\nConnection: close\r\n\r\n";
    private static final String ERROR_404 = "HTTP/1.1 404 Not Found\r\nDate: Tue, 07 Oct 2025 14:00:00 GMT\r\nConnection: close\r\n\r\n";
    private static final String ERROR_500 = "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";

    private static final String CONTENT = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    private static final String CHUNK_1 = "%x\r";
    private static final String CHUNK_2 = "\n";
    private static final String CHUNK = CHUNK_1 + CHUNK_2;
    private static final String CRLF = "\r\n";

    private static final String THIS_SHOULD_NOT_BE_TREATED_AS_BODY = "THIS_SHOULD_NOT_BE_TREATED_AS_BODY";

    @Test
    void testHttpWithChunkedContentOk() throws Exception {
        String h = HEADERS_A + HEADERS_C + HEADERS_Z;
        String hc = h + "64\r\n" + CONTENT + CRLF + "0\r\n\r\n";

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feed(hc.getBytes());

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(h, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithChunkedContentOkManyPartial() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(HEADERS_A + HEADERS_C + HEADERS_Z, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithChunkedContentOkManyPartialAndSplitChunkHeader() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        String h = HEADERS_A + HEADERS_C + HEADERS_Z;
        assertEquals(h, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithContentDiscardExtra() throws Exception {
        String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
        String hc = h + CONTENT + "EXTRA";

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feed(hc.getBytes());

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(h, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithContentManyPartialOk() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithContentManyPartialOkAndSplitCRLF() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithContentOk() throws Exception {
        String h = HEADERS_A + HEADERS_B.formatted(100) + HEADERS_Z;
        String hc = h + CONTENT;

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feed(hc.getBytes());

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload.content().length);
        assertEquals(CONTENT, new String(httpPayload.content()));
        assertEquals(h, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithMultipleFrames() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        // first payload
        CompletableFuture<HttpPayload> futureHttpPayload1 = harness.expectPayload();
        feeder.feedAll(parts.subList(0, 4));
        HttpPayload httpPayload1 = futureHttpPayload1.get(1, TimeUnit.SECONDS);
        assertEquals(100, httpPayload1.content().length);

        // second payload
        CompletableFuture<HttpPayload> futureHttpPayload2 = harness.expectPayload();
        feeder.feedAll(parts.subList(4, 6));
        HttpPayload httpPayload2 = futureHttpPayload2.get(1, TimeUnit.SECONDS);
        assertEquals(50, httpPayload2.content().length);

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithZeroContentLength() throws Exception {
        String h = HEADERS_A + HEADERS_B.formatted(0) + HEADERS_Z;
        String hc = h + CONTENT; // parser must ignore body entirely

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feed(hc.getBytes());

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(0, httpPayload.content().length);
        assertEquals(h, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testOk204() throws Exception {
        List<byte[]> parts = List.of(OK_204.getBytes());

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(0, httpPayload.content().length);
        assertEquals(OK_204, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testError403() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_403.substring(0, 15).getBytes(),
                ERROR_403.substring(15).getBytes()
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(0, httpPayload.content().length);
        assertEquals(ERROR_403, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testError404() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_404.substring(0, 20).getBytes(),
                ERROR_404.substring(20).getBytes()
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(0, httpPayload.content().length);
        assertEquals(ERROR_404, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testError500() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                ERROR_500.substring(0, 22).getBytes(),
                ERROR_500.substring(22).getBytes()
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(0, httpPayload.content().length);
        assertEquals(ERROR_500, new String(httpPayload.headers()));

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithNoContentLength() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.getBytes(), 
                HEADERS_Z.getBytes(), 
                THIS_SHOULD_NOT_BE_TREATED_AS_BODY.getBytes()
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> future = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload payload = future.get(1, TimeUnit.SECONDS);
        assertEquals(HEADERS_A + HEADERS_Z, new String(payload.headers()));
        assertEquals(0, payload.content().length);

        parser.close();
        feeder.close();
    }

    @Test
    void testHttpWithWrongContentLength() throws Exception {
        int declaredLength = 10;
        List<byte[]> parts = List.of(
        // @formatter:off
                HEADERS_A.getBytes(), 
                HEADERS_B.formatted(declaredLength).getBytes(),
                HEADERS_Z.getBytes(),
                CONTENT.getBytes()
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> future = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload payload = future.get(1, TimeUnit.SECONDS);
        assertEquals(HEADERS_A + HEADERS_B.formatted(declaredLength) + HEADERS_Z, new String(payload.headers()));
        assertEquals(CONTENT.substring(0, declaredLength), new String(payload.content()));
        assertEquals(declaredLength, payload.content().length);

        parser.close();
        feeder.close();
    }
}
