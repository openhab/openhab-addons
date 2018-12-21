/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.utility;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.LoggerFactory;

/**
 * Various utility functions that are used when interacting with or parsing data
 * pertaining to the KLF200 unit.
 *
 * @author MFK - Initial Contribution
 */
public class KLFUtils {

    /** The Constant SLIP_BYTE_END. */
    private static final byte SLIP_BYTE_END = (byte) 0xC0;

    /** The Constant SLIP_BYTE_ESC. */
    private static final byte SLIP_BYTE_ESC = (byte) 0xDB;

    /** The Constant SLIP_BYTE_ESC_END. */
    private static final byte SLIP_BYTE_ESC_END = (byte) 0xDC;

    /** The Constant SLIP_BYTE_ESC_ESC. */
    private static final byte SLIP_BYTE_ESC_ESC = (byte) 0xDD;

    /**
     * Extracts the command code (two bytes) from a KLF data packet, and
     * converts to a BigEngian unsigned 16-bit integer. Note that this method
     * assumes that the KLF data packet has not not yet been 'Slip RFC1055'
     * encoded in the case of request data and that in the case of response
     * data, that it has already been 'Slip RFC1055'.
     *
     * @param data
     *                 Data array that is to be sent to or has been received from a
     *                 KLF200 unit. The data must not be 'Slip RFC1055' encoded.
     * @return BigEngian unsigned 16-bit integer representing the command code
     *         in the data array supplied.
     */
    public static short decodeKLFCommand(byte[] data) {
        return (short) (((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
    }

    /**
     * Extract a UTF 8 string from an array of bytes. Typically this is used
     * when interpreting a response payload from a KLF200 unit.
     *
     * @param data
     *                  A byte data array that contains a UTF-8 string at a specific
     *                  location.
     * @param start
     *                  The location within the array of the first byte of the UTF-8
     *                  string.
     * @param end
     *                  The end location of the last byte of the UTF-8 string.
     * @return A string as extracted from the input data array. If bytes within
     *         the range supplied are zero, these are assumed to be padding and
     *         discarded.
     */
    public static String extractUTF8String(byte[] data, int start, int end) {
        int len = end - start;
        byte[] relevant = new byte[len];
        System.arraycopy(data, start, relevant, 0, len);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < relevant.length; ++i) {
            if (relevant[i] != 0) {
                baos.write(relevant[i]);
            }
        }
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * Format a byte array into a printable HEX string. Typically used when
     * logging byte arrays.
     *
     * @param b
     *              The byte array to be formatted into a printable string
     * @return A formatted and printable representation of the input byte array
     *         as HEX values
     */
    public static String formatBytes(byte b) {
        String ret = Integer.toHexString(b & 0xFF);
        if (1 == ret.length()) {
            ret = "0" + ret;
        }
        return ret;
    }

    /**
     * Format a byte into a printable HEX string. Typically used when logging
     * bytes.
     *
     * @param bb
     *               the bb
     * @return A formatted and printable representation of the input byte as a
     *         HEX String.
     */
    public static String formatBytes(byte bb[]) {
        String ret = "{";
        for (byte b : bb) {
            ret += formatBytes(b) + ",";
        }
        if (",".equals(ret.substring(ret.length() - 1))) {
            ret = ret.substring(0, ret.length() - 1);
        }
        ret += "}";
        return ret;
    }

    /**
     * Decode the input byte array using the Slip RFC 1055 protocol.
     *
     * @param packet
     *                   A SLIP RFC 1055 encoded data array
     * @return Decoded byte array or null in the event of an error with
     *         decoding.
     *
     * @author Guenther Schreiner
     */
    public static byte[] slipRFC1055decode(byte[] packet) {
        if (packet.length < 3) {
            LoggerFactory.getLogger(KLFUtils.class).error("Attempt to decode a packet that is too short: {} -> {}",
                    packet.length, formatBytes(packet));
            return null;
        }
        if (packet[0] != SLIP_BYTE_END) {
            LoggerFactory.getLogger(KLFUtils.class).error(
                    "Attempt to decode a packet with an unexpectid character at position 0: {}", formatBytes(packet));
            return null;
        }
        ;
        if (packet[packet.length - 1] != SLIP_BYTE_END) {
            LoggerFactory.getLogger(KLFUtils.class).error(
                    "Attempt to decode a packet with an unexpectid character at position {} (end): {}",
                    packet.length - 1, formatBytes(packet));
            return null;
        }
        ;
        int additional = -2;
        for (int i = 0; i < packet.length; i++) {
            if (packet[i] == SLIP_BYTE_ESC) {
                additional--;
            }
        }
        byte[] payload = new byte[packet.length + additional];

        int packetIndex = 0;
        for (int i = 0; i < packet.length; i++) {
            if ((i == 0) || (i == packet.length - 1)) {
                continue;
            }
            if ((packet[i] == SLIP_BYTE_ESC) && (packet[i + 1] == SLIP_BYTE_ESC_ESC)) {
                payload[packetIndex++] = SLIP_BYTE_ESC;
                i++;
            } else if ((packet[i] == SLIP_BYTE_ESC) && (packet[i + 1] == SLIP_BYTE_ESC_END)) {
                payload[packetIndex++] = SLIP_BYTE_END;
                i++;
            } else {
                payload[packetIndex++] = packet[i];
            }
        }
        return payload;
    }

    /**
     * Encode the input byte array using the Slip RFC 1055 protocol.
     *
     * @param payload
     *                    the payload
     * @return A SLIP RFC 1055 encoded data array or null in the event of an
     *         error.
     *
     * @author Guenther Schreiner
     */
    public static byte[] slipRFC1055encode(byte[] payload) {
        int additional = 2;
        for (byte b : payload) {
            if ((b == SLIP_BYTE_ESC) || (b == SLIP_BYTE_END)) {
                additional++;
            }
        }
        byte[] packet = new byte[payload.length + additional];
        int packetIndex = 0;
        packet[packetIndex++] = SLIP_BYTE_END;

        for (byte b : payload) {
            if (b == SLIP_BYTE_ESC) {
                packet[packetIndex++] = SLIP_BYTE_ESC;
                packet[packetIndex++] = SLIP_BYTE_ESC_ESC;
            } else if (b == SLIP_BYTE_END) {
                packet[packetIndex++] = SLIP_BYTE_ESC;
                packet[packetIndex++] = SLIP_BYTE_ESC_END;
            } else {
                packet[packetIndex++] = b;
            }
        }
        packet[packetIndex++] = SLIP_BYTE_END;
        assert (packetIndex == packet.length);
        return packet;
    }

    /**
     * Convert a byte to an int.
     *
     * @param one
     *                The byte to be extracted
     * @return int representation
     */
    public static int extractOneByte(byte one) {
        return (one & 0xff);
    }

    /**
     * Convert two bytes into an int.
     *
     * @param one
     *                First byte
     * @param two
     *                Second byte
     * @return int representation.
     */
    public static int extractTwoBytes(byte one, byte two) {
        return 0x00 << 24 | 0x00 << 16 | (one & 0xff) << 8 | (two & 0xff);
    }

    /**
     * Extract four bytes.
     *
     * @param one
     *                  the one
     * @param two
     *                  the two
     * @param three
     *                  the three
     * @param four
     *                  the four
     * @return the int representation of the 4 bytes
     */
    public static int extractFourBytes(byte one, byte two, byte three, byte four) {
        return one << 24 | (two & 0xff) << 16 | (three & 0xff) << 8 | (four & 0xff);
    }

    /**
     * Extract unsigned int 32.
     *
     * @param one
     *                  the one
     * @param two
     *                  the two
     * @param three
     *                  the three
     * @param four
     *                  the four
     * @return the long
     */
    public static long extractUnsignedInt32(byte one, byte two, byte three, byte four) {
        ByteBuffer b = ByteBuffer.wrap(new byte[] { one, two, three, four });
        return b.getInt();
    }

    /**
     * Converts a long value to an unsigned byte array
     * 
     * @param value long value
     * @return Array of 4 bytes representing the long
     */
    public static byte[] longToBytes(long value) {
        byte[] data = new byte[4];
        data[3] = (byte) value;
        data[2] = (byte) (value >>> 8);
        data[1] = (byte) (value >>> 16);
        data[0] = (byte) (value >>> 32);
        return data;
    }
}
