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
 * Valid values for AspectRatio.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 */
public enum AspectRatio {
    NORMAL(0x00),
    AUTO(0x30),
    FULL(0x40),
    ZOOM(0x50),
    WIDE(0x70),
    ANAMORPHIC(0x80),
    SQUEEZE(0x90),
    ERROR(0xFF);

    private final int value;

    private AspectRatio(int value) {
        this.value = value;
    }

    public static AspectRatio forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.value == value).findFirst().get();
    }

    public int toInt() {
        return value;
    }
}
