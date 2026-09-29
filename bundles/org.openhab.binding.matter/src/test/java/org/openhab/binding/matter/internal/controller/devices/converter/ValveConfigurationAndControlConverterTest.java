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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.FeatureMap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveFaultBitmap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ValveConfigurationAndControlCluster.ValveStateEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.binding.matter.internal.client.dto.ws.TriggerEvent;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
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
        // state, current-state, target-state, duration, close-time, fault -- no level channel without the
        // Level feature
        assertEquals(6, channels.size());
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-state".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-current-state".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-target-state".equals(c.getUID().toString())));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> "matter:node:test:12345:1#valve-close-time".equals(c.getUID().toString())));
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
        assertEquals(7, channels.size());
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
    void testHandleCommandDurationZeroWritesNull() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-duration");
        converter.handleCommand(channelUID, new QuantityType<>(0, Units.SECOND));
        // DefaultOpenDuration has a minimum of 1; null is what clears it, so 0 must not go on the wire.
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION), eq("null"));
    }

    @Test
    void testHandleCommandDurationNegativeIsIgnored() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-duration");
        converter.handleCommand(channelUID, new QuantityType<>(-5, Units.SECOND));
        verify(mockHandler, never()).writeAttribute(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.ATTRIBUTE_DEFAULT_OPEN_DURATION), anyString());
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
    void testOnEventTargetStateUpdatesOnlyTargetChannel() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_TARGET_STATE;
        message.value = ValveStateEnum.OPEN;
        converter.onEvent(message);
        // TargetState is intent, not position: it must not move the switch or the current-state channel, so a
        // target of Open on an open the device later declines can't latch the switch ON.
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-target-state"), eq(new DecimalType(1)));
        verify(mockHandler, times(0)).updateState(eq(1), eq("valve-state"), eq(OnOffType.ON));
        verify(mockHandler, times(0)).updateState(eq(1), eq("valve-current-state"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventTargetStateNullMapsToUndef() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_TARGET_STATE;
        message.value = null;
        converter.onEvent(message);
        // Null TargetState means the valve has settled and is no longer moving toward a target.
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-target-state"), eq(UnDefType.UNDEF));
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

    private EventTriggeredMessage valveFaultEvent(ValveFaultBitmap fault) {
        EventTriggeredMessage message = new EventTriggeredMessage();
        message.path = new Path();
        message.path.eventName = "valveFault";
        TriggerEvent event = new TriggerEvent();
        event.data = new ValveConfigurationAndControlCluster.ValveFault(fault);
        message.events = new TriggerEvent[] { event };
        return message;
    }

    @Test
    void testOnEventValveFaultFiresOneEventPerBit() {
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, true, true, false, false, false)));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("leaking"));
    }

    @Test
    void testOnEventValveFaultOnlyFiresNewlySetBits() {
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, true, false, false, false, false)));
        // A second fault appearing must not re-fire the one that was already reported.
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, true, true, false, false, false)));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("leaking"));
        // Once cleared, the same fault occurring again is reported.
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, false, false, false, false, false)));
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, true, false, false, false, false)));
        verify(mockHandler, times(2)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
    }

    @Test
    void testOnEventValveFaultAttributeTriggersOnlyOnce() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_VALVE_FAULT;
        message.value = new ValveFaultBitmap(false, true, false, false, false, false);
        converter.onEvent(message);
        // A valve that reports the attribute but not the event still triggers.
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
        // One that reports both does not trigger twice, since each carries the whole bitmap.
        converter.onEvent(valveFaultEvent(new ValveFaultBitmap(false, true, false, false, false, false)));
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("valve-fault"), eq("blocked"));
    }

    @Test
    void testHandleCommandLevelOnUsesDeviceDefaultLevel() {
        mockCluster.featureMap = new FeatureMap(false, true);
        ValveConfigurationAndControlConverter levelConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-level");
        // A null target level lets the valve open to its own DefaultOpenLevel.
        levelConverter.handleCommand(channelUID, OnOffType.ON);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.open(null, null)));
    }

    @Test
    void testHandleCommandLevelRoundsToSupportedStep() {
        mockCluster.featureMap = new FeatureMap(false, true);
        mockCluster.levelStep = 15;
        ValveConfigurationAndControlConverter levelConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#valve-level");
        // 50 is not a multiple of 15, so it would be rejected with CONSTRAINT_ERROR; 45 is the nearest supported.
        levelConverter.handleCommand(channelUID, new PercentType(50));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.open(null, 45)));
        // 100 is always supported regardless of the step, and is nearer than the last full step of 90.
        levelConverter.handleCommand(channelUID, new PercentType(97));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.open(null, 100)));
        // A level that rounds down to 0 closes the valve rather than sending an out-of-constraint target.
        levelConverter.handleCommand(channelUID, new PercentType(5));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ValveConfigurationAndControlCluster.CLUSTER_NAME),
                eq(ValveConfigurationAndControlCluster.close()));
    }

    @Test
    void testOnEventRemainingDurationDerivesCloseTime() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = 120;
        Instant before = Instant.now();
        converter.onEvent(message);
        Instant after = Instant.now();
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-close-time"), state.capture());
        Instant closeTime = ((DateTimeType) state.getValue()).getInstant();
        assertFalse(closeTime.isBefore(before.plusSeconds(120)));
        assertFalse(closeTime.isAfter(after.plusSeconds(120)));
    }

    @Test
    void testOnEventCurrentLevelNullMapsToUndef() {
        mockCluster.featureMap = new FeatureMap(false, true);
        ValveConfigurationAndControlConverter levelConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_CURRENT_LEVEL;
        message.value = null;
        levelConverter.onEvent(message);
        // A null CurrentLevel means the level is unknown, which must not leave the last one showing.
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-level"), eq(UnDefType.UNDEF));
    }

    @Test
    void testOnEventCloseTimeKeepsDerivedInstantWhenAutoCloseTimeClears() throws InterruptedException {
        mockCluster.featureMap = new FeatureMap(true, false);
        ValveConfigurationAndControlConverter tsConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = 120;
        tsConverter.onEvent(message);
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-close-time"), state.capture());
        Instant derived = ((DateTimeType) state.getValue()).getInstant();

        // Let the clock move, so re-basing the duration would give a different instant.
        Thread.sleep(10);

        // The valve loses its UTC time mid-countdown. RemainingDuration is not reported as it counts down, so the
        // close time must stay where the earlier report put it.
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_AUTO_CLOSE_TIME;
        message.value = null;
        tsConverter.onEvent(message);
        verify(mockHandler, times(2)).updateState(eq(1), eq("valve-close-time"), state.capture());
        assertEquals(derived, ((DateTimeType) state.getValue()).getInstant());
    }

    @Test
    void testOnEventRemainingDurationNullMapsToUndef() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = null;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-close-time"), eq(UnDefType.UNDEF));
    }

    @Test
    void testOnEventAutoCloseTimeIsPreferredOnTimeSyncValve() {
        mockCluster.featureMap = new FeatureMap(true, false);
        ValveConfigurationAndControlConverter tsConverter = new ValveConfigurationAndControlConverter(mockCluster,
                mockHandler, 1, "TestLabel");
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        // Matter timestamps count from 2000-01-01 UTC, so this is 2026-01-01T00:00:00Z.
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_AUTO_CLOSE_TIME;
        message.value = 820540800000000L;
        tsConverter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("valve-close-time"),
                eq(new DateTimeType(Instant.parse("2026-01-01T00:00:00Z"))));

        // On a TimeSync valve AutoCloseTime is authoritative, so RemainingDuration must not derive over it.
        message.path.attributeName = ValveConfigurationAndControlCluster.ATTRIBUTE_REMAINING_DURATION;
        message.value = 120;
        tsConverter.onEvent(message);
        verify(mockHandler, times(2)).updateState(eq(1), eq("valve-close-time"),
                eq(new DateTimeType(Instant.parse("2026-01-01T00:00:00Z"))));
    }
}
