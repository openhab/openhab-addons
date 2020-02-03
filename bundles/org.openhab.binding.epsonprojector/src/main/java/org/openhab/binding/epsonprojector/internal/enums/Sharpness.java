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
 * Valid values for Sharpness.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 */
public enum Sharpness {
    STANDARD(0x00),
    HIGHPASS(0x01),
    LOWPASS(0x02),
    HORIZONTAL(0x04),
    VERTICAL(0x05),
    ERROR(0xFF);

    private final int value;

    private Sharpness(int value) {
        this.value = value;
    }

    public static Sharpness forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.value == value).findFirst().orElseThrow();
    }

    public int toInt() {
        return value;
    }
}
