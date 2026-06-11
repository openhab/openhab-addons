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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NetworkCommissioningCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NetworkCommissioningCluster.NetworkCommissioningStatusEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NetworkCommissioningCluster.NetworkInfoStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.NetworkCommissioningCluster.WiFiBandEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;

/**
 * Test class for NetworkCommissioningConverter
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class NetworkCommissioningConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private NetworkCommissioningCluster mockCluster;
    @NonNullByDefault({})
    private NetworkCommissioningConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.maxNetworks = 5;
        mockCluster.scanMaxTimeSeconds = 30;
        mockCluster.connectMaxTimeSeconds = 60;
        mockCluster.interfaceEnabled = true;
        mockCluster.lastNetworkingStatus = NetworkCommissioningStatusEnum.SUCCESS;
        mockCluster.lastConnectErrorValue = 0;
        mockCluster.threadVersion = 4;
        mockCluster.networks = new ArrayList<>();
        mockCluster.supportedWiFiBands = new ArrayList<>();
        converter = Mockito.spy(new NetworkCommissioningConverter(mockCluster, mockHandler, 1, "TestLabel"));
    }

    @Test
    void testOnEventWithMaxNetworks() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS;
        message.value = 5;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS),
                eq(5));
    }

    @Test
    void testOnEventWithScanMaxTimeSeconds() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS;
        message.value = 30;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS), eq(30));
    }

    @Test
    void testOnEventWithConnectMaxTimeSeconds() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS;
        message.value = 60;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS), eq(60));
    }

    @Test
    void testOnEventWithInterfaceEnabled() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED;
        message.value = true;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED), eq(true));
    }

    @Test
    void testOnEventWithLastNetworkingStatus() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS;
        message.value = NetworkCommissioningStatusEnum.SUCCESS;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS),
                eq(NetworkCommissioningStatusEnum.SUCCESS));
    }

    @Test
    void testOnEventWithLastConnectErrorValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE;
        message.value = 0;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE), eq(0));
    }

    @Test
    void testOnEventWithThreadVersion() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION;
        message.value = 4;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION), eq(4));
    }

    @Test
    void testOnEventWithNetworks() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_NETWORKS;
        message.value = new ArrayList<>();
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_NETWORKS),
                Mockito.anyString());
    }

    @Test
    void testOnEventWithNetworksNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_NETWORKS;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_NETWORKS),
                eq(null));
    }

    @Test
    void testOnEventWithSupportedWiFiBands() {
        List<WiFiBandEnum> wiFiBands = new ArrayList<>();
        wiFiBands.add(WiFiBandEnum.V2G4);
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS;
        message.value = wiFiBands;
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS), Mockito.anyString());
    }

    @Test
    void testOnEventWithSupportedWiFiBandsNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS), eq(null));
    }

    @Test
    void testOnEventWithSupportedThreadFeatures() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES;
        message.value = new ArrayList<>();
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES), Mockito.anyString());
    }

    @Test
    void testOnEventWithSupportedThreadFeaturesNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES), eq(null));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS), eq(5));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS), eq(30));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS), eq(60));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED), eq(true));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS),
                eq(NetworkCommissioningStatusEnum.SUCCESS));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE), eq(0));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION), eq(4));
    }

    @Test
    void testInitStateWithNullValues() {
        mockCluster.maxNetworks = null;
        mockCluster.scanMaxTimeSeconds = null;
        mockCluster.connectMaxTimeSeconds = null;
        mockCluster.interfaceEnabled = null;
        mockCluster.lastNetworkingStatus = null;
        mockCluster.lastConnectErrorValue = null;
        mockCluster.threadVersion = null;
        mockCluster.networks = null;
        mockCluster.lastNetworkId = null;
        mockCluster.supportedWiFiBands = null;
        mockCluster.supportedThreadFeatures = null;
        converter.initState();
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_MAX_NETWORKS), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SCAN_MAX_TIME_SECONDS), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_CONNECT_MAX_TIME_SECONDS), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_INTERFACE_ENABLED), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORKING_STATUS), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_CONNECT_ERROR_VALUE), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_THREAD_VERSION), eq(null));
        // Verify JSON-serialized fields pass null instead of the string "null" to remove properties
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_NETWORKS), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_LAST_NETWORK_ID), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS), eq(null));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_THREAD_FEATURES), eq(null));
    }

    @Test
    void testInitStateWithNetworksAndWiFiBands() {
        List<NetworkInfoStruct> networks = new ArrayList<>();
        List<WiFiBandEnum> wiFiBands = new ArrayList<>();
        wiFiBands.add(WiFiBandEnum.V2G4);
        wiFiBands.add(WiFiBandEnum.V5G);
        mockCluster.networks = networks;
        mockCluster.supportedWiFiBands = wiFiBands;
        converter.initState();
        // Verify that JSON serialization is called (we can't easily verify the JSON content in unit tests)
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(NetworkCommissioningCluster.ATTRIBUTE_NETWORKS), Mockito.anyString());
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(NetworkCommissioningCluster.ATTRIBUTE_SUPPORTED_WI_FI_BANDS), Mockito.anyString());
    }
}
