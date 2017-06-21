/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * List of Errors
 * derived from vacuum_cleaner-EN.pdf
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum VacuumErrorType {

    ERROR00(0, "No error"),
    ERROR01(1, "Laser distance sensor error"),
    ERROR02(2, "Collision sensor error"),
    ERROR03(3, "Wheels on top of void, move robot"),
    ERROR04(4, "Clean hovering sensors, move robot"),
    ERROR05(5, "Clean main brush"),
    ERROR06(6, "Clean side brush"),
    ERROR07(7, "Main wheel stuck?"),
    ERROR08(8, "Device stuck, clean area"),
    ERROR09(9, "Dust collector missing"),
    ERROR010(10, "Clean filter"),
    ERROR011(11, "Stuck in magnetic barrier"),
    ERROR012(12, "Low battery"),
    ERROR013(13, "Charging fault"),
    ERROR014(14, "Battery fault"),
    ERROR015(15, "Wall sensors dirty, wipe them"),
    ERROR016(16, "Place me on flat surface"),
    ERROR017(17, "Side brushes problem, reboot me"),
    ERROR018(18, "Suction fan problem"),
    ERROR019(19, "Unpowered charging station"),
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
