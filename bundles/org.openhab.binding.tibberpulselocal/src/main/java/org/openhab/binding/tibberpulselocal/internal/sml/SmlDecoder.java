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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * Decodes the SML 1.04 frames of a stream message into {@link SmlReading}s. It is written from the SML specification
 * and the observed frame and has no dependency; the tolerance for truncated frames and the fixed OBIS table follow the
 * Home Assistant integration
 * <a href="https://github.com/marq24/ha-tibber-pulse-local">marq24/ha-tibber-pulse-local</a>, without copying code.
 *
 * Rules:
 * <ul>
 * <li>A frame that fails the checks of {@link SmlFrames} or is structurally broken (a length beyond the frame, a list
 * count larger than the remaining bytes, nesting deeper than eight levels, an incomplete message) yields nothing, not
 * even a part of it.
 * <li>A problem with one entry (unknown OBIS code, unit code other than the one of the table, scaler outside -18 to
 * 18, a value that is not a number) skips only that entry.
 * <li>Integers are read as signed or unsigned as the entry says, so a 64-bit counter with the high bit set stays
 * positive. The value is raw x 10^scaler, computed with {@link BigDecimal} without rounding.
 * <li>The net power is 1-0:16.7.0, or 1-0:1.7.0 minus 1-0:2.7.0 if the first is absent and both others are present;
 * otherwise it is absent.
 * </ul>
 *
 * The meter id is returned in the reading only. This class never logs and never puts content into an exception.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public final class SmlDecoder {

    private static final int TYPE_OCTET_STRING = 0;
    private static final int TYPE_INTEGER = 5;
    private static final int TYPE_UNSIGNED = 6;
    private static final int TYPE_LIST = 7;

    private static final int MAX_DEPTH = 8;
    private static final int MAX_TL_BYTES = 4;
    private static final int MAX_INTEGER_BYTES = 8;
    private static final int MAX_SCALER = 18;
    /** Server ids are at most ten bytes (DIN 43863-5); the limit keeps a hostile frame from filling a property. */
    private static final int MAX_METER_ID_BYTES = 64;
    private static final int TAG_GET_LIST_RESPONSE = 0x0701;
    private static final int OBIS_LENGTH = 6;
    private static final HexFormat HEX = HexFormat.of();

    /** Marks a list that was skipped. */
    private static final Object LIST = new Object();
    /** Marks a value of a type or width that this decoder does not interpret. */
    private static final Object UNSUPPORTED = new Object();

    private SmlDecoder() {
    }

    /**
     * @param payload the bytes after the message header; may contain zero, one, or several frames
     * @param offset index of the first payload byte
     * @return one reading for each valid frame that carries at least one applicable entry, in frame order; empty if
     *         there is none
     */
    public static List<SmlReading> decode(byte[] payload, int offset) {
        List<SmlReading> readings = new ArrayList<>();
        for (byte[] body : SmlFrames.extract(payload, offset)) {
            SmlReading reading = decodeFrame(body);
            if (reading != null) {
                readings.add(reading);
            }
        }
        return readings;
    }

    private static @Nullable SmlReading decodeFrame(byte[] body) {
        Cursor cursor = new Cursor(body);
        Collector collector = new Collector();
        try {
            while (!cursor.atEnd()) {
                readMessage(cursor, collector);
            }
        } catch (SmlFormatException e) {
            return null;
        }
        return collector.toReading();
    }

    /** SmlMessage: list of 6 (transaction id, group, abort flag, body, CRC, end marker). */
    private static void readMessage(Cursor c, Collector collector) throws SmlFormatException {
        c.expectList(6);
        c.value();
        c.value();
        c.value();
        c.expectList(2);
        Object tag = c.value();
        if (tag instanceof BigInteger t && t.equals(BigInteger.valueOf(TAG_GET_LIST_RESPONSE))) {
            readListResponse(c, collector);
        } else {
            c.value();
        }
        // The frame CRC covers the message CRC; it is skipped, not re-checked.
        c.value();
        c.expectEndOfMessage();
    }

    /** GetListResponse: client id, server id, list name, sensor time, value list, signature, gateway time. */
    private static void readListResponse(Cursor c, Collector collector) throws SmlFormatException {
        Element response = c.next();
        if (response.type() != TYPE_LIST) {
            return;
        }
        if (response.count() != 7) {
            c.skipElements(response.count());
            return;
        }
        c.value();
        c.value();
        c.value();
        c.value();
        Element list = c.next();
        if (list.type() == TYPE_LIST) {
            for (int i = 0; i < list.count(); i++) {
                readEntry(c, collector);
            }
        }
        c.value();
        c.value();
    }

    /** ListEntry: object name, status, value time, unit, scaler, value, signature. */
    private static void readEntry(Cursor c, Collector collector) throws SmlFormatException {
        Element entry = c.next();
        if (entry.type() != TYPE_LIST) {
            return;
        }
        if (entry.count() != 7) {
            c.skipElements(entry.count());
            return;
        }
        Object name = c.value();
        c.value();
        c.value();
        Object unit = c.value();
        Object scaler = c.value();
        Object value = c.value();
        c.value();
        collector.entry(name, unit, scaler, value);
    }

    private record Element(int type, int count, int dataStart, int dataLength) {
    }

    private static final class SmlFormatException extends Exception {
        private static final long serialVersionUID = 1L;

        SmlFormatException() {
            // No message and no stack trace: this is routine control flow for damaged frames.
            super(null, null, false, false);
        }
    }

    /** Bounds-checked reader of SML type-length-value elements. */
    private static final class Cursor {
        private final byte[] data;
        private int pos;
        private int depth;

        Cursor(byte[] data) {
            this.data = data;
        }

        boolean atEnd() {
            return pos >= data.length;
        }

        /**
         * Reads the header of the next element. For a list, the position is behind the header and the elements
         * follow; for any other type, the position is behind the data.
         */
        Element next() throws SmlFormatException {
            if (pos >= data.length) {
                throw new SmlFormatException();
            }
            int current = data[pos++] & 0xFF;
            int type = (current >> 4) & 0x07;
            int length = current & 0x0F;
            int headerBytes = 1;
            while ((current & 0x80) != 0) {
                if (pos >= data.length || headerBytes >= MAX_TL_BYTES) {
                    throw new SmlFormatException();
                }
                current = data[pos++] & 0xFF;
                length = (length << 4) | (current & 0x0F);
                headerBytes++;
            }
            if (type == TYPE_LIST) {
                // Every element needs at least one byte, so this bounds all allocation by the frame size.
                if (length > data.length - pos) {
                    throw new SmlFormatException();
                }
                return new Element(type, length, pos, 0);
            }
            // The length of a scalar counts its own header bytes.
            int dataLength = length - headerBytes;
            if (dataLength < 0 || dataLength > data.length - pos) {
                throw new SmlFormatException();
            }
            Element element = new Element(type, 0, pos, dataLength);
            pos += dataLength;
            return element;
        }

        void expectList(int count) throws SmlFormatException {
            Element e = next();
            if (e.type() != TYPE_LIST || e.count() != count) {
                throw new SmlFormatException();
            }
        }

        void expectEndOfMessage() throws SmlFormatException {
            if (pos >= data.length || data[pos] != 0x00) {
                throw new SmlFormatException();
            }
            pos++;
        }

        void skipElements(int count) throws SmlFormatException {
            if (++depth > MAX_DEPTH) {
                throw new SmlFormatException();
            }
            for (int i = 0; i < count; i++) {
                Element e = next();
                if (e.type() == TYPE_LIST) {
                    skipElements(e.count());
                }
            }
            depth--;
        }

        /**
         * Reads one element.
         *
         * @return null for an absent value, a {@link BigInteger} for an integer, a byte array for a non-empty octet
         *         string, {@link #LIST} for a list (skipped with its content), or {@link #UNSUPPORTED}
         */
        @Nullable
        Object value() throws SmlFormatException {
            Element e = next();
            switch (e.type()) {
                case TYPE_LIST:
                    skipElements(e.count());
                    return LIST;
                case TYPE_OCTET_STRING:
                    return e.dataLength() == 0 ? null
                            : Arrays.copyOfRange(data, e.dataStart(), e.dataStart() + e.dataLength());
                case TYPE_INTEGER:
                case TYPE_UNSIGNED:
                    if (e.dataLength() == 0 || e.dataLength() > MAX_INTEGER_BYTES) {
                        return UNSUPPORTED;
                    }
                    byte[] raw = Arrays.copyOfRange(data, e.dataStart(), e.dataStart() + e.dataLength());
                    return e.type() == TYPE_INTEGER ? new BigInteger(raw) : new BigInteger(1, raw);
                default:
                    return UNSUPPORTED;
            }
        }
    }

    /** Applies the entry rules and derives the net power. */
    private static final class Collector {
        private final Map<Obis, BigDecimal> values = new EnumMap<>(Obis.class);
        private @Nullable String meterId;

        void entry(@Nullable Object name, @Nullable Object unit, @Nullable Object scaler, @Nullable Object value) {
            if (!(name instanceof byte[] objName) || objName.length != OBIS_LENGTH) {
                return;
            }
            if (isMeterId(objName)) {
                if (meterId == null && value instanceof byte[] id && id.length <= MAX_METER_ID_BYTES) {
                    meterId = HEX.formatHex(id);
                }
                return;
            }
            Obis obis = Obis.find(objName);
            if (obis == null || !(unit instanceof BigInteger unitCode)
                    || !unitCode.equals(BigInteger.valueOf(obis.unitCode()))) {
                return;
            }
            int exponent = 0;
            if (scaler != null) {
                if (!(scaler instanceof BigInteger s) || s.abs().compareTo(BigInteger.valueOf(MAX_SCALER)) > 0) {
                    return;
                }
                exponent = s.intValue();
            }
            if (!(value instanceof BigInteger raw)) {
                return;
            }
            BigDecimal decoded = new BigDecimal(raw).scaleByPowerOfTen(exponent);
            if (decoded.scale() < 0) {
                // A positive scaler would give an exponent notation such as 1.2E+3 in the state string.
                decoded = decoded.setScale(0);
            }
            values.put(obis, decoded);
        }

        /** The meter's server id: 0-0:96.1.0 or 1-0:96.1.0. */
        private static boolean isMeterId(byte[] n) {
            return (n[0] == 0x00 || n[0] == 0x01) && n[1] == 0x00 && n[2] == 0x60 && n[3] == 0x01 && n[4] == 0x00
                    && n[5] == (byte) 0xff;
        }

        @Nullable
        SmlReading toReading() {
            if (!values.containsKey(Obis.POWER)) {
                BigDecimal imported = values.get(Obis.IMPORT_POWER);
                BigDecimal exported = values.get(Obis.EXPORT_POWER);
                if (imported != null && exported != null) {
                    values.put(Obis.POWER, imported.subtract(exported));
                }
            }
            Map<String, QuantityType<?>> channels = new LinkedHashMap<>();
            for (Obis obis : Obis.values()) {
                String channelId = obis.channelId();
                BigDecimal value = values.get(obis);
                if (channelId != null && value != null) {
                    channels.put(channelId, obis.toQuantity(value));
                }
            }
            return channels.isEmpty() ? null : new SmlReading(channels, meterId);
        }
    }
}
