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
package org.openhab.binding.smartmeter.internal.helper;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public enum Baudrate {
    AUTO(-1) {
        @Override
        public String toString() {
            return name();
        }
    },
    _300(300),
    _600(600),
    _1200(1200),
    _1800(1800),
    _2400(2400),
    _4800(4800),
    _9600(9600),
    _19200(19200),
    _38400(38400);

    private int baudrate;

    private Baudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public static Baudrate fromBaudrate(int baudrate) {
        for (Baudrate baud : values()) {
            if (baud.getBaudrate() == baudrate) {
                return baud;
            }
        }
        return Baudrate._9600;
    }

    public static Baudrate fromString(String baudrate) {
        try {
            if (baudrate.equalsIgnoreCase(AUTO.name())) {
                return Baudrate.AUTO;
            }
            return valueOf("_" + baudrate.toUpperCase());
        } catch (Exception e) {
            return Baudrate.AUTO;
        }
    }

    @Override
    public String toString() {
        return getBaudrate() + "bd";
    }
}
