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
 * Valid values for GammaStep.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 */
public enum GammaStep {
    TONE1(0x00),
    TONE2(0x01),
    TONE3(0x02),
    TONE4(0x03),
    TONE5(0x04),
    TONE6(0x05),
    TONE7(0x06),
    TONE8(0x07),
    TONE9(0x08),
    ERROR(0xFF);

    private final int value;

    private GammaStep(int value) {
        this.value = value;
    }

    public static GammaStep forValue(int value) {
        return Arrays.stream(values()).filter(e -> e.value == value).findFirst().orElseThrow();
    }

    public int toInt() {
        return value;
    }
}
