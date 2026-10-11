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

import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Checks the binding's cipher against BouncyCastle and against a captured handshake.
 *
 * <p>
 * BouncyCastle is the implementation a client known to work against these devices uses, so it defines
 * the expected behaviour. The captured stream then confirms the result end to end.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class VerifyCipher {

    private static final int BLOCK = 32;

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[32];
        byte[] plain = new byte[32];

        RijndaelEngine engine = new RijndaelEngine(256);
        engine.init(true, new KeyParameter(key));
        byte[] expected = new byte[32];
        engine.processBlock(plain, 0, expected, 0);

        Class<?> clazz = Class.forName("org.openhab.binding.e3dc.internal.rscp.Rijndael256");
        Constructor<?> constructor = clazz.getDeclaredConstructor(byte[].class);
        constructor.setAccessible(true);
        Object instance = constructor.newInstance((Object) key);
        Method encrypt = clazz.getDeclaredMethod("encrypt", byte[].class);
        encrypt.setAccessible(true);
        Method decrypt = clazz.getDeclaredMethod("decrypt", byte[].class);
        decrypt.setAccessible(true);

        byte[] actual = (byte[]) encrypt.invoke(instance, (Object) plain);
        System.out.println("BouncyCastle : " + BlockCheck.toHex(expected));
        System.out.println("binding      : " + BlockCheck.toHex(actual));
        System.out.println(Arrays.equals(expected, actual) ? "ENCRYPTION MATCHES BOUNCYCASTLE" : "ENCRYPTION DIFFERS");
        System.out.println();

        // decryption must invert encryption
        byte[] back = (byte[]) decrypt.invoke(instance, (Object) actual);
        System.out.println("decrypt(encrypt(x)) == x : " + Arrays.equals(back, plain));

        if (args.length < 3) {
            return;
        }
        String password = args[1];
        byte[] raw = Files.readAllBytes(Path.of(args[0]));
        Map<String, byte[]> streams = KeyRecover.reassemble(raw, 5033);
        byte[] client = streams.get("client");

        Class<?> cipherClass = Class.forName("org.openhab.binding.e3dc.internal.rscp.RscpCipher");
        Constructor<?> cipherConstructor = cipherClass.getDeclaredConstructor(byte[].class);
        cipherConstructor.setAccessible(true);
        Method cipherDecrypt = cipherClass.getDeclaredMethod("decrypt", byte[].class);
        cipherDecrypt.setAccessible(true);
        Object cipher = cipherConstructor.newInstance((Object) password.getBytes("UTF-8"));

        byte[] first = Arrays.copyOf(client, BLOCK);
        byte[] decrypted = (byte[]) cipherDecrypt.invoke(cipher, (Object) first);
        int magic = (decrypted[0] & 0xFF) | ((decrypted[1] & 0xFF) << 8);
        System.out.println();
        System.out.println("capture first client block decrypted: " + BlockCheck.toHex(decrypted));
        System.out.printf("magic = 0x%04X %s%n", magic, magic == 0xE3DC ? "*** MATCH ***" : "");
    }

    private VerifyCipher() {
    }
}
