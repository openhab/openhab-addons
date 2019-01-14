/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
