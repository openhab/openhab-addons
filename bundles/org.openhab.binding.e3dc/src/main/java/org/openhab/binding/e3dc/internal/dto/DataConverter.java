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
package org.openhab.binding.e3dc.internal.dto;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataConverter} Helper class to convert bytes from modbus into desired data format
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DataConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConverter.class);
    private static final long MAX_INT32 = new Long("4294967296");

    public static int getIntValue(byte[] bytes, int start) {
        return ((bytes[start] & 0xff) << 8) | (bytes[start + 1] & 0xff);
    }

    public static long getLongValue(byte[] bytes, int start) {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) + (bytes[i] & 0xff);
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
    public static long getInt32_swap(byte[] bytes, int startIndex) {
        // LOGGER.info("Convert {} {} {} {}", bytes[start], bytes[start + 1], bytes[start + 2], bytes[start + 3]);
        if (bytes.length - startIndex < 4) {
            return Long.MIN_VALUE;
        }
        long a = getIntValue(bytes, startIndex);
        long b = getIntValue(bytes, startIndex + 2);
        if (b < 32768) {
            return b * 65536 + a;
        } else {
            return (MAX_INT32 - b * 65536 - a) * -1;
        }
    }

    public static String getString(byte[] bArray, int i) {
        byte[] slice = Arrays.copyOfRange(bArray, i, i + 32);
        return new String(slice);
    }

    public static void logArray(byte[] bArray) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bArray.length; i++) {
            // logger.info("Byte {} is {}", i, bArray[i]);
            s.append(bArray[i] + " : ");
        }
        LOGGER.info("PowerBlock: {}", s.toString());
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
