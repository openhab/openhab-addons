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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpResponse;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;

/**
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiDiscoveryServiceTest {

    private UdpSenderService udpSenderService = mock(UdpSenderService.class);
    private RevogiDiscoveryService revogiDiscoveryService = new RevogiDiscoveryService(udpSenderService);

    @Test
    public void discoverSmartStripSuccesfully() {
        // given
        DiscoveryResponse discoveryResponse = new DiscoveryResponse("1234", "reg", "sak", "Strip", "mac", "5.11");
        List<UdpResponse> discoveryString = Collections.singletonList(new UdpResponse(
                "{\"response\":0,\"data\":{\"sn\":\"1234\",\"regid\":\"reg\",\"sak\":\"sak\",\"name\":\"Strip\",\"mac\":\"mac\",\"ver\":\"5.11\"}}",
                "127.0.0.1"));
        when(udpSenderService.broadcastUpdDatagram("00sw=all,,,;")).thenReturn(discoveryString);

        // when
        List<DiscoveryRawResponse> discoverSmartStrips = revogiDiscoveryService.discoverSmartStrips();

        // then
        assertThat(discoverSmartStrips.size(), equalTo(1));
        assertThat(discoverSmartStrips.get(0).getData(), equalTo(discoveryResponse));
        assertThat(discoverSmartStrips.get(0).getIpAddress(), equalTo("127.0.0.1"));
    }

    @Test
    public void invalidUdpResponse() {
        // given
        List<UdpResponse> discoveryString = Collections.singletonList(new UdpResponse("something invalid", "12345"));
        when(udpSenderService.broadcastUpdDatagram("00sw=all,,,;")).thenReturn(discoveryString);

        // when
        List<DiscoveryRawResponse> discoverSmartStrips = revogiDiscoveryService.discoverSmartStrips();

        // then
        assertThat(discoverSmartStrips.isEmpty(), is(true));
    }
}
