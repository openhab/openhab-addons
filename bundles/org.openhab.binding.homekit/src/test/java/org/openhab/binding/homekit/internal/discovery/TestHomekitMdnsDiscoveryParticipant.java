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
package org.openhab.binding.homekit.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.net.mac.MacResolver;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

/**
 * Unit tests for {@link HomekitMdnsDiscoveryParticipant}.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestHomekitMdnsDiscoveryParticipant {

    private @NonNullByDefault({}) MacResolver macResolver;
    private @NonNullByDefault({}) StorageService storageService;
    private @NonNullByDefault({}) Storage<String> storage;
    private @NonNullByDefault({}) TestableHomekitParticipant participant;

    /**
     * Subclass to expose protected methods and capture discovery results for verification.
     */
    class TestableHomekitParticipant extends HomekitMdnsDiscoveryParticipant {

        @Nullable
        DiscoveryResult lastDiscovered;

        TestableHomekitParticipant(StorageService storageService, MacResolver resolver) {
            super(storageService, resolver);
        }

        @Override
        protected void thingDiscovered(DiscoveryResult result) {
            this.lastDiscovered = result;
            super.thingDiscovered(result);
        }
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        macResolver = mock(MacResolver.class);
        when(macResolver.resolveMac(any())).thenReturn(CompletableFuture.completedFuture(null));
        storageService = mock(StorageService.class);
        storage = mock(Storage.class);
        when(storageService.<String> getStorage(any(), any())).thenReturn(storage);
        participant = new TestableHomekitParticipant(storageService, macResolver);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private ServiceInfo mockService(String ip, String uniqueId, int category) {
        ServiceInfo svc = mock(ServiceInfo.class);

        Inet4Address addr = mockInet4(ip);
        when(svc.getInet4Addresses()).thenReturn(new Inet4Address[] { addr });

        when(svc.getPort()).thenReturn(1234);
        when(svc.getName()).thenReturn("TestDevice");
        when(svc.getServer()).thenReturn("test.local.");

        Map<String, String> props = Map.of("id", uniqueId, "ci", Integer.toString(category));

        when(svc.getTextBytes()).thenReturn(buildTxtRecord(props));

        return svc;
    }

    private Inet4Address mockInet4(String ip) {
        try {
            return (Inet4Address) InetAddress.getByName(ip);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] buildTxtRecord(Map<String, String> props) {
        var baos = new java.io.ByteArrayOutputStream();
        props.forEach((k, v) -> {
            byte[] bytes = (k + "=" + v).getBytes(StandardCharsets.UTF_8);
            baos.write(bytes.length);
            baos.writeBytes(bytes);
        });
        return baos.toByteArray();
    }

    // ------------------------------------------------------------
    // TESTS
    // ------------------------------------------------------------

    @Test
    void testImmediateDiscoveryWhenMacCached() {
        ServiceInfo svc = mockService("10.0.0.2", "AA:BB:CC:DD:EE:FF", 1);

        when(macResolver.resolveMac("10.0.0.2")).thenReturn(CompletableFuture.completedFuture("AA:BB:CC:DD:EE:FF"));

        DiscoveryResult result = participant.createResult(svc);

        assertNotNull(result);
        assertEquals("AA:BB:CC:DD:EE:FF", result.getProperties().get("macAddress"));
    }

    @Test
    void testDeferredDiscoveryWhenMacNotCached() {
        ServiceInfo svc = mockService("10.0.0.3", "11:22:33:44:55:66", 1);

        // MAC not cached → return an incomplete future
        CompletableFuture<@Nullable String> futureMac = new CompletableFuture<>();
        when(macResolver.resolveMac("10.0.0.3")).thenReturn(futureMac);

        // createResult should return null because MAC is not yet known
        DiscoveryResult result = participant.createResult(svc);

        assertNull(result);
    }

    @Test
    void testAsyncCallbackPublishesResult() {
        ServiceInfo svc = mockService("10.0.0.4", "22:33:44:55:66:77", 1);

        // Create a future that is NOT completed yet
        CompletableFuture<@Nullable String> futureMac = new CompletableFuture<>();
        when(macResolver.resolveMac("10.0.0.4")).thenReturn(futureMac);

        // First call: MAC not yet known → no immediate result
        DiscoveryResult result = participant.createResult(svc);
        assertNull(result);
        assertNull(participant.lastDiscovered);

        // Now complete the future asynchronously
        futureMac.complete("22:33:44:55:66:77");

        // The participant should now have published the discovery
        DiscoveryResult discoveryResult = participant.lastDiscovered;
        assertNotNull(discoveryResult);
        assertEquals("22:33:44:55:66:77", discoveryResult.getProperties().get("macAddress"));
    }

    @Test
    void testNoDuplicateDiscoveryUnderRace() {
        ServiceInfo svc = mockService("10.0.0.5", "33:44:55:66:77:88", 1);

        // Create a future that we can complete manually
        CompletableFuture<@Nullable String> futureMac = new CompletableFuture<>();
        when(macResolver.resolveMac("10.0.0.5")).thenReturn(futureMac);

        // First call: MAC not yet known → no immediate result
        DiscoveryResult result = participant.createResult(svc);
        assertNull(result);
        assertNull(participant.lastDiscovered);

        // Complete the future once
        futureMac.complete("33:44:55:66:77:88");

        // Participant should publish exactly one discovery
        DiscoveryResult discoveryResult = participant.lastDiscovered;
        assertNotNull(discoveryResult);
        assertEquals("33:44:55:66:77:88", discoveryResult.getProperties().get("macAddress"));

        // Try completing again — should NOT publish a second discovery
        participant.lastDiscovered = null; // reset to detect duplicates
        futureMac.complete("33:44:55:66:77:88");

        // No new discovery should be published
        assertNull(participant.lastDiscovered);
    }

    @Test
    void testGetThingUIDPure() {
        ServiceInfo svc = mockService("10.0.0.6", "44:55:66:77:88:99", 1);

        var uid = participant.getThingUID(svc);

        assertNotNull(uid);
        assertEquals("445566778899", uid.getId());
    }
}
