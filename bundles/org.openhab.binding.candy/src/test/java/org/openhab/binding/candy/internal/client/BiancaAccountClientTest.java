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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.protocol.CandyApplianceSelectionException;
import org.openhab.binding.candy.internal.protocol.CandyAuthenticationException;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Exercises cookie-isolated web login and key retrieval against synthetic local services, never a real account.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BiancaAccountClientTest {
    private final HttpClient http = new HttpClient();
    private final HttpServer server;
    private final URI base;
    private final BiancaAccountClient client;
    private final ConcurrentLinkedQueue<Throwable> fixtureFailures = new ConcurrentLinkedQueue<>();
    private final CountDownLatch stopped = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private final CountDownLatch entered = new CountDownLatch(1);
    private final AtomicInteger posts = new AtomicInteger();
    private final AtomicInteger inventories = new AtomicInteger();
    private volatile String inventory = "[{\"appliance\":{\"id\":\"1\",\"encryption_key\":\"abcdefghijklmnop\"}}]";
    private volatile String detail = "{\"appliance\":{\"id\":\"1\",\"encryption_key\":\"ponmlkjihgfedcba\"}}";
    private volatile String state = "";
    private volatile boolean rejectedLogin;
    private volatile boolean unsafeAction;
    private volatile boolean unsafeRedirect;
    private volatile boolean wrongState;
    private volatile boolean stall;
    private volatile boolean cancellationExpected;
    private volatile int inventoryStatus = 200;
    private volatile int detailStatus = 200;

    BiancaAccountClientTest() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        client = new BiancaAccountClient(http, URI.create(base + "/CandyApp"), base);
        http.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStopped(@Nullable LifeCycle event) {
                stopped.countDown();
            }
        });
    }

    @BeforeEach
    void start() {
        server.createContext("/", exchange -> {
            try {
                route(exchange);
            } catch (IOException failure) {
                if (!cancellationExpected) {
                    fixtureFailures.add(failure);
                }
                exchange.close();
            } catch (Exception | AssertionError failure) {
                fixtureFailures.add(failure);
                respond(exchange, 500, "fixture failure");
            }
        });
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        release.countDown();
        server.stop(0);
        http.stop();
        assertTrue(fixtureFailures.isEmpty(), () -> "Fixture failures: " + fixtureFailures);
    }

    @Test
    void retrievesProgramsOnlyForTheKeyMatchedToTheLocalWasher() throws Exception {
        inventory = """
                [{"appliance":{"id":"other","interface_type":"Bianca","encryption_key":"ponmlkjihgfedcba",
                  "programs":[{"program":{"id":"wrong","name":"Wrong appliance"}}]}},
                 {"appliance":{"id":"matched","interface_type":"Bianca","encryption_key":"abcdefghijklmnop",
                  "programs":[{"program":{"id":"cottons","name":"Cottons"}}]}}]
                """;
        var programs = client.fetchPrograms(configuration(), "abcdefghijklmnop").get(15, TimeUnit.SECONDS);
        assertEquals(1, programs.size());
        assertEquals("cottons", programs.getFirst().id());
        assertTrue(http.isStopped());
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.fetchPrograms(configuration(), "0000000000000000").get(15, TimeUnit.SECONDS));
        assertInstanceOf(CandyApplianceSelectionException.class, failure.getCause());
    }

    @Test
    void unavailableForeignEnrichmentDoesNotHideTheMatchedCatalog() throws Exception {
        inventory = """
                [{"appliance":{"id":"foreign"}},
                 {"appliance":{"id":"matched","interface_type":"Bianca","encryption_key":"abcdefghijklmnop",
                  "programs":[{"program":{"id":"cottons","name":"Cottons"}}]}}]
                """;
        detailStatus = 503;
        assertEquals("cottons",
                client.fetchPrograms(configuration(), "abcdefghijklmnop").get(15, TimeUnit.SECONDS).getFirst().id());
    }

    @Test
    void enrichesCatalogWithoutUsingProgramsFromAnotherInterface() throws Exception {
        inventory = "[{\"appliance\":{\"id\":\"1\",\"encryption_key\":\"abcdefghijklmnop\"}}]";
        detail = """
                {"appliance":{"id":"1","interface_type":"SmartTouch","encryption_key":"abcdefghijklmnop","programs":[]}}
                """;
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.fetchPrograms(configuration(), "abcdefghijklmnop").get(15, TimeUnit.SECONDS));
        assertEquals(message("@text/catalog-error-interface"),
                assertInstanceOf(CandyProtocolException.class, failure.getCause()).getMessage());
    }

    @Test
    void logsInWithCredentialsAndReadsExistingKeys() throws Exception {
        assertEquals(List.of("abcdefghijklmnop"), client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
        assertEquals(1, posts.get());
        assertEquals(1, inventories.get());
        assertTrue(http.isStopped());
    }

    @Test
    void debugLogsDescribeSetupWithoutExposingSecrets() throws Exception {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        var loggedClient = clientWithLogger(logger);
        loggedClient.fetchKeys(configuration()).get(15, TimeUnit.SECONDS);
        String messages = recordedMessages(logger);
        assertTrue(messages.contains("Starting Salesforce account authorization"));
        assertTrue(messages.contains("Account HTTP response"));
        assertTrue(messages.contains("Account key lookup completed"));
        assertFalse(mockingDetails(logger).getInvocations().stream()
                .anyMatch(invocation -> "warn".equals(invocation.getMethod().getName())));
        for (String secret : List.of("owner@example.test", "owner%40example.test", "p& +ass", "p%26",
                "abcdefghijklmnop", "fixture+refresh", "fixture%2Brefresh", "fixture-bearer", "fixture-source",
                "session=fixture", state)) {
            assertFalse(messages.contains(secret), "Sensitive fixture data appeared in account diagnostics");
        }
    }

    @Test
    void warnsAboutBlockedRedirectsWithoutLoggingTheirDestination() {
        unsafeRedirect = true;
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        var loggedClient = clientWithLogger(logger);
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> loggedClient.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
        assertInstanceOf(CandyProtocolException.class, failure.getCause());
        assertTrue(mockingDetails(logger).getInvocations().stream()
                .anyMatch(invocation -> "warn".equals(invocation.getMethod().getName())));
        String messages = recordedMessages(logger);
        assertFalse(messages.contains("untrusted.invalid"));
        assertFalse(messages.contains("p& +ass"));
        assertFalse(messages.contains("owner@example.test"));
    }

    @Test
    void retriesWithFreshCookiesAfterATemporaryFailure() throws Exception {
        inventoryStatus = 500;
        assertEquals(message("@text/account-error-transport"), failedLogin().getMessage());
        inventoryStatus = 200;
        assertEquals(List.of("abcdefghijklmnop"), client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
        assertEquals(2, posts.get());
        assertEquals(2, inventories.get());
        assertTrue(http.isStopped());
    }

    @Test
    void readsWrappedInventoriesAndEnrichesMissingKeys() throws Exception {
        inventory = "{\"appliances\":[{\"appliance\":{\"id\":\"1\"}}]}";
        assertEquals(List.of("ponmlkjihgfedcba"), client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
    }

    @Test
    void retainsKnownKeysWhenOptionalEnrichmentFails() throws Exception {
        inventory = "[{\"id\":\"1\"},{\"id\":\"2\",\"encryption_key\":\"abcdefghijklmnop\"}]";
        detailStatus = 500;
        assertEquals(List.of("abcdefghijklmnop"), client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void authenticationFailureDuringOptionalEnrichmentIsNotIgnored(boolean catalog) {
        inventory = """
                [{"appliance":{"id":"foreign"}},
                 {"appliance":{"id":"matched","interface_type":"Bianca","encryption_key":"abcdefghijklmnop",
                  "programs":[{"program":{"id":"cottons","name":"Cottons"}}]}}]
                """;
        detailStatus = 401;
        var request = catalog ? client.fetchPrograms(configuration(), "abcdefghijklmnop")
                : client.fetchKeys(configuration());
        ExecutionException failure = assertThrows(ExecutionException.class, () -> request.get(15, TimeUnit.SECONDS));
        assertInstanceOf(CandyAuthenticationException.class, failure.getCause());
    }

    @Test
    void supportsAnOptionalApplianceSelector() throws Exception {
        inventory = "[{\"id\":\"1\",\"encryption_key\":\"abcdefghijklmnop\"},"
                + "{\"id\":\"2\",\"encryption_key\":\"ponmlkjihgfedcba\"}]";
        BiancaConfiguration configuration = configuration();
        configuration.applianceId = "2";
        assertEquals(List.of("ponmlkjihgfedcba"), client.fetchKeys(configuration).get(15, TimeUnit.SECONDS));
    }

    @Test
    void rejectsAnUnknownApplianceSelector() {
        BiancaConfiguration configuration = configuration();
        configuration.applianceId = "missing";
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.fetchKeys(configuration).get(15, TimeUnit.SECONDS));
        assertInstanceOf(CandyApplianceSelectionException.class, failure.getCause());
    }

    @Test
    void reportsLoginFailureWithoutExposingCredentials() {
        rejectedLogin = true;
        var failure = failedLogin();
        assertInstanceOf(CandyAuthenticationException.class, failure);
        assertEquals(message("@text/account-error-authentication"), failure.getMessage());
        assertEquals(1, posts.get());
        assertEquals(0, inventories.get());
    }

    @Test
    void refusesToPostCredentialsToAnotherOrigin() {
        unsafeAction = true;
        assertInstanceOf(CandyAuthenticationException.class, failedLogin());
        assertEquals(0, posts.get());
    }

    @Test
    void refusesUntrustedRedirects() {
        unsafeRedirect = true;
        assertEquals(message("@text/account-error-redirect"), failedLogin().getMessage());
        assertEquals(0, inventories.get());
    }

    @Test
    void rejectsCallbacksWithTheWrongState() {
        wrongState = true;
        assertInstanceOf(CandyAuthenticationException.class, failedLogin());
        assertEquals(0, inventories.get());
    }

    @ParameterizedTest
    @CsvSource({ "401, true", "403, true", "429, false", "500, false", "302, false" })
    void classifiesApiFailuresWithoutFollowingRedirects(int status, boolean authentication) {
        inventoryStatus = status;
        var failure = failedLogin();
        if (authentication) {
            assertInstanceOf(CandyAuthenticationException.class, failure);
        } else {
            assertFalse(failure instanceof CandyAuthenticationException);
            assertEquals(message("@text/account-error-transport"), failure.getMessage());
        }
    }

    @Test
    void rejectsMalformedAccountData() {
        inventory = "{private-invalid-json";
        var failure = failedLogin();
        assertEquals(message("@text/account-error-response"), failure.getMessage());
    }

    @Test
    void reportsMissingKeysWithoutTreatingPartialDataAsUnsupported() {
        inventory = "[{\"id\":\"1\"}]";
        detail = "{\"appliance\":{\"id\":\"1\"}}";
        assertEquals(message("@text/account-error-key"), failedLogin().getMessage());
    }

    @Test
    void cancellationStopsTheOwnedClient() throws Exception {
        stall = true;
        var future = client.fetchKeys(configuration());
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            cancellationExpected = true;
            assertTrue(future.cancel(true));
            assertTrue(stopped.await(10, TimeUnit.SECONDS));
        } finally {
            release.countDown();
        }
        assertTrue(http.isStopped());
    }

    @Test
    void isolatesCookiesBetweenAccountClients() throws Exception {
        client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS);
        HttpClient secondHttp = new HttpClient();
        try {
            var second = new BiancaAccountClient(secondHttp, URI.create(base + "/CandyApp"), base);
            assertEquals(List.of("abcdefghijklmnop"), second.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
            assertTrue(secondHttp.isStopped());
        } finally {
            secondHttp.stop();
        }
    }

    private CandyProtocolException failedLogin() {
        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> client.fetchKeys(configuration()).get(15, TimeUnit.SECONDS));
        assertTrue(http.isStopped());
        return assertInstanceOf(CandyProtocolException.class, failure.getCause());
    }

    private BiancaAccountClient clientWithLogger(Logger logger) {
        try (var factory = mockStatic(LoggerFactory.class, CALLS_REAL_METHODS)) {
            factory.when(() -> LoggerFactory.getLogger(BiancaAccountClient.class)).thenReturn(logger);
            return new BiancaAccountClient(http, URI.create(base + "/CandyApp"), base);
        }
    }

    private String recordedMessages(Logger logger) {
        return mockingDetails(logger).getInvocations().stream()
                .map(invocation -> invocation.getMethod().getName() + Arrays.deepToString(invocation.getArguments()))
                .collect(Collectors.joining("\n"));
    }

    private void route(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        if ("/CandyApp/services/oauth2/authorize/expid_mobileCandy".equals(path)) {
            assertEquals(null, exchange.getRequestHeaders().getFirst("Cookie"));
            state = parameters(exchange.getRequestURI().getRawQuery()).getOrDefault("state", "");
            assertFalse(state.isBlank());
            exchange.getResponseHeaders().add("Set-Cookie", "session=fixture; Path=/CandyApp; HttpOnly");
            exchange.getResponseHeaders().add("Location", "/CandyApp/authorization?source=fixture-source");
            respond(exchange, 302, "");
        } else if ("/CandyApp/login".equals(path)) {
            assertEquals("session=fixture", exchange.getRequestHeaders().getFirst("Cookie"));
            assertEquals(null, exchange.getRequestHeaders().getFirst("Authorization"));
            if ("GET".equals(exchange.getRequestMethod())) {
                if (stall) {
                    entered.countDown();
                    release.await(20, TimeUnit.SECONDS);
                    exchange.close();
                    return;
                }
                respond(exchange, 200, loginForm());
            } else {
                posts.incrementAndGet();
                var fields = parameters(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                assertEquals("owner@example.test", fields.get("username"));
                assertEquals("p& +ass", fields.get("pw"));
                assertEquals("a&b", fields.get("csrf"));
                respond(exchange, 200,
                        rejectedLogin ? loginForm() : "<script>window.location.replace('/CandyApp/consent');</script>");
            }
        } else if ("/CandyApp/consent".equals(path)) {
            assertEquals(null, exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.getResponseHeaders().add("Location",
                    unsafeRedirect ? "https://untrusted.invalid/capture"
                            : "candy://mobilesdk/detect/oauth/done#refresh_token=fixture%2Brefresh&state="
                                    + (wrongState ? "wrong" : state));
            respond(exchange, 302, "");
        } else if ("/CandyApp/services/oauth2/token".equals(path)) {
            var form = parameters(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("hybrid_refresh", form.get("grant_type"));
            assertEquals("fixture+refresh", form.get("refresh_token"));
            assertEquals(null, exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, 200, "{\"id_token\":\"fixture-bearer\"}");
        } else if (path.startsWith("/api/v1/appliances")) {
            assertEquals("Bearer fixture-bearer", exchange.getRequestHeaders().getFirst("Authorization"));
            assertEquals("1", exchange.getRequestHeaders().getFirst("Salesforce-Auth"));
            assertEquals(null, exchange.getRequestHeaders().getFirst("Cookie"));
            if ("/api/v1/appliances.json".equals(path)) {
                inventories.incrementAndGet();
                exchange.getResponseHeaders().add("Location", "https://untrusted.invalid/capture");
                respond(exchange, inventoryStatus, inventory);
            } else {
                respond(exchange, detailStatus, detail);
            }
        } else {
            respond(exchange, 404, "");
        }
    }

    private String loginForm() {
        return """
                <form method='post' action='%s'>
                  <input type='hidden' name='csrf' value='a&amp;b'>
                  <input name='username'><input name='pw' type='password'>
                  <input type='hidden' name='oauth_callback' value='candy://mobilesdk/detect/oauth/done'>
                </form>
                """.formatted(unsafeAction ? "https://untrusted.invalid/login" : "/CandyApp/login");
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        try (exchange) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
    }

    private Map<String, String> parameters(String value) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : value.split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return values;
    }

    private BiancaConfiguration configuration() {
        BiancaConfiguration configuration = new BiancaConfiguration();
        configuration.username = "owner@example.test";
        configuration.password = "p& +ass";
        return configuration;
    }

    private String message(String reference) {
        return ResourceBundle.getBundle("OH-INF.i18n.candy", Locale.getDefault())
                .getString(reference.substring("@text/".length()));
    }
}
