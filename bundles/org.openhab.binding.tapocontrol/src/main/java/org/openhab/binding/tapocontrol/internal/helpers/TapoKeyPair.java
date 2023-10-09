/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    private String publicKey = "";
    private String privateKey = "";

    private final String ALGORITHM = "RSA";
    private final int KEYSIZE = 1024;

    public TapoKeyPair() {
        createKeyPair();
    }

    /**
     * Create Key-Pairs
     *
     */
    private void createKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEYSIZE, new SecureRandom());
            KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();

            publicKey = new String(getMimeEncoder().encode(((RSAPublicKey) generateKeyPair.getPublic()).getEncoded()));
            privateKey = new String(
                    getMimeEncoder().encode(((RSAPrivateKey) generateKeyPair.getPrivate()).getEncoded()));
        } catch (Exception e) {
            publicKey = "";
            privateKey = "";
        }
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    /**
     * get Private-Key
     * 
     * @return String -----BEGIN PRIVATE KEY-----\n%s\n-----END PRIVATE KEY-----
     */
    public String getPrivateKey() {
        return String.format("-----BEGIN PRIVATE KEY-----%n%s%n-----END PRIVATE KEY-----%n", privateKey);
    }

    /**
     * get Public-Key
     * 
     * @return String -----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----
     */
    public String getPublicKey() {
        return String.format("-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----%n", publicKey);
    }

    /**
     * get Private-Key as byte-array
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPrivateKeyBytes() {
        try {
            return privateKey.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * get Public-Key as byte-array
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPublicKeyBytes() {
        try {
            return publicKey.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
