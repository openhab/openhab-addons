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
package org.openhab.binding.miio.internal.robot;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of Errors
 * derived from vacuum_cleaner-EN.pdf and the Mi Home Roborock plugin
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
    ERROR25(25, "Camera error"),
    ERROR26(26, "Wall sensor error"),
    ERROR27(27, "Vibrating mop module jammed"),
    ERROR28(28, "Robot may be on a carpet"),
    ERROR29(29, "Unable to cross carpet or suspected pet waste found"),
    ERROR32(32, "No dust bag or filter installed in auto-empty dock"),
    ERROR34(34, "Clean the auto-empty dock bin"),
    ERROR35(35, "Auto-empty dock voltage error"),
    ERROR36(36, "Wash roller jammed"),
    ERROR37(37, "Wash roller not lowered properly"),
    ERROR38(38, "Check the clean water tank"),
    ERROR39(39, "Check the dirty water tank"),
    ERROR40(40, "Dock water filter not installed"),
    ERROR41(41, "Clean water tank empty"),
    ERROR42(42, "Check the dock water filter installation"),
    ERROR43(43, "Positioning button error"),
    ERROR44(44, "Check the dirty water tank cover"),
    ERROR45(45, "Wash roller jammed"),
    ERROR126(126, "Dock error"),
    ERROR127(127, "Dock error"),
    ERROR130(130, "Dock error"),
    ERROR131(131, "Dock error"),
    ERROR134(134, "Dock error"),
    ERROR135(135, "Wash roller jammed"),
    ERROR137(137, "Wash roller jammed"),
    ERROR138(138, "Main brush jammed"),
    ERROR140(140, "Main brush jammed"),
    ERROR141(141, "Wheel jammed"),
    ERROR143(143, "Wheel jammed"),
    ERROR145(145, "Dock error"),
    ERROR147(147, "Dock error"),
    ERROR148(148, "Dock error"),
    ERROR149(149, "Dock error"),
    ERROR150(150, "Dock error"),
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
        // The Mi Home app shows all other codes from 100 upwards as internal errors
        if (value >= 100 && value <= 255) {
            return ERROR255;
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
