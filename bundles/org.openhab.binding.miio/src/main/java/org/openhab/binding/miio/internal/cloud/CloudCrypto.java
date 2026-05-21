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
package org.openhab.binding.miio.internal.cloud;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.miio.internal.MiIoCryptoException;

/**
 * The {@link CloudCrypto} is responsible for encryption for Xiaomi cloud communication.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudCrypto {

    /**
     * Compute SHA256 hash value for the byte array
     *
     * @param inBytes ByteArray to be hashed
     * @return BASE64 encoded hash value
     * @throws MiIoCryptoException
     */
    public static String sha256Hash(byte[] inBytes) throws MiIoCryptoException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(inBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new MiIoCryptoException(e.getMessage(), e);
        }
    }

    /**
     * Compute HmacSHA256 hash value for the byte array
     *
     * @param key for encoding
     * @param cipherText ByteArray to be encoded
     * @return BASE64 encoded hash value
     * @throws MiIoCryptoException
     */
    public static String hMacSha256Encode(byte[] key, byte[] cipherText) throws MiIoCryptoException {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
            sha256Hmac.init(secretKey);
            return Base64.getEncoder().encodeToString(sha256Hmac.doFinal(cipherText));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MiIoCryptoException(e.getMessage(), e);
        }
    }

    /**
     * Encrypts a payload using RC4, discarding the first 1024 bytes of the keystream.
     *
     * @param password The Base64 encoded key.
     * @param payload The string payload to encrypt.
     * @return The Base64 encoded encrypted payload.
     * @throws MiIoCryptoException if an error occurs during encryption.
     */
    public static String encryptRc4(String password, String payload) throws MiIoCryptoException {
        try {
            byte[] key = Base64.getDecoder().decode(password);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");

            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            // Discard the first 1024 bytes of the keystream
            byte[] discard = new byte[1024];
            cipher.update(discard);

            byte[] encryptedPayload = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedPayload);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new MiIoCryptoException(e.getMessage(), e);
        }
    }

    /**
     * Decrypts an RC4 encrypted payload, discarding the first 1024 bytes of the keystream.
     *
     * @param password The Base64 encoded key.
     * @param payload The Base64 encoded encrypted payload.
     * @return The decrypted payload as a byte array.
     * @throws MiIoCryptoException if an error occurs during decryption.
     */
    public static byte[] decryptRc4(String password, String payload) throws MiIoCryptoException {
        try {
            byte[] key = Base64.getDecoder().decode(password);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "RC4");

            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            // Discard the first 1024 bytes of the keystream
            byte[] discard = new byte[1024];
            cipher.update(discard);

            byte[] decodedPayload = Base64.getDecoder().decode(payload);
            return cipher.doFinal(decodedPayload);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new MiIoCryptoException(e.getMessage(), e);
        }
    }
}
