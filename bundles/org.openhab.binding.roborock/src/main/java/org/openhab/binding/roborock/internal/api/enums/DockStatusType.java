/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of dockingstation status
 *
 * @author David Kumar - Initial contribution
 */
@NonNullByDefault
public enum DockStatusType {
    UNKNOWN(-1, "Unknown"),
    OK(0, "OK"),
    ERROR_SUCTION(34, "Suction Error"),
    ERROR_FRESH_WATER_TANK(38, "Error fresh water tank"),
    ERROR_FRESH_DIRTY_WATER_TANK(39, "Error dirty water tank"),
    MAINTENANCE_BRUSH_JAMMED(42, "Maintenance brush jammed"),
    DIRTY_TANK_LATCH_OPEN(44, "Dirty tank latch open"),
    ERROR_DUST_CONTAINER(46, "Missing dust container/dust bag"),
    CLEANING_TANK_FULL_OR_BLOCKED(53, "Cleaning tank full or blocked");

    private final int id;
    private final String description;

    DockStatusType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return this.id;
    }

    public static DockStatusType getType(int value) {
        for (DockStatusType st : values()) {
            if (st.getId() == value) {
                return st;
            }
        }

        return UNKNOWN;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "Status " + Integer.toString(this.id) + ": " + this.description;
    }
}
