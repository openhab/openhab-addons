/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IPUtils} class defines some static IP utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class IPUtils {

    public static String[] getAllIPv4Addresses(@Nullable String IPWithMask) {
        if (IPWithMask == null) {
            return new String[0];
        }
        if (IPWithMask.indexOf("/") < 1) {
            return new String[0];
        }

        String[] splitted = IPWithMask.split("/");

        if (!validateIP(splitted[0]) || !validateSubnet(splitted[1])) {
            return new String[0];
        }
        InetAddress ip;
        try {
            ip = InetAddress.getByName(splitted[0]);
        } catch (UnknownHostException e) {
            return new String[0];
        }

        List<byte[]> xList = getAllAddresses(ip.getAddress(), Integer.parseInt(splitted[1]));
        if (xList.size() > 2) {
            xList.remove(0); // remove network address
            xList.remove(xList.size() - 1); // remove broadcast address
        }
        return xList.stream().map(m -> {
            try {
                return InetAddress.getByAddress(m).getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }).sorted((a, b) -> {
            int[] aOct = Arrays.stream(a.split("\\.")).mapToInt(Integer::parseInt).toArray();
            int[] bOct = Arrays.stream(b.split("\\.")).mapToInt(Integer::parseInt).toArray();
            int r = 0;
            for (int i = 0; i < aOct.length && i < bOct.length; i++) {
                r = Integer.compare(aOct[i], bOct[i]);
                if (r != 0) {
                    return r;
                }
            }
            return r;
        }).collect(Collectors.toList()).toArray(new String[0]);
    }

    public static boolean validateSubnet(final String subnet) {
        final String PATTERN = "((?:[1-9])|(?:[1-2][0-9])|(?:3[0-2]))";
        return subnet.matches(PATTERN);
    }

    private static boolean validateIP(final String ip) {
        final String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    /**
     * Creates a list of all possible IP addresses in a subnet given an IP address and the network mask
     * 
     * @param address IP address in byte array form (i.e. 127.0.0.1 = 01111111 00000000 00000000 00000001)
     * @param maskLength Network mask length (i.e. the number after the forward-slash, '/', in CIDR notation)
     * @return All possible IP addresses
     */
    private static List<byte[]> getAllAddresses(byte[] address, int maskLength) {
        return getAllAddresses(address, maskLength, true);
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
    private static List<byte[]> getAllAddresses(byte[] address, int maskLength, boolean scrub) {
        if (scrub) {
            scrubAddress(address, maskLength);
        }

        // logAddress(address);

        if (maskLength >= address.length * 8) {
            return Collections.singletonList(address);
        }

        List<byte[]> addresses = new ArrayList<byte[]>();

        int set = maskLength / 8;
        int pos = maskLength % 8;

        byte[] addressLeft = address.clone();
        addressLeft[set] |= 1 << pos;
        addresses.addAll(getAllAddresses(addressLeft, maskLength + 1, false));

        byte[] addressRight = address.clone();
        addressRight[set] &= ~(1 << pos);
        addresses.addAll(getAllAddresses(addressRight, maskLength + 1, false));

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
