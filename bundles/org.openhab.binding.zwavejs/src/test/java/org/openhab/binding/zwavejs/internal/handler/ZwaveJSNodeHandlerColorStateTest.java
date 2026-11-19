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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
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
import org.mockito.MockedStatic;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Args;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeSetValueCommand;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSNodeHandlerMock;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.ColorUtil;

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
    public void testUnlinkedColorUpdatePreservesOtherRepresentation() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-hex-color");
        try {
            handler.initialize();
            handler.onNodeStateChanged(valueEvent(51, "Color Switch", "currentColor", null,
                    Map.of("red", 64, "green", 64, "blue", 128, "warmWhite", 0)));
            clearInvocations(callback);

            handler.onNodeStateChanged(valueEvent(38, "Multilevel Switch", "currentValue", null, 20));

            verify(callback).stateUpdated(eq(new ChannelUID(handler.getThing().getUID(), "color-switch-hex-color")),
                    eq(new HSBType("120,100,20")));
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
    public void testHueChangeSendsColorBeforeBrightness() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "color-switch-color"),
                    new HSBType("0,100,20"));

            List<NodeSetValueCommand> commands = sentValueCommands(bridge);
            assertEquals(2, commands.size());
            assertEquals(51, commands.getFirst().valueId.commandClass);
            assertEquals("targetColor", commands.getFirst().valueId.property);
            assertEquals(Map.of("red", 255, "green", 0, "blue", 0, "warmWhite", 0), commands.getFirst().value);
            assertEquals(38, commands.getLast().valueId.commandClass);
            assertEquals("targetValue", commands.getLast().valueId.property);
            assertEquals(20, commands.getLast().value);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testPendingBrightnessCannotUndoOff() throws IOException {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        List<Runnable> pending = new ArrayList<>();
        doAnswer(invocation -> {
            pending.add(invocation.getArgument(0));
            return mock(Future.class);
        }).when(scheduler).submit(any(Runnable.class));
        try (MockedStatic<ThreadPoolManager> pools = mockStatic(ThreadPoolManager.class, CALLS_REAL_METHODS)) {
            pools.when(() -> ThreadPoolManager.getScheduledPool(anyString())).thenReturn(scheduler);
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
            try {
                handler.initialize();
                ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
                clearInvocations(bridge);
                ChannelUID uid = new ChannelUID(handler.getThing().getUID(), "color-switch-color");

                handler.handleCommand(uid, new HSBType("0,100,20"));
                handler.handleCommand(uid, OnOffType.OFF);
                pending.forEach(Runnable::run);

                List<NodeSetValueCommand> commands = sentValueCommands(bridge);
                NodeSetValueCommand lastCommand = commands.getLast();
                assertEquals(38, lastCommand.valueId.commandClass);
                assertEquals(0, lastCommand.value);
            } finally {
                handler.dispose();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "color-switch-color", "color-switch-hex-color" })
    public void testRgbQuantizationDoesNotTurnDimmingIntoColorChange(String channelId) throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), channelId);
        try {
            handler.initialize();
            handler.onNodeStateChanged(valueEvent(51, "Color Switch", "currentColor", null,
                    Map.of("red", 255, "green", 128, "blue", 0, "warmWhite", 0)));
            handler.onNodeStateChanged(valueEvent(51, "Color Switch", "hexColor", null, "FF8000"));
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), channelId), new HSBType("30,100,20"));

            List<NodeSetValueCommand> commands = sentValueCommands(bridge);
            assertEquals(1, commands.size());
            assertEquals(38, commands.getFirst().valueId.commandClass);
            assertEquals(20, commands.getFirst().value);
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUnconfirmedColorChangeIsRetried() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);
            ChannelUID uid = new ChannelUID(handler.getThing().getUID(), "color-switch-color");

            handler.handleCommand(uid, new HSBType("0,100,20"));
            handler.handleCommand(uid, new HSBType("0,100,20"));

            assertEquals(2, sentValueCommands(bridge).stream()
                    .filter(command -> Integer.valueOf(51).equals(command.valueId.commandClass)).count());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "color-switch-color", "color-switch-hex-color" })
    public void testReportedBlackDoesNotConfirmWhite(String channelId) throws IOException {
        Node node = colorNode();
        setReportedColor(node, Map.of("red", 0, "green", 0, "blue", 0, "warmWhite", 0), "000000");
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, node, channelId);
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), channelId), new HSBType("0,0,20"));

            assertTrue(sentValueCommands(bridge).stream()
                    .anyMatch(command -> Integer.valueOf(51).equals(command.valueId.commandClass)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUnknownReportInvalidatesConfirmedColor() throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            handler.onNodeStateChanged(valueEvent(51, "Color Switch", "currentColor", null, null));
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "color-switch-color"),
                    new HSBType("120,100,20"));

            assertTrue(sentValueCommands(bridge).stream()
                    .anyMatch(command -> Integer.valueOf(51).equals(command.valueId.commandClass)));
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 255 })
    public void testWhiteComponentReportDoesNotConfirmRgb(int red) throws IOException {
        Node node = colorNode();
        setReportedColor(node, Map.of("red", red, "green", 0, "blue", 0, "warmWhite", 255), "000000");
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, node, "color-switch-color");
        try {
            handler.initialize();
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);
            HSBType white = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(2500));

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "color-switch-color"),
                    new HSBType(white.getHue(), white.getSaturation(), new PercentType(20)));

            assertTrue(sentValueCommands(bridge).stream()
                    .anyMatch(command -> Integer.valueOf(51).equals(command.valueId.commandClass)));
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @CsvSource({ "OFF, 99", "ON, 0" })
    public void testColorSwitchingHonorsDimmerInversion(String command, int expectedValue) throws IOException {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, colorNode(), "color-switch-color");
        try {
            handler.initialize();
            Objects.requireNonNull(handler.getThing().getChannel("multilevel-switch-value")).getConfiguration()
                    .put("inverted", true);
            ZwaveJSBridgeHandler bridge = Objects.requireNonNull(handler.getBridgeHandler());
            clearInvocations(bridge);

            handler.handleCommand(new ChannelUID(handler.getThing().getUID(), "color-switch-color"),
                    OnOffType.valueOf(command));

            List<NodeSetValueCommand> commands = sentValueCommands(bridge);
            assertEquals(1, commands.size());
            assertEquals(expectedValue, commands.getFirst().value);
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testAggregateAndHexKeepTheirOwnReportedColor(boolean hexFirst) throws IOException {
        Node node = colorNode();
        Map<String, Integer> warmWhite = Map.of("red", 0, "green", 0, "blue", 0, "warmWhite", 255);
        setReportedColor(node, warmWhite, "000000");
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ZwaveJSNodeHandlerMock handler = createHandler(callback, node, "color-switch-color", "color-switch-hex-color");
        try {
            handler.initialize();
            assertWhiteRepresentations(callback, handler, 42);
            clearInvocations(callback);
            Event aggregateEvent = valueEvent(51, "Color Switch", "currentColor", null, warmWhite);
            Event hexEvent = valueEvent(51, "Color Switch", "hexColor", null, "000000");
            handler.onNodeStateChanged(hexFirst ? hexEvent : aggregateEvent);
            handler.onNodeStateChanged(hexFirst ? aggregateEvent : hexEvent);
            clearInvocations(callback);

            handler.onNodeStateChanged(valueEvent(38, "Multilevel Switch", "currentValue", null, 17));

            assertWhiteRepresentations(callback, handler, 17);
        } finally {
            handler.dispose();
        }
    }

    private void assertWhiteRepresentations(ThingHandlerCallback callback, ZwaveJSNodeHandlerMock handler,
            int brightness) {
        HSBType white = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(2500));
        ArgumentCaptor<State> aggregateStates = ArgumentCaptor.forClass(State.class);
        verify(callback, atLeastOnce()).stateUpdated(
                eq(new ChannelUID(handler.getThing().getUID(), "color-switch-color")), aggregateStates.capture());
        assertEquals(new HSBType(white.getHue(), white.getSaturation(), new PercentType(brightness)),
                aggregateStates.getValue());
        ArgumentCaptor<State> hexStates = ArgumentCaptor.forClass(State.class);
        verify(callback, atLeastOnce()).stateUpdated(
                eq(new ChannelUID(handler.getThing().getUID(), "color-switch-hex-color")), hexStates.capture());
        assertEquals(new HSBType("0,0," + brightness), hexStates.getValue());
    }

    private void setReportedColor(Node node, Map<String, Integer> color, String hexColor) {
        for (Value value : node.values) {
            if (value.commandClass == 51 && "currentColor".equals(value.property) && value.propertyKey == null) {
                value.value = color;
            } else if (value.commandClass == 51 && "hexColor".equals(value.property)) {
                value.value = hexColor;
            }
        }
    }

    private List<NodeSetValueCommand> sentValueCommands(ZwaveJSBridgeHandler bridge) {
        ArgumentCaptor<BaseCommand> commands = ArgumentCaptor.forClass(BaseCommand.class);
        verify(bridge, atLeastOnce()).sendCommand(commands.capture());
        return commands.getAllValues().stream().filter(NodeSetValueCommand.class::isInstance)
                .map(NodeSetValueCommand.class::cast).toList();
    }

    private ZwaveJSNodeHandlerMock createHandler(ThingHandlerCallback callback, Node node, String... linkedChannelIds) {
        Thing thing = ZwaveJSNodeHandlerMock.mockThing(node.nodeId);
        ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createHandler(callback, thing, "store_4.json", false);
        Set<String> linkedChannels = Set.of(linkedChannelIds);
        doAnswer(invocation -> linkedChannels.contains(invocation.getArgument(0))).when(handler).isLinked(anyString());
        doAnswer(invocation -> linkedChannels.contains(((ChannelUID) invocation.getArgument(0)).getId())).when(handler)
                .isLinked(any(ChannelUID.class));
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
            @Nullable Object newValue) {
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
