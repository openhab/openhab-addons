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
package org.openhab.binding.matter.internal.bridge.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MATTER_SOURCE;

import java.math.BigDecimal;
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
import org.openhab.core.util.ColorUtil;

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
        verify(colorItem).send(OnOffType.ON, MATTER_SOURCE);

        device.handleMatterEvent("onOff", "onOff", false);
        verify(colorItem).send(OnOffType.OFF, MATTER_SOURCE);
    }

    @Test
    void testHandleMatterEventColor() {
        // Turn device on first and wait for future completion
        device.handleMatterEvent("onOff", "onOff", true);
        verify(colorItem).send(OnOffType.ON, MATTER_SOURCE);
        device.updateState(colorItem, initialHSBState);

        device.handleMatterEvent("colorControl", "currentHue", Double.valueOf(127));
        device.handleMatterEvent("colorControl", "currentSaturation", Double.valueOf(127));

        verify(colorItem).send(new HSBType(new DecimalType(180), new PercentType(50), new PercentType(50)),
                MATTER_SOURCE);
    }

    @Test
    void testHandleMatterEventLevel() {
        device.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(127));
        verify(colorItem).send(new PercentType(50), MATTER_SOURCE);
    }

    @Test
    void testALevelAlwaysMeansOn() {
        // the bridge turns a light off with an onOff event, so no level may read back as off
        device.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(1));
        device.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(2));
        device.handleMatterEvent("levelControl", "currentLevel", Double.valueOf(4));
        verify(colorItem, Mockito.times(3)).send(new PercentType(1), MATTER_SOURCE);
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
        // a color set in openHAB switches apps to hue and saturation
        assertEquals(8, firstStates.size());
        assertListContains(firstStates, "colorControl", "colorMode", 0);
        assertListContains(firstStates, "colorControl", "enhancedColorMode", 0);
        assertEquals("levelControl", firstStates.get(0).clusterName);
        assertEquals("onOff", firstStates.get(1).clusterName);

        assertListContains(firstStates, "onOff", "onOff", true);
        assertListContains(firstStates, "levelControl", "currentLevel", 254);
        assertListContains(firstStates, "colorControl", "currentHue", 127);
        assertListContains(firstStates, "colorControl", "currentSaturation", 254);
        assertListContains(firstStates, "colorControl", "currentX", xyValue(hsb, 0));
        assertListContains(firstStates, "colorControl", "currentY", xyValue(hsb, 1));

        // Second call assertions, an off light reports the minimum level
        List<AttributeState> secondStates = capturedCalls.get(1);
        // a brightness change leaves the color mode alone
        assertEquals(6, secondStates.size());
        assertListContains(secondStates, "onOff", "onOff", false);
        assertListContains(secondStates, "levelControl", "currentLevel", 1);
        assertListContains(secondStates, "colorControl", "currentHue", 127);
        assertListContains(secondStates, "colorControl", "currentSaturation", 254);
        // an off light keeps its color
        assertListContains(secondStates, "colorControl", "currentX", xyValue(hsb, 0));
        assertListContains(secondStates, "colorControl", "currentY", xyValue(hsb, 1));
    }

    @Test
    void testHandleMatterEventXy() {
        device.handleMatterEvent("onOff", "onOff", true);
        device.updateState(colorItem, initialHSBState);
        HSBType teal = new HSBType(new DecimalType(200), new PercentType(60), new PercentType(100));

        device.handleMatterEvent("colorControl", "currentX", Double.valueOf(xyValue(teal, 0)));
        verify(colorItem, never()).send(any(HSBType.class), any());
        device.handleMatterEvent("colorControl", "currentY", Double.valueOf(xyValue(teal, 1)));

        ArgumentCaptor<HSBType> captor = ArgumentCaptor.forClass(HSBType.class);
        verify(colorItem).send(captor.capture(), eq(MATTER_SOURCE));
        HSBType sent = captor.getValue();
        // converting to x and y and back loses a little precision
        double hueDifference = Math.abs(sent.getHue().doubleValue() - 200);
        assertTrue(Math.min(hueDifference, 360 - hueDifference) < 3, "hue " + sent.getHue());
        assertTrue(Math.abs(sent.getSaturation().doubleValue() - 60) < 3, "saturation " + sent.getSaturation());
        assertEquals(50, sent.getBrightness().intValue());
    }

    @Test
    void testEchoOfAppColorKeepsColorMode() {
        device.handleMatterEvent("onOff", "onOff", true);
        device.updateState(colorItem, initialHSBState);
        // an app picks a color temperature, which the bridge sends to the item as a color
        device.handleMatterEvent("colorControl", "colorTemperatureMireds", Double.valueOf(370));
        ArgumentCaptor<HSBType> sent = ArgumentCaptor.forClass(HSBType.class);
        verify(colorItem).send(sent.capture(), eq(MATTER_SOURCE));

        // the device reports it back, slightly rounded
        HSBType color = sent.getValue();
        device.updateState(colorItem,
                new HSBType(new DecimalType(color.getHue().doubleValue() + 0.5),
                        new PercentType(BigDecimal.valueOf(Math.min(100, color.getSaturation().doubleValue() + 0.5))),
                        color.getBrightness()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AttributeState>> states = ArgumentCaptor.forClass(List.class);
        verify(client, times(2)).setEndpointStates(any(), states.capture());
        assertTrue(states.getValue().stream().noneMatch(state -> "colorMode".equals(state.attributeName)));
    }

    private static int xyValue(HSBType hsb, int index) {
        double[] xy = ColorUtil.hsbToXY(new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED));
        return (int) Math.round(xy[index] * 65536);
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
        assertEquals(xyValue(hsb, 0), colorMap.get("currentX"));
        assertEquals(xyValue(hsb, 1), colorMap.get("currentY"));
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
