/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibberpulselocal.internal.sml;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Finds SML 1.04 transport frames in a byte array and validates them.
 *
 * A frame is {@code 1b1b1b1b 01010101}, the escaped message bytes, padding to a multiple of four bytes, then
 * {@code 1b1b1b1b 1a <padding count> <CRC low> <CRC high>}. The CRC is CRC-16/X-25 over everything from the first start
 * byte up to and including the padding count. Four {@code 1b} bytes inside the data are sent doubled.
 *
 * A frame that fails any check is dropped without a sound and the search continues behind its start, so a valid
 * frame after a corrupt one in the same payload is still found.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class SmlFrames {

    private static final int ESCAPE_LENGTH = 4;
    private static final byte ESCAPE = 0x1b;
    private static final byte[] START = { 0x1b, 0x1b, 0x1b, 0x1b, 0x01, 0x01, 0x01, 0x01 };
    private static final byte END_MARKER = 0x1a;
    /** End escape, marker, padding count, and two CRC bytes. */
    private static final int TRAILER_LENGTH = 8;

    private SmlFrames() {
    }

    /**
     * @param payload the bytes that contain zero or more frames
     * @param offset index of the first byte to scan
     * @return the message bytes of every valid frame, in order, with escapes resolved and padding removed
     */
    static List<byte[]> extract(byte[] payload, int offset) {
        List<byte[]> frames = new ArrayList<>();
        int from = offset;
        while (true) {
            int start = indexOfStart(payload, from);
            if (start < 0) {
                return frames;
            }
            int end = frameEnd(payload, start);
            if (end < 0) {
                from = start + 1;
                continue;
            }
            byte[] body = body(payload, start, end);
            if (body == null) {
                from = start + 1;
                continue;
            }
            frames.add(body);
            from = end + TRAILER_LENGTH;
        }
    }

    /**
     * @return the index of the end escape of the frame starting at {@code start}, or -1 if the frame is truncated or
     *         malformed
     */
    private static int frameEnd(byte[] data, int start) {
        int i = start + START.length;
        while (i + ESCAPE_LENGTH <= data.length) {
            if (!isEscape(data, i)) {
                i++;
                continue;
            }
            if (i + ESCAPE_LENGTH + 1 > data.length) {
                return -1;
            }
            if (isEscape(data, i + ESCAPE_LENGTH)) {
                i += 2 * ESCAPE_LENGTH; // doubled escape: four literal 1b bytes
            } else if (data[i + ESCAPE_LENGTH] == END_MARKER) {
                return i + TRAILER_LENGTH <= data.length ? i : -1;
            } else {
                return -1; // anything else behind an escape, for example the start of the next frame
            }
        }
        return -1;
    }

    /**
     * Checks padding and CRC of the frame and returns its unescaped message bytes.
     *
     * @return the bytes, or null if the frame is invalid
     */
    private static byte @Nullable [] body(byte[] data, int start, int end) {
        int padding = data[end + ESCAPE_LENGTH + 1] & 0xFF;
        int messageStart = start + START.length;
        if (padding > 3 || padding > end - messageStart) {
            return null;
        }
        int crc = crc16(data, start, end + ESCAPE_LENGTH + 2);
        int stored = (data[end + ESCAPE_LENGTH + 2] & 0xFF) | ((data[end + ESCAPE_LENGTH + 3] & 0xFF) << 8);
        if (crc != stored) {
            return null;
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream(end - messageStart);
        int limit = end - padding;
        int i = messageStart;
        while (i < limit) {
            if (i + 2 * ESCAPE_LENGTH <= limit && isEscape(data, i) && isEscape(data, i + ESCAPE_LENGTH)) {
                body.write(ESCAPE);
                body.write(ESCAPE);
                body.write(ESCAPE);
                body.write(ESCAPE);
                i += 2 * ESCAPE_LENGTH;
            } else {
                body.write(data[i++]);
            }
        }
        return body.toByteArray();
    }

    private static int indexOfStart(byte[] data, int from) {
        for (int i = Math.max(from, 0); i + START.length <= data.length; i++) {
            int k = 0;
            while (k < START.length && data[i + k] == START[k]) {
                k++;
            }
            if (k == START.length) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isEscape(byte[] data, int index) {
        return index + ESCAPE_LENGTH <= data.length && data[index] == ESCAPE && data[index + 1] == ESCAPE
                && data[index + 2] == ESCAPE && data[index + 3] == ESCAPE;
    }

    /**
     * CRC-16/X-25: reflected polynomial 0x8408, initial value 0xFFFF, final XOR 0xFFFF. The check value of the ASCII
     * string {@code 123456789} is 0x906E.
     *
     * @param to exclusive end index
     */
    static int crc16(byte[] data, int from, int to) {
        int crc = 0xFFFF;
        for (int i = from; i < to; i++) {
            crc ^= data[i] & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0x8408 : crc >>> 1;
            }
        }
        return crc ^ 0xFFFF;
    }
}
