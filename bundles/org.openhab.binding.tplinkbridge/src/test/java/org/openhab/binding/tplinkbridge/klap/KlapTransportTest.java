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
package org.openhab.binding.tplinkbridge.klap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests {@link KlapTransport} against a fake device that speaks the KLAP handshake and request protocol.
 *
 * @author Christopher Smith - Initial contribution
 */
class KlapTransportTest {
    private static final TPLinkCredentials ACCOUNT = new TPLinkCredentials("user@example.com", "account-password");
    private static final TPLinkCredentials KASA_SETUP = new TPLinkCredentials("user@example.com", "kasaSetup");
    private static final String REQUEST = "{\"system\":{\"get_sysinfo\":{}}}";
    private static final String RESPONSE = "{\"system\":{\"get_sysinfo\":{\"err_code\":0,\"relay_state\":1}}}";

    private final FakeDevice device = new FakeDevice(KASA_SETUP);
    private final HttpClient httpClient = new HttpClient();
    private final AtomicReference<List<TPLinkCredentials>> credentials = new AtomicReference<>(
            List.of(ACCOUNT, KASA_SETUP));
    private HttpServer server;
    private KlapTransport transport;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/app", device::handle);
        server.start();
        httpClient.start();
        transport = new KlapTransport(httpClient, "127.0.0.1:" + server.getAddress().getPort(), credentials::get);
    }

    @AfterEach
    void tearDown() throws Exception {
        httpClient.stop();
        server.stop(0);
    }

    @Test
    void logsInWithMatchingCandidateInOneHandshakeAndExchangesPayload() throws IOException {
        assertThat(transport.exchange(REQUEST), is(RESPONSE));
        assertThat(transport.exchange(REQUEST), is(RESPONSE));

        assertThat(device.handshakes.get(), is(1));
        assertThat(device.requests, is(List.of(REQUEST, REQUEST)));
    }

    @Test
    void sendsSessionCookieWithRequests() throws IOException {
        transport.exchange(REQUEST);

        assertThat(device.lastCookie.get(), is("TP_SESSIONID=session1"));
    }

    @Test
    void failsWithAuthenticationErrorWhenNoCandidateMatches() {
        credentials.set(List.of(ACCOUNT));

        assertThrows(KlapAuthenticationException.class, () -> transport.exchange(REQUEST));
        assertThat(device.requests.isEmpty(), is(true));
    }

    @Test
    void failsWithAuthenticationErrorWithoutCredentials() {
        credentials.set(List.of(new TPLinkCredentials("", "")));

        assertThrows(KlapAuthenticationException.class, () -> transport.exchange(REQUEST));
        assertThat(device.handshakes.get(), is(0));
    }

    @Test
    void readsCredentialsAgainAtNextLogin() throws IOException {
        credentials.set(List.of(ACCOUNT));
        assertThrows(KlapAuthenticationException.class, () -> transport.exchange(REQUEST));

        credentials.set(List.of(KASA_SETUP));

        assertThat(transport.exchange(REQUEST), is(RESPONSE));
    }

    @Test
    void logsInAgainOnceWhenDeviceRejectsSession() throws IOException {
        transport.exchange(REQUEST);
        device.rejectRequests.set(1);

        assertThat(transport.exchange(REQUEST), is(RESPONSE));
        assertThat(device.handshakes.get(), is(2));
    }

    @Test
    void failsWhenDeviceKeepsRejectingSession() throws IOException {
        transport.exchange(REQUEST);
        device.rejectRequests.set(Integer.MAX_VALUE);

        IOException e = assertThrows(IOException.class, () -> transport.exchange(REQUEST));
        assertThat(e instanceof KlapAuthenticationException, is(false));
        assertThat(device.handshakes.get(), is(2));
    }

    @Test
    void keepsShortSessionUntilItIsAboutToExpire() throws IOException {
        device.sessionTimeoutSeconds = 30;

        transport.login();

        assertThat(transport.isLoggedIn(), is(true));
        transport.exchange(REQUEST);
        transport.exchange(REQUEST);
        assertThat(device.handshakes.get(), is(1));
    }

    @Test
    void renewsSessionNearItsTimeout() throws IOException {
        device.sessionTimeoutSeconds = 0;

        transport.exchange(REQUEST);
        transport.exchange(REQUEST);

        assertThat(device.handshakes.get(), is(2));
    }

    @Test
    void logsInAgainAfterHostChange() throws IOException {
        transport.exchange(REQUEST);

        transport.setHost("localhost:" + server.getAddress().getPort());
        transport.exchange(REQUEST);

        assertThat(device.handshakes.get(), is(2));
    }

    @Test
    void logsInAheadOfFirstExchange() throws IOException {
        transport.login();

        assertThat(transport.isLoggedIn(), is(true));
        assertThat(transport.exchange(REQUEST), is(RESPONSE));
        assertThat(device.handshakes.get(), is(1));
    }

    @Test
    void isNotLoggedInAfterClose() throws IOException {
        transport.login();

        transport.close();

        assertThat(transport.isLoggedIn(), is(false));
    }

    @Test
    void failsWithoutHost() {
        KlapTransport noHost = new KlapTransport(httpClient, null, credentials::get);

        assertThrows(IOException.class, () -> noHost.exchange(REQUEST));
    }

    /**
     * Minimal KLAP v2 device accepting the auth hash of the given credentials.
     */
    private static class FakeDevice {
        final AtomicInteger handshakes = new AtomicInteger();
        final AtomicInteger rejectRequests = new AtomicInteger();
        final List<String> requests = new CopyOnWriteArrayList<>();
        final AtomicReference<String> lastCookie = new AtomicReference<>();
        volatile int sessionTimeoutSeconds = 86400;
        private final byte[] authHash;
        private byte[] localSeed = new byte[0];
        private byte[] remoteSeed = new byte[0];
        private KlapCipher cipher;

        FakeDevice(TPLinkCredentials credentials) {
            try {
                authHash = KlapCipher.sha256(KlapCipher.sha1(credentials.username().getBytes(StandardCharsets.UTF_8)),
                        KlapCipher.sha1(credentials.password().getBytes(StandardCharsets.UTF_8)));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        }

        synchronized void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            byte[] body = exchange.getRequestBody().readAllBytes();
            try {
                switch (path) {
                    case "/app/handshake1" -> {
                        handshakes.incrementAndGet();
                        localSeed = body;
                        remoteSeed = new byte[16];
                        new SecureRandom().nextBytes(remoteSeed);
                        exchange.getResponseHeaders().add("Set-Cookie",
                                "TP_SESSIONID=session" + handshakes.get() + ";TIMEOUT=" + sessionTimeoutSeconds);
                        respond(exchange, 200,
                                KlapCipher.concat(remoteSeed, KlapCipher.sha256(localSeed, remoteSeed, authHash)));
                    }
                    case "/app/handshake2" -> {
                        boolean valid = MessageDigest.isEqual(body, KlapCipher.sha256(remoteSeed, localSeed, authHash));
                        cipher = valid ? new KlapCipher(localSeed, remoteSeed, authHash) : null;
                        respond(exchange, valid ? 200 : 403, new byte[0]);
                    }
                    case "/app/request" -> {
                        lastCookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
                        if (cipher == null || rejectRequests.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
                            respond(exchange, 403, new byte[0]);
                            return;
                        }
                        int seq = Integer.parseInt(exchange.getRequestURI().getQuery().replace("seq=", ""));
                        requests.add(cipher.decrypt(body, seq));
                        respond(exchange, 200, cipher.encrypt(RESPONSE, seq));
                    }
                    default -> respond(exchange, 404, new byte[0]);
                }
            } catch (GeneralSecurityException e) {
                respond(exchange, 500, new byte[0]);
            }
        }

        private static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        }
    }
}
