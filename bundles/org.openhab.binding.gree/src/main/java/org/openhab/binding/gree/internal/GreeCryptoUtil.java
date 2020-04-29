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
package org.openhab.binding.gree.internal;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
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
    static String AES_General_Key = "a3K8Bx%2r8Y7#xDh";

    public static String GetAESGeneralKey() {
        return AES_General_Key;
    }

    public static byte[] GetAESGeneralKeyByteArray() {
        return AES_General_Key.getBytes();
    }

    public static String decryptPack(byte[] keyarray, String message) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyarray, "AES");
            // BASE64Decoder decoder = new BASE64Decoder();
            Base64.Decoder decoder = Base64.getDecoder();
            // Decoder decoder = new Decoder();
            byte[] imageByte = decoder.decode(message);
            // byte[] imageByte = decoder.decodeBuffer(message);

            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytePlainText = aesCipher.doFinal(imageByte);

            return new String(bytePlainText);
        } catch (Exception ex) {
            throw new GreeException(ex, "Decryption of recieved data failed");
        }
    }

    public static String encryptPack(byte[] keyarray, String message) throws GreeException {
        try {
            Key key = new SecretKeySpec(keyarray, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytePlainText = aesCipher.doFinal(message.getBytes());

            Base64.Encoder newencoder = Base64.getEncoder();
            String encrytpedMessage = new String(newencoder.encode(bytePlainText));
            return encrytpedMessage.substring(0, encrytpedMessage.length());
        } catch (Exception ex) {
            throw new GreeException(ex, "Unable to encrypt outbound data");
        }
    }
}
