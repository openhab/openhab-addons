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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.Token;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.observe.NotificationListener;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConfig.DtlsRole;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Transport profile and exchange tests without appliance connections.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class CoapTransportTest {
    private final InetAddress host = InetAddress.getLoopbackAddress();

    @Test
    void ownerIdentityUsesRawUuidBytesAndOnlySamsungPskCipher() throws Exception {
        ApplianceConfiguration config = configuration();
        var dtls = CoapTransport.dtlsConfiguration(config, host);
        var profile = dtls.getConfiguration();
        assertEquals(DtlsRole.CLIENT_ONLY, profile.get(DtlsConfig.DTLS_ROLE));
        assertEquals(1, profile.get(DtlsConfig.DTLS_MAX_CONNECTIONS));
        assertFalse(profile.get(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY));
        assertEquals(List.of(CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256),
                profile.get(DtlsConfig.DTLS_CIPHER_SUITES));
        var store = dtls.getAdvancedPskStore();
        assertNotNull(store);
        assertArrayEquals(HexFormat.of().parseHex("0123456789abcdef0123456789abcdef"),
                store.getIdentity(new InetSocketAddress(host, 49155), null).getBytes());
        assertEquals(CoapTransport.clientPort(config, host), dtls.getAddress().getPort());
        assertEquals(CoapTransport.clientPort(config, host), CoapTransport.clientPort(config, host));
        config.clientPort = 41001;
        assertEquals(41001, CoapTransport.clientPort(config, host));
        var coap = CoapTransport.networkConfiguration();
        assertTrue(coap.get(CoapConfig.BLOCKWISE_REUSE_TOKEN));
        assertFalse(coap.get(CoapConfig.BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER));
        assertEquals(65536, coap.get(CoapConfig.MAX_RESOURCE_BODY_SIZE));
    }

    @Test
    void certificateProfileAcceptsOneImportedPrivateKeyWithPinOrCaTrust() throws Exception {
        Path directory = Path.of("target", "certificate-profile-test");
        Files.createDirectories(directory);
        Path file = directory.resolve("client.p12");
        Process keytool = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "appliance-test", "-keyalg", "EC", "-groupname", "secp256r1", "-dname",
                "CN=synthetic-appliance-test", "-storetype", "PKCS12", "-keystore", file.toString(), "-storepass",
                "test-password", "-keypass", "test-password", "-noprompt")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            assertTrue(keytool.waitFor(30, TimeUnit.SECONDS));
            assertEquals(0, keytool.exitValue());
            ApplianceConfiguration config = configuration();
            config.ownerId = "";
            config.ownerPsk = "";
            config.keyStore = file.toString();
            config.keyStorePassword = "test-password";
            config.serverFingerprint = "ab".repeat(32);
            config.validate();
            var dtls = CoapTransport.dtlsConfiguration(config, host);
            assertNotNull(dtls.getCertificateIdentityProvider());
            assertInstanceOf(PinnedCertificateVerifier.class, dtls.getAdvancedCertificateVerifier());
            assertNull(dtls.getAdvancedPskStore());
            assertTrue(dtls.getConfiguration().get(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY));
            assertFalse(dtls.getConfiguration().get(DtlsConfig.DTLS_TRUNCATE_CLIENT_CERTIFICATE_PATH));
            assertEquals(List.of(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256),
                    dtls.getConfiguration().get(DtlsConfig.DTLS_CIPHER_SUITES));
            config.serverFingerprint = "";
            config.validate();
            assertInstanceOf(SamsungCertificateVerifier.class,
                    CoapTransport.dtlsConfiguration(config, host).getAdvancedCertificateVerifier());
            config.keyStorePassword = "private-invalid-password";
            config.port = 49155;
            IOException failure = assertThrows(IOException.class, () -> new CoapTransport(config));
            assertFalse(failure.toString().contains(config.keyStorePassword));
            assertNull(failure.getCause());
            KeyStore empty = KeyStore.getInstance("PKCS12");
            empty.load(null, null);
            try (var output = Files.newOutputStream(file)) {
                empty.store(output, "test-password".toCharArray());
            }
            config.keyStorePassword = "test-password";
            assertThrows(IOException.class, () -> new CoapTransport(config));
        } finally {
            keytool.destroyForcibly();
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void validatesPathsBeforeSendingToTheFixedPeer() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        try (CoapTransport transport = new CoapTransport(host, 49155, 100, endpoint)) {
            for (String path : List.of("https://example.org/power/0", "//other/power/0", "power/0", "/", "/../power/0",
                    "/a/./b", "/a//b", "/%2e%2e/power/0", "/power/0#x", "/a\\b", "/oic/sec/doxm", "/oic/sec",
                    "/oic/res?rt=oic.r.doxm", "/a?value=http://other", "/a?x=%2F")) {
                assertThrows(IOException.class, () -> transport.get(path), path);
            }
            verify(endpoint, never()).sendRequest(any());
            var uri = CoapTransport.resourceUri(host, 49155, "/device/0?if=oic.if.b");
            assertEquals("coaps", uri.getScheme());
            assertEquals(49155, uri.getPort());
            assertEquals("/device/0", uri.getPath());
            assertEquals("if=oic.if.b", uri.getQuery());
        }
        verify(endpoint).destroy();
    }

    @Test
    void sendsCborGetAndOnePostWithoutRetry() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        AtomicReference<Request> last = new AtomicReference<>();
        doAnswer(invocation -> {
            Request request = invocation.getArgument(0);
            last.set(request);
            Response response = new Response(ResponseCode.CONTENT);
            response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
            response.setPayload(Cbor.encode(JsonParser.parseString("{\"value\":true}")));
            request.setResponse(response);
            return null;
        }).when(endpoint).sendRequest(any());
        try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
            assertTrue(transport.get("/power/0").getAsJsonObject().get("value").getAsBoolean());
            assertEquals(60, last.get().getOptions().getAccept());
            JsonObject fields = new JsonObject();
            fields.addProperty("value", false);
            transport.post("/power/0", fields);
            assertEquals(60, last.get().getOptions().getContentFormat());
            assertEquals(fields, Cbor.decode(last.get().getPayload()));
        }
        verify(endpoint, times(2)).sendRequest(any());
    }

    @Test
    void acceptsAnEmptySuccessfulPostAcknowledgement() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        doAnswer(invocation -> {
            ((Request) invocation.getArgument(0)).setResponse(new Response(ResponseCode.CHANGED));
            return null;
        }).when(endpoint).sendRequest(any());
        try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
            assertDoesNotThrow(() -> transport.post("/power/0", new JsonObject()));
            verify(endpoint, times(1)).sendRequest(any());
        }
    }

    @Test
    void rejectsWrongContentFormatErrorsAndOversizeBodies() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        for (Response response : List.of(new Response(ResponseCode.UNAUTHORIZED), new Response(ResponseCode.CONTENT),
                oversized())) {
            doAnswer(invocation -> {
                ((Request) invocation.getArgument(0)).setResponse(response);
                return null;
            }).when(endpoint).sendRequest(any());
            try (CoapTransport transport = new CoapTransport(host, 49155, 100, endpoint)) {
                assertThrows(IOException.class, () -> transport.get("/power/0"));
            }
        }
    }

    @Test
    void closeCancelsAnInFlightRequestAndIsIdempotent() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        CountDownLatch sent = new CountDownLatch(1);
        AtomicReference<Request> pending = new AtomicReference<>();
        doAnswer(invocation -> {
            pending.set(invocation.getArgument(0));
            sent.countDown();
            return null;
        }).when(endpoint).sendRequest(any());
        CoapTransport transport = new CoapTransport(host, 49155, 60000, endpoint);
        CompletableFuture<IOException> result = CompletableFuture
                .supplyAsync(() -> assertThrows(IOException.class, () -> transport.get("/power/0")));
        try {
            assertTrue(sent.await(5, TimeUnit.SECONDS));
            transport.close();
            assertNotNull(result.get(5, TimeUnit.SECONDS));
            assertTrue(pending.get().isCanceled());
            assertThrows(IOException.class, () -> transport.get("/power/0"));
            transport.close();
            verify(endpoint, times(1)).destroy();
            verify(endpoint, times(1)).sendRequest(any());
        } finally {
            transport.close();
            result.cancel(true);
        }
    }

    @Test
    void timeoutCancelsWithoutRepeatingPost() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        AtomicReference<Request> pending = new AtomicReference<>();
        doAnswer(invocation -> {
            pending.set(invocation.getArgument(0));
            return null;
        }).when(endpoint).sendRequest(any());
        try (CoapTransport transport = new CoapTransport(host, 49155, 1, endpoint)) {
            assertThrows(IOException.class, () -> transport.post("/power/0", new JsonObject()));
            assertTrue(pending.get().isCanceled());
            verify(endpoint, times(1)).sendRequest(any());
        }
    }

    @Test
    void interruptionIsPreservedAndExceptionDetailsAreSafe() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        try (CoapTransport transport = new CoapTransport(host, 49155, 60000, endpoint)) {
            Thread.currentThread().interrupt();
            try {
                assertThrows(IOException.class, () -> transport.get("/power/0"));
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
            doThrow(new IllegalArgumentException("secretcredential")).when(endpoint).sendRequest(any());
            IOException failure = assertThrows(IOException.class, () -> transport.get("/power/0"));
            assertFalse(failure.toString().contains("secretcredential"));
            assertNull(failure.getCause());
        }
    }

    @Test
    void observesCborResourcesAndCancelsWithoutOwningTheSharedEndpoint() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getConfig()).thenReturn(CoapTransport.networkConfiguration());
        AtomicReference<Request> request = new AtomicReference<>();
        AtomicReference<NotificationListener> notifications = new AtomicReference<>();
        doAnswer(invocation -> {
            notifications.set(invocation.getArgument(0));
            return null;
        }).when(endpoint).addNotificationListener(any());
        doAnswer(invocation -> {
            Request sent = invocation.getArgument(0);
            sent.setToken(new Token(new byte[] { 1 }));
            request.set(sent);
            return null;
        }).when(endpoint).sendRequest(any());
        LinkedBlockingQueue<JsonElement> updates = new LinkedBlockingQueue<>();
        AtomicInteger failures = new AtomicInteger();
        try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
            Transport.Subscription subscription = transport.observe("/power/0", new Transport.Listener() {
                @Override
                public void onUpdate(JsonElement payload) {
                    updates.add(payload);
                }

                @Override
                public void onFailure() {
                    failures.incrementAndGet();
                }
            });
            assertEquals(0, request.get().getOptions().getObserve());
            assertEquals(MediaTypeRegistry.APPLICATION_CBOR, request.get().getOptions().getAccept());
            assertEquals(CoapTransport.resourceUri(host, 49155, "/power/0").toString(), request.get().getURI());
            Response initial = notification(true, 0);
            initial.setToken(request.get().getToken());
            request.get().setResponse(initial);
            assertEquals(JsonParser.parseString("{\"value\":true}"), updates.poll(5, TimeUnit.SECONDS));
            Response changed = notification(false, 1);
            changed.setToken(request.get().getToken());
            notifications.get().onNotification(request.get(), changed);
            assertEquals(JsonParser.parseString("{\"value\":false}"), updates.poll(5, TimeUnit.SECONDS));
            subscription.close();
            subscription.close();
            assertTrue(request.get().isCanceled());
            assertEquals(0, failures.get());
            verify(endpoint, times(1)).removeNotificationListener(any());
            verify(endpoint, never()).destroy();
        }
        verify(endpoint, times(1)).destroy();
    }

    @Test
    void unsupportedInvalidAndFailedObservationsSignalFallback() throws Exception {
        Response malformed = notification(true, 0);
        malformed.setPayload(new byte[] { (byte) 0xff });
        for (Response response : List.of(new Response(ResponseCode.UNAUTHORIZED), new Response(ResponseCode.CONTENT),
                oversized(), malformed)) {
            Endpoint endpoint = mock(Endpoint.class);
            when(endpoint.getConfig()).thenReturn(CoapTransport.networkConfiguration());
            AtomicReference<Request> request = new AtomicReference<>();
            doAnswer(invocation -> {
                request.set(invocation.getArgument(0));
                return null;
            }).when(endpoint).sendRequest(any());
            CountDownLatch failed = new CountDownLatch(1);
            try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
                transport.observe("/power/0", new Transport.Listener() {
                    @Override
                    public void onUpdate(JsonElement payload) {
                        fail("Invalid observation must not publish a state");
                    }

                    @Override
                    public void onFailure() {
                        failed.countDown();
                    }
                });
                request.get().setResponse(response);
                assertTrue(failed.await(5, TimeUnit.SECONDS));
                verify(endpoint).removeNotificationListener(any());
            }
        }
    }

    @Test
    void closeCancelsPendingObservationAndRejectsNewSubscriptions() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getConfig()).thenReturn(CoapTransport.networkConfiguration());
        AtomicReference<Request> request = new AtomicReference<>();
        doAnswer(invocation -> {
            request.set(invocation.getArgument(0));
            return null;
        }).when(endpoint).sendRequest(any());
        Transport.Listener listener = mock(Transport.Listener.class);
        CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint);
        transport.observe("/power/0", listener);
        transport.close();
        transport.close();
        assertTrue(request.get().isCanceled());
        assertThrows(IOException.class, () -> transport.observe("/power/0", listener));
        verify(endpoint).removeNotificationListener(any());
        verify(endpoint).destroy();
        verifyNoInteractions(listener);
    }

    @Test
    void failedObservationStartRemovesItsListenerAndCancelsRequest() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getConfig()).thenReturn(CoapTransport.networkConfiguration());
        AtomicReference<Request> request = new AtomicReference<>();
        doAnswer(invocation -> {
            request.set(invocation.getArgument(0));
            throw new IllegalStateException("private-credential");
        }).when(endpoint).sendRequest(any());
        Transport.Listener listener = mock(Transport.Listener.class);
        try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
            IOException failure = assertThrows(IOException.class, () -> transport.observe("/power/0", listener));
            assertFalse(failure.toString().contains("private-credential"));
            assertNull(failure.getCause());
            assertTrue(request.get().isCanceled());
            verify(endpoint).removeNotificationListener(any());
        }
    }

    @Test
    void observationTimeoutSignalsFailureAndCancelsRenewal() throws Exception {
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getConfig()).thenReturn(CoapTransport.networkConfiguration());
        AtomicReference<Request> request = new AtomicReference<>();
        doAnswer(invocation -> {
            request.set(invocation.getArgument(0));
            return null;
        }).when(endpoint).sendRequest(any());
        CountDownLatch failed = new CountDownLatch(1);
        try (CoapTransport transport = new CoapTransport(host, 49155, 1000, endpoint)) {
            transport.observe("/power/0", new Transport.Listener() {
                @Override
                public void onUpdate(JsonElement payload) {
                    fail("Timed-out request must not publish state");
                }

                @Override
                public void onFailure() {
                    failed.countDown();
                }
            });
            request.get().setTimedOut(true);
            assertTrue(failed.await(5, TimeUnit.SECONDS));
            verify(endpoint).removeNotificationListener(any());
        }
    }

    private static Response notification(boolean value, int sequence) throws IOException {
        Response response = new Response(ResponseCode.CONTENT);
        response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR).setObserve(sequence);
        response.setPayload(Cbor.encode(JsonParser.parseString("{\"value\":" + value + "}")));
        return response;
    }

    private static Response oversized() {
        Response response = new Response(ResponseCode.CONTENT);
        response.getOptions().setContentFormat(60);
        response.setPayload(new byte[65537]);
        return response;
    }

    static ApplianceConfiguration configuration() {
        ApplianceConfiguration config = new ApplianceConfiguration();
        config.host = "127.0.0.1";
        config.ownerId = "01234567-89ab-cdef-0123-456789abcdef";
        config.ownerPsk = "00112233445566778899aabbccddeeff";
        return config;
    }
}
