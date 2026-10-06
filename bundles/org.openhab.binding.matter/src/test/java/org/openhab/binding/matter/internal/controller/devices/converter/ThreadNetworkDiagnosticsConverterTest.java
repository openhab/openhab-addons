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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.NeighborTableStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.RouteTableStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadNetworkDiagnosticsCluster.RoutingRoleEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    void testOnEventWithNeighborTable() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE;
        message.value = new ArrayList<>();
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE), Mockito.anyString());
    }

    @Test
    void testOnEventWithNeighborTableNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE), eq(null));
    }

    @Test
    void testOnEventWithRouteTable() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE;
        message.value = new ArrayList<>();
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE), Mockito.anyString());
    }

    @Test
    void testOnEventWithRouteTableNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE), eq(null));
    }

    @Test
    void testOnEventWithExtAddress() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS;
        message.value = BigInteger.valueOf(1234567890L);
        converter.onEvent(message);
        // Structured attribute should be JSON-serialized
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS), Mockito.anyString());
    }

    @Test
    void testOnEventWithExtAddressNull() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS;
        message.value = null;
        converter.onEvent(message);
        verify(converter, times(1))
                .updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS), eq(null));
    }

    @Test
    void testOnEventWithExtAddressWritesHex() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS;
        message.value = new BigInteger("11129334752758696942");
        converter.onEvent(message);
        verify(converter, times(1)).updateThingAttributeProperty(
                eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS), eq("9A7356FDEC978BEE"));
    }

    @Test
    void testInitStateWritesTableExtAddressesAsHex() {
        mockCluster.neighborTable = List.of(new NeighborTableStruct(new BigInteger("13706561541729176713"), 0L, 29696,
                0L, 0L, 3, -66, -68, 0, 0, true, true, true, false));
        mockCluster.routeTable = List.of(new RouteTableStruct(BigInteger.ZERO, 30720, 30, 63, 0, 0, 0, 0, true, false));
        converter.initState();
        JsonObject neighbor = capturedTable(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE).get(0)
                .getAsJsonObject();
        assertEquals("BE377D1A0ACC4C89", neighbor.get("extAddress").getAsString());
        assertEquals(29696, neighbor.get("rloc16").getAsInt());
        JsonObject route = capturedTable(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE).get(0)
                .getAsJsonObject();
        assertEquals("0000000000000000", route.get("extAddress").getAsString());
    }

    @Test
    void testInitStateDoesNotReadFromDevice() {
        converter.initState();
        verify(mockHandler, never()).readAttributes(any(), anyInt(), anyString(), anyList());
    }

    @Test
    void testPollClusterReadsAllAttributesInOneRequest() {
        ThreadNetworkDiagnosticsCluster reply = new ThreadNetworkDiagnosticsCluster(BigInteger.ONE, 1);
        reply.routingRole = RoutingRoleEnum.ROUTER;
        reply.networkName = "openHAB-Thread";
        reply.partitionId = 3_000_000_000L;
        reply.neighborTable = List.of(new NeighborTableStruct(new BigInteger("11129334752758696942"), 8L, 30720, 0L, 0L,
                3, -65, -66, 18, 0, true, true, true, false));
        reply.routeTable = List.of();
        replyWith(CompletableFuture.completedFuture(reply));

        converter.pollCluster();

        ArgumentCaptor<List<String>> names = ArgumentCaptor.captor();
        verify(mockHandler, times(1)).readAttributes(eq(ThreadNetworkDiagnosticsCluster.class), eq(1),
                eq(ThreadNetworkDiagnosticsCluster.CLUSTER_NAME), names.capture());
        assertTrue(names.getValue()
                .containsAll(List.of(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE,
                        ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME,
                        ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PARTITION_ID,
                        ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE,
                        ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE)));
        verify(converter).updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE),
                eq(RoutingRoleEnum.ROUTER));
        verify(converter).updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NETWORK_NAME),
                eq("openHAB-Thread"));
        verify(converter).updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_PARTITION_ID),
                eq(3_000_000_000L));
        assertEquals("9A7356FDEC978BEE", capturedTable(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_NEIGHBOR_TABLE).get(0)
                .getAsJsonObject().get("extAddress").getAsString());
        verify(converter).updateThingAttributeProperty(eq(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTE_TABLE),
                eq("[]"));
    }

    @Test
    void testPollClusterKeepsAttributesMissingFromReply() {
        replyWith(CompletableFuture.completedFuture(new ThreadNetworkDiagnosticsCluster(BigInteger.ONE, 1)));
        converter.pollCluster();
        verify(converter, never()).updateThingAttributeProperty(any(), any());
    }

    @Test
    void testPollClusterReadsOptionalAttributesOnlyWhenReported() {
        mockCluster.rloc16 = null;
        mockCluster.extAddress = null;
        replyWith(CompletableFuture.completedFuture(new ThreadNetworkDiagnosticsCluster(BigInteger.ONE, 1)));
        converter.pollCluster();
        ArgumentCaptor<List<String>> names = ArgumentCaptor.captor();
        verify(mockHandler).readAttributes(any(), anyInt(), anyString(), names.capture());
        assertFalse(names.getValue().contains(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16));
        assertFalse(names.getValue().contains(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS));

        mockCluster.rloc16 = 0x7400;
        mockCluster.extAddress = BigInteger.TEN;
        converter.pollCluster();
        verify(mockHandler, times(2)).readAttributes(any(), anyInt(), anyString(), names.capture());
        assertTrue(names.getValue().contains(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_RLOC16));
        assertTrue(names.getValue().contains(ThreadNetworkDiagnosticsCluster.ATTRIBUTE_EXT_ADDRESS));
    }

    @Test
    void testPollClusterSkipsWhilePreviousPollRuns() {
        CompletableFuture<ThreadNetworkDiagnosticsCluster> pending = new CompletableFuture<>();
        replyWith(pending);
        converter.pollCluster();
        converter.pollCluster();
        verify(mockHandler, times(1)).readAttributes(any(), anyInt(), anyString(), anyList());

        pending.complete(new ThreadNetworkDiagnosticsCluster(BigInteger.ONE, 1));
        converter.pollCluster();
        verify(mockHandler, times(2)).readAttributes(any(), anyInt(), anyString(), anyList());
    }

    @Test
    void testPollClusterSkipsSleepyEndDevice() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ThreadNetworkDiagnosticsCluster.ATTRIBUTE_ROUTING_ROLE;
        message.value = RoutingRoleEnum.SLEEPY_END_DEVICE;
        converter.onEvent(message);
        converter.pollCluster();
        verify(mockHandler, never()).readAttributes(any(), anyInt(), anyString(), anyList());
    }

    @Test
    void testPollClusterSkipsDeviceThatStartedAsSleepyEndDevice() {
        mockCluster.routingRole = RoutingRoleEnum.SLEEPY_END_DEVICE;
        ThreadNetworkDiagnosticsConverter sleepy = new ThreadNetworkDiagnosticsConverter(mockCluster, mockHandler, 1,
                "TestLabel");
        sleepy.pollCluster();
        verify(mockHandler, never()).readAttributes(any(), anyInt(), anyString(), anyList());
    }

    @Test
    void testPollClusterReadFailureKeepsPropertiesAndAllowsNextPoll() {
        replyWith(CompletableFuture.failedFuture(new IllegalStateException("Busy")));
        converter.pollCluster();
        converter.pollCluster();
        verify(converter, never()).updateThingAttributeProperty(any(), any());
        verify(mockHandler, times(2)).readAttributes(any(), anyInt(), anyString(), anyList());
    }

    private void replyWith(CompletableFuture<ThreadNetworkDiagnosticsCluster> reply) {
        doReturn(reply).when(mockHandler).readAttributes(eq(ThreadNetworkDiagnosticsCluster.class), anyInt(),
                anyString(), anyList());
    }

    private JsonArray capturedTable(String attributeName) {
        ArgumentCaptor<Object> captured = ArgumentCaptor.forClass(Object.class);
        verify(converter).updateThingAttributeProperty(eq(attributeName), captured.capture());
        return JsonParser.parseString(String.valueOf(captured.getValue())).getAsJsonArray();
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
