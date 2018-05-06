/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer.internal;

/**
 * Utility class for different unit conversions.
 *
 * @author Gerhard Riegler - Contribution originating from OH1
 *
 */
public enum Unit {
    FAHRENHEIT,
    MPH,
    INCHES,
    BEAUFORT,
    KNOTS,
    MPS;

    /**
     * Parses the string and returns the Unit enum.
     */
    public static Unit parse(String name) {
        for (Unit unit : Unit.values()) {
            if (unit.name().equalsIgnoreCase(name)) {
                return unit;
            }
        }
        return null;
    }
}
