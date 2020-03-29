/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal.udp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class UdpSenderServiceTest {

    private final DatagramSocketWrapper datagramSocketWrapper = mock(DatagramSocketWrapper.class);

    private final UdpSenderService udpSenderService = new UdpSenderService(datagramSocketWrapper);

    private int numberOfInterfaces = 0;

    @Before
    public void setUp() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp()) {
                numberOfInterfaces += (int) networkInterface.getInterfaceAddresses().stream()
                        .filter(interfaceAddress -> interfaceAddress.getBroadcast() != null)
                        .map(InterfaceAddress::getBroadcast).count();
            }
        }
    }

    @Test
    public void testTimeout() throws IOException {
        // given
        doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());

        // when
        List<UdpResponse> list = udpSenderService.broadcastUpdDatagram("send something");

        // then
        assertThat(list.isEmpty(), is(true));
        verify(datagramSocketWrapper, times(numberOfInterfaces * 2)).receiveAnswer(any());
    }

    @Test
    public void testOneAnswer() throws IOException {
        // given
        byte[] receivedBuf = "valid answer".getBytes();
        doAnswer(invocation -> {
            DatagramPacket argument = invocation.getArgument(0);
            argument.setData(receivedBuf);
            argument.setAddress(InetAddress.getLocalHost());
            return null;
        }).doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());

        // when
        List<UdpResponse> list = udpSenderService.broadcastUpdDatagram("send something");

        // then
        assertThat(list.get(0).getAnswer(), is("valid answer"));
        verify(datagramSocketWrapper, times(1 + 2 * numberOfInterfaces)).receiveAnswer(any());
    }

}
