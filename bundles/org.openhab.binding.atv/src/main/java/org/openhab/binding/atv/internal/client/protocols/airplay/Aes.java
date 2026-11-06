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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Minimal AES helpers for AirPlay legacy device authentication.
 *
 * <p>
 * Encryption is done incrementally: each input chunk is fed through the cipher and only the
 * ciphertext of the <em>last</em> chunk is returned (earlier chunks merely advance the
 * keystream). AirPlay legacy uses this with AES-128-CTR for the pair-verify signature and
 * AES-128-GCM for the pair-setup EPK step.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class Aes {

    /** GCM authentication tag length in bytes (the standard default). */
    static final int GCM_TAG_LENGTH = 16;

    private Aes() {
    }

    /**
     * Encrypts chunks with AES-CTR and returns the ciphertext of the last chunk only:
     * earlier chunks advance the keystream but their ciphertext is discarded.
     *
     * @param key the AES key (16 bytes for AirPlay legacy)
     * @param iv the 16-byte counter/IV block
     * @param chunks the plaintext chunks
     * @return ciphertext of the last chunk (empty when no chunks are given)
     */
    static byte[] ctrEncryptLast(byte[] key, byte[] iv, byte[]... chunks) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] result = new byte[0];
            for (byte[] chunk : chunks) {
                byte[] updated = cipher.update(chunk);
                result = updated == null ? new byte[0] : updated;
            }
            cipher.doFinal();
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-CTR encryption failed", e);
        }
    }

    /**
     * Ciphertext and authentication tag produced by {@link #gcmEncrypt(byte[], byte[], byte[])}.
     *
     * @param ciphertext the encrypted data (same length as the plaintext)
     * @param tag the 16-byte GCM authentication tag
     */
    record GcmResult(byte[] ciphertext, byte[] tag) {
    }

    /**
     * Encrypts data with AES-GCM (no AAD).
     *
     * @param key the AES key (16 bytes for AirPlay legacy)
     * @param iv the 16-byte IV (AirPlay legacy uses a full SHA-512 derived block)
     * @param data the plaintext
     * @return ciphertext and authentication tag
     */
    static GcmResult gcmEncrypt(byte[] key, byte[] iv, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            byte[] combined = cipher.doFinal(data);
            byte[] ciphertext = Arrays.copyOfRange(combined, 0, combined.length - GCM_TAG_LENGTH);
            byte[] tag = Arrays.copyOfRange(combined, combined.length - GCM_TAG_LENGTH, combined.length);
            return new GcmResult(ciphertext, tag);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-GCM encryption failed", e);
        }
    }
}
