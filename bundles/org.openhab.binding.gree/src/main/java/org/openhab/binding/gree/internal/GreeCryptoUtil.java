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

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    public static byte[] getAESGeneralKeyByteArray() {
        return AES_KEY.getBytes(StandardCharsets.UTF_8);
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
}
