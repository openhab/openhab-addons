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
package org.openhab.binding.epsonprojector.internal.enums;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Valid values for ColorMode.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public enum ColorMode {
    AUTO(0x00),
    SRGB(0x01),
    NORMAL(0x02),
    MEETING(0x03),
    PRESENTATION(0x04),
    CINEMANIGHT(0x05),
    DYNAMIC(0x06),
    NATURAL(0x07),
    SPORTS(0x08),
    HD(0x09),
    CUSTOM(0x10),
    BLACKBOARD(0x11),
    WHITEBOARD(0x12),
    THX(0x13),
    PHOTO(0x14),
    CINEMA(0x15),
    UNKNOWN16(0x16),
    CINEMA3D(0x17),
    DYNAMIC3D(0x18),
    THX3D(0x19),
    BWCINEMA(0x20),
    UNKNOWN21(0x21),
    DIGITALCINEMA(0x22),
    SILVER(0x0A),
    XVCOLOR(0x0B),
    LIVINGROOM(0x0C),
    DICOMSIM(0x0F),
    UNKNOWN(0xFF);

    private final int value;

    ColorMode(int value) {
        this.value = value;
    }

    public static ColorMode forValue(int value) {
        try {
            return Arrays.stream(values()).filter(e -> e.value == value).findFirst().get();
        } catch (NoSuchElementException e) {
            return UNKNOWN;
        }
    }

    public int toInt() {
        return value;
    }
}
