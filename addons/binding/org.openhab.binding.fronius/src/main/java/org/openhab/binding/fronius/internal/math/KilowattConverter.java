/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.math;

/**
 * Helper class for unit conversions
 *
 * @author Thomas Rokohl
 *
 */
public class KilowattConverter {

    public static double getConvertFactor(String fromUnit, String toUnit) {
        String adjustedFromUnit = fromUnit.replace("Wh", "");
        String adjustedtoUnit = toUnit.replace("Wh", "");
        return SiPrefixFactors.getFactorToBaseUnit(adjustedFromUnit) * 1
                / SiPrefixFactors.getFactorToBaseUnit(adjustedtoUnit);
    }

    public static double convertTo(double value, String fromUnit, String toUnit) {
        return value * getConvertFactor(fromUnit, toUnit);
    }
}
