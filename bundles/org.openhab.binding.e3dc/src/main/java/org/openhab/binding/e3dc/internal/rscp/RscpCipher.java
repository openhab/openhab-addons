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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rijndael-256 CBC chaining for RSCP. The key is the RSCP password truncated or padded with {@code 0xFF};
 * both chaining vectors start as 32 {@code 0xFF} bytes and are replaced by the last processed cipher block,
 * so the chaining state is carried across frames for the lifetime of a connection.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class RscpCipher {

    private static final int BLOCK = Rijndael256.BLOCK;

    private final Rijndael256 cipher;
    private final byte[] key;
    private final byte[] encryptIv = new byte[BLOCK];
    private final byte[] decryptIv = new byte[BLOCK];

    RscpCipher(byte[] password) {
        this.key = Rijndael256.keyFromPassword(password);
        this.cipher = new Rijndael256(key);
        Arrays.fill(encryptIv, (byte) 0xFF);
        Arrays.fill(decryptIv, (byte) 0xFF);
    }

    byte[] encrypt(byte[] plain) {
        int paddedLength = ((plain.length + BLOCK - 1) / BLOCK) * BLOCK;
        byte[] in = Arrays.copyOf(plain, paddedLength);
        byte[] out = new byte[paddedLength];
        byte[] block = new byte[BLOCK];
        for (int offset = 0; offset < paddedLength; offset += BLOCK) {
            System.arraycopy(in, offset, block, 0, BLOCK);
            Rijndael256.xorInto(block, encryptIv);
            byte[] encrypted = cipher.encrypt(key, block);
            System.arraycopy(encrypted, 0, out, offset, BLOCK);
            System.arraycopy(encrypted, 0, encryptIv, 0, BLOCK);
        }
        return out;
    }

    /** Decrypts whole blocks; trailing bytes that do not form a complete block are ignored. */
    byte[] decrypt(byte[] encrypted) {
        int length = (encrypted.length / BLOCK) * BLOCK;
        byte[] out = new byte[length];
        for (int offset = 0; offset < length; offset += BLOCK) {
            byte[] block = cipher.decrypt(key, Arrays.copyOfRange(encrypted, offset, offset + BLOCK));
            Rijndael256.xorInto(block, decryptIv);
            System.arraycopy(block, 0, out, offset, BLOCK);
            System.arraycopy(encrypted, offset, decryptIv, 0, BLOCK);
        }
        return out;
    }

    byte[] peek(byte[] encrypted) {
        byte[] iv = decryptIv.clone();
        int length = (encrypted.length / BLOCK) * BLOCK;
        byte[] out = new byte[length];
        for (int offset = 0; offset < length; offset += BLOCK) {
            byte[] block = cipher.decrypt(key, Arrays.copyOfRange(encrypted, offset, offset + BLOCK));
            Rijndael256.xorInto(block, iv);
            System.arraycopy(block, 0, out, offset, BLOCK);
            System.arraycopy(encrypted, offset, iv, 0, BLOCK);
        }
        return out;
    }
}
