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
package org.openhab.binding.rachio.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_IP_MASK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudNetworkSettings;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests controller model reconciliation helpers.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioDeviceTest {

    @Test
    public void runListUsesExactZoneNumbers() {
        RachioDevice device = createDevice();
        device.runList = "1";

        JsonArray zones = selectedZones(device.getAllRunZonesJson(60));

        assertEquals(1, zones.size());
        assertEquals("zone-1", zones.get(0).getAsJsonObject().get("id").getAsString());
    }

    @Test
    public void runAllOnlyIncludesEnabledZonesInStableOrder() {
        RachioDevice device = createDevice();
        device.runList = "ALL";

        JsonArray zones = selectedZones(device.getAllRunZonesJson(60));

        assertEquals(2, zones.size());
        assertEquals("zone-1", zones.get(0).getAsJsonObject().get("id").getAsString());
        assertEquals("zone-11", zones.get(1).getAsJsonObject().get("id").getAsString());
        assertEquals(1, zones.get(0).getAsJsonObject().get("sortOrder").getAsInt());
        assertEquals(2, zones.get(1).getAsJsonObject().get("sortOrder").getAsInt());
    }

    @Test
    public void networkMaskPropertyUsesNetmask() {
        RachioDevice device = createDevice();
        RachioCloudNetworkSettings network = new RachioCloudNetworkSettings();
        network.ip = "192.0.2.10";
        network.nm = "255.255.255.0";
        device.setNetwork(network);

        assertEquals("255.255.255.0", device.fillProperties().get(PROPERTY_IP_MASK));
    }

    @Test
    public void zoneSnapshotsRemainStableWhileExistingZonesKeepTheirIdentity() {
        RachioDevice device = createDevice();
        Map<String, RachioZone> originalSnapshot = device.getZones();
        RachioZone originalZone = Objects.requireNonNull(originalSnapshot.get("zone-1"));
        originalZone.setStartRunTime(42);
        originalZone.recordLastWateredDate(200);
        Map<String, RachioZone> replacement = new HashMap<>(originalSnapshot);
        replacement.remove("zone-2");
        RachioCloudZone updatedCloudZone = zone("zone-1", 1, true);
        updatedCloudZone.name = "Updated zone";
        updatedCloudZone.lastWateredDate = 100;
        RachioZone updatedZone = new RachioZone(updatedCloudZone, device.getThingID());
        originalZone.update(updatedZone);
        replacement.put(originalZone.id, originalZone);

        device.replaceZones(replacement);

        assertEquals(3, originalSnapshot.size());
        assertEquals(2, device.getZones().size());
        assertSame(originalZone, Objects.requireNonNull(device.getZones().get("zone-1")));
        assertEquals("Updated zone", originalZone.name);
        assertEquals(42, originalZone.getStartRunTime());
        assertEquals(200, originalZone.lastWateredDate);
        assertThrows(UnsupportedOperationException.class, () -> originalSnapshot.clear());
    }

    private RachioDevice createDevice() {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "device-1";
        cloudDevice.name = "Controller";
        cloudDevice.zones = List.of(zone("zone-11", 11, true), zone("zone-2", 2, false), zone("zone-1", 1, true));
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudZone zone(String id, int number, boolean enabled) {
        RachioCloudZone zone = new RachioCloudZone();
        zone.id = id;
        zone.name = id;
        zone.zoneNumber = number;
        zone.enabled = enabled;
        return zone;
    }

    private JsonArray selectedZones(String json) {
        JsonObject request = JsonParser.parseString(json).getAsJsonObject();
        return request.getAsJsonArray("zones");
    }
}
