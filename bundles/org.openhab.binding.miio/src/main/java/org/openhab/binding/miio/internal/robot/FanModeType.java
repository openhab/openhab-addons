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
package org.openhab.binding.miio.internal.robot;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of Errors
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum FanModeType {

    SILENT(38, "Silent"),
    STANDARD(60, "Standard"),
    TURBO(75, "Turbo"),
    POWER(77, "Power"),
    FULL(90, "Full"),
    MAX(100, "Max"),
    QUIET(101, "Quiet"),
    BALANCED(102, "Balanced"),
    TURBO2(103, "Turbo"),
    MAX2(104, "Max"),
    MOB(105, "Mob"),
    CUSTOM(-1, "Custom");

    private final int id;
    private final String description;

    FanModeType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static FanModeType getType(int value) {
        for (FanModeType st : FanModeType.values()) {
            if (st.getId() == value) {
                return st;
            }
        }
        return CUSTOM;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Status " + Integer.toString(id) + ": " + description;
    }
}
