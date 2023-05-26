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
public enum ACOpMode {
    COOL(0),
    DRY(1),
    FAN(2),
    AI(3),
    HEAT(4),
    AIRCLEAN(5),
    ENSAV(8),
    OP_UNK(-1);

    private final int opMode;

    ACOpMode(int v) {
        this.opMode = v;
    }

    public static ACOpMode statusOf(int value) {
        switch ((int) value) {
            case 0:
                return COOL;
            case 1:
                return DRY;
            case 2:
                return FAN;
            case 3:
                return AI;
            case 4:
                return HEAT;
            case 5:
                return AIRCLEAN;
            case 8:
                return ENSAV;
            default:
                return OP_UNK;
        }
    }

    public int getValue() {
        return this.opMode;
    }

    /**
     * Value of command (not state, but command to change the state of device)
     * 
     * @return value of the command to reach the state
     */
    public int commandValue() {
        switch (this) {
            case COOL:
                return 0;// "@AC_MAIN_OPERATION_MODE_COOL_W"
            case DRY:
                return 1; // "@AC_MAIN_OPERATION_MODE_DRY_W"
            case FAN:
                return 2; // "@AC_MAIN_OPERATION_MODE_FAN_W"
            case AI:
                return 3; // "@AC_MAIN_OPERATION_MODE_AI_W"
            case HEAT:
                return 4; // "@AC_MAIN_OPERATION_MODE_HEAT_W"
            case AIRCLEAN:
                return 5; // "@AC_MAIN_OPERATION_MODE_AIRCLEAN_W"
            case ENSAV:
                return 8; // "AC_MAIN_OPERATION_MODE_ENERGY_SAVING_W"
            default:
                throw new IllegalArgumentException("Enum not accepted for command:" + this);
        }
    }
}
