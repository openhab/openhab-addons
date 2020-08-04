/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * The {@link DataConverter} Helper class to convert bytes from modbus into desired data format
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DataConverter {
    private static final long MAX_INT32 = (long) Math.pow(2, Integer.SIZE);

    /**
     * Get unit16 value from 2 bytes
     *
     * @param wrap
     * @return
     */
    public static int getUInt16Value(ByteBuffer wrap) {
        return ((wrap.get() & 0xff) << 8) | wrap.get() & 0xff;
    }

    public static long getLongValue(ByteBuffer wrap) {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) + (wrap.get() & 0xff);
        }
        return value;
    }

    /**
     * Get double value from 2 bytes with correction factor
     *
     * @param wrap
     * @return
     */
    public static double getUDoubleValue(ByteBuffer wrap, double factor) {
        Integer d = getUInt16Value(wrap);
        double df = d.doubleValue() * factor;
        return round(df, 2);
    }

    /**
     * Conversion done according to E3DC Modbus Specification V1.7
     *
     * @param wrap
     * @return decoded long value, Long.MIN_VALUE otherwise
     */
    public static long getInt32Swap(ByteBuffer wrap) {
        long a = getUInt16Value(wrap);
        long b = getUInt16Value(wrap);
        if (b < 32768) {
            return b * 65536 + a;
        } else {
            return (MAX_INT32 - b * 65536 - a) * -1;
        }
    }

    public static String getString(byte[] bArray) {
        return new String(bArray, StandardCharsets.US_ASCII).trim();
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
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
