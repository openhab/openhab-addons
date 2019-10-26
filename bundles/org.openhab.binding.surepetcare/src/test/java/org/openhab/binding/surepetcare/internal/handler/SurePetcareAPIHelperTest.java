/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;

/**
 * The {@link SurePetcareAPIHelperTest} class implements unit test case for {@link SurePetcareUtils}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareAPIHelperTest extends SurePetcareAPIHelper {

    public static final byte[] TEST_MAC_ADDRESS_1 = new byte[] { (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
            (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88 };

    public static final byte[] TEST_MAC_ADDRESS_2 = new byte[] { (byte) 0xff, (byte) 0xee, (byte) 0xdd, (byte) 0xcc,
            (byte) 0xbb, (byte) 0xaa, (byte) 0x99, (byte) 0x88 };

    public static final String TEST_HOST_NAME = "orion";

    private static InetAddress localHostAddress;
    private static NetworkInterface netif1, netif2;
    private static SurePetcareAPIHelper api = new SurePetcareAPIHelper();

    @BeforeClass
    public static void classSetup() {
        try {
            // create a mock localhost address returning the test host name
            localHostAddress = mock(InetAddress.class);
            when(localHostAddress.getHostName()).thenReturn(TEST_HOST_NAME);

            // create a mock network interface 1 returning the MAC_ADDRESS_1
            netif1 = mock(NetworkInterface.class);
            when(netif1.getHardwareAddress()).thenReturn(TEST_MAC_ADDRESS_1);

            // create a mock network interface 2 returning the MAC_ADDRESS_2
            netif2 = mock(NetworkInterface.class);
            when(netif2.getHardwareAddress()).thenReturn(TEST_MAC_ADDRESS_2);
        } catch (SocketException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetDeviceIdSingleNetworkInterface() {
        // create a single network interface list with the above MAC address
        List<NetworkInterface> interfaces = new ArrayList<>();
        interfaces.add(netif1);

        int deviceId = api.getDeviceId(Collections.enumeration(interfaces), localHostAddress);
        assertEquals(2007686672, deviceId);
    }

    @Test
    public void testGetDeviceIdMultipleNetworkInterfaces() {
        // create a network interface list with 2 interfaces
        List<NetworkInterface> interfaces = new ArrayList<>();
        interfaces.add(netif2);
        interfaces.add(netif1);

        int deviceId = api.getDeviceId(Collections.enumeration(interfaces), localHostAddress);
        assertEquals(1148693214, deviceId);
    }

    @Test
    public void testGetDeviceIdNoNetworkInterface() {
        // create an empty network interface list
        List<NetworkInterface> interfaces = new ArrayList<>();

        int deviceId = api.getDeviceId(Collections.enumeration(interfaces), localHostAddress);
        assertEquals(106011461, deviceId);
    }

}
