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
package org.openhab.binding.matter.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_STATUS_DETAIL_NODE_CHECK_IN_MISSED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_STATUS_DETAIL_NODE_CONNECTING;
import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_STATUS_DETAIL_NODE_NOT_FOUND;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterFirmwareProvider;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.PairingResult;
import org.openhab.binding.matter.internal.client.dto.ws.IcdStateMessage;
import org.openhab.binding.matter.internal.client.dto.ws.IcdStateMessage.IcdEvent;
import org.openhab.binding.matter.internal.client.dto.ws.NodeState;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;

/**
 * Tests for {@link ControllerHandler} node refresh and connection status behavior.
 *
 * @author Bernhard Kaszt - Initial contribution
 * @author Dan Cunningham - Connection status, check-in and pairing tests
 */
@NonNullByDefault
public class ControllerHandlerTest {

    private static final BigInteger NODE_ID = BigInteger.valueOf(42);

    @Mock
    @NonNullByDefault({})
    private Bridge bridge;
    @Mock
    @NonNullByDefault({})
    private MatterWebsocketService websocketService;
    @Mock
    @NonNullByDefault({})
    private TranslationService translationService;
    @Mock
    @NonNullByDefault({})
    private MatterFirmwareProvider firmwareProvider;
    @Mock
    @NonNullByDefault({})
    private MatterControllerClient client;

    @NonNullByDefault({})
    private ControllerHandler handler;

    @BeforeEach
    @SuppressWarnings("null")
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(bridge.getThings()).thenReturn(List.of());
        when(client.initializeNode(any(), any())).thenReturn(completedVoid());
        when(client.requestAllNodeData(any())).thenReturn(completedVoid());

        handler = new ControllerHandler(bridge, websocketService, translationService, firmwareProvider);
        // The constructor creates a real MatterControllerClient; swap it for the mock so we can verify calls.
        setField(handler, "client", client);
        // Mark the handler ready so updateNode() does not short-circuit.
        setField(handler, "ready", Boolean.TRUE);
    }

    @AfterEach
    public void tearDown() {
        ControllerHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.dispose();
        }
    }

    /**
     * A structure change must force a full data refresh even for a sleepy node that was already enumerated.
     * matter.js reports a structure change while the node stays connected; the binding reacts by reconnecting
     * (updateNode -> initializeNode), whose resulting Connected event drives the data request. For sleepy nodes
     * reconnect handling skips that request, so without clearing the "enumerated" marker on the structure
     * change the channels are never rebuilt.
     */
    @Test
    public void structureChangeRefreshesSleepyNode() throws Exception {
        linkEnumeratedNode(NODE_ID, true);

        handler.onEvent(nodeState(NODE_ID, NodeState.STRUCTURECHANGED));
        handler.onEvent(nodeState(NODE_ID, NodeState.CONNECTED));

        verify(client).requestAllNodeData(NODE_ID);
    }

    /**
     * Control: a non-sleepy node is refreshed on reconnect anyway, so the structure change path must keep working
     * for it too.
     */
    @Test
    public void structureChangeRefreshesNonSleepyNode() throws Exception {
        linkEnumeratedNode(NODE_ID, false);

        handler.onEvent(nodeState(NODE_ID, NodeState.STRUCTURECHANGED));
        handler.onEvent(nodeState(NODE_ID, NodeState.CONNECTED));

        verify(client).requestAllNodeData(NODE_ID);
    }

    /**
     * Control: a plain reconnect (Connected without a preceding structure change) of an already enumerated sleepy
     * node must still skip the expensive re-enumeration - that is the optimization the structure-change fix must
     * not regress.
     */
    @Test
    public void reconnectSkipsRefreshForSleepyNode() throws Exception {
        linkEnumeratedNode(NODE_ID, true);

        handler.onEvent(nodeState(NODE_ID, NodeState.CONNECTED));

        verify(client, never()).requestAllNodeData(any());
    }

    /**
     * A plain reconnect (Connected without a preceding structure change) of an already enumerated sleepy
     * node must not set the endpoint status to UNKNOWN before restoring ONLINE.
     */
    @Test
    public void reconnectDoesNotSetUnknownForSleepyNode() throws Exception {
        NodeHandler nodeHandler = mock(NodeHandler.class);
        when(nodeHandler.shouldRefreshOnReconnect()).thenReturn(false);
        when(nodeHandler.getNodeId()).thenReturn(NODE_ID);

        Thing childThing = mock(Thing.class);
        when(childThing.getHandler()).thenReturn(nodeHandler);
        when(bridge.getThings()).thenReturn(List.of(childThing));

        Map<BigInteger, NodeHandler> linkedNodes = getField(handler, "linkedNodes");
        linkedNodes.put(NODE_ID, nodeHandler);
        Set<BigInteger> enumeratedNodes = getField(handler, "enumeratedNodes");
        enumeratedNodes.add(NODE_ID);

        handler.onEvent(nodeState(NODE_ID, NodeState.CONNECTED));

        verify(nodeHandler, never()).setEndpointStatus(eq(ThingStatus.UNKNOWN), any(), any());
        verify(nodeHandler).setEndpointStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    /**
     * matter.js keeps trying to reach a node after initializeNode times out, so the status shows the reason it last
     * reported instead of the timeout.
     */
    @Test
    public void connectionTimeoutKeepsLastConnectionState() throws Exception {
        NodeHandler nodeHandler = linkNodeThing(NODE_ID);
        when(translationService.getTranslation(THING_STATUS_DETAIL_NODE_NOT_FOUND)).thenReturn("not found");
        when(client.initializeNode(any(), any())).thenReturn(CompletableFuture.failedFuture(new TimeoutException()));

        handler.onEvent(nodeState(NODE_ID, NodeState.WAITINGFORDEVICEDISCOVERY));
        handler.updateNode(NODE_ID);

        verify(nodeHandler).setEndpointStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "not found");
    }

    @Test
    public void connectionTimeoutWithoutStateSaysConnecting() throws Exception {
        NodeHandler nodeHandler = linkNodeThing(NODE_ID);
        when(translationService.getTranslation(THING_STATUS_DETAIL_NODE_CONNECTING)).thenReturn("connecting");
        when(client.initializeNode(any(), any())).thenReturn(CompletableFuture.failedFuture(new TimeoutException()));

        handler.updateNode(NODE_ID);

        verify(nodeHandler).setEndpointStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "connecting");
    }

    @Test
    public void reconnectingSchedulesOffline() throws Exception {
        linkNodeThing(NODE_ID);

        handler.onEvent(nodeState(NODE_ID, NodeState.RECONNECTING));

        Map<BigInteger, ?> offlineTimers = getField(handler, "offlineTimers");
        assertTrue(offlineTimers.containsKey(NODE_ID));
    }

    /**
     * A sleepy device that checks in drops its connection while it sleeps. matter.js reports a missed check-in when
     * it is really gone, so no offline timer is started.
     */
    @Test
    public void reconnectingDoesNotScheduleOfflineForCheckInDevice() throws Exception {
        NodeHandler nodeHandler = linkNodeThing(NODE_ID);
        when(nodeHandler.receivesCheckIns()).thenReturn(true);

        handler.onEvent(nodeState(NODE_ID, NodeState.RECONNECTING));

        Map<BigInteger, ?> offlineTimers = getField(handler, "offlineTimers");
        assertFalse(offlineTimers.containsKey(NODE_ID));
    }

    @Test
    public void missedCheckInSetsOffline() throws Exception {
        NodeHandler nodeHandler = linkNodeThing(NODE_ID);
        when(translationService.getTranslation(THING_STATUS_DETAIL_NODE_CHECK_IN_MISSED)).thenReturn("missed");

        handler.onEvent(icdState(NODE_ID, IcdEvent.CHECK_IN_MISSED));

        verify(nodeHandler).updateIcdState(any());
        verify(nodeHandler).setEndpointStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "missed");
    }

    @Test
    public void checkInAfterMissedCheckInSetsOnline() throws Exception {
        NodeHandler nodeHandler = linkNodeThing(NODE_ID);
        Thing thing = nodeHandler.getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(thing.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "missed"));

        handler.onEvent(icdState(NODE_ID, IcdEvent.CHECKED_IN));

        verify(nodeHandler).setEndpointStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    @Test
    public void pairDeviceReturnsCertificateWarnings() throws Exception {
        PairingResult result = new PairingResult();
        result.nodeId = NODE_ID;
        result.warnings = List.of("test certificate");
        when(client.isConnected()).thenReturn(true);
        when(client.pairNode("12345678901")).thenReturn(CompletableFuture.completedFuture(result));

        assertEquals(List.of("test certificate"), handler.pairDevice("12345678901").get(5, TimeUnit.SECONDS));
        verify(client).initializeNode(eq(NODE_ID), any());
    }

    /**
     * Links a node handler whose Thing is a child of the controller, so status updates reach it
     */
    @SuppressWarnings("null")
    private NodeHandler linkNodeThing(BigInteger nodeId) throws Exception {
        NodeHandler nodeHandler = mock(NodeHandler.class);
        when(nodeHandler.getNodeId()).thenReturn(nodeId);
        when(nodeHandler.getOfflineGraceSeconds()).thenReturn(60);
        Bridge nodeThing = mock(Bridge.class);
        when(nodeThing.getHandler()).thenReturn(nodeHandler);
        when(nodeHandler.getThing()).thenReturn(nodeThing);
        when(bridge.getThings()).thenReturn(List.of(nodeThing));
        Map<BigInteger, NodeHandler> linkedNodes = getField(handler, "linkedNodes");
        linkedNodes.put(nodeId, nodeHandler);
        return nodeHandler;
    }

    private static IcdStateMessage icdState(BigInteger nodeId, IcdEvent event) {
        IcdStateMessage message = new IcdStateMessage();
        message.nodeId = nodeId;
        message.event = event;
        message.litSupported = true;
        message.registered = true;
        return message;
    }

    private void linkEnumeratedNode(BigInteger nodeId, boolean sleepy) throws Exception {
        NodeHandler nodeHandler = mock(NodeHandler.class);
        when(nodeHandler.shouldRefreshOnReconnect()).thenReturn(!sleepy);

        Map<BigInteger, NodeHandler> linkedNodes = getField(handler, "linkedNodes");
        linkedNodes.put(nodeId, nodeHandler);
        Set<BigInteger> enumeratedNodes = getField(handler, "enumeratedNodes");
        enumeratedNodes.add(nodeId);
    }

    private static NodeStateMessage nodeState(BigInteger nodeId, NodeState state) {
        NodeStateMessage message = new NodeStateMessage();
        message.nodeId = nodeId;
        message.state = state;
        return message;
    }

    @SuppressWarnings("null")
    private static CompletableFuture<Void> completedVoid() {
        return CompletableFuture.<Void> completedFuture(null);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = ControllerHandler.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String name) throws Exception {
        Field field = ControllerHandler.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
