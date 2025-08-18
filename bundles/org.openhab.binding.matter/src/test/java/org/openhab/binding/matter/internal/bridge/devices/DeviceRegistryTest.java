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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.StringItem;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class DeviceRegistryTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;

    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    private GenericItem item = new StringItem("testItem");

    @Test
    void createOnOffLightDevice() {
        BaseDevice device = DeviceRegistry.createDevice("OnOffLight", metadataRegistry, client, item);

        assertNotNull(device);
        assertTrue(device instanceof OnOffLightDevice);
    }

    @Test
    void createThermostatDevice() {
        BaseDevice device = DeviceRegistry.createDevice("Thermostat", metadataRegistry, client, item);

        assertNotNull(device);
        assertTrue(device instanceof ThermostatDevice);
    }

    @Test
    void createInvalidDeviceType() {
        BaseDevice device = DeviceRegistry.createDevice("InvalidDeviceType", metadataRegistry, client, item);

        assertNull(device);
    }

    @Test
    void createAllDevices() {
        String[] deviceTypes = { "OnOffLight", "OnOffPlugInUnit", "DimmableLight", "Thermostat", "WindowCovering",
                "DoorLock", "TemperatureSensor", "HumiditySensor", "OccupancySensor", "ContactSensor", "ColorLight" };

        for (String deviceType : deviceTypes) {
            BaseDevice device = DeviceRegistry.createDevice(deviceType, metadataRegistry, client, item);
            assertNotNull(device, "Device creation failed for type: " + deviceType);
        }
    }
}
