/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4PacketHandler} is responsible for creating and parsing
 * packets to / from the PS4.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class SonyPS4PacketHandler {

    private static final String VERSION = "1.1";
    private static final String DDP_VERSION = "00020020";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxfAO/MDk5ovZpp7xlG9J"
            + "JKc4Sg4ztAz+BbOt6Gbhub02tF9bryklpTIyzM0v817pwQ3TCoigpxEcWdTykhDL"
            + "cGhAbcp6E7Xh8aHEsqgtQ/c+wY1zIl3fU//uddlB1XuipXthDv6emXsyyU/tJWqc"
            + "zy9HCJncLJeYo7MJvf2TE9nnlVm1x4flmD0k1zrvb3MONqoZbKb/TQVuVhBv7SM+"
            + "U5PSi3diXIx1Nnj4vQ8clRNUJ5X1tT9XfVmKQS1J513XNZ0uYHYRDzQYujpLWucu"
            + "ob7v50wCpUm3iKP1fYCixMP6xFm0jPYz1YQaMV35VkYwc40qgk3av0PDS+1G0dCm" + "swIDAQAB"
            + "-----END PUBLIC KEY-----";
    private static final int REQ_VERSION = 0x20000;

    // PS4 Commands
    private static final int HELLO_REQ = 0x6f636370;
    private static final int BYEBYE_REQ = 0x04;
    private static final int LOGIN_RSP = 0x07;
    private static final int APP_START_REQ = 0x0a;
    private static final int APP_START_RSP = 0x0b;
    private static final int OSK_START_REQ = 0x0c;
    private static final int OSK_CHANGE_STRING_REQ = 0x0e;
    private static final int OSK_CONTROL_REQ = 0x10;
    private static final int UNKNOWN_1_RSP = 0x12;
    private static final int STATUS_REQ = 0x14;
    private static final int STANDBY_REQ = 0x1a;
    private static final int STANDBY_RSP = 0x1b;
    private static final int REMOTE_CONTROL_REQ = 0x1c;
    private static final int LOGIN_REQ = 0x1e;
    private static final int HANDSHAKE_REQ = 0x20;
    private static final int APP_START2_REQ = 0x24;

    private final Logger logger = LoggerFactory.getLogger(SonyPS4PacketHandler.class);
    private final byte[] remoteSeed = new byte[16];
    private final byte[] randomSeed = new byte[16];
    private IvParameterSpec ivSpec;
    private Cipher aesEncryptCipher;
    private Cipher aesDecryptCipher;
    private Cipher ps4Cipher;

    public SonyPS4PacketHandler() {
        new SecureRandom().nextBytes(randomSeed);
        ps4Cipher = getRsaCipher(PUBLIC_KEY);
    }

    private ByteBuffer newPacketOfSize(int size) {
        ByteBuffer packet = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size);
        return packet;
    }

    private ByteBuffer newPacketForEncryption(int size) {
        // Size should always be a multiple of 16.
        int realSize = (((size + 15) >> 4) << 4);
        ByteBuffer packet = ByteBuffer.allocate(realSize).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size);
        return packet;
    }

    public void handleHelloResponse(ByteBuffer helloBuffer) {
        helloBuffer.position(20);
        helloBuffer.get(remoteSeed, 0, 16);
        ivSpec = new IvParameterSpec(remoteSeed);
    }

    public byte[] decryptResponsePacket(byte[] input) {
        return aesDecryptCipher.update(input);
    }

    public byte[] handleLoginResponse(byte[] input) {
        try {
            return aesDecryptCipher.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] makeSearchPacket() {
        StringBuilder packet = new StringBuilder("SRCH * HTTP/1.1\n");
        packet.append("device-discovery-protocol-version:" + DDP_VERSION + "\n");
        return packet.toString().getBytes();
    }

    public byte[] makeWakeupPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("WAKEUP * HTTP/1.1\n");
        packet.append("client-type:i\n");
        packet.append("auth-type:C\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append("device-discovery-protocol-version:" + DDP_VERSION + "\n");
        return packet.toString().getBytes();
    }

    public byte[] makeLaunchPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("LAUNCH * HTTP/1.1\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append("device-discovery-protocol-version:" + DDP_VERSION + "\n");
        return packet.toString().getBytes();
    }

    public byte[] makeHelloPacket() {
        ByteBuffer packet = newPacketOfSize(28);
        packet.putInt(HELLO_REQ);
        packet.putInt(REQ_VERSION);
        packet.put(new byte[16]); // Seed = 16 bytes
        return packet.array();
    }

    public byte[] makeHandshakePacket() {
        byte[] msg = null;
        try {
            msg = ps4Cipher.doFinal(randomSeed);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.debug("Cipher exception: {}", e);
        }
        if (msg == null || msg.length != 256) {
            return new byte[0];
        }
        ByteBuffer packet = newPacketOfSize(8 + 256 + 16);
        packet.putInt(HANDSHAKE_REQ);
        packet.put(msg);
        packet.put(remoteSeed); // Seed = 16 bytes
        return packet.array();
    }

    public byte[] makeLoginPacket(String userCredential, String pinCode) {
        ByteBuffer packet = newPacketForEncryption(16 + 64 + 256 + 16 + 16 + 16);
        packet.putInt(LOGIN_REQ);
        packet.put(pinCode.getBytes()); // PIN Code
        packet.position(12);
        packet.putInt(0x0201); // Magic number
        packet.put(userCredential.getBytes(StandardCharsets.US_ASCII));
        packet.position(16 + 64);
        packet.put("OpenHAB PlayStation Binding".getBytes(StandardCharsets.UTF_8)); // app_label
        packet.position(16 + 64 + 256);
        packet.put("4.4".getBytes()); // os_version
        packet.position(16 + 64 + 256 + 16);
        packet.put("PS4 Waker".getBytes()); // model
        packet.position(16 + 64 + 256 + 16 + 16);
        packet.put(new byte[16]); // pass_code

        SecretKeySpec keySpec = new SecretKeySpec(randomSeed, "AES");
        try {
            aesEncryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            aesEncryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            aesDecryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            aesDecryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return aesEncryptCipher.update(packet.array());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] makeStatusPacket(int status) {
        ByteBuffer packet = newPacketForEncryption(16);
        packet.putInt(STATUS_REQ);
        packet.putInt(status); // status
        return aesEncryptCipher.update(packet.array());
    }

    public byte[] makeStandbyPacket() {
        ByteBuffer packet = newPacketForEncryption(8);
        packet.putInt(STANDBY_REQ);
        return aesEncryptCipher.update(packet.array());
    }

    public byte[] makeApplicationPacket(String applicationName) {
        ByteBuffer packet = newPacketForEncryption(8 + 16 + 8);
        packet.putInt(APP_START_REQ);
        packet.put(applicationName.getBytes()); // AppName
        return aesEncryptCipher.update(packet.array());
    }

    public byte[] makeByebyePacket() {
        ByteBuffer packet = newPacketForEncryption(8);
        packet.putInt(BYEBYE_REQ);
        return aesEncryptCipher.update(packet.array());
    }

    private Cipher getRsaCipher(String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String keyString = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            byte[] keyData = Base64.getDecoder().decode(keyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            logger.debug("PS4 public key: {}", publicKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            logger.debug("Initialized RSA public key cipher");
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            logger.error("Exception enabling RSA cipher: {}", e.getMessage());
            return null;
        }
    }

}
