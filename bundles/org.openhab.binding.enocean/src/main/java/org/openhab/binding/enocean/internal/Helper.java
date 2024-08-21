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
package org.openhab.binding.enocean.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class Helper {

    public static byte[] concatAll(byte[] a, byte[]... rest) {
        if (rest.length == 0) {
            return a;
        }

        int totalLength = a.length;
        for (byte[] b : rest) {
            if (b != null) {
                totalLength += b.length;
            }
        }

        byte[] result = Arrays.copyOf(a, totalLength);
        int offset = a.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result != null ? result : new byte[0];
    }

    public static int tryParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Scales the given value
     * 
     * @param value Value to be scaled
     * @param minUnscaled Value for min value in unscaled dimension
     * @param maxUnscaled Value for max value in unscaled dimension
     * @param minValue
     * @param maxValue
     * @return
     */
    public static double scaleValue(double value, double minUnscaled, double maxUnscaled, double minValue,
            double maxValue) {
        double range = maxValue - minValue;
        double unscaledRange = maxUnscaled - minUnscaled;

        if (maxUnscaled > minUnscaled) {
            return minValue + (value * range / unscaledRange);
        } else {
            return maxValue + (value * range / unscaledRange);
        }
    }
}
