/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.network.internal.dhcp;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.junit.jupiter.api.Test;
import org.openhab.binding.network.internal.dhcp.DHCPPacket.BadPacketException;

/**
 * Tests cases for DHCP related functionality
 *
 * @author David Graeff - Initial contribution
 */
public class DHCPTest {
    @Test
    public void testService() throws SocketException {
        String testIP = "10.1.2.3";
        IPRequestReceivedCallback dhcpListener = mock(IPRequestReceivedCallback.class);

        // if this is not the case this test is not very useful, we don't always have the static field under control.
        assumeTrue(DHCPListenService.instance == null);
        DHCPListenService.register(testIP, dhcpListener);
        assertThat(DHCPListenService.instance, is(notNullValue()));
        DHCPListenService.unregister(testIP);
        assertThat(DHCPListenService.instance, is(nullValue()));
    }

    @Test
    public void testReceivePacketCallback() throws BadPacketException, IOException {
        String testIP = "10.1.2.3";
        InetAddress testAddress = InetAddress.getByName(testIP);
        IPRequestReceivedCallback dhcpListener = mock(IPRequestReceivedCallback.class);
        DHCPPacketListenerServer s = new DHCPPacketListenerServer(dhcpListener);
        s.receivePacket(new DHCPPacket(new byte[] { DHCPPacket.DHCPREQUEST }, testAddress.getAddress()), testAddress);
        // Test case if DHCP packet does not contain a DHO_DHCP_REQUESTED_ADDRESS option.
        // The destination IP should be deducted by the UDP address in this case
        s.receivePacket(new DHCPPacket(new byte[] { DHCPPacket.DHCPREQUEST }, null), testAddress);
        verify(dhcpListener, times(2)).dhcpRequestReceived(eq(testIP));
    }
}
