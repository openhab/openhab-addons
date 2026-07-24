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
package org.openhab.binding.ecovacs.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.impl.DeviceDescription;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.model.DeviceType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Tests for device description parsing and device type detection.
 *
 * @author Stefan Höhn - Initial contribution
 */
class DeviceDescriptionTest {

    private final Gson gson = new Gson();

    @Test
    void testDeviceTypeDefaultsToVacuum() {
        DeviceDescription desc = new DeviceDescription("Deebot Test", "abc123", null, DeviceType.VACUUM,
                org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion.JSON_V2, true, new java.util.HashSet<>());
        assertEquals(DeviceType.VACUUM, desc.deviceType);
    }

    @Test
    void testDeviceTypeSetToMower() {
        DeviceDescription desc = new DeviceDescription("GOAT Test", "xyz789", null, DeviceType.MOWER,
                org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion.JSON_V2, true, new java.util.HashSet<>());
        assertEquals(DeviceType.MOWER, desc.deviceType);
    }

    @Test
    void testSupportedDeviceListContainsGoatO500() {
        List<DeviceDescription> devices = loadSupportedDeviceList();
        assertTrue(devices.stream().anyMatch(d -> "300lc5".equals(d.deviceClass)),
                "GOAT O500 Panorama (300lc5) should be in supported device list");
    }

    @Test
    void testGoatO500IsMowerType() {
        List<DeviceDescription> devices = loadSupportedDeviceList();
        DeviceDescription goat = devices.stream().filter(d -> "300lc5".equals(d.deviceClass)).findFirst().orElse(null);
        assertNotNull(goat);
        assertEquals(DeviceType.MOWER, goat.deviceType);
        assertEquals("GOAT O500 Panorama", goat.modelName);
    }

    @Test
    void testGoatO500HasMowingCapability() {
        List<DeviceDescription> devices = loadSupportedDeviceList();
        DeviceDescription goat = devices.stream().filter(d -> "300lc5".equals(d.deviceClass)).findFirst().orElse(null);
        assertNotNull(goat);
        assertTrue(goat.capabilities.contains(DeviceCapability.MOWING));
        assertTrue(goat.capabilities.contains(DeviceCapability.CUTTING_HEIGHT));
        assertTrue(goat.capabilities.contains(DeviceCapability.ZONE_MOWING));
        assertTrue(goat.capabilities.contains(DeviceCapability.EDGE_MOWING));
    }

    @Test
    void testExistingVacuumsRemainVacuumType() {
        List<DeviceDescription> devices = loadSupportedDeviceList();
        // Deebot OZMO 950 (class ls1ok3 or similar) should be VACUUM
        DeviceDescription vacuum = devices.stream().filter(d -> d.modelName.contains("DEEBOT")).findFirst()
                .orElse(null);
        assertNotNull(vacuum);
        assertEquals(DeviceType.VACUUM, vacuum.deviceType);
    }

    @Test
    void testDeviceClassLinkResolvesDeviceType() {
        // Verify that linked GOAT devices in the supported list resolve to MOWER type
        List<DeviceDescription> devices = loadSupportedDeviceList();

        // Find a linked GOAT device (e.g., "77atlz" links to "guzput")
        DeviceDescription linked = devices.stream().filter(d -> "77atlz".equals(d.deviceClass)).findFirst()
                .orElse(null);
        assertNotNull(linked, "GOAT G1 variant (77atlz) should be in supported device list");
        assertNotNull(linked.deviceClassLink, "Should have a deviceClassLink");

        // Find the target
        DeviceDescription target = devices.stream().filter(d -> linked.deviceClassLink.equals(d.deviceClass))
                .findFirst().orElse(null);
        assertNotNull(target, "Target device (guzput) should be in supported device list");
        assertEquals(DeviceType.MOWER, target.deviceType);

        // Resolve the link
        DeviceDescription resolved = linked.resolveLinkWith(target);
        assertEquals(DeviceType.MOWER, resolved.deviceType);
        assertTrue(resolved.capabilities.contains(DeviceCapability.MOWING));
    }

    private List<DeviceDescription> loadSupportedDeviceList() {
        ClassLoader cl = Objects.requireNonNull(getClass().getClassLoader());
        JsonReader reader = new JsonReader(new InputStreamReader(
                Objects.requireNonNull(cl.getResourceAsStream("devices/supported_device_list.json"))));
        Type listType = new TypeToken<List<DeviceDescription>>() {
        }.getType();
        return gson.fromJson(reader, listType);
    }
}
