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
package org.openhab.binding.tapocontrol.internal.api.protocol.aes;

import static java.util.Base64.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.helpers.TapoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TAPO-CIPHER
 * Based on K4CZP3R's p100-java-poc
 * 
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class SecurePasstroughCipher {
    private final Logger logger = LoggerFactory.getLogger(SecurePasstroughCipher.class);
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

    /**
     * CREATE NEW EMPTY CIPHER
     */
    public SecurePasstroughCipher() {
    }

    /**
     * CREATE NEW CIPHER WITH KEY AND CREDENTIALS
     * 
     * @param handshakeKey key from Handshake-Request
     * @param keyPair keyPair
     * @throws TapoErrorHandler
     */
    public SecurePasstroughCipher(String handshakeKey, TapoKeyPair keyPair) throws TapoErrorHandler {
        setKey(handshakeKey, keyPair);
    }

    /**
     * SET NEW KEY AND CREDENTIALS
     * 
     * @param handshakeKey key from Handshake-Request
     * @param keyPair keyPair
     */
    public void setKey(String handshakeKey, TapoKeyPair keyPair) throws TapoErrorHandler {
        logger.trace("Init passtroughCipher with key: {} ", handshakeKey);
        try {
            byte[] decode = getMimeDecoder().decode(handshakeKey.getBytes(HANDSHAKE_CHARSET));
            byte[] decode2 = getMimeDecoder().decode(keyPair.getPrivateKeyBytes());
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
        } catch (Exception e) {
            logger.warn("handshake Failed: {}", e.getMessage());
            throw new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, e.getMessage());
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
            SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bArr2);
            encodeCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            decodeCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            encodeCipher.init(1, secretKeySpec, ivParameterSpec);
            decodeCipher.init(2, secretKeySpec, ivParameterSpec);
        } catch (Exception e) {
            logger.warn("initCipher failed: {}", e.getMessage());
            encodeCipher = null;
            decodeCipher = null;
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
        doFinal = encodeCipher.doFinal(str.getBytes(CIPHER_CHARSET));
        String encrypted = getMimeEncoder().encodeToString(doFinal);
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
        byte[] data = getMimeDecoder().decode(str.getBytes(CIPHER_CHARSET));
        byte[] doFinal;
        doFinal = decodeCipher.doFinal(data);
        return new String(doFinal);
    }
}
