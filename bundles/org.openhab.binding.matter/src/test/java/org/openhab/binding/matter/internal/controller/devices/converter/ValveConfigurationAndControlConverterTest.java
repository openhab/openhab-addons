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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.FeatureMap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveFaultBitmap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveStateEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for ValveConfigurationAndControlConverter
 *
 * @author Jason Hubbard - Initial contribution
 */
@NonNullByDefault
class ValveConfigurationAndControlConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ValveConfigurationAndControlCluster mockCluster;
    @NonNullByDefault({})
    private ValveConfigurationAndControlConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new ValveConfigurationAndControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        // state, current-state, duration, remaining-duration, fault -- no level channel without the Level feature
        assertEquals(5, channels.size());
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-state".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-current-state".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-remaining-duration".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .noneMatch(c -> "matter:node:test:12345:1#valve-level".equals(c.getUID().toString())));
    }

    @Test
    void testCreateChannelsWithLevelFeature() {
        mockCluster.featureMap = new FeatureMap(false, true);
        ValveConfigurationAndControlConverter levelConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = levelConverter.createChannels(channelGroupUID);
        assertEquals(6, channels.size());
        Channel levelChannel = channels.keySet().stream()
                .filter(c -> "matter:node:test:12345:1#valve-level".equals(c.getUID().toString())).findFirst()
                .orElseThrow();
        assertEquals("Dimmer", levelChannel.getAcceptedItemType());
    }

    @Test
    void testHandleCommandOpen() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-state");
        converter.handleCommand(channelUID, OnOffType.ON);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.open(null, null)));
    }

    @Test
    void testHandleCommandClose() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-state");
        converter.handleCommand(channelUID, OnOffType.OFF);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.close()));
    }

    @Test
    void testHandleCommandLevelPercent() {
        mockCluster.featureMap = new FeatureMap(false, true);
        ValveConfigurationAndControlConverter levelConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-level");
        levelConverter.handleCommand(channelUID, new PercentType(50));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.open(null, 50)));
    }

    @Test
    void testHandleCommandDuration() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-duration");
        converter.handleCommand(channelUID, new QuantityType<>(30, Units.SECOND));
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION), eq("30"));
    }

    @Test
    void testOnEventCurrentStateOpen() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_STATE;
        message.value = ValveStateEnum.OPEN;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-state"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventCurrentStateClosed() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_STATE;
        message.value = ValveStateEnum.CLOSED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-state"), eq(OnOffType.OFF));
    }

    @Test
    void testOnEventTargetStateOpen() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_TARGET_STATE;
        message.value = ValveStateEnum.OPEN;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-state"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventCurrentStateExposesEnumValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_STATE;
        message.value = ValveStateEnum.OPEN;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-current-state"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventCurrentStateTransitioning() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_STATE;
        message.value = ValveStateEnum.TRANSITIONING;
        converter.onEvent(message);
        // The current-state channel exposes TRANSITIONING; the switch keeps its last stable value (no update).
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-current-state"), eq(new DecimalType(2)));
        verify(mockHandler, times(0)).updateState(eq(1), eq("valve-state"), eq(OnOffType.ON));
        verify(mockHandler, times(0)).updateState(eq(1), eq("valve-state"), eq(OnOffType.OFF));
    }

    @Test
    void testOnEventValveFaultFiresOneEventPerBit() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_VALVE_FAULT;
        message.value = new ValveFaultBitmap(false, true, true, false, false, false);
        converter.onEvent(message);
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("leaking"));
    }

    @Test
    void testOnEventRemainingDuration() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = 120;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-remaining-duration"),
                eq(new QuantityType<>(120L, Units.SECOND)));
    }

    @Test
    void testOnEventRemainingDurationNullMapsToUndef() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = null;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-remaining-duration"), eq(UnDefType.UNDEF));
    }
}
