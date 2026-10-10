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
package org.openhab.binding.smartthings.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Private subnet parsing and address bounds.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class DiscoverySubnetTest {
    @Test
    void normalizesHostBitsAndOmitsNetworkAndBroadcast() {
        var addresses = DiscoverySubnet.addresses("192.168.1.107/24");
        assertEquals(254, addresses.size());
        assertEquals("192.168.1.1", addresses.getFirst().getHostAddress());
        assertEquals("192.168.1.254", addresses.getLast().getHostAddress());
        assertEquals(254, addresses.stream().distinct().count());
        assertEquals(List.of("10.255.255.1", "10.255.255.2"),
                DiscoverySubnet.addresses("10.255.255.3/30").stream().map(a -> a.getHostAddress()).toList());
    }

    @Test
    void supportsPointToPointAndSingleHostSubnets() {
        assertEquals(List.of("172.31.255.254", "172.31.255.255"),
                DiscoverySubnet.addresses("172.31.255.255/31").stream().map(a -> a.getHostAddress()).toList());
        assertEquals(List.of("192.168.1.50"),
                DiscoverySubnet.addresses("192.168.1.50/32").stream().map(a -> a.getHostAddress()).toList());
        for (int prefix = 24; prefix <= 32; prefix++) {
            assertTrue(DiscoverySubnet.addresses("10.0.0.0/" + prefix).size() <= 256);
        }
    }

    @Test
    void refusesNonPrivateNonLiteralMalformedAndOversizedRanges() {
        for (String subnet : List.of("", "localhost/32", "example.org/24", "10.0.0.1", "10.0.0.0/23", "10.0.0.0/0",
                "10.0.0.0/33", "10.0.0.0/-1", "10.0.0.0/024", "010.0.0.1/24", "10.0.0.256/24", "10.0.0.-1/24",
                "10.0.0/24", "10.0.0.1.2/24", " 10.0.0.1/32", "10.0.0.1/32 ", "10.0.0.1/32/32", "::1/32",
                "127.0.0.1/32", "0.0.0.0/32", "224.0.0.1/32", "255.255.255.255/32", "169.254.1.1/32", "8.8.8.8/32",
                "100.64.0.0/24", "172.15.255.255/32", "172.32.0.0/24", "192.167.255.255/32", "192.169.0.0/24",
                "0xa.0.0.1/32", "10.0.0.1/３２")) {
            assertThrows(IllegalArgumentException.class, () -> DiscoverySubnet.addresses(subnet), subnet);
        }
    }
}
