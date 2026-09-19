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
package org.openhab.binding.tapocontrol.internal.api.camera;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Pure crypto helper functions for the Tapo camera local API. All hashes are uppercase hex;
 * the MD5-based password hash is zero-padded to 32 characters. Formulas follow the proven
 * behavior of current Tapo camera firmware.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public final class TapoCameraCrypto {
    private static final int HASH_HEX_LENGTH = 32;

    private TapoCameraCrypto() {
    }

    public static String md5HashUpper(String input) {
        return toUpperHex(digest("MD5", input));
    }

    /** MD5 hash of the raw password, zero-padded to 32 chars (legacy login). */
    public static String legacyPasswordHash(String password) {
        return md5HashUpper(password);
    }

    public static String sha256HexUpper(String input) {
        return toUpperHex(digest("SHA-256", input));
    }

    /** SHA-256 hash of the raw password (secure login). */
    public static String securePasswordHash(String password) {
        return sha256HexUpper(password);
    }

    /** Expected device_confirm response: SHA256(cnonce ‖ passwordHash ‖ nonce) ‖ nonce ‖ cnonce. */
    public static String computeDeviceConfirm(String passwordHash, String cnonce, String nonce) {
        return sha256HexUpper(cnonce + passwordHash + nonce) + nonce + cnonce;
    }

    /** Validates the device_confirm sent by the camera before credentials are transmitted. */
    public static boolean validateDeviceConfirm(String passwordHash, String cnonce, String nonce,
            String deviceConfirm) {
        return computeDeviceConfirm(passwordHash, cnonce, nonce).equals(deviceConfirm);
    }

    /** Login credential: SHA256(passwordHash ‖ cnonce ‖ nonce) ‖ cnonce ‖ nonce. */
    public static String computeDigestPasswd(String passwordHash, String cnonce, String nonce) {
        return sha256HexUpper(passwordHash + cnonce + nonce) + cnonce + nonce;
    }

    public static String deriveKeyToken(String prefix, String passwordHash, String cnonce, String nonce) {
        String hashedKey = sha256HexUpper(cnonce + passwordHash + nonce);
        return sha256HexUpper(prefix + cnonce + nonce + hashedKey).substring(0, HASH_HEX_LENGTH);
    }

    public static String deriveLsk(String passwordHash, String cnonce, String nonce) {
        return deriveKeyToken("lsk", passwordHash, cnonce, nonce);
    }

    public static String deriveIvb(String passwordHash, String cnonce, String nonce) {
        return deriveKeyToken("ivb", passwordHash, cnonce, nonce);
    }

    /** Request integrity header value: SHA256(SHA256(passwordHash ‖ cnonce) ‖ envelopeJson ‖ seq). */
    public static String computeTapoTag(String passwordHash, String cnonce, String envelopeJson, long seq) {
        String innerHash = sha256HexUpper(passwordHash + cnonce);
        return sha256HexUpper(innerHash + envelopeJson + seq);
    }

    public static String encryptBase64(String plainText, String hexKey, String hexIv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(hexToBytes(hexKey), "AES"),
                new IvParameterSpec(hexToBytes(hexIv)));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptBase64(String cipherTextBase64, String hexKey, String hexIv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(hexToBytes(hexKey), "AES"),
                new IvParameterSpec(hexToBytes(hexIv)));
        byte[] decoded = Base64.getDecoder().decode(cipherTextBase64);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    private static byte[] digest(String algorithm, String input) {
        try {
            return MessageDigest.getInstance(algorithm).digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM does not provide " + algorithm, e);
        }
    }

    private static String toUpperHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        String hex = sb.toString().toUpperCase();
        while (hex.length() < HASH_HEX_LENGTH) {
            hex = "0" + hex;
        }
        return hex;
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) ((Character.digit(hex.charAt(i * 2), 16) << 4)
                    + Character.digit(hex.charAt(i * 2 + 1), 16));
        }
        return out;
    }
}
