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
 * Container class for Fan Mode enums related to Roborock Vacuums
 *
 * @author Paul Smedley - Initial contribution
 *
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
    ERROR27(27, "Vibrarise jammed"),
    ERROR28(28, "Robot on carpet"),
    ERROR29(29, "Filter Blocked"),
    ERROR30(30, "Invisible wall detected"),
    ERROR31(31, "Cannot cross carpet"),
    ERROR32(33, "Internal error"),
    ERROR34(34, "Clean auto-empty dock"),
    ERROR35(35, "Auto empty dock voltage error"),
    ERROR36(36, "Wash roller may be jammed"),
    ERROR37(37, "Wash roller not lowered properly"),
    ERROR38(38, "Check the clean water tank"),
    ERROR39(39, "Check the dirty water tank"),
    ERROR40(40, "Reinstall the water filter"),
    ERROR41(41, "Clean water tank empty"),
    ERROR42(42, "Check that the water filter has been correctly installed"),
    ERROR43(43, "Positioning button error"),
    ERROR44(44, "Clean the dock water filter"),
    ERROR45(45, "Wash roller may be jammed"),
    ERROR48(49, "Up water exception"),
    ERROR49(49, "Drain water exception"),
    ERROR51(51, "Unit temperature protection"),
    ERROR52(52, "Clean carousel exception"),
    ERROR53(53, "Clean carousel water full"),
    ERROR54(54, "Water carriage drop"),
    ERROR55(55, "Check clean carouse"),
    ERROR56(56, "Audio error"),
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
        for (VacuumErrorType m : VacuumErrorType.values()) {
            if (m.getId() == value) {
                return m;
            }
        }

        // Default to unknown
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }
}
