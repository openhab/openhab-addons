/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.handler;

/**
 * Enumeration containing the firmware types.
 *
 * @author Karel Goderis - Initial contribution
 */
public enum Firmware {
    F0(0, "BCU 1, BCU 2, BIM M113"),
    F1(1, "Unidirectional devices"),
    F3(3, "Property based device management"),
    F7(7, "BIM M112"),
    F8(8, "IR Decoder, TP1 legacy"),
    F9(9, "Repeater, Coupler");

    private int code;
    private String name;

    private Firmware(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String getName(int code) {
        for (Firmware c : Firmware.values()) {
            if (c.code == code) {
                return c.name;
            }
        }
        return null;
    }
}
