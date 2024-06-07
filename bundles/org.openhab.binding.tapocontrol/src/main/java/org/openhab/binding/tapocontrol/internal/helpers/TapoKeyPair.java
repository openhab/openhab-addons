/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.helpers;

import static java.util.Base64.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tapo RSA-KeyPair Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoKeyPair {
    private static final String ALGORITHM_RSA = "RSA";
    private static final String LINE_SEPARATOR = "\n";
    private static final int LINE_LENGTH = 64;
    private byte[] publicKeyBytes = {};
    private byte[] privateKeyBytes = {};

    public TapoKeyPair(int keysize) {
        try {
            createNewKeyPair(ALGORITHM_RSA, keysize);
        } catch (Exception e) {
            publicKeyBytes = new byte[0];
            privateKeyBytes = new byte[0];
        }
    }

    public TapoKeyPair(String algorithm, int keysize) throws NoSuchAlgorithmException {
        createNewKeyPair(algorithm, keysize);
    }

    /**
     * Create Key-Pairs
     *
     */
    public void createNewKeyPair(String algorithm, int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keysize, new SecureRandom());
        KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();
        Encoder mimEncoder = getMimeEncoder(LINE_LENGTH, LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8));

        publicKeyBytes = mimEncoder.encode(((RSAPublicKey) generateKeyPair.getPublic()).getEncoded());
        privateKeyBytes = mimEncoder.encode(((RSAPrivateKey) generateKeyPair.getPrivate()).getEncoded());
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    /**
     * get PEM-formated Private-Key
     * 
     * @return String -----BEGIN PRIVATE KEY-----\n%s\n-----END PRIVATE KEY-----
     */
    public String getPrivateKey() {
        return String.format("-----BEGIN PRIVATE KEY-----%2$s%1$s%2$s-----END PRIVATE KEY-----%2$s",
                new String(privateKeyBytes), LINE_SEPARATOR);
    }

    /**
     * get PEM-formated Public-Key
     * 
     * @return String -----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----
     */
    public String getPublicKey() {
        return String.format("-----BEGIN PUBLIC KEY-----%2$s%1$s%2$s-----END PUBLIC KEY-----%2$s",
                new String(publicKeyBytes), LINE_SEPARATOR);
    }

    /**
     * get Private-Key as byte-array
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPrivateKeyBytes() {
        return privateKeyBytes;
    }

    /**
     * get Public-Key as byte-array
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPublicKeyBytes() {
        return publicKeyBytes;
    }
}
