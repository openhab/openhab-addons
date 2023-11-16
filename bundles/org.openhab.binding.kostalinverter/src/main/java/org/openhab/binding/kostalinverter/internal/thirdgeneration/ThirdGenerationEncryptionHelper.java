/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

import static org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationBindingConstants.*;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The {@link ThirdGenerationEncryptionHelper} is responsible for handling the encryption for the authentication
 * handlers.
 *
 * @author René Stakemeier - Initial contribution
 */
final class ThirdGenerationEncryptionHelper {

    private ThirdGenerationEncryptionHelper() {
    }

    /**
     * This method generates the HMACSha256 encrypted value of the given value
     *
     * @param password Password used for encryption
     * @param valueToEncrypt value to encrypt
     * @return encrypted value
     * @throws InvalidKeyException thrown if the key generated from the password is invalid
     * @throws NoSuchAlgorithmException thrown if HMAC SHA 256 is not supported
     */
    static byte[] getHMACSha256(byte[] password, String valueToEncrypt)
            throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKeySpec signingKey = new SecretKeySpec(password, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        mac.update(valueToEncrypt.getBytes());
        return mac.doFinal();
    }

    /**
     * This methods generates the client proof.
     * It is calculated as XOR between the {@link clientSignature} and the {@link serverSignature}
     *
     * @param clientSignature client signature
     * @param serverSignature server signature
     * @return client proof
     */
    static String createClientProof(byte[] clientSignature, byte[] serverSignature) {
        byte[] result = new byte[clientSignature.length];
        for (int i = 0; i < clientSignature.length; i++) {
            result[i] = (byte) (0xff & (clientSignature[i] ^ serverSignature[i]));
        }
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * Create the PBKDF2 hash
     *
     * @param password password
     * @param salt salt
     * @param rounds rounds
     * @return hash
     * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA256 is not supported
     * @throws InvalidKeySpecException if the key specification is not supported
     */
    static byte[] getPBKDF2Hash(String password, byte[] salt, int rounds)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Create the SHA256 hash value for the given byte array
     *
     * @param valueToHash byte array to get the hash value for
     * @return the hash value
     * @throws NoSuchAlgorithmException if SHA256 is not supported
     */
    static byte[] getSha256Hash(byte[] valueToHash) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(SHA_256_HASH).digest(valueToHash);
    }

    /**
     * Create the nonce (numbers used once) for the client for communication
     *
     * @return nonce
     */
    static String createClientNonce() {
        Random generator = new SecureRandom();

        // Randomize the random generator
        byte[] randomizeArray = new byte[1024];
        generator.nextBytes(randomizeArray);

        // 3 words of 4 bytes are required for the handshake
        byte[] nonceArray = new byte[12];
        generator.nextBytes(nonceArray);

        // return the base64 encoded value of the random words
        return Base64.getMimeEncoder().encodeToString(nonceArray);
    }
}
