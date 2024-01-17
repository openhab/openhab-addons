/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum for product archetypes.
 *
 * @see <a href="https://developers.meethue.com/develop/hue-api-v2/api-reference/#resource_light_get">API v2
 *      documentation</a>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum Archetype {
    // device archetypes
    BRIDGE_V2,
    UNKNOWN_ARCHETYPE,
    CLASSIC_BULB,
    SULTAN_BULB,
    FLOOD_BULB,
    SPOT_BULB,
    CANDLE_BULB,
    LUSTER_BULB,
    PENDANT_ROUND,
    PENDANT_LONG,
    CEILING_ROUND,
    CEILING_SQUARE,
    FLOOR_SHADE,
    FLOOR_LANTERN,
    TABLE_SHADE,
    RECESSED_CEILING,
    RECESSED_FLOOR,
    SINGLE_SPOT,
    DOUBLE_SPOT,
    TABLE_WASH,
    WALL_LANTERN,
    WALL_SHADE,
    FLEXIBLE_LAMP,
    GROUND_SPOT,
    WALL_SPOT,
    PLUG,
    HUE_GO,
    HUE_LIGHTSTRIP,
    HUE_IRIS,
    HUE_BLOOM,
    BOLLARD,
    WALL_WASHER,
    HUE_PLAY,
    VINTAGE_BULB,
    CHRISTMAS_TREE,
    HUE_CENTRIS,
    HUE_LIGHTSTRIP_TV,
    HUE_TUBE,
    HUE_SIGNE,
    STRING_LIGHT,
    // room archetypes
    LIVING_ROOM,
    KITCHEN,
    DINING,
    BEDROOM,
    KIDS_BEDROOM,
    BATHROOM,
    NURSERY,
    RECREATION,
    OFFICE,
    GYM,
    HALLWAY,
    TOILET,
    FRONT_DOOR,
    GARAGE,
    TERRACE,
    GARDEN,
    DRIVEWAY,
    CARPORT,
    HOME,
    DOWNSTAIRS,
    UPSTAIRS,
    TOP_FLOOR,
    ATTIC,
    GUEST_ROOM,
    STAIRCASE,
    LOUNGE,
    MAN_CAVE,
    COMPUTER,
    STUDIO,
    MUSIC,
    TV,
    READING,
    CLOSET,
    STORAGE,
    LAUNDRY_ROOM,
    BALCONY,
    PORCH,
    BARBECUE,
    POOL,
    OTHER;

    public static Archetype of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return UNKNOWN_ARCHETYPE;
    }

    @Override
    public String toString() {
        String s = this.name().replace("_", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
