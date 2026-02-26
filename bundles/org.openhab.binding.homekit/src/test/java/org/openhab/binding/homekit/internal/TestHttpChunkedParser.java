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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    void testValidChunkedPayload() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(header, new String(httpPayload.headers()));
        assertEquals("123456789123456789abcdef", new String(httpPayload.content()));

        parser.close();
        feeder.close();
    }

    @Test
    void testBadChunkedSizePayload() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        assertThrows(Exception.class, () -> futureHttpPayload.get(1, TimeUnit.SECONDS));

        parser.close();
        feeder.close();
    }

    @Test
    void testValidChunkedPayloadWithSplitFrames() throws Exception {
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

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);
        assertEquals(header, new String(httpPayload.headers()));
        assertEquals("123456789123456789abcdef", new String(httpPayload.content()));

        parser.close();
        feeder.close();
    }

    @Test
    void testChunkedPayloadWithEmptyLines() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                s0.getBytes(),
                s1.getBytes(),
                s2.getBytes(),
                s3.getBytes(),
                crlf.getBytes(),          // end of headers
                "\r\n".getBytes(),        // <-- empty line before chunk-size
                "\r\n".getBytes(),        // <-- another empty line
                s5.getBytes(),            // "09\r\n"  (chunk size = 9)
                s6.getBytes(),            // "123456789\r\n"
                "\r\n".getBytes(),        // <-- empty line before next chunk-size
                s7.getBytes(),            // "0f\r\n"  (chunk size = 15)
                s8.getBytes(),            // "123456789abcdef\r\n"
                "\r\n".getBytes(),        // <-- empty line before terminating chunk
                s9.getBytes(),            // "0\r\n"
                crlf.getBytes()           // final CRLF
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        HttpPayload httpPayload = futureHttpPayload.get(1, TimeUnit.SECONDS);

        assertEquals(header, new String(httpPayload.headers()));
        assertEquals("123456789123456789abcdef", new String(httpPayload.content()));

        parser.close();
        feeder.close();
    }

    @Test
    void testIncompleteChunkedPayload() throws Exception {
        List<byte[]> parts = List.of(
        // @formatter:off
                s0.getBytes(),
                s1.getBytes(),
                s2.getBytes(),
                s3.getBytes(),
                crlf.getBytes(),   // end of headers
                s5.getBytes(),     // "09\r\n"
                s6.getBytes(),     // "123456789\r\n"
                s7.getBytes(),     // "0f\r\n"
                s8.getBytes(),     // "123456789abcdef\r\n"
                s9.getBytes()      // "0\r\n"  <-- TERMINATING CHUNK
                // MISSING FINAL CRLF HERE
        // @formatter:on
        );

        ParserTestStreamFeeder feeder = new ParserTestStreamFeeder();
        ParserTestHarness harness = new ParserTestHarness();
        HttpPayloadParser parser = new HttpPayloadParser(feeder.in, harness);
        parser.start();

        CompletableFuture<HttpPayload> futureHttpPayload = harness.expectPayload();
        feeder.feedAll(parts);

        // The parser must NOT emit a payload because the message is incomplete.
        assertThrows(Exception.class, () -> futureHttpPayload.get(500, TimeUnit.MILLISECONDS),
                "Parser should not emit incomplete chunked payload");

        parser.close();
        feeder.close();
    }
}
