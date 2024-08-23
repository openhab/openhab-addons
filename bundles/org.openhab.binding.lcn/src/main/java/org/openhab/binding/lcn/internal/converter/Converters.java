/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Holds all Converter objects.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class Converters {
    public static final Converter TEMPERATURE;
    public static final Converter LIGHT;
    public static final Converter CO2;
    public static final Converter CURRENT;
    public static final Converter VOLTAGE;
    public static final Converter ANGLE;
    public static final Converter WINDSPEED;
    public static final Converter IDENTITY;

    static {
        TEMPERATURE = new ValueConverter(SIUnits.CELSIUS, n -> (n - 1000) / 10d, h -> Math.round(h * 10) + 1000);
        LIGHT = new ValueConverter(Units.LUX, Converters::lightToHumanReadable, Converters::lightToNative);
        CO2 = new ValueConverter(Units.PARTS_PER_MILLION, n -> (double) n, Math::round);
        CURRENT = new ValueConverter(Units.AMPERE, n -> n / 100d, h -> Math.round(h * 100));
        VOLTAGE = new ValueConverter(Units.VOLT, n -> n / 400d, h -> Math.round(h * 400));
        ANGLE = new ValueConverter(Units.DEGREE_ANGLE, n -> (n - 1000) / 10d, Converters::angleToNative);
        WINDSPEED = new ValueConverter(Units.METRE_PER_SECOND, n -> n / 10d, h -> Math.round(h * 10));
        IDENTITY = new ValueConverter(null, n -> (double) n, Math::round);
    }

    private static long lightToNative(double value) {
        return Math.round(Math.log(value) * 100);
    }

    private static double lightToHumanReadable(long value) {
        // Max. value hardware can deliver is 100klx. Apply hard limit, because higher native values lead to very big
        // lux values.
        if (value > lightToNative(100e3)) {
            return Double.NaN;
        }
        return Math.exp(value / 100d);
    }

    private static long angleToNative(double h) {
        return (Math.round(h * 10) + 1000);
    }
}
