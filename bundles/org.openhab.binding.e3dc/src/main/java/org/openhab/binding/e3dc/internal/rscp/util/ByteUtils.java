/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp.util;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * The {@link ByteUtils} provides support functions for handling byte arrays.
 *
 * @author Brendon Votteler - Initial Contribution
 */
public class ByteUtils {
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        return bytesFromOffsetToLong(bytes, 0);
    }

    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }

    public static byte[] shortToBytes(short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(0, x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        return bytesFromOffsetToInt(bytes, 0);
    }

    public static short bytesToShort(byte[] bytes) {
        return bytesFromOffsetToShort(bytes, 0);
    }

    public static long bytesFromOffsetToLong(byte[] bytes, int offset) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, offset, Long.BYTES);
        buffer.rewind();
        return buffer.getLong();
    }

    public static int bytesFromOffsetToInt(byte[] bytes, int offset) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, offset, Integer.BYTES);
        buffer.rewind();
        return buffer.getInt();
    }

    public static short bytesFromOffsetToShort(byte[] bytes, int offset) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes, offset, Short.BYTES);
        buffer.rewind();
        return buffer.getShort();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static int calculateCRC32Checksum(byte[] bytes, int start, int length) {
        if ((start + length) > bytes.length) {
            throw new IllegalArgumentException("Byte array too small for start or length.");
        }

        Checksum checksum = new CRC32();
        // fill with all until end of data part
        checksum.update(bytes, start, length);
        // get the current checksum value
        return (int) checksum.getValue();
    }

    public static byte[] reverseByteArray(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            newBytes[i] = bytes[bytes.length - 1 - i];
        }
        return newBytes;
    }

    public static int arrayPosition(byte[] haystack, byte[] needle) {
        if (haystack == null || needle == null) {
            return -1;
        }

        if (needle.length < 1 || needle.length > haystack.length) {
            return -1;
        }
        outer: for (int i = 0; i < haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }

        return -1;
    }

    public static byte[] truncateFirstNBytes(byte[] bytes, int n) {
        if (n < 1 || bytes == null || bytes.length < n) {
            return null;
        }

        byte[] truncatedBytes = new byte[bytes.length - n];
        System.arraycopy(bytes, n, truncatedBytes, 0, truncatedBytes.length);

        return truncatedBytes;
    }

    public static byte[] copyBytesIntoNewArray(byte[] src, int offset, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(src, offset, copy, 0, length);
        return copy;
    }
}
