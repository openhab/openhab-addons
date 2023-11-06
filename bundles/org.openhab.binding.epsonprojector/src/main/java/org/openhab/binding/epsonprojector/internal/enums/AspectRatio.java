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
 * Valid values for AspectRatio.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public enum AspectRatio {
    NORMAL(0x00),
    RATIO4X3(0x10),
    ZOOM4X3(0x12),
    RATIO16X9(0x20),
    UP16X9(0x21),
    DOWN16X9(0x22),
    AUTO(0x30),
    FULL(0x40),
    ZOOM(0x50),
    REAL(0x60),
    WIDE(0x70),
    ANAMORPHIC(0x80),
    SQUEEZE(0x90),
    UNKNOWN(0xFF);

    private final int value;

    AspectRatio(int value) {
        this.value = value;
    }

    public static AspectRatio forValue(int value) {
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
