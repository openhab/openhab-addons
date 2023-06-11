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
package org.openhab.binding.revogi.internal.udp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.net.NetUtil;

/**
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class UdpSenderServiceTest {

    private final DatagramSocketWrapper datagramSocketWrapper = mock(DatagramSocketWrapper.class);

    private final UdpSenderService udpSenderService = new UdpSenderService(datagramSocketWrapper, 1L);

    private final int numberOfInterfaces = NetUtil.getAllBroadcastAddresses().size();

    @Test
    public void testTimeout() throws IOException, ExecutionException, InterruptedException {
        // given
        doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());

        // when
        CompletableFuture<List<UdpResponseDTO>> list = udpSenderService.broadcastUdpDatagram("send something");

        // then
        assertThat(list.get(), equalTo(Collections.emptyList()));
        verify(datagramSocketWrapper, times(numberOfInterfaces * 2)).receiveAnswer(any());
    }

    @Test
    public void testOneAnswer() throws IOException, ExecutionException, InterruptedException {
        // given
        byte[] receivedBuf = "valid answer".getBytes();
        doAnswer(invocation -> {
            DatagramPacket argument = invocation.getArgument(0);
            argument.setData(receivedBuf);
            argument.setAddress(InetAddress.getLocalHost());
            return null;
        }).doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());

        // when
        CompletableFuture<List<UdpResponseDTO>> future = udpSenderService.broadcastUdpDatagram("send something");

        // then
        List<UdpResponseDTO> udpResponses = future.get();
        assertThat(udpResponses.get(0).getAnswer(), is("valid answer"));
        verify(datagramSocketWrapper, times(1 + 2 * numberOfInterfaces)).receiveAnswer(any());
    }
}
