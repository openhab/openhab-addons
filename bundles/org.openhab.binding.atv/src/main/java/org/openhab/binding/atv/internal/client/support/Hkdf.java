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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HKDF-SHA512 key derivation (RFC 5869, extract-then-expand).
 *
 * <p>
 * Used throughout HAP pairing and MRP/AirPlay/Companion session key derivation, always
 * with a fixed output length of 32 bytes.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Hkdf {

    /** Output length in bytes used by all key derivations. */
    public static final int OUTPUT_LENGTH = 32;

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final int SHA512_LENGTH = 64;

    private Hkdf() {
    }

    /**
     * Derives a 32-byte key from a shared secret.
     *
     * @param salt HKDF salt, encoded as UTF-8 (e.g. {@code "Pair-Setup-Encrypt-Salt"})
     * @param info HKDF info, encoded as UTF-8
     * @param inputKeyMaterial the shared secret (IKM)
     * @return 32 bytes of output key material
     */
    public static byte[] expand(String salt, String info, byte[] inputKeyMaterial) {
        return expand(salt.getBytes(StandardCharsets.UTF_8), info.getBytes(StandardCharsets.UTF_8), inputKeyMaterial,
                OUTPUT_LENGTH);
    }

    /**
     * Derives key material with HKDF-SHA512.
     *
     * @param salt HKDF salt; an empty array is treated as a hash-length block of zeros
     *            per RFC 5869
     * @param info HKDF info (context)
     * @param inputKeyMaterial the shared secret (IKM)
     * @param length number of output bytes
     * @return {@code length} bytes of output key material
     */
    public static byte[] expand(byte[] salt, byte[] info, byte[] inputKeyMaterial, int length) {
        return derive(HMAC_SHA512, SHA512_LENGTH, salt, info, inputKeyMaterial, length);
    }

    /** RFC 5869 extract-then-expand for any HMAC algorithm; package-private for the RFC test vectors. */
    static byte[] derive(String macAlgorithm, int hashLength, byte[] salt, byte[] info, byte[] inputKeyMaterial,
            int length) {
        if (length < 0 || length > 255 * hashLength) {
            throw new IllegalArgumentException("HKDF output length out of range: " + length);
        }
        try {
            Mac mac = Mac.getInstance(macAlgorithm);
            // SecretKeySpec rejects an empty key; HMAC pads keys with zeros, so this is the same key.
            byte[] extractKey = salt.length == 0 ? new byte[hashLength] : salt;
            mac.init(new SecretKeySpec(extractKey, macAlgorithm));
            byte[] prk = mac.doFinal(inputKeyMaterial);

            mac.init(new SecretKeySpec(prk, macAlgorithm));
            byte[] output = new byte[length];
            byte[] block = new byte[0];
            int written = 0;
            for (int counter = 1; written < length; counter++) {
                mac.update(block);
                mac.update(info);
                mac.update((byte) counter);
                block = mac.doFinal();
                int chunk = Math.min(block.length, length - written);
                System.arraycopy(block, 0, output, written, chunk);
                written += chunk;
            }
            return output;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(macAlgorithm + " is not available", e);
        }
    }
}
