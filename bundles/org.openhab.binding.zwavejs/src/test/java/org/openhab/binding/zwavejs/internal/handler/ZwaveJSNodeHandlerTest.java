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
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.messages.EventMessage;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSNodeHandlerMock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

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
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

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
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

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
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

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
}
