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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for DoorLockConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class DoorLockConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private DoorLockCluster mockCluster;

    @NonNullByDefault({})
    private DoorLockConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new DoorLockConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateStandardChannels() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, false, false, false);
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#doorlock-lockstate", channel.getUID().toString());
        assertEquals("Switch", channel.getAcceptedItemType());
    }

    @Test
    void testCreateChannelsWithUnbolting() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(2, channels.size());
        boolean hasLockState = channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().equals("matter:node:test:12345:1#doorlock-lockstate"));
        boolean hasBoltState = channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().equals("matter:node:test:12345:1#doorlock-boltstate"));
        assertEquals(true, hasLockState);
        assertEquals(true, hasBoltState);
    }

    @Test
    void testHandleCommandLock() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#doorlock-lockstate");
        converter.handleCommand(channelUID, OnOffType.ON);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(DoorLockCluster.CLUSTER_NAME),
                eq(DoorLockCluster.lockDoor(null)));
    }

    @Test
    void testHandleCommandUnlock() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#doorlock-lockstate");
        converter.handleCommand(channelUID, OnOffType.OFF);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(DoorLockCluster.CLUSTER_NAME),
                eq(DoorLockCluster.unlockDoor(null)));
    }

    @Test
    void testHandleBoltCommands() {
        ChannelUID boltChannelUID = new ChannelUID("matter:node:test:12345:1#doorlock-boltstate");
        converter.handleCommand(boltChannelUID, OnOffType.ON);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(DoorLockCluster.CLUSTER_NAME),
                eq(DoorLockCluster.lockDoor(null)));
        converter.handleCommand(boltChannelUID, OnOffType.OFF);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(DoorLockCluster.CLUSTER_NAME),
                eq(DoorLockCluster.unboltDoor(null)));
    }

    @Test
    void testOnEventWithLockState() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "lockState";
        message.value = DoorLockCluster.LockStateEnum.LOCKED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.ON));
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-boltstate"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventWithLockStateUnboltingUnlocked() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "lockState";
        message.value = DoorLockCluster.LockStateEnum.UNLOCKED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.OFF));
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-boltstate"), eq(OnOffType.OFF));
    }

    @Test
    void testOnEventWithLockStateUnboltingUnlatched() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "lockState";
        message.value = DoorLockCluster.LockStateEnum.UNLATCHED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.ON));
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-boltstate"), eq(OnOffType.OFF));
    }

    @Test
    void testOnEventWithLockStateUnboltingNotFullyLocked() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "lockState";
        message.value = DoorLockCluster.LockStateEnum.NOT_FULLY_LOCKED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(UnDefType.UNDEF));
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-boltstate"), eq(UnDefType.UNDEF));
    }

    @Test
    void testInitState() {
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, true, false, false);
        mockCluster.lockState = DoorLockCluster.LockStateEnum.LOCKED;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.ON));
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-boltstate"), eq(OnOffType.ON));
    }
}
