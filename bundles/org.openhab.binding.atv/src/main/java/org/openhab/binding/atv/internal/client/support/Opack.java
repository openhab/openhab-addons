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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Support for Apple's OPACK serialization format (used by the Companion protocol).
 *
 * <p>
 * Notes:
 * <ul>
 * <li>Absolute time (0x06) is not fully implemented: it unpacks to an {@link OpackTime} value
 * wrapper holding the raw timestamp, but packing an {@link Instant} or {@link OpackTime}
 * raises an error.</li>
 * <li>The pack implementation does not implement UID referencing beyond the automatic
 * back-reference (pointer) deduplication table.</li>
 * <li>Likely other cases missing.</li>
 * </ul>
 *
 * <p>
 * <b>Object model.</b> {@link #pack(Object)} accepts: {@code null}, {@link Boolean},
 * {@link UUID}, integral {@link Number}s ({@link Byte}/{@link Short}/{@link Integer}/
 * {@link Long}), {@link SizedLong} (an integer that remembers its encoded width),
 * {@link Float}/{@link Double} (both packed as float64), {@link String}, {@code byte[]},
 * {@link List} and {@link Map} (use {@link LinkedHashMap} to control entry order — encoding
 * order follows iteration order). {@link #unpack(byte[])} produces the same model; multi-byte
 * integers (tags 0x30-0x33) unpack to {@link SizedLong} so they re-encode with the same width.
 *
 * <p>
 * <b>Back-references.</b> The packer keeps a table of the encoded byte strings of every value
 * whose encoding is longer than one byte (in encounter order) and replaces any repeated encoding
 * with a pointer (0xA0-0xC0 inline index, 0xC1-0xC4 length-prefixed index). The unpacker keeps
 * the mirror table of decoded values, skipping single-byte types (booleans, null, small
 * integers) and containers, and skipping values already present under value-equality semantics
 * (numeric cross-type equality, bytes content equality). The packer and unpacker tables must
 * stay in lockstep — drift here changes payload sizes and breaks interop with real devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Opack {

    private Opack() {
    }

    /**
     * An integer that preserves the byte width (1, 2, 4 or 8) it was — or should be — encoded
     * with.
     *
     * @param value the integer value (treat as unsigned for size 8)
     * @param size the encoded width in bytes: normally 1, 2, 4 or 8. Other sizes are
     *            tolerated and re-encode as 8 bytes (size 0 behaves as no hint).
     */
    public record SizedLong(long value, int size) {
    }

    /**
     * Value wrapper for OPACK absolute time (tag 0x06). The timestamp is only parsed as a
     * little-endian integer; {@link #pack(Object)} throws {@link UnsupportedOperationException}
     * for it since packing is not implemented.
     *
     * @param value raw absolute time value (unsigned 64-bit, little-endian on the wire)
     */
    public record OpackTime(long value) {
    }

    /**
     * Result of {@link #unpack(byte[])}: the decoded value and any bytes remaining after it.
     *
     * <p>
     * Note: record equality compares {@code remaining} by reference; use the accessors.
     */
    public record UnpackResult(Object value, byte[] remaining) {
    }

    /** Wrapper giving {@code byte[]} content-based equality for use as a hash map key. */
    private record ByteArrayKey(byte[] bytes) {
        @Override
        public boolean equals(@Nullable Object other) {
            return other instanceof ByteArrayKey key && Arrays.equals(bytes, key.bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }
    }

    // Packing

    /**
     * Packs a data structure with OPACK and returns the raw bytes.
     *
     * @throws IllegalArgumentException for unsupported types or out-of-range integers
     * @throws UnsupportedOperationException for absolute time values
     */
    public static byte[] pack(@Nullable Object data) {
        return pack(data, new HashMap<>());
    }

    /**
     * {@code objectList} maps the encoded byte string of every previously seen multi-byte
     * object to its back-reference index (only unique entries are tracked).
     */
    private static byte[] pack(@Nullable Object data, Map<ByteArrayKey, Integer> objectList) {
        byte[] packed;
        if (data == null) {
            packed = new byte[] { 0x04 };
        } else if (data instanceof Boolean bool) {
            packed = new byte[] { (byte) (bool ? 1 : 2) };
        } else if (data instanceof UUID uuid) {
            packed = new byte[17];
            packed[0] = 0x05;
            writeBigEndian(packed, 1, uuid.getMostSignificantBits());
            writeBigEndian(packed, 9, uuid.getLeastSignificantBits());
        } else if (data instanceof Instant || data instanceof OpackTime) {
            throw new UnsupportedOperationException("absolute time");
        } else if (data instanceof SizedLong sized) {
            packed = packLong(sized.value(), sized.size());
        } else if (data instanceof Byte || data instanceof Short || data instanceof Integer || data instanceof Long) {
            packed = packLong(((Number) data).longValue(), 0);
        } else if (data instanceof Float || data instanceof Double) {
            // Every float is packed as float64 (0x36).
            long bits = Double.doubleToLongBits(((Number) data).doubleValue());
            packed = new byte[9];
            packed[0] = 0x36;
            writeLittleEndian(packed, 1, bits, 8);
        } else if (data instanceof String string) {
            packed = packString(string);
        } else if (data instanceof byte[] bytes) {
            packed = packBytes(bytes);
        } else if (data instanceof List<?> list) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(0xD0 + Math.min(list.size(), 0xF));
            for (Object item : list) {
                out.writeBytes(pack(item, objectList));
            }
            if (list.size() >= 0xF) {
                out.write(0x03);
            }
            packed = out.toByteArray();
        } else if (data instanceof Map<?, ?> map) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(0xE0 + Math.min(map.size(), 0xF));
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.writeBytes(pack(entry.getKey(), objectList));
                out.writeBytes(pack(entry.getValue(), objectList));
            }
            if (map.size() >= 0xF) {
                out.write(0x03);
            }
            packed = out.toByteArray();
        } else {
            throw new IllegalArgumentException("Unsupported OPACK type: " + data.getClass());
        }

        // Reuse if already in the object list, otherwise add it (only encodings longer than
        // one byte are tracked, and only when not already present).
        Integer objectIndex = objectList.get(new ByteArrayKey(packed));
        if (objectIndex != null) {
            return pointerBytes(objectIndex);
        }
        if (packed.length > 1) {
            objectList.put(new ByteArrayKey(packed), objectList.size());
        }
        return packed;
    }

    /**
     * Packs an integer. {@code sizeHint} is 0 for plain integers or 1/2/4/8 for
     * {@link SizedLong} values.
     */
    private static byte[] packLong(long value, int sizeHint) {
        if (value < 0x28 && sizeHint == 0) {
            if (value + 8 < 0) {
                throw new IllegalArgumentException("Cannot pack integer: " + value);
            }
            return new byte[] { (byte) (value + 8) };
        }
        if ((sizeHint == 0 && fitsUnsigned(value, 1)) || sizeHint == 1) {
            return sizedIntBytes(0x30, value, 1);
        }
        if ((sizeHint == 0 && fitsUnsigned(value, 2)) || sizeHint == 2) {
            return sizedIntBytes(0x31, value, 2);
        }
        if ((sizeHint == 0 && fitsUnsigned(value, 4)) || sizeHint == 4) {
            return sizedIntBytes(0x32, value, 4);
        }
        // A Java long always fits in 8 bytes treated as unsigned, so no range check is needed.
        return sizedIntBytes(0x33, value, 8);
    }

    /** Encodes {@code tag} plus {@code size} little-endian bytes, with a range check. */
    private static byte[] sizedIntBytes(int tag, long value, int size) {
        if (size != 8 && !fitsUnsigned(value, size)) {
            throw new IllegalArgumentException("Integer " + value + " does not fit in " + size + " byte(s)");
        }
        byte[] result = new byte[1 + size];
        result[0] = (byte) tag;
        writeLittleEndian(result, 1, value, size);
        return result;
    }

    private static boolean fitsUnsigned(long value, int size) {
        return value >= 0 && (size == 8 || value <= ((1L << (size * 8)) - 1));
    }

    private static byte[] packString(String data) {
        byte[] encoded = data.getBytes(StandardCharsets.UTF_8);
        if (encoded.length <= 0x20) {
            return withHeader(new byte[] { (byte) (0x40 + encoded.length) }, encoded);
        } else if (encoded.length <= 0xFF) {
            return withLengthHeader(0x61, encoded, 1);
        } else if (encoded.length <= 0xFFFF) {
            return withLengthHeader(0x62, encoded, 2);
        } else if (encoded.length <= 0xFFFFFF) {
            return withLengthHeader(0x63, encoded, 3);
        } else {
            // A Java array length always fits in 4 bytes, so no larger tag is needed.
            return withLengthHeader(0x64, encoded, 4);
        }
    }

    private static byte[] packBytes(byte[] data) {
        if (data.length <= 0x20) {
            return withHeader(new byte[] { (byte) (0x70 + data.length) }, data);
        } else if (data.length <= 0xFF) {
            return withLengthHeader(0x91, data, 1);
        } else if (data.length <= 0xFFFF) {
            return withLengthHeader(0x92, data, 2);
        } else {
            // A Java array length always fits in 4 bytes, so the 8-byte-length tag (0x94)
            // is unreachable here.
            return withLengthHeader(0x93, data, 4);
        }
    }

    private static byte[] withHeader(byte[] header, byte[] payload) {
        byte[] result = Arrays.copyOf(header, header.length + payload.length);
        System.arraycopy(payload, 0, result, header.length, payload.length);
        return result;
    }

    private static byte[] withLengthHeader(int tag, byte[] payload, int lengthBytes) {
        byte[] header = new byte[1 + lengthBytes];
        header[0] = (byte) tag;
        writeLittleEndian(header, 1, payload.length, lengthBytes);
        return withHeader(header, payload);
    }

    /** Emits a back-reference to {@code objectIndex}. */
    private static byte[] pointerBytes(int objectIndex) {
        if (objectIndex < 0x21) {
            return new byte[] { (byte) (0xA0 + objectIndex) };
        } else if (objectIndex <= 0xFF) {
            return new byte[] { (byte) 0xC1, (byte) objectIndex };
        } else if (objectIndex <= 0xFFFF) {
            byte[] result = new byte[3];
            result[0] = (byte) 0xC2;
            writeLittleEndian(result, 1, objectIndex, 2);
            return result;
        } else {
            // 0xC3 (4 bytes); 0xC4 (8 bytes) is unreachable since indexes are ints.
            byte[] result = new byte[5];
            result[0] = (byte) 0xC3;
            writeLittleEndian(result, 1, objectIndex, 4);
            return result;
        }
    }

    private static void writeLittleEndian(byte[] target, int offset, long value, int size) {
        for (int i = 0; i < size; i++) {
            target[offset + i] = (byte) (value >>> (8 * i));
        }
    }

    private static void writeBigEndian(byte[] target, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            target[offset + i] = (byte) (value >>> (8 * (7 - i)));
        }
    }

    // Unpacking

    /**
     * Unpacks raw OPACK data into Java objects, returning the decoded value and the bytes
     * remaining after it.
     *
     * @throws IllegalArgumentException for unknown tags or truncated input
     */
    public static UnpackResult unpack(byte[] data) {
        Unpacker unpacker = new Unpacker(data);
        Object value = unpacker.unpackObject();
        if (value == null) {
            throw new IllegalArgumentException("OPACK top-level value is null");
        }
        return new UnpackResult(value, Arrays.copyOfRange(data, unpacker.position, data.length));
    }

    /** Decodes OPACK data using a cursor instead of slicing the array on each step. */
    private static final class Unpacker {
        private final byte[] data;
        private int position;
        private final List<@Nullable Object> objectList = new ArrayList<>();

        private Unpacker(byte[] data) {
            this.data = data;
        }

        private @Nullable Object unpackObject() {
            int tag = require(1)[position] & 0xFF;
            position++;
            Object value;
            boolean addToObjectList = true;
            if (tag == 0x01) {
                value = Boolean.TRUE;
                addToObjectList = false;
            } else if (tag == 0x02) {
                value = Boolean.FALSE;
                addToObjectList = false;
            } else if (tag == 0x04) {
                value = null;
                addToObjectList = false;
            } else if (tag == 0x05) {
                byte[] raw = take(16);
                value = new UUID(readBigEndian(raw, 0), readBigEndian(raw, 8));
            } else if (tag == 0x06) {
                // Only the raw timestamp is parsed; see OpackTime.
                value = new OpackTime(readLittleEndian(8));
            } else if (tag >= 0x08 && tag <= 0x2F) {
                value = (long) (tag - 8);
                addToObjectList = false;
            } else if (tag == 0x35) {
                value = (double) Float.intBitsToFloat((int) readLittleEndian(4));
            } else if (tag == 0x36) {
                value = Double.longBitsToDouble(readLittleEndian(8));
            } else if ((tag & 0xF0) == 0x30) {
                int noofBytes = 1 << (tag & 0xF);
                value = new SizedLong(readLittleEndian(noofBytes), noofBytes);
            } else if (tag >= 0x40 && tag <= 0x60) {
                value = new String(take(tag - 0x40), StandardCharsets.UTF_8);
            } else if (tag > 0x60 && tag <= 0x64) {
                int length = (int) readLittleEndian(tag & 0xF);
                value = new String(take(length), StandardCharsets.UTF_8);
            } else if (tag >= 0x70 && tag <= 0x90) {
                value = take(tag - 0x70);
            } else if (tag >= 0x91 && tag <= 0x94) {
                int length = (int) readLittleEndian(1 << ((tag & 0xF) - 1));
                value = take(length);
            } else if ((tag & 0xF0) == 0xD0) {
                int count = tag & 0xF;
                List<@Nullable Object> output = new ArrayList<>();
                if (count == 0xF) { // Endless list
                    while ((require(1)[position] & 0xFF) != 0x03) {
                        output.add(unpackObject());
                    }
                    position++;
                } else {
                    for (int i = 0; i < count; i++) {
                        output.add(unpackObject());
                    }
                }
                value = output;
                addToObjectList = false;
            } else if ((tag & 0xE0) == 0xE0) {
                int count = tag & 0xF;
                Map<@Nullable Object, @Nullable Object> output = new LinkedHashMap<>();
                if (count == 0xF) { // Endless dict
                    while ((require(1)[position] & 0xFF) != 0x03) {
                        Object key = unpackObject();
                        output.put(key, unpackObject());
                    }
                    position++;
                } else {
                    for (int i = 0; i < count; i++) {
                        Object key = unpackObject();
                        output.put(key, unpackObject());
                    }
                }
                value = output;
                addToObjectList = false;
            } else if (tag >= 0xA0 && tag <= 0xC0) {
                value = objectList.get(tag - 0xA0);
            } else if (tag >= 0xC1 && tag <= 0xC4) {
                int uid = (int) readLittleEndian(tag - 0xC0);
                value = objectList.get(uid);
            } else {
                throw new IllegalArgumentException("Unsupported OPACK tag: 0x" + Integer.toHexString(tag));
            }

            // Skip adding a value that is already present, using value-equality (see
            // pythonEquals below), matching the packer's dedup table.
            if (addToObjectList && !containsPythonEqual(objectList, value)) {
                objectList.add(value);
            }
            return value;
        }

        private byte[] require(int count) {
            if (position + count > data.length) {
                throw new IllegalArgumentException("Not enough OPACK data: need " + count + " byte(s) at offset "
                        + position + ", have " + (data.length - position));
            }
            return data;
        }

        private byte[] take(int count) {
            require(count);
            byte[] result = Arrays.copyOfRange(data, position, position + count);
            position += count;
            return result;
        }

        private long readLittleEndian(int count) {
            require(count);
            long value = 0;
            for (int i = 0; i < count && i < 8; i++) {
                value |= (data[position + i] & 0xFFL) << (8 * i);
            }
            position += count;
            return value;
        }

        private static long readBigEndian(byte[] source, int offset) {
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (source[offset + i] & 0xFFL);
            }
            return value;
        }
    }

    // Value-equality semantics for the unpacker reference table

    private static boolean containsPythonEqual(List<@Nullable Object> list, @Nullable Object value) {
        for (Object element : list) {
            if (pythonEquals(element, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Value-equality for the types the unpacker records: numbers compare by value across
     * types (a {@link SizedLong} of any width, an {@link OpackTime} and a plain integer of
     * equal value are all equal), and byte arrays compare by content.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private static boolean pythonEquals(@Nullable Object left, @Nullable Object right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof byte[] leftBytes && right instanceof byte[] rightBytes) {
            return Arrays.equals(leftBytes, rightBytes);
        }
        if (isNumeric(left) && isNumeric(right)) {
            if (isFloatingPoint(left) || isFloatingPoint(right)) {
                return numericDouble(left) == numericDouble(right);
            }
            return numericLong(left) == numericLong(right);
        }
        return left.equals(right);
    }

    private static boolean isNumeric(Object value) {
        return value instanceof Number || value instanceof SizedLong || value instanceof OpackTime;
    }

    private static boolean isFloatingPoint(Object value) {
        return value instanceof Double || value instanceof Float;
    }

    private static long numericLong(Object value) {
        if (value instanceof SizedLong sized) {
            return sized.value();
        }
        if (value instanceof OpackTime time) {
            return time.value();
        }
        return ((Number) value).longValue();
    }

    private static double numericDouble(Object value) {
        if (value instanceof SizedLong sized) {
            return sized.value();
        }
        if (value instanceof OpackTime time) {
            return time.value();
        }
        return ((Number) value).doubleValue();
    }
}
