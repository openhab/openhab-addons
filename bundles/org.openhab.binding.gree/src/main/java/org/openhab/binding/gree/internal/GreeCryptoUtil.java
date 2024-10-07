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
package org.openhab.binding.gree.internal;

import static org.openhab.binding.gree.internal.GreeBindingConstants.EncryptionTypes;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gree.internal.gson.GreeBaseDTO;

/**
 * The CryptoUtil class provides functionality for encrypting and decrypting
 * messages sent to and from the Air Conditioner
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeCryptoUtil {
    private static final String AES_KEY = "a3K8Bx%2r8Y7#xDh";
    private static final String GCM_KEY = "{yxAHAY_Lm6pbC/<";
    private static final String GCM_IV = "5440784449675a516c5e6313";
    private static final String GCM_ADD = "qualcomm-test";
    private static final int TAG_LENGTH = 16;

    public static byte[] getAESGeneralKeyByteArray() {
        return AES_KEY.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getGCMGeneralKeyByteArray() {
        return GCM_KEY.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getGeneralKeyByteArray(EncryptionTypes encType) {
        if (encType == EncryptionTypes.GCM) {
            return getGCMGeneralKeyByteArray();
        }
        return getAESGeneralKeyByteArray();
    }

    public static byte[] getGCMIVByteArray() {
        return HexFormat.of().parseHex(GCM_IV);
    }

    public static byte[] getGCMADDByteArray() {
        return GCM_ADD.getBytes(StandardCharsets.UTF_8);
    }

    public static <T extends GreeBaseDTO> EncryptionTypes getEncryptionType(T response) {
        return response.tag != null ? EncryptionTypes.GCM : EncryptionTypes.ECB;
    }

    public static <T extends GreeBaseDTO> String decrypt(T response) throws GreeException {
        return decrypt(response, getEncryptionType(response));
    }

    public static <T extends GreeBaseDTO> String decrypt(byte[] keyarray, T response) throws GreeException {
        return decrypt(keyarray, response, getEncryptionType(response));
    }

    public static <T extends GreeBaseDTO> String decrypt(T response, EncryptionTypes encType) throws GreeException {
        if (encType == EncryptionTypes.UNKNOWN) {
            encType = getEncryptionType(response);
        }

        if (encType == EncryptionTypes.GCM) {
            return decrypt(getGCMGeneralKeyByteArray(), response, encType);
        } else {
            return decrypt(getAESGeneralKeyByteArray(), response, encType);
        }
    }

    public static <T extends GreeBaseDTO> String decrypt(byte[] keyarray, T response, EncryptionTypes encType)
            throws GreeException {
        if (encType == EncryptionTypes.UNKNOWN) {
            encType = getEncryptionType(response);
        }

        if (encType == EncryptionTypes.GCM) {
            return decryptGCMPack(keyarray, response.pack, response.tag);
        } else {
            return decryptPack(keyarray, response.pack);
        }
    }

    public static String decryptPack(byte[] keyarray, String message) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyarray, "AES");
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] imageByte = decoder.decode(message);

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytePlainText = aesCipher.doFinal(imageByte);

            return new String(bytePlainText, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException
                | IllegalBlockSizeException ex) {
            throw new GreeException("Decryption of recieved data failed", ex);
        }
    }

    public static String decryptGCMPack(byte[] keyBytes, String pack, String tag) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyBytes, "AES");
            Base64.Decoder decoder = Base64.getDecoder();

            byte[] packBytes = decoder.decode(pack);
            byte[] tagBytes = decoder.decode(tag);

            byte[] messageBytes = new byte[packBytes.length + tagBytes.length];
            System.arraycopy(packBytes, 0, messageBytes, 0, packBytes.length);
            System.arraycopy(tagBytes, 0, messageBytes, packBytes.length, tagBytes.length);

            Cipher gcmCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, getGCMIVByteArray());
            gcmCipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
            gcmCipher.updateAAD(getGCMADDByteArray());

            byte[] bytePlainText = gcmCipher.doFinal(messageBytes);
            return new String(bytePlainText, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException
                | IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
            throw new GreeException("GCM decryption of recieved data failed", ex);
        }
    }

    public static String[] encrypt(byte[] keyarray, String message, EncryptionTypes encType) throws GreeException {
        if (encType == EncryptionTypes.GCM) {
            return encryptGCMPack(keyarray, message);
        } else {
            String[] res = new String[1];
            res[0] = encryptPack(keyarray, message);
            return res;
        }
    }

    public static String encryptPack(byte[] keyarray, String message) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyarray, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytePlainText = aesCipher.doFinal(message.getBytes());

            Base64.Encoder newencoder = Base64.getEncoder();
            return new String(newencoder.encode(bytePlainText), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException
                | IllegalBlockSizeException ex) {
            throw new GreeException("Unable to encrypt outbound data", ex);
        }
    }

    public static String[] encryptGCMPack(byte[] keyarray, String message) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyarray, "AES");

            Cipher gcmCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, getGCMIVByteArray());
            gcmCipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
            gcmCipher.updateAAD(getGCMADDByteArray());

            byte[] encrypted = gcmCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            int packLength = encrypted.length - TAG_LENGTH;
            byte[] pack = new byte[packLength];
            byte[] tag = new byte[TAG_LENGTH];
            System.arraycopy(encrypted, 0, pack, 0, packLength);
            System.arraycopy(encrypted, packLength, tag, 0, TAG_LENGTH);

            Base64.Encoder encoder = Base64.getEncoder();
            String[] encryptedData = new String[2];
            encryptedData[0] = new String(encoder.encode(pack), StandardCharsets.UTF_8);
            encryptedData[1] = new String(encoder.encode(tag), StandardCharsets.UTF_8);
            return encryptedData;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException
                | IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
            throw new GreeException("Unable to encrypt (gcm) outbound data", ex);
        }
    }
}
