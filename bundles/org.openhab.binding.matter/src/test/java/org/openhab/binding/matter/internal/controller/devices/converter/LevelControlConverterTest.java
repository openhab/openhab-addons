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
import org.openhab.core.thing.ChannelUID;
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
        mockCluster.featureMap = new LevelControlCluster.FeatureMap(true, true, false);
        converter = new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    private LevelControlConverter nonLightingConverter() {
        mockCluster.featureMap = new LevelControlCluster.FeatureMap(true, false, false);
        return new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    private void sendLevel(LevelControlConverter converter, int level) {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL;
        message.value = level;
        converter.onEvent(message);
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

    @Test
    void testLitLightAtItsDimmestIsNotReportedAsOff() {
        mockCluster.currentLevel = 254;
        converter.initState(true);
        sendLevel(converter, 1);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(1)));
    }

    @Test
    void testZeroPercentTurnsOffALight() {
        converter.handleCommand(new ChannelUID("matter:node:test:12345:1#levelcontrol-level"), PercentType.ZERO);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(OnOffCluster.CLUSTER_NAME), eq(OnOffCluster.off()));
    }

    @Test
    void testZeroPercentMovesANonLightingDeviceToLevelZero() {
        // Without the Lighting feature level 0 is valid, and is how the device is turned off
        LevelControlConverter converter = nonLightingConverter();
        converter.handleCommand(new ChannelUID("matter:node:test:12345:1#levelcontrol-level"), PercentType.ZERO);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(LevelControlCluster.CLUSTER_NAME),
                eq(LevelControlCluster.moveToLevelWithOnOff(0, 0, null, null)));
    }

    @Test
    void testNonLightingLevelZeroIsReportedAsZeroPercent() {
        LevelControlConverter converter = nonLightingConverter();
        mockCluster.currentLevel = 254;
        converter.initState(true);
        sendLevel(converter, 0);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(PercentType.ZERO));
    }
}
