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
 * Reports how structured the decrypted client stream is.
 *
 * <p>
 * A correct key turns ciphertext into protocol data, which is full of zero bytes because tags, lengths
 * and padding are mostly small numbers. A wrong key produces a uniform distribution. Counting zero bytes
 * therefore shows whether the cipher and the key are right even when the capture does not start at a
 * frame boundary, where the magic cannot be expected.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class StructureCheck {

    public static void main(String[] args) throws Exception {
        byte[] raw = Files.readAllBytes(Path.of(args[0]));
        String password = args.length > 1 ? args[1] : "smartpower";
        Map<String, byte[]> streams = KeyRecover.reassemble(raw, 5033);
        byte[] client = streams.get("client");

        Class<?> cipherClass = Class.forName("org.openhab.binding.e3dc.internal.rscp.RscpCipher");
        Constructor<?> constructor = cipherClass.getDeclaredConstructor(byte[].class);
        constructor.setAccessible(true);
        Method decrypt = cipherClass.getDeclaredMethod("decrypt", byte[].class);
        decrypt.setAccessible(true);

        Object cipher = constructor.newInstance((Object) password.getBytes("UTF-8"));
        byte[] plain = (byte[]) decrypt.invoke(cipher, (Object) client);

        int zeros = 0;
        int printable = 0;
        int[] histogram = new int[256];
        for (byte b : plain) {
            int v = b & 0xFF;
            histogram[v]++;
            if (v == 0) {
                zeros++;
            }
            if (v >= 32 && v < 127) {
                printable++;
            }
        }
        System.out.printf("decrypted %d bytes with password '%s'%n", plain.length, password);
        System.out.printf("  zero bytes     : %d (%.1f%%)%n", zeros, 100.0 * zeros / plain.length);
        System.out.printf("  printable      : %d (%.1f%%)%n", printable, 100.0 * printable / plain.length);
        System.out.println("  a correct key gives clearly more than 1/256 zeros (about 0.4%)");

        int distinct = 0;
        for (int count : histogram) {
            if (count > 0) {
                distinct++;
            }
        }
        System.out.printf("  distinct values: %d of 256%n", distinct);
        System.out.printf("  first 64 bytes : %s%n", BlockCheck.toHex(Arrays.copyOf(plain, 64)));
        System.out.println();
        if (zeros * 100 / plain.length > 5) {
            System.out.println("STRUCTURED: the cipher, key and chaining are correct");
        } else {
            System.out.println("NOT STRUCTURED: the key or the cipher is wrong");
        }
    }

    private StructureCheck() {
    }
}
