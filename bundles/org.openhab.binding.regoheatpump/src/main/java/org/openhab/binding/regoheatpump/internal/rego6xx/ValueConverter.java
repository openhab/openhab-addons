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
package org.openhab.binding.regoheatpump.internal.rego6xx;

/**
 * The {@link ValueConverter} is responsible for converting various rego 6xx specific data types.
 *
 * @author Boris Krivonog - Initial contribution
 */
class ValueConverter {
    public static byte[] shortToSevenBitFormat(short value) {
        byte b1 = (byte) ((value & 0xC000) >> 14);
        byte b2 = (byte) ((value & 0x3F80) >> 7);
        byte b3 = (byte) (value & 0x007F);

        return new byte[] { b1, b2, b3 };
    }

    public static short sevenBitFormatToShort(byte[] buffer, int offset) {
        return (short) (buffer[offset] << 14 | buffer[offset + 1] << 7 | buffer[offset + 2]);
    }

    public static byte arrayToByte(byte[] buffer, int offset) {
        return (byte) (buffer[offset] << 4 | buffer[offset + 1]);
    }

    public static String stringFromBytes(byte[] buffer, int offset, int charCount) {
        StringBuilder builder = new StringBuilder(charCount);

        int length = offset + charCount * 2;
        for (int i = offset; i < length; i += 2) {
            builder.append((char) arrayToByte(buffer, i));
        }

        return builder.toString();
    }
}
