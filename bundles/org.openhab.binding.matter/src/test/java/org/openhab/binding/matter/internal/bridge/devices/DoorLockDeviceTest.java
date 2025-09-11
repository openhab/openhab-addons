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
package org.openhab.binding.matter.internal.bridge.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DoorLockCluster;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for DoorLockDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class DoorLockDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private SwitchItem switchItem;
    @NonNullByDefault({})
    private GroupItem groupItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private DoorLockDevice switchDevice;
    @NonNullByDefault({})
    private DoorLockDevice groupDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        switchItem = Mockito.spy(new SwitchItem("testSwitch"));
        groupItem = Mockito.spy(new GroupItem("testGroup", switchItem));

        switchDevice = new DoorLockDevice(metadataRegistry, client, switchItem);
        groupDevice = new DoorLockDevice(metadataRegistry, client, groupItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("DoorLock", switchDevice.deviceType());
    }

    @Test
    void testHandleMatterEventLockState() {
        switchDevice.handleMatterEvent("doorLock", "lockState",
                Double.valueOf(DoorLockCluster.LockStateEnum.LOCKED.value));
        verify(switchItem).send(OnOffType.ON);

        switchDevice.handleMatterEvent("doorLock", "lockState",
                Double.valueOf(DoorLockCluster.LockStateEnum.UNLOCKED.value));
        verify(switchItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventLockStateGroup() {
        groupDevice.handleMatterEvent("doorLock", "lockState",
                Double.valueOf(DoorLockCluster.LockStateEnum.LOCKED.value));
        verify(groupItem).send(OnOffType.ON);

        groupDevice.handleMatterEvent("doorLock", "lockState",
                Double.valueOf(DoorLockCluster.LockStateEnum.UNLOCKED.value));
        verify(groupItem).send(OnOffType.OFF);
    }

    @Test
    void testUpdateState() {
        switchDevice.updateState(switchItem, OnOffType.ON);
        verify(client).setEndpointState(any(), eq("doorLock"), eq("lockState"),
                eq(DoorLockCluster.LockStateEnum.LOCKED.value));

        switchDevice.updateState(switchItem, OnOffType.OFF);
        verify(client).setEndpointState(any(), eq("doorLock"), eq("lockState"),
                eq(DoorLockCluster.LockStateEnum.UNLOCKED.value));
    }

    @Test
    void testActivate() {
        switchItem.setState(OnOffType.ON);
        MatterDeviceOptions options = switchDevice.activate();

        Map<String, Object> doorLockMap = options.clusters.get("doorLock");
        assertNotNull(doorLockMap);
        assertEquals(DoorLockCluster.LockStateEnum.LOCKED.value, doorLockMap.get("lockState"));
    }

    @Test
    void testActivateWithUnlockedState() {
        switchItem.setState(OnOffType.OFF);
        MatterDeviceOptions options = switchDevice.activate();

        Map<String, Object> doorLockMap = options.clusters.get("doorLock");
        assertNotNull(doorLockMap);
        assertEquals(DoorLockCluster.LockStateEnum.UNLOCKED.value, doorLockMap.get("lockState"));
    }
}
