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
 * List of Errors
 * derived from vacuum_cleaner-EN.pdf
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum VacuumErrorType {

    ERROR00(0, "No error"),
    ERROR01(1, "Laser sensor fault"),
    ERROR02(2, "Collision sensor fault"),
    ERROR03(3, "Wheel floating"),
    ERROR04(4, "Cliff sensor fault"),
    ERROR05(5, "Main brush blocked"),
    ERROR06(6, "Side brush blocked"),
    ERROR07(7, "Wheel blocked"),
    ERROR08(8, "Device stuck"),
    ERROR09(9, "Dust bin missing"),
    ERROR10(10, "Filter blocked"),
    ERROR11(11, "Magnetic field detected"),
    ERROR12(12, "Low battery"),
    ERROR13(13, "Charging problem"),
    ERROR14(14, "Battery failure"),
    ERROR15(15, "Wall sensor fault"),
    ERROR16(16, "Uneven surface"),
    ERROR17(17, "Side brush failure"),
    ERROR18(18, "Suction fan failure"),
    ERROR19(19, "Unpowered charging station"),
    ERROR20(20, "Unknown Error"),
    ERROR21(21, "Laser pressure sensor problem"),
    ERROR22(22, "Charge sensor problem"),
    ERROR23(23, "Dock problem"),
    ERROR24(24, "No-go zone or invisible wall detected"),
    ERROR254(254, "Bin full"),
    ERROR255(255, "Internal error"),
    UNKNOWN(-1, "Unknown Error");

    private final int id;
    private final String description;

    VacuumErrorType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static VacuumErrorType getType(int value) {
        for (VacuumErrorType st : VacuumErrorType.values()) {
            if (st.getId() == value) {
                return st;
            }
        }
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Error " + Integer.toString(id) + ": " + description;
    }
}
