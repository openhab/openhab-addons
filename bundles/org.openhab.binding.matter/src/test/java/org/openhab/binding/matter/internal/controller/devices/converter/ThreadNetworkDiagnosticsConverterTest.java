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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.RoutingRoleEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for ThreadNetworkDiagnosticsConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ThreadNetworkDiagnosticsConverterTest {

    @Mock
    @NonNullByDefault({})
    private ThreadNetworkDiagnosticsCluster mockCluster;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient mockBridgeClient;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider mockStateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider mockChannelTypeProvider;
    @NonNullByDefault({})
    private TestMatterBaseThingHandler mockHandler;
    @NonNullByDefault({})
    private ThreadNetworkDiagnosticsConverter converter;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHandler = Mockito.spy(new TestMatterBaseThingHandler(mockBridgeClient, mockStateDescriptionProvider,
                mockChannelTypeProvider));
        mockCluster.channel = 15;
        mockCluster.routingRole = ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER;
        mockCluster.networkName = "TestNetwork";
        mockCluster.panId = 0x1234;
        mockCluster.extendedPanId = BigInteger.valueOf(223372036854775807L);
        mockCluster.rloc16 = 0xABCD;
        converter = new ThreadNetworkDiagnosticsConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID thingUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(thingUID);
        assertEquals(6, channels.size());

        // Verify each channel was created with correct UID and item type
        for (Channel channel : channels.keySet()) {
            String uid = channel.getUID().toString();
            if (uid.contains("channel")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-channel", uid);
                assertEquals("Number", channel.getAcceptedItemType());
            } else if (uid.contains("routingrole")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-routingrole", uid);
                assertEquals("Number", channel.getAcceptedItemType());
            } else if (uid.contains("networkname")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-networkname", uid);
                assertEquals("String", channel.getAcceptedItemType());
            } else if (uid.endsWith("-panid")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-panid", uid);
                assertEquals("Number", channel.getAcceptedItemType());
            } else if (uid.endsWith("-extendedpanid")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-extendedpanid", uid);
                assertEquals("Number", channel.getAcceptedItemType());
            } else if (uid.contains("rloc16")) {
                assertEquals("matter:node:test:12345:1#threadnetworkdiagnostics-rloc16", uid);
                assertEquals("Number", channel.getAcceptedItemType());
            }
        }
    }

    @Test
    void testOnEventWithChannel() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL;
        message.value = 15;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL),
                eq(new DecimalType(15)));
    }

    @Test
    void testOnEventWithRoutingRole() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE;
        message.value = RoutingRoleEnum.LEADER;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE),
                eq(new DecimalType(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER.getValue())));
    }

    @Test
    void testOnEventWithNetworkName() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME;
        message.value = "TestNetwork";
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME),
                eq(new StringType("TestNetwork")));
    }

    @Test
    void testOnEventWithPanId() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PAN_ID;
        message.value = 0x1234;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID),
                eq(new DecimalType(0x1234)));
    }

    @Test
    void testOnEventWithExtendedPanId() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXTENDED_PAN_ID;
        message.value = 223372036854775807L;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID),
                eq(new DecimalType(223372036854775807L)));
    }

    @Test
    void testOnEventWithRloc16() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16;
        message.value = 0xABCD;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16),
                eq(new DecimalType(0xABCD)));
    }

    @Test
    void testOnEventWithNonNumberValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_CHANNEL;
        message.value = "invalid";
        converter.onEvent(message);
        // Should not call updateState for non-number values
        verify(mockHandler, times(0)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL),
                eq(new DecimalType(15)));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL),
                eq(new DecimalType(15)));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE),
                eq(new DecimalType(ThreadNetworkDiagnosticsCluster.RoutingRoleEnum.LEADER.getValue())));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME),
                eq(new StringType("TestNetwork")));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID),
                eq(new DecimalType(0x1234)));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID),
                eq(new DecimalType(223372036854775807L)));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16),
                eq(new DecimalType(0xABCD)));
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
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_CHANNEL),
                eq(UnDefType.NULL));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_ROUTINGROLE),
                eq(UnDefType.NULL));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_NETWORKNAME),
                eq(UnDefType.NULL));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_PANID),
                eq(UnDefType.NULL));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_EXTENDEDPANID),
                eq(UnDefType.NULL));
        verify(mockHandler, times(1)).updateState(eq(1), eq(CHANNEL_ID_THREADNETWORKDIAGNOSTICS_RLOC16),
                eq(UnDefType.NULL));
    }
}
