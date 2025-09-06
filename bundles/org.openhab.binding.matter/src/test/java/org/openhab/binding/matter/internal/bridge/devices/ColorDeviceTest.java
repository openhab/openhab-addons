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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Test class for ColorDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ColorDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private ColorItem colorItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private ColorDevice device;
    @NonNullByDefault({})
    private HSBType initialHSBState;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        colorItem = Mockito.spy(new ColorItem("test"));
        initialHSBState = new HSBType(new DecimalType(0), new PercentType(0), new PercentType(50));
        when(colorItem.getStateAs(HSBType.class)).thenReturn(initialHSBState);
        device = new ColorDevice(metadataRegistry, client, colorItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("ColorLight", device.deviceType());
    }

    @Test
    void testHandleMatterEventOnOff() {
        device.handleMatterEvent("onOff", "onOff", true);
        verify(colorItem).send(OnOffType.ON);

        device.handleMatterEvent("onOff", "onOff", false);
        verify(colorItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventColor() {
        // Turn device on first and wait for future completion
        device.handleMatterEvent("onOff", "onOff", true);
        verify(colorItem).send(OnOffType.ON);
        device.updateState(colorItem, initialHSBState);

        device.handleMatterEvent("colorControl", "currentHue", Double.valueOf(127));
        device.handleMatterEvent("colorControl", "currentSaturation", Double.valueOf(127));

        verify(colorItem).send(new HSBType(new DecimalType(180), new PercentType(50), new PercentType(50)));
    }

    @Test
    void testHandleMatterEventLevel() {
        device.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(127));
        verify(colorItem).send(new PercentType(50));
    }

    @Test
    void testUpdateStateWithHSB() {
        HSBType hsb = new HSBType(new DecimalType(180), new PercentType(100), new PercentType(100));
        device.updateState(colorItem, hsb);

        hsb = new HSBType(new DecimalType(180), new PercentType(100), new PercentType(0));
        device.updateState(colorItem, hsb);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AttributeState>> captor = ArgumentCaptor.forClass(List.class);
        verify(client, times(2)).setEndpointStates(any(), captor.capture());

        List<List<AttributeState>> capturedCalls = captor.getAllValues();

        // First call assertions
        List<AttributeState> firstStates = capturedCalls.get(0);
        assertEquals(4, firstStates.size());

        assertListContains(firstStates, "onOff", "onOff", true);
        assertListContains(firstStates, "levelControl", "currentLevel", 254);
        assertListContains(firstStates, "colorControl", "currentHue", 127);
        assertListContains(firstStates, "colorControl", "currentSaturation", 254);

        // Second call assertions
        List<AttributeState> secondStates = capturedCalls.get(1);
        assertEquals(3, secondStates.size());
        assertListContains(secondStates, "onOff", "onOff", false);
        assertListContains(secondStates, "colorControl", "currentHue", 127);
        assertListContains(secondStates, "colorControl", "currentSaturation", 254);
    }

    private void assertListContains(List<AttributeState> list, String cluster, String attribute, Object value) {
        AttributeState found = list.stream()
                .filter(s -> cluster.equals(s.clusterName) && attribute.equals(s.attributeName)).findFirst()
                .orElse(null);
        assertNotNull(found, "Expected state not found: " + cluster + "." + attribute);
        assertEquals(value, found.state);
    }

    @Test
    void testActivate() {
        HSBType hsb = new HSBType(new DecimalType(0), new PercentType(100), new PercentType(100));
        when(colorItem.getStateAs(HSBType.class)).thenReturn(hsb);
        colorItem.setState(hsb);
        MatterDeviceOptions options = device.activate();

        Map<String, Object> levelMap = options.clusters.get("levelControl");
        Map<String, Object> colorMap = options.clusters.get("colorControl");
        Map<String, Object> onOffMap = options.clusters.get("onOff");

        assertNotNull(levelMap);
        assertNotNull(colorMap);
        assertNotNull(onOffMap);

        assertEquals(254, levelMap.get("currentLevel"));
        assertEquals(0, colorMap.get("currentHue"));
        assertEquals(254, colorMap.get("currentSaturation"));
        assertEquals(true, onOffMap.get("onOff"));
    }

    @Test
    void testActivateWithOffState() {
        HSBType hsb = new HSBType(new DecimalType(0), new PercentType(100), new PercentType(0));
        when(colorItem.getStateAs(HSBType.class)).thenReturn(hsb);
        colorItem.setState(hsb);
        MatterDeviceOptions options = device.activate();

        Map<String, Object> levelMap = options.clusters.get("levelControl");
        Map<String, Object> onOffMap = options.clusters.get("onOff");
        assertNotNull(levelMap);
        assertNotNull(onOffMap);
        assertNotEquals(0, levelMap.get("currentLevel"));
        assertEquals(false, onOffMap.get("onOff"));
    }

    @AfterEach
    void tearDown() {
        device.dispose();
    }
}
