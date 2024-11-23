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
package org.openhab.binding.playstation.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PS4Crypto} is responsible for encryption and decryption of
 * packets to / from the PS4.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS4Crypto {

    private final Logger logger = LoggerFactory.getLogger(PS4Crypto.class);

    // Public key is from ps4-waker (https://github.com/dhleong/ps4-waker)
    private static final String PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----\
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxfAO/MDk5ovZpp7xlG9J\
            JKc4Sg4ztAz+BbOt6Gbhub02tF9bryklpTIyzM0v817pwQ3TCoigpxEcWdTykhDL\
            cGhAbcp6E7Xh8aHEsqgtQ/c+wY1zIl3fU//uddlB1XuipXthDv6emXsyyU/tJWqc\
            zy9HCJncLJeYo7MJvf2TE9nnlVm1x4flmD0k1zrvb3MONqoZbKb/TQVuVhBv7SM+\
            U5PSi3diXIx1Nnj4vQ8clRNUJ5X1tT9XfVmKQS1J513XNZ0uYHYRDzQYujpLWucu\
            ob7v50wCpUm3iKP1fYCixMP6xFm0jPYz1YQaMV35VkYwc40qgk3av0PDS+1G0dCm\
            swIDAQAB\
            -----END PUBLIC KEY-----\
            """;

    private final byte[] remoteSeed = new byte[16];
    private final byte[] randomSeed = new byte[16];
    private @Nullable Cipher ps4Cipher;
    private @Nullable Cipher aesEncryptCipher;
    private @Nullable Cipher aesDecryptCipher;

    PS4Crypto() {
        ps4Cipher = getRsaCipher(PUBLIC_KEY);
    }

    void clearCiphers() {
        aesEncryptCipher = null;
        aesDecryptCipher = null;
    }

    void initCiphers() {
        new SecureRandom().nextBytes(randomSeed);
        SecretKeySpec keySpec = new SecretKeySpec(randomSeed, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(remoteSeed);
        try {
            Cipher encCipher = Cipher.getInstance("AES/CBC/NoPadding");
            encCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            Cipher decCipher = Cipher.getInstance("AES/CBC/NoPadding");
            decCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            logger.debug("Ciphers initialized.");
            aesEncryptCipher = encCipher;
            aesDecryptCipher = decCipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            logger.warn("Can not initialize ciphers.", e);
        }
    }

    int parseHelloResponsePacket(ByteBuffer rBuffer) {
        int result = -1;
        rBuffer.rewind();
        final int buffSize = rBuffer.remaining();
        final int size = rBuffer.getInt();
        if (size > buffSize || size < 12) {
            logger.warn("Response size ({}) not good, buffer size ({}).", size, buffSize);
            return result;
        }
        int cmdValue = rBuffer.getInt();
        int statusValue = rBuffer.getInt();
        PS4Command command = PS4Command.valueOfTag(cmdValue);
        byte[] respBuff = new byte[size];
        rBuffer.rewind();
        rBuffer.get(respBuff);
        if (command == PS4Command.HELLO_REQ) {
            if (statusValue == PS4PacketHandler.REQ_VERSION) {
                rBuffer.position(20);
                rBuffer.get(remoteSeed, 0, 16);
                initCiphers();
                result = 0;
            }
        } else {
            logger.debug("Unknown resp-cmd, size:{}, command:{}, status:{}, data:{}.", size, cmdValue, statusValue,
                    respBuff);
        }
        return result;
    }

    ByteBuffer makeHandshakePacket() {
        byte[] msg = null;
        Cipher hsCipher = ps4Cipher;
        if (hsCipher != null) {
            try {
                msg = hsCipher.doFinal(randomSeed);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                logger.debug("Cipher exception: {}", e.getMessage());
            }
        }
        if (msg == null || msg.length != 256) {
            return ByteBuffer.allocate(0);
        }
        ByteBuffer packet = PS4PacketHandler.newPacketOfSize(8 + 256 + 16, PS4Command.HANDSHAKE_REQ);
        packet.put(msg);
        packet.put(remoteSeed); // Seed = 16 bytes
        packet.rewind();
        return packet;
    }

    ByteBuffer encryptPacket(ByteBuffer packet) {
        Cipher encCipher = aesEncryptCipher;
        if (encCipher != null) {
            return ByteBuffer.wrap(encCipher.update(packet.array()));
        }
        logger.debug("Not encrypting packet.");
        return ByteBuffer.allocate(0);
    }

    ByteBuffer decryptPacket(ByteBuffer encBuffer) {
        Cipher decCipher = aesDecryptCipher;
        if (decCipher != null) {
            byte[] respBuff = new byte[encBuffer.position()];
            encBuffer.position(0);
            encBuffer.get(respBuff, 0, respBuff.length);
            byte[] data = decCipher.update(respBuff);
            if (data != null) {
                return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            }
        }
        logger.debug("Not decrypting response.");
        return ByteBuffer.allocate(0);
    }

    private @Nullable Cipher getRsaCipher(String key) {
        try {
            String keyString = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            byte[] keyData = Base64.getDecoder().decode(keyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec x509keySpec = new X509EncodedKeySpec(keyData);
            PublicKey publicKey = keyFactory.generatePublic(x509keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            logger.debug("Initialized RSA public key cipher");
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            logger.warn("Exception enabling RSA cipher.", e);
            return null;
        }
    }
}
