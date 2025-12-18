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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * Helper class to take a raw password, and prepare it for use in SRP Authentication.
 * 
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class SrpPassword {
    public final byte[] passwordHash;
    private byte @Nullable [] salt;
    private @Nullable Integer iterations;
    private @Nullable Integer keyLength;

    /**
     * 
     * @param password The unhashed password
     */
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

    /**
     * Set the parameters required for PBKDF2 encoding.
     * 
     * @param salt
     * @param iterations
     * @param keyLength
     */
    public void setEncryptInfo(byte[] salt, int iterations, int keyLength) {
        this.salt = salt;
        this.iterations = iterations;
        this.keyLength = keyLength;
    }

    /**
     * Encode the password using PBKDF2 with the previously set parameters.
     * 
     * @return The encoded password
     */
    public byte[] encode() {
        byte[] salt = this.salt;
        Integer iterations = this.iterations;
        Integer keyLength = this.keyLength;
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
