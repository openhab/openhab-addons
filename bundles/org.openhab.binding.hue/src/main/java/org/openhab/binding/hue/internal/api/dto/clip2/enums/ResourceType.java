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
package org.openhab.binding.hue.internal.api.dto.clip2.enums;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum for resource types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ResourceType {
    AUTH_V1,
    BEHAVIOR_INSTANCE,
    BEHAVIOR_SCRIPT,
    BELL_BUTTON,
    BRIDGE,
    BRIDGE_HOME,
    BUTTON,
    CAMERA_MOTION,
    CLIP,
    CONTACT,
    CONVENIENCE_AREA_MOTION,
    DEVICE,
    DEVICE_POWER,
    DEVICE_SOFTWARE_UPDATE,
    ENTERTAINMENT,
    ENTERTAINMENT_CONFIGURATION,
    GEOFENCE_CLIENT,
    GEOLOCATION,
    GROUPED_LIGHT,
    GROUPED_LIGHT_LEVEL,
    GROUPED_MOTION,
    HOMEKIT,
    LIGHT,
    LIGHT_LEVEL,
    MATTER,
    MATTER_FABRIC,
    MOTION,
    MOTION_AREA_CANDIDATE,
    MOTION_AREA_CONFIGURATION,
    PUBLIC_IMAGE,
    RELATIVE_ROTARY,
    ROOM,
    SCENE,
    SECURITY_AREA_MOTION,
    SERVICE_GROUP,
    SMART_SCENE,
    SPEAKER,
    TAMPER,
    TEMPERATURE,
    WIFI_CONNECTIVITY,
    ZGP_CONNECTIVITY,
    ZIGBEE_CONNECTIVITY,
    ZIGBEE_DEVICE_DISCOVERY,
    ZONE,
    // === special types ===
    UPDATE,
    ADD,
    DELETE,
    ERROR;

    public static final Set<ResourceType> SSE_TYPES = EnumSet.of(UPDATE, ADD, DELETE, ERROR);

    public static ResourceType of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return ERROR.setUnknownTypeId(value);
    }

    private @Nullable String unknownTypeId;

    private ResourceType setUnknownTypeId(@Nullable String value) {
        unknownTypeId = value;
        return this;
    }

    @Override
    public String toString() {
        String s = this.name().replace("_", " ");
        s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        return unknownTypeId == null ? s : s + String.format(" (%s)", unknownTypeId);
    }
}
