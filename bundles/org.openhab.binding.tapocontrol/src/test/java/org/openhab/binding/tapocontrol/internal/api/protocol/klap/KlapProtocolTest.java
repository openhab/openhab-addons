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
package org.openhab.binding.tapocontrol.internal.api.protocol.klap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.api.TapoConnectorInterface;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests {@link KlapProtocol} against a fake KLAP device that speaks the handshake and request protocol.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
class KlapProtocolTest {
    private static final TapoCredentials ACCOUNT = new TapoCredentials("user@example.com", "account-password");
    private static final TapoCredentials KASA_SETUP = new TapoCredentials("user@example.com", "kasaSetup");
    private static final String SYSINFO_REQUEST = "{\"system\":{\"get_sysinfo\":{}}}";
    private static final String SYSINFO_RESPONSE = "{\"system\":{\"get_sysinfo\":{\"err_code\":0,\"relay_state\":1}}}";

    private final FakeDevice device = new FakeDevice(KASA_SETUP);
    private final HttpClient httpClient = new HttpClient();
    private @Nullable HttpServer server;
    private @Nullable KlapProtocol klap;

    @BeforeEach
    void setUp() throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/app", device::handle);
        httpServer.start();
        server = httpServer;
        httpClient.start();

        TapoConnectorInterface connector = mock(TapoConnectorInterface.class);
        when(connector.getHttpClient()).thenReturn(httpClient);
        when(connector.getBaseUrl()).thenReturn("127.0.0.1:" + httpServer.getAddress().getPort());
        when(connector.getThingUID()).thenReturn("tapocontrol:test");
        klap = new KlapProtocol(connector);
    }

    @AfterEach
    void tearDown() throws Exception {
        KlapProtocol protocol = klap;
        if (protocol != null) {
            protocol.logout();
        }
        httpClient.stop();
        HttpServer httpServer = server;
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void logsInWithMatchingCandidateInOneHandshakeAndExchangesPayload() throws TapoErrorHandler {
        KlapProtocol protocol = protocol();

        assertThat(protocol.login(List.of(ACCOUNT, KASA_SETUP)), is(true));
        assertThat(device.handshakes.get(), is(1));

        assertThat(protocol.exchange(SYSINFO_REQUEST), is(SYSINFO_RESPONSE));
        assertThat(device.requests, is(List.of(SYSINFO_REQUEST)));
    }

    @Test
    void failsLoginWhenNoCandidateMatches() {
        KlapProtocol protocol = protocol();

        assertThrows(TapoErrorHandler.class, () -> protocol.login(List.of(ACCOUNT)));
        assertThat(protocol.isLoggedIn(), is(false));
    }

    @Test
    void logsInAgainOnceWhenDeviceRejectsSession() throws TapoErrorHandler {
        KlapProtocol protocol = protocol();
        protocol.login(List.of(ACCOUNT, KASA_SETUP));
        device.rejectRequests.set(1);

        assertThat(protocol.exchange(SYSINFO_REQUEST), is(SYSINFO_RESPONSE));
        assertThat(device.handshakes.get(), is(2));
    }

    @Test
    void failsWhenDeviceKeepsRejectingSession() throws TapoErrorHandler {
        KlapProtocol protocol = protocol();
        protocol.login(List.of(KASA_SETUP));
        device.rejectRequests.set(Integer.MAX_VALUE);

        assertThrows(TapoErrorHandler.class, () -> protocol.exchange(SYSINFO_REQUEST));
    }

    @Test
    void treatsSessionAsExpiredNearItsTimeout() throws TapoErrorHandler {
        KlapProtocol protocol = protocol();
        device.sessionTimeoutSeconds = 30;

        protocol.login(List.of(KASA_SETUP));

        assertThat(protocol.isLoggedIn(), is(false));
    }

    private KlapProtocol protocol() {
        KlapProtocol protocol = klap;
        if (protocol == null) {
            throw new IllegalStateException("not set up");
        }
        return protocol;
    }

    /**
     * Minimal KLAP v2 device: handshake1/handshake2 with the auth hash of the given credentials, encrypted requests.
     */
    private static class FakeDevice {
        final AtomicInteger handshakes = new AtomicInteger();
        final AtomicInteger rejectRequests = new AtomicInteger();
        final List<String> requests = new ArrayList<>();
        volatile int sessionTimeoutSeconds = 86400;
        private final byte[] authHash;
        private byte[] localSeed = new byte[0];
        private byte[] remoteSeed = new byte[0];
        private @Nullable KlapCipher cipher;

        FakeDevice(TapoCredentials credentials) {
            authHash = sha256(concat(sha1(credentials.username()), sha1(credentials.password())));
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
                        respond(exchange, 200, concat(remoteSeed, sha256(concat(localSeed, remoteSeed, authHash))));
                    }
                    case "/app/handshake2" -> {
                        boolean valid = MessageDigest.isEqual(body, sha256(concat(remoteSeed, localSeed, authHash)));
                        cipher = valid ? new KlapCipher(localSeed, remoteSeed, authHash) : null;
                        respond(exchange, valid ? 200 : 403, new byte[0]);
                    }
                    case "/app/request" -> {
                        KlapCipher sessionCipher = cipher;
                        if (sessionCipher == null || rejectRequests.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
                            respond(exchange, 403, new byte[0]);
                            return;
                        }
                        int seq = Integer.parseInt(exchange.getRequestURI().getQuery().replace("seq=", ""));
                        requests.add(sessionCipher.decrypt(body, seq));
                        respond(exchange, 200, sessionCipher.encrypt(SYSINFO_RESPONSE));
                    }
                    default -> respond(exchange, 404, new byte[0]);
                }
            } catch (TapoErrorHandler | NoSuchAlgorithmException e) {
                respond(exchange, 500, new byte[0]);
            }
        }

        private static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        }

        private static byte[] sha1(String value) {
            return digest("SHA-1", value.getBytes(StandardCharsets.UTF_8));
        }

        private static byte[] sha256(byte[] value) {
            return digest("SHA-256", value);
        }

        private static byte[] digest(String algorithm, byte[] value) {
            try {
                return MessageDigest.getInstance(algorithm).digest(value);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        private static byte[] concat(byte[]... parts) {
            int length = 0;
            for (byte[] part : parts) {
                length += part.length;
            }
            byte[] result = new byte[length];
            int offset = 0;
            for (byte[] part : parts) {
                System.arraycopy(part, 0, result, offset, part.length);
                offset += part.length;
            }
            return result;
        }
    }
}
