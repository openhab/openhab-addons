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
import java.util.BitSet;

import org.eclipse.californium.elements.util.StandardCharsets;

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
     * @param bytes
     * @param start
     * @return
     */
    public static int getUIntt16Value(ByteBuffer wrap) {
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
     * Conversion done according to E3DC Modbus Specification V1.7
     *
     * @param bytes - byte array with at least 4 bytes available from start
     * @param startIndex - start index for decoding
     * @return decoded long value, Long.MIN_VALUE otherwise
     */
    public static long getInt32Swap(ByteBuffer wrap) {
        long a = getUIntt16Value(wrap);
        long b = getUIntt16Value(wrap);
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
}
