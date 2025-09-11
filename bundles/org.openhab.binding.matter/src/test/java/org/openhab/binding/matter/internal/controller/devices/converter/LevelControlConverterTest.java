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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for LevelControlConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class LevelControlConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private LevelControlCluster mockCluster;
    @NonNullByDefault({})
    private LevelControlConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#levelcontrol-level", channel.getUID().toString());
        assertEquals("Dimmer", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithLevel() {
        AttributeChangedMessage levelMessage = new AttributeChangedMessage();
        levelMessage.path = new Path();
        levelMessage.path.attributeName = LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL;
        levelMessage.value = 254;

        // Set lastOnOff to ON to ensure level update is processed
        AttributeChangedMessage onOffMessage = new AttributeChangedMessage();
        onOffMessage.path = new Path();
        onOffMessage.path.attributeName = OnOffCluster.ATTRIBUTE_ON_OFF;
        onOffMessage.value = Boolean.TRUE;
        converter.onEvent(onOffMessage);

        converter.onEvent(levelMessage);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(100)));
    }

    @Test
    void testOffEventWithOnOff() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = OnOffCluster.ATTRIBUTE_ON_OFF;
        message.value = Boolean.FALSE;

        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(OnOffType.OFF));
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
