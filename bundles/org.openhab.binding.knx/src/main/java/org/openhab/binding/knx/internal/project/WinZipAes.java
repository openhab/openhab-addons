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
package org.openhab.binding.knx.internal.project;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The key derivation of a protected ETS 6 project and the WinZip AES primitives (format note §2.1).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class WinZipAes {

    static final int AUTHENTICATION_CODE_LENGTH = 10;
    static final int VERIFIER_LENGTH = 2;
    static final int ZIP_KEY_ITERATIONS = 1000;

    private static final byte[] ETS_SALT = "21.project.ets.knx.org".getBytes(StandardCharsets.US_ASCII);
    private static final int ETS_ITERATIONS = 65536;
    private static final int ETS_KEY_LENGTH = 32;

    private WinZipAes() {
    }

    /**
     * The password of the nested archive: the ASCII bytes of Base64(PBKDF2-HMAC-SHA256(UTF-16LE bytes of the
     * project password, "21.project.ets.knx.org", 65536, 32)). The caller overwrites the result when done.
     */
    static byte[] etsZipPassword(char[] password) {
        byte[] utf16 = new byte[password.length * 2];
        byte[] key = null;
        try {
            for (int i = 0; i < password.length; i++) {
                utf16[2 * i] = (byte) password[i];
                utf16[2 * i + 1] = (byte) (password[i] >>> 8);
            }
            key = pbkdf2("HmacSHA256", utf16, ETS_SALT, ETS_ITERATIONS, ETS_KEY_LENGTH);
            return Base64.getEncoder().encode(key);
        } finally {
            Arrays.fill(utf16, (byte) 0);
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
        }
    }

    /**
     * PBKDF2 (RFC 8018 §5.2) over raw password bytes. The JDK's own PBKDF2 takes chars and encodes them as UTF-8,
     * which does not give the UTF-16LE bytes ETS hashes.
     *
     * @param password at least one byte
     */
    static byte[] pbkdf2(String macAlgorithm, byte[] password, byte[] salt, int iterations, int length) {
        Mac mac = mac(macAlgorithm, password, 0, password.length);
        int blockLength = mac.getMacLength();
        byte[] result = new byte[length];
        byte[] u = new byte[blockLength];
        byte[] t = new byte[blockLength];
        try {
            for (int block = 1, offset = 0; offset < length; block++, offset += blockLength) {
                mac.update(salt);
                mac.update(new byte[] { (byte) (block >>> 24), (byte) (block >>> 16), (byte) (block >>> 8),
                        (byte) block });
                mac.doFinal(u, 0);
                System.arraycopy(u, 0, t, 0, blockLength);
                for (int i = 1; i < iterations; i++) {
                    mac.update(u);
                    mac.doFinal(u, 0);
                    for (int j = 0; j < blockLength; j++) {
                        t[j] ^= u[j];
                    }
                }
                System.arraycopy(t, 0, result, offset, Math.min(blockLength, length - offset));
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 failed", e);
        } finally {
            Arrays.fill(u, (byte) 0);
            Arrays.fill(t, (byte) 0);
        }
    }

    /** The first 10 bytes of HMAC-SHA1 over the data, with the key taken from a slice of {@code key}. */
    static byte[] authenticationCode(byte[] key, int keyOffset, int keyLength, byte[] data, int offset, int length) {
        Mac mac = mac("HmacSHA1", key, keyOffset, keyLength);
        mac.update(data, offset, length);
        byte[] code = mac.doFinal();
        try {
            return Arrays.copyOf(code, AUTHENTICATION_CODE_LENGTH);
        } finally {
            Arrays.fill(code, (byte) 0);
        }
    }

    /** AES on single blocks, for the counter mode of {@link AesCtrInputStream}. */
    static Cipher blockCipher(byte[] key, int offset, int length) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, offset, length, "AES"));
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES is not available", e);
        }
    }

    private static Mac mac(String algorithm, byte[] key, int offset, int length) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, offset, length, algorithm));
            return mac;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(algorithm + " is not available", e);
        }
    }
}
