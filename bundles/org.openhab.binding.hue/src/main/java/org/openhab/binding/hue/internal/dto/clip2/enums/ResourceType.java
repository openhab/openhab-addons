/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2.enums;

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
    BRIDGE,
    BRIDGE_HOME,
    BUTTON,
    DEVICE,
    DEVICE_POWER,
    ENTERTAINMENT,
    ENTERTAINMENT_CONFIGURATION,
    GEOFENCE,
    GEOFENCE_CLIENT,
    GEOLOCATION,
    GROUPED_LIGHT,
    HOMEKIT,
    LIGHT,
    LIGHT_LEVEL,
    MOTION,
    PUBLIC_IMAGE,
    ROOM,
    RELATIVE_ROTARY,
    SCENE,
    SMART_SCENE,
    TEMPERATURE,
    ZGP_CONNECTIVITY,
    ZIGBEE_CONNECTIVITY,
    ZONE,
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
