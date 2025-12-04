/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 *
 * TODO
 * 
 * @author Simon Spielmann - Initial contribution
 */
public class SrpPassword {
    public final byte[] passwordHash;
    private byte[] salt;
    private Integer iterations;
    private Integer keyLength;

    public SrpPassword(String password) {
        this.passwordHash = sha256(password);
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public void setEncryptInfo(byte[] salt, int iterations, int keyLength) {
        this.salt = salt;
        this.iterations = iterations;
        this.keyLength = keyLength;
    }

    /*
     * public byte[] encode() {
     * if (salt == null || iterations == null || keyLength == null) {
     * throw new IllegalStateException("Encrypt info not set");
     * }
     * try {
     * String pseudoPassword = new String(passwordHash, StandardCharsets.ISO_8859_1);
     * PBEKeySpec spec = new PBEKeySpec(pseudoPassword.toCharArray(), salt, iterations, keyLength * 8);
     * SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
     * return skf.generateSecret(spec).getEncoded();
     * } catch (Exception e) {
     * throw new RuntimeException("Error during PBKDF2 encoding", e);
     * }
     * }
     */

    public byte[] encode() {
        if (salt == null || iterations == null || keyLength == null) {
            throw new IllegalStateException("Encrypt info not set");
        }
        try {

            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(passwordHash, salt, iterations);
            KeyParameter key = (KeyParameter) gen.generateDerivedParameters(keyLength * 8);
            return key.getKey();
        } catch (Exception e) {
            throw new RuntimeException("Error during PBKDF2 encoding", e);
        }
    }

    // Helper: Convert byte[] to char[] for PBEKeySpec
    private char[] toCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) (bytes[i] & 0xFF);
        }
        return chars;
    }
}
