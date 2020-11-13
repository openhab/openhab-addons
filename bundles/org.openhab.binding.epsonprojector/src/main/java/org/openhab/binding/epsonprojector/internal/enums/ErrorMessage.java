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
 * Messages for documented error codes.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public enum ErrorMessage {
    NO_ERROR(0, "No error"),
    ERROR1(1, "Fan error"),
    ERROR3(3, "Lamp failure at power on"),
    ERROR4(4, "High internal temperature error"),
    ERROR6(6, "Lamp error"),
    ERROR7(7, "Open Lamp cover door error"),
    ERROR8(8, "Cinema filter error"),
    ERROR9(9, "Electric dual-layered capacitor is disconnected"),
    ERROR10(10, "Auto iris error"),
    ERROR11(11, "Subsystem error"),
    ERROR12(12, "Low air flow error"),
    ERROR13(13, "Air filter air flow sensor error"),
    ERROR14(14, "Power supply unit error (ballast)"),
    ERROR15(15, "Shutter error"),
    ERROR16(16, "Cooling system error (peltier element)"),
    ERROR17(17, "Cooling system error (pump)"),
    ERROR18(18, "Static iris error"),
    ERROR19(19, "Power supply unit error (disagreement of ballast)"),
    ERROR20(20, "Exhaust shutter error"),
    ERROR21(21, "Obstacle detection error"),
    ERROR22(22, "IF board discernment error");

    private final int code;
    private final String message;

    ErrorMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static String forCode(int code) {
        try {
            return Arrays.stream(values()).filter(e -> e.code == code).findFirst().get().getMessage();
        } catch (NoSuchElementException e) {
            return "Unknown error code: " + code;
        }
    }
}
