/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.network.NetworkBindingConstants;
import org.openhab.binding.network.internal.PresenceDetectionValue;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
public class DiscoveryTest {
    private final String ip = "127.0.0.1";

    @Mock
    PresenceDetectionValue value;

    @Mock
    DiscoveryListener listener;

    @Before
    public void setUp() {
        initMocks(this);
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
        verify(listener).thingDiscovered(anyObject(), result.capture());
        DiscoveryResult dresult = result.getValue();
        Assert.assertThat(dresult.getThingUID(), is(NetworkDiscoveryService.createPingUID(ip)));
        Assert.assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_HOSTNAME), is(ip));
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
        verify(listener).thingDiscovered(anyObject(), result.capture());
        DiscoveryResult dresult = result.getValue();
        Assert.assertThat(dresult.getThingUID(), is(NetworkDiscoveryService.createServiceUID(ip, 1010)));
        Assert.assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_HOSTNAME), is(ip));
        Assert.assertThat(dresult.getProperties().get(NetworkBindingConstants.PARAMETER_PORT), is(1010));
    }
}
