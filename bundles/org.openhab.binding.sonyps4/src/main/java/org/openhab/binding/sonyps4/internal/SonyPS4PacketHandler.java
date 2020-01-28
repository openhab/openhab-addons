/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sonyps4.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
 * The {@link SonyPS4PacketHandler} is responsible for creating and parsing
 * packets to / from the PS4.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4PacketHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4PacketHandler.class);

    private static final String APPLICATION_NAME = "OpenHAB PlayStation 4 Binding";

    private static final String OS_VERSION = "4.4";
    private static final String DDP_VERSION = "device-discovery-protocol-version:00020020\n";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxfAO/MDk5ovZpp7xlG9J"
            + "JKc4Sg4ztAz+BbOt6Gbhub02tF9bryklpTIyzM0v817pwQ3TCoigpxEcWdTykhDL"
            + "cGhAbcp6E7Xh8aHEsqgtQ/c+wY1zIl3fU//uddlB1XuipXthDv6emXsyyU/tJWqc"
            + "zy9HCJncLJeYo7MJvf2TE9nnlVm1x4flmD0k1zrvb3MONqoZbKb/TQVuVhBv7SM+"
            + "U5PSi3diXIx1Nnj4vQ8clRNUJ5X1tT9XfVmKQS1J513XNZ0uYHYRDzQYujpLWucu"
            + "ob7v50wCpUm3iKP1fYCixMP6xFm0jPYz1YQaMV35VkYwc40qgk3av0PDS+1G0dCm" + "swIDAQAB"
            + "-----END PUBLIC KEY-----";
    private static final int REQ_VERSION = 0x20000;

    private final byte[] remoteSeed = new byte[16];
    private final byte[] randomSeed = new byte[16];
    private @Nullable Cipher aesEncryptCipher;
    private @Nullable Cipher aesDecryptCipher;
    private @Nullable Cipher ps4Cipher;

    SonyPS4PacketHandler() {
        new SecureRandom().nextBytes(randomSeed);
        ps4Cipher = getRsaCipher(PUBLIC_KEY);
    }

    private void initCiphers() {
        IvParameterSpec ivSpec = new IvParameterSpec(remoteSeed);
        SecretKeySpec keySpec = new SecretKeySpec(randomSeed, "AES");
        Cipher encCipher = null;
        Cipher decCipher = null;
        try {
            encCipher = Cipher.getInstance("AES/CBC/NoPadding");
            encCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            decCipher = Cipher.getInstance("AES/CBC/NoPadding");
            decCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            logger.warn("Can not initialize ciphers.", e);
        }
        logger.debug("Ciphers initialized.");
        aesEncryptCipher = encCipher;
        aesDecryptCipher = decCipher;
    }

    /**
     * Allocates a new ByteBuffer of exactly size.
     *
     * @param size The size of the packet.
     * @return A ByteBuffer of exactly size.
     */
    private ByteBuffer newPacketOfSize(int size) {
        ByteBuffer packet = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size);
        return packet;
    }

    /**
     * Allocates a new ByteBuffer of size aligned to be a multiple of 16.
     *
     * @param size The size of the data in the packet.
     * @return A 16byte aligned ByteBuffer.
     */
    private ByteBuffer newPacketForEncryption(int size) {
        int realSize = (((size + 15) >> 4) << 4);
        ByteBuffer packet = ByteBuffer.allocate(realSize).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size);
        return packet;
    }

    private byte[] encryptPacket(ByteBuffer packet) {
        Cipher encCipher = aesEncryptCipher;
        if (encCipher != null) {
            return encCipher.update(packet.array());
        }
        return new byte[0];
    }

    int parseEncryptedPacketFinal(ByteBuffer encBuffer) {
        int result = 0;
        Cipher decCipher = aesDecryptCipher;
        if (decCipher != null) {
            logger.debug("Decrypting response.");
            byte[] data = null;
            try {
                data = decCipher.doFinal(encBuffer.array());
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                logger.warn("Can not decrypt response.", e);
            }
            if (data != null) {
                result = parseResponsePacket(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            } else {
                logger.debug("Not decrypting response.");
            }
        }
        return result;
    }

    int parseEncryptedPacket(ByteBuffer encBuffer) {
        int result = 0;
        Cipher decCipher = aesDecryptCipher;
        if (decCipher != null) {
            logger.debug("Decrypting response.");
            byte[] data = decCipher.update(encBuffer.array());
            if (data != null) {
                result = parseResponsePacket(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            } else {
                logger.debug("Not decrypting response.");
            }
        }
        return result;
    }

    int parseResponsePacket(ByteBuffer rBuffer) {
        int result = -1;
        rBuffer.rewind();
        final int buffSize = rBuffer.remaining();
        final int size = rBuffer.getInt();
        if (size > buffSize || size < 12) {
            logger.info("Response size ({}) not good, buffer size ({}).", size, buffSize);
            return result;
        }
        int cmdValue = rBuffer.getInt();
        int statusValue = rBuffer.getInt();
        SonyPS4Command command = SonyPS4Command.valueOfTag(cmdValue);
        byte[] respBuff = new byte[size];
        rBuffer.rewind();
        rBuffer.get(respBuff);
        if (command != null) {
            logger.debug("Response size:{}, command:{}, status:{}, data:{}.", size, command, statusValue, respBuff);
            switch (command) {
                case HELLO_REQ:
                    rBuffer.position(20);
                    rBuffer.get(remoteSeed, 0, 16);
                    initCiphers();
                    if (statusValue == REQ_VERSION) {
                        result = 0;
                    }
                    break;
                case LOGIN_RSP:
                case APP_START_RSP:
                case SERVER_STATUS_RSP:
                case STANDBY_RSP:
                case LOGOUT_RSP:
                case APP_START2_RSP:
                    result = statusValue;
                    break;
                default:
                    result = statusValue;
                    logger.info("Unknown response command: {}. Missing case.", cmdValue);
                    break;
            }
        } else {
            logger.info("Unknown response command: {}. Not in enum", cmdValue);
        }
        return result;
    }

    byte[] makeSearchPacket() {
        StringBuilder packet = new StringBuilder("SRCH * HTTP/1.1\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    byte[] makeWakeupPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("WAKEUP * HTTP/1.1\n");
        packet.append("client-type:i\n");
        packet.append("auth-type:C\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    byte[] makeLaunchPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("LAUNCH * HTTP/1.1\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    byte[] makeHelloPacket() {
        ByteBuffer packet = newPacketOfSize(28);
        packet.putInt(SonyPS4Command.HELLO_REQ.value);
        packet.putInt(REQ_VERSION);
        packet.put(new byte[16]); // Seed = 16 bytes
        return packet.array();
    }

    byte[] makeHandshakePacket() {
        byte[] msg = null;
        Cipher hsCipher = ps4Cipher;
        if (hsCipher != null) {
            try {
                msg = hsCipher.doFinal(randomSeed);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                logger.debug("Cipher exception: {}", e);
            }
        }
        if (msg == null || msg.length != 256) {
            return new byte[0];
        }
        ByteBuffer packet = newPacketOfSize(8 + 256 + 16);
        packet.putInt(SonyPS4Command.HANDSHAKE_REQ.value);
        packet.put(msg);
        packet.put(remoteSeed); // Seed = 16 bytes
        return packet.array();
    }

    byte[] makeLoginPacket(String userCredential, String pinCode, String pairingCode) {
        ByteBuffer packet = newPacketForEncryption(16 + 64 + 256 + 16 + 16 + 16);
        packet.putInt(SonyPS4Command.LOGIN_REQ.value);
        packet.put(pinCode.getBytes(), 0, 4); // pin Code
        packet.putInt(0x0201); // Magic number
        packet.put(userCredential.getBytes(StandardCharsets.US_ASCII), 0, 64);
        packet.put(APPLICATION_NAME.getBytes(StandardCharsets.UTF_8)); // app_label
        packet.position(16 + 64 + 256);
        packet.put(OS_VERSION.getBytes()); // os_version
        packet.position(16 + 64 + 256 + 16);
        packet.put("Mac mini 2012".getBytes(StandardCharsets.UTF_8)); // Model, name of paired unit, shown on the PS4 in
                                                                      // the settings view.
        packet.position(16 + 64 + 256 + 16 + 16);
        if (!pairingCode.isEmpty()) {
            packet.put(pairingCode.getBytes(), 0, 8); // Pairing code
        }
        return encryptPacket(packet);
    }

    byte[] makeStatusPacket(int status) {
        ByteBuffer packet = newPacketForEncryption(16);
        packet.putInt(SonyPS4Command.STATUS_REQ.value);
        packet.putInt(status); // status
        return encryptPacket(packet);
    }

    byte[] makeStandbyPacket() {
        ByteBuffer packet = newPacketForEncryption(8);
        packet.putInt(SonyPS4Command.STANDBY_REQ.value);
        return encryptPacket(packet);
    }

    byte[] makeApplicationPacket(String applicationName) {
        ByteBuffer packet = newPacketForEncryption(8 + 16 + 8);
        packet.putInt(SonyPS4Command.APP_START_REQ.value);
        packet.put(applicationName.getBytes()); // AppName
        return encryptPacket(packet);
    }

    byte[] makeByebyePacket() {
        ByteBuffer packet = newPacketForEncryption(8);
        packet.putInt(SonyPS4Command.BYEBYE_REQ.value);
        return encryptPacket(packet);
    }

    byte[] makeLogoutPacket() {
        ByteBuffer packet = newPacketForEncryption(8);
        packet.putInt(SonyPS4Command.LOGOUT_REQ.value);
        return encryptPacket(packet);
    }

    byte[] makeRemoteControlPacket(int pushedKey) {
        ByteBuffer packet = newPacketForEncryption(16);
        packet.putInt(SonyPS4Command.REMOTE_CONTROL_REQ.value);
        packet.putInt(pushedKey);
        packet.putInt(0); // HoldTime
        return encryptPacket(packet);
    }

    private @Nullable Cipher getRsaCipher(String key) {
        try {
            String keyString = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            byte[] keyData = Base64.getDecoder().decode(keyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            logger.debug("PS4 public key: {}", publicKey);
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
