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
package org.openhab.binding.tuya.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CryptoUtil} is a support class for encrypting/decrypting messages
 *
 * Parts of this code are inspired by the TuyAPI project (see notice file)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CryptoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoUtil.class);

    private static final int[] CRC_32_TABLE = { 0x00000000, 0x77073096, 0xee0e612c, 0x990951ba, 0x076dc419, 0x706af48f,
            0xe963a535, 0x9e6495a3, 0x0edb8832, 0x79dcb8a4, 0xe0d5e91e, 0x97d2d988, 0x09b64c2b, 0x7eb17cbd, 0xe7b82d07,
            0x90bf1d91, 0x1db71064, 0x6ab020f2, 0xf3b97148, 0x84be41de, 0x1adad47d, 0x6ddde4eb, 0xf4d4b551, 0x83d385c7,
            0x136c9856, 0x646ba8c0, 0xfd62f97a, 0x8a65c9ec, 0x14015c4f, 0x63066cd9, 0xfa0f3d63, 0x8d080df5, 0x3b6e20c8,
            0x4c69105e, 0xd56041e4, 0xa2677172, 0x3c03e4d1, 0x4b04d447, 0xd20d85fd, 0xa50ab56b, 0x35b5a8fa, 0x42b2986c,
            0xdbbbc9d6, 0xacbcf940, 0x32d86ce3, 0x45df5c75, 0xdcd60dcf, 0xabd13d59, 0x26d930ac, 0x51de003a, 0xc8d75180,
            0xbfd06116, 0x21b4f4b5, 0x56b3c423, 0xcfba9599, 0xb8bda50f, 0x2802b89e, 0x5f058808, 0xc60cd9b2, 0xb10be924,
            0x2f6f7c87, 0x58684c11, 0xc1611dab, 0xb6662d3d, 0x76dc4190, 0x01db7106, 0x98d220bc, 0xefd5102a, 0x71b18589,
            0x06b6b51f, 0x9fbfe4a5, 0xe8b8d433, 0x7807c9a2, 0x0f00f934, 0x9609a88e, 0xe10e9818, 0x7f6a0dbb, 0x086d3d2d,
            0x91646c97, 0xe6635c01, 0x6b6b51f4, 0x1c6c6162, 0x856530d8, 0xf262004e, 0x6c0695ed, 0x1b01a57b, 0x8208f4c1,
            0xf50fc457, 0x65b0d9c6, 0x12b7e950, 0x8bbeb8ea, 0xfcb9887c, 0x62dd1ddf, 0x15da2d49, 0x8cd37cf3, 0xfbd44c65,
            0x4db26158, 0x3ab551ce, 0xa3bc0074, 0xd4bb30e2, 0x4adfa541, 0x3dd895d7, 0xa4d1c46d, 0xd3d6f4fb, 0x4369e96a,
            0x346ed9fc, 0xad678846, 0xda60b8d0, 0x44042d73, 0x33031de5, 0xaa0a4c5f, 0xdd0d7cc9, 0x5005713c, 0x270241aa,
            0xbe0b1010, 0xc90c2086, 0x5768b525, 0x206f85b3, 0xb966d409, 0xce61e49f, 0x5edef90e, 0x29d9c998, 0xb0d09822,
            0xc7d7a8b4, 0x59b33d17, 0x2eb40d81, 0xb7bd5c3b, 0xc0ba6cad, 0xedb88320, 0x9abfb3b6, 0x03b6e20c, 0x74b1d29a,
            0xead54739, 0x9dd277af, 0x04db2615, 0x73dc1683, 0xe3630b12, 0x94643b84, 0x0d6d6a3e, 0x7a6a5aa8, 0xe40ecf0b,
            0x9309ff9d, 0x0a00ae27, 0x7d079eb1, 0xf00f9344, 0x8708a3d2, 0x1e01f268, 0x6906c2fe, 0xf762575d, 0x806567cb,
            0x196c3671, 0x6e6b06e7, 0xfed41b76, 0x89d32be0, 0x10da7a5a, 0x67dd4acc, 0xf9b9df6f, 0x8ebeeff9, 0x17b7be43,
            0x60b08ed5, 0xd6d6a3e8, 0xa1d1937e, 0x38d8c2c4, 0x4fdff252, 0xd1bb67f1, 0xa6bc5767, 0x3fb506dd, 0x48b2364b,
            0xd80d2bda, 0xaf0a1b4c, 0x36034af6, 0x41047a60, 0xdf60efc3, 0xa867df55, 0x316e8eef, 0x4669be79, 0xcb61b38c,
            0xbc66831a, 0x256fd2a0, 0x5268e236, 0xcc0c7795, 0xbb0b4703, 0x220216b9, 0x5505262f, 0xc5ba3bbe, 0xb2bd0b28,
            0x2bb45a92, 0x5cb36a04, 0xc2d7ffa7, 0xb5d0cf31, 0x2cd99e8b, 0x5bdeae1d, 0x9b64c2b0, 0xec63f226, 0x756aa39c,
            0x026d930a, 0x9c0906a9, 0xeb0e363f, 0x72076785, 0x05005713, 0x95bf4a82, 0xe2b87a14, 0x7bb12bae, 0x0cb61b38,
            0x92d28e9b, 0xe5d5be0d, 0x7cdcefb7, 0x0bdbdf21, 0x86d3d2d4, 0xf1d4e242, 0x68ddb3f8, 0x1fda836e, 0x81be16cd,
            0xf6b9265b, 0x6fb077e1, 0x18b74777, 0x88085ae6, 0xff0f6a70, 0x66063bca, 0x11010b5c, 0x8f659eff, 0xf862ae69,
            0x616bffd3, 0x166ccf45, 0xa00ae278, 0xd70dd2ee, 0x4e048354, 0x3903b3c2, 0xa7672661, 0xd06016f7, 0x4969474d,
            0x3e6e77db, 0xaed16a4a, 0xd9d65adc, 0x40df0b66, 0x37d83bf0, 0xa9bcae53, 0xdebb9ec5, 0x47b2cf7f, 0x30b5ffe9,
            0xbdbdf21c, 0xcabac28a, 0x53b39330, 0x24b4a3a6, 0xbad03605, 0xcdd70693, 0x54de5729, 0x23d967bf, 0xb3667a2e,
            0xc4614ab8, 0x5d681b02, 0x2a6f2b94, 0xb40bbe37, 0xc30c8ea1, 0x5a05df1b, 0x2d02ef8d };
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int SESSION_KEY_LENGTH = 16;

    private static final Random SECURE_RNG = new SecureRandom();

    private CryptoUtil() {
        // prevent instantiation
    }

    /**
     * Compute a Tuya compatible checksum
     *
     * @param bytes an {@link byte[]} containing the input data
     * @param start the start position of the checksum calculation
     * @param end the end position of the checksum position
     * @return the calculated checksum
     */
    public static int calculateChecksum(byte[] bytes, int start, int end) {
        int crc = 0xffffffff;

        for (int i = start; i < end; i++) {
            crc = (crc >>> 8) ^ CRC_32_TABLE[(crc ^ bytes[i]) & 0xff];
        }

        return ~crc;
    }

    /**
     * Calculate an SHA-256 hash of the input data
     *
     * @param data input data as String
     * @return the resulting SHA-256 hash as hexadecimal String
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data.getBytes(StandardCharsets.UTF_8));
            return HexUtils.bytesToHex(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Algorithm SHA-256 not found. This should never happen. Check your Java setup.");
        }
        return "";
    }

    /**
     * Calculate a MD5 hash of the input data
     *
     * @param data input data as String
     * @return the resulting MD5 hash as hexadecimal String
     */
    public static String md5(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data.getBytes(StandardCharsets.UTF_8));
            return HexUtils.bytesToHex(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Algorithm MD5 not found. This should never happen. Check your Java setup.");
        }
        return "";
    }

    /**
     * Calculate an SHA-256 MAC of the input data with a given secret
     *
     * @param data input data as String
     * @param secret the secret to be used
     * @return the resulting MAC as hexadecimal String
     */
    public static String hmacSha256(String data, String secret) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);

            return HexUtils.bytesToHex(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            //
        }
        return "";
    }

    /**
     * Decrypt an AES-GCM encoded message
     *
     * @param msg the message as Base64 encoded string
     * @param password the password as string
     * @param t the timestamp of the message (used as AAD)
     * @return the decrypted message as String (or null if decryption failed)
     */
    public static @Nullable String decryptAesGcm(String msg, String password, long t) {
        try {
            byte[] rawBuffer = Base64.getDecoder().decode(msg);
            // first four bytes are IV length
            int ivLength = rawBuffer[0] << 24 | (rawBuffer[1] & 0xFF) << 16 | (rawBuffer[2] & 0xFF) << 8
                    | (rawBuffer[3] & 0xFF);
            // data length is full length without IV length and IV
            int dataLength = rawBuffer.length - 4 - ivLength;
            SecretKey secretKey = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), 8, 16, "AES");
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, rawBuffer, 4, ivLength);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            cipher.updateAAD(Long.toString(t).getBytes(StandardCharsets.UTF_8));
            byte[] decoded = cipher.doFinal(rawBuffer, 4 + ivLength, dataLength);
            return new String(decoded);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.warn("Decryption of MQ failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Decrypt an AES-GCM encoded message
     *
     * @param data the message as array of bytes
     * @param key the key as array of bytes
     * @param headerData optional, the header data as array of bytes (used as AAD)
     * @param nonce optional, the IV/nonce as array of bytes (12 bytes)
     * @return the decrypted message as String (or null if decryption failed)
     */
    public static byte @Nullable [] decryptAesGcm(byte[] data, byte[] key, byte @Nullable [] headerData,
            byte @Nullable [] nonce) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(Objects.requireNonNullElse(nonce, data), 0, iv, 0, GCM_IV_LENGTH);
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmIv = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv);
            if (headerData != null) {
                cipher.updateAAD(headerData);
            }
            return cipher.doFinal(data, GCM_IV_LENGTH, data.length - GCM_IV_LENGTH);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.warn("Decryption of MQ failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Decrypt an AES-ECB encoded message
     *
     * @param data the message as array of bytes
     * @param key the key as array of bytes
     * @param unpad remove padding (for protocol 3.4)
     * @return the decrypted message as String (or null if decryption failed)
     */
    public static byte @Nullable [] decryptAesEcb(byte[] data, byte[] key, boolean unpad) {
        if (data.length == 0) {
            return data.clone();
        }
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(data);
            if (unpad) {
                int padlength = decrypted[decrypted.length - 1];
                return Arrays.copyOf(decrypted, decrypted.length - padlength);
            }
            return decrypted;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.warn("Decryption of MQ failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Encrypt an AES-GCM encoded message
     *
     * @param data the message as array of bytes
     * @param key the key as array of bytes
     * @param headerData optional, the header data as array of bytes (used as AAD)
     * @param nonce optional, the IV/nonce as array of bytes (12 bytes)
     * @return the encrypted message as array of bytes (or null if encryption failed)
     */
    public static byte @Nullable [] encryptAesGcm(byte[] data, byte[] key, byte @Nullable [] headerData,
            byte @Nullable [] nonce) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            if (nonce != null) {
                System.arraycopy(nonce, 0, iv, 0, GCM_IV_LENGTH);
            } else {
                SECURE_RNG.nextBytes(iv);
            }
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            if (headerData != null) {
                cipher.updateAAD(headerData);
            }
            byte[] encryptedBytes = cipher.doFinal(data);
            byte[] result = new byte[GCM_IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, result, GCM_IV_LENGTH, encryptedBytes.length);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            LOGGER.warn("Encryption of MQ failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Encrypt an AES-ECB encoded message
     *
     * @param data the message as array of bytes
     * @param key the key as array of bytes
     * @return the encrypted message as array of bytes (or null if decryption failed)
     */
    public static byte @Nullable [] encryptAesEcb(byte[] data, byte[] key, boolean padding) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            final Cipher cipher = padding ? Cipher.getInstance("AES/ECB/PKCS5Padding")
                    : Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.warn("Encryption of MQ failed: {}", e.getMessage());
        }

        return null;
    }

    public static byte @Nullable [] hmac(byte[] data, byte[] key) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
            sha256HMAC.init(secretKey);

            return sha256HMAC.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.warn("Creating HMAC hash failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Generate a {@link byte[]} with the given size
     *
     * @param size the size in bytes
     * @return the array filled with random bytes.
     */
    public static byte[] generateRandom(int size) {
        byte[] random = new byte[size];
        SECURE_RNG.nextBytes(random);
        return random;
    }

    /**
     * Generate a protocol 3.4 and 3.5 session key from local and remote key for a device
     *
     * @param localKey the randomly generated local key
     * @param remoteKey the provided remote key
     * @param deviceKey the (constant) device key
     * @param protocol the protocol version
     * @return the session key for these keys and protocol
     */
    public static byte @Nullable [] generateSessionKey(byte[] localKey, byte[] remoteKey, byte[] deviceKey,
            ProtocolVersion protocol) {
        byte[] sessionKey = localKey.clone();
        for (int i = 0; i < sessionKey.length; i++) {
            sessionKey[i] = (byte) (sessionKey[i] ^ remoteKey[i]);
        }
        byte[] result = new byte[SESSION_KEY_LENGTH];
        if (protocol == ProtocolVersion.V3_4) {
            result = CryptoUtil.encryptAesEcb(sessionKey, deviceKey, false);
        } else if (protocol == ProtocolVersion.V3_5) {
            byte[] nonce = new byte[GCM_IV_LENGTH];
            System.arraycopy(localKey, 0, nonce, 0, GCM_IV_LENGTH);
            byte[] encrypted = CryptoUtil.encryptAesGcm(sessionKey, deviceKey, null, nonce);
            if (encrypted != null) {
                System.arraycopy(encrypted, GCM_IV_LENGTH, result, 0, SESSION_KEY_LENGTH);
            }
        }
        return result;
    }
}
