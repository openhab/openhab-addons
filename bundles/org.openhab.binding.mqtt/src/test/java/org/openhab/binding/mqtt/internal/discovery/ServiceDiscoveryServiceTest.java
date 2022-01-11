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
package org.openhab.binding.mqtt.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttService;

/**
 * Tests cases for {@link org.openhab.binding.mqtt.internal.discovery.MqttServiceDiscoveryService}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ServiceDiscoveryServiceTest {

    private @Mock MqttService service;
    private @Mock DiscoveryListener discoverListener;

    @BeforeEach
    public void initMocks() {
        Map<String, MqttBrokerConnection> brokers = new TreeMap<>();
        brokers.put("testname", new MqttBrokerConnection("tcp://123.123.123.123", null, false, null));
        brokers.put("textual", new MqttBrokerConnection("tcp://123.123.123.123", null, true, null));
        when(service.getAllBrokerConnections()).thenReturn(brokers);
    }

    @Test
    public void testDiscovery() {
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
