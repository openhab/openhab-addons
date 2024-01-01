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
package org.openhab.binding.miio.internal.robot;

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
    ERROR_DUST_CONTAINER(46, "Missing dust container/dust bag");

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
        byte b;
        int i;
        DockStatusType[] arrayOfDockStatusType;
        for (i = (arrayOfDockStatusType = values()).length, b = 0; b < i;) {
            DockStatusType st = arrayOfDockStatusType[b];
            if (st.getId() == value) {
                return st;
            }
            b++;
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
