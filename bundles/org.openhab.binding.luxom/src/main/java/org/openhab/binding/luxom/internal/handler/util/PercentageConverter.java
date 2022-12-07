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
package org.openhab.binding.luxom.internal.handler.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * converts the hexadecimal string representation to an integer value between 0 - 100
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class PercentageConverter {
    /**
     * @param hexRepresentation
     * @return if hexRepresentation == null return -1, otherwise return percentage
     */
    public static int getPercentage(@Nullable String hexRepresentation) {
        if (hexRepresentation == null)
            return -1;
        int decimal = Integer.parseInt(hexRepresentation, 16);
        BigDecimal level = new BigDecimal(100 * decimal).divide(new BigDecimal(255), RoundingMode.FLOOR);
        return level.intValue();
    }

    public static String getHexRepresentation(int percentage) {
        BigDecimal decimal = new BigDecimal(255 * percentage).divide(new BigDecimal(100), RoundingMode.CEILING);
        return Integer.toHexString(decimal.intValue()).toUpperCase();
    }
}
