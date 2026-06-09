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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterFirmwareProvider;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.ws.NodeState;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.Bridge;

/**
 * Tests for {@link ControllerHandler} node refresh behavior.
 *
 * @author Bernhard Kaszt - Initial contribution
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
     * requestAllNodeDataIfNeeded skips that request, so without clearing the "enumerated" marker on the structure
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
