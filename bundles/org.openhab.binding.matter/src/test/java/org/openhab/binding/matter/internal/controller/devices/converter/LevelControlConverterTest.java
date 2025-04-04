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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.StateDescription;

/**
 * Test class for LevelControlConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class LevelControlConverterTest {

    private static final ThingTypeUID THING_TYPE_TEST = new ThingTypeUID("matter", "test");

    @Mock
    @NonNullByDefault({})
    private LevelControlCluster mockCluster;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient mockBridgeClient;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider mockStateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider mockChannelTypeProvider;
    @NonNullByDefault({})
    private TestMatterBaseThingHandler mockHandler;
    @NonNullByDefault({})
    private LevelControlConverter converter;

    @SuppressWarnings("null")
    private static class TestMatterBaseThingHandler extends MatterBaseThingHandler {
        public TestMatterBaseThingHandler(MatterBridgeClient bridgeClient,
                MatterStateDescriptionOptionProvider stateDescriptionProvider,
                MatterChannelTypeProvider channelTypeProvider) {
            super(ThingBuilder.create(THING_TYPE_TEST, "test").build(), stateDescriptionProvider, channelTypeProvider);
        }

        @Override
        public void updateState(String channelId, org.openhab.core.types.State state) {
            super.updateState(channelId, state);
        }

        @Override
        public BigInteger getNodeId() {
            return BigInteger.ONE;
        }

        @Override
        public ThingTypeUID getDynamicThingTypeUID() {
            return THING_TYPE_TEST;
        }

        @Override
        public boolean isBridgeType() {
            return false;
        }
    }

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHandler = Mockito.spy(new TestMatterBaseThingHandler(mockBridgeClient, mockStateDescriptionProvider,
                mockChannelTypeProvider));
        converter = new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID thingUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(thingUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#levelcontrol-level", channel.getUID().toString());
        assertEquals("Dimmer", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithLevel() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "currentLevel";
        message.value = 254;

        // Set lastOnOff to ON to ensure level update is processed
        AttributeChangedMessage onOffMessage = new AttributeChangedMessage();
        onOffMessage.path = new Path();
        onOffMessage.path.attributeName = "onOff";
        onOffMessage.value = true;
        converter.onEvent(onOffMessage);

        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(100)));
    }

    @Test
    void testOnEventWithOnOff() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "onOff";
        message.value = true;

        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(OnOffType.ON));
    }

    @Test
    void testInitState() {
        mockCluster.currentLevel = 254;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(100)));
    }

    @Test
    void testInitStateOff() {
        mockCluster.currentLevel = 254;
        converter.initState(false);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(OnOffType.OFF));
    }
}
