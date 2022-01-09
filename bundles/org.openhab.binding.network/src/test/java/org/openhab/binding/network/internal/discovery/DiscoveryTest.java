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
package org.openhab.binding.network.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.network.internal.NetworkBindingConstants;
import org.openhab.binding.network.internal.PresenceDetectionValue;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class DiscoveryTest {
    private final String ip = "127.0.0.1";

    private @Mock PresenceDetectionValue value;
    private @Mock DiscoveryListener listener;

    @BeforeEach
    public void setUp() {
        when(value.getHostAddress()).thenReturn(ip);
        when(value.getLowestLatency()).thenReturn(10.0);
        when(value.isReachable()).thenReturn(true);
        when(value.getSuccessfulDetectionTypes()).thenReturn("TESTMETHOD");
    }

    @Test
    public void pingDeviceDetected() {
        NetworkDiscoveryService d = new NetworkDiscoveryService();
        d.addDiscoveryListener(listener);

        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);

        // Ping device
        when(value.isPingReachable()).thenReturn(true);
        when(value.isTCPServiceReachable()).thenReturn(false);
        d.partialDetectionResult(value);
        verify(listener).thingDiscovered(any(), result.capture());
        DiscoveryResult dresult = result.getValue();
        assertThat(dresult.getThingUID(), is(NetworkDiscoveryService.createPingUID(ip)));
        assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_HOSTNAME), is(ip));
    }

    @Test
    public void tcpDeviceDetected() {
        NetworkDiscoveryService d = new NetworkDiscoveryService();
        d.addDiscoveryListener(listener);

        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);

        // TCP device
        when(value.isPingReachable()).thenReturn(false);
        when(value.isTCPServiceReachable()).thenReturn(true);
        when(value.getReachableTCPports()).thenReturn(Collections.singletonList(1010));
        d.partialDetectionResult(value);
        verify(listener).thingDiscovered(any(), result.capture());
        DiscoveryResult dresult = result.getValue();
        assertThat(dresult.getThingUID(), is(NetworkDiscoveryService.createServiceUID(ip, 1010)));
        assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_HOSTNAME), is(ip));
        assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_PORT), is(1010));
    }
}
