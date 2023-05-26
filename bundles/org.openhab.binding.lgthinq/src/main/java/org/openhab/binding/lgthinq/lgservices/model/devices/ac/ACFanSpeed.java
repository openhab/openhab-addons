/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ACCanonicalSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum ACFanSpeed {
    F1(2.0),
    F2(3.0),
    F3(4.0),
    F4(5.0),
    F5(6.0),
    F_AUTO(8.0),
    F_UNK(-1);

    private final double funStrength;

    ACFanSpeed(double v) {
        this.funStrength = v;
    }

    public static ACFanSpeed statusOf(double value) {
        switch ((int) value) {
            case 2:
                return F1;
            case 3:
                return F2;
            case 4:
                return F3;
            case 5:
                return F4;
            case 6:
                return F5;
            case 8:
                return F_AUTO;
            default:
                return F_UNK;
        }
    }

    /**
     * "0": "@AC_MAIN_WIND_STRENGTH_SLOW_W",
     * "1": "@AC_MAIN_WIND_STRENGTH_SLOW_LOW_W",
     * "2": "@AC_MAIN_WIND_STRENGTH_LOW_W",
     * "3": "@AC_MAIN_WIND_STRENGTH_LOW_MID_W",
     * "4": "@AC_MAIN_WIND_STRENGTH_MID_W",
     * "5": "@AC_MAIN_WIND_STRENGTH_MID_HIGH_W",
     * "6": "@AC_MAIN_WIND_STRENGTH_HIGH_W",
     * "7": "@AC_MAIN_WIND_STRENGTH_POWER_W",
     * "8": "@AC_MAIN_WIND_STRENGTH_NATURE_W",
     */
    /**
     * Value of command (not state, but command to change the state of device)
     *
     * @return value of the command to reach the state
     */
    public int commandValue() {
        switch (this) {
            case F1:
                return 2;
            case F2:
                return 3;
            case F3:
                return 4;
            case F4:
                return 5;
            case F5:
                return 6;
            case F_AUTO:
                return 8;
            default:
                throw new IllegalArgumentException("Enum not accepted for command:" + this);
        }
    }
}
