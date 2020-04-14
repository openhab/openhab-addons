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
 * Valid values for Source.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 */
public enum Source {
    COMPONENT(0x14),
    PC_DSUB(0x20),
    HDMI1(0x30),
    HDMI2(0xA0),
    VIDEO(0x41),
    SVIDEO(0x42),
    ERROR(0xFF);

    private final int value;

    private Source(int value) {
        this.value = value;
    }

    public static Source forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.value == value).findFirst().get();
    }

    public int toInt() {
        return value;
    }
}
