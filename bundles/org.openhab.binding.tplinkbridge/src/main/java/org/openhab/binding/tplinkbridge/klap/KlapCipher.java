/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tplinkbridge.klap;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * AES cipher of an established KLAP session. Key, IV and signature key are derived from both handshake seeds and the
 * auth hash. Every request uses the next sequence number, and the device answers with the same one.
 *
 * @author Christopher Smith - Initial contribution, derived from the tapocontrol KLAP cipher by Christian Wild
 */
@NonNullByDefault
class KlapCipher {
    static final int SIGNATURE_LENGTH = 32;

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final byte[] key;
    private final byte[] ivPrefix;
    private final byte[] signatureKey;
    private int sequence;

    KlapCipher(byte[] localSeed, byte[] remoteSeed, byte[] authHash) throws GeneralSecurityException {
        key = Arrays.copyOf(derive("lsk", localSeed, remoteSeed, authHash), 16);
        byte[] ivHash = derive("iv", localSeed, remoteSeed, authHash);
        ivPrefix = Arrays.copyOf(ivHash, 12);
        sequence = ByteBuffer.wrap(ivHash, ivHash.length - 4, 4).getInt();
        signatureKey = Arrays.copyOf(derive("ldk", localSeed, remoteSeed, authHash), 28);
    }

    /**
     * Advances to the next sequence number and returns it. Call before {@link #encrypt(String, int)}.
     */
    int nextSequence() {
        return ++sequence;
    }

    /**
     * Encrypts a request and prefixes it with its signature.
     */
    byte[] encrypt(String payload, int seq) throws GeneralSecurityException {
        byte[] cipherText = cipher(Cipher.ENCRYPT_MODE, seq).doFinal(payload.getBytes(StandardCharsets.UTF_8));
        byte[] signature = sha256(signatureKey, sequenceBytes(seq), cipherText);
        return concat(signature, cipherText);
    }

    /**
     * Checks the signature of a message for the request with the given sequence number and decrypts it.
     */
    String decrypt(byte[] message, int seq) throws GeneralSecurityException {
        if (message.length < SIGNATURE_LENGTH) {
            throw new GeneralSecurityException("KLAP message shorter than its signature");
        }
        byte[] cipherText = Arrays.copyOfRange(message, SIGNATURE_LENGTH, message.length);
        if (!MessageDigest.isEqual(Arrays.copyOf(message, SIGNATURE_LENGTH),
                sha256(signatureKey, sequenceBytes(seq), cipherText))) {
            throw new GeneralSecurityException("KLAP message signature doesn't match");
        }
        byte[] plain = cipher(Cipher.DECRYPT_MODE, seq).doFinal(cipherText);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private Cipher cipher(int mode, int seq) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, new SecretKeySpec(key, "AES"), new IvParameterSpec(concat(ivPrefix, sequenceBytes(seq))));
        return cipher;
    }

    private static byte[] derive(String label, byte[] localSeed, byte[] remoteSeed, byte[] authHash)
            throws GeneralSecurityException {
        return sha256(label.getBytes(StandardCharsets.US_ASCII), localSeed, remoteSeed, authHash);
    }

    private static byte[] sequenceBytes(int seq) {
        return ByteBuffer.allocate(4).putInt(seq).array();
    }

    static byte[] sha256(byte[]... parts) throws GeneralSecurityException {
        return digest("SHA-256", parts);
    }

    static byte[] sha1(byte[] value) throws GeneralSecurityException {
        return digest("SHA-1", value);
    }

    static byte[] md5(byte[]... parts) throws GeneralSecurityException {
        return digest("MD5", parts);
    }

    private static byte[] digest(String algorithm, byte[]... parts) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        for (byte[] part : parts) {
            digest.update(part);
        }
        return digest.digest();
    }

    static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (byte[] part : parts) {
            buffer.put(part);
        }
        return buffer.array();
    }
}
