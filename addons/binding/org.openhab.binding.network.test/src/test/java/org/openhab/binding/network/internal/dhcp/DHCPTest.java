/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.dhcp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.junit.Test;
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
        assertThat(DHCPListenService.instance, is(nullValue()));
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
