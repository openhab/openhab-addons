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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * Loopback transport tests with real HTTP challenges and bounded responses.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Preemptive BASIC authentication tests
 * @author Andreas Vilippus - Transport encoding and security regression coverage
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
    void basicAuthenticatesFirstRequestWithoutChallenge() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<@Nullable String> authorization = new AtomicReference<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                if (requests.incrementAndGet() == 1) {
                    authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                }
                exchange.sendResponseHeaders(207, 2);
                exchange.getResponseBody().write("ok".getBytes(StandardCharsets.UTF_8));
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "BASIC");
            var client = new CalDavClient(http, config);
            assertEquals("ok", client.request("PROPFIND", URI.create(config.url), "", "0"));
            assertEquals(1, requests.get());
            assertEquals("Basic dXNlcjpzZWNyZXQ=", authorization.get());
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void basicAuthenticationAndRedirectDoNotLeakCredentials() throws Exception {
        checkBasicChallenge("BASIC");
    }

    private void checkBasicChallenge(String authType) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger redirected = new AtomicInteger();
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
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
            var config = config(server, authType);
            var client = new CalDavClient(http, config);
            assertEquals("ok", client.request("PROPFIND", URI.create(config.url), "", "0"));
            assertEquals("BASIC".equals(authType) ? 1 : 2, requests.get());
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
        checkDigestChallenge("DIGEST");
    }

    private void checkDigestChallenge(String authType) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
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
            var config = config(server, authType);
            assertEquals("",
                    new CalDavClient(http, config).request("REPORT", URI.create(config.url), "<report/>", "1"));
            assertEquals(2, requests.get());
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void autoAuthenticatesBasicAndDigestChallenges() throws Exception {
        checkBasicChallenge("AUTO");
        checkDigestChallenge("AUTO");
    }

    @Test
    void anonymousRequestDoesNotSendAuthorization() throws Exception {
        for (String authType : List.of("AUTO", "BASIC", "DIGEST")) {
            checkAnonymousRequest(authType, false);
        }
    }

    @Test
    void anonymousClientDoesNotAuthenticateWhenChallenged() throws Exception {
        for (String authType : List.of("AUTO", "BASIC", "DIGEST")) {
            checkAnonymousRequest(authType, true);
        }
    }

    private void checkAnonymousRequest(String authType, boolean challenge) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<@Nullable String> authorization = new AtomicReference<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                if (challenge && !"/public".equals(exchange.getRequestURI().getPath())) {
                    exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"calendar\"");
                    exchange.getResponseHeaders().add("WWW-Authenticate",
                            "Digest realm=\"calendar\", nonce=\"testnonce\", algorithm=MD5, qop=\"auth\"");
                    exchange.sendResponseHeaders(401, -1);
                } else {
                    exchange.sendResponseHeaders(207, 2);
                    exchange.getResponseBody().write("ok".getBytes(StandardCharsets.UTF_8));
                }
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, authType);
            config.username = "";
            config.password = "";
            CalDavConfiguration.validate(config);
            var client = new CalDavClient(http, config);
            if (challenge) {
                assertEquals(401, assertThrows(CalDavHttpException.class,
                        () -> client.request("PROPFIND", URI.create(config.url), "", "0")).statusCode());
            } else {
                assertEquals("ok", client.request("PROPFIND", URI.create(config.url), "", "0"));
            }
            assertNull(authorization.get());
            assertEquals(1, requests.get());
            assertEquals("ok", client.request("PROPFIND", URI.create(config.url + "public"), "", "0"));
            assertNull(authorization.get());
            assertEquals(2, requests.get());
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

    @Test
    void decodesDeclaredXmlCharsetsAndBomWithoutLosingCalendarText() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String content = "<d:multistatus xmlns:d=\"DAV:\"><d:displayname>Ärger</d:displayname></d:multistatus>";
        server.createContext("/latin", exchange -> {
            try (exchange) {
                byte[] data = ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + content)
                        .getBytes(StandardCharsets.ISO_8859_1);
                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=ISO-8859-1");
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.createContext("/utf16", exchange -> {
            try (exchange) {
                byte[] data = ("<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + content)
                        .getBytes(StandardCharsets.UTF_16);
                exchange.getResponseHeaders().set("Content-Type", "application/xml");
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.createContext("/bom-precedence", exchange -> {
            try (exchange) {
                byte[] data = ("<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + content)
                        .getBytes(StandardCharsets.UTF_16);
                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=UTF-8");
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.createContext("/declaration", exchange -> {
            try (exchange) {
                byte[] data = ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + content)
                        .getBytes(StandardCharsets.ISO_8859_1);
                exchange.getResponseHeaders().set("Content-Type", "application/xml");
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "AUTO");
            var client = new CalDavClient(http, config);
            for (String path : List.of("latin", "utf16", "declaration", "bom-precedence")) {
                var root = CalDavXml.parse(client.request("PROPFIND", URI.create(config.url + path), "", "0"))
                        .getDocumentElement();
                assertEquals("Ärger", DavResponse.text(root, "DAV:", "displayname"), path);
            }
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void interruptedRequestAbortsAndNextRequestRecovers() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var serverWorkers = Executors.newFixedThreadPool(2);
        server.setExecutor(serverWorkers);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        server.createContext("/blocked", exchange -> {
            try (exchange) {
                entered.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new IOException("Test request was not released");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                exchange.sendResponseHeaders(204, -1);
            }
        });
        server.createContext("/ok", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
        });
        var requester = Executors.newSingleThreadExecutor();
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "AUTO");
            var client = new CalDavClient(http, config);
            AtomicReference<@Nullable Thread> thread = new AtomicReference<>();
            var request = requester.submit(() -> {
                thread.set(Thread.currentThread());
                assertThrows(InterruptedException.class,
                        () -> client.request("GET", URI.create(config.url + "blocked"), "", "0"));
                assertTrue(Thread.currentThread().isInterrupted());
            });
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            Objects.requireNonNull(thread.get()).interrupt();
            request.get(10, TimeUnit.SECONDS);
            assertEquals("", client.request("GET", URI.create(config.url + "ok"), "", "0"));
        } finally {
            release.countDown();
            http.stop();
            server.stop(0);
            requester.shutdownNow();
            serverWorkers.shutdownNow();
            assertTrue(requester.awaitTermination(10, TimeUnit.SECONDS));
            assertTrue(serverWorkers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void utf8BasicChallengePreservesUnicodeCredentials() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");
                if (authorization == null) {
                    exchange.getResponseHeaders().set("WWW-Authenticate",
                            "Basic realm=\"calendar\", charset=\"UTF-8\"");
                    exchange.sendResponseHeaders(401, -1);
                } else {
                    String expected = "Basic "
                            + Base64.getEncoder().encodeToString("üser:päss".getBytes(StandardCharsets.UTF_8));
                    exchange.sendResponseHeaders(expected.equals(authorization) ? 204 : 403, -1);
                }
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "AUTO");
            config.username = "üser";
            config.password = "päss";
            assertEquals("", new CalDavClient(http, config).request("REPORT", URI.create(config.url), "", "0"));
            assertEquals(2, requests.get());
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void sha256DigestRenegotiatesAnExpiredCachedNonce() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> nonce = new AtomicReference<>("first");
        AtomicInteger successes = new AtomicInteger();
        server.createContext("/", exchange -> {
            try (exchange) {
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");
                Map<String, String> fields = new HashMap<>();
                if (authorization != null) {
                    var matcher = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^, ]+))").matcher(authorization);
                    while (matcher.find()) {
                        fields.put(matcher.group(1), Objects.requireNonNullElse(matcher.group(2), matcher.group(3)));
                    }
                }
                if (!nonce.get().equals(fields.get("nonce"))) {
                    exchange.getResponseHeaders().set("WWW-Authenticate", "Digest realm=\"calendar\", nonce=\""
                            + nonce.get() + "\", algorithm=SHA-256, qop=\"auth\", stale=true");
                    exchange.sendResponseHeaders(401, -1);
                } else {
                    try {
                        String expected = digest("SHA-256",
                                digest("SHA-256", "user:calendar:secret") + ":" + nonce.get() + ":" + fields.get("nc")
                                        + ":" + fields.get("cnonce") + ":auth:" + digest("SHA-256", "REPORT:/"));
                        boolean valid = expected.equals(fields.get("response"));
                        if (valid) {
                            successes.incrementAndGet();
                        }
                        exchange.sendResponseHeaders(valid ? 207 : 403, -1);
                    } catch (IOException e) {
                        exchange.sendResponseHeaders(500, -1);
                    }
                }
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "DIGEST");
            var client = new CalDavClient(http, config);
            assertEquals("", client.request("REPORT", URI.create(config.url), "", "0"));
            nonce.set("second");
            assertEquals("", client.request("REPORT", URI.create(config.url), "", "0"));
            assertEquals(2, successes.get());
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void timeoutDoesNotLeakCredentialsAndNextRequestRecovers() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var workers = Executors.newFixedThreadPool(2);
        server.setExecutor(workers);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        server.createContext("/timeout", exchange -> {
            try (exchange) {
                entered.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new IOException("Test server was not released");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                exchange.sendResponseHeaders(204, -1);
            }
        });
        server.createContext("/ok", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            var config = config(server, "BASIC");
            config.requestTimeout = 1;
            var client = new CalDavClient(http, config);
            IOException failure = assertThrows(IOException.class,
                    () -> client.request("GET", URI.create(config.url + "timeout"), "", "0"));
            assertEquals(0, entered.getCount());
            assertFalse(failure.toString().contains(config.password));
            assertFalse(failure.toString().contains("Authorization"));
            assertEquals("", client.request("GET", URI.create(config.url + "ok"), "", "0"));
        } finally {
            release.countDown();
            http.stop();
            server.stop(0);
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void validatesTlsTrustAndHostnameAndKeepsOptOutIsolated(@TempDir Path directory) throws Exception {
        Path store = directory.resolve("server.p12");
        Process keytool = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "test", "-keyalg", "RSA", "-keysize", "2048", "-storetype", "PKCS12",
                "-keystore", store.toString(), "-storepass", "fixture-password", "-keypass", "fixture-password",
                "-dname", "CN=127.0.0.1", "-ext", "SAN=ip:127.0.0.1", "-validity", "2", "-noprompt")
                .redirectErrorStream(true).redirectOutput(directory.resolve("keytool.log").toFile()).start();
        try {
            assertTrue(keytool.waitFor(20, TimeUnit.SECONDS), "Keytool generation timed out");
            assertEquals(0, keytool.exitValue());
        } finally {
            keytool.destroyForcibly();
        }
        KeyStore keys = KeyStore.getInstance("PKCS12");
        try (var input = Files.newInputStream(store)) {
            keys.load(input, "fixture-password".toCharArray());
        }
        KeyManagerFactory manager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        manager.init(keys, "fixture-password".toCharArray());
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(manager.getKeyManagers(), null, null);
        HttpsServer server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(context));
        server.createContext("/", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
        });
        SslContextFactory.Client verified = new SslContextFactory.Client();
        verified.setTrustStorePath(store.toString());
        verified.setTrustStorePassword("fixture-password");
        HttpClient trusted = new HttpClient(verified);
        HttpClient untrusted = new HttpClient(new SslContextFactory.Client());
        HttpClient insecure = new HttpClient(new SslContextFactory.Client(true));
        try {
            server.start();
            trusted.start();
            untrusted.start();
            insecure.start();
            AccountConfiguration config = new AccountConfiguration();
            config.url = "https://127.0.0.1:" + server.getAddress().getPort() + "/";
            assertThrows(IOException.class,
                    () -> new CalDavClient(untrusted, config).request("GET", URI.create(config.url), "", "0"));
            assertEquals("", new CalDavClient(trusted, config).request("GET", URI.create(config.url), "", "0"));
            config.url = "https://localhost:" + server.getAddress().getPort() + "/";
            assertThrows(IOException.class,
                    () -> new CalDavClient(trusted, config).request("GET", URI.create(config.url), "", "0"));
            assertEquals("", new CalDavClient(insecure, config).request("GET", URI.create(config.url), "", "0"));
            assertThrows(IOException.class,
                    () -> new CalDavClient(trusted, config).request("GET", URI.create(config.url), "", "0"));
        } finally {
            trusted.stop();
            untrusted.stop();
            insecure.stop();
            server.stop(0);
        }
    }

    @Test
    void malformedErrorEncodingKeepsHttpStatus() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                int status = Integer.parseInt(exchange.getRequestURI().getPath().substring(1));
                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=unknown-fixture-charset");
                if (status == 401) {
                    exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"calendar\"");
                }
                exchange.sendResponseHeaders(status, 3);
                exchange.getResponseBody().write("bad".getBytes(StandardCharsets.UTF_8));
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            AccountConfiguration config = new AccountConfiguration();
            config.url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            var client = new CalDavClient(http, config);
            for (int status : List.of(401, 403, 405, 501)) {
                CalDavHttpException failure = assertThrows(CalDavHttpException.class,
                        () -> client.request("REPORT", URI.create(config.url + status), "", "0"));
                assertEquals(status, failure.statusCode());
                assertFalse(failure.invalidSyncToken());
            }
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    private static String digest(String algorithm, String text) throws IOException {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance(algorithm).digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
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
