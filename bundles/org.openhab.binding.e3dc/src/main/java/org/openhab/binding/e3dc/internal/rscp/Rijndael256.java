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

import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rijndael with a 256 bit block, the cipher the RSCP interface encrypts with.
 *
 * <p>
 * A 256 bit block is outside the AES standard, so the JDK cannot do it. BouncyCastle can, and its engine
 * is what devices of this kind interoperate with: for a 256 bit block it shifts rows 1 and 2 by seven
 * and five columns in the inverse direction rather than by one and three as the Rijndael specification
 * describes. A cipher written from the specification is internally consistent and still rejected by the
 * hardware, so the engine is used instead of a reimplementation.
 *
 * <p>
 * The block and the key are both 32 bytes. The key material is the RSCP password padded with
 * {@code 0xFF}, and a connection starts with a chaining vector of 32 {@code 0xFF} bytes.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class Rijndael256 {

    /** Block and key length in bytes. */
    static final int BLOCK = 32;

    private final RijndaelEngine engine;

    /**
     * Creates a cipher from raw key material.
     *
     * @param key the key, which must be {@value #BLOCK} bytes
     */
    Rijndael256(byte[] key) {
        if (key.length != BLOCK) {
            throw new IllegalArgumentException("The key must be " + BLOCK + " bytes long");
        }
        engine = new RijndaelEngine(BLOCK * 8);
    }

    /**
     * Derives the key from the RSCP password: the password bytes followed by {@code 0xFF} up to the key
     * length, or truncated to it for a longer password.
     */
    static byte[] keyFromPassword(byte[] password) {
        byte[] key = new byte[BLOCK];
        Arrays.fill(key, (byte) 0xFF);
        System.arraycopy(password, 0, key, 0, Math.min(password.length, BLOCK));
        return key;
    }

    /** Encrypts one block. */
    byte[] encrypt(byte[] key, byte[] block) {
        return process(key, block, true);
    }

    /** Decrypts one block. */
    byte[] decrypt(byte[] key, byte[] block) {
        return process(key, block, false);
    }

    /**
     * Runs one block through the engine.
     *
     * <p>
     * The engine keeps no state between blocks once initialised, so it is configured per call and the
     * chaining stays in the caller, where the connection lifetime is known.
     */
    private byte[] process(byte[] key, byte[] block, boolean encrypt) {
        engine.init(encrypt, new KeyParameter(key));
        byte[] out = new byte[BLOCK];
        engine.processBlock(block, 0, out, 0);
        return out;
    }

    /** Exposed so the XOR of two byte arrays used by the mode can stay local to this package. */
    static void xorInto(byte[] target, byte[] source) {
        for (int i = 0; i < target.length; i++) {
            target[i] ^= source[i];
        }
    }
}
