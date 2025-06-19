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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ACTargetTmp}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum ACTargetTmp {
    _17(17.0),
    _18(18.0),
    _19(19.0),
    _20(20.0),
    _21(21.0),
    _22(22.0),
    _23(23.0),
    _24(24.0),
    _25(25.0),
    _26(26.0),
    _27(27.0),
    _28(28.0),
    _29(29.0),
    _30(30.0),
    UNK(-1);

    private final double targetTmp;

    ACTargetTmp(double v) {
        this.targetTmp = v;
    }

    public static ACTargetTmp statusOf(double value) {
        switch ((int) value) {
            case 17:
                return _17;
            case 18:
                return _18;
            case 19:
                return _19;
            case 20:
                return _20;
            case 21:
                return _21;
            case 22:
                return _22;
            case 23:
                return _23;
            case 24:
                return _24;
            case 25:
                return _25;
            case 26:
                return _26;
            case 27:
                return _27;
            case 28:
                return _28;
            case 29:
                return _29;
            case 30:
                return _30;
            default:
                return UNK;
        }
    }

    public double getValue() {
        return this.targetTmp;
    }

    /**
     * Value of command (not state, but command to change the state of device)
     *
     * @return value of the command to reach the state
     */
    public int commandValue() {
        return (int) this.targetTmp;
    }
}
