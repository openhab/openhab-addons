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
package org.openhab.binding.unifi.internal.protect.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Tests privacy-zone preservation and idempotent openHAB privacy-mode updates.
 *
 * @author Joseph Matz - Initial contribution
 */
@NonNullByDefault
public class PrivacyZoneHelperTest {

    @Test
    public void enablePreservesExistingZoneAndAddsManagedZone() {
        JsonObject existing = zone(7, "Driveway");
        existing.addProperty("futureField", "keep-me");
        JsonArray privacyZones = new JsonArray();
        privacyZones.add(existing);

        JsonArray updated = PrivacyZoneHelper.updatePrivacyZones(privacyZones, true);

        assertEquals(2, updated.size());
        assertEquals(existing, updated.get(0));
        assertEquals("keep-me", updated.get(0).getAsJsonObject().get("futureField").getAsString());

        JsonObject managed = updated.get(1).getAsJsonObject();
        assertEquals(8, managed.get("id").getAsInt());
        assertEquals(PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME, managed.get("name").getAsString());
        assertEquals("privacyZones-8", managed.get("uniqueId").getAsString());
        assertFalse(managed.get("isTriggerLightEnabled").getAsBoolean());
        assertEquals(4, managed.getAsJsonArray("points").size());

        assertEquals(1, privacyZones.size(), "input array must not be mutated");
    }

    @Test
    public void disableRemovesOnlyManagedZone() {
        JsonObject first = zone(1, "Front Walk");
        first.addProperty("unknownSetting", 123);
        JsonObject managed = zone(2, PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME);
        managed.addProperty("serverOnlyField", "preserve-while-enabled");
        JsonObject third = zone(9, "Neighbor Window");

        JsonArray privacyZones = new JsonArray();
        privacyZones.add(first);
        privacyZones.add(managed);
        privacyZones.add(third);

        JsonArray updated = PrivacyZoneHelper.updatePrivacyZones(privacyZones, false);

        assertEquals(2, updated.size());
        assertEquals(first, updated.get(0));
        assertEquals(third, updated.get(1));
        assertEquals(3, privacyZones.size(), "input array must not be mutated");
    }

    @Test
    public void enableIsIdempotentAndPreservesManagedZoneFields() {
        JsonObject existing = zone(5, "Existing");
        JsonObject managed = zone(12, PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME);
        managed.addProperty("serverOnlyField", "keep-me");

        JsonArray privacyZones = new JsonArray();
        privacyZones.add(existing);
        privacyZones.add(managed);

        JsonArray updated = PrivacyZoneHelper.updatePrivacyZones(privacyZones, true);

        assertEquals(privacyZones, updated);
        assertEquals("keep-me", updated.get(1).getAsJsonObject().get("serverOnlyField").getAsString());
    }

    @Test
    public void duplicateManagedZonesAreCollapsed() {
        JsonArray privacyZones = new JsonArray();
        privacyZones.add(zone(2, PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME));
        privacyZones.add(zone(3, "Existing"));
        privacyZones.add(zone(4, PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME));

        JsonArray updated = PrivacyZoneHelper.updatePrivacyZones(privacyZones, true);

        assertEquals(2, updated.size());
        assertEquals(PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME,
                updated.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals("Existing", updated.get(1).getAsJsonObject().get("name").getAsString());
    }

    @Test
    public void typedPrivacyZonesReportManagedState() {
        Camera.PrivacyZone existing = new Camera.PrivacyZone();
        existing.name = "Existing";
        Camera.PrivacyZone managed = new Camera.PrivacyZone();
        managed.name = PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME;

        assertFalse(PrivacyZoneHelper.isPrivacyModeEnabled(null));
        assertFalse(PrivacyZoneHelper.isPrivacyModeEnabled(List.of(existing)));
        assertTrue(PrivacyZoneHelper.isPrivacyModeEnabled(List.of(existing, managed)));
    }

    private static JsonObject zone(int id, String name) {
        JsonObject zone = new JsonObject();
        zone.addProperty("id", id);
        zone.addProperty("name", name);
        zone.addProperty("color", "#123456");
        zone.add("points", new JsonArray());
        return zone;
    }
}
