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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.gson.JsonParser;

/**
 * Public OIC CBOR advertisement and bounded loopback discovery tests.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class DiscoveryTest {
    private static final String IDENTITY = "12345678-1234-5678-9abc-123456789abc";
    private final InetAddress host = InetAddress.getByAddress(new byte[] { (byte) 192, 0, 2, 10 });

    DiscoveryTest() throws IOException {
    }

    @Test
    void discoversLegacySamsungSecurePortFromCborLinks() throws Exception {
        byte[] resources = Cbor.encode(JsonParser.parseString("""
                [{"di":"12345678-1234-5678-9abc-123456789abc","links":[
                  {"href":"/oic/sec/doxm","rt":["oic.r.doxm"],"p":{"sec":true,"port":49155}}
                ]}]
                """));
        var descriptor = Discovery.descriptor(Cbor.decode(resources), device(), host);
        assertEquals(49155, descriptor.securePort());
        assertEquals(IDENTITY, descriptor.deviceId());
        assertEquals("Samsung Room A/C", descriptor.name());
    }

    @Test
    void discoversCoapsAndIgnoresPlainCoapEndpoint() throws Exception {
        var listing = JsonParser.parseString("""
                {"links":[{"href":"/oic/sec/doxm","eps":[
                  {"ep":"coap://192.0.2.10:49154"},
                  {"ep":"coaps://192.0.2.10:49155"}]}]}
                """);
        assertEquals(49155, Discovery.descriptor(listing, device(), host).securePort());
        InetAddress ipv6 = InetAddress.getByName("::1");
        assertEquals(49155, Discovery.securePort(ipv6, "coaps://[::1]:49155"));
        assertEquals(5684, Discovery.securePort(host, "coaps://192.0.2.10"));
    }

    @Test
    void rejectsForeignHostsAndUriComponentsWithoutResolvingNames() {
        for (String ep : List.of("coaps://192.0.2.11:49155", "coaps://example.org:49155",
                "coaps://user:password@192.0.2.10:49155", "coaps://192.0.2.10:49155/",
                "coaps://192.0.2.10:49155/power/0", "coaps://192.0.2.10:49155?x=y", "coaps://192.0.2.10:49155#fragment",
                "coaps://192.0.2.10:0", "coaps://192.0.2.10:65536", "coap://192.0.2.10:49155",
                "coaps://[bad-ipv6]:49155")) {
            assertThrows(IOException.class, () -> Discovery.securePort(host, ep), ep);
        }
    }

    @Test
    void rejectsMissingAmbiguousMalformedAndConflictingAdvertisements() throws IOException {
        for (String listing : List.of("{}", "null", "[true]", "{\"links\":[{\"eps\":[{\"ep\":false}]}]}",
                "{\"di\":\"different\",\"links\":[]}", "{\"links\":[{\"p\":{\"sec\":true,\"port\":0}}]}",
                "{\"links\":[{\"p\":{\"sec\":true,\"port\":65536}}]}",
                "{\"links\":[{\"p\":{\"sec\":true,\"port\":\"49155\"}}]}",
                "{\"links\":[{\"p\":{\"sec\":\"true\",\"port\":49155}}]}",
                "{\"links\":[{\"p\":{\"sec\":false,\"port\":49155}}]}",
                "{\"links\":[{\"p\":{\"sec\":true,\"port\":49155.5}}]}",
                "{\"links\":[{\"p\":{\"sec\":true,\"port\":49155}}," + "{\"p\":{\"sec\":true,\"port\":49156}}]}")) {
            assertThrows(IOException.class, () -> Discovery.descriptor(JsonParser.parseString(listing), device(), host),
                    listing);
        }
        assertThrows(IOException.class, () -> Discovery.descriptor(JsonParser.parseString("{}"),
                JsonParser.parseString("{\"di\":\"not-a-device\"}"), host));
    }

    @Test
    void verifiesSamsungPlatformWithoutConfusingPlatformAndDeviceIdentities() throws IOException {
        for (String manufacturer : List.of("Samsung", "Samsung Electronics", "Samsung Electronics Co., Ltd.",
                "SAMSUNG ELECTRONICS")) {
            Discovery.verifySamsung(
                    JsonParser.parseString(
                            "{\"mnmn\":\"" + manufacturer + "\",\"pi\":\"87654321-4321-6789-abcd-987654321abc\"}"),
                    IDENTITY);
        }
        Discovery.verifySamsung(
                JsonParser.parseString("[{\"di\":\"" + IDENTITY + "\",\"mnmn\":\"Samsung Electronics\"}]"), IDENTITY);
    }

    @Test
    void rejectsMissingForeignMalformedAndConflictingManufacturerMetadata() {
        for (String platform : List.of("{}", "null", "[]", "[true]", "{\"mnmn\":null}", "{\"mnmn\":42}",
                "{\"mnmn\":\"\"}", "{\"mnmn\":\"Other vendor\"}", "{\"mnmn\":\"Not Samsung\"}",
                "{\"mnmn\":\"Samsung impersonator\"}", "{\"mnmn\":\"Samsung\",\"di\":\"invalid\"}",
                "{\"mnmn\":\"Samsung\",\"di\":\"87654321-4321-6789-abcd-987654321abc\"}",
                "[{\"mnmn\":\"Samsung\"},{\"mnmn\":\"Other vendor\"}]")) {
            assertThrows(IOException.class, () -> Discovery.verifySamsung(JsonParser.parseString(platform), IDENTITY),
                    platform);
        }
    }

    @Test
    @Timeout(10)
    void discoversWorkingPortAfterAnUnavailablePort() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        var endpoint = new CoapEndpoint.Builder().setConfiguration(CoapTransport.networkConfiguration())
                .setInetSocketAddress(new InetSocketAddress(loopback, 0)).build();
        var server = new CoapServer(CoapTransport.networkConfiguration());
        server.addEndpoint(endpoint);
        var oic = new CoapResource("oic");
        oic.add(resource("d", Cbor.encode(device())));
        oic.add(resource("p", Cbor.encode(JsonParser.parseString("{\"mnmn\":\"Samsung Electronics\"}"))));
        oic.add(resource("res", Cbor.encode(JsonParser.parseString("""
                [{"di":"12345678-1234-5678-9abc-123456789abc","links":[
                  {"href":"/oic/sec/doxm","p":{"sec":true,"port":49155}}
                ]}]
                """))));
        server.add(oic);
        try {
            server.start();
            int unavailable;
            try (var socket = new DatagramSocket(new InetSocketAddress(loopback, 0))) {
                unavailable = socket.getLocalPort();
            }
            var ports = new LinkedHashSet<>(List.of(unavailable, endpoint.getAddress().getPort()));
            var found = Discovery.discoverSamsung(loopback, ports, 5);
            assertEquals(IDENTITY, found.deviceId());
            oic.add(resource("p", Cbor.encode(JsonParser.parseString("{\"mnmn\":\"Other vendor\"}"))));
            assertThrows(IOException.class, () -> Discovery.discoverSamsung(loopback,
                    new LinkedHashSet<>(List.of(endpoint.getAddress().getPort())), 2));
            assertEquals(IDENTITY, Discovery.discover(loopback, endpoint.getAddress().getPort(), 2).deviceId());
            assertEquals(49155, found.securePort());
        } finally {
            server.destroy();
        }
    }

    @Test
    @Timeout(15)
    void lastDiscoveryPortCanUseTheRemainingConfiguredTimeout() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        var endpoint = new CoapEndpoint.Builder().setConfiguration(CoapTransport.networkConfiguration())
                .setInetSocketAddress(new InetSocketAddress(loopback, 0)).build();
        var server = new CoapServer(CoapTransport.networkConfiguration());
        var responses = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "discovery-response-test");
            thread.setDaemon(true);
            return thread;
        });
        byte[] listing = Cbor.encode(JsonParser.parseString("""
                [{"di":"12345678-1234-5678-9abc-123456789abc","links":[
                  {"href":"/oic/sec/doxm","p":{"sec":true,"port":49155}}
                ]}]
                """));
        var oic = new CoapResource("oic");
        oic.add(resource("d", Cbor.encode(device())));
        oic.add(new CoapResource("res") {
            @Override
            @NonNullByDefault({})
            public void handleGET(CoapExchange exchange) {
                exchange.accept();
                // Exceed the old one-second probe cap, with margin inside the six-second deadline.
                responses.schedule(
                        () -> exchange.respond(ResponseCode.CONTENT, listing, MediaTypeRegistry.APPLICATION_CBOR), 2,
                        TimeUnit.SECONDS);
            }
        });
        server.addEndpoint(endpoint);
        server.add(oic);
        try {
            server.start();
            var found = Discovery.discover(loopback, new LinkedHashSet<>(List.of(endpoint.getAddress().getPort())), 6);
            assertEquals(IDENTITY, found.deviceId());
        } finally {
            responses.shutdownNow();
            server.destroy();
            assertTrue(responses.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static CoapResource resource(String name, byte[] payload) {
        return new CoapResource(name) {
            @Override
            @NonNullByDefault({})
            public void handleGET(CoapExchange exchange) {
                exchange.respond(ResponseCode.CONTENT, payload, MediaTypeRegistry.APPLICATION_CBOR);
            }
        };
    }

    private static com.google.gson.JsonElement device() throws IOException {
        return Cbor.decode(Cbor.encode(JsonParser.parseString("""
                {"di":"12345678-1234-5678-9abc-123456789abc","n":"Samsung Room A/C",
                 "rt":["oic.wk.d","oic.d.airconditioner"]}
                """)));
    }
}
