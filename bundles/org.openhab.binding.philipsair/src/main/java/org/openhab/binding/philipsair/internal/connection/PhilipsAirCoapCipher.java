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
package org.openhab.binding.philipsair.internal.connection;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;

/**
 * Performs supporting crypto for coap protocol messages
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class PhilipsAirCoapCipher {
    private static final String SHARED_SECRET = "JiangPan";
    private static final int COUNTER_LENGTH = 8;
    private static final int HASH_LENGTH = 64;

    public static String decryptMsg(@Nullable String responseText, Logger logger) {
        if (responseText == null || responseText.isBlank()) {
            return "";
        }
        if (responseText.length() < COUNTER_LENGTH + HASH_LENGTH) {
            logger.debug("Message too short to decrypt: '{}'", responseText);
            return "";
        }
        String counter = responseText.substring(0, COUNTER_LENGTH);
        String hash = responseText.substring(responseText.length() - HASH_LENGTH);
        String encodedMessageAndCounter = responseText.substring(0, responseText.length() - HASH_LENGTH);
        String encodedMessage = responseText.substring(COUNTER_LENGTH, responseText.length() - HASH_LENGTH);
        try {
            String calculatedHash = toSHA(encodedMessageAndCounter);
            if (!hash.equalsIgnoreCase(calculatedHash)) {
                // the hash is unkeyed, it only detects corrupted messages
                logger.debug("Ignoring message with hash mismatch, expected '{}' got '{}'", hash, calculatedHash);
                return "";
            }
            String keyAndIv = toMD5(SHARED_SECRET + counter);
            String secret = keyAndIv.substring(0, keyAndIv.length() / 2);
            String iv = keyAndIv.substring(keyAndIv.length() / 2);
            String decryptedMsg = decrypt(encodedMessage, secret, iv);
            if (logger.isTraceEnabled()) {
                logger.trace("Decrypting: '{}'", responseText);
                logger.trace("Seq: {}, encodedMessage: {}", counter, encodedMessage);
                logger.trace("Calculated Hash: {}, Message Hash {}", calculatedHash, hash);
                logger.trace("Decrypted: {}", decryptedMsg);
            }
            return decryptedMsg;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
                | IllegalArgumentException e) {
            logger.debug("Error decrypting message: {}. Message Text: '{}'", e.getMessage(), responseText);
        }
        return "";
    }

    public @Nullable static String encryptedMsg(String commandText, long counter, Logger logger) {
        return encryptedMsg(commandText, String.format("%08X", counter), logger);
    }

    private static @Nullable String encryptedMsg(String commandText, String sequence, Logger logger) {
        try {
            String keyAndIv = toMD5(SHARED_SECRET + sequence);
            String secret = keyAndIv.substring(0, keyAndIv.length() / 2);
            String iv = keyAndIv.substring(keyAndIv.length() / 2);
            String encryptedCmd = encrypt(commandText, secret, iv);
            String calculatedHash = toSHA(sequence + encryptedCmd);
            String encrypedMessage = sequence + encryptedCmd + calculatedHash;
            if (logger.isTraceEnabled()) {
                logger.trace("Encrypting: '{}'", commandText);
                logger.trace("Encrypted: {}", encryptedCmd);
                logger.trace("Hash: {}", calculatedHash);
                logger.trace("Encypted message: {}", encrypedMessage);
            }
            return encrypedMessage;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            logger.trace("Error decoding message: {}", e.getMessage(), e);
        }
        return null;
    }

    private static String toSHA(String originalString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
        return HexUtils.bytesToHex(encodedhash);
    }

    private static String toMD5(String originalString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
        return HexUtils.bytesToHex(encodedhash);
    }

    public static String decrypt(String strToDecrypt, String secret, String iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        return new String(cipher.doFinal(HexUtils.hexToBytes(strToDecrypt)), StandardCharsets.UTF_8);
    }

    public static String encrypt(String strToEncrypt, String secret, String iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        return HexUtils.bytesToHex(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }
}
