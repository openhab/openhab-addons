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

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Test class for HumiditySensorDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class HumiditySensorDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private NumberItem numberItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private HumiditySensorDevice device;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        numberItem = Mockito.spy(new NumberItem("testHumidity"));
        device = new HumiditySensorDevice(metadataRegistry, client, numberItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("HumiditySensor", device.deviceType());
    }

    @Test
    void testUpdateStateWithDecimalType() {
        device.updateState(numberItem, new DecimalType(50.0));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(5000));

        device.updateState(numberItem, new DecimalType(0.0));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(0));

        device.updateState(numberItem, new DecimalType(100.0));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(10000));
    }

    @Test
    void testUpdateStateWithQuantityType() {
        device.updateState(numberItem, new QuantityType<Dimensionless>(50.0, Units.PERCENT));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(5000));

        device.updateState(numberItem, new QuantityType<Dimensionless>(0.0, Units.PERCENT));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(0));

        device.updateState(numberItem, new QuantityType<Dimensionless>(100.0, Units.PERCENT));
        verify(client).setEndpointState(any(), eq("relativeHumidityMeasurement"), eq("measuredValue"), eq(10000));
    }

    @Test
    void testActivate() {
        numberItem.setState(new DecimalType(50.0));
        MatterDeviceOptions options = device.activate();

        Map<String, Object> humidityMap = options.clusters.get("relativeHumidityMeasurement");
        assertNotNull(humidityMap);
        assertEquals(5000, humidityMap.get("measuredValue"));
    }

    @Test
    void testActivateWithZeroState() {
        numberItem.setState(new DecimalType(0));
        MatterDeviceOptions options = device.activate();

        Map<String, Object> humidityMap = options.clusters.get("relativeHumidityMeasurement");
        assertNotNull(humidityMap);
        assertEquals(0, humidityMap.get("measuredValue"));
    }

    @Test
    void testActivateWithMaxState() {
        numberItem.setState(new DecimalType(100));
        MatterDeviceOptions options = device.activate();

        Map<String, Object> humidityMap = options.clusters.get("relativeHumidityMeasurement");
        assertNotNull(humidityMap);
        assertEquals(10000, humidityMap.get("measuredValue"));
    }
}
