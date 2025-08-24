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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.BridgedEndpoint;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MetaDataMapping;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;

/**
 * Test class for GenericDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class GenericDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private NumberItem numberItem;
    @NonNullByDefault({})
    private TestGenericDevice device;

    @SuppressWarnings("null")
    private static class TestGenericDevice extends BaseDevice {
        public TestGenericDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client,
                GenericItem primaryItem) {
            super(metadataRegistry, client, primaryItem);
        }

        @Override
        public String deviceType() {
            return "TestDevice";
        }

        @Override
        public MatterDeviceOptions activate() {
            MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
            Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
            return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
        }

        @Override
        public void dispose() {
        }

        @Override
        public void updateState(org.openhab.core.items.Item item, org.openhab.core.types.State state) {
        }

        @Override
        public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        Map<String, Object> config = Map.of("label", "Test Label", "fixedLabels", "ON=1, OFF=0", "cluster.attribute",
                "value");
        Metadata metadata = new Metadata(key, "attr1,attr2", config);
        numberItem = new NumberItem("testNumber");
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);
        when(client.addEndpoint(any(BridgedEndpoint.class))).thenReturn(CompletableFuture.completedFuture("success"));

        device = new TestGenericDevice(metadataRegistry, client, numberItem);
    }

    @Test
    void testLevelToPercent() {
        assertEquals(0, ValueUtils.levelToPercent(0).intValue());
        assertEquals(50, ValueUtils.levelToPercent(127).intValue());
        assertEquals(100, ValueUtils.levelToPercent(254).intValue());
    }

    @Test
    void testPercentToLevel() {
        assertEquals(0, ValueUtils.percentToLevel(new PercentType(0)));
        assertEquals(127, ValueUtils.percentToLevel(new PercentType(50)));
        assertEquals(254, ValueUtils.percentToLevel(new PercentType(100)));
    }

    @Test
    void testTemperatureToValue() {
        // Test Celsius values
        assertEquals(2000, ValueUtils.temperatureToValue(new QuantityType<Temperature>(20.0, SIUnits.CELSIUS)));
        assertEquals(0, ValueUtils.temperatureToValue(new QuantityType<Temperature>(0.0, SIUnits.CELSIUS)));
        assertEquals(-1000, ValueUtils.temperatureToValue(new QuantityType<Temperature>(-10.0, SIUnits.CELSIUS)));

        // Test Fahrenheit values
        assertEquals(0, ValueUtils.temperatureToValue(new QuantityType<Temperature>(32.0, ImperialUnits.FAHRENHEIT)));
        assertEquals(2000,
                ValueUtils.temperatureToValue(new QuantityType<Temperature>(68.0, ImperialUnits.FAHRENHEIT)));

        // Test DecimalType (assumed Celsius)
        assertEquals(2000, ValueUtils.temperatureToValue(new DecimalType(20.0)));
        assertEquals(-1000, ValueUtils.temperatureToValue(new DecimalType(-10.0)));
    }

    @SuppressWarnings({ "null" })
    @Test
    void testValueToTemperature() {
        assertEquals(20.0, ValueUtils.valueToTemperature(2000).toUnit(SIUnits.CELSIUS).doubleValue(), 0.01);
        assertEquals(0.0, ValueUtils.valueToTemperature(0).toUnit(SIUnits.CELSIUS).doubleValue(), 0.01);
        assertEquals(-10.0, ValueUtils.valueToTemperature(-1000).toUnit(SIUnits.CELSIUS).doubleValue(), 0.01);
    }

    @Test
    void testMetaDataMapping() {
        MetaDataMapping mapping = device.metaDataMapping(numberItem);
        assertEquals("Test Label", mapping.label);
        assertEquals(2, mapping.attributes.size());
        assertTrue(mapping.attributes.contains("attr1"));
        assertTrue(mapping.attributes.contains("attr2"));
        assertEquals("value", mapping.getAttributeOptions().get("cluster.attribute"));
    }

    @Test
    void testActivateBridgedEndpointValues() {
        BridgedEndpoint endpoint = device.activateBridgedEndpoint();
        assertEquals("TestDevice", endpoint.deviceType);
        assertEquals("testNumber", endpoint.id);
        assertEquals("Test Label", endpoint.nodeLabel);
        assertEquals("testNumber", endpoint.productName);
        assertEquals("Type Number", endpoint.productLabel);
        assertEquals(String.valueOf(numberItem.getName().hashCode()), endpoint.serialNumber);
    }

    @Test
    void testActivateBridgedEndpointTwiceThrows() {
        device.activateBridgedEndpoint();
        assertThrows(IllegalStateException.class, () -> device.activateBridgedEndpoint());
    }

    @Test
    void testMapClusterAttributes() {
        Map<String, Object> attributes = Map.of("cluster1.attr1", "value1", "cluster1.attr2", "value2",
                "cluster2.attr1", "value3");

        Map<String, Map<String, Object>> result = device.mapClusterAttributes(attributes);
        assertNotNull(result);
        Map<String, Object> cluster1 = result.get("cluster1");
        Map<String, Object> cluster2 = result.get("cluster2");
        assertNotNull(cluster1);
        assertNotNull(cluster2);
        assertEquals("value1", cluster1.get("attr1"));
        assertEquals("value2", cluster1.get("attr2"));
        assertEquals("value3", cluster2.get("attr1"));
    }

    @Test
    void testMapClusterAttributesInvalidFormat() {
        Map<String, Object> attributes = Map.of("invalidFormat", "value");
        assertThrows(IllegalArgumentException.class, () -> device.mapClusterAttributes(attributes));
    }
}
