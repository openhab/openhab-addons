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
package org.openhab.binding.e3dc.internal.rscp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Encoder and decoder for RSCP data items and the surrounding frame.
 *
 * <p>
 * Frame layout, all little endian:
 *
 * <pre>
 * magic    uint16  0xE3DC
 * ctrl     uint16  bit 0x0010 enables the trailing CRC32
 * seconds  uint64
 * nanos    uint32
 * length   uint16  number of payload bytes
 * payload  length bytes of RSCP items
 * crc32    uint32    only present when ctrl bit 0x0010 is set
 * </pre>
 *
 * <p>
 * Item layout, all little endian:
 *
 * <pre>
 * tag      uint32
 * type     uint8
 * length   uint16
 * data     length bytes
 * </pre>
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class RscpCodec {

    /** The magic of a frame as it travels on the wire. */
    static final byte[] MAGIC_BYTES = { (byte) 0xE3, (byte) 0xDC };
    /** The protocol version, carried in the second control byte together with the CRC flag. */
    static final int VERSION = 0x11;

    /** The bit in the first control byte that announces the trailing CRC32. */
    static final int CTRL_CRC = 0x10;

    /**
     * The control bytes of a frame with a CRC, as they travel on the wire.
     *
     * <p>
     * The first byte is zero and the second carries the protocol version together with the CRC
     * flag. This is what a byte comparison against a client that works on real hardware shows for
     * the login frame: magic, control {0x00, 0x11} and an identical payload.
     */
    static final byte[] CTRL_BYTES = { 0, (byte) VERSION };
    static final int HEADER_LENGTH = 18;
    static final int ITEM_HEADER_LENGTH = 7;

    private RscpCodec() {
    }

    /** Decodes every item contained in a frame payload. */
    public static List<RscpValue> decodeFrame(byte[] frame) {
        ByteBuffer buffer = ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN);
        buffer.getShort(); // magic
        // the flag sits in the second control byte, which is read from the raw header
        int ctrl = frame[3] & 0xFF;
        buffer.getShort(); // control
        buffer.getLong(); // seconds
        buffer.getInt(); // nanos
        int dataLength = buffer.getShort() & 0xFFFF;
        int end = Math.min(HEADER_LENGTH + dataLength, frame.length - ((ctrl & CTRL_CRC) != 0 ? 4 : 0));
        ByteBuffer data = ByteBuffer.wrap(frame, HEADER_LENGTH, Math.max(0, end - HEADER_LENGTH)).slice()
                .order(ByteOrder.LITTLE_ENDIAN);
        List<RscpValue> items = new ArrayList<>();
        while (data.hasRemaining()) {
            items.add(decodeItem(data));
        }
        return items;
    }

    private static RscpValue decodeItem(ByteBuffer buffer) {
        int tag = buffer.getInt();
        int typeCode = buffer.get() & 0xFF;
        int length = buffer.getShort() & 0xFFFF;
        RscpType type = RscpType.fromCode(typeCode);
        if (type == RscpType.CONTAINER) {
            int end = buffer.position() + length;
            List<RscpValue> children = new ArrayList<>();
            while (buffer.position() < end && buffer.hasRemaining()) {
                children.add(decodeItem(buffer));
            }
            return RscpValue.container(tag, children);
        }
        Object value;
        switch (type) {
            case NONE:
                value = null;
                skip(buffer, length);
                break;
            case BOOL:
                value = buffer.get() != 0;
                skip(buffer, length - 1);
                break;
            case CHAR8:
                value = (int) buffer.get();
                skip(buffer, length - 1);
                break;
            case UCHAR8:
                value = buffer.get() & 0xFF;
                skip(buffer, length - 1);
                break;
            case INT16:
                value = (int) buffer.getShort();
                skip(buffer, length - 2);
                break;
            case UINT16:
                value = buffer.getShort() & 0xFFFF;
                skip(buffer, length - 2);
                break;
            case INT32:
                value = buffer.getInt();
                skip(buffer, length - 4);
                break;
            case UINT32:
                value = buffer.getInt() & 0xFFFFFFFFL;
                skip(buffer, length - 4);
                break;
            case INT64:
            case UINT64:
                value = buffer.getLong();
                skip(buffer, length - 8);
                break;
            case FLOAT32:
                value = buffer.getFloat();
                skip(buffer, length - 4);
                break;
            case DOUBLE64:
                value = buffer.getDouble();
                skip(buffer, length - 8);
                break;
            case ERROR: {
                int code = 0;
                for (int i = 0; i < Math.min(length, 4); i++) {
                    code |= (buffer.get() & 0xFF) << (8 * i);
                }
                skip(buffer, length - Math.min(length, 4));
                value = RscpError.fromCode(code);
                break;
            }
            default: {
                byte[] bytes = new byte[Math.min(length, buffer.remaining())];
                buffer.get(bytes);
                skip(buffer, length - bytes.length);
                value = bytes;
                break;
            }
        }
        return RscpValue.scalar(tag, type, value);
    }

    private static void skip(ByteBuffer buffer, int count) {
        if (count > 0) {
            buffer.position(Math.min(buffer.position() + count, buffer.limit()));
        }
    }

    /**
     * Encodes a frame including the CRC32 trailer.
     *
     * <p>
     * A frame carries a list of elements, each one a top level item. There is no outer element with a
     * tag of its own, so callers pass the items directly; wrapping them in a container would announce a
     * tag the interface does not have.
     */
    public static byte[] encodeFrame(List<RscpValue> items) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        for (RscpValue item : items) {
            encodeItem(item, payload);
        }
        byte[] data = payload.toByteArray();
        if (data.length > 0xFFFF) {
            throw new IOException("RSCP payload exceeds the maximum frame size: " + data.length);
        }

        ByteBuffer frame = ByteBuffer.allocate(HEADER_LENGTH + data.length + 4).order(ByteOrder.LITTLE_ENDIAN);
        frame.put(MAGIC_BYTES[0]);
        frame.put(MAGIC_BYTES[1]);
        // the control bytes are written as they travel
        frame.put(CTRL_BYTES[0]);
        frame.put(CTRL_BYTES[1]);
        long now = System.currentTimeMillis();
        frame.putLong(now / 1000);
        frame.putInt((int) ((now % 1000) * 1_000_000));
        frame.putShort((short) data.length);
        frame.put(data);
        byte[] bytes = frame.array();
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length - 4);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - 4, (int) crc.getValue());
        return bytes;
    }

    private static void encodeItem(RscpValue item, ByteArrayOutputStream out) throws IOException {
        byte[] data;
        int typeCode = item.getType().getCode();
        switch (item.getType()) {
            case NONE:
                data = new byte[0];
                break;
            case CONTAINER: {
                ByteArrayOutputStream inner = new ByteArrayOutputStream();
                for (RscpValue child : item.getChildren()) {
                    encodeItem(child, inner);
                }
                data = inner.toByteArray();
                break;
            }
            case CSTRING: {
                // no trailing NUL: the device expects the bare text, as the reference clients send it
                data = String.valueOf(item.getValue()).getBytes(StandardCharsets.UTF_8);
                break;
            }
            case BOOL:
                data = new byte[] { (byte) (Boolean.TRUE.equals(item.getValue()) ? 1 : 0) };
                break;
            case CHAR8:
            case UCHAR8:
                data = new byte[] { (byte) toLong(item) };
                break;
            case INT16:
            case UINT16:
                data = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) toLong(item)).array();
                break;
            case INT32:
            case UINT32:
                data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) toLong(item)).array();
                break;
            case INT64:
            case UINT64:
                data = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(toLong(item)).array();
                break;
            case FLOAT32:
                data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(requireNumber(item).floatValue())
                        .array();
                break;
            case DOUBLE64:
                data = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                        .putDouble(requireNumber(item).doubleValue()).array();
                break;
            case BYTEARRAY:
            case BITFIELD:
                data = requireBytes(item);
                break;
            default:
                throw new IOException("Cannot encode RSCP type " + item.getType());
        }
        out.write(ByteBuffer.allocate(ITEM_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putInt(item.getTag())
                .put((byte) typeCode).putShort((short) data.length).array());
        out.write(data);
    }

    private static Number requireNumber(RscpValue item) throws IOException {
        Number number = item.asNumber();
        if (number == null) {
            throw new IOException("RSCP type " + item.getType() + " needs a numeric value");
        }
        return number;
    }

    private static byte[] requireBytes(RscpValue item) throws IOException {
        byte[] bytes = item.asBytes();
        if (bytes == null) {
            throw new IOException("RSCP type " + item.getType() + " needs a byte array value");
        }
        return bytes;
    }

    private static long toLong(RscpValue item) throws IOException {
        return requireNumber(item).longValue();
    }

    /**
     * Reads a complete frame from a plaintext stream. The length is taken from the header, so this
     * does not depend on socket read timing.
     */
    public static byte[] readFrame(InputStream in) throws IOException {
        byte[] header = readFully(in, HEADER_LENGTH);
        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        if (header[0] != MAGIC_BYTES[0] || header[1] != MAGIC_BYTES[1]) {
            throw new IOException(String.format("Not an RSCP frame, magic was %02X %02X", header[0], header[1]));
        }
        int ctrl = header[3] & 0xFF;
        buffer.position(4); // past magic and control, which were read from the raw header
        buffer.getLong(); // seconds
        buffer.getInt(); // nanos
        int dataLength = buffer.getShort() & 0xFFFF;
        int remaining = dataLength + ((ctrl & CTRL_CRC) != 0 ? 4 : 0);
        byte[] frame = new byte[HEADER_LENGTH + remaining];
        System.arraycopy(header, 0, frame, 0, HEADER_LENGTH);
        byte[] rest = readFully(in, remaining);
        System.arraycopy(rest, 0, frame, HEADER_LENGTH, remaining);
        if ((ctrl & CTRL_CRC) != 0) {
            CRC32 crc = new CRC32();
            crc.update(frame, 0, frame.length - 4);
            int expected = ByteBuffer.wrap(frame, frame.length - 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if ((int) crc.getValue() != expected) {
                throw new IOException("RSCP frame CRC mismatch");
            }
        }
        return frame;
    }

    public static byte[] readFully(InputStream in, int length) throws IOException {
        byte[] out = new byte[length];
        int read = 0;
        while (read < length) {
            int count = in.read(out, read, length - read);
            if (count < 0) {
                throw new EOFException("Connection closed after " + read + " of " + length + " bytes");
            }
            read += count;
        }
        return out;
    }
}
