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
package org.openhab.binding.pentair.internal.parser;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PentairBasePacket } base class is meant to be extended for either a "standard" pentair packet or the
 * non-standard intellchlor packet
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairBasePacket {
    private static final char[] HEXARRAY = "0123456789ABCDEF".toCharArray();

    public byte[] buf;

    public PentairBasePacket(int l) {
        buf = new byte[l];
    }

    public PentairBasePacket(byte[] buf) {
        this(buf, buf.length);
    }

    public PentairBasePacket(byte[] buf, int l) {
        this.buf = new byte[l];
        System.arraycopy(buf, 0, this.buf, 0, l);
    }

    public int getLength() {
        return buf.length;
    }

    public int getByte(int n) {
        return (buf[n]) & 0xff;
    }

    public void setByte(int n, byte b) {
        buf[n] = b;
    }

    /**
     * Helper function to convert byte to hex representation
     *
     * @param b byte to re
     * @return 2 character hex string representing the byte
     */
    public static String byteToHex(int b) {
        char[] hexChars = new char[2];

        hexChars[0] = HEXARRAY[b >>> 4];
        hexChars[1] = HEXARRAY[b & 0x0F];

        return new String(hexChars);
    }

    /**
     * @param bytes array of bytes to convert to a hex string. Entire buf length is converted.
     * @return hex string
     */
    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, bytes.length);
    }

    /**
     * @param bytes array of bytes to convert to a hex string.
     * @param len Number of bytes to convert
     * @return hex string
     */
    public static String toHexString(byte[] bytes, int len) {
        char[] hexChars = new char[len * 3];
        for (int j = 0; j < len; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEXARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEXARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static String toHexString(ByteBuffer buf) {
        return toHexString(buf.array(), buf.limit());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toHexString(buf, getLength());
    }
}
