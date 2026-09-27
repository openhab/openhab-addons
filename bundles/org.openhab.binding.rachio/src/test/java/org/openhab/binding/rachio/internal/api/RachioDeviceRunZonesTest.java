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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests controller-level multi-zone watering payload generation.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioDeviceRunZonesTest {
    @Test
    void multiZonePayloadUsesControllerRunTimeForEverySelectedZone() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6), zone("zone-7-id", 7));
        Objects.requireNonNull(device.getZoneByNumber(6)).setStartRunTime(0);
        Objects.requireNonNull(device.getZoneByNumber(7)).setStartRunTime(0);
        device.setRunTime(30);
        device.setRunZones("6,7");

        String json = device.getAllRunZonesJson(120);

        assertSelectedZones(json, new String[] { "zone-6-id", "zone-7-id" }, 30);
    }

    @Test
    void multiZonePayloadFallsBackToDefaultRuntimeWhenControllerRunTimeIsZero() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6), zone("zone-7-id", 7));
        device.setRunTime(0);
        device.setRunZones("6,7");

        String json = device.getAllRunZonesJson(120);

        assertSelectedZones(json, new String[] { "zone-6-id", "zone-7-id" }, 120);
    }

    @Test
    void multiZonePayloadIgnoresZoneRunTimesWhenControllerRunTimeIsConfigured() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6), zone("zone-7-id", 7));
        Objects.requireNonNull(device.getZoneByNumber(6)).setStartRunTime(45);
        Objects.requireNonNull(device.getZoneByNumber(7)).setStartRunTime(60);
        device.setRunTime(30);
        device.setRunZones("6,7");

        String json = device.getAllRunZonesJson(120);

        assertThat(device.getMultiZoneRunTime(120), is(30));
        assertSelectedZones(json, new String[] { "zone-6-id", "zone-7-id" }, 30);
    }

    @Test
    void activeZoneEventStoresResolvedZoneIdentity() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(6));

        assertThat(device.applyActiveZoneEvent("ZONE_STARTED", 6, zone), is(true));

        assertThat(device.activeZoneNumber, is(6));
        assertThat(device.activeZoneName, is("Zone 6"));
        assertThat(device.activeZoneId, is("zone-6-id"));
    }

    @Test
    void activeZonePauseEventDoesNotClearCurrentZone() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(6));
        device.applyActiveZoneEvent("ZONE_STARTED", 6, zone);

        assertThat(device.applyActiveZoneEvent("ZONE_CYCLING", 6, zone), is(false));

        assertThat(device.activeZoneNumber, is(6));
        assertThat(device.activeZoneName, is("Zone 6"));
        assertThat(device.activeZoneId, is("zone-6-id"));
    }

    @Test
    void activeZoneStopAndCompleteEventsClearCurrentZone() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(6));
        device.applyActiveZoneEvent("ZONE_STARTED", 6, zone);

        assertThat(device.applyActiveZoneEvent("ZONE_STOPPED", 6, zone), is(true));
        assertThat(device.activeZoneNumber, is(-1));
        assertThat(device.activeZoneName, is(""));
        assertThat(device.activeZoneId, is(""));

        device.applyActiveZoneEvent("ZONE_STARTED", 6, zone);
        assertThat(device.applyActiveZoneEvent("ZONE_COMPLETED", 6, zone), is(true));
        assertThat(device.activeZoneNumber, is(-1));
        assertThat(device.activeZoneName, is(""));
        assertThat(device.activeZoneId, is(""));
    }

    @Test
    void completedOlderZoneDoesNotClearNewerActiveZone() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6), zone("zone-7-id", 7));
        RachioZone oldZone = Objects.requireNonNull(device.getZoneByNumber(6));
        RachioZone activeZone = Objects.requireNonNull(device.getZoneByNumber(7));
        device.applyActiveZoneEvent("ZONE_STARTED", 7, activeZone);

        assertThat(device.applyActiveZoneEvent("ZONE_COMPLETED", 6, oldZone), is(false));

        assertThat(device.activeZoneNumber, is(7));
        assertThat(device.activeZoneName, is("Zone 7"));
        assertThat(device.activeZoneId, is("zone-7-id"));
    }

    @Test
    void activeZoneEventKeepsZoneNumberWhenZoneCannotBeResolved() {
        RachioDevice device = deviceWithZones(zone("zone-6-id", 6));

        assertThat(device.applyActiveZoneEvent("ZONE_STARTED", 7, null), is(true));

        assertThat(device.activeZoneNumber, is(7));
        assertThat(device.activeZoneName, is(""));
        assertThat(device.activeZoneId, is(""));
    }

    private RachioDevice deviceWithZones(RachioCloudZone... zones) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "device-id";
        cloudDevice.name = "Test Controller";
        cloudDevice.macAddress = "ABCDEF123456";
        cloudDevice.zones.addAll(java.util.List.of(zones));
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudZone zone(String id, int zoneNumber) {
        RachioCloudZone zone = new RachioCloudZone();
        zone.id = id;
        zone.name = "Zone " + zoneNumber;
        zone.zoneNumber = zoneNumber;
        zone.enabled = true;
        return zone;
    }

    private void assertSelectedZones(String json, String[] expectedIds, int expectedDuration) {
        JsonArray zones = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("zones");
        assertThat(zones.size(), is(expectedIds.length));
        for (int index = 0; index < expectedIds.length; index++) {
            JsonObject zone = zones.get(index).getAsJsonObject();
            assertThat(zone.get("id").getAsString(), is(expectedIds[index]));
            assertThat(zone.get("duration").getAsInt(), is(expectedDuration));
            assertThat(zone.get("sortOrder").getAsInt(), is(index + 1));
        }
    }
}
