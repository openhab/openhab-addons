/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.dto;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ValueBuffer;

/**
 * The {@link DataConverter} Helper class to convert bytes from modbus into desired data format
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DataConverter {

    /**
     * Get double value from 2 bytes with correction factor
     *
     * @param wrap
     * @return double
     */
    public static double getUDoubleValue(ValueBuffer wrap, double factor) {
        return round(wrap.getUInt16() * factor, 2);
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
