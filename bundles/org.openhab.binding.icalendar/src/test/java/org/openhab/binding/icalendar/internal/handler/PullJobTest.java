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
package org.openhab.binding.icalendar.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * Tests of the digest authentication handling of {@link PullJob} with local test servers.
 *
 * @author Contributors - Initial contribution
 */
@NonNullByDefault
public class PullJobTest {
    private static final String DIGEST_CHALLENGE = "Digest realm=\"test\", nonce=\"abc123\", qop=\"auth\", algorithm=MD5";
    private static final String CALENDAR = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//test//EN\r\nEND:VCALENDAR\r\n";

    private @Nullable HttpServer origin;
    private @Nullable HttpServer foreign;
    private @Nullable HttpClient httpClient;
    private @Nullable File destination;

    @BeforeEach
    public void setUp() throws Exception {
        HttpClient client = new HttpClient();
        client.start();
        httpClient = client;
        destination = Files.createTempFile("pulljobtest", ".ics").toFile();
        Files.deleteIfExists(destination.toPath());
    }

    @AfterEach
    public void tearDown() throws Exception {
        HttpServer server = origin;
        if (server != null) {
            server.stop(0);
        }
        server = foreign;
        if (server != null) {
            server.stop(0);
        }
        HttpClient client = httpClient;
        if (client != null) {
            client.stop();
        }
        File file = destination;
        if (file != null) {
            Files.deleteIfExists(file.toPath());
        }
    }

    private HttpServer startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();
        return server;
    }

    private void runJob(URI source) {
        new PullJob(Objects.requireNonNull(httpClient), source, "user", "secret", Objects.requireNonNull(destination),
                100_000, () -> {
                }, null).run();
    }

    @Test
    public void testDigestIsNotSentToOtherOriginAfterRedirect() throws Exception {
        final AtomicReference<@Nullable String> foreignAuthorization = new AtomicReference<>();
        final AtomicInteger foreignRequests = new AtomicInteger();
        HttpServer foreignServer = startServer();
        foreign = foreignServer;
        foreignServer.createContext("/cal.ics", exchange -> {
            foreignRequests.incrementAndGet();
            foreignAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.getResponseHeaders().add("WWW-Authenticate", DIGEST_CHALLENGE);
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        HttpServer originServer = startServer();
        origin = originServer;
        final String redirectTarget = "http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":"
                + foreignServer.getAddress().getPort() + "/cal.ics";
        originServer.createContext("/cal.ics", exchange -> {
            exchange.getResponseHeaders().add("Location", redirectTarget);
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        runJob(URI.create("http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":"
                + originServer.getAddress().getPort() + "/cal.ics"));

        assertEquals(1, foreignRequests.get(), "Redirect target should be requested exactly once (no retry)");
        assertEquals(null, foreignAuthorization.get(), "No credentials may reach the redirect target");
        assertFalse(Objects.requireNonNull(destination).exists());
    }

    @Test
    public void testDigestIsSentToConfiguredOrigin() throws Exception {
        final AtomicReference<@Nullable String> authorization = new AtomicReference<>();
        HttpServer originServer = startServer();
        origin = originServer;
        originServer.createContext("/cal.ics", exchange -> {
            String header = exchange.getRequestHeaders().getFirst("Authorization");
            if (header != null && header.startsWith("Digest ")) {
                authorization.set(header);
                byte[] body = CALENDAR.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } else {
                exchange.getResponseHeaders().add("WWW-Authenticate", DIGEST_CHALLENGE);
                exchange.sendResponseHeaders(401, -1);
            }
            exchange.close();
        });

        runJob(URI.create("http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":"
                + originServer.getAddress().getPort() + "/cal.ics"));

        String header = authorization.get();
        assertTrue(header != null && header.contains("username=\"user\""));
        assertTrue(Objects.requireNonNull(destination).exists());
    }
}
