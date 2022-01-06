/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
}
