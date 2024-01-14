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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO Credentials
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoCredentials {

    private final Logger logger = LoggerFactory.getLogger(TapoCredentials.class);
    private MimeEncode mimeEncoder;
    private String encodedPassword = "";
    private String encodedEmail = "";
    private String publicKey = "";
    private String privateKey = "";
    private String username = "";
    private String password = "";

    /**
     * INIT CLASS
     *
     */
    public TapoCredentials() {
        this.mimeEncoder = new MimeEncode();
    }

    /**
     * INIT CLASS
     *
     * @param eMail E-Mail-adress of Tapo Cloud
     * @param password Password of Tapo Cloud
     */
    public TapoCredentials(String eMail, String password) {
        this.mimeEncoder = new MimeEncode();
        setCredectials(eMail, password);
    }

    /**
     * set credentials.
     *
     * @param eMail username (eMail-adress) of Tapo Cloud
     * @param password Password of Tapo Cloud
     */
    public void setCredectials(String eMail, String password) {
        try {
            this.username = eMail;
            this.password = password;
            encryptCredentials(eMail, password);
            createKeyPair();
        } catch (Exception e) {
            logger.warn("error init credential class '{}'", e.toString());
        }
    }

    /**
     * encrypt credentials.
     *
     * @param username username (eMail-adress) of Tapo Cloud
     * @param passowrd Password of Tapo Cloud
     */
    private void encryptCredentials(String username, String password) throws Exception {
        logger.trace("encrypt credentials for '{}'", username);

        /* Password Encoding */
        byte[] byteWord = password.getBytes();
        this.encodedPassword = mimeEncoder.encodeToString(byteWord);

        /* User Encoding */
        String encodedUser = this.shaDigestUsername(username);
        byteWord = encodedUser.getBytes("UTF-8");
        this.encodedEmail = mimeEncoder.encodeToString(byteWord);
    }

    /**
     * Create Key-Pairs
     *
     */
    public void createKeyPair() throws NoSuchAlgorithmException {
        logger.trace("generating new keypair");
        KeyPairGenerator instance = KeyPairGenerator.getInstance("RSA");
        instance.initialize(1024, new SecureRandom());
        KeyPair generateKeyPair = instance.generateKeyPair();

        this.publicKey = new String(mimeEncoder.encode(((RSAPublicKey) generateKeyPair.getPublic()).getEncoded()));
        this.privateKey = new String(mimeEncoder.encode(((RSAPrivateKey) generateKeyPair.getPrivate()).getEncoded()));
        logger.trace("new privateKey: '{}'", this.privateKey);
        logger.trace("new ublicKey: '{}'", this.publicKey);
    }

    /**
     * shaDigest USERNAME
     *
     */
    private String shaDigestUsername(String str) throws NoSuchAlgorithmException {
        byte[] bArr = str.getBytes();
        byte[] digest = MessageDigest.getInstance("SHA1").digest(bArr);

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append("0");
                sb.append(hexString);
            } else {
                sb.append(hexString);
            }
        }
        return sb.toString();
    }

    /**
     * RETURN ENCODED PASSWORD
     *
     */
    public String getEncodedPassword() {
        return encodedPassword;
    }

    /**
     * RETURN ENCODED E-MAIL
     *
     */
    public String getEncodedEmail() {
        return encodedEmail;
    }

    /**
     * RETURN PASSWORD
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * RETURN Username (E-MAIL)
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     * RETURN PRIVATE-KEY
     * 
     * @return String -----BEGIN PRIVATE KEY-----\n%s\n-----END PRIVATE KEY-----
     */
    public String getPrivateKey() {
        return String.format("-----BEGIN PRIVATE KEY-----%n%s%n-----END PRIVATE KEY-----%n", privateKey);
    }

    /**
     * RETURN PUBLIC KEY
     * 
     * @return String -----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----
     */
    public String getPublicKey() {
        return String.format("-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----%n", publicKey);
    }

    /**
     * RETURN PRIVATE-KEY (BYTES)
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPrivateKeyBytes() {
        try {
            return privateKey.getBytes("UTF-8");
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * RETURN PUBLIC-KEY (BYTES)
     * 
     * @return UTF-8 coded byte[] with private key
     */
    public byte[] getPublicKeyBytes() {
        try {
            return publicKey.getBytes("UTF-8");
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * CHECK IF CREDENTIALS ARE SET
     * 
     * @return
     */
    public Boolean areSet() {
        return !(this.username.isEmpty() || this.password.isEmpty());
    }
}
