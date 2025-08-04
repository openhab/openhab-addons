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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Test class for DimmableLightDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class DimmableLightDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private DimmerItem dimmerItem;
    @NonNullByDefault({})
    private SwitchItem switchItem;
    @NonNullByDefault({})
    private GroupItem groupItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private DimmableLightDevice dimmerDevice;
    @NonNullByDefault({})
    private DimmableLightDevice switchDevice;
    @NonNullByDefault({})
    private DimmableLightDevice groupDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        dimmerItem = Mockito.spy(new DimmerItem("testDimmer"));
        switchItem = Mockito.spy(new SwitchItem("testSwitch"));
        groupItem = Mockito.spy(new GroupItem("testGroup", dimmerItem));

        dimmerDevice = new DimmableLightDevice(metadataRegistry, client, dimmerItem);
        switchDevice = new DimmableLightDevice(metadataRegistry, client, switchItem);
        groupDevice = new DimmableLightDevice(metadataRegistry, client, groupItem);
    }

    @AfterEach
    void tearDown() {
        dimmerDevice.dispose();
        switchDevice.dispose();
        groupDevice.dispose();
    }

    @Test
    void testDeviceType() {
        assertEquals("DimmableLight", dimmerDevice.deviceType());
    }

    @Test
    void testHandleMatterEventOnOff() {
        dimmerDevice.handleMatterEvent("onOff", "onOff", true);
        verify(dimmerItem).send(OnOffType.ON);

        dimmerDevice.handleMatterEvent("onOff", "onOff", false);
        verify(dimmerItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventOnOffGroup() {
        groupDevice.handleMatterEvent("onOff", "onOff", true);
        verify(groupItem).send(OnOffType.ON);

        groupDevice.handleMatterEvent("onOff", "onOff", false);
        verify(groupItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventLevel() {
        dimmerDevice.handleMatterEvent("onOff", "onOff", true);
        verify(dimmerItem).send(OnOffType.ON);

        dimmerDevice.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(254));
        verify(dimmerItem).send(new PercentType(100));

        dimmerDevice.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(127));
        verify(dimmerItem).send(new PercentType(50));
    }

    @Test
    void testHandleMatterEventLevelGroup() {
        groupDevice.handleMatterEvent("onOff", "onOff", true);
        groupDevice.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(254));
        verify(groupItem).send(new PercentType(100));

        groupDevice.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(127));
        verify(groupItem).send(new PercentType(50));
    }

    @Test
    void testUpdateStateWithPercent() {
        dimmerDevice.updateState(dimmerItem, new PercentType(100));
        List<AttributeState> expectedStates = new ArrayList<>();
        expectedStates.add(new AttributeState("onOff", "onOff", true));
        expectedStates.add(new AttributeState("levelControl", "currentLevel", 254));
        verify(client).setEndpointStates(any(), eq(expectedStates));

        dimmerDevice.updateState(dimmerItem, PercentType.ZERO);
        expectedStates.clear();
        expectedStates.add(new AttributeState("onOff", "onOff", false));
        verify(client).setEndpointStates(any(), eq(expectedStates));
    }

    @Test
    void testActivate() {
        dimmerItem.setState(new PercentType(100));
        MatterDeviceOptions options = dimmerDevice.activate();

        Map<String, Object> levelMap = options.clusters.get("levelControl");
        Map<String, Object> onOffMap = options.clusters.get("onOff");

        assertNotNull(levelMap);
        assertNotNull(onOffMap);

        assertEquals(254, levelMap.get("currentLevel"));
        assertEquals(true, onOffMap.get("onOff"));
    }

    @Test
    void testActivateWithOffState() {
        dimmerItem.setState(PercentType.ZERO);
        MatterDeviceOptions options = dimmerDevice.activate();

        Map<String, Object> levelMap = options.clusters.get("levelControl");
        Map<String, Object> onOffMap = options.clusters.get("onOff");

        assertNotNull(levelMap);
        assertNotNull(onOffMap);

        assertNotEquals(0, levelMap.get("currentLevel"));
        assertEquals(false, onOffMap.get("onOff"));
    }
}
