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
package org.openhab.binding.zwavejs.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Result;
import org.openhab.binding.zwavejs.internal.api.dto.State;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.api.dto.messages.VersionMessage;
import org.openhab.binding.zwavejs.internal.discovery.NodeDiscoveryService;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSBridgeHandlerMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeHandlerTest {

    @Test
    public void testInvalidConfiguration() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testValidConfiguration() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("loclahost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testDiscoveryForActiveNodes() throws IOException {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final NodeDiscoveryService discoveryService = mock(NodeDiscoveryService.class);
        doNothing().when(handler).getFullState();
        handler.registerDiscoveryListener(discoveryService);

        ResultMessage resultMessage = DataUtil.fromJson("store_4.json", ResultMessage.class);

        handler.onEvent(resultMessage);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(discoveryService, times(29)).addNodeDiscovery(any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithVersionMessage() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        VersionMessage versionMessage = new VersionMessage();
        versionMessage.driverVersion = "1.0.0";
        versionMessage.serverVersion = "1.2.3";
        versionMessage.minSchemaVersion = 1;
        versionMessage.maxSchemaVersion = 3;
        versionMessage.homeId = 12345;

        handler.onEvent(versionMessage);

        try {
            verify(thing).setProperties(argThat(properties -> properties.containsKey("driverVersion")
                    && properties.get("driverVersion").equals("1.0.0") && properties.containsKey("serverVersion")
                    && properties.get("serverVersion").equals("1.2.3") && properties.containsKey("minSchemaVersion")
                    && properties.get("minSchemaVersion").equals("1") && properties.containsKey("maxSchemaVersion")
                    && properties.get("maxSchemaVersion").equals("3") && properties.containsKey("homeId")
                    && properties.get("homeId").equals("12345")));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithResultMessageGetValue() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        ZwaveNodeListener nodeListener = mock(ZwaveNodeListener.class);
        when(nodeListener.getId()).thenReturn(3);
        handler.registerNodeListener(nodeListener);

        ResultMessage resultMessage = new ResultMessage();
        resultMessage.messageId = "getvalue|1|2|CommandClass|propertyKey|propertyName|3";
        resultMessage.result = new Result();
        resultMessage.result.value = "testValue";

        handler.onEvent(resultMessage);

        try {
            verify(nodeListener).onNodeStateChanged(any(Event.class));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithResultMessageStateUpdate() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        ResultMessage resultMessage = new ResultMessage();
        resultMessage.result = new Result();
        resultMessage.result.state = new State();
        resultMessage.result.state.nodes = List.of(new Node() {
            {
                nodeId = 1;
                status = Status.ALIVE;
            }
        });

        handler.onEvent(resultMessage);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithEventMessageNodeAdded() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final NodeDiscoveryService discoveryService = mock(NodeDiscoveryService.class);
        handler.registerDiscoveryListener(discoveryService);

        EventMessage eventMessage = new EventMessage();
        eventMessage.event = new Event();
        eventMessage.event.event = "node added";
        eventMessage.event.node = new Node();
        eventMessage.event.node.nodeId = 5;
        eventMessage.event.node.status = Status.ALIVE;

        handler.onEvent(eventMessage);

        try {
            verify(discoveryService).addNodeDiscovery(eq(eventMessage.event.node));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithUnhandledEventType() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        EventMessage eventMessage = new EventMessage();
        eventMessage.event = new Event();
        eventMessage.event.event = "unhandled event";

        handler.onEvent(eventMessage);

        try {
            // No specific verification, just ensuring no exceptions are thrown
        } finally {
            handler.dispose();
        }
    }
}
