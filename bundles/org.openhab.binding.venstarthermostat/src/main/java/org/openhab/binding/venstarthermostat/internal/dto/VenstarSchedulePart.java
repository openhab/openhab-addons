/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VenstarSchedulePart} represents the schedule part (part of day) returned
 * from the REST API.
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public enum VenstarSchedulePart {
    MORNING(0, "morning", "Morning"),
    DAY(1, "day", "Day"),
    EVENING(2, "evening", "Evening"),
    NIGHT(3, "night", "Night"),
    INACTIVE(255, "inactive", "Inactive");

    private int part;
    private String name;
    private String friendlyName;

    VenstarSchedulePart(int part, String name, String friendlyName) {
        this.part = part;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public int part() {
        return part;
    }

    public String partName() {
        return name;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public static VenstarSchedulePart fromInt(int part) throws IllegalArgumentException {
        for (VenstarSchedulePart sp : values()) {
            if (sp.part == part) {
                return sp;
            }
        }

        throw (new IllegalArgumentException("Invalid schedule part " + part));
    }
}
