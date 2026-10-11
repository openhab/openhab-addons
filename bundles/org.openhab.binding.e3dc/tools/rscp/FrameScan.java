/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0. If a copy of the MPL was not distributed with this
 * work for additional information regarding copyright ownership.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Finds RSCP frames inside a decrypted capture.
 *
 * <p>
 * A capture that begins in the middle of a session has no magic at offset zero, so the frames have to be
 * located in the decrypted bytes. Consecutive frames show the framing is understood and that the header
 * fields are consistent.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class FrameScan {

    public static void main(String[] args) throws Exception {
        byte[] raw = Files.readAllBytes(Path.of(args[0]));
        String password = args.length > 1 ? args[1] : "smartpower";
        Map<String, byte[]> streams = KeyRecover.reassemble(raw, 5033);

        Class<?> cipherClass = Class.forName("org.openhab.binding.e3dc.internal.rscp.RscpCipher");
        Constructor<?> constructor = cipherClass.getDeclaredConstructor(byte[].class);
        constructor.setAccessible(true);
        Method decrypt = cipherClass.getDeclaredMethod("decrypt", byte[].class);
        decrypt.setAccessible(true);

        for (Map.Entry<String, byte[]> entry : streams.entrySet()) {
            Object cipher = constructor.newInstance((Object) password.getBytes("UTF-8"));
            byte[] plain = (byte[]) decrypt.invoke(cipher, (Object) entry.getValue());
            System.out.printf("%n=== %s: %d decrypted bytes ===%n", entry.getKey(), plain.length);

            int found = 0;
            int offset = 0;
            while (offset + 18 <= plain.length && found < 6) {
                int magic = (plain[offset] & 0xFF) | ((plain[offset + 1] & 0xFF) << 8);
                if (magic == 0xE3DC) {
                    ByteBuffer header = ByteBuffer.wrap(plain, offset, 18).order(ByteOrder.LITTLE_ENDIAN);
                    header.getShort();
                    int ctrl = header.getShort() & 0xFFFF;
                    long seconds = header.getLong();
                    header.getInt();
                    int length = header.getShort() & 0xFFFF;
                    System.out.printf("  frame at %5d: ctrl=0x%04X seconds=%d payload=%d total=%d%n", offset, ctrl,
                            seconds, length, 18 + length + ((ctrl & 0x0010) != 0 ? 4 : 0));
                    found++;
                    int advance = 18 + length + ((ctrl & 0x0010) != 0 ? 4 : 0);
                    offset += Math.max(advance, 32);
                } else {
                    offset += 32;
                }
            }
            if (found == 0) {
                System.out.println("  no frame boundary found in this direction");
            }
        }
    }

    private FrameScan() {
    }
}
