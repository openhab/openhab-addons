/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.RoutingRoleEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;

/**
 * Test class for ThreadNetworkDiagnosticsConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ThreadNetworkDiagnosticsConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ThreadNetworkDiagnosticsCluster mockCluster;
    @NonNullByDefault({})
    private ThreadNetworkDiagnosticsConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.channel = 15;
        mockCluster.routingRole = ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER;
        mockCluster.networkName = "TestNetwork";
        mockCluster.panId = 0x1234; // 4660
        mockCluster.extendedPanId = BigInteger.valueOf(223372036854775807L);
        mockCluster.rloc16 = 0xABCD; // 43981
        converter = Mockito.spy(new ThreadNetworkDiagnosticsConverter(mockCluster, mockHandler, 1, "TestLabel"));
    }

    @Test
    void testOnEventWithRoutingRole() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE;
        message.value = RoutingRoleEnum.LEADER;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE),
                eq(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER));
    }

    @Test
    void testOnEventWithNetworkName() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME;
        message.value = "TestNetwork";
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME), eq("TestNetwork"));
    }

    @Test
    void testOnEventWithPanId() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID;
        message.value = 0x1234;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID),
                eq(4660));
    }

    @Test
    void testOnEventWithExtendedPanId() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID;
        message.value = 223372036854775807L;
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID), eq(223372036854775807L));
    }

    @Test
    void testOnEventWithRloc16() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16;
        message.value = 0xABCD;
        converter.onEvent(message);
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16), eq(43981));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL), eq(15));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE),
                eq(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME), eq("TestNetwork"));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID), eq(4660));
        verify(converter, atLeastOnce()).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID),
                eq(BigInteger.valueOf(223372036854775807L)));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16), eq(43981));
    }

    @Test
    void testInitStateWithNullValues() {
        mockCluster.channel = null;
        mockCluster.routingRole = null;
        mockCluster.networkName = null;
        mockCluster.panId = null;
        mockCluster.extendedPanId = null;
        mockCluster.rloc16 = null;
        converter.initState();
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID), eq(null));
        verify(converter, atLeastOnce())
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16), eq(null));
    }
}
