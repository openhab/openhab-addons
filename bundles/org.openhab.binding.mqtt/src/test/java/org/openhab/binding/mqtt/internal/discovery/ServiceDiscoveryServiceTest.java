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
package org.openhab.binding.mqtt.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.MqttBindingConstants;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.internal.discovery.MqttServiceDiscoveryService}.
 *
 * @author David Graeff - Initial contribution
 */
public class ServiceDiscoveryServiceTest {
    @Mock
    private MqttService service;

    @Mock
    private DiscoveryListener discoverListener;

    @Before
    public void initMocks() throws ConfigurationException {
        MockitoAnnotations.initMocks(this);
        Map<String, MqttBrokerConnection> brokers = new TreeMap<>();
        brokers.put("testname", new MqttBrokerConnection("tcp://123.123.123.123", null, false, null));
        brokers.put("textual", new MqttBrokerConnection("tcp://123.123.123.123", null, true, null));
        when(service.getAllBrokerConnections()).thenReturn(brokers);
    }

    @Test
    public void testDiscovery() throws ConfigurationException {
        // Setting the MqttService will enable the background scanner
        MqttServiceDiscoveryService d = new MqttServiceDiscoveryService();
        d.addDiscoveryListener(discoverListener);
        d.setMqttService(service);
        d.startScan();

        // We expect 3 discoveries. An embedded thing, a textual configured one, a non-textual one
        ArgumentCaptor<DiscoveryResult> discoveryCapture = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoverListener, times(2)).thingDiscovered(eq(d), discoveryCapture.capture());
        List<DiscoveryResult> discoveryResults = discoveryCapture.getAllValues();
        assertThat(discoveryResults.size(), is(2));
        assertThat(discoveryResults.get(0).getThingTypeUID(), is(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER));
        assertThat(discoveryResults.get(1).getThingTypeUID(), is(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER));

        // Add another thing
        d.brokerAdded("anotherone", new MqttBrokerConnection("tcp://123.123.123.123", null, false, null));
        discoveryCapture = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoverListener, times(3)).thingDiscovered(eq(d), discoveryCapture.capture());
        discoveryResults = discoveryCapture.getAllValues();
        assertThat(discoveryResults.size(), is(3));
        assertThat(discoveryResults.get(2).getThingTypeUID(), is(MqttBindingConstants.BRIDGE_TYPE_SYSTEMBROKER));
    }
}
