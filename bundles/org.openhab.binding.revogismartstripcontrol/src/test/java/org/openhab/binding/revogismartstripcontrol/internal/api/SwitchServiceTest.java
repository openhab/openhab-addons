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
package org.openhab.binding.revogismartstripcontrol.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpResponse;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        List<UdpResponse> response = Collections.singletonList(new UdpResponse("V3{\"response\":20,\"code\":200}", "127.0.0.1"));
        when(udpSenderService.sendMessage("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}", "127.0.0.1")).thenReturn(response);

        // when
        SwitchResponse switchResponse = switchService.switchPort("serial", "127.0.0.1", 1, 1);

        // then
        assertThat(switchResponse, equalTo(new SwitchResponse(20, 200)));
    }

    @Test
    public void getStatusSuccesfullyWithBroadcast() {
        // given
        List<UdpResponse> response = Collections.singletonList(new UdpResponse("V3{\"response\":20,\"code\":200}", "127.0.0.1"));
        when(udpSenderService.broadcastUpdDatagram("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}")).thenReturn(response);

        // when
        SwitchResponse switchResponse = switchService.switchPort("serial", "", 1, 1);

        // then
        assertThat(switchResponse, equalTo(new SwitchResponse(20, 200)));
    }

    @Test
    public void invalidUdpResponse() {
        // given
        List<UdpResponse> response = Collections.singletonList(new UdpResponse("something invalid", "12345"));
        when(udpSenderService.sendMessage("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}", "127.0.0.1")).thenReturn(response);

        // when
        SwitchResponse switchResponse = switchService.switchPort("serial", "127.0.0.1", 1, 1);

        // then
        assertThat(switchResponse, equalTo(new SwitchResponse(0, 503)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getExceptionOnWrongState() {
        switchService.switchPort("serial", "127.0.0.1", 1, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getExceptionOnWrongPort() {
        switchService.switchPort("serial", "127.0.0.1", -1, 1);
    }
}