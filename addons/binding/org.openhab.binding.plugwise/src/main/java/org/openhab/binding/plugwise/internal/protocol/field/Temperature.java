/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * A temperature (Celsius) class that is used for converting from and to Plugwise protocol values.
 *
 * @author Wouter Born - Initial contribution
 */
public class Temperature {

    private static final String EMPTY_VALUE = "FFFF";

    private double value;

    public Temperature(double value) {
        this.value = value;
    }

    public Temperature(String hexValue) {
        if (EMPTY_VALUE.equals(hexValue)) {
            value = Double.MIN_VALUE;
        } else {
            value = 175.72 * (Integer.parseInt(hexValue, 16) / 65536.0) - 46.85;
        }
    }

    public double getValue() {
        return value;
    }

    public String toHex() {
        return String.format("%04X", Math.round((value + 46.85) / 175.72 * 65536.0));
    }

    @Override
    public String toString() {
        return String.format("%.3f\u00B0C", value);
    }

}
