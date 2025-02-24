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
package org.openhab.binding.mideaac.internal.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.cloud.CloudProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link Security} class provides Security coding and decoding.
 * The basic aes Protocol is used by both V2 and V3 devices.
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - JavaDoc
 */
@NonNullByDefault
public class Security {

    private @Nullable SecretKeySpec encKey = null;
    private Logger logger = LoggerFactory.getLogger(Security.class);
    private IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    CloudProvider cloudProvider;

    /**
     * Set Cloud Provider
     * 
     * @param cloudProvider Name of Cloud provider
     */
    public Security(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    /**
     * Basic Decryption for all devices using common signkey
     * 
     * @param encryptData encrypted array
     * @return decypted array
     */
    public byte[] aesDecrypt(byte[] encryptData) {
        byte[] plainText = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES decryption error: InvalidKeyException: {}", e.getMessage());
                return new byte[0];
            }

            try {
                plainText = cipher.doFinal(encryptData);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES decryption error: IllegalBlockSizeException: {}", e.getMessage());
                return new byte[0];
            } catch (BadPaddingException e) {
                logger.warn("AES decryption error: BadPaddingException: {}", e.getMessage());
                return new byte[0];
            }

        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES decryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            logger.warn("AES decryption error: NoSuchPaddingException: {}", e.getMessage());
            return new byte[0];
        }
        return plainText;
    }

    /**
     * Basic Encryption for all devices using common signkey
     * 
     * @param plainText Plain Text
     * @return encrpted byte[] array
     */
    public byte[] aesEncrypt(byte[] plainText) {
        byte[] encryptData = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES encryption error: InvalidKeyException: {}", e.getMessage());
            }

            try {
                encryptData = cipher.doFinal(plainText);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES encryption error: IllegalBlockSizeException: {}", e.getMessage());
                return new byte[0];
            } catch (BadPaddingException e) {
                logger.warn("AES encryption error: BadPaddingException: {}", e.getMessage());
                return new byte[0];
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES encryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            logger.warn("AES encryption error: NoSuchPaddingException: {}", e.getMessage());
            return new byte[0];
        }

        return encryptData;
    }

    /**
     * Secret key using MD5
     * 
     * @return encKey
     * @throws NoSuchAlgorithmException missing algorithm
     */
    public @Nullable SecretKeySpec getEncKey() throws NoSuchAlgorithmException {
        if (encKey == null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(cloudProvider.signkey().getBytes(StandardCharsets.US_ASCII));
            byte[] key = md.digest();
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            encKey = skeySpec;
        }

        return encKey;
    }

    /**
     * Encode32 Data
     * 
     * @param raw byte array
     * @return byte[]
     */
    public byte[] encode32Data(byte[] raw) {
        byte[] combine = ByteBuffer
                .allocate(raw.length + cloudProvider.signkey().getBytes(StandardCharsets.US_ASCII).length).put(raw)
                .put(cloudProvider.signkey().getBytes(StandardCharsets.US_ASCII)).array();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(combine);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Encode32 Data: NoSuchAlgorithmException {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * Message types
     */
    public enum MsgType {
        MSGTYPE_HANDSHAKE_REQUEST(0x0),
        MSGTYPE_HANDSHAKE_RESPONSE(0x1),
        MSGTYPE_ENCRYPTED_RESPONSE(0x3),
        MSGTYPE_ENCRYPTED_REQUEST(0x6),
        MSGTYPE_TRANSPARENT(0xf);

        private final int value;

        private MsgType(int value) {
            this.value = value;
        }

        /**
         * Message type Id
         * 
         * @return message type
         */
        public int getId() {
            return value;
        }

        /**
         * Plain language message
         * 
         * @param id id
         * @return message type
         */
        public static MsgType fromId(int id) {
            for (MsgType type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return MSGTYPE_TRANSPARENT;
        }
    }

    private int requestCount = 0;
    private int responseCount = 0;
    private byte[] tcpKey = new byte[0];

    /**
     * Advanced Encryption for V3 devices
     * 
     * @param data input data array
     * @param msgtype message type
     * @return encoded byte array
     */
    public byte[] encode8370(byte[] data, MsgType msgtype) {
        ByteBuffer headerBuffer = ByteBuffer.allocate(256);
        ByteBuffer dataBuffer = ByteBuffer.allocate(256);

        headerBuffer.put(new byte[] { (byte) 0x83, (byte) 0x70 });

        int size = data.length;
        int padding = 0;

        logger.trace("Size: {}", size);
        byte[] paddingData = null;
        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            if ((size + 2) % 16 != 0) {
                padding = 16 - (size + 2 & 0xf);
                size += padding + 32;
                logger.trace("Padding size: {}, size: {}", padding, size);
                paddingData = getRandomBytes(padding);
            }
        }
        headerBuffer.put(Utils.toBytes((short) size));

        headerBuffer.put(new byte[] { 0x20, (byte) (padding << 4 | msgtype.value) });

        if (requestCount > 0xfff) {
            logger.trace("requestCount is too big to convert: {}, changing requestCount to 0", requestCount);
            requestCount = 0;
        }

        dataBuffer.put(Utils.toBytes((short) requestCount));
        requestCount += 1;

        dataBuffer.put(data);
        if (paddingData != null) {
            dataBuffer.put(paddingData);
        }

        headerBuffer.flip();
        byte[] finalHeader = new byte[headerBuffer.remaining()];
        headerBuffer.get(finalHeader);

        dataBuffer.flip();
        byte[] finalData = new byte[dataBuffer.remaining()];
        dataBuffer.get(finalData);

        logger.trace("Header:      {}", Utils.bytesToHex(finalHeader));

        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            byte[] sign = sha256(Utils.concatenateArrays(finalHeader, finalData));
            logger.trace("Sign:        {}", Utils.bytesToHex(sign));
            logger.trace("TcpKey:      {}", Utils.bytesToHex(tcpKey));

            finalData = Utils.concatenateArrays(aesCbcEncrypt(finalData, tcpKey), sign);
        }

        byte[] result = Utils.concatenateArrays(finalHeader, finalData);
        return result;
    }

    /**
     * Advanced Decryption for V3 devices
     * 
     * @param data input data array
     * @return decrypted byte array
     * @throws IOException IO exception
     */
    public Decryption8370Result decode8370(byte[] data) throws IOException {
        if (data.length < 6) {
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        }
        byte[] header = Arrays.copyOfRange(data, 0, 6);
        logger.trace("Header:        {}", Utils.bytesToHex(header));
        if (header[0] != (byte) 0x83 || header[1] != (byte) 0x70) {
            logger.warn("Not an 8370 message");
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        }
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        int size = dataBuffer.getShort(2) + 8;
        logger.trace("Size: {}", size);
        byte[] leftover = null;
        if (data.length < size) {
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        } else if (data.length > size) {
            leftover = Arrays.copyOfRange(data, size, data.length);
            data = Arrays.copyOfRange(data, 0, size);
        }
        int padding = header[5] >> 4;
        logger.trace("Padding: {}", padding);
        MsgType msgtype = MsgType.fromId(header[5] & 0xf);
        logger.trace("MsgType: {}", msgtype.toString());
        data = Arrays.copyOfRange(data, 6, data.length);

        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            byte[] sign = Arrays.copyOfRange(data, data.length - 32, data.length);
            data = Arrays.copyOfRange(data, 0, data.length - 32);
            data = aesCbcDecrypt(data, tcpKey);
            byte[] signLocal = sha256(Utils.concatenateArrays(header, data));

            logger.trace("Sign:        {}", Utils.bytesToHex(sign));
            logger.trace("SignLocal:   {}", Utils.bytesToHex(signLocal));
            logger.trace("TcpKey:      {}", Utils.bytesToHex(tcpKey));
            logger.trace("Data:        {}", Utils.bytesToHex(data));

            if (!Arrays.equals(sign, signLocal)) {
                logger.warn("Sign does not match");
                return new Decryption8370Result(new ArrayList<byte[]>(), data);
            }

            if (padding > 0) {
                data = Arrays.copyOfRange(data, 0, data.length - padding);
            }
        } else {
            logger.warn("MsgType: {}", msgtype.toString());
            throw new IOException(msgtype.toString() + " response was received");
        }

        dataBuffer = ByteBuffer.wrap(data);
        responseCount = dataBuffer.getShort(0);
        logger.trace("responseCount: {}", responseCount);
        logger.trace("requestCount: {}", requestCount);
        data = Arrays.copyOfRange(data, 2, data.length);
        if (leftover != null) {
            Decryption8370Result r = decode8370(leftover);
            ArrayList<byte[]> responses = r.getResponses();
            responses.add(0, data);
            return new Decryption8370Result(responses, r.buffer);
        }

        ArrayList<byte[]> responses = new ArrayList<byte[]>();
        responses.add(data);
        return new Decryption8370Result(responses, new byte[] {});
    }

    /**
     * Retrieve TCP key
     * 
     * @param response message
     * @param key key
     * @return tcp key
     */
    public boolean tcpKey(byte[] response, byte key[]) {
        byte[] payload = Arrays.copyOfRange(response, 0, 32);
        byte[] sign = Arrays.copyOfRange(response, 32, 64);
        byte[] plain = aesCbcDecrypt(payload, key);
        byte[] signLocal = sha256(plain);

        logger.trace("Payload:   {}", Utils.bytesToHex(payload));
        logger.trace("Sign:      {}", Utils.bytesToHex(sign));
        logger.trace("SignLocal: {}", Utils.bytesToHex(signLocal));
        logger.trace("Plain:     {}", Utils.bytesToHex(plain));

        if (!Arrays.equals(sign, signLocal)) {
            logger.warn("Sign does not match");
            return false;
        }
        tcpKey = Utils.strxor(plain, key);
        logger.trace("TcpKey:    {}", Utils.bytesToHex(tcpKey));
        return true;
    }

    private byte[] aesCbcDecrypt(byte[] encryptData, byte[] decrypt_key) {
        byte[] plainText = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key = new SecretKeySpec(decrypt_key, "AES");

            try {
                cipher.init(Cipher.DECRYPT_MODE, key, iv);
            } catch (InvalidKeyException e) {
                logger.warn("AES decryption error: InvalidKeyException: {}", e.getMessage());
                return new byte[0];
            } catch (InvalidAlgorithmParameterException e) {
                logger.warn("AES decryption error: InvalidAlgorithmParameterException: {}", e.getMessage());
                return new byte[0];
            }

            try {
                plainText = cipher.doFinal(encryptData);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES decryption error: IllegalBlockSizeException: {}", e.getMessage());
                return new byte[0];
            } catch (BadPaddingException e) {
                logger.warn("AES decryption error: BadPaddingException: {}", e.getMessage());
                return new byte[0];
            }

        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES decryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            logger.warn("AES decryption error: NoSuchPaddingException: {}", e.getMessage());
            return new byte[0];
        }

        return plainText;
    }

    private byte[] aesCbcEncrypt(byte[] plainText, byte[] encrypt_key) {
        byte[] encryptData = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            SecretKeySpec key = new SecretKeySpec(encrypt_key, "AES");

            try {
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            } catch (InvalidKeyException e) {
                logger.warn("AES encryption error: InvalidKeyException: {}", e.getMessage());
            } catch (InvalidAlgorithmParameterException e) {
                logger.warn("AES encryption error: InvalidAlgorithmParameterException: {}", e.getMessage());
            }

            try {
                encryptData = cipher.doFinal(plainText);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES encryption error: IllegalBlockSizeException: {}", e.getMessage());
                return new byte[0];
            } catch (BadPaddingException e) {
                logger.warn("AES encryption error: BadPaddingException: {}", e.getMessage());
                return new byte[0];
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES encryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return new byte[0];
        } catch (NoSuchPaddingException e) {
            logger.warn("AES encryption error: NoSuchPaddingException: {}", e.getMessage());
            return new byte[0];
        }

        return encryptData;
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("SHA256 digest error: NoSuchAlgorithmException: {}", e.getMessage());
            return new byte[0];
        }
    }

    private byte[] getRandomBytes(int size) {
        byte[] random = new byte[size];
        new Random().nextBytes(random);
        return random;
    }

    /**
     * Path to cloud provider
     * 
     * @param url url of cloud provider
     * @param payload message
     * @return lower case hex string
     */
    public @Nullable String sign(String url, JsonObject payload) {
        logger.trace("url: {}", url);
        String path;
        try {
            path = new URI(url).getPath();

            String query = Utils.getQueryString(payload);

            String sign = path + query + cloudProvider.appkey();
            logger.trace("sign: {}", sign);
            return Utils.bytesToHexLowercase(sha256((sign).getBytes(StandardCharsets.US_ASCII)));
        } catch (URISyntaxException e) {
            logger.warn("Syntax error{}", e.getMessage());
        }

        return null;
    }

    /**
     * Provides a randown iotKey for Cloud Providers that do not have one
     * 
     * @param data input data array
     * @param random random values
     * @return sign
     */
    public @Nullable String newSign(String data, String random) {
        String msg = cloudProvider.iotkey();
        if (!data.isEmpty()) {
            msg += data;
        }
        msg += random;
        String sign;

        try {
            sign = hmac(msg, cloudProvider.hmackey(), "HmacSHA256");
        } catch (InvalidKeyException e) {
            logger.warn("HMAC digest error: InvalidKeyException: {}", e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("HMAC digest error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        }

        return sign; // .hexdigest();
    }

    /**
     * Converts parameters to lower case string for communication with cloud
     * 
     * @param data data array
     * @param key key
     * @param algorithm method
     * @throws NoSuchAlgorithmException no Algorithm
     * @throws InvalidKeyException bad key
     * @return lower case string
     */
    public String hmac(String data, String key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return Utils.bytesToHexLowercase(mac.doFinal(data.getBytes()));
    }

    /**
     * Encrypts password for cloud API using SHA-256
     * 
     * @param loginId Login ID
     * @param password Login password
     * @return string
     */
    public @Nullable String encryptPassword(@Nullable String loginId, String password) {
        try {
            // Hash the password
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(password.getBytes(StandardCharsets.US_ASCII));

            // Create the login hash with the loginID + password hash + appKey, then hash it all AGAIN
            String loginHash = loginId + Utils.bytesToHexLowercase(m.digest()) + cloudProvider.appkey();
            m = MessageDigest.getInstance("SHA-256");
            m.update(loginHash.getBytes(StandardCharsets.US_ASCII));
            return Utils.bytesToHexLowercase(m.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.warn("encryptPassword error: NoSuchAlgorithmException: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Encrypts password for cloud API using MD5
     * 
     * @param loginId Login ID
     * @param password Login password
     * @return string
     */
    public @Nullable String encryptIamPassword(@Nullable String loginId, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes(StandardCharsets.US_ASCII));

            MessageDigest mdSecond = MessageDigest.getInstance("MD5");
            mdSecond.update(Utils.bytesToHexLowercase(md.digest()).getBytes(StandardCharsets.US_ASCII));

            // if self._use_china_server:
            // return mdSecond.hexdigest()

            String loginHash = loginId + Utils.bytesToHexLowercase(mdSecond.digest()) + cloudProvider.appkey();
            return Utils.bytesToHexLowercase(sha256(loginHash.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            logger.warn("encryptIamPasswordt error: NoSuchAlgorithmException: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Gets UDPID from byte data
     * 
     * @param data data array
     * @return string of lower case bytes
     */
    public String getUdpId(byte[] data) {
        byte[] b = sha256(data);
        byte[] b1 = Arrays.copyOfRange(b, 0, 16);
        byte[] b2 = Arrays.copyOfRange(b, 16, b.length);
        byte[] b3 = new byte[16];
        int i = 0;
        while (i < b1.length) {
            b3[i] = (byte) (b1[i] ^ b2[i]);
            i++;
        }
        return Utils.bytesToHexLowercase(b3);
    }
}
