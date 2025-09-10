/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal;

import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * ChaCha20 encryption and decryption utility class.
 * Uses BouncyCastle's ChaCha20Poly1305 implementation.
 * Requires a 32-byte key and a 12-byte nonce.
 * The nonce must be unique for each encryption operation with the same key.
 * The ciphertext includes the authentication tag.
 * See RFC 8439 for more details.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class ChaCha20 {

    /**
     * Encrypts the given plaintext using ChaCha20-Poly1305.
     *
     * @param key 32-byte encryption key
     * @param nonce 12-byte nonce
     * @param plaintext data to encrypt
     * @return encrypted data (ciphertext + authentication tag)
     */
    public static byte[] encrypt(byte[] key, byte[] nonce, byte[] plaintext) {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
            cipher.init(true, params);

            byte[] out = new byte[cipher.getOutputSize(plaintext.length)];
            int len = cipher.processBytes(plaintext, 0, plaintext.length, out, 0);
            cipher.doFinal(out, len);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts the given ciphertext using ChaCha20-Poly1305.
     *
     * @param key 32-byte decryption key
     * @param nonce 12-byte nonce
     * @param ciphertext data to decrypt (ciphertext + authentication tag)
     * @return decrypted data (plaintext)
     */
    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] ciphertext) {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
            cipher.init(false, params);

            byte[] out = new byte[cipher.getOutputSize(ciphertext.length)];
            int len = cipher.processBytes(ciphertext, 0, ciphertext.length, out, 0);
            cipher.doFinal(out, len);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}