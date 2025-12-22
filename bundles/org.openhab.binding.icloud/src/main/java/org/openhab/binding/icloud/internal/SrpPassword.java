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
 * SrpPassword represents a password for SRP authentication.
 *
 * @author Simon Spielmann - Initial contribution
 */
public class SrpPassword {
    private final byte[] passwordHash;
    private byte[] salt;
    private Integer iterations;
    private Integer keyLength;

    /**
     * Constructor for SrpPassword.
     *
     * @param password the password as a String
     */
    public SrpPassword(String password) {
        this.passwordHash = sha256(password);
    }

    /**
     * Calculates the SHA-256 hash of the input string.
     *
     * @param input the input string
     * @return the SHA-256 hash as a byte array
     */
    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Sets the encryption information.
     *
     * @param salt The salt
     * @param iterations Number of iterations
     * @param keyLength Key length
     */
    public void setEncryptInfo(byte[] salt, int iterations, int keyLength) {
        this.salt = salt;
        this.iterations = iterations;
        this.keyLength = keyLength;
    }

    /**
     * Encodes the password using PBKDF2 with the provided encryption information.
     *
     * @return The encoded password as a byte array
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
}
