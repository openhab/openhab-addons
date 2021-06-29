/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Define Thermostat control modes
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public enum ThermostatControlMode {
    AUTO(0x0),
    MANUAL(0x1),
    TEMPORARY(0x2),
    BOOST(0x3),
    UNKOWN(-1);

    private final int controlMode;

    ThermostatControlMode(int mode) {
        this.controlMode = mode;
    }

    ThermostatControlMode(byte mode) {
        this.controlMode = mode;
    }

    public byte toByte() {
        return (byte) controlMode;
    }

    public int toInt() {
        return controlMode;
    }

    static ThermostatControlMode fromString(String value) {
        switch (value) {
            case "AUTOMATIC":
                return AUTO;
            case "MANUAL":
                return MANUAL;
            case "VACATION":
                return TEMPORARY;
            case "BOOST":
                return BOOST;
            default:
                return UNKOWN;
        }
    }

    @Override
    public String toString() {
        switch (controlMode) {
            case 0x0:
                return "AUTOMATIC";
            case 0x1:
                return "MANUAL";
            case 0x2:
                return "VACATION";
            case 0x3:
                return "BOOST";
            default:
                return "UNKOWN";
        }
    }
}
