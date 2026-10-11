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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

/**
 * Decrypts a captured session with the binding's own cipher.
 *
 * <p>
 * The first block of the client stream is the authentication request, so a correct cipher and password
 * turn it into a frame that begins with the RSCP magic. That is the end to end check: it exercises the
 * engine, the key derivation, the chaining and the padding together.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class CaptureDecrypt {

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
            byte[] data = entry.getValue();
            Object cipher = constructor.newInstance((Object) password.getBytes("UTF-8"));
            byte[] plain = (byte[]) decrypt.invoke(cipher, (Object) Arrays.copyOf(data, 32));
            int magic = (plain[0] & 0xFF) | ((plain[1] & 0xFF) << 8);
            System.out.printf("stream %-6s first block: %s%n", entry.getKey(), BlockCheck.toHex(plain));
            System.out.printf("              magic 0x%04X %s%n", magic, magic == 0xE3DC ? "*** MATCH ***" : "");
        }
    }

    private CaptureDecrypt() {
    }
}
