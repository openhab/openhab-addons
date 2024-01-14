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
package org.openhab.binding.senechome.internal;

/**
 * Enum with available Senec specific wallbox states.
 *
 * @author Erwin Guib - Initial Contribution
 */
public enum SenecWallboxStatus {
    WAIT_FOR_EV(0xA1, "Waiting for EV"),
    EV_ASKING_CHARGE(0xB1, "EV asking for charge"),
    EV_CHARGE_PERMISSION(0xB2, "EV has charge permission"),
    EV_CHARGING(0xC2, "Charging"),
    EV_CHARGING_REDUCED_CURRENT_ERROR(0xC3, "Charging reduced current (error F16, F17)"),
    EV_CHARGING_REDUCED_CURRENT_IMBALANCE(0xC4, "Charging reduced current (imbalance F15)"),
    DISABLED(0xE0, "Wallbox disabled"),
    TEST_PRODUCTION(0xE1, "production test"),
    EVCC_PROGRAM(0xE2, "EVCC program mode"),
    BUS_IDLE(0xE3, "Bus idle"),
    UNEXPECTED_CLOSED_CONTACT(0xF1, "unexpected closed contact (welded)"),
    INTERNAL_ERROR(0xF2, "Internal error"),
    DC_RESIDUAL_CURRENT(0xF3, "DC residual current detected"),
    UPSTREAM_COM_TIMEOUT(0xF4, "Upstream communication timeout"),
    LOCK_SOCKET_FAILED(0xF5, "Lock of socket failed"),
    CS_OUT_OF_RANGE(0xF6, "CS out of range"),
    EV_HIGH_TEMP(0xF7, "State D requested by EV"),
    CP_OUT_OF_RANGE(0xF8, "CP out of range"),
    OVERCURRENT(0xF9, "Overcurrent detected"),
    TEMP_OUT_OF_LIMITS(0xFA, "Temperature outside limits"),
    UNEXPECTED_OPEN_CONTACT(0xFB, "unexpected opened contact"),
    RESERVED_1(0xFC, "Reserved State"),
    RESERVED_2(0xFD, "Reserved State"),
    UNKNOWN(-1, "UNKNOWN");

    private final int code;
    private final String description;

    SenecWallboxStatus(int index, String description) {
        this.code = index;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SenecWallboxStatus fromCode(int code) {
        for (SenecWallboxStatus state : SenecWallboxStatus.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return SenecWallboxStatus.UNKNOWN;
    }

    public static String descriptionFromCode(int code) {
        for (SenecWallboxStatus state : SenecWallboxStatus.values()) {
            if (state.code == code) {
                return state.description;
            }
        }
        return SenecWallboxStatus.UNKNOWN.description;
    }
}
