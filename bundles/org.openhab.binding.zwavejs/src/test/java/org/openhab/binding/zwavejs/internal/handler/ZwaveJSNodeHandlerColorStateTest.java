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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Args;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeSetValueCommand;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSNodeHandlerMock;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSNodeHandlerColorStateTest {

    @ParameterizedTest
    @ValueSource(strings = { "color-switch-color", "color-switch-hex-color" })
    public void testColorInitializesFromUnlinkedSourceChannels(String colorChannelId) throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), colorChannelId);
        try {
            handler.initialize();

            ArgumentCaptor<State> states = ArgumentCaptor.forClass(State.class);
            verify(callback, atLeastOnce())
                    .stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), colorChannelId)), states.capture());
            assertEquals(new HSBType("120,100,42"), states.getValue());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @CsvSource({ "color-switch-color, 0, 0", "color-switch-color, 20, 20", "color-switch-color, 80, 80",
            "color-switch-color, 99, 100", "color-switch-hex-color, 0, 0", "color-switch-hex-color, 20, 20",
            "color-switch-hex-color, 80, 80", "color-switch-hex-color, 99, 100" })
    public void testUnlinkedDimmerUpdatesColor(String colorChannelId, int brightness, int expectedBrightness)
            throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), colorChannelId);
        try {
            handler.initialize();
            clearInvocations(callback);

            assertTrue(
                    handler.onNodeStateChanged(valueEvent(38, "Multilevel Switch", "currentValue", null, brightness)));

            verify(callback).stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), colorChannelId)),
                    eq(new HSBType("120,100," + expectedBrightness)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUnlinkedColorUpdatesCachedHueAndSaturation() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-hex-color");
        try {
            handler.initialize();
            handler.onNodeStateChanged(valueEvent(51, "Color Switch", "currentColor", null,
                    Map.of("red", 64, "green", 64, "blue", 128, "warmWhite", 0)));
            clearInvocations(callback);

            handler.onNodeStateChanged(valueEvent(38, "Multilevel Switch", "currentValue", null, 20));

            verify(callback).stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), "color-switch-hex-color")),
                    eq(new HSBType("240,50,20")));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testColorTemperatureInitializesFromUnlinkedWhiteChannels() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorTemperatureNode(),
                "color-switch-color-temperature");
        try {
            handler.initialize();

            ArgumentCaptor<State> states = ArgumentCaptor.forClass(State.class);
            verify(callback, atLeastOnce()).stateUpdated(
                    eq(new ChannelUID(handler.getThing().getUID(), "color-switch-color-temperature")),
                    states.capture());
            assertEquals(new PercentType(33), states.getValue());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @CsvSource({ "0, 128, 50", "1, 64, 50" })
    public void testUnlinkedWhiteChannelUpdatesColorTemperature(int propertyKey, int value, int expectedTemperature)
            throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorTemperatureNode(),
                "color-switch-color-temperature");
        try {
            handler.initialize();
            clearInvocations(callback);

            assertTrue(handler.onNodeStateChanged(valueEvent(51, "Color Switch", "currentColor", propertyKey, value)));

            verify(callback).stateUpdated(
                    eq(new ChannelUID(handler.getThing().getUID(), "color-switch-color-temperature")),
                    eq(new PercentType(expectedTemperature)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUnrelatedUnlinkedChannelRemainsIgnored() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            clearInvocations(callback);

            assertTrue(handler.onNodeStateChanged(valueEvent(50, "Meter", "value", 66049, 15)));

            verify(callback, never()).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @MethodSource("colorDimmerCommands")
    public void testColorDimmingDoesNotResendRgb(String colorChannelId, Command command, int expectedValue)
            throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), colorChannelId);
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridgeHandler = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridgeHandler);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), colorChannelId), command);

            ArgumentCaptor<BaseCommand> commands = ArgumentCaptor.forClass(BaseCommand.class);
            verify(bridgeHandler).sendCommand(commands.capture());
            NodeSetValueCommand sentCommand = assertInstanceOf(NodeSetValueCommand.class, commands.getValue());
            assertEquals(44, sentCommand.nodeId);
            assertEquals(38, sentCommand.valueId.commandClass);
            assertEquals(0, sentCommand.valueId.endpoint);
            assertEquals("targetValue", sentCommand.valueId.property);
            assertEquals(expectedValue, sentCommand.value);
        } finally {
            handler.dispose();
        }
    }

    private static Stream<Arguments> colorDimmerCommands() {
        return Stream.of("color-switch-color", "color-switch-hex-color")
                .flatMap(channelId -> Stream.of(Arguments.of(channelId, OnOffType.OFF, 0),
                        Arguments.of(channelId, OnOffType.ON, 255), Arguments.of(channelId, new PercentType(20), 20),
                        Arguments.of(channelId, PercentType.HUNDRED, 99),
                        Arguments.of(channelId, new HSBType("120,100,20"), 20),
                        Arguments.of(channelId, new HSBType("120,100,0"), 0),
                        Arguments.of(channelId, new HSBType("240,50,0"), 0)));
    }

    @Test
    public void testHomekitOffSequenceDoesNotSendRgb() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridgeHandler = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridgeHandler);
            ChannelUID colorChannelUID = new ChannelUID(handler.getThing().getUID(), "color-switch-color");

            handler.handleCommand(colorChannelUID, OnOffType.OFF);
            handler.handleCommand(colorChannelUID, new HSBType("120,100,0"));

            ArgumentCaptor<BaseCommand> commands = ArgumentCaptor.forClass(BaseCommand.class);
            verify(bridgeHandler, times(2)).sendCommand(commands.capture());
            for (BaseCommand command : commands.getAllValues()) {
                NodeSetValueCommand sentCommand = assertInstanceOf(NodeSetValueCommand.class, command);
                assertEquals(38, sentCommand.valueId.commandClass);
                assertEquals("targetValue", sentCommand.valueId.property);
                assertEquals(0, sentCommand.value);
            }
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testHueChangeStillSendsColorAndBrightness() throws IOException, InterruptedException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridgeHandler = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridgeHandler);
            CountDownLatch sent = new CountDownLatch(2);
            doAnswer(invocation -> {
                sent.countDown();
                return null;
            }).when(bridgeHandler).sendCommand(any(BaseCommand.class));

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "color-switch-color"),
                    new HSBType("0,100,20"));

            assertTrue(sent.await(5, TimeUnit.SECONDS));
            ArgumentCaptor<BaseCommand> commands = ArgumentCaptor.forClass(BaseCommand.class);
            verify(bridgeHandler, times(2)).sendCommand(commands.capture());
            assertTrue(commands.getAllValues().stream()
                    .anyMatch(command -> command instanceof NodeSetValueCommand set
                            && Integer.valueOf(51).equals(set.valueId.commandClass)
                            && "targetColor".equals(set.valueId.property)
                            && Map.of("red", 255, "green", 0, "blue", 0, "warmWhite", 0).equals(set.value)));
            assertTrue(commands.getAllValues().stream()
                    .anyMatch(command -> command instanceof NodeSetValueCommand set
                            && Integer.valueOf(38).equals(set.valueId.commandClass)
                            && "targetValue".equals(set.valueId.property) && Integer.valueOf(20).equals(set.value)));
        } finally {
            handler.dispose();
        }
    }

    private ZwaveJSNodeHandlerMock createHandler(ThingHandlerCallback callback, Node node, String linkedChannelId) {
        Thing thing = ZwaveJSNodeHandlerMock.mockThing(node.nodeId);
        ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createHandler(callback, thing, "store_4.json", false);
        ChannelUID linkedChannelUID = new ChannelUID(thing.getUID(), linkedChannelId);
        doReturn(false).when(handler).isLinked(anyString());
        doReturn(false).when(handler).isLinked(any(ChannelUID.class));
        doReturn(true).when(handler).isLinked(linkedChannelId);
        doReturn(true).when(handler).isLinked(linkedChannelUID);
        ZwaveJSBridgeHandler bridgeHandler = Objects.requireNonNull(handler.getBridgeHandler());
        doReturn(bridgeHandler).when(handler).getBridgeHandler();
        doReturn(node).when(bridgeHandler).requestNodeDetails(node.nodeId);
        return handler;
    }

    private Node colorNode() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_4.json", 44);
        for (Value value : node.values) {
            if (value.endpoint == 0 && value.commandClass == 38 && "currentValue".equals(value.property)) {
                value.value = 42;
            } else if (value.endpoint == 0 && value.commandClass == 51) {
                if ("currentColor".equals(value.property) && value.propertyKey == null) {
                    value.value = Map.of("red", 0, "green", 255, "blue", 0, "warmWhite", 0);
                } else if ("hexColor".equals(value.property)) {
                    value.value = "00FF00";
                }
            }
        }
        return node;
    }

    private Node colorTemperatureNode() throws IOException {
        Node node = colorNode();
        node.values.stream()
                .filter(value -> value.commandClass == 51 && "currentColor".equals(value.property)
                        && (value.propertyKey instanceof Number key && key.intValue() == 0))
                .findFirst().orElseThrow().value = 64;
        Value coldWhite = DataUtil.getNodeFromStore("store_4.json", 44).values.stream()
                .filter(value -> value.commandClass == 51 && "currentColor".equals(value.property)
                        && (value.propertyKey instanceof Number key && key.intValue() == 0))
                .findFirst().orElseThrow();
        coldWhite.propertyKey = 1;
        coldWhite.propertyKeyName = "Cold White";
        coldWhite.value = 128;
        node.values.add(coldWhite);
        return node;
    }

    private Event valueEvent(int commandClass, String commandClassName, String property, @Nullable Integer propertyKey,
            Object newValue) {
        Event event = new Event();
        event.nodeId = 44;
        event.args = new Args();
        event.args.commandClass = commandClass;
        event.args.commandClassName = commandClassName;
        event.args.endpoint = 0;
        event.args.property = property;
        event.args.propertyName = property;
        event.args.propertyKey = propertyKey;
        event.args.newValue = newValue;
        return event;
    }
}
