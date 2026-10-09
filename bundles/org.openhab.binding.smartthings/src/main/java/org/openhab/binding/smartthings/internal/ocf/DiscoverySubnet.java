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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strict, bounded RFC 1918 IPv4 subnet enumeration without name resolution.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class DiscoverySubnet {
    private DiscoverySubnet() {
    }

    static List<InetAddress> addresses(String cidr) {
        if (!cidr.matches("(?:0|[1-9][0-9]{0,2})(?:\\.(?:0|[1-9][0-9]{0,2})){3}/(?:2[4-9]|3[0-2])")) {
            throw new IllegalArgumentException(
                    "Discovery subnet must be a literal private IPv4 CIDR with prefix /24 to /32");
        }
        String[] parts = cidr.split("/");
        long address = 0;
        for (String octet : parts[0].split("\\.")) {
            int value = Integer.parseInt(octet);
            if (value > 255) {
                throw new IllegalArgumentException("Invalid IPv4 discovery subnet");
            }
            address = (address << 8) | value;
        }
        if ((address & 0xff000000L) != 0x0a000000L && (address & 0xfff00000L) != 0xac100000L
                && (address & 0xffff0000L) != 0xc0a80000L) {
            throw new IllegalArgumentException("Discovery subnet must be within 10/8, 172.16/12 or 192.168/16");
        }
        int prefix = Integer.parseInt(parts[1]);
        int size = 1 << (32 - prefix);
        long network = address & ~(size - 1L);
        // /31 point-to-point networks and /32 hosts have no network/broadcast addresses to omit.
        int first = prefix < 31 ? 1 : 0;
        int limit = prefix < 31 ? size - 1 : size;
        List<InetAddress> addresses = new ArrayList<>(limit - first);
        for (int i = first; i < limit; i++) {
            long host = network + i;
            try {
                addresses.add(InetAddress.getByAddress(
                        new byte[] { (byte) (host >>> 24), (byte) (host >>> 16), (byte) (host >>> 8), (byte) host }));
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Cannot construct an IPv4 address", e);
            }
        }
        return List.copyOf(addresses);
    }
}
