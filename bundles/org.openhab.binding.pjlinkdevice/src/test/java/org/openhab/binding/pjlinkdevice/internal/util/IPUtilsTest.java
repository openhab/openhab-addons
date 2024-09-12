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

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.net.CidrAddress;

/**
 * The {@link IPUtilsTest} class defines some static utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class IPUtilsTest {

    @Test
    public void checkValidRangeCount() throws UnknownHostException {
        InetAddress testableAddress = InetAddress.getByName("192.168.1.0");
        List<InetAddress> addresses = IPUtils
                .getAddressesRangeByCidrAddress(new CidrAddress(testableAddress, (short) 24), 24);

        assertEquals(254, addresses.size());
        assertEquals("192.168.1.1", addresses.get(0).getHostAddress());
        assertEquals("192.168.1.254", addresses.get(253).getHostAddress());
    }

    @Test
    public void checkValidLargeRangeCount() throws UnknownHostException {
        InetAddress testableAddress = InetAddress.getByName("127.0.0.0");
        List<InetAddress> addresses = IPUtils
                .getAddressesRangeByCidrAddress(new CidrAddress(testableAddress, (short) 16), 16);

        assertEquals(65534, addresses.size());
        assertEquals("127.0.0.1", addresses.get(0).getHostAddress());
        assertEquals("127.0.255.254", addresses.get(65533).getHostAddress());
    }

    @Test
    public void checkInvalidPrefixLength() throws UnknownHostException {
        InetAddress testableAddress = InetAddress.getByName("192.168.1.0");
        List<InetAddress> addresses = IPUtils
                .getAddressesRangeByCidrAddress(new CidrAddress(testableAddress, (short) 16), 24);

        assertEquals(0, addresses.size());
    }
}
