/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster.InterfaceTypeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster.NetworkInterface;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;

/**
 * Test class for GeneralDiagnosticsConverter
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class GeneralDiagnosticsConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private GeneralDiagnosticsCluster mockCluster;
    @NonNullByDefault({})
    private GeneralDiagnosticsConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.networkInterfaces = new ArrayList<>();
        converter = Mockito.spy(new GeneralDiagnosticsConverter(mockCluster, mockHandler, 1, "TestLabel"));
    }

    @Test
    void testOnEventWithNetworkInterfaces() {
        List<NetworkInterface> networkInterfaces = new ArrayList<>();
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES;
        message.value = networkInterfaces;
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES), Mockito.anyString());
    }

    @Test
    void testOnEventWithNetworkInterfacesNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES), eq(null));
    }

    @Test
    void testInitState() {
        converter.initState();
        // Verify that JSON serialization is called (we can't easily verify the JSON content in unit tests)
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES), Mockito.anyString());
    }

    @Test
    void testInitStateWithNullNetworkInterfaces() {
        mockCluster.networkInterfaces = null;
        converter.initState();
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES), eq(null));
    }

    @Test
    void testInitStateWithPopulatedNetworkInterfaces() {
        List<NetworkInterface> networkInterfaces = new ArrayList<>();
        NetworkInterface eth0 = new NetworkInterface("eth0", true, true, true, null, new ArrayList<>(),
                new ArrayList<>(), InterfaceTypeEnum.ETHERNET);
        NetworkInterface wlan0 = new NetworkInterface("wlan0", true, false, false, null, new ArrayList<>(),
                new ArrayList<>(), InterfaceTypeEnum.WI_FI);
        networkInterfaces.add(eth0);
        networkInterfaces.add(wlan0);
        mockCluster.networkInterfaces = networkInterfaces;
        converter.initState();
        // Verify that JSON serialization is called with the populated list
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES), Mockito.anyString());
    }
}
