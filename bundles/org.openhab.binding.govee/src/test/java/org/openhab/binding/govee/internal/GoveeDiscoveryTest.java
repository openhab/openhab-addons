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
package org.openhab.binding.govee.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.govee.internal.model.DiscoveryResponse;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.net.mac.MacResolver;

import com.google.gson.Gson;

/**
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeDiscoveryTest {

    String response = """
             {
                "msg":{
                   "cmd":"scan",
                   "data":{
                      "ip":"192.168.178.171",
                      "device":"7D:31:C3:35:33:33:44:15",
                      "sku":"H6076",
                      "bleVersionHard":"3.01.01",
                      "bleVersionSoft":"1.04.04",
                      "wifiVersionHard":"1.00.10",
                      "wifiVersionSoft":"1.02.11"
                   }
                }
            }
             """;

    // A status update is received on the same port as discovery responses. Its "data" object does not
    // contain the device information expected by discovery, so Gson leaves those fields null.
    String statusResponse = """
             {
                "msg":{
                   "cmd":"devStatus",
                   "data":{
                      "onOff":1,
                      "brightness":100,
                      "color":{ "r":255, "g":255, "b":255 },
                      "colorTemInKelvin":7200
                   }
                }
            }
             """;

    // A malformed scan response that is missing the "ip" field.
    String scanResponseWithoutIp = """
             {
                "msg":{
                   "cmd":"scan",
                   "data":{
                      "device":"7D:31:C3:35:33:33:44:15",
                      "sku":"H6076"
                   }
                }
            }
             """;

    @Test
    public void testProcessScanMessage() {
        GoveeDiscoveryService service = new GoveeDiscoveryService(new CommunicationManager());
        DiscoveryResponse resp = new Gson().fromJson(response, DiscoveryResponse.class);
        Objects.requireNonNull(resp);
        @Nullable
        DiscoveryResult result = service.responseToResult(resp);
        assertNotNull(result);
        Map<String, Object> deviceProperties = result.getProperties();
        assertEquals(deviceProperties.get(GoveeBindingConstants.DEVICE_TYPE), "H6076");
        assertEquals(deviceProperties.get(GoveeBindingConstants.IP_ADDRESS), "192.168.178.171");
        assertEquals(deviceProperties.get(GoveeBindingConstants.CONFIG_DEVICE_ID), "7D:31:C3:35:33:33:44:15");
        assertEquals(deviceProperties.get(GoveeBindingConstants.PROPERTY_DEVICE_ID), "7D:31:C3:35:33:33:44:15");
        assertFalse(deviceProperties.containsKey(GoveeBindingConstants.PROPERTY_NETWORK_MAC_ADDRESS));
        assertEquals("macAddress", result.getRepresentationProperty());
        assertEquals("govee:govee-light:7D_31_C3_35_33_33_44_15", result.getThingUID().toString());
    }

    @Test
    public void testResolvedNetworkMacIsAddedToDiscoveryResult() {
        MacResolver macResolver = new MacResolver() {
            @Override
            public CompletableFuture<@Nullable String> resolveMac(String ipAddress) {
                return CompletableFuture.completedFuture("60:74:f4:42:56:0e");
            }
        };
        List<DiscoveryResult> results = new ArrayList<>();
        GoveeDiscoveryService service = new GoveeDiscoveryService(new CommunicationManager(), macResolver) {
            @Override
            protected void thingDiscovered(DiscoveryResult result) {
                results.add(result);
            }
        };
        DiscoveryResponse resp = new Gson().fromJson(response, DiscoveryResponse.class);
        Objects.requireNonNull(resp);

        service.onDiscoveryResponse(resp);

        assertEquals(2, results.size());
        assertFalse(results.get(0).getProperties().containsKey(GoveeBindingConstants.PROPERTY_NETWORK_MAC_ADDRESS));
        assertEquals("60:74:f4:42:56:0e",
                results.get(1).getProperties().get(GoveeBindingConstants.PROPERTY_NETWORK_MAC_ADDRESS));
        assertEquals("7D:31:C3:35:33:33:44:15",
                results.get(1).getProperties().get(GoveeBindingConstants.CONFIG_DEVICE_ID));
    }

    @Test
    public void testStatusMessageIsIgnored() {
        GoveeDiscoveryService service = new GoveeDiscoveryService(new CommunicationManager());
        DiscoveryResponse resp = new Gson().fromJson(statusResponse, DiscoveryResponse.class);
        Objects.requireNonNull(resp);
        // Must not throw and must not be discovered as a Thing.
        assertNull(service.responseToResult(resp));
    }

    @Test
    public void testScanMessageWithoutIpIsIgnored() {
        GoveeDiscoveryService service = new GoveeDiscoveryService(new CommunicationManager());
        DiscoveryResponse resp = new Gson().fromJson(scanResponseWithoutIp, DiscoveryResponse.class);
        Objects.requireNonNull(resp);
        // Must not throw a NullPointerException when the IP field is absent.
        assertNull(service.responseToResult(resp));
    }
}
