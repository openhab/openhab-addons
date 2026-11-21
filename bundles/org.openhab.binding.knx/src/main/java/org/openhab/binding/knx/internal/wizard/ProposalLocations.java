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
package org.openhab.binding.knx.internal.wizard;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The semantic location tag and the icon a location group gets from the kind of its space.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ProposalLocations {

    public static final String BUILDING = "Building";
    public static final String FLOOR = "Floor";
    public static final String ROOM = "Room";
    public static final String CORRIDOR = "Corridor";
    public static final String OUTDOOR = "Outdoor";

    /** The tags a location group may carry; each is a location tag of core 5.2. */
    public static final List<String> TAGS = List.of(BUILDING, FLOOR, ROOM, CORRIDOR, OUTDOOR);

    static final String GROUP_ICON = "group";

    private ProposalLocations() {
    }

    /**
     * @param type the type of the space as the project writes it
     * @return the tag, {@code null} for a kind no location tag fits
     */
    public static @Nullable String tag(String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "building", "buildingpart" -> BUILDING;
            case "floor" -> FLOOR;
            case "room" -> ROOM;
            case "corridor", "stairway" -> CORRIDOR;
            case "area", "ground" -> OUTDOOR;
            default -> null;
        };
    }

    /** {@return the icon of the classic set for the kind of space, {@code group} when it has none} */
    public static String category(String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "building", "buildingpart" -> "house";
            case "floor" -> "groundfloor";
            case "corridor" -> "corridor";
            case "area", "ground" -> "garden";
            default -> GROUP_ICON;
        };
    }
}
