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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Writer helpers for constructing DMAP tagged data.
 *
 * <p>
 * A DMAP tag consists of a 4-character ASCII name, a 4-byte big-endian length and the
 * payload. RAOP uses these helpers to build SET_PARAMETER metadata bodies.
 *
 * <p>
 * Note on {@link #stringTag(String, String)}: the length field is the code point count of
 * the string, not the payload's byte length, so it differs from the payload size for
 * non-ASCII strings.
 *
 * <p>
 * A minimal reader ({@link #parse(byte[])} and the {@code read*} helpers) is included for test
 * assertions only; production code never parses DMAP data here.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class DmapTags {

    private DmapTags() {
        // static utility class
    }

    /**
     * Creates a DMAP tag with uint8 data.
     *
     * @param name 4-character ASCII tag name
     * @param value value written as one big-endian byte (must fit in 0-255)
     * @return encoded tag bytes
     */
    public static byte[] uint8Tag(String name, long value) {
        return tag(name, uintBytes(value, 1));
    }

    /**
     * Creates a DMAP tag with uint16 data.
     *
     * @param name 4-character ASCII tag name
     * @param value value written as two big-endian bytes (must fit in 0-65535)
     * @return encoded tag bytes
     */
    public static byte[] uint16Tag(String name, long value) {
        return tag(name, uintBytes(value, 2));
    }

    /**
     * Creates a DMAP tag with uint32 data.
     *
     * @param name 4-character ASCII tag name
     * @param value value written as four big-endian bytes (must fit in 0-4294967295)
     * @return encoded tag bytes
     */
    public static byte[] uint32Tag(String name, long value) {
        return tag(name, uintBytes(value, 4));
    }

    /**
     * Creates a DMAP tag with uint64 data.
     *
     * @param name 4-character ASCII tag name
     * @param value value written as eight big-endian bytes (interpreted as unsigned)
     * @return encoded tag bytes
     */
    public static byte[] uint64Tag(String name, long value) {
        byte[] payload = new byte[8];
        for (int i = 7; i >= 0; i--) {
            payload[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return tag(name, payload);
    }

    /**
     * Creates a DMAP tag with boolean data (single byte 0x01 or 0x00, length 1).
     *
     * @param name 4-character ASCII tag name
     * @param value boolean value
     * @return encoded tag bytes
     */
    public static byte[] boolTag(String name, boolean value) {
        return tag(name, new byte[] { value ? (byte) 0x01 : (byte) 0x00 });
    }

    /**
     * Creates a DMAP tag with raw data.
     *
     * @param name 4-character ASCII tag name
     * @param value raw payload bytes
     * @return encoded tag bytes
     */
    public static byte[] rawTag(String name, byte[] value) {
        return tag(name, value);
    }

    /**
     * Creates a DMAP tag with string data.
     *
     * <p>
     * The length field is the code point count of the string, while the payload is the
     * UTF-8 encoding.
     *
     * @param name 4-character ASCII tag name
     * @param value string payload
     * @return encoded tag bytes
     */
    public static byte[] stringTag(String name, String value) {
        int length = value.codePointCount(0, value.length());
        byte[] payload = value.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(name.getBytes(StandardCharsets.UTF_8));
        out.writeBytes(uintBytes(length, 4));
        out.writeBytes(payload);
        return out.toByteArray();
    }

    /**
     * Creates a DMAP container tag (same encoding as a raw tag).
     *
     * @param name 4-character ASCII tag name
     * @param data already-encoded child tags
     * @return encoded tag bytes
     */
    public static byte[] containerTag(String name, byte[] data) {
        return rawTag(name, data); // Same as raw
    }

    private static byte[] tag(String name, byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(name.getBytes(StandardCharsets.UTF_8));
        out.writeBytes(uintBytes(payload.length, 4));
        out.writeBytes(payload);
        return out.toByteArray();
    }

    private static byte[] uintBytes(long value, int length) {
        long max = (1L << (length * 8)) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException("Value " + value + " does not fit in " + length + " byte(s)");
        }
        byte[] result = new byte[length];
        for (int i = length - 1; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return result;
    }

    // Minimal reader, for test assertions only

    /**
     * One parsed DMAP entry: a 4-character tag name and its raw payload bytes.
     *
     * @param name 4-character ASCII tag name
     * @param data raw payload bytes (parse recursively for containers)
     */
    public record Entry(String name, byte[] data) {

        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof Entry entry && name.equals(entry.name) && Arrays.equals(data, entry.data);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + Arrays.hashCode(data);
        }

        @Override
        public String toString() {
            return "Entry[name=" + name + ", data=" + Arrays.toString(data) + "]";
        }
    }

    /**
     * Walks a byte sequence of DMAP tags and returns the top-level entries. For test assertions
     * only.
     *
     * @param data encoded DMAP data
     * @return list of top-level entries in encounter order
     * @throws IllegalArgumentException if the data is truncated
     */
    public static List<Entry> parse(byte[] data) {
        List<Entry> entries = new ArrayList<>();
        int pos = 0;
        while (pos < data.length) {
            if (pos + 8 > data.length) {
                throw new IllegalArgumentException("Truncated tag header at offset " + pos);
            }
            String name = new String(data, pos, 4, StandardCharsets.US_ASCII);
            long length = readUint(Arrays.copyOfRange(data, pos + 4, pos + 8));
            int end = pos + 8 + (int) length;
            if (end > data.length) {
                throw new IllegalArgumentException("Truncated tag payload for " + name + " at offset " + pos);
            }
            entries.add(new Entry(name, Arrays.copyOfRange(data, pos + 8, end)));
            pos = end;
        }
        return entries;
    }

    /**
     * Returns the payload of the first entry with the given name, or {@code null} if absent. For
     * test assertions only.
     *
     * @param entries parsed entries
     * @param name tag name to look for
     * @return payload bytes or {@code null}
     */
    public static byte @Nullable [] first(List<Entry> entries, String name) {
        for (Entry entry : entries) {
            if (entry.name().equals(name)) {
                return entry.data();
            }
        }
        return null;
    }

    /**
     * Interprets payload bytes as a big-endian unsigned integer. For test assertions only.
     *
     * @param data payload bytes (at most 8)
     * @return the value
     */
    public static long readUint(byte[] data) {
        long value = 0;
        for (byte b : data) {
            value = (value << 8) | (b & 0xFF);
        }
        return value;
    }

    /**
     * Interprets payload bytes as a UTF-8 string. For test assertions only.
     *
     * @param data payload bytes
     * @return the decoded string
     */
    public static String readString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Interprets payload bytes as a boolean (value equals 1). For test assertions only.
     *
     * @param data payload bytes
     * @return {@code true} if the unsigned value equals 1
     */
    public static boolean readBool(byte[] data) {
        return readUint(data) == 1;
    }
}
