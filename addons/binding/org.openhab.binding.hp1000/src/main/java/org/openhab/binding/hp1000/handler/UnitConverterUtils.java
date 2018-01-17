/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hp1000.handler;

/**
 * The {@link UnitConverterUtils} is a helper to convert units
 *
 * @author Daniel Bauer - Initial contribution
 */
final class UnitConverterUtils {

    public static double fahrenheitToCelius(double fahrenheit) {
        return ((fahrenheit - 32.0) * 5.0) / 9.0;
    }

    public static double inchesToMillimeters(double inches) {
        return inches * 25.4;
    }

    public static double inhgToHpa(double inHg) {
        return inHg * 33.86389;
    }

    public static double mphTokmh(double mph) {
        return mph * 1.609344;
    }

    private UnitConverterUtils() {
        // helper class
    }
}
