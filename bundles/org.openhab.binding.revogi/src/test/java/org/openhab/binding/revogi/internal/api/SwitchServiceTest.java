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
package org.openhab.binding.revogi.internal.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.revogi.internal.udp.UdpResponseDTO;
import org.openhab.binding.revogi.internal.udp.UdpSenderService;

/**
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class SwitchServiceTest {

    private UdpSenderService udpSenderService = mock(UdpSenderService.class);
    private SwitchService switchService = new SwitchService(udpSenderService);

    @Test
    public void getStatusSuccesfully() {
        // given
        List<UdpResponseDTO> response = Collections
                .singletonList(new UdpResponseDTO("V3{\"response\":20,\"code\":200}", "127.0.0.1"));
        when(udpSenderService.sendMessage("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}", "127.0.0.1"))
                .thenReturn(CompletableFuture.completedFuture(response));

        // when
        CompletableFuture<SwitchResponseDTO> switchResponse = switchService.switchPort("serial", "127.0.0.1", 1, 1);

        // then
        assertThat(switchResponse.getNow(new SwitchResponseDTO(0, 0)), equalTo(new SwitchResponseDTO(20, 200)));
    }

    @Test
    public void getStatusSuccesfullyWithBroadcast() {
        // given
        List<UdpResponseDTO> response = Collections
                .singletonList(new UdpResponseDTO("V3{\"response\":20,\"code\":200}", "127.0.0.1"));
        when(udpSenderService.broadcastUdpDatagram("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}"))
                .thenReturn(CompletableFuture.completedFuture(response));

        // when
        CompletableFuture<SwitchResponseDTO> switchResponse = switchService.switchPort("serial", "", 1, 1);

        // then
        assertThat(switchResponse.getNow(new SwitchResponseDTO(0, 0)), equalTo(new SwitchResponseDTO(20, 200)));
    }

    @Test
    public void invalidUdpResponse() {
        // given
        List<UdpResponseDTO> response = Collections.singletonList(new UdpResponseDTO("something invalid", "12345"));
        when(udpSenderService.sendMessage("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}", "127.0.0.1"))
                .thenReturn(CompletableFuture.completedFuture(response));

        // when
        CompletableFuture<SwitchResponseDTO> switchResponse = switchService.switchPort("serial", "127.0.0.1", 1, 1);

        // then
        assertThat(switchResponse.getNow(new SwitchResponseDTO(0, 0)), equalTo(new SwitchResponseDTO(0, 503)));
    }

    @Test
    public void getExceptionOnWrongState() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> switchService.switchPort("serial", "127.0.0.1", 1, 12));
    }

    @Test
    public void getExceptionOnWrongPort() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> switchService.switchPort("serial", "127.0.0.1", -1, 1));
    }
}
