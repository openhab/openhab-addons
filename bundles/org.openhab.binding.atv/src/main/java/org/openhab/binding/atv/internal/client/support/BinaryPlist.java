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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.InvalidResponseError;

/**
 * Reader and writer for Apple binary property lists ({@code bplist00}).
 *
 * <p>
 * Used for AirPlay legacy pairing, {@code /play} payloads, data-channel messages and
 * NSKeyedArchiver payloads. The underlying format is documented in Apple's
 * {@code CF-744.18/CFBinaryPList.c}.
 *
 * <p>
 * Java object model:
 * <ul>
 * <li>dict &harr; {@link Map}&lt;String, Object&gt; (parsed as {@link LinkedHashMap},
 * preserving order)</li>
 * <li>array &harr; {@link List}&lt;Object&gt;</li>
 * <li>string &harr; {@link String} (ASCII or UTF-16BE on the wire)</li>
 * <li>data &harr; {@code byte[]}</li>
 * <li>integer &harr; {@link Long} (any {@code Number} except {@code Double}/
 * {@code Float} is written as an integer)</li>
 * <li>real &harr; {@link Double} (always written as a 64-bit double)</li>
 * <li>boolean &harr; {@link Boolean}</li>
 * <li>date &harr; {@link Instant} (stored as seconds since 2001-01-01T00:00:00Z,
 * the Core Foundation epoch)</li>
 * <li>UID &harr; {@link Uid}</li>
 * <li>null &harr; {@code null}</li>
 * </ul>
 *
 * <p>
 * The writer emits a flat object table without deduplication (one entry per
 * occurrence), integers in the smallest of 1/2/4/8 bytes (the 8-byte form being
 * signed), and non-ASCII strings as UTF-16BE — all encodings byte-compatible with
 * {@code plistlib.dumps(..., fmt=FMT_BINARY, sort_keys=False)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class BinaryPlist {

    /** Magic bytes at the start of every binary plist. */
    private static final byte[] MAGIC = "bplist00".getBytes(StandardCharsets.US_ASCII);

    /** Seconds between the Unix epoch (1970-01-01) and the CF epoch (2001-01-01). */
    private static final long CF_EPOCH_OFFSET_SECONDS = 978307200L;

    /** Trailer length in bytes. */
    private static final int TRAILER_SIZE = 32;

    private BinaryPlist() {
    }

    /**
     * Parses a binary property list.
     *
     * @param data the raw {@code bplist00} bytes
     * @return the top-level object (see class javadoc for the type mapping)
     * @throws InvalidResponseError if the data is not a valid binary plist
     */
    public static Object parse(byte[] data) {
        try {
            return new Parser(data).parse();
        } catch (InvalidResponseError e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidResponseError("Invalid binary plist", e);
        }
    }

    /**
     * Serializes an object graph to a binary property list.
     *
     * @param value the top-level object (see class javadoc for supported types)
     * @return the serialized {@code bplist00} bytes
     * @throws IllegalArgumentException if the graph contains an unsupported type or
     *             a non-string dictionary key
     */
    public static byte[] dump(Object value) {
        return new Writer().write(value);
    }

    /** Parser state. */
    private static final class Parser {

        /** Sentinel marking a not-yet-parsed object table slot. */
        private static final Object UNDEFINED = new Object();

        private final byte[] data;
        private long[] offsets = new long[0];
        private Object[] objects = new Object[0];
        private int refSize;
        private int pos;

        Parser(byte[] data) {
            this.data = data;
        }

        Object parse() {
            if (data.length < MAGIC.length + TRAILER_SIZE + 1) {
                throw new InvalidResponseError("Binary plist too short: " + data.length + " bytes");
            }
            for (int i = 0; i < MAGIC.length; i++) {
                if (data[i] != MAGIC[i]) {
                    throw new InvalidResponseError("Not a binary plist (bad magic)");
                }
            }

            // Trailer: 5 unused bytes, sort version, offset int size, object ref size,
            // number of objects, top object, offset table offset (all big-endian).
            int trailer = data.length - TRAILER_SIZE;
            int offsetSize = data[trailer + 6] & 0xff;
            refSize = data[trailer + 7] & 0xff;
            long numObjects = readLong(trailer + 8);
            long topObject = readLong(trailer + 16);
            long offsetTableOffset = readLong(trailer + 24);

            if (numObjects <= 0 || numObjects > Integer.MAX_VALUE || offsetSize <= 0 || refSize <= 0
                    || offsetTableOffset < MAGIC.length
                    || offsetTableOffset + numObjects * offsetSize > data.length - TRAILER_SIZE) {
                throw new InvalidResponseError("Invalid binary plist trailer");
            }

            int count = (int) numObjects;
            offsets = new long[count];
            for (int i = 0; i < count; i++) {
                offsets[i] = readUnsigned((int) offsetTableOffset + (long) i * offsetSize, offsetSize);
            }
            objects = new Object[count];
            java.util.Arrays.fill(objects, UNDEFINED);
            Object top = readObject(checkRef(topObject));
            if (top == null) {
                throw new InvalidResponseError("Binary plist top object is null");
            }
            return top;
        }

        private int checkRef(long ref) {
            if (ref < 0 || ref >= objects.length) {
                throw new InvalidResponseError("Object reference out of range: " + ref);
            }
            return (int) ref;
        }

        private long readLong(int at) {
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result = (result << 8) | (data[at + i] & 0xffL);
            }
            return result;
        }

        private long readUnsigned(long at, int size) {
            if (size <= 0 || size > 8 || at < 0 || at + size > data.length) {
                throw new InvalidResponseError("Invalid integer field in binary plist");
            }
            long result = 0;
            for (int i = 0; i < size; i++) {
                result = (result << 8) | (data[(int) at + i] & 0xffL);
            }
            return result;
        }

        /** Reads the extended size for data/string/array/dict markers. */
        private int getSize(int tokenL) {
            if (tokenL == 0xF) {
                int m = data[pos++] & 0x3;
                int s = 1 << m;
                long size = readUnsigned(pos, s);
                pos += s;
                if (size < 0 || size > Integer.MAX_VALUE) {
                    throw new InvalidResponseError("Size too large in binary plist: " + size);
                }
                return (int) size;
            }
            return tokenL;
        }

        private byte[] readBytes(int count) {
            if (count < 0 || pos + count > data.length) {
                throw new InvalidResponseError("Truncated binary plist object");
            }
            byte[] result = new byte[count];
            System.arraycopy(data, pos, result, 0, count);
            pos += count;
            return result;
        }

        private int[] readRefs(int count) {
            int[] refs = new int[count];
            for (int i = 0; i < count; i++) {
                refs[i] = checkRef(readUnsigned(pos, refSize));
                pos += refSize;
            }
            return refs;
        }

        private @Nullable Object readObject(int ref) {
            Object cached = objects[ref];
            if (cached != UNDEFINED) {
                return cached;
            }

            long offset = offsets[ref];
            if (offset < MAGIC.length || offset >= data.length - TRAILER_SIZE) {
                throw new InvalidResponseError("Object offset out of range: " + offset);
            }
            pos = (int) offset;
            int token = data[pos++] & 0xff;
            int tokenH = token & 0xF0;
            int tokenL = token & 0x0F;
            Object result;

            if (token == 0x00) { // null
                result = null;
            } else if (token == 0x08) { // false
                result = Boolean.FALSE;
            } else if (token == 0x09) { // true
                result = Boolean.TRUE;
            } else if (token == 0x0f) { // fill byte, plistlib returns b''
                result = new byte[0];
            } else if (tokenH == 0x10) { // int, 2^tokenL bytes; 8+ byte forms are signed
                int size = 1 << tokenL;
                if (size > 8) {
                    throw new InvalidResponseError("Integers larger than 8 bytes are not supported");
                }
                // 1/2/4 byte forms are unsigned (zero-extended below), the 8 byte
                // form is signed two's complement — both fall out of the same shift.
                byte[] raw = readBytes(size);
                long v = 0;
                for (byte b : raw) {
                    v = (v << 8) | (b & 0xffL);
                }
                result = v;
            } else if (token == 0x22) { // 32-bit real
                byte[] raw = readBytes(4);
                int bits = (int) ((raw[0] & 0xffL) << 24 | (raw[1] & 0xffL) << 16 | (raw[2] & 0xffL) << 8
                        | (raw[3] & 0xffL));
                result = (double) Float.intBitsToFloat(bits);
            } else if (token == 0x23) { // 64-bit real
                result = Double.longBitsToDouble(readLong(pos));
                pos += 8;
            } else if (token == 0x33) { // date: double seconds since CF epoch
                double seconds = Double.longBitsToDouble(readLong(pos));
                pos += 8;
                result = cfSecondsToInstant(seconds);
            } else if (tokenH == 0x40) { // data
                result = readBytes(getSize(tokenL));
            } else if (tokenH == 0x50) { // ASCII string
                byte[] raw = readBytes(getSize(tokenL));
                for (byte b : raw) {
                    if ((b & 0x80) != 0) {
                        throw new InvalidResponseError("Invalid ASCII string in binary plist");
                    }
                }
                result = new String(raw, StandardCharsets.US_ASCII);
            } else if (tokenH == 0x60) { // UTF-16BE string, size counted in code units
                result = new String(readBytes(getSize(tokenL) * 2), StandardCharsets.UTF_16BE);
            } else if (tokenH == 0x80) { // UID, 1 + tokenL bytes
                int size = 1 + tokenL;
                if (size > 8) {
                    throw new InvalidResponseError("UIDs larger than 8 bytes are not supported");
                }
                long uid = readUnsigned(pos, size);
                pos += size;
                result = new Uid(uid);
            } else if (tokenH == 0xA0) { // array
                int size = getSize(tokenL);
                int[] refs = readRefs(size);
                List<@Nullable Object> list = new ArrayList<>(size);
                objects[ref] = list; // memoize before recursing (allows shared refs)
                for (int childRef : refs) {
                    list.add(readObject(childRef));
                }
                return list;
            } else if (tokenH == 0xD0) { // dict
                int size = getSize(tokenL);
                int[] keyRefs = readRefs(size);
                int[] valueRefs = readRefs(size);
                Map<String, @Nullable Object> map = new LinkedHashMap<>();
                objects[ref] = map; // memoize before recursing
                for (int i = 0; i < size; i++) {
                    Object key = readObject(keyRefs[i]);
                    if (!(key instanceof String stringKey)) {
                        throw new InvalidResponseError("Binary plist dictionary key is not a string: " + key);
                    }
                    map.put(stringKey, readObject(valueRefs[i]));
                }
                return map;
            } else {
                throw new InvalidResponseError(String.format("Unsupported binary plist token 0x%02x", token));
            }

            objects[ref] = result;
            return result;
        }
    }

    private static Instant cfSecondsToInstant(double cfSeconds) {
        long whole = (long) Math.floor(cfSeconds);
        long nanos = Math.round((cfSeconds - whole) * 1_000_000_000L);
        return Instant.ofEpochSecond(whole + CF_EPOCH_OFFSET_SECONDS, nanos);
    }

    /** Writer state (no object deduplication). */
    private static final class Writer {

        private final List<Object> objectList = new ArrayList<>();
        /** Per object-id child references for containers (dict: keys then values). */
        private final List<int[]> childRefs = new ArrayList<>();
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private int refSize;

        byte[] write(Object value) {
            flatten(value);

            int numObjects = objectList.size();
            refSize = countToSize(numObjects);

            out.writeBytes(MAGIC);

            long[] objectOffsets = new long[numObjects];
            for (int i = 0; i < numObjects; i++) {
                objectOffsets[i] = out.size();
                writeObject(objectList.get(i), childRefs.get(i));
            }

            long offsetTableOffset = out.size();
            int offsetSize = countToSize(offsetTableOffset);
            for (long offset : objectOffsets) {
                writeUnsigned(offset, offsetSize);
            }

            // Trailer: 5 pad bytes, sort version 0, offset int size, object ref size,
            // number of objects, top object (always 0 here), offset table offset.
            for (int i = 0; i < 6; i++) {
                out.write(0);
            }
            out.write(offsetSize);
            out.write(refSize);
            writeUnsigned(numObjects, 8);
            writeUnsigned(0, 8);
            writeUnsigned(offsetTableOffset, 8);
            return out.toByteArray();
        }

        /**
         * Assigns object-table ids depth-first (dict keys before values) without
         * deduplication.
         */
        private int flatten(Object value) {
            int id = objectList.size();
            objectList.add(value);
            childRefs.add(new int[0]);

            if (value instanceof Map<?, ?> map) {
                int size = map.size();
                int[] refs = new int[size * 2];
                List<Object> values = new ArrayList<>(size);
                int i = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String)) {
                        throw new IllegalArgumentException("Binary plist keys must be strings: " + entry.getKey());
                    }
                    refs[i++] = flatten(entry.getKey());
                    values.add(entry.getValue());
                }
                for (Object child : values) {
                    refs[i++] = flatten(child);
                }
                childRefs.set(id, refs);
            } else if (value instanceof List<?> list) {
                int[] refs = new int[list.size()];
                for (int i = 0; i < refs.length; i++) {
                    refs[i] = flatten(list.get(i));
                }
                childRefs.set(id, refs);
            } else if (value != null && !(value instanceof Boolean) && !(value instanceof Number)
                    && !(value instanceof String) && !(value instanceof byte[]) && !(value instanceof Instant)
                    && !(value instanceof Uid)) {
                throw new IllegalArgumentException("Unsupported type in binary plist: " + value.getClass().getName());
            }
            return id;
        }

        private void writeObject(Object value, int[] refs) {
            switch (value) {
                case null -> out.write(0x00);
                case Boolean b -> out.write(b ? 0x09 : 0x08);
                case Double d -> {
                    out.write(0x23);
                    writeUnsigned(Double.doubleToLongBits(d), 8);
                }
                case Float f -> {
                    out.write(0x23);
                    writeUnsigned(Double.doubleToLongBits(f.doubleValue()), 8);
                }
                case Number n -> writeInt(n.longValue());
                case Instant instant -> {
                    double seconds = (double) (instant.getEpochSecond() - CF_EPOCH_OFFSET_SECONDS)
                            + instant.getNano() / 1e9;
                    out.write(0x33);
                    writeUnsigned(Double.doubleToLongBits(seconds), 8);
                }
                case byte[] bytes -> {
                    writeSize(0x40, bytes.length);
                    out.writeBytes(bytes);
                }
                case String s -> writeString(s);
                case Uid uid -> writeUid(uid.value());
                case List<?> ignored -> {
                    writeSize(0xA0, refs.length);
                    for (int childRef : refs) {
                        writeUnsigned(childRef, refSize);
                    }
                }
                case Map<?, ?> ignored -> {
                    writeSize(0xD0, refs.length / 2);
                    for (int childRef : refs) {
                        writeUnsigned(childRef, refSize);
                    }
                }
                default -> throw new IllegalArgumentException(
                        "Unsupported type in binary plist: " + value.getClass().getName());
            }
        }

        private void writeInt(long value) {
            if (value < 0) {
                out.write(0x13);
                writeUnsigned(value, 8);
            } else if (value < 1L << 8) {
                out.write(0x10);
                writeUnsigned(value, 1);
            } else if (value < 1L << 16) {
                out.write(0x11);
                writeUnsigned(value, 2);
            } else if (value < 1L << 32) {
                out.write(0x12);
                writeUnsigned(value, 4);
            } else {
                out.write(0x13);
                writeUnsigned(value, 8);
            }
        }

        private void writeString(String value) {
            boolean ascii = true;
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) > 0x7f) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                writeSize(0x50, value.length());
                out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
            } else {
                byte[] encoded = value.getBytes(StandardCharsets.UTF_16BE);
                writeSize(0x60, encoded.length / 2);
                out.writeBytes(encoded);
            }
        }

        private void writeUid(long value) {
            if (value < 1L << 8) {
                out.write(0x80);
                writeUnsigned(value, 1);
            } else if (value < 1L << 16) {
                out.write(0x81);
                writeUnsigned(value, 2);
            } else if (value < 1L << 32) {
                out.write(0x83);
                writeUnsigned(value, 4);
            } else {
                out.write(0x87);
                writeUnsigned(value, 8);
            }
        }

        private void writeSize(int token, long size) {
            if (size < 15) {
                out.write(token | (int) size);
            } else if (size < 1L << 8) {
                out.write(token | 0xF);
                out.write(0x10);
                writeUnsigned(size, 1);
            } else if (size < 1L << 16) {
                out.write(token | 0xF);
                out.write(0x11);
                writeUnsigned(size, 2);
            } else if (size < 1L << 32) {
                out.write(token | 0xF);
                out.write(0x12);
                writeUnsigned(size, 4);
            } else {
                out.write(token | 0xF);
                out.write(0x13);
                writeUnsigned(size, 8);
            }
        }

        private void writeUnsigned(long value, int size) {
            for (int shift = (size - 1) * 8; shift >= 0; shift -= 8) {
                out.write((int) (value >>> shift) & 0xff);
            }
        }

        private static int countToSize(long count) {
            if (count < 1L << 8) {
                return 1;
            } else if (count < 1L << 16) {
                return 2;
            } else if (count < 1L << 32) {
                return 4;
            }
            return 8;
        }
    }
}
