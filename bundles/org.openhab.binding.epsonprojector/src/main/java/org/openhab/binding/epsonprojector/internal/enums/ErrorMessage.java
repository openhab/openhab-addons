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
 * Messages for documented error codes.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Yannick Schaus - Refactoring
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public enum ErrorMessage {
    NO_ERROR(0, "00 : No error"),
    ERROR1(1, "01 : Fan error"),
    ERROR3(3, "03 : Lamp failure at power on"),
    ERROR4(4, "04 : High internal temperature error"),
    ERROR6(6, "06 : Lamp error"),
    ERROR7(7, "07 : Open Lamp cover door error"),
    ERROR8(8, "08 : Cinema filter error"),
    ERROR9(9, "09 : Electric dual-layered capacitor is disconnected"),
    ERROR10(10, "0A : Auto iris error"),
    ERROR11(11, "0B : Subsystem error"),
    ERROR12(12, "0C : Low air flow error"),
    ERROR13(13, "0D : Air filter air flow sensor error"),
    ERROR14(14, "0E : Power supply unit error (ballast)"),
    ERROR15(15, "0F : Shutter error"),
    ERROR16(16, "10 : Cooling system error (peltier element)"),
    ERROR17(17, "11 : Cooling system error (pump)"),
    ERROR18(18, "12 : Static iris error"),
    ERROR19(19, "13 : Power supply unit error (disagreement of ballast)"),
    ERROR20(20, "14 : Exhaust shutter error"),
    ERROR21(21, "15 : Obstacle detection error"),
    ERROR22(22, "16 : IF board discernment error"),
    ERROR23(23, "17 : Communication error of \"Stack projection function\""),
    ERROR24(24, "18 : I2C error");

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
            // for example, will display 10 as '0A' to match format of error codes in the epson documentation
            return "Unknown error code (hex): "
                    + String.format("%2s", Integer.toHexString(code)).replace(' ', '0').toUpperCase();
        }
    }
}
