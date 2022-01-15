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
package org.openhab.binding.mihome.internal;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encrypts communication between openhab & xiaomi bridge (required by xiaomi).
 *
 * @author Ondřej Pečta - Initial contribution to Xiaomi MiHome Binding for OH 1.x
 * @author Dieter Schmidt - Refactor logger
 */
public class EncryptionHelper {

    protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    // AES‐CBC 128 initial vector, taken from protocol description
    protected static final byte[] IV = parseHexBinary("17996D093D28DDB3BA695A2E6F58562E");

    private final Logger logger = LoggerFactory.getLogger(EncryptionHelper.class);

    public String encrypt(String text, String key) {
        return encrypt(text, key, IV);
    }

    public String encrypt(String text, String key, byte[] iv) {
        IvParameterSpec vector = new IvParameterSpec(iv);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.warn("Failed to construct Cipher");
            return "";
        }
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, vector);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            logger.warn("Failed to init Cipher");
            return "";
        }
        byte[] encrypted;
        try {
            encrypted = cipher.doFinal(text.getBytes());
            return bytesToHex(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.warn("Failed to finally encrypt");
            return "";
        }
    }

    private static byte[] parseHexBinary(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);
        }

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i + 1));
            if (h == -1 || l == -1) {
                throw new IllegalArgumentException("contains illegal character for hexBinary: " + s);
            }

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
