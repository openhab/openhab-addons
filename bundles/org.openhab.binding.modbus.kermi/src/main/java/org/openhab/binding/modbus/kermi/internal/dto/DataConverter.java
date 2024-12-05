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
package org.openhab.binding.modbus.kermi.internal.dto;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ValueBuffer;

/**
 * The {@link DataConverter} Helper class to convert bytes from modbus into desired data format
 *
 * @author Bernd Weymann - Initial contribution
 * @author Kai Neuhaus - used for Modbus.Kermi Binding
 */
@NonNullByDefault
public class DataConverter {

    /**
     * Retrieves a double value from the first two bytes of the provided {@link ValueBuffer} using the given correction
     * factor.
     *
     * @param wrap The {@link ValueBuffer} from which to extract the bytes.
     * @param factor The correction factor to apply to the extracted value.
     * @return The calculated double value.
     */
    public static double getUDoubleValue(ValueBuffer wrap, double factor) {
        return round(wrap.getUInt16() * factor, 2);
    }

    /**
     * Retrieves a signed double value from the first two bytes of the provided {@link ValueBuffer} using the given
     * correction factor.
     *
     * @param wrap The {@link ValueBuffer} from which to extract the bytes. This {@link ValueBuffer} should contain at
     *            least two bytes.
     * @param factor The correction factor to apply to the extracted value. This factor is used to adjust the calculated
     *            value.
     * @return The calculated signed double value. The value is obtained by extracting the first two bytes from the
     *         {@link ValueBuffer},
     *         converting them to a signed 16-bit integer, multiplying it by the correction factor, and rounding the
     *         result to two decimal places.
     */
    public static double getSDoubleValue(ValueBuffer wrap, double factor) {
        return round(wrap.getSInt16() * factor, 2);
    }

    public static String getString(byte[] bArray) {
        return ModbusBitUtilities.extractStringFromBytes(bArray, 0, bArray.length, StandardCharsets.US_ASCII).trim();
    }

    public static int toInt(BitSet bitSet) {
        int intValue = 0;
        for (int bit = 0; bit < bitSet.length(); bit++) {
            if (bitSet.get(bit)) {
                intValue |= (1 << bit);
            }
        }
        return intValue;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        long tmp = Math.round(value * factor);
        return (double) tmp / factor;
    }
}
