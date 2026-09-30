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

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of Errors
 * derived from vacuum_cleaner-EN.pdf and the Mi Home Roborock plugin
 * <p>
 * The Mi Home plugin shares one error table between all models. Code 29 is the only code that has a model specific
 * meaning: the Roborock S7 family (T7S, S7) reports a carpet it cannot cross, other models report suspected pet waste.
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
    ERROR29(29, "Suspected pet waste found"),
    ERROR29_CARPET(29, "Unable to cross carpet"),
    ERROR32(32, "No dust bag or filter installed in auto-empty dock"),
    ERROR33(33, "Auto-empty dock fan error"),
    ERROR34(34, "Clean the auto-empty dock bin"),
    ERROR35(35, "Auto-empty dock voltage error"),
    ERROR36(36, "Wash roller jammed"),
    ERROR37(37, "Wash roller not lowered properly"),
    ERROR38(38, "Check the clean water tank"),
    ERROR39(39, "Check the dirty water tank"),
    ERROR40(40, "Water filter not installed"),
    ERROR41(41, "Clean water tank empty"),
    ERROR42(42, "Check the dock water filter installation"),
    ERROR43(43, "Positioning button error"),
    ERROR44(44, "Check the dirty water tank cover"),
    ERROR45(45, "Wash roller jammed"),
    ERROR126(126, "Dock fan error"),
    ERROR127(127, "Dock IIC error"),
    ERROR130(130, "No data from clean water tank peristaltic pump ODO"),
    ERROR131(131, "Over-current of clean water tank peristaltic pump"),
    ERROR134(134, "Air pump switch error"),
    ERROR135(135, "Wash roller locked rotor"),
    ERROR137(137, "Wash roller motor temperature too high"),
    ERROR138(138, "Main brush locked rotor"),
    ERROR140(140, "Main brush motor temperature too high"),
    ERROR141(141, "Left wheel motor temperature too high"),
    ERROR143(143, "Right wheel motor temperature too high"),
    ERROR145(145, "Dirty water pump error"),
    ERROR147(147, "Maintenance brush positioning error - left"),
    ERROR148(148, "Maintenance brush positioning error - middle"),
    ERROR149(149, "Maintenance brush positioning error - right"),
    ERROR150(150, "Solenoid valve error"),
    ERROR254(254, "Bin full"),
    ERROR255(255, "Internal error"),
    UNKNOWN(-1, "Unknown Error");

    private static final Pattern CARPET_ERROR_MODELS = Pattern.compile("roborock\\.vacuum\\.a1[45](v[2-5])?");

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

    /**
     * Get the error type taking the model specific meaning of the error code into account
     *
     * @param value the error code reported by the vacuum
     * @param model the model id of the vacuum, e.g. roborock.vacuum.a15
     * @return the error type
     */
    public static VacuumErrorType getType(int value, String model) {
        if (value == ERROR29_CARPET.getId() && CARPET_ERROR_MODELS.matcher(model).matches()) {
            return ERROR29_CARPET;
        }
        return getType(value);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Error " + Integer.toString(id) + ": " + description;
    }
}
