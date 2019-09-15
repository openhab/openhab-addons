/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * The {@link SenecBatteryStatus} class defines available Senec specific
 * battery states.
 *
 * @author Steven.Schwarznau - Initial contribution
 *
 */
public enum SenecBatteryStatus {

    ERROR(1),
    MAINTENANCE_CHARGING(5),
    MAINTENANCE_REQUIRED(7),
    FULL_CHARGING(10),
    BALANCE_CHARGING(11),
    CHARGED(13),
    CHARGING(14),
    BATTERY_LOW(15),
    DISCHARGING(16),
    DISCHARGING_AND_SOLAR(17),
    DISCHARGING_AND_GRID(18),
    PASSIVE(19),
    TURNED_OFF(20),
    SELF_CONSUMPTION(21),
    RESTARTING(22),
    UNKNOWN(-1);

    private int code;

    SenecBatteryStatus(int index) {
        this.code = index;
    }

    public int getCode() {
        return code;
    }

    public static SenecBatteryStatus fromCode(int code) {
        for (SenecBatteryStatus state : SenecBatteryStatus.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return SenecBatteryStatus.UNKNOWN;
    }
}
