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
import static org.mockito.Mockito.times;
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
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Test class for OnOffLightDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class OnOffLightDeviceTest {

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
    private OnOffLightDevice device;
    @NonNullByDefault({})
    private OnOffLightDevice groupDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        switchItem = Mockito.spy(new SwitchItem("test"));
        groupItem = Mockito.spy(new GroupItem("testGroup", switchItem));

        device = new OnOffLightDevice(metadataRegistry, client, switchItem);
        groupDevice = new OnOffLightDevice(metadataRegistry, client, groupItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("OnOffLight", device.deviceType());
    }

    @Test
    void testHandleMatterEventOnOff() {
        device.handleMatterEvent("onOff", "onOff", true);
        verify(switchItem).send(OnOffType.ON);

        device.handleMatterEvent("onOff", "onOff", false);
        verify(switchItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventOnOffGroup() {
        groupDevice.handleMatterEvent("onOff", "onOff", true);
        verify(groupItem).send(OnOffType.ON);

        groupDevice.handleMatterEvent("onOff", "onOff", false);
        verify(groupItem).send(OnOffType.OFF);
    }

    @Test
    void testUpdateStateWithOnOff() {
        device.updateState(switchItem, OnOffType.ON);
        verify(client, times(1)).setEndpointState(any(), eq("onOff"), eq("onOff"), eq(true));

        device.updateState(switchItem, OnOffType.OFF);
        verify(client, times(1)).setEndpointState(any(), eq("onOff"), eq("onOff"), eq(false));
    }

    @Test
    void testUpdateStateWithPercent() {
        device.updateState(switchItem, new PercentType(100));
        verify(client, times(1)).setEndpointState(any(), eq("onOff"), eq("onOff"), eq(true));

        device.updateState(switchItem, PercentType.ZERO);
        verify(client, times(1)).setEndpointState(any(), eq("onOff"), eq("onOff"), eq(false));
    }

    @Test
    void testActivate() {
        switchItem.setState(OnOffType.ON);
        MatterDeviceOptions options = device.activate();

        Map<String, Object> onOffMap = options.clusters.get("onOff");
        assertNotNull(onOffMap);
        assertEquals(1, options.clusters.size());
        assertEquals(true, onOffMap.get("onOff"));
    }

    @Test
    void testActivateWithOffState() {
        switchItem.setState(OnOffType.OFF);
        MatterDeviceOptions options = device.activate();

        Map<String, Object> onOffMap = options.clusters.get("onOff");
        assertNotNull(onOffMap);
        assertEquals(1, options.clusters.size());
        assertEquals(false, onOffMap.get("onOff"));
    }
}
