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
package org.openhab.binding.network.internal.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests cases for {@link NetworkUtils}.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
public class NetworkUtilsTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final NetworkUtils networkUtils = new NetworkUtils();

    private @NonNullByDefault({}) HttpServer server;
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) String baseUrl;
    private boolean serverStopped;

    @BeforeEach
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ok", exchange -> respond(exchange, 200));
        server.createContext("/notfound", exchange -> respond(exchange, 404));
        server.createContext("/moved", exchange -> {
            exchange.getResponseHeaders().add("Location", "/ok");
            respond(exchange, 302);
        });
        server.start();
        baseUrl = "http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":" + server.getAddress().getPort();

        httpClient = new HttpClient();
        // Redirects must not be followed by httpPing even if the client is configured to follow them
        httpClient.setFollowRedirects(true);
        httpClient.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        httpClient.stop();
        stopServer();
    }

    @Test
    public void httpPingReturnsSuccessStatusCode() throws InterruptedException {
        HttpPingResult result = networkUtils.httpPing(httpClient, URI.create(baseUrl + "/ok"), TIMEOUT);

        assertNotNull(result);
        assertThat(result.statusCode(), is(200));
    }

    @Test
    public void httpPingReturnsErrorStatusCode() throws InterruptedException {
        HttpPingResult result = networkUtils.httpPing(httpClient, URI.create(baseUrl + "/notfound"), TIMEOUT);

        assertNotNull(result);
        assertThat(result.statusCode(), is(404));
    }

    @Test
    public void httpPingDoesNotFollowRedirects() throws InterruptedException {
        HttpPingResult result = networkUtils.httpPing(httpClient, URI.create(baseUrl + "/moved"), TIMEOUT);

        assertNotNull(result);
        assertThat(result.statusCode(), is(302));
    }

    @Test
    public void httpPingWithoutResponse() throws Exception {
        stopServer();

        assertNull(networkUtils.httpPing(httpClient, URI.create(baseUrl + "/ok"), TIMEOUT));
    }

    private void stopServer() {
        if (!serverStopped) {
            serverStopped = true;
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, int statusCode) throws IOException {
        byte[] body = "presence detection".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
