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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

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
                "store_1.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testStore1Node3ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(3);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_1.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testStore1Node6ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(6);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandlerMock handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing,
                "store_1.json");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(4)).stateUpdated(any(), any());
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testStore1Node6SwitchEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(6);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_1.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_6_switch.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:binary-switch-value");
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
    public void testStore1Node6PowerEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(6);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_1.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_6_power.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:multilevel-sensor-power");
        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback).stateUpdated(eq(channelid), eq(new QuantityType<Power>(62.4, Units.WATT)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testStore2Node2PowerEventUpdate() throws IOException {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(2);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_2.json");

        EventMessage eventMessage = DataUtil.fromJson("event_node_2_power.json", EventMessage.class);
        handler.onNodeStateChanged(eventMessage.event);

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:meter-value-66049-2");
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
    public void testStore4Node7ChannelsCreation() {
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
    public void testStore4Node7ConfigCreation() {
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
    public void testStore4Node10ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(10);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

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
    public void testStore4Node40ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(40);
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
    public void testStore4Node44ChannelsCreation() {
        final Thing thing = ZwaveJSNodeHandlerMock.mockThing(44);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSNodeHandler handler = ZwaveJSNodeHandlerMock.createAndInitHandler(callback, thing, "store_4.json");

        ChannelUID channelid = new ChannelUID("zwavejs:test-bridge:test-thing:color-switch-hex-color");

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(argThat(arg -> arg.getUID().equals(thing.getUID())),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
            verify(callback, times(15)).stateUpdated(any(), any());
            verify(callback).stateUpdated(eq(channelid), eq(HSBType.fromRGB(0, 0, 0)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testStore4Node78ChannelsCreation() {
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
    public void testStore4Node78SwitchEventUpdate() throws IOException {
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
}
