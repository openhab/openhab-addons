/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;

/**
 * The {@link IPUtils} class defines some static IP utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class IPUtils {

    /**
     * For all network interfaces (except loopback) all Ipv4 addresses are returned.
     * This list can for example, be used to scan the network for available devices.
     * 
     * @return A full list of IP {@link InetAddress} (except network and broadcast)
     */
    public static List<InetAddress> getFullRangeOfAddressesToScan() {
        List<InetAddress> addressesToScan = List.of();
        List<CidrAddress> ipV4InterfaceAddresses = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> a.getAddress() instanceof Inet4Address).collect(Collectors.toList());

        for (CidrAddress i : ipV4InterfaceAddresses) {
            addressesToScan.addAll(getAddressesRangeByCidrAddress(i, 24));
        }
        return addressesToScan;
    }

    /**
     * For the given {@link CidrAddress} all Ipv4 addresses are returned.
     * This list can for example, be used to scan the network for available devices.
     * 
     * @param iFaceAddress The {@link CidrAddress} of the network interface
     * @param maxPrefixLength Control the prefix length of the network (e.g. 24 for class C)
     * @return A full list of IP {@link InetAddress} (except network and broadcast)
     */
    public static List<InetAddress> getAddressesRangeByCidrAddress(CidrAddress iFaceAddress, int maxPrefixLength) {
        if (!(iFaceAddress.getAddress() instanceof Inet4Address) || iFaceAddress.getPrefix() < maxPrefixLength) {
            return List.of();
        }

        List<byte[]> addresses = getAddressesInSubnet(iFaceAddress.getAddress().getAddress(), iFaceAddress.getPrefix(),
                true);
        if (addresses.size() > 2) {
            addresses.remove(0); // remove network address
            addresses.remove(addresses.size() - 1); // remove broadcast address
        }

        return addresses.stream().map(m -> {
            try {
                return InetAddress.getByAddress(m);
            } catch (UnknownHostException e) {
                return null;
            }
        }).filter(f -> f != null).sorted((a, b) -> {
            byte[] aOct = a.getAddress();
            byte[] bOct = b.getAddress();
            int r = 0;
            for (int i = 0; i < aOct.length && i < bOct.length; i++) {
                r = Integer.compare(aOct[i] & 0xff, bOct[i] & 0xff);
                if (r != 0) {
                    return r;
                }
            }
            return r;
        }).collect(Collectors.toList());
    }

    /**
     * Recursively calculate each IP address within a subnet
     * 
     * @param address IP address in byte array form (i.e. 127.0.0.1 = 01111111 00000000 00000000 00000001)
     * @param maskLength Network mask length (i.e. the number after the forward-slash, '/', in CIDR notation)
     * @param scrub Whether or not to scrub the unmasked bits of the address
     * @return All possible IP addresses
     * 
     * @author: https://gist.github.com/ssttevee/b0e2b431f4b23d289537
     */
    private static List<byte[]> getAddressesInSubnet(byte[] address, int maskLength, boolean scrub) {
        if (scrub) {
            scrubAddress(address, maskLength);
        }

        if (maskLength >= address.length * 8) {
            return Collections.singletonList(address);
        }

        List<byte[]> addresses = new ArrayList<byte[]>();

        int set = maskLength / 8;
        int pos = maskLength % 8;

        byte[] addressLeft = address.clone();
        addressLeft[set] |= 1 << pos;
        addresses.addAll(getAddressesInSubnet(addressLeft, maskLength + 1, false));

        byte[] addressRight = address.clone();
        addressRight[set] &= ~(1 << pos);
        addresses.addAll(getAddressesInSubnet(addressRight, maskLength + 1, false));

        return addresses;
    }

    /**
     * Set the unmasked bits of the IP address to 0
     * 
     * @param address IP address in byte array form (i.e. 127.0.0.1 = 01111111 00000000 00000000 00000001)
     * @param maskLength Network mask length (i.e. the number after the forward-slash, '/', in CIDR notation)
     * @return The scrubbed IP address
     * 
     * @author: https://gist.github.com/ssttevee/b0e2b431f4b23d289537
     */
    private static byte[] scrubAddress(byte[] address, int maskLength) {
        for (int i = 0; i < address.length * 8; i++) {
            if (i < maskLength)
                continue;

            address[i / 8] &= ~(1 << (i % 8));
        }

        return address;
    }
}
