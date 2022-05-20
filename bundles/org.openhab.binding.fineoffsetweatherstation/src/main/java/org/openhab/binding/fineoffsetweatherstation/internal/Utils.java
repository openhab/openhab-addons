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
package org.openhab.binding.fineoffsetweatherstation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class Utils {

    public static String toHexString(byte[] hex, int length, String delimiter) {
        String[] hexadecimal = new String[length];
        for (int i = 0; i < length; i++) {
            hexadecimal[i] = String.format("%02X", hex[i]);
        }
        return String.join(delimiter, hexadecimal);
    }

    public static boolean validateChecksum(byte[] data, int sizeBytes) {
        int size;

        // general response
        // | 1 byte size | 2 byte size
        // | -----------------------------|--------------------
        // | 0 - 0xff - header | 0 - 0xff - header
        // | 1 - 0xff | 1 - 0xff
        // | 2 - command | 2 - command
        // | 3 - total size of response | 3 - size1
        // | 4-X - data | 4 - size2
        // | X+1 - checksum | 5-X - data
        // | | X+1 - checksum

        if (sizeBytes == 1) {
            size = Utils.toUInt8(data[3]);
        } else {
            size = toUInt16(data, 3);
        }

        byte checksum = sum(data, 2, size);
        return checksum == data[size + 1];
    }

    private static byte sum(byte[] data, int start, int end) {
        byte checksum = 0;
        for (var i = start; i <= end; i++) {
            checksum += data[i];
        }
        return checksum;
    }

    public static int toUInt8(byte data) {
        return Byte.toUnsignedInt(data);
    }

    public static int toInt16(byte[] array, int start) {
        int result = ((int) array[start]) << 24;
        result |= Utils.toUInt8(array[start + 1]) << 16;
        return result >> 16;
    }

    public static int toUInt16(byte[] array, int start) {
        return (Utils.toUInt8(array[start]) << 8 | Utils.toUInt8(array[start + 1]));
    }

    public static int toUInt32(byte[] array, int start) {
        return (Utils.toUInt8(array[start++]) << 24 | Utils.toUInt8(array[start++]) << 16
                | Utils.toUInt8(array[start++]) << 8 | Utils.toUInt8(array[start]));
    }

    public static long toUInt64(byte[] array, int start) {
        return ((long) Utils.toUInt8(array[start++]) << 24 | (long) toUInt8(array[start++]) << 16
                | (long) toUInt8(array[start++]) << 8 | Utils.toUInt8(array[start]));
    }
}
