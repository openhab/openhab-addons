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
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * Builds RSCP frames by hand so diagnostics do not depend on the binding's own encoder.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class AuthFrames {

    /** A complete authentication request as it goes on the wire before encryption. */
    public static byte[] authFrame(String user, String password) throws Exception {
        ByteArrayOutputStream children = new ByteArrayOutputStream();
        children.write(item(0x00000002, 0x0D, user));
        children.write(item(0x00000003, 0x0D, password));
        byte[] inner = children.toByteArray();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.write(item(0x00000001, 0x0E, inner));
        byte[] data = payload.toByteArray();

        ByteBuffer frame = ByteBuffer.allocate(18 + data.length + 4).order(ByteOrder.LITTLE_ENDIAN);
        frame.putShort((short) 0xE3DC);
        frame.putShort((short) 0x0010);
        frame.putLong(System.currentTimeMillis() / 1000);
        frame.putInt(0);
        frame.putShort((short) data.length);
        frame.put(data);
        byte[] bytes = frame.array();
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length - 4);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - 4, (int) crc.getValue());
        return bytes;
    }

    public static byte[] item(int tag, int type, byte[] value) {
        ByteBuffer header = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN).putInt(tag).put((byte) type)
                .putShort((short) value.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(header.array(), 0, 7);
        out.write(value, 0, value.length);
        return out.toByteArray();
    }

    public static byte[] item(int tag, int type, String text) {
        byte[] raw = text.getBytes(StandardCharsets.UTF_8);
        byte[] value = new byte[raw.length + 1];
        System.arraycopy(raw, 0, value, 0, raw.length);
        return item(tag, type, value);
    }

    private AuthFrames() {
    }
}
