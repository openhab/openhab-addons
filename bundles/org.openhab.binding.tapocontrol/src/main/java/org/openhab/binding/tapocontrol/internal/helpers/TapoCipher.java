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
package org.openhab.binding.tapocontrol.internal.helpers;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO-CIPHER
 * Based on K4CZP3R's p100-java-poc
 * 
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TapoCipher {
    private final Logger logger = LoggerFactory.getLogger(TapoCipher.class);
    protected static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    protected static final String CIPHER_ALGORITHM = "AES";
    protected static final String CIPHER_CHARSET = "UTF-8";
    protected static final String HANDSHAKE_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    protected static final String HANDSHAKE_ALGORITHM = "RSA";
    protected static final String HANDSHAKE_CHARSET = "UTF-8";

    @NonNullByDefault({})
    private Cipher encodeCipher;
    @NonNullByDefault({})
    private Cipher decodeCipher;
    @NonNullByDefault({})
    private MimeEncode mimeEncode;

    /**
     * CREATE NEW EMPTY CIPHER
     */
    public TapoCipher() {
    }

    /**
     * CREATE NEW CIPHER WITH KEY AND CREDENTIALS
     * 
     * @param handshakeKey Key from Handshake-Request
     * @param credentials TapoCredentials
     * @throws Exception
     */
    public TapoCipher(String handshakeKey, TapoCredentials credentials) {
        setKey(handshakeKey, credentials);
    }

    /**
     * SET NEW KEY AND CREDENTIALS
     * 
     * @param handshakeKey
     * @param credentials
     */
    public void setKey(String handshakeKey, TapoCredentials credentials) {
        logger.trace("Init TapoCipher with key: {} ", handshakeKey);
        MimeEncode mimeEncode = new MimeEncode();
        try {
            byte[] decode = mimeEncode.decode(handshakeKey.getBytes(HANDSHAKE_CHARSET));
            byte[] decode2 = mimeEncode.decode(credentials.getPrivateKeyBytes());
            Cipher instance = Cipher.getInstance(HANDSHAKE_TRANSFORMATION);
            KeyFactory kf = KeyFactory.getInstance(HANDSHAKE_ALGORITHM);
            PrivateKey p = kf.generatePrivate(new PKCS8EncodedKeySpec(decode2));
            instance.init(Cipher.DECRYPT_MODE, p);
            byte[] doFinal = instance.doFinal(decode);
            byte[] bArr = new byte[16];
            byte[] bArr2 = new byte[16];
            System.arraycopy(doFinal, 0, bArr, 0, 16);
            System.arraycopy(doFinal, 16, bArr2, 0, 16);
            initCipher(bArr, bArr2);
        } catch (Exception ex) {
            logger.warn("Something went wrong: {}", ex.getMessage());
        }
    }

    /**
     * INIT ENCODE/DECDE-CIPHERS
     * 
     * @param bArr
     * @param bArr2
     * @throws Exception
     */
    protected void initCipher(byte[] bArr, byte[] bArr2) throws Exception {
        try {
            mimeEncode = new MimeEncode();
            SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bArr2);
            this.encodeCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.decodeCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            this.encodeCipher.init(1, secretKeySpec, ivParameterSpec);
            this.decodeCipher.init(2, secretKeySpec, ivParameterSpec);
        } catch (Exception e) {
            logger.warn("initChiper failed: {}", e.getMessage());
            this.encodeCipher = null;
            this.decodeCipher = null;
        }
    }

    /**
     * ENCODE STRING
     * 
     * @param str source string to encode
     * @return encoded string
     * @throws Exception
     */
    public String encode(String str) throws Exception {
        byte[] doFinal;
        doFinal = this.encodeCipher.doFinal(str.getBytes(CIPHER_CHARSET));
        String encrypted = mimeEncode.encodeToString(doFinal);
        return encrypted.replace("\r\n", "");
    }

    /**
     * DECODE STRING
     * 
     * @param str source string to decode
     * @return decoded string
     * @throws Exception
     */
    public String decode(String str) throws Exception {
        byte[] data = mimeEncode.decode(str.getBytes(CIPHER_CHARSET));
        byte[] doFinal;
        doFinal = this.decodeCipher.doFinal(data);
        return new String(doFinal);
    }
}
