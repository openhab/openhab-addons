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
package org.openhab.binding.candy.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.protocol.CandyCodec;
import org.openhab.binding.candy.internal.protocol.CandyInvalidHostException;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Exercises the real HTTP stack against an isolated fixture server, never a physical appliance.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BiancaLocalClientTest {
    private final HttpClient http = new HttpClient();
    private final BiancaLocalClient client = new BiancaLocalClient(http);
    private final HttpServer server;

    BiancaLocalClientTest() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    }

    @BeforeEach
    void start() throws Exception {
        server.start();
        http.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.stop(0);
        http.stop();
    }

    @Test
    void writesOneEncryptedRequestWithoutFollowingRedirectsOrSendingCredentials() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<String> route = new AtomicReference<>();
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            route.set(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            assertNull(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.getResponseHeaders().add("Location", "/unexpected-replay");
            respond(exchange, 302, "");
        });
        BiancaConfiguration settings = configuration();
        settings.username = "account-not-for-lan";
        settings.password = "password-not-for-lan";
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.write(settings, "Write=1&StSt=0").get(15, TimeUnit.SECONDS));
        assertEquals(message("@text/communication-error-http"),
                assertInstanceOf(CandyProtocolException.class, failure.getCause()).getMessage());
        assertEquals("GET /http-write.json?encrypted=1&data=36100A10005B564E3A1E3818505E", route.get());
        assertEquals(1, requests.get());
        assertTrue(http.isRunning());
    }

    @Test
    void writeTimeoutDoesNotReplayAndClientRemainsUsable() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            holdResponse(exchange, entered, release, "{}");
        });
        var settings = configuration();
        settings.requestTimeout = 1;
        try {
            var request = client.write(settings, "Write=1&StSt=0");
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> request.get(10, TimeUnit.SECONDS));
            assertEquals(message("@text/communication-error-timeout"),
                    assertInstanceOf(CandyProtocolException.class, failure.getCause()).getMessage());
            assertEquals(1, requests.get());
            assertTrue(http.isRunning());
        } finally {
            release.countDown();
        }
    }

    @Test
    void cancellingAWriteAbortsOnlyThatRequest() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        server.createContext("/", exchange -> holdResponse(exchange, entered, release, "{}"));
        try {
            var request = client.write(configuration(), "Write=1&StSt=0");
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            assertTrue(request.cancel(true));
            assertTrue(request.isCancelled());
            assertTrue(http.isRunning());
        } finally {
            release.countDown();
        }
    }

    @Test
    void httpSuccessDoesNotRequireOrPublishAnUnverifiedResponseBody() throws Exception {
        server.createContext("/", exchange -> respond(exchange, 200, "device-specific acknowledgement"));
        assertEquals(200, client.write(configuration(), "Write=1&StSt=0").get(15, TimeUnit.SECONDS));
    }

    @Test
    void readsOnlyTheFixedStatusRoute() throws Exception {
        String wire = CandyCodec.encode("{\"statusLavatrice\":{\"MachMd\":2}}", "abcdefghijklmnop");
        AtomicReference<String> route = new AtomicReference<>();
        server.createContext("/", exchange -> {
            route.set(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            respond(exchange, 200, wire);
        });
        var snapshot = client.read(configuration()).get(15, TimeUnit.SECONDS);
        assertEquals(new StringType("RUNNING"), snapshot.states().get("machine-state"));
        assertEquals("GET /http-read.json?encrypted=1", route.get());
        assertTrue(http.isStarted());
    }

    @Test
    void automaticallyMatchesAnAccountKeyWithOneLocalRequest() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        String wire = CandyCodec.encode("{\"statusLavatrice\":{\"MachMd\":2}}", "abcdefghijklmnop");
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            assertEquals(null, exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, 200, wire);
        });
        BiancaConfiguration configuration = configuration();
        configuration.encryptionKey = "";
        var snapshot = client.read(configuration, List.of("ponmlkjihgfedcba", "abcdefghijklmnop")).get(15,
                TimeUnit.SECONDS);
        assertEquals(new StringType("RUNNING"), snapshot.states().get("machine-state"));
        assertEquals("abcdefghijklmnop", configuration.encryptionKey);
        assertEquals(1, requests.get());
    }

    @Test
    void debugLogsDescribeKeyMatchingWithoutExposingKeysOrPayloads() throws Exception {
        String wire = CandyCodec.encode("{\"statusLavatrice\":{\"MachMd\":2}}", "abcdefghijklmnop");
        server.createContext("/", exchange -> respond(exchange, 200, wire));
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        BiancaLocalClient loggedClient;
        try (var factory = mockStatic(LoggerFactory.class, CALLS_REAL_METHODS)) {
            factory.when(() -> LoggerFactory.getLogger(BiancaLocalClient.class)).thenReturn(logger);
            loggedClient = new BiancaLocalClient(http);
        }
        BiancaConfiguration configuration = configuration();
        configuration.encryptionKey = "";
        loggedClient.read(configuration, List.of("ponmlkjihgfedcba", "abcdefghijklmnop")).get(15, TimeUnit.SECONDS);
        String messages = mockingDetails(logger).getInvocations().stream()
                .map(invocation -> invocation.getMethod().getName() + Arrays.deepToString(invocation.getArguments()))
                .collect(Collectors.joining("\n"));
        assertTrue(messages.contains("Matched an account key"));
        assertTrue(messages.contains("Decoded usable local status"));
        for (String secret : List.of("abcdefghijklmnop", "ponmlkjihgfedcba", wire, "statusLavatrice", "MachMd")) {
            assertFalse(messages.contains(secret), "Sensitive fixture data appeared in local diagnostics");
        }
    }

    @Test
    void doesNotRetainAnAccountKeyWhenNoCandidateDecodes() throws CandyProtocolException {
        String wire = CandyCodec.encode("{\"statusLavatrice\":{\"MachMd\":2}}", "abcdefghijklmnop");
        server.createContext("/", exchange -> respond(exchange, 200, wire));
        BiancaConfiguration configuration = configuration();
        configuration.encryptionKey = "";
        ExecutionException failure = assertThrows(ExecutionException.class, () -> client
                .read(configuration, List.of("ponmlkjihgfedcba", "1234567890123456")).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/account-error-key-match"), cause.getMessage());
        assertEquals("", configuration.encryptionKey);
    }

    @Test
    void refusesRedirects() {
        AtomicInteger redirected = new AtomicInteger();
        server.createContext("/http-read.json", exchange -> {
            exchange.getResponseHeaders().add("Location", "/unexpected");
            respond(exchange, 302, "redirect");
        });
        server.createContext("/unexpected", exchange -> {
            redirected.incrementAndGet();
            respond(exchange, 200, "unexpected");
        });
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/communication-error-http"), cause.getMessage());
        assertEquals(0, redirected.get());
    }

    @Test
    void boundsResponseSize() {
        server.createContext("/", exchange -> respond(exchange, 200, "A".repeat(CandyCodec.MAX_WIRE_BYTES + 1)));
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/communication-error-transport"), cause.getMessage());
    }

    @Test
    void sanitizesInvalidResponsesAndHttpFailures() {
        server.createContext("/", exchange -> respond(exchange, 200, "private-invalid-body"));
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/protocol-error-hex"), cause.getMessage());
        assertNull(cause.getCause());
    }

    @Test
    void cancellationAbortsAnInFlightRead() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        server.createContext("/", exchange -> holdResponse(exchange, entered, release));
        var future = client.read(configuration());
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            assertTrue(future.cancel(true));
            assertTrue(future.isCancelled());
            assertTrue(http.isStarted());
        } finally {
            release.countDown();
        }
    }

    @Test
    void deadlineCompletesAStalledRead() {
        CountDownLatch release = new CountDownLatch(1);
        server.createContext("/", exchange -> holdResponse(exchange, new CountDownLatch(0), release));
        BiancaConfiguration configuration = configuration();
        configuration.requestTimeout = 1;
        try {
            ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> client.read(configuration).get(15, TimeUnit.SECONDS));
            var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
            assertEquals(message("@text/communication-error-timeout"), cause.getMessage());
            assertInstanceOf(TimeoutException.class, cause.getCause());
        } finally {
            release.countDown();
        }
    }

    @Test
    void rejectsTruncatedResponses() {
        server.createContext("/", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(200, 100);
                exchange.getResponseBody().write('0');
            }
        });
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/communication-error-transport"), cause.getMessage());
    }

    @Test
    void rejectsEmptyResponses() {
        server.createContext("/", exchange -> respond(exchange, 200, ""));
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/protocol-error-hex"), cause.getMessage());
    }

    @Test
    void retainsTheKeyCapturedWhenTheRequestStarted() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        String wire = CandyCodec.encode("{\"statusLavatrice\":{\"MachMd\":2}}", "abcdefghijklmnop");
        server.createContext("/", exchange -> holdResponse(exchange, entered, release, wire));
        BiancaConfiguration configuration = configuration();
        var future = client.read(configuration);
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            configuration.encryptionKey = "ponmlkjihgfedcba";
            release.countDown();
            assertEquals(new StringType("RUNNING"), future.get(15, TimeUnit.SECONDS).states().get("machine-state"));
        } finally {
            release.countDown();
        }
    }

    @Test
    void unavailableClientStillCompletesTheOperation() throws Exception {
        http.stop();
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/communication-error-client"), cause.getMessage());
        assertInstanceOf(RejectedExecutionException.class, cause.getCause());
    }

    @Test
    void rejectsInvalidHostWithoutCreatingAnHttpRequest() {
        HttpClient unusedHttpClient = mock(HttpClient.class);
        BiancaConfiguration configuration = new BiancaConfiguration();
        configuration.hostName = "washer.local?secret=1";
        configuration.encryptionKey = "abcdefghijklmnop";
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> new BiancaLocalClient(unusedHttpClient).read(configuration).get(15, TimeUnit.SECONDS));
        assertInstanceOf(CandyInvalidHostException.class, failure.getCause());
        verifyNoInteractions(unusedHttpClient);
    }

    @Test
    void requestCreationFailureCompletesTheOperation() {
        HttpClient unavailableHttpClient = mock(HttpClient.class);
        IllegalStateException unavailable = new IllegalStateException("private transport details");
        when(unavailableHttpClient.newRequest(any(URI.class))).thenThrow(unavailable);
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> new BiancaLocalClient(unavailableHttpClient).read(configuration()).get(15, TimeUnit.SECONDS));
        var cause = assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertEquals(message("@text/communication-error-client"), cause.getMessage());
        assertSame(unavailable, cause.getCause());
    }

    private BiancaConfiguration configuration() {
        BiancaConfiguration configuration = new BiancaConfiguration() {
            @Override
            public Optional<URI> statusUri() {
                return Optional.of(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/http-read.json"));
            }
        };
        configuration.encryptionKey = "abcdefghijklmnop";
        return configuration;
    }

    private void respond(HttpExchange exchange, int code, String body) throws IOException {
        try (exchange) {
            byte[] bytes = body.getBytes(StandardCharsets.US_ASCII);
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
    }

    private void holdResponse(HttpExchange exchange, CountDownLatch entered, CountDownLatch release)
            throws IOException {
        holdResponse(exchange, entered, release, "00");
    }

    private void holdResponse(HttpExchange exchange, CountDownLatch entered, CountDownLatch release, String body)
            throws IOException {
        entered.countDown();
        try {
            if (release.await(25, TimeUnit.SECONDS)) {
                respond(exchange, 200, body);
            } else {
                exchange.close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exchange.close();
        }
    }

    private String message(String reference) {
        return ResourceBundle.getBundle("OH-INF.i18n.candy", Locale.getDefault())
                .getString(reference.substring("@text/".length()));
    }
}
