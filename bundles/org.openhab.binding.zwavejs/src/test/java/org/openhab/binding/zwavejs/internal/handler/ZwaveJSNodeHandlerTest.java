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
package org.openhab.binding.zwavejs.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.commands.BaseCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeGetValueCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeSetValueCommand;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSNodeHandlerMock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSNodeHandlerTest {

    @Test
    public void testInvalidConfiguration() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(0);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode7ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(12)).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode7ChannelsCreationInclConfig() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json",
                true);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(74)).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode7ConfigChannelToggle() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json", false);

        // Capture initial state
        Configuration startConfiguration = handler.getThing().getConfiguration();
        List<Channel> startChannels = handler.getThing().getChannels();

        // Test flipping configAsChannel to true
        handler.configAsChannel = true;
        handler.initialize();
        List<Channel> configAsChannelChannels = handler.getThing().getChannels();

        // Test flipping configAsChannel back to false
        handler.configAsChannel = false;
        handler.initialize();
        List<Channel> endChannels = handler.getThing().getChannels();

        try {
            assertEquals(63, startConfiguration.getProperties().size());
            assertEquals(31, startChannels.size());
            assertEquals(93, configAsChannelChannels.size()); // 63+31-1 as the `id` is not included in the channels
            assertEquals(31, endChannels.size());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode7ConfigCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json",
                false);
        Configuration configuration = handler.getThing().getConfiguration();
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            assertEquals(63, configuration.getProperties().size());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode7EP2ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:multilevel-switch-value-2");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).stateUpdated(eq(channelid), eq(new PercentType(94)));
        } finally {
            handler.dispose();
        }

        Channel channel = handler.getThing().getChannels().stream()
                .filter(f -> "multilevel-switch-value-2".equals(f.getUID().getId())).findFirst().orElse(null);

        assertNotNull(channel);
        assertEquals("Dimmer", channel.getAcceptedItemType());
        assertEquals("EP2 Current Value", channel.getLabel());
    }

    @Test
    public void testNode7PowerEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_7_power.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:meter-value-66049-1");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(1)).stateUpdated(eq(channelid), eq(new QuantityType<Power>(2.16, Units.WATT)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode25SwitchEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(25);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_25_switch.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:binary-switch-value-2");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).stateUpdated(eq(channelid), eq(OnOffType.OFF));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode25EventNodeRemoved() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(25);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_controller_node_removed.json", EventMessage.class);
        handler.onNodeRemoved(eventMessage.event);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode39ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(39);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(4)).stateUpdated(any(), any());
            verify(callback).stateUpdated(eq(channelid), eq(new PercentType(98)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode44ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(44);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:color-switch-hex-color");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            // 18 = 15 direct updates + 2 handled color updates + 1 handled color temperature update
            verify(callback, times(18)).stateUpdated(any(), any());
            verify(callback).stateUpdated(eq(channelid), eq(new HSBType("0,0,100")));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode60RollerShutterAndDimmerEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(60);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_60_dimmer.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelIdDimmer = new ChannelUID("zwavejs:test-bridge:test-thing:multilevel-switch-value-1");
        ChannelUID channelIdRollerShutter = new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual-1");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(1)).stateUpdated(eq(channelIdDimmer), eq(new PercentType(52)));
            verify(callback, times(1)).stateUpdated(eq(channelIdRollerShutter), eq(new PercentType(52)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode60RollerShutterAndSwitchDownEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(60);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_60_switch_down.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelIdSwitch = new ChannelUID("zwavejs:test-bridge:test-thing:multilevel-switch-down-1");
        ChannelUID channelIdRollerShutter = new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual-1");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(1)).stateUpdated(eq(channelIdSwitch), eq(OnOffType.ON));
            verify(callback, times(1)).stateUpdated(eq(channelIdRollerShutter), eq(UpDownType.DOWN));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode60RollerShutterAndSwitchUpEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(60);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_60_switch_up.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelIdSwitch = new ChannelUID("zwavejs:test-bridge:test-thing:multilevel-switch-up-1");
        ChannelUID channelIdRollerShutter = new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual-1");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(1)).stateUpdated(eq(channelIdSwitch), eq(OnOffType.ON));
            verify(callback, times(1)).stateUpdated(eq(channelIdRollerShutter), eq(UpDownType.UP));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode78ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(78);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(10)).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode78SwitchEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(78);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_78_switch.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID(
                "zwavejs:test-bridge:test-thing:notification-access-control-door-state-simple");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).stateUpdated(eq(channelid), eq(OnOffType.ON));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode186ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(186);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(15)).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testNode186NotificationEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(186);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_186_notification.json", EventMessage.class);

        handler.onNodeStateChanged(ZwaveJSBridgeHandler.normalizeNotificationEvent(eventMessage.event));

        ChannelUID channelIdNotification = new ChannelUID("zwavejs:test-bridge:test-thing:notification-virtual");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(1)).stateUpdated(eq(channelIdNotification),
                    eq(new StringType(new Gson().toJson(eventMessage.event.args))));
        } finally {
            handler.dispose();
        }
    }

    private ZwaveJSNodeHandlerMock arrangeHandleCommandTest(int nodeId) {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(nodeId);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock nodeHandler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");

        ZwaveJSBridgeHandler bridgeHandler = nodeHandler.getBridgeHandler();

        doReturn(bridgeHandler).when(nodeHandler).getBridgeHandler();

        return nodeHandler;
    }

    private <T extends BaseCommand> T captureCommand(ZwaveJSNodeHandler nodeHandler, Class<? extends T> clazz) {
        ArgumentCaptor<T> captor = ArgumentCaptor.forClass(clazz);
        verify(nodeHandler.getBridgeHandler(), atLeastOnce()).sendCommand(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testRollershutterCommandOn() {
        // Arrange
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(39);

        // Act
        nodeHandler.handleCommand(new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual"), OnOffType.ON);

        // Assert: verify sendCommand was called with the expected NodeSetValueCommand
        verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeSetValueCommand.class));

        NodeSetValueCommand sendCommand = captureCommand(nodeHandler, NodeSetValueCommand.class);

        // dimmer = 255
        assertEquals(39, sendCommand.nodeId);
        assertEquals(38, sendCommand.valueId.commandClass);
        assertEquals("targetValue", sendCommand.valueId.property);
        assertEquals(99, sendCommand.value);
    }

    @Test
    public void testRollershutterCommandUp() {
        // Arrange
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(39);

        // Act
        nodeHandler.handleCommand(new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual"),
                UpDownType.UP);

        // Assert: verify sendCommand was called with the expected NodeSetValueCommand
        verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeSetValueCommand.class));

        NodeSetValueCommand sendCommand = captureCommand(nodeHandler, NodeSetValueCommand.class);

        assertEquals(39, sendCommand.nodeId);
        assertEquals(38, sendCommand.valueId.commandClass);
        assertEquals("On", sendCommand.valueId.property);
        assertEquals(true, sendCommand.value);
    }

    @Test
    public void testRollershutterCommand44() {
        // Arrange
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(39);

        // Act
        nodeHandler.handleCommand(new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual"),
                PercentType.valueOf("44"));

        // Assert: verify sendCommand was called with the expected NodeSetValueCommand
        verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeSetValueCommand.class));

        NodeSetValueCommand sendCommand = captureCommand(nodeHandler, NodeSetValueCommand.class);

        assertEquals(39, sendCommand.nodeId);
        assertEquals(38, sendCommand.valueId.commandClass);
        assertEquals("targetValue", sendCommand.valueId.property);
        assertEquals(44, sendCommand.value);
    }

    @Test
    public void testRollershutterCommandStop() {
        // Arrange
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(39);

        // Act
        nodeHandler.handleCommand(new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual"),
                UpDownType.UP);
        nodeHandler.handleCommand(new ChannelUID("zwavejs:test-bridge:test-thing:rollershutter-virtual"),
                StopMoveType.STOP);

        // Assert: verify sendCommand was called with the expected NodeSetValueCommand
        verify(nodeHandler.getBridgeHandler(), times(2)).sendCommand(any(NodeSetValueCommand.class));

        NodeSetValueCommand sendCommand = captureCommand(nodeHandler, NodeSetValueCommand.class);

        assertEquals(39, sendCommand.nodeId);
        assertEquals(38, sendCommand.valueId.commandClass);
        assertEquals("On", sendCommand.valueId.property);
        assertEquals(false, sendCommand.value);
    }

    @Test
    public void testHandleCommand_NonExistingChannel() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(7);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_4.json");
        try {
            // Use a channel UID that does not exist
            ChannelUID nonExisting = new ChannelUID("zwavejs:test-bridge:test-thing:non-existing-channel");
            handler.handleCommand(nonExisting, OnOffType.ON);
            // Should not throw, should not call sendCommand
            verify(handler.getBridgeHandler(), never()).sendCommand(any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testHandleCommand_RefreshType() {
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(7);

        try {
            Channel channel = nodeHandler.getThing().getChannels().get(0);
            nodeHandler.handleCommand(channel.getUID(), org.openhab.core.types.RefreshType.REFRESH);
            // Should call sendCommand once
            verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeGetValueCommand.class));
        } finally {
            nodeHandler.dispose();
        }
    }

    @Test
    public void testHandleCommand_OnOffType() {
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(7);

        try {
            Channel channel = nodeHandler.getThing().getChannels().stream()
                    .filter(c -> "multilevel-switch-value-1".equals(c.getUID().getId())).findFirst().orElse(null);
            assertNotNull(channel);
            nodeHandler.handleCommand(channel.getUID(), OnOffType.ON);
            // Should call sendCommand once
            verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeSetValueCommand.class));
        } finally {
            nodeHandler.dispose();
        }
    }

    @Test
    public void testHandleCommand_QuantityType() {
        ZwaveJSNodeHandlerMock nodeHandler = arrangeHandleCommandTest(7);

        try {
            Channel channel = nodeHandler.getThing().getChannels().stream()
                    .filter(c -> "meter-value-66049-1".equals(c.getUID().getId())).findFirst().orElse(null);
            assertNotNull(channel);
            nodeHandler.handleCommand(channel.getUID(), new QuantityType<>(5.0, Units.WATT));
            // Should call sendCommand once
            verify(nodeHandler.getBridgeHandler(), times(1)).sendCommand(any(NodeSetValueCommand.class));
        } finally {
            nodeHandler.dispose();
        }
    }
}
