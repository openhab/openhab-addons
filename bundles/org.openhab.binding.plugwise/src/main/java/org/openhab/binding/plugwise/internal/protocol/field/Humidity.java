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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A relative humidity class that is used for converting from and to Plugwise protocol values.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class Humidity {

    private static final String EMPTY_VALUE = "FFFF";
    private static final double MAX_HEX_VALUE = 65536;
    private static final double MULTIPLIER = 125;
    private static final double OFFSET = 6;

    private final double value;

    public Humidity(double value) {
        this.value = value;
    }

    public Humidity(String hexValue) {
        if (EMPTY_VALUE.equals(hexValue)) {
            value = Double.MIN_VALUE;
        } else {
            value = MULTIPLIER * (Integer.parseInt(hexValue, 16) / MAX_HEX_VALUE) - OFFSET;
        }
    }

    public double getValue() {
        return value;
    }

    public String toHex() {
        return String.format("%04X", Math.round((value + OFFSET) / MULTIPLIER * MAX_HEX_VALUE));
    }

    @Override
    public String toString() {
        return String.format("%.3f%%", value);
    }
}
