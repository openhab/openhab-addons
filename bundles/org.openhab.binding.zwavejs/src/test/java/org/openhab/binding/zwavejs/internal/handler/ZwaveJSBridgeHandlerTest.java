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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Args;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Metadata;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Result;
import org.openhab.binding.zwavejs.internal.api.dto.State;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
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
            verify(discoveryService, times(24)).addNodeDiscovery(any());
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
        eventMessage.event.node.ready = true;

        handler.onEvent(eventMessage);

        try {
            verify(discoveryService).addNodeDiscovery(eq(eventMessage.event.node));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testFullStateDefersUnreadyNodeDiscovery() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final NodeDiscoveryService discoveryService = mock(NodeDiscoveryService.class);
        handler.registerDiscoveryListener(discoveryService);

        Node node = new Node();
        node.nodeId = 5;
        node.status = Status.ALIVE;
        node.ready = false;

        ResultMessage resultMessage = new ResultMessage();
        resultMessage.result = new Result();
        resultMessage.result.state = new State();
        resultMessage.result.state.nodes = List.of(node);

        handler.onEvent(resultMessage);

        try {
            verify(discoveryService, never()).addNodeDiscovery(any());
            assertSame(node, handler.requestNodeDetails(node.nodeId));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testReadyEventUpdatesCachedNodeAndNotifiesListener() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final ZwaveNodeListener nodeListener = mock(ZwaveNodeListener.class);
        when(nodeListener.getId()).thenReturn(5);
        handler.registerNodeListener(nodeListener);

        Node node = new Node();
        node.nodeId = 5;
        node.ready = true;

        EventMessage eventMessage = new EventMessage();
        eventMessage.event = new Event();
        eventMessage.event.event = "ready";
        eventMessage.event.nodeId = node.nodeId;
        eventMessage.event.nodeState = node;

        handler.onEvent(eventMessage);

        try {
            assertSame(node, handler.requestNodeDetails(node.nodeId));
            verify(nodeListener).onNodeReady(node);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testReadyEventDiscoversNodeWithoutListener() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final NodeDiscoveryService discoveryService = mock(NodeDiscoveryService.class);
        handler.registerDiscoveryListener(discoveryService);

        Node node = new Node();
        node.nodeId = 5;
        node.ready = true;

        EventMessage eventMessage = new EventMessage();
        eventMessage.event = new Event();
        eventMessage.event.event = "ready";
        eventMessage.event.nodeId = node.nodeId;
        eventMessage.event.nodeState = node;

        handler.onEvent(eventMessage);

        try {
            verify(discoveryService).addNodeDiscovery(node);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testMetadataUpdateFiltersUnchangedGeneratedDefinition() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final ZwaveNodeListener nodeListener = mock(ZwaveNodeListener.class);
        when(nodeListener.getId()).thenReturn(5);
        handler.registerNodeListener(nodeListener);

        Value value = createValue();
        Node node = createReadyNode(value);
        handler.onEvent(createReadyEvent(node));
        clearInvocations(nodeListener);

        Metadata unchanged = createMetadata("Level");
        unchanged.comments = "Changed upstream comment";
        handler.onEvent(createDefinitionEvent("metadata updated", unchanged, null));

        try {
            verify(nodeListener, never()).onNodeDefinitionChanged(any());
            assertSame(unchanged, Objects.requireNonNull(handler.requestNodeDetails(5)).values.get(0).metadata);

            handler.onEvent(createDefinitionEvent("metadata updated", createMetadata("Updated level"), null));
            verify(nodeListener).onNodeDefinitionChanged(node);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testValueAddedPairsWithMetadataAndValueRemovedIsFiltered() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final ZwaveNodeListener nodeListener = mock(ZwaveNodeListener.class);
        when(nodeListener.getId()).thenReturn(5);
        handler.registerNodeListener(nodeListener);

        Node node = createReadyNode();
        handler.onEvent(createReadyEvent(node));
        clearInvocations(nodeListener);

        handler.onEvent(createDefinitionEvent("metadata updated", createMetadata("Level"), null));
        verify(nodeListener, never()).onNodeDefinitionChanged(any());
        assertEquals(0, node.values.size());

        handler.onEvent(createDefinitionEvent("value added", null, 42));
        verify(nodeListener).onNodeDefinitionChanged(node);
        assertEquals(1, node.values.size());
        assertEquals(42, node.values.get(0).value);

        clearInvocations(nodeListener);
        handler.onEvent(createDefinitionEvent("value removed", null, null));
        verify(nodeListener).onNodeDefinitionChanged(node);
        assertEquals(0, node.values.size());

        clearInvocations(nodeListener);
        handler.onEvent(createDefinitionEvent("value removed", null, null));
        try {
            verify(nodeListener, never()).onNodeDefinitionChanged(any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testOnEventWithEventMessageValueNotification() throws IOException {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandlerMock handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        ZwaveNodeListener nodeListener = mock(ZwaveNodeListener.class);
        when(nodeListener.getId()).thenReturn(60);
        handler.registerNodeListener(nodeListener);

        EventMessage eventMessage = DataUtil.fromJson("event_node_60_scene_activation_notification.json",
                EventMessage.class);

        handler.onEvent(eventMessage);

        try {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(nodeListener).onNodeStateChanged(eventCaptor.capture());
            assertEquals(16L, eventCaptor.getValue().args.newValue);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNormalizeValueNotificationCopiesValueToNewValue() {
        Event event = new Event();
        event.args = new Args();
        event.args.value = 16;

        Event result = ZwaveJSBridgeHandler.normalizeValueNotification(event);

        assertEquals(16, result.args.newValue);
    }

    @Test
    public void testNormalizeValueNotificationKeepsExistingNewValue() {
        Event event = new Event();
        event.args = new Args();
        event.args.newValue = 42;
        event.args.value = 16;

        Event result = ZwaveJSBridgeHandler.normalizeValueNotification(event);

        assertEquals(42, result.args.newValue);
    }

    @ParameterizedTest
    @CsvSource({ "true, true", "false, false", "TRUE, true", "False, false", "'  true  ', true" })
    public void testConvertValueTypeBoolean(String input, boolean expected) {
        Object result = ZwaveJSBridgeHandler.convertValueType(input);
        assertInstanceOf(Boolean.class, result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({ "42, 42.0", "42.5, 42.5", "-3.14, -3.14", "0, 0.0", "'  7  ', 7.0", "1, 1.0" })
    public void testConvertValueTypeNumber(String input, double expected) {
        Object result = ZwaveJSBridgeHandler.convertValueType(input);
        assertInstanceOf(Double.class, result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "hello", "on", "off", "yes", "no", "3abc", "   trimme   " })
    public void testConvertValueTypeString(String input) {
        Object result = ZwaveJSBridgeHandler.convertValueType(input);
        assertInstanceOf(String.class, result);
        assertEquals(input.trim(), result);
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

        handler.dispose();
    }

    private static Value createValue() {
        Value value = new Value();
        value.endpoint = 0;
        value.commandClass = 38;
        value.commandClassName = "Multilevel Switch";
        value.property = "currentValue";
        value.propertyName = "currentValue";
        value.metadata = createMetadata("Level");
        value.value = 42;
        return value;
    }

    private static Metadata createMetadata(String label) {
        Metadata metadata = new Metadata();
        metadata.type = MetadataType.NUMBER;
        metadata.readable = true;
        metadata.label = label;
        metadata.min = 0;
        metadata.max = 99L;
        return metadata;
    }

    private static Node createReadyNode(Value... values) {
        Node node = new Node();
        node.nodeId = 5;
        node.ready = true;
        node.values = List.of(values);
        return node;
    }

    private static EventMessage createReadyEvent(Node node) {
        EventMessage message = new EventMessage();
        message.event = new Event();
        message.event.event = "ready";
        message.event.nodeId = node.nodeId;
        message.event.nodeState = node;
        return message;
    }

    private static EventMessage createDefinitionEvent(String eventType, @Nullable Metadata metadata,
            @Nullable Object value) {
        EventMessage message = new EventMessage();
        message.event = new Event();
        message.event.event = eventType;
        message.event.nodeId = 5;
        message.event.args = new Args();
        message.event.args.endpoint = 0;
        message.event.args.commandClass = 38;
        message.event.args.commandClassName = "Multilevel Switch";
        message.event.args.property = "currentValue";
        message.event.args.propertyName = "currentValue";
        message.event.args.metadata = metadata;
        message.event.args.newValue = value;
        return message;
    }
}
