/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    int code;

    SenecBatteryStatus(int index) {
        this.code = index;
    }

    public int getCode() {
        return code;
    }

    public static SenecBatteryStatus byCode(int code) {
        for (SenecBatteryStatus state : SenecBatteryStatus.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return SenecBatteryStatus.UNKNOWN;
    }
}
