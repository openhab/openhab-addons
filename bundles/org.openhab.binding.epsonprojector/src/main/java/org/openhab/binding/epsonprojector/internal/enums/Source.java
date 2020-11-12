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
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Valid values for Source.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public enum Source {
    UNKNOWN(0x00),
    COMPONENT(0x14),
    PC_DSUB(0x20),
    HDMI1(0x30),
    HDMI2(0xA0),
    VIDEO(0x41),
    SVIDEO(0x42),
    ERROR(0xFF);

    private final int value;

    Source(int value) {
        this.value = value;
    }

    public static Source forValue(int value) {
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
