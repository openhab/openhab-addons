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
package org.openhab.binding.sensorcommunity.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NumberUtils} class provides helpers for converting Numbers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NumberUtils {
    public static final double UNDEF = Double.NaN;

    public static double round(Object o, int places) {
        double value = convert(o);

        // for negative places return plain number
        if (places < 0) {
            return value;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double convert(Object o) {
        // ensure value not null
        double value = UNDEF;
        if (o instanceof Number number) {
            value = number.doubleValue();
        } else if (o instanceof String) {
            value = Double.parseDouble(o.toString());
        }
        return value;
    }
}
