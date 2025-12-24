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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.CredentialTypeEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.UserStatusEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster.UserTypeEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.binding.matter.internal.client.dto.ws.TriggerEvent;
import org.openhab.binding.matter.internal.controller.devices.converter.DoorLockConverter.LockUser;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.StateDescription;

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
        mockCluster.featureMap = new DoorLockCluster.FeatureMap(false, false, false, false, false, false, false, false,
                false, false, false, false, false);
        converter = new DoorLockConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(3, channels.size());
        boolean foundLockState = false;
        boolean foundAlarm = false;
        boolean foundLockOperationError = false;
        for (Channel channel : channels.keySet()) {
            String channelId = channel.getUID().getIdWithoutGroup();
            switch (channelId) {
                case "doorlock-lockstate":
                    assertEquals("Switch", channel.getAcceptedItemType());
                    foundLockState = true;
                    break;
                case "doorlock-alarm":
                    assertEquals(ChannelKind.TRIGGER, channel.getKind());
                    foundAlarm = true;
                    break;
                case "doorlock-lockoperationerror":
                    assertEquals(ChannelKind.TRIGGER, channel.getKind());
                    foundLockOperationError = true;
                    break;
            }
        }
        assertTrue(foundLockState, "Should have lockstate channel");
        assertTrue(foundAlarm, "Should have alarm trigger channel");
        assertTrue(foundLockOperationError, "Should have lockoperationerror trigger channel");
    }

    @Test
    void testCreateChannelsWithDoorState() {
        mockCluster.featureMap.doorPositionSensor = true;

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(4, channels.size());
        boolean foundDoorState = false;
        for (Channel channel : channels.keySet()) {
            String channelId = channel.getUID().getIdWithoutGroup();
            if ("doorlock-doorstate".equals(channelId)) {
                assertEquals("Contact", channel.getAcceptedItemType());
                foundDoorState = true;
            }
        }
        assertTrue(foundDoorState, "Should have doorstate channel when doorPositionSensor feature is enabled");
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
    void testOnEventWithLockState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "lockState";
        message.value = DoorLockCluster.LockStateEnum.LOCKED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventWithDoorState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "doorState";
        message.value = DoorLockCluster.DoorStateEnum.DOOR_CLOSED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-doorstate"), eq(OpenClosedType.CLOSED));
    }

    @Test
    void testOnEventWithDoorStateOpen() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "doorState";
        message.value = DoorLockCluster.DoorStateEnum.DOOR_OPEN;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-doorstate"), eq(OpenClosedType.OPEN));
    }

    @Test
    void testOnEventWithOperatingMode() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "operatingMode";
        message.value = DoorLockCluster.OperatingModeEnum.VACATION;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateConfiguration(anyMap());
    }

    @Test
    void testOnEventWithAutoRelockTime() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "autoRelockTime";
        message.value = Integer.valueOf(30);
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateConfiguration(anyMap());
    }

    @Test
    void testOnEventWithOneTouchLocking() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "enableOneTouchLocking";
        message.value = Boolean.TRUE;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateConfiguration(anyMap());
    }

    @Test
    void testInitState() {
        mockCluster.lockState = DoorLockCluster.LockStateEnum.LOCKED;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.ON));
    }

    @Test
    void testInitStateUnlocked() {
        mockCluster.lockState = DoorLockCluster.LockStateEnum.UNLOCKED;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("doorlock-lockstate"), eq(OnOffType.OFF));
    }

    @Test
    void testOnDoorLockAlarmEvent() {
        EventTriggeredMessage message = new EventTriggeredMessage();
        message.path = new Path();
        message.path.eventName = "doorLockAlarm";
        DoorLockCluster.DoorLockAlarm alarmData = new DoorLockCluster.DoorLockAlarm(
                DoorLockCluster.AlarmCodeEnum.LOCK_JAMMED);
        message.events = new TriggerEvent[] { new TriggerEvent() };
        message.events[0].data = alarmData;
        converter.onEvent(message);
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("doorlock-alarm"), eq("0"));
    }

    @Test
    void testOnLockOperationErrorEvent() {
        EventTriggeredMessage message = new EventTriggeredMessage();
        message.path = new Path();
        message.path.eventName = "lockOperationError";
        // INVALID_CREDENTIAL has value 1
        DoorLockCluster.LockOperationError errorData = new DoorLockCluster.LockOperationError(
                DoorLockCluster.LockOperationTypeEnum.LOCK, DoorLockCluster.OperationSourceEnum.MANUAL,
                DoorLockCluster.OperationErrorEnum.INVALID_CREDENTIAL, null, null, null, null);
        message.events = new TriggerEvent[] { new TriggerEvent() };
        message.events[0].data = errorData;
        converter.onEvent(message);
        verify(mockHandler, times(1)).triggerChannel(eq(1), eq("doorlock-lockoperationerror"), eq("1"));
    }

    // LockUser tests
    @Test
    void testLockUserIsOccupied() {
        LockUser user = new LockUser(1);
        assertFalse(user.isOccupied(), "New user should not be occupied");

        user.userStatus = UserStatusEnum.AVAILABLE;
        assertFalse(user.isOccupied(), "Available user should not be occupied");

        user.userStatus = UserStatusEnum.OCCUPIED_ENABLED;
        assertTrue(user.isOccupied(), "Occupied enabled user should be occupied");

        user.userStatus = UserStatusEnum.OCCUPIED_DISABLED;
        assertTrue(user.isOccupied(), "Occupied disabled user should be occupied");
    }

    @Test
    void testLockUserHasCredentialOfType() {
        LockUser user = new LockUser(1);
        assertFalse(user.hasCredentialOfType(CredentialTypeEnum.PIN), "New user should have no credentials");

        user.credentials.add(new DoorLockCluster.CredentialStruct(CredentialTypeEnum.PIN, 1));
        assertTrue(user.hasCredentialOfType(CredentialTypeEnum.PIN), "User should have PIN credential");
        assertFalse(user.hasCredentialOfType(CredentialTypeEnum.RFID), "User should not have RFID credential");

        user.credentials.add(new DoorLockCluster.CredentialStruct(CredentialTypeEnum.RFID, 2));
        assertTrue(user.hasCredentialOfType(CredentialTypeEnum.RFID), "User should have RFID credential");
    }

    @Test
    void testLockUserIsManagedByFabric() {
        LockUser user = new LockUser(1);
        // No creator fabric - should be managed by any fabric
        assertTrue(user.isManagedByFabric(1), "User with no creator fabric should be managed by any fabric");
        assertTrue(user.isManagedByFabric(2), "User with no creator fabric should be managed by any fabric");

        // Set creator fabric
        user.creatorFabricIndex = 1;
        assertTrue(user.isManagedByFabric(1), "User should be managed by its creator fabric");
        assertFalse(user.isManagedByFabric(2), "User should not be managed by other fabrics");
    }

    @Test
    void testCreateChannelsWithUserFeature() {
        // Enable user feature
        mockCluster.featureMap.user = true;
        mockCluster.numberOfTotalUsersSupported = 10;

        // Create new converter with user feature enabled
        DoorLockConverter userConverter = new DoorLockConverter(mockCluster, mockHandler, 1, "TestLabel");

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = userConverter.createChannels(channelGroupUID);
        // Still creates the same channels - user management is via config, not channels
        assertEquals(3, channels.size());
    }

    @Test
    void testCreateChannelsWithPinCredentialFeature() {
        // Enable PIN credential feature
        mockCluster.featureMap.pinCredential = true;
        mockCluster.minPinCodeLength = 4;
        mockCluster.maxPinCodeLength = 8;

        DoorLockConverter pinConverter = new DoorLockConverter(mockCluster, mockHandler, 1, "TestLabel");

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = pinConverter.createChannels(channelGroupUID);
        assertEquals(3, channels.size());
    }

    @Test
    void testLockUserDefaultUserType() {
        LockUser user = new LockUser(1);
        assertNull(user.userType, "New user should have null userType");

        user.userType = UserTypeEnum.UNRESTRICTED_USER;
        assertEquals(UserTypeEnum.UNRESTRICTED_USER, user.userType);
    }
}
