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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builds SML frames with correct message and frame CRCs, so that every scenario can be produced without hand-written
 * hex. It has its own CRC implementation on purpose (cross-checked against the fixture), not the production one.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public final class SmlFrameBuilder {

    public static final byte[] POWER = obis(0x10, 0x07);
    public static final byte[] POWER_IMPORT = obis(0x01, 0x07);
    public static final byte[] POWER_EXPORT = obis(0x02, 0x07);
    public static final byte[] ENERGY_IMPORT = obis(0x01, 0x08);
    public static final byte[] ENERGY_EXPORT = obis(0x02, 0x08);
    public static final byte[] VOLTAGE_L1 = obis(0x20, 0x07);
    public static final byte[] FREQUENCY = obis(0x0e, 0x07);
    public static final byte[] METER_ID = { 0x01, 0x00, 0x60, 0x01, 0x00, (byte) 0xff };
    public static final byte[] FIRMWARE = { 0x01, 0x00, 0x00, 0x02, 0x00, (byte) 0xff };

    public static final int UNIT_W = 27;
    public static final int UNIT_WH = 30;
    public static final int UNIT_A = 33;
    public static final int UNIT_V = 35;
    public static final int UNIT_HZ = 44;

    private final List<byte[]> entries = new ArrayList<>();
    private boolean doubleEscapes;

    /** @return the binary form of 1-0:c.d.0*255 */
    public static byte[] obis(int c, int d) {
        return new byte[] { 0x01, 0x00, (byte) c, (byte) d, 0x00, (byte) 0xff };
    }

    /** Adds an entry with a signed integer of the given width. */
    public SmlFrameBuilder signed(byte[] obis, int unit, @Nullable Integer scaler, long value, int width) {
        return entry(obis, unit, scaler, integer(BigInteger.valueOf(value), width, true));
    }

    /** Adds an entry with an unsigned integer of the given width. */
    public SmlFrameBuilder unsigned(byte[] obis, int unit, @Nullable Integer scaler, BigInteger value, int width) {
        return entry(obis, unit, scaler, integer(value, width, false));
    }

    public SmlFrameBuilder octets(byte[] obis, byte[] value) {
        return entry(obis, null, null, octetString(value));
    }

    /** Adds an entry with a prepared value element. */
    public SmlFrameBuilder entry(byte[] obis, @Nullable Integer unit, @Nullable Integer scaler, byte[] valueElement) {
        entries.add(list(7, octetString(obis), absent(), absent(),
                unit == null ? absent() : integer(BigInteger.valueOf(unit), 1, false),
                scaler == null ? absent() : integer(BigInteger.valueOf(scaler), 1, true), valueElement, absent()));
        return this;
    }

    /** Sends every run of four 0x1b bytes in the frame doubled, as the transport requires. */
    public SmlFrameBuilder doubleEscapes() {
        doubleEscapes = true;
        return this;
    }

    /** @return the frame bytes of a frame with an open response, the list response, and a close response */
    public byte[] build() {
        byte[] messages = concat(
                message(0x0101,
                        list(6, absent(), absent(), octetString(new byte[] { 1 }), octetString(new byte[] { 1, 2 }),
                                absent(), absent())),
                message(0x0701, listResponse()), message(0x0201, list(1, absent())));
        return frame(messages);
    }

    /** @return a frame around the given message bytes, with padding and CRC */
    public byte[] frame(byte[] messages) {
        byte[] escaped = doubleEscapes ? escape(messages) : messages;
        int padding = (4 - escaped.length % 4) % 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] { 0x1b, 0x1b, 0x1b, 0x1b, 1, 1, 1, 1 });
        out.writeBytes(escaped);
        out.writeBytes(new byte[padding]);
        out.writeBytes(new byte[] { 0x1b, 0x1b, 0x1b, 0x1b, 0x1a, (byte) padding });
        byte[] withoutCrc = out.toByteArray();
        int crc = crc(withoutCrc, withoutCrc.length);
        out.write(crc & 0xFF);
        out.write(crc >> 8);
        return out.toByteArray();
    }

    private byte[] listResponse() {
        List<byte[]> parts = new ArrayList<>();
        parts.add(octetString(new byte[] { 1, 2, 3, 4, 5, 6 }));
        parts.add(octetString(new byte[] { 0x0a, 1, 2, 3 }));
        parts.add(octetString(obis(0x62, 0x0a)));
        parts.add(absent());
        parts.add(list(entries.size(), entries.toArray(new byte[0][])));
        parts.add(absent());
        parts.add(absent());
        return list(7, parts.toArray(new byte[0][]));
    }

    /** SmlMessage: list of 6 with the CRC of the message up to the CRC field. */
    public static byte[] message(int tag, byte[] body) {
        byte[] head = concat(tl(7, 6), octetString(new byte[] { 0x11 }), integer(BigInteger.ZERO, 1, false),
                integer(BigInteger.ZERO, 1, false), tl(7, 2), integer(BigInteger.valueOf(tag), 2, false), body);
        int crc = crc(head, head.length);
        // The CRC is stored low byte first.
        return concat(head, integer(BigInteger.valueOf(((crc & 0xFF) << 8) | (crc >> 8)), 2, false), new byte[] { 0 });
    }

    public static byte[] list(int count, byte[]... elements) {
        return concat(tl(7, count), concat(elements));
    }

    public static byte[] octetString(byte[] data) {
        return concat(tl(0, data.length), data);
    }

    /** The "not set" marker of optional fields. */
    public static byte[] absent() {
        return new byte[] { 0x01 };
    }

    public static byte[] integer(BigInteger value, int width, boolean signed) {
        byte[] full = value.toByteArray();
        byte[] data = new byte[width];
        // Sign-extend or truncate the two's complement representation to the width.
        byte fill = (byte) (signed && value.signum() < 0 ? 0xff : 0);
        for (int i = 0; i < width; i++) {
            int src = full.length - 1 - i;
            data[width - 1 - i] = src >= 0 ? full[src] : fill;
        }
        return concat(tl(signed ? 5 : 6, width), data);
    }

    /**
     * Type-length header. For a list the length is the element count, otherwise the number of data bytes; the header
     * bytes are added to a scalar length as the format requires.
     */
    public static byte[] tl(int type, int length) {
        if (type == 7) {
            return header(type, length);
        }
        for (int bytes = 1; bytes <= 3; bytes++) {
            int total = length + bytes;
            if (total < (1 << (4 * bytes))) {
                return header(type, total);
            }
        }
        throw new IllegalArgumentException("too long");
    }

    private static byte[] header(int type, int encodedLength) {
        int bytes = 1;
        while (encodedLength >= (1 << (4 * bytes))) {
            bytes++;
        }
        byte[] out = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            int nibble = (encodedLength >> (4 * (bytes - 1 - i))) & 0x0F;
            out[i] = (byte) (nibble | (i == 0 ? type << 4 : 0) | (i < bytes - 1 ? 0x80 : 0));
        }
        return out;
    }

    private static byte[] escape(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < data.length) {
            if (i + 4 <= data.length && data[i] == 0x1b && data[i + 1] == 0x1b && data[i + 2] == 0x1b
                    && data[i + 3] == 0x1b) {
                out.writeBytes(new byte[] { 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b, 0x1b });
                i += 4;
            } else {
                out.write(data[i++]);
            }
        }
        return out.toByteArray();
    }

    /** CRC-16/X-25, bitwise, independent of the production implementation. */
    public static int crc(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= data[i] & 0xFF;
            for (int k = 0; k < 8; k++) {
                if ((crc & 1) == 1) {
                    crc = (crc >> 1) ^ 0x8408;
                } else {
                    crc >>= 1;
                }
            }
        }
        return ~crc & 0xFFFF;
    }

    public static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }
}
