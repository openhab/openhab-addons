/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * A relative humidity class that is used for converting from and to Plugwise protocol values.
 *
 * @author Wouter Born - Initial contribution
 */
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
