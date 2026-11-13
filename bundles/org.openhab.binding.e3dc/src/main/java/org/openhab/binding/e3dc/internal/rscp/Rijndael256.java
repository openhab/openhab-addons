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
 * Rijndael with a 256 bit block, the cipher the RSCP interface encrypts with. Outside the AES standard, so
 * BouncyCastle's engine is used: a cipher from the specification is rejected by the hardware.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class Rijndael256 {

    static final int BLOCK = 32;

    private final RijndaelEngine engine;

    Rijndael256(byte[] key) {
        if (key.length != BLOCK) {
            throw new IllegalArgumentException("The key must be " + BLOCK + " bytes long");
        }
        engine = new RijndaelEngine(BLOCK * 8);
    }

    static byte[] keyFromPassword(byte[] password) {
        byte[] key = new byte[BLOCK];
        Arrays.fill(key, (byte) 0xFF);
        System.arraycopy(password, 0, key, 0, Math.min(password.length, BLOCK));
        return key;
    }

    byte[] encrypt(byte[] key, byte[] block) {
        return process(key, block, true);
    }

    byte[] decrypt(byte[] key, byte[] block) {
        return process(key, block, false);
    }

    private byte[] process(byte[] key, byte[] block, boolean encrypt) {
        engine.init(encrypt, new KeyParameter(key));
        byte[] out = new byte[BLOCK];
        engine.processBlock(block, 0, out, 0);
        return out;
    }

    static void xorInto(byte[] target, byte[] source) {
        for (int i = 0; i < target.length; i++) {
            target[i] ^= source[i];
        }
    }
}
