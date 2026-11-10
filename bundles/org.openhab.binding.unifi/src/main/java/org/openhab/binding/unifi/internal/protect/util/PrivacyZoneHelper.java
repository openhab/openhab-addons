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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Utilities for managing the privacy zone owned by openHAB without changing user-created zones.
 *
 * @author Joseph Matz - Initial contribution
 */
@NonNullByDefault
public final class PrivacyZoneHelper {

    public static final String OPENHAB_PRIVACY_ZONE_NAME = "openHAB Privacy Mode";

    private static final String OPENHAB_PRIVACY_ZONE_COLOR = "#586CED";

    private PrivacyZoneHelper() {
    }

    public static boolean isPrivacyModeEnabled(@Nullable List<Camera.PrivacyZone> privacyZones) {
        return privacyZones != null
                && privacyZones.stream().anyMatch(zone -> OPENHAB_PRIVACY_ZONE_NAME.equals(zone.name));
    }

    /**
     * Return a copy of the supplied privacy-zone array with the openHAB privacy zone added or removed.
     * Existing zone objects are copied as raw JSON so fields unknown to this binding are preserved.
     */
    public static JsonArray updatePrivacyZones(@Nullable JsonArray privacyZones, boolean enabled) {
        JsonArray updated = new JsonArray();
        int nextId = 1;
        boolean managedZoneKept = false;

        if (privacyZones != null) {
            for (JsonElement zoneElement : privacyZones) {
                if (zoneElement.isJsonObject()) {
                    JsonObject zone = zoneElement.getAsJsonObject();
                    JsonElement idElement = zone.get("id");
                    if (idElement instanceof JsonPrimitive primitive && primitive.isNumber()) {
                        nextId = Math.max(nextId, primitive.getAsInt() + 1);
                    }

                    if (isManagedZone(zone)) {
                        if (!enabled || managedZoneKept) {
                            continue;
                        }
                        managedZoneKept = true;
                    }
                }
                updated.add(zoneElement.deepCopy());
            }
        }

        if (enabled && !managedZoneKept) {
            updated.add(createManagedZone(nextId));
        }

        return updated;
    }

    private static boolean isManagedZone(JsonObject zone) {
        JsonElement nameElement = zone.get("name");
        return nameElement instanceof JsonPrimitive primitive && primitive.isString()
                && OPENHAB_PRIVACY_ZONE_NAME.equals(primitive.getAsString());
    }

    private static JsonObject createManagedZone(int id) {
        JsonObject zone = new JsonObject();
        zone.addProperty("id", id);
        zone.addProperty("name", OPENHAB_PRIVACY_ZONE_NAME);
        zone.addProperty("color", OPENHAB_PRIVACY_ZONE_COLOR);

        JsonArray points = new JsonArray();
        points.add(point(0, 0));
        points.add(point(1, 0));
        points.add(point(1, 1));
        points.add(point(0, 1));
        zone.add("points", points);

        zone.addProperty("isTriggerLightEnabled", false);
        zone.add("direction", JsonNull.INSTANCE);
        zone.addProperty("uniqueId", "privacyZones-" + id);
        zone.add("mergeId", JsonNull.INSTANCE);
        zone.add("objectTypes", new JsonArray());
        zone.add("sensitivity", JsonNull.INSTANCE);
        zone.add("loiterTriggers", new JsonArray());
        zone.add("quality", JsonNull.INSTANCE);
        zone.add("isTargetCounting", JsonNull.INSTANCE);
        zone.add("plan", JsonNull.INSTANCE);
        zone.add("originalType", JsonNull.INSTANCE);
        zone.add("zoneIds", JsonNull.INSTANCE);
        return zone;
    }

    private static JsonArray point(int x, int y) {
        JsonArray point = new JsonArray();
        point.add(x);
        point.add(y);
        return point;
    }
}
