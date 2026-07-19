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
package org.openhab.binding.atv.internal.client.support;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reading and writing of Google protobuf variants (varints).
 *
 * <p>
 * A variant is an unsigned integer encoded as little-endian 7-bit groups, where the high
 * bit of each byte signals that more bytes follow. Values are treated as unsigned 64-bit
 * quantities.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Variant {

    private Variant() {
    }

    /**
     * Result of reading a variant.
     *
     * @param value the decoded (unsigned) value
     * @param consumed number of bytes consumed from the input
     */
    public record Result(long value, int consumed) {
    }

    /**
     * Read and parse a binary protobuf variant value from the start of a buffer.
     *
     * @param data buffer to read from
     * @return decoded value and number of bytes consumed
     * @throws IllegalArgumentException if the buffer ends in the middle of a variant
     */
    public static Result readVariant(byte[] data) {
        return readVariant(data, 0);
    }

    /**
     * Read and parse a binary protobuf variant value.
     *
     * @param data buffer to read from
     * @param offset offset of the first variant byte
     * @return decoded value and number of bytes consumed (starting at {@code offset})
     * @throws IllegalArgumentException if the buffer ends in the middle of a variant
     */
    public static Result readVariant(byte[] data, int offset) {
        long result = 0;
        int cnt = 0;
        for (int i = offset; i < data.length; i++) {
            int b = data[i] & 0xFF;
            result |= (long) (b & 0x7F) << (7 * cnt);
            cnt++;
            if ((b & 0x80) == 0) {
                return new Result(result, cnt);
            }
        }
        throw new IllegalArgumentException("invalid variant");
    }

    /**
     * Stream-friendly variant read used by MRP framing.
     *
     * <p>
     * Attempts to read a variant starting at the buffer's current position. On success
     * the buffer position is advanced past the variant and the result is returned. If
     * the buffer does not (yet) contain a complete variant, {@link Optional#empty()} is
     * returned and the buffer position is left untouched so more data can be appended.
     *
     * @param buffer buffer positioned at the first variant byte
     * @return decoded value and consumed byte count, or empty if more data is needed
     */
    public static Optional<Result> tryReadVariant(ByteBuffer buffer) {
        long result = 0;
        int cnt = 0;
        int pos = buffer.position();
        while (pos + cnt < buffer.limit()) {
            int b = buffer.get(pos + cnt) & 0xFF;
            result |= (long) (b & 0x7F) << (7 * cnt);
            cnt++;
            if ((b & 0x80) == 0) {
                buffer.position(pos + cnt);
                return Optional.of(new Result(result, cnt));
            }
        }
        return Optional.empty();
    }

    /**
     * Convert an integer to a protobuf variant binary buffer.
     *
     * @param number value to encode, interpreted as unsigned 64-bit
     * @return encoded variant bytes
     */
    public static byte[] writeVariant(long number) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long n = number;
        while (Long.compareUnsigned(n, 0x80) >= 0) {
            out.write((int) ((n & 0x7F) | 0x80));
            n >>>= 7;
        }
        out.write((int) n);
        return out.toByteArray();
    }
}
