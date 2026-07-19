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
package org.openhab.binding.atv.internal.client.support;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;

/**
 * Transparent encryption layer using ChaCha20-Poly1305 AEAD.
 *
 * <p>
 * Two independent auto-incrementing message counters are kept, one for outgoing (encrypt)
 * and one for incoming (decrypt) messages.
 *
 * <p>
 * The nonce for each message is derived from the counter, encoded as a little-endian
 * integer of {@code nonceLength} bytes and left-padded with zero bytes up to the 12-byte
 * ChaCha20-Poly1305 nonce size:
 * <ul>
 * <li>8-byte mode (default; used by MRP and RAOP v2): nonce is 4 zero bytes followed by
 * the 8-byte little-endian counter.</li>
 * <li>12-byte mode (used by Companion): nonce is the 12-byte little-endian counter.</li>
 * <li>An explicit nonce may be passed per call (HAP pairing uses fixed ASCII nonces such
 * as {@code "PS-Msg05"}); a short explicit nonce is left-padded with zero bytes to
 * 12 bytes and the corresponding counter is <em>not</em> incremented.</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Chacha20Cipher {

    /** ChaCha20-Poly1305 nonce length in bytes. */
    public static final int NONCE_LENGTH = 12;

    private static final int MAC_SIZE_BITS = 128;

    private final byte[] outKey;
    private final byte[] inKey;
    private final int nonceLength;
    private long outCounter;
    private long inCounter;

    /**
     * Creates a new cipher in the default 8-byte nonce mode.
     *
     * @param outKey 32-byte key used to encrypt outgoing messages
     * @param inKey 32-byte key used to decrypt incoming messages
     */
    public Chacha20Cipher(byte[] outKey, byte[] inKey) {
        this(outKey, inKey, 8);
    }

    /**
     * Creates a new cipher.
     *
     * @param outKey 32-byte key used to encrypt outgoing messages
     * @param inKey 32-byte key used to decrypt incoming messages
     * @param nonceLength number of little-endian counter bytes in the nonce (1..12);
     *            shorter counters are left-padded with zero bytes to 12 bytes
     */
    public Chacha20Cipher(byte[] outKey, byte[] inKey, int nonceLength) {
        if (nonceLength < 1 || nonceLength > NONCE_LENGTH) {
            throw new IllegalArgumentException("nonceLength must be in [1, " + NONCE_LENGTH + "]: " + nonceLength);
        }
        this.outKey = outKey.clone();
        this.inKey = inKey.clone();
        this.nonceLength = nonceLength;
    }

    /**
     * Returns the nonce that will be used by {@link #encrypt(byte[])} in the <em>next</em>
     * call if no explicit nonce is specified. Always 12 bytes.
     */
    public byte[] outNonce() {
        return counterNonce(outCounter);
    }

    /**
     * Returns the nonce that will be used by {@link #decrypt(byte[])} in the <em>next</em>
     * call if no explicit nonce is specified. Always 12 bytes.
     */
    public byte[] inNonce() {
        return counterNonce(inCounter);
    }

    /**
     * Encrypts data using the current out counter as nonce, then increments the counter.
     */
    public byte[] encrypt(byte[] data) {
        return encrypt(data, null, null);
    }

    /**
     * Encrypts data with the counter-derived or an explicitly specified nonce.
     *
     * @param data plaintext to encrypt
     * @param nonce explicit nonce, or {@code null} to use the out counter (which is then
     *            incremented; an explicit nonce leaves the counter untouched). A nonce
     *            shorter than 12 bytes is left-padded with zero bytes.
     * @param aad additional authenticated data, or {@code null}
     * @return ciphertext followed by the 16-byte Poly1305 tag
     */
    public byte[] encrypt(byte[] data, byte @Nullable [] nonce, byte @Nullable [] aad) {
        if (nonce == null) {
            nonce = outNonce();
            outCounter++;
        } else if (nonce.length < NONCE_LENGTH) {
            nonce = padNonce(nonce);
        }
        return process(true, outKey, nonce, aad, data);
    }

    /**
     * Decrypts data using the current in counter as nonce, then increments the counter.
     */
    public byte[] decrypt(byte[] data) {
        return decrypt(data, null, null);
    }

    /**
     * Decrypts data with the counter-derived or an explicitly specified nonce.
     *
     * @param data ciphertext followed by the 16-byte Poly1305 tag
     * @param nonce explicit nonce, or {@code null} to use the in counter (which is then
     *            incremented; an explicit nonce leaves the counter untouched). A nonce
     *            shorter than 12 bytes is left-padded with zero bytes.
     * @param aad additional authenticated data, or {@code null}
     * @return decrypted plaintext
     * @throws AuthenticationError if the authentication tag does not verify
     */
    public byte[] decrypt(byte[] data, byte @Nullable [] nonce, byte @Nullable [] aad) {
        if (nonce == null) {
            nonce = inNonce();
            inCounter++;
        } else if (nonce.length < NONCE_LENGTH) {
            nonce = padNonce(nonce);
        }
        return process(false, inKey, nonce, aad, data);
    }

    private byte[] counterNonce(long counter) {
        byte[] nonce = new byte[NONCE_LENGTH];
        int offset = NONCE_LENGTH - nonceLength;
        // A long has at most 8 significant bytes; any higher-order counter bytes stay 0.
        for (int i = 0; i < Math.min(nonceLength, Long.BYTES); i++) {
            nonce[offset + i] = (byte) (counter >>> (8 * i));
        }
        return nonce;
    }

    private static byte[] padNonce(byte[] nonce) {
        byte[] padded = new byte[NONCE_LENGTH];
        System.arraycopy(nonce, 0, padded, NONCE_LENGTH - nonce.length, nonce.length);
        return padded;
    }

    private static byte[] process(boolean forEncryption, byte[] key, byte[] nonce, byte @Nullable [] aad, byte[] data) {
        ChaCha20Poly1305 engine = new ChaCha20Poly1305();
        engine.init(forEncryption, new AEADParameters(new KeyParameter(key), MAC_SIZE_BITS, nonce, aad));
        byte[] output = new byte[engine.getOutputSize(data.length)];
        int written = engine.processBytes(data, 0, data.length, output, 0);
        try {
            engine.doFinal(output, written);
        } catch (InvalidCipherTextException e) {
            throw new AuthenticationError("ChaCha20-Poly1305 authentication failed", e);
        }
        return output;
    }
}
