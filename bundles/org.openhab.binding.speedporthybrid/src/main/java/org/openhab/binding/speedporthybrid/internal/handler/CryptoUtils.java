/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.handler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for cryptographic operations.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class CryptoUtils {

    private final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    public static final CryptoUtils INSTANCE = new CryptoUtils();

    private CryptoUtils() {
        // prohibit external instantiation
    }

    @Nullable
    public String hashPassword(@Nullable String challengev, @Nullable String password) {
        String pass = challengev + ":" + password;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(pass.getBytes());

            return hexToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Error creating SHA256 hash.", e);
        }

        return null;
    }

    public byte[] encrypt(@Nullable String challengev, byte[] derivedKey, String data)
            throws DecoderException, IllegalStateException, InvalidCipherTextException {
        if (challengev == null || derivedKey.length <= 0) {
            throw new IllegalArgumentException(
                    "Invalid auth argument: challengev and derivedKey must not be null or empty.");
        }
        byte[] iv = hexToBytes(challengev.substring(16, 32));
        byte[] authData = hexToBytes(challengev.substring(32, 48));

        byte[] inputData = data.getBytes();

        ParametersWithIV params = new ParametersWithIV(new KeyParameter(derivedKey), iv);

        CCMBlockCipher cipher = new CCMBlockCipher(new AESEngine());
        cipher.init(true, params);
        byte[] outputText = new byte[cipher.getOutputSize(inputData.length)];
        int outputLen = cipher.processBytes(inputData, 0, inputData.length, outputText, 0);
        cipher.processAADBytes(authData, 0, authData.length);
        cipher.doFinal(outputText, outputLen);

        return hexToString(outputText).getBytes();
    }

    public @Nullable String deriveKey(@Nullable String challengev, @Nullable String password) {
        if (challengev == null || password == null) {
            throw new IllegalArgumentException("Invalid arguments: challengev and password must not be null.");
        }

        try {
            byte[] salt = challengev.substring(0, 16).getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordHash = digest.digest(password.getBytes());

            int iterations = 1000;
            PBEKeySpec spec = new PBEKeySpec(Hex.encodeHex(passwordHash), salt, iterations, 128);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return hexToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

        }

        return null;
    }

    private byte[] hexToBytes(String hex) throws DecoderException {
        return Hex.decodeHex(hex.toCharArray());
    }

    private String hexToString(byte[] hex) {
        return new String(Hex.encodeHex(hex));
    }

}
