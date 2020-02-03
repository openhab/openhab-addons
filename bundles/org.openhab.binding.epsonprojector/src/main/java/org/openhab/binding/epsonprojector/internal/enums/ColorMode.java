/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal.enums;

import java.util.Arrays;

/**
 * Valid values for ColorMode.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 */
public enum ColorMode {
    CINEMANIGHT(0x05),
    DYNAMIC(0x06),
    NATURAL(0x07),
    HD(0x09),
    SILVER(0x0A),
    XVCOLOR(0x0B),
    LIVINGROOM(0x0C),
    THX(0x13),
    CINEMA(0x15),
    CINEMA3D(0x17),
    DYNAMIC3D(0x18),
    THX3D(0x19),
    BWCINEMA(0x20),
    ERROR(0xFF);

    private final int value;

    private ColorMode(int value) {
        this.value = value;
    }

    public static ColorMode forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.value == value).findFirst().orElseThrow();
    }

    public int toInt() {
        return value;
    }
}
