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
package org.openhab.binding.caldav.internal.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;

import com.sun.net.httpserver.HttpServer;

/**
 * Loopback transport tests with real HTTP challenges and bounded responses.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class CalDavClientTest {
    private AccountConfiguration config(HttpServer server, String authType) {
        AccountConfiguration result = new AccountConfiguration();
        result.url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        result.username = "user";
        result.password = "secret";
        result.authType = authType;
        result.requestTimeout = 10;
        return result;
    }

    @Test
    void basicChallengeAndRedirectDoNotLeakCredentials() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger redirected = new AtomicInteger();
        server.createContext("/", exchange -> {
            try (exchange) {
                String auth = exchange.getRequestHeaders().getFirst("Authorization");
                if (auth == null) {
                    exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"calendar\"");
                    exchange.sendResponseHeaders(401, -1);
                } else if (("Basic "
                        + Base64.getEncoder().encodeToString("user:secret".getBytes(StandardCharsets.UTF_8)))
                        .equals(auth)) {
                    exchange.sendResponseHeaders(200, 2);
                    exchange.getResponseBody().write("ok".getBytes(StandardCharsets.UTF_8));
                } else {
                    exchange.sendResponseHeaders(403, -1);
                }
            }
        });
        server.createContext("/redirect", exchange -> {
            try (exchange) {
                exchange.getResponseHeaders().set("Location", "/target");
                exchange.sendResponseHeaders(302, -1);
            }
        });
        server.createContext("/target", exchange -> {
            try (exchange) {
                redirected.incrementAndGet();
                exchange.sendResponseHeaders(200, -1);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "BASIC");
            var client = new CalDavClient(http, config);
            assertEquals("ok", client.request("PROPFIND", URI.create(config.url), "", "0"));
            assertEquals(302, assertThrows(CalDavHttpException.class,
                    () -> client.request("GET", URI.create(config.url + "redirect"), "", "0")).statusCode());
            assertEquals(0, redirected.get());
            assertThrows(IllegalArgumentException.class,
                    () -> client.request("GET", URI.create("https://foreign.example/"), "", "0"));
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void digestChallengeAuthenticatesReportWithCorrectMethod() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                String auth = exchange.getRequestHeaders().getFirst("Authorization");
                if (auth == null) {
                    exchange.getResponseHeaders().set("WWW-Authenticate",
                            "Digest realm=\"calendar\", nonce=\"testnonce\", algorithm=MD5, qop=\"auth\"");
                    exchange.sendResponseHeaders(401, -1);
                } else {
                    Map<String, String> fields = new HashMap<>();
                    var matcher = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^, ]+))").matcher(auth);
                    while (matcher.find()) {
                        fields.put(matcher.group(1), Objects.requireNonNullElse(matcher.group(2), matcher.group(3)));
                    }
                    String expected = md5(md5("user:calendar:secret") + ":testnonce:" + fields.get("nc") + ":"
                            + fields.get("cnonce") + ":auth:" + md5("REPORT:/"));
                    exchange.sendResponseHeaders(
                            auth.startsWith("Digest ") && expected.equals(fields.get("response")) ? 207 : 403, -1);
                }
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "DIGEST");
            assertEquals("",
                    new CalDavClient(http, config).request("REPORT", URI.create(config.url), "<report/>", "1"));
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void oversizedResponseFailsAndNextRequestRecovers() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(200, CalDavClient.MAX_RESPONSE_BYTES + 1L);
            }
        });
        server.createContext("/small", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "AUTO");
            var client = new CalDavClient(http, config);
            assertThrows(IOException.class, () -> client.request("GET", URI.create(config.url), "", "0"));
            assertEquals("", client.request("GET", URI.create(config.url + "small"), "", "0"));
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    private static String md5(String text) throws IOException {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}
