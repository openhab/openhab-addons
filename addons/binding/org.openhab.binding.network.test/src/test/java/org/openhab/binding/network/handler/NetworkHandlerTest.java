/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.network.NetworkBindingConstants;
import org.openhab.binding.network.internal.NetworkBindingConfiguration;
import org.openhab.binding.network.internal.PresenceDetection;
import org.openhab.binding.network.internal.PresenceDetectionValue;

/**
 * Tests cases for {@link NetworkHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkHandlerTest {
    private ThingUID thingUID = new ThingUID("network", "ttype", "ping");
    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Before
    public void setUp() {
        initMocks(this);
        when(thing.getUID()).thenReturn(thingUID);
    }

    @Test
    public void checkAllConfigurations() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, true, config));
        handler.setCallback(callback);
        // Provide all possible configuration
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_RETRY, 10);
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            conf.put(NetworkBindingConstants.PARAMETER_PORT, 8080);
            conf.put(NetworkBindingConstants.PARAMETER_REFRESH_INTERVAL, 101010);
            conf.put(NetworkBindingConstants.PARAMETER_TIMEOUT, 1234);
            return conf;
        });
        PresenceDetection presenceDetection = spy(new PresenceDetection(handler, 2000));
        // Mock start/stop automatic refresh
        doNothing().when(presenceDetection).startAutomaticRefresh(anyObject());
        doNothing().when(presenceDetection).stopAutomaticRefresh();

        handler.initialize(presenceDetection);
        assertThat(handler.retries, is(10));
        assertThat(presenceDetection.getHostname(), is("127.0.0.1"));
        assertThat(presenceDetection.getServicePorts().iterator().next(), is(8080));
        assertThat(presenceDetection.getRefreshInterval(), is(101010L));
        assertThat(presenceDetection.getTimeout(), is(1234));
    }

    @Test
    public void tcpDeviceInitTests() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, true, config));
        Assert.assertThat(handler.isTCPServiceDevice(), is(true));
        handler.setCallback(callback);
        // Port is missing, should make the device OFFLINE
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            return conf;
        });
        handler.initialize(new PresenceDetection(handler, 2000));
        // Check that we are offline
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        Assert.assertThat(statusInfoCaptor.getValue().getStatusDetail(),
                is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void pingDeviceInitTests() {
        NetworkBindingConfiguration config = new NetworkBindingConfiguration();
        NetworkHandler handler = spy(new NetworkHandler(thing, false, config));
        handler.setCallback(callback);
        // Provide minimal configuration
        when(thing.getConfiguration()).thenAnswer(a -> {
            Configuration conf = new Configuration();
            conf.put(NetworkBindingConstants.PARAMETER_HOSTNAME, "127.0.0.1");
            return conf;
        });
        PresenceDetection presenceDetection = spy(new PresenceDetection(handler, 2000));
        // Mock start/stop automatic refresh
        doNothing().when(presenceDetection).startAutomaticRefresh(anyObject());
        doNothing().when(presenceDetection).stopAutomaticRefresh();

        handler.initialize(presenceDetection);
        // Check that we are online
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(equalTo(ThingStatus.ONLINE)));

        // Mock result value
        PresenceDetectionValue value = mock(PresenceDetectionValue.class);
        when(value.getLowestLatency()).thenReturn(10.0);
        when(value.isReachable()).thenReturn(true);
        when(value.getSuccessfulDetectionTypes()).thenReturn("TESTMETHOD");

        // Partitial result from the PresenceDetection object should affect the
        // ONLINE and LATENCY channel
        handler.partialDetectionResult(value);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_ONLINE)),
                eq(OnOffType.ON));
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_LATENCY)),
                eq(new DecimalType(10.0)));

        // Final result affects the LASTSEEN channel
        when(value.isFinished()).thenReturn(true);
        handler.finalDetectionResult(value);
        verify(callback).stateUpdated(eq(new ChannelUID(thingUID, NetworkBindingConstants.CHANNEL_LASTSEEN)),
                anyObject());
    }
}
